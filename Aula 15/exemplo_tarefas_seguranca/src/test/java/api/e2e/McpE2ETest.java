// Mesmo pacote dos outros testes E2E.
package api.e2e;

// Importa as asserções do AssertJ.
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// O cliente MCP OFICIAL do SDK Java: o mesmo tipo de cliente que um assistente de IA usa.
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import api.dto.EstatisticasDTO;

// Aula 14: teste E2E do servidor MCP.
// Sobe a aplicação inteira (com PostgreSQL e RabbitMQ de verdade, herdados do
// AbstractE2ETest) e conversa com /mcp usando um cliente MCP real.
// Não tem IA aqui: o teste faz o papel do assistente e chama as ferramentas direto.
class McpE2ETest extends AbstractE2ETest {


    private McpSyncClient cliente;

    @BeforeEach
    void conectar() {
        var transporte = HttpClientStreamableHttpTransport.builder("http://localhost:" + porta)
                .endpoint("/mcp")
                // Aula 15: o /mcp exige token, como as rotas REST.
                .customizeRequest(r -> r.header("Authorization", "Bearer " + token()))
                .build();
        cliente = McpClient.sync(transporte).build();
        // initialize + notifications/initialized: o "aperto de mão".
        cliente.initialize();
    }

    @AfterEach
    void desconectar() {
        cliente.closeGracefully();
    }

    // Lê o texto da primeira parte do resultado de uma ferramenta.
    private String texto(CallToolResult resultado) {
        return ((TextContent) resultado.content().get(0)).text();
    }

    @Test
    @DisplayName("initialize: o servidor se apresenta como 'tarefas' e manda as instruções")
    void apresentacao() {
        assertThat(cliente.getServerInfo().name()).isEqualTo("tarefas");
        assertThat(cliente.getServerInstructions()).contains("yyyy-MM-dd");
    }

    @Test
    @DisplayName("tools/list: as 5 ferramentas, com descrição e parâmetros obrigatórios")
    void listaFerramentas() {
        var ferramentas = cliente.listTools().tools();

        assertThat(ferramentas).extracting(Tool::name).containsExactlyInAnyOrder(
                "listar_tarefas", "buscar_tarefa", "cadastrar_tarefa", "concluir_tarefa", "estatisticas_tarefas");
        Tool cadastrar = ferramentas.stream().filter(t -> t.name().equals("cadastrar_tarefa")).findFirst().orElseThrow();
        assertThat(cadastrar.inputSchema().required())
                .containsExactlyInAnyOrder("titulo", "responsavel", "dataPrazo", "prioridade");
        assertThat(cadastrar.annotations().readOnlyHint()).isFalse();
    }

    @Test
    @DisplayName("cadastrar pelo MCP grava no banco: a tarefa aparece no GET /tarefas/{id} do REST")
    void cadastraEApareceNoRest() {
        String prazo = LocalDate.now().plusDays(3).toString();
        CallToolResult resultado = cliente.callTool(new CallToolRequest("cadastrar_tarefa",
                Map.of("titulo", "Criada pela IA", "responsavel", "Ana", "dataPrazo", prazo, "prioridade", 4)));

        assertThat(resultado.isError()).isFalse();
        // O texto é o JSON do TarefaResponseDTO: pegamos o id e conferimos pela porta REST.
        String id = texto(resultado).replaceAll("^\\{\"id\":(\\d+),.*$", "$1");
        var porRest = http.getForEntity("/tarefas/" + id, Map.class);
        assertThat(porRest.getStatusCode().value()).isEqualTo(200);
        assertThat(porRest.getBody()).containsEntry("titulo", "Criada pela IA");
    }

    @Test
    @DisplayName("dados inválidos: isError = true com a mensagem para a IA corrigir")
    void cadastroInvalido() {
        CallToolResult resultado = cliente.callTool(new CallToolRequest("cadastrar_tarefa",
                Map.of("titulo", "X", "responsavel", "Ana", "dataPrazo", "2020-01-01", "prioridade", 9)));

        assertThat(resultado.isError()).isTrue();
        assertThat(texto(resultado)).contains("Prioridade máxima é 5").contains("passado");
    }

    @Test
    @DisplayName("estatisticas_tarefas devolve o mesmo que GET /tarefas/estatisticas")
    void estatisticasIguaisAoRest() {
        String peloMcp = texto(cliente.callTool(new CallToolRequest("estatisticas_tarefas", Map.of())));
        EstatisticasDTO peloRest = http.getForObject("/tarefas/estatisticas", EstatisticasDTO.class);

        assertThat(peloMcp).contains("\"total\":" + peloRest.total());
    }

    @Test
    @DisplayName("recurso tarefas://regras e prompt planejar_semana")
    void recursoEPrompt() {
        var regras = cliente.readResource(new ReadResourceRequest("tarefas://regras"));
        assertThat(((TextResourceContents) regras.contents().get(0)).text()).contains("Regras de negócio");

        var prompt = cliente.getPrompt(new GetPromptRequest("planejar_semana", Map.of("responsavel", "Bruno")));
        assertThat(((TextContent) prompt.messages().get(0).content()).text()).contains("Bruno").contains("listar_tarefas");
    }
}
