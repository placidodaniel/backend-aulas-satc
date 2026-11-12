// Declara o pacote dos DTOs: as classes que formam o contrato da API.
package api.dto;

// Importa a lista de mensagens.
import java.util.List;

// Importa a validação e a documentação do Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// Aula 14: o que a tela manda para POST /assistente.
@Schema(description = "Pedido para o assistente de IA")
public record AssistentePedidoDTO(

        // A conversa até agora; a última mensagem é a pergunta nova.
        @Schema(description = "A conversa até agora; a última é o pedido novo")
        @NotEmpty(message = "mande pelo menos uma mensagem")
        @Size(max = 30, message = "conversa longa demais: comece uma nova")
        List<@Valid MensagemAssistenteDTO> mensagens,

        // Modelo (opcional). Vazio = o configurado em ASSISTENTE_MODELO.
        @Schema(description = "Nome do modelo; vazio usa o padrão", example = "qwen2.5:3b")
        String modelo,

        // true = o modelo só recebe as ferramentas de leitura (readOnlyHint).
        @Schema(description = "Se true, só oferece ao modelo as ferramentas que não alteram dados", example = "false")
        boolean somenteLeitura
) {
}
