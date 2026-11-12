// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
// Importa a validação que rejeita valores nulos.
import jakarta.validation.constraints.NotNull;

// Corpo do PUT /tarefas/{id}/responsavel -- só o id do responsável. O nome e o
// e-mail já estão no banco; o cliente não precisa (nem deve) reenviá-los.
// Na Aula 08 era uma classe com getter/setter; como é um DTO de entrada da
// rota de tarefas, virou record junto com o TarefaRequestDTO.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Qual responsável cadastrado deve ser ligado à tarefa")
public record VinculoResponsavelDTO(

        // Id de um responsável que já existe em /responsaveis.
        @Schema(description = "Id de um responsável cadastrado", example = "1")
        @NotNull(message = "responsavelId é obrigatório")
        Long responsavelId

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
