// Pacote do assistente da tela (Aula 14).
package api.assistente;

// Importa a documentação do Swagger.
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
// Importa a validação do corpo.
import jakarta.validation.Valid;

// Importa as anotações de rota.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa os DTOs.
import api.dto.AssistentePedidoDTO;
import api.dto.AssistenteRespostaDTO;
import api.dto.AssistenteStatusDTO;
import api.dto.ErroDTO;

// Aula 14: as rotas que a tela assistente.html usa.
// Como todo Controller do projeto, só recebe e repassa: quem trabalha é o AssistenteService.
@Tag(name = "Assistente", description = "Aula 14: um modelo de IA (Ollama ou OpenRouter) usando as ferramentas MCP do sistema")
@RestController
@RequestMapping("/assistente")
public class AssistenteController {

    private final AssistenteService service;

    public AssistenteController(AssistenteService service) {
        this.service = service;
    }

    // GET /assistente/status: o servidor de modelos está no ar? Quais modelos ele tem?
    @Operation(summary = "Diz se o servidor de modelos está no ar e quais modelos ele tem")
    @GetMapping("/status")
    public AssistenteStatusDTO status() {
        return service.status();
    }

    // POST /assistente: manda a conversa, recebe a resposta e as ferramentas usadas.
    @Operation(summary = "Conversa com o assistente",
            description = "O modelo recebe a conversa e as ferramentas do servidor MCP (/mcp). "
                    + "As ferramentas que ele pedir são executadas pelo MCP e aparecem em 'passos'.")
    @ApiResponse(responseCode = "200", description = "Resposta do modelo")
    @ApiResponse(responseCode = "400", description = "Conversa inválida",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "502", description = "O servidor de modelos falhou (fora do ar, modelo não baixado, sem suporte a ferramentas...)",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public AssistenteRespostaDTO conversar(@Valid @RequestBody AssistentePedidoDTO pedido) {
        return service.conversar(pedido);
    }
}
