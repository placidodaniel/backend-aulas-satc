// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa o tipo de data do parâmetro "data".
import java.time.LocalDate;

// Importa as anotações de documentação do Swagger/OpenAPI.
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// Importa a anotação que diz em que formato a data chega na URL.
import org.springframework.format.annotation.DateTimeFormat;
// Importa a anotação que libera chamadas de outras origens.
import org.springframework.web.bind.annotation.CrossOrigin;
// Importa a anotação de leitura HTTP.
import org.springframework.web.bind.annotation.GetMapping;
// Importa a anotação que define o prefixo das rotas.
import org.springframework.web.bind.annotation.RequestMapping;
// Importa a anotação que lê parâmetros da query string (?chave=valor).
import org.springframework.web.bind.annotation.RequestParam;
// Importa a anotação que registra a classe como controller REST.
import org.springframework.web.bind.annotation.RestController;

// Importa os DTOs de saída.
import api.dto.ErroDTO;
import api.dto.PrevisaoDTO;
// Importa a camada que contém as regras da previsão.
import api.service.PrevisaoService;

// >>> AULA 12: a rota da previsão. Para quem chama a nossa API, /previsao é
// uma rota como outra qualquer: o Controller não sabe (nem precisa saber) que,
// por trás do PrevisaoService, a resposta vem de outro sistema.
//
// O que muda está nas respostas possíveis: além de 400, esta rota pode
// devolver 502 Bad Gateway (o serviço de previsão falhou).
// Agrupa os endpoints desta classe sob "Previsão do tempo" no Swagger UI.
@Tag(name = "Previsão do tempo", description = "Integração com o Open-Meteo: aqui a nossa API é CLIENTE de outra API")
// Permite que o frontend local faça chamadas para a API.
@CrossOrigin(origins = "*")
// Informa ao Spring que a classe atende requisições REST.
@RestController
// Adiciona /previsao antes de todas as rotas abaixo.
@RequestMapping("/previsao")
public class PrevisaoController {

    // Guarda a camada de serviço usada pelas rotas.
    private final PrevisaoService service;

    // Recebe o service injetado pelo Spring.
    public PrevisaoController(PrevisaoService service) {
        // Guarda o service para uso nos métodos HTTP.
        this.service = service;
    }

    // GET /previsao?latitude=-28.68&longitude=-49.37&data=2026-10-24
    // @DateTimeFormat(iso = DATE): a data chega na URL como yyyy-MM-dd, o mesmo
    // formato do JSON. Sem essa anotação, o Spring tentaria o formato do
    // idioma do servidor e recusaria "2026-10-24".
    @Operation(summary = "Previsão do tempo de um dia",
            description = "Consulta o Open-Meteo e devolve a previsão traduzida. A data vai de hoje até 15 dias depois.")
    @ApiResponse(responseCode = "200", description = "Previsão do dia pedido")
    @ApiResponse(responseCode = "400", description = "Parâmetro ausente, coordenada fora do planeta ou data fora da janela de previsão",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "502", description = "O serviço de previsão não respondeu",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping
    public PrevisaoDTO buscar(
            @Parameter(description = "Latitude, de -90 a 90", example = "-28.6775") @RequestParam double latitude,
            @Parameter(description = "Longitude, de -180 a 180", example = "-49.3697") @RequestParam double longitude,
            @Parameter(description = "Dia da previsão (yyyy-MM-dd), de hoje até 15 dias depois")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        // Delega a consulta para o service.
        return service.buscar(latitude, longitude, data);
    }
}
