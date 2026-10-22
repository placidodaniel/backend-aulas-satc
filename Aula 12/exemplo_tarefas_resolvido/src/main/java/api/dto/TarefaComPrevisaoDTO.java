// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 2: DTO COMPOSTO -- uma tarefa (do NOSSO banco) com a previsão do
// tempo para o prazo dela (do Open-Meteo). Dado próprio + dado externo numa
// resposta só, como o ResponsavelDetalheDTO da Aula 09 juntava o responsável
// com as tarefas dele.
//
// Quando não há previsão para o prazo (já passou, ou é longe demais), a
// tarefa vem do mesmo jeito: "previsao" vem null e "aviso" explica o motivo.
// É um FALLBACK: o pedido não falha por causa de uma parte que não existe.
// Exercício 5: a mesma forma serve para cada item de GET /tarefas/chuvosas.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Uma tarefa com a previsão do tempo para o prazo dela")
public record TarefaComPrevisaoDTO(

        // A tarefa, no mesmo formato do GET /tarefas/{id}.
        @Schema(description = "A tarefa, igual ao GET /tarefas/{id}")
        TarefaResponseDTO tarefa,

        // A previsão do dia do prazo, ou null.
        @Schema(description = "Previsão para o dia do prazo; null quando não há previsão para esse dia")
        PrevisaoDTO previsao,

        // Por que não veio previsão (ou null, quando veio).
        @Schema(description = "Motivo de não haver previsão; null quando a previsão veio", example = "null")
        String aviso

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
