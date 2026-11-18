// Declara o pacote dos DTOs: as classes que formam o contrato da API.
package api.dto;

// Importa a lista de modelos.
import java.util.List;

// Importa a documentação do Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Aula 14: o que GET /assistente/status devolve. A tela usa para avisar quando
// o servidor de modelos não está no ar, antes de a pessoa digitar qualquer coisa.
@Schema(description = "Se o servidor de modelos está no ar e quais modelos ele tem")
public record AssistenteStatusDTO(

        @Schema(description = "true quando o servidor de modelos respondeu", example = "true")
        boolean disponivel,

        @Schema(description = "Endereço do servidor de modelos", example = "http://localhost:11434/v1")
        String servidor,

        @Schema(description = "Modelo usado quando a tela não escolhe outro", example = "qwen2.5:3b")
        String modeloPadrao,

        @Schema(description = "Modelos disponíveis no servidor")
        List<String> modelos,

        @Schema(description = "Quando não está disponível, o motivo")
        String mensagem
) {
}
