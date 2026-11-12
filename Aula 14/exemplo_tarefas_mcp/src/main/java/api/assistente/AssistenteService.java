// Pacote do assistente da tela (Aula 14).
package api.assistente;

// Importa datas, listas e o "dicionário" dos argumentos.
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

// Importa a configuração e o "ambiente" do Spring (para descobrir a porta).
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

// Importa o Jackson, que converte JSON <-> objetos Java.
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// O CLIENTE MCP oficial do SDK Java: o mesmo que o teste McpE2ETest usa.
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

// Importa os DTOs da tela.
import api.dto.AssistentePedidoDTO;
import api.dto.AssistenteRespostaDTO;
import api.dto.AssistenteStatusDTO;
import api.dto.PassoAssistenteDTO;

// =====================================================================
// AULA 14: o HOST MCP da tela do assistente.
// =====================================================================
//
// Na Parte 1 vimos três papéis: o HOST (o app que junta modelo e
// ferramentas), o CLIENTE MCP e o SERVIDOR MCP. No Claude Code, o host é o
// próprio Claude Code. Na tela assistente.html, o host é ESTA classe:
//
//   tela ──POST /assistente──> AssistenteService (host)
//                                 ├──> modelo (Ollama ou OpenRouter): "o que fazer agora?"
//                                 └──> cliente MCP ──POST /mcp──> TarefaMcpTools ──> TarefaService
//
// O ciclo é o do slide "Uma conversa com ferramentas, passo a passo":
//   1. Conectar no servidor MCP e pegar a lista de ferramentas (tools/list).
//   2. Mandar ao modelo a conversa + a lista de ferramentas.
//   3. Se o modelo responder com TEXTO, acabou.
//   4. Se ele pedir FERRAMENTAS, o host executa cada uma pelo MCP (tools/call),
//      junta o resultado na conversa e volta ao passo 2.
//
// Por que passar pelo MCP, se o TarefaService está aqui do lado? Para a tela
// usar EXATAMENTE as mesmas ferramentas (descrições, validação, dicas) que o
// Claude Code ou o Copilot usam. Uma ferramenta nova em TarefaMcpTools
// aparece na tela sem mudar uma linha deste arquivo.
@Service
public class AssistenteService {

    private final ModeloClient modelos;
    private final ObjectMapper json;
    private final Environment ambiente;
    private final String urlMcp;
    private final int maxRodadas;

    public AssistenteService(ModeloClient modelos, ObjectMapper json, Environment ambiente,
                             @Value("${assistente.mcp-url:}") String urlMcp,
                             @Value("${assistente.max-rodadas:6}") int maxRodadas) {
        this.modelos = modelos;
        this.json = json;
        this.ambiente = ambiente;
        this.urlMcp = urlMcp;
        this.maxRodadas = maxRodadas;
    }

    // Para a tela saber se o servidor de modelos está no ar e quais modelos ele tem.
    public AssistenteStatusDTO status() {
        try {
            return new AssistenteStatusDTO(true, modelos.url(), modelos.modeloPadrao(), modelos.listarModelos(), null);
        } catch (AssistenteIndisponivelException e) {
            return new AssistenteStatusDTO(false, modelos.url(), modelos.modeloPadrao(), List.of(), e.getMessage());
        }
    }

    public AssistenteRespostaDTO conversar(AssistentePedidoDTO pedido) {
        String modelo = pedido.modelo() == null || pedido.modelo().isBlank()
                ? modelos.modeloPadrao() : pedido.modelo().trim();

        // try-with-resources: o cliente MCP é fechado no fim, dê certo ou não.
        try (McpSyncClient mcp = conectar()) {

            // PASSO 1: a lista de ferramentas, traduzida para o formato do chat completions.
            List<ChatCompletions.Ferramenta> ferramentas = mcp.listTools().tools().stream()
                    // "Só leitura" na tela: o modelo nem fica sabendo das ferramentas que alteram dados.
                    .filter(t -> !pedido.somenteLeitura() || somenteLeitura(t))
                    .map(this::traduzir)
                    .toList();

            // A conversa começa com as instruções gerais (system) e segue com o histórico da tela.
            List<ChatCompletions.Mensagem> conversa = new ArrayList<>();
            conversa.add(ChatCompletions.Mensagem.sistema(instrucoes(pedido.somenteLeitura(), mcp)));
            pedido.mensagens().forEach(m -> conversa.add("assistente".equals(m.papel())
                    ? ChatCompletions.Mensagem.assistente(m.texto())
                    : ChatCompletions.Mensagem.usuario(m.texto())));

            List<PassoAssistenteDTO> passos = new ArrayList<>();

            // PASSOS 2 a 4, com um limite: um modelo confuso poderia pedir ferramentas para sempre.
            for (int rodada = 1; rodada <= maxRodadas; rodada++) {
                ChatCompletions.Resposta resposta = modelos.conversar(modelo, conversa, ferramentas);
                ChatCompletions.Mensagem mensagem = resposta.choices().get(0).message();
                String quemRespondeu = resposta.model() != null ? resposta.model() : modelo;

                // PASSO 3: respondeu em texto. Fim.
                if (mensagem == null || !mensagem.pediuFerramentas()) {
                    String texto = mensagem == null || mensagem.content() == null || mensagem.content().isBlank()
                            ? "(o modelo não devolveu texto)" : mensagem.content().trim();
                    return new AssistenteRespostaDTO(texto, quemRespondeu, passos);
                }

                // PASSO 4: o pedido de ferramentas entra na conversa (o modelo precisa
                // ver o próprio pedido junto com os resultados) e cada ferramenta é executada.
                conversa.add(mensagem);
                for (ChatCompletions.ChamadaFerramenta chamada : mensagem.toolCalls()) {
                    PassoAssistenteDTO passo = executar(mcp, chamada);
                    passos.add(passo);
                    // O resultado vai de volta, ligado ao pedido pelo id. Erro também vai:
                    // é com a mensagem de erro que o modelo corrige os argumentos.
                    conversa.add(ChatCompletions.Mensagem.resultadoDeFerramenta(chamada.id(), passo.resultado()));
                }
            }
            throw new AssistenteIndisponivelException("O modelo pediu ferramentas " + maxRodadas
                    + " vezes seguidas sem responder. Tente reformular o pedido ou outro modelo.");
        }
    }

    // Abre um cliente MCP apontado para o NOSSO servidor (/mcp) e faz o aperto de mão.
    private McpSyncClient conectar() {
        String base = urlMcp.isBlank()
                // Sem configuração: a própria aplicação, na porta em que ela subiu.
                ? "http://localhost:" + ambiente.getProperty("local.server.port", ambiente.getProperty("server.port", "8080"))
                : urlMcp;
        var transporte = HttpClientStreamableHttpTransport.builder(base).endpoint("/mcp").build();
        McpSyncClient cliente = McpClient.sync(transporte)
                .requestTimeout(Duration.ofSeconds(30))
                .build();
        cliente.initialize();
        return cliente;
    }

    // Ferramenta MCP → ferramenta do chat completions. O JSON Schema passa direto:
    // os dois lados usam o mesmo formato para descrever os parâmetros.
    private ChatCompletions.Ferramenta traduzir(Tool ferramenta) {
        Map<String, Object> parametros = json.convertValue(ferramenta.inputSchema(), new TypeReference<>() {
        });
        return new ChatCompletions.Ferramenta("function",
                new ChatCompletions.DefinicaoFerramenta(ferramenta.name(), ferramenta.description(), parametros));
    }

    // Usa a dica readOnlyHint que o servidor declarou em cada @McpTool.
    private boolean somenteLeitura(Tool ferramenta) {
        return ferramenta.annotations() != null && Boolean.TRUE.equals(ferramenta.annotations().readOnlyHint());
    }

    // Executa UMA ferramenta pelo MCP (tools/call) e guarda o que aconteceu.
    private PassoAssistenteDTO executar(McpSyncClient mcp, ChatCompletions.ChamadaFerramenta chamada) {
        String nome = chamada.function().name();
        String argumentosTexto = chamada.function().arguments();

        // "arguments" é um TEXTO com JSON dentro: convertemos para Map.
        Map<String, Object> argumentos;
        try {
            argumentos = argumentosTexto == null || argumentosTexto.isBlank()
                    ? Map.of()
                    : json.readValue(argumentosTexto, new TypeReference<>() {
                    });
        } catch (JsonProcessingException e) {
            // Modelos pequenos às vezes geram JSON quebrado. Avisamos o modelo para ele tentar de novo.
            return new PassoAssistenteDTO(nome, argumentosTexto,
                    "Os argumentos não são um JSON válido: " + e.getOriginalMessage(), true);
        }

        CallToolResult resultado = mcp.callTool(new CallToolRequest(nome, argumentos));
        String texto = resultado.content().stream()
                .filter(TextContent.class::isInstance)
                .map(c -> ((TextContent) c).text())
                .collect(Collectors.joining("\n"));
        return new PassoAssistenteDTO(nome, argumentosTexto == null || argumentosTexto.isBlank() ? "{}" : argumentosTexto,
                texto, Boolean.TRUE.equals(resultado.isError()));
    }

    // As instruções gerais (mensagem "system"). Levam a data de HOJE e um
    // CALENDÁRIO dos próximos dias: sem isso, o modelo não sabe o que é "amanhã".
    //
    // Por que o calendário, e não só "hoje é quarta-feira"? Porque modelos pequenos
    // erram conta de datas: no teste, "sexta que vem" virou uma quarta-feira.
    // Com a tabela pronta, o modelo só precisa LER a data, não calcular. É a boa
    // prática "dê contexto" aplicada pelo próprio sistema.
    private String instrucoes(boolean somenteLeitura, McpSyncClient mcp) {
        LocalDate hoje = LocalDate.now();
        String diaDaSemana = nomeDoDia(hoje);
        String calendario = hoje.datesUntil(hoje.plusDays(15))
                .map(d -> "- " + nomeDoDia(d) + " " + d + (d.equals(hoje) ? " (hoje)" : d.equals(hoje.plusDays(1)) ? " (amanhã)" : ""))
                .collect(Collectors.joining("\n"));
        String doServidor = mcp.getServerInstructions() == null ? "" : mcp.getServerInstructions();
        return """
                Você é o assistente do sistema de tarefas da disciplina de Backend.
                Hoje é %s, %s.
                Calendário dos próximos dias (use para converter "amanhã", "sexta", "semana que vem"):
                %s
                Responda sempre em português do Brasil, de forma curta e direta.
                Use as ferramentas para consultar ou alterar as tarefas: nunca invente tarefas, ids ou números.
                Converta datas como "amanhã" ou "sexta que vem" para o formato yyyy-MM-dd antes de chamar uma ferramenta.
                Se uma ferramenta devolver erro, leia a mensagem, corrija os argumentos e tente de novo uma vez.
                Depois de cadastrar ou concluir, diga o que foi feito e o id da tarefa.
                %s
                Instruções do servidor de tarefas: %s
                """.formatted(diaDaSemana, hoje.format(DateTimeFormatter.ISO_LOCAL_DATE), calendario,
                somenteLeitura ? "Modo somente leitura: você só pode consultar. Se pedirem para alterar algo, explique que o modo está ligado." : "",
                doServidor);
    }

    // "quarta-feira", "sexta-feira"... no nosso idioma.
    private static String nomeDoDia(LocalDate data) {
        return data.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));
    }
}
