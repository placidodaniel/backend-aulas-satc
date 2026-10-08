// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa as anotações de documentação do Swagger/OpenAPI.
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
// Importa a anotação que dispara a validação do DTO.
import jakarta.validation.Valid;

// Importa o status HTTP usado na criação.
import org.springframework.http.HttpStatus;
// Importa o tipo usado para responder com status e corpo.
import org.springframework.http.ResponseEntity;
// Importa as anotações de rota e de corpo.
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa os DTOs de entrada, de saída e de erro.
import api.dto.ErroDTO;
import api.dto.LeitorRequestDTO;
import api.dto.LeitorResponseDTO;
// Importa a camada que contém as operações do leitor.
import api.service.LeitorService;

// CAMADA: Controller
// RESPONSABILIDADE: receber as requisições de /leitores.
// ROTAS ATENDIDAS: POST /leitores. GET /leitores e GET /leitores/{id} estão no
//                  contrato e entram na próxima etapa.
// SITUAÇÃO: implementada em parte.
// Agrupa os endpoints desta classe sob "Leitores" no Swagger UI.
@Tag(name = "Leitores", description = "Cadastro de leitores")
// Informa ao Spring que a classe atende requisições REST.
@RestController
// Adiciona /leitores antes de todas as rotas abaixo.
@RequestMapping("/leitores")
public class LeitorController {

    // Guarda a camada de serviço usada pelas rotas.
    private final LeitorService service;

    // Recebe o service injetado pelo Spring.
    public LeitorController(LeitorService service) {
        // Guarda o service para uso nos métodos HTTP.
        this.service = service;
    }

    // @Valid dispara a validação do LeitorRequestDTO antes deste método rodar.
    @Operation(summary = "Cadastra um leitor")
    @ApiResponse(responseCode = "201", description = "Leitor cadastrado: o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido: a lista \"campos\" diz o que corrigir",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<LeitorResponseDTO> criar(@Valid @RequestBody LeitorRequestDTO dto) {
        // Delega o cadastro para o service e devolve 201 Created.
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }
}
