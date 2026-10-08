// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
// Importa a validação de campo obrigatório.
import jakarta.validation.constraints.NotNull;

// CAMADA: DTO
// RESPONSABILIDADE: o que o cliente envia para registrar um empréstimo. Entram
//                   só os ids: nome do leitor e título do livro já estão no banco.
//                   A data de retirada não entra: quem decide é a API.
// SITUAÇÃO: implementada.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Dados para registrar um empréstimo")
public record EmprestimoRequestDTO(

        // Quem está pegando o livro.
        @Schema(description = "Id do leitor", example = "1")
        @NotNull(message = "O id do leitor é obrigatório")
        Long leitorId,

        // Qual livro está saindo.
        @Schema(description = "Id do livro", example = "1")
        @NotNull(message = "O id do livro é obrigatório")
        Long livroId

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
