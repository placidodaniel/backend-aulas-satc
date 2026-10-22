// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data devolvido no JSON.
import java.time.LocalDate;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 4: versão ENXUTA de uma tarefa, para aparecer dentro de outro DTO
// (a lista "tarefas" do ResponsavelDetalheDTO).
//
// Não tem "responsavelVinculado": a tarefa já está dentro do responsável, e
// repetir o mesmo {id, nome, email} em cada item só aumentaria o JSON. Também
// é o que garante que não há ciclo: o resumo da tarefa não aponta de volta
// para o responsável.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Resumo de uma tarefa, usado dentro de outros DTOs")
public record TarefaResumoDTO(

        // Id da tarefa -- dá para chamar GET /tarefas/{id} se precisar do resto.
        @Schema(description = "Identificador da tarefa", example = "1")
        Long id,

        // Título da tarefa.
        @Schema(description = "Título da tarefa", example = "Estudar DTO e Swagger")
        String titulo,

        // Estado da tarefa.
        @Schema(description = "true quando a tarefa já foi concluída", example = "false")
        boolean concluida,

        // Prazo da tarefa.
        @Schema(description = "Data limite (yyyy-MM-dd)", example = "2026-12-01")
        LocalDate dataPrazo

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
