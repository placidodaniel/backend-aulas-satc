// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 5: um erro de validação -- qual campo e o que há de errado nele.
// Sai dentro da lista "campos" do ErroDTO.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Erro de validação de um campo do corpo da requisição")
public record CampoErroDTO(

        // Nome do campo no JSON (o mesmo do componente do record).
        @Schema(description = "Nome do campo no JSON", example = "titulo")
        String campo,

        // O "message" da anotação de validação que falhou.
        @Schema(description = "Mensagem da validação que falhou", example = "Título é obrigatório")
        String mensagem

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
