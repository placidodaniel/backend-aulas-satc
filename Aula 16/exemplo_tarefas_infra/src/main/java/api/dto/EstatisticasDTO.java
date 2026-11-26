// Declara o pacote dos DTOs: as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data do próximo prazo.
import java.time.LocalDate;
// Importa o "dicionário" chave → quantidade.
import java.util.Map;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Aula 14: o resumo numérico das tarefas.
//
// O MESMO DTO sai por duas portas:
//   - GET /tarefas/estatisticas  (REST, para o navegador, o Postman, o Swagger);
//   - ferramenta MCP "estatisticas_tarefas" (para o assistente de IA).
// Quem calcula é o TarefaService: as duas portas só repassam.
@Schema(description = "Números gerais das tarefas cadastradas")
public record EstatisticasDTO(

        // Quantidade total de tarefas no banco.
        @Schema(description = "Total de tarefas cadastradas", example = "12")
        long total,

        // Tarefas ainda não concluídas (inclui as atrasadas).
        @Schema(description = "Tarefas não concluídas", example = "7")
        long abertas,

        // Tarefas concluídas.
        @Schema(description = "Tarefas concluídas", example = "5")
        long concluidas,

        // Abertas com o prazo antes de hoje.
        @Schema(description = "Tarefas abertas com o prazo vencido", example = "2")
        long atrasadas,

        // concluidas / total, de 0 a 100, com uma casa decimal.
        @Schema(description = "Percentual de tarefas concluídas (0 a 100)", example = "41.7")
        double percentualConcluido,

        // Quantas tarefas ABERTAS cada responsável tem (nome → quantidade).
        @Schema(description = "Tarefas abertas por responsável", example = "{\"Ana\": 3, \"Bruno\": 4}")
        Map<String, Long> abertasPorResponsavel,

        // Quantas tarefas ABERTAS há em cada prioridade (1 a 5).
        @Schema(description = "Tarefas abertas por prioridade (1 a 5)", example = "{\"1\": 0, \"5\": 2}")
        Map<Integer, Long> abertasPorPrioridade,

        // O prazo mais próximo entre as abertas que ainda não venceram (null se não houver).
        @Schema(description = "Prazo mais próximo entre as tarefas abertas e não vencidas", example = "2026-11-20")
        LocalDate proximoPrazo

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
