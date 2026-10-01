// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data devolvido no JSON.
import java.time.LocalDate;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// >>> DTO DE SAÍDA (response): o JSON que a API DEVOLVE para o cliente.
//
// Na Aula 08, o Controller devolvia a entidade Tarefa e o Jackson transformava
// em JSON TUDO o que tivesse getter. Com este record, a resposta é uma decisão
// explícita: só sai o que está listado aqui, nesta ordem.
//
// Exercício 2: "diasRestantes" e "atrasada" não existem na entidade nem na
// tabela -- são CALCULADOS pelo TarefaMapper a cada resposta. O DTO de saída
// não precisa ser um espelho da tabela: ele pode ter menos campos (esconder)
// ou mais campos (derivar).
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Tarefa como a API devolve para o cliente")
public record TarefaResponseDTO(

        // Gerado pelo banco no INSERT -- por isso existe aqui e não no request.
        @Schema(description = "Identificador gerado pelo banco", example = "1")
        Long id,

        // Título atual da tarefa.
        @Schema(description = "Título da tarefa", example = "Estudar DTO e Swagger")
        String titulo,

        // Estado controlado pela API (PUT /tarefas/{id}/concluir).
        @Schema(description = "true quando a tarefa já foi concluída", example = "false")
        boolean concluida,

        // Nome do responsável em texto livre.
        @Schema(description = "Nome de quem vai fazer a tarefa", example = "Ana")
        String responsavel,

        // Prazo, no mesmo formato yyyy-MM-dd da entrada.
        @Schema(description = "Data limite (yyyy-MM-dd)", example = "2026-12-01")
        LocalDate dataPrazo,

        // Preenchida pela entidade no INSERT -- o cliente só lê.
        @Schema(description = "Data em que a tarefa foi cadastrada (yyyy-MM-dd)", example = "2026-10-06")
        LocalDate dataCadastro,

        // Prioridade de 1 a 5.
        @Schema(description = "Prioridade de 1 (mais baixa) a 5 (mais alta)", example = "3")
        int prioridade,

        // Exercício 2: dias entre hoje e o prazo. Negativo = o prazo já passou.
        @Schema(description = "Dias de hoje até o prazo: 0 = vence hoje, negativo = prazo já passou", example = "7")
        long diasRestantes,

        // Exercício 2: pendente e com prazo antes de hoje.
        @Schema(description = "true quando a tarefa não foi concluída e o prazo já passou", example = "false")
        boolean atrasada,

        // Outro DTO, e não a entidade Responsavel: DTO de saída só aponta para
        // DTOs de saída. Vem null quando a tarefa não tem responsável vinculado.
        @Schema(description = "Responsável cadastrado, ou null se a tarefa não tiver vínculo")
        ResponsavelResponseDTO responsavelVinculado

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
