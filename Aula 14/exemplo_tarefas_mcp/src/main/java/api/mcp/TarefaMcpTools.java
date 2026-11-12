// Pacote da "porta MCP" da aplicação (Aula 14).
package api.mcp;

// Importa o tipo de data do prazo.
import java.time.LocalDate;
// Importa o erro que o LocalDate.parse lança quando o texto não é uma data.
import java.time.format.DateTimeParseException;
// Importa a lista devolvida nas consultas.
import java.util.List;
// Importa o conjunto de erros de validação.
import java.util.Set;
// Importa o junta-textos usado na mensagem de erro.
import java.util.stream.Collectors;

// Importa as anotações do MCP: @McpTool marca um método como ferramenta,
// @McpToolParam descreve cada parâmetro para a IA.
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
// Importa a anotação que registra a classe como bean.
import org.springframework.stereotype.Component;

// Importa o validador do Bean Validation (o mesmo que o @Valid usa no Controller).
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

// Importa os DTOs: as ferramentas usam o MESMO contrato da API REST.
import api.dto.EstatisticasDTO;
import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
// Importa o Service: é ele quem tem as regras de negócio.
import api.service.TarefaService;

// =====================================================================
// AULA 14: as FERRAMENTAS (tools) que o servidor MCP oferece para a IA.
// =====================================================================
//
// Pense nesta classe como um "Controller para a IA":
//
//   Navegador/Postman ──HTTP/JSON──> TarefaController ──┐
//                                                       ├──> TarefaService ──> banco
//   Assistente de IA  ──MCP (/mcp)─> TarefaMcpTools  ───┘
//
// - O Controller transforma uma ROTA HTTP em chamada ao Service.
// - Esta classe transforma uma FERRAMENTA MCP em chamada ao Service.
// Nenhuma regra de negócio mora aqui: só traduzimos e repassamos.
//
// Como a IA "enxerga" estas ferramentas? Quando o assistente se conecta em
// http://localhost:8080/mcp, ele pede a lista (tools/list) e recebe, para cada
// método anotado com @McpTool: o nome, a descrição e o formato dos parâmetros
// (um JSON Schema gerado a partir dos tipos Java e das @McpToolParam).
// É LENDO A DESCRIÇÃO que o modelo decide qual ferramenta usar. Por isso as
// descrições abaixo são escritas para a IA: dizem o que faz, quando usar e o
// formato de cada campo.
//
// @Component: o Spring cria um objeto desta classe e o servidor MCP do
// Spring AI procura nele os métodos com @McpTool (annotation scanner).
@Component
public class TarefaMcpTools {

    // As regras de negócio (as mesmas do REST).
    private final TarefaService service;

    // Validador do Bean Validation. No Controller, quem chama é o @Valid;
    // aqui não existe @Valid, então chamamos o validador "na mão".
    private final Validator validator;

    // Injeção pelo construtor, igual a todo o resto do projeto.
    public TarefaMcpTools(TarefaService service, Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    // ------------------------------------------------------------------
    // 1) LISTAR: só lê. As "annotations" são DICAS para o cliente de IA:
    //    readOnlyHint = true      → não altera nada (alguns clientes nem pedem confirmação);
    //    destructiveHint = false  → não apaga nem sobrescreve dados (o padrão é true!);
    //    openWorldHint = false    → só mexe no nosso banco, não na internet (o padrão é true).
    // ------------------------------------------------------------------
    @McpTool(name = "listar_tarefas",
            description = "Lista as tarefas cadastradas, ordenadas pelo prazo. "
                    + "Use 'situacao' para filtrar: todas, abertas, concluidas ou atrasadas. "
                    + "Use 'responsavel' para ver só as tarefas de uma pessoa (nome exato, sem diferenciar maiúsculas).",
            annotations = @McpTool.McpAnnotations(title = "Listar tarefas", readOnlyHint = true,
                    destructiveHint = false, openWorldHint = false))
    public List<TarefaResponseDTO> listarTarefas(
            @McpToolParam(description = "Filtro: todas (padrão), abertas, concluidas ou atrasadas", required = false)
            String situacao,
            @McpToolParam(description = "Nome do responsável, por exemplo Ana. Vazio = todos", required = false)
            String responsavel) {

        // Começa pela lista certa: de um responsável ou de todos.
        List<TarefaResponseDTO> tarefas = (responsavel == null || responsavel.isBlank())
                ? service.listarTodas()
                : service.buscarPorResponsavel(responsavel.trim());

        // Sem filtro (ou "todas"): devolve como está.
        String filtro = situacao == null || situacao.isBlank() ? "todas" : situacao.trim().toLowerCase();

        // Aplica o filtro de situação sobre os campos que o DTO já calcula.
        return switch (filtro) {
            case "todas" -> tarefas;
            case "abertas" -> tarefas.stream().filter(t -> !t.concluida()).toList();
            case "concluidas", "concluídas" -> tarefas.stream().filter(TarefaResponseDTO::concluida).toList();
            case "atrasadas" -> tarefas.stream().filter(TarefaResponseDTO::atrasada).toList();
            // Valor inesperado: a mensagem volta para a IA, que corrige e tenta de novo.
            default -> throw new IllegalArgumentException(
                    "situacao inválida: '" + situacao + "'. Use todas, abertas, concluidas ou atrasadas.");
        };
    }

    // ------------------------------------------------------------------
    // 2) BUSCAR UMA: também só lê.
    // ------------------------------------------------------------------
    @McpTool(name = "buscar_tarefa",
            description = "Busca uma tarefa pelo id e devolve todos os campos, inclusive dias restantes e se está atrasada.",
            annotations = @McpTool.McpAnnotations(title = "Buscar tarefa", readOnlyHint = true,
                    destructiveHint = false, openWorldHint = false))
    public TarefaResponseDTO buscarTarefa(
            @McpToolParam(description = "Id da tarefa (número inteiro)", required = true) Long id) {
        // Id inexistente: o Service lança TarefaNaoEncontradaException
        // e a mensagem ("Tarefa 99 não encontrada") volta para a IA como erro.
        return service.buscarPorId(id);
    }

    // ------------------------------------------------------------------
    // 3) CADASTRAR: altera o banco. readOnlyHint = false (o padrão) e
    //    destructiveHint = false: cria, mas não apaga nada.
    // ------------------------------------------------------------------
    @McpTool(name = "cadastrar_tarefa",
            description = "Cadastra uma nova tarefa e devolve a tarefa criada, já com o id. "
                    + "Todos os campos são obrigatórios. O prazo é uma data no formato yyyy-MM-dd, de hoje em diante. "
                    + "Se o usuário disser 'sexta que vem' ou 'amanhã', converta para a data antes de chamar.",
            annotations = @McpTool.McpAnnotations(title = "Cadastrar tarefa", readOnlyHint = false,
                    destructiveHint = false, openWorldHint = false))
    public TarefaResponseDTO cadastrarTarefa(
            @McpToolParam(description = "O que precisa ser feito, por exemplo 'Estudar MCP'", required = true)
            String titulo,
            @McpToolParam(description = "Nome de quem vai fazer a tarefa, por exemplo 'Ana'", required = true)
            String responsavel,
            @McpToolParam(description = "Data limite no formato yyyy-MM-dd, por exemplo 2026-12-01", required = true)
            String dataPrazo,
            @McpToolParam(description = "Prioridade de 1 (mais baixa) a 5 (mais alta)", required = true)
            Integer prioridade) {

        // A data chega como texto: convertemos aqui para dar uma mensagem clara
        // se a IA mandar outro formato (ex.: "01/12/2026").
        LocalDate prazo;
        try {
            prazo = LocalDate.parse(dataPrazo);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "dataPrazo inválida: '" + dataPrazo + "'. Use o formato yyyy-MM-dd, por exemplo 2026-12-01.");
        }

        // Monta o MESMO DTO que o POST /tarefas recebe (sem vínculo com responsável cadastrado).
        TarefaRequestDTO dto = new TarefaRequestDTO(titulo, responsavel, prazo, prioridade, null);

        // Valida com as anotações do DTO (@NotBlank, @FutureOrPresent, @Min, @Max).
        // É a regra de ouro desta aula: TODA porta de entrada valida. A IA também erra.
        validar(dto);

        // Daqui em diante é igual ao REST: salva, publica o evento no RabbitMQ, conta a métrica.
        return service.criar(dto);
    }

    // ------------------------------------------------------------------
    // 4) CONCLUIR: altera o banco. idempotentHint = true: chamar duas vezes
    //    tem o mesmo efeito de chamar uma (a tarefa continua concluída).
    // ------------------------------------------------------------------
    @McpTool(name = "concluir_tarefa",
            description = "Marca uma tarefa como concluída. Se ela já estiver concluída, nada muda e a tarefa é devolvida como está.",
            annotations = @McpTool.McpAnnotations(title = "Concluir tarefa", readOnlyHint = false,
                    destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public TarefaResponseDTO concluirTarefa(
            @McpToolParam(description = "Id da tarefa a concluir", required = true) Long id) {
        // Busca primeiro: o PUT /tarefas/{id}/concluir do REST ALTERNA (conclui/reabre).
        // Para a IA isso seria perigoso: "concluir" duas vezes reabriria a tarefa.
        TarefaResponseDTO atual = service.buscarPorId(id);
        if (atual.concluida()) {
            return atual;
        }
        return service.alternarConcluida(id);
    }

    // ------------------------------------------------------------------
    // 5) ESTATÍSTICAS: só lê. Mesmo cálculo do GET /tarefas/estatisticas.
    // ------------------------------------------------------------------
    @McpTool(name = "estatisticas_tarefas",
            description = "Devolve os números gerais das tarefas: total, abertas, concluídas, atrasadas, "
                    + "percentual concluído, abertas por responsável, abertas por prioridade e o próximo prazo. "
                    + "Use para responder perguntas como 'quem está mais sobrecarregado?' ou 'como está o andamento?'.",
            annotations = @McpTool.McpAnnotations(title = "Estatísticas das tarefas", readOnlyHint = true,
                    destructiveHint = false, openWorldHint = false))
    public EstatisticasDTO estatisticasTarefas() {
        return service.estatisticas();
    }

    // Roda o Bean Validation e, se houver erro, junta as mensagens numa só.
    // A exceção vira um resultado de erro do MCP (isError = true), com este texto:
    // a IA lê, entende o que corrigir e normalmente tenta de novo sozinha.
    private void validar(TarefaRequestDTO dto) {
        Set<ConstraintViolation<TarefaRequestDTO>> erros = validator.validate(dto);
        if (!erros.isEmpty()) {
            String mensagem = erros.stream()
                    .map(e -> e.getPropertyPath() + ": " + e.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Dados inválidos. " + mensagem);
        }
    }
}
