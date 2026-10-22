// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
// Importa a validação de formato de e-mail.
import jakarta.validation.constraints.Email;
// Importa a validação que rejeita texto nulo, vazio ou só com espaços.
import jakarta.validation.constraints.NotBlank;

// Exercício 1: o antigo ResponsavelDTO (classe com getter/setter) virou
// record, no mesmo modelo do TarefaRequestDTO. As validações não mudaram.
// Não tem "id": quem gera é o banco.
// Exercício 6: @Schema documenta o DTO e dá o exemplo do "Try it out".
@Schema(description = "Dados que o cliente envia para cadastrar um responsável")
public record ResponsavelRequestDTO(

        // Nome obrigatório.
        @Schema(description = "Nome do responsável", example = "Ana Souza")
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        // @Email sozinho aceita null e "" -- por isso vem junto do @NotBlank.
        @Schema(description = "E-mail do responsável", example = "ana@satc.edu.br")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
