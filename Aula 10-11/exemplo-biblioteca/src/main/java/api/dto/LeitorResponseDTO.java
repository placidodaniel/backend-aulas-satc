// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// CAMADA: DTO
// RESPONSABILIDADE: o que a API devolve sobre um leitor.
// SITUAÇÃO: implementada. Será usado pelas rotas de leitores e dentro do
//           EmprestimoResponseDTO na próxima etapa.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Um leitor cadastrado")
public record LeitorResponseDTO(

        // Gerado pelo banco no cadastro.
        @Schema(description = "Id do leitor", example = "1")
        Long id,

        // O nome cadastrado.
        @Schema(description = "Nome do leitor", example = "Ana Souza")
        String nome,

        // O e-mail cadastrado.
        @Schema(description = "E-mail do leitor", example = "ana@exemplo.com")
        String email

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
