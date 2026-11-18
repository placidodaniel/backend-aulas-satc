// Declara o pacote dos DTOs: as classes que formam o contrato da API.
package api.dto;

// Importa a lista de passos.
import java.util.List;

// Importa a documentação do Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Aula 14: o que POST /assistente devolve para a tela.
@Schema(description = "Resposta do assistente e as ferramentas que ele usou")
public record AssistenteRespostaDTO(

        @Schema(description = "A resposta final do modelo, em texto")
        String resposta,

        @Schema(description = "O modelo que respondeu", example = "qwen2.5:3b")
        String modelo,

        @Schema(description = "As ferramentas MCP chamadas, na ordem")
        List<PassoAssistenteDTO> passos
) {
}
