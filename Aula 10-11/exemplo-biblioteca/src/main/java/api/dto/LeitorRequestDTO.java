// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
// Importa a validação de formato de e-mail.
import jakarta.validation.constraints.Email;
// Importa a validação de texto obrigatório.
import jakarta.validation.constraints.NotBlank;

// CAMADA: DTO
// RESPONSABILIDADE: o que o cliente envia para cadastrar um leitor.
// SITUAÇÃO: implementada.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Dados para cadastrar um leitor")
public record LeitorRequestDTO(

        // Nome obrigatório.
        @Schema(description = "Nome do leitor", example = "Ana Souza")
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        // E-mail obrigatório e em formato válido.
        @Schema(description = "E-mail do leitor", example = "ana@exemplo.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
