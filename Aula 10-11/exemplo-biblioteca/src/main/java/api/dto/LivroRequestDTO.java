// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
// Importa a validação de texto obrigatório.
import jakarta.validation.constraints.NotBlank;
// Importa a validação de tamanho máximo.
import jakarta.validation.constraints.Size;

// CAMADA: DTO
// RESPONSABILIDADE: o que o cliente envia para cadastrar um livro. Não tem id
//                   nem disponivel: esses campos quem decide é a API.
// SITUAÇÃO: implementada.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Dados para cadastrar um livro")
public record LivroRequestDTO(

        // Título obrigatório.
        @Schema(description = "Título do livro", example = "Dom Casmurro")
        @NotBlank(message = "Título é obrigatório")
        String titulo,

        // ISBN obrigatório, com o mesmo limite da coluna no banco.
        @Schema(description = "ISBN do livro", example = "978-85-359-0277-5")
        @NotBlank(message = "ISBN é obrigatório")
        @Size(max = 20, message = "ISBN tem no máximo 20 caracteres")
        String isbn

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
