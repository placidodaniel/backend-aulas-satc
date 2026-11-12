// Declara o pacote dos DTOs: as classes que formam o contrato da API.
package api.dto;

// Importa a validação e a documentação do Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Aula 14: uma mensagem da conversa que a tela do assistente guarda e reenvia.
// A API não guarda a conversa: a cada pedido, a tela manda o histórico inteiro.
@Schema(description = "Uma mensagem da conversa com o assistente")
public record MensagemAssistenteDTO(

        // Quem falou: "usuario" (você) ou "assistente" (o modelo).
        @Schema(description = "usuario ou assistente", example = "usuario")
        @NotBlank(message = "papel é obrigatório")
        @Pattern(regexp = "usuario|assistente", message = "papel deve ser usuario ou assistente")
        String papel,

        // O texto da mensagem.
        @Schema(description = "O texto da mensagem", example = "Quem tem mais tarefas abertas?")
        @NotBlank(message = "texto é obrigatório")
        String texto
) {
}
