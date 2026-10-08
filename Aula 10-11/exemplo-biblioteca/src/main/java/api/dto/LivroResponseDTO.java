// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// CAMADA: DTO
// RESPONSABILIDADE: o que a API devolve sobre um livro.
// SITUAÇÃO: implementada.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Um livro do acervo")
public record LivroResponseDTO(

        // Gerado pelo banco no cadastro.
        @Schema(description = "Id do livro", example = "1")
        Long id,

        // O título cadastrado.
        @Schema(description = "Título do livro", example = "Dom Casmurro")
        String titulo,

        // O ISBN cadastrado.
        @Schema(description = "ISBN do livro", example = "978-85-359-0277-5")
        String isbn,

        // Verdadeiro quando o exemplar está na estante.
        @Schema(description = "Se o exemplar está disponível para empréstimo", example = "true")
        boolean disponivel

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
