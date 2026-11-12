// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa a lista devolvida nas consultas.
import java.util.List;

// Importa as anotações de documentação do Swagger/OpenAPI.
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
// Importa a anotação que libera chamadas de outras origens.
import org.springframework.web.bind.annotation.CrossOrigin;
// Importa a anotação de leitura HTTP.
import org.springframework.web.bind.annotation.GetMapping;
// Importa a anotação para ler parâmetros do caminho.
import org.springframework.web.bind.annotation.PathVariable;
// Importa a anotação de criação HTTP.
import org.springframework.web.bind.annotation.PostMapping;
// Importa a anotação que lê o corpo JSON.
import org.springframework.web.bind.annotation.RequestBody;
// Importa a anotação que define o prefixo das rotas.
import org.springframework.web.bind.annotation.RequestMapping;
// Importa a anotação que registra a classe como controller REST.
import org.springframework.web.bind.annotation.RestController;

// Exercício 1: só DTOs -- nenhum import de api.model neste arquivo.
import api.dto.ErroDTO;
import api.dto.ResponsavelDetalheDTO;
import api.dto.ResponsavelRequestDTO;
import api.dto.ResponsavelResponseDTO;
import api.dto.TarefaResponseDTO;
// Importa a camada que contém as regras de responsáveis.
import api.service.ResponsavelService;

// Exercício 1: o JSON de cada endpoint continua IGUAL ao da Aula 08 (a página
// responsaveis.html não percebeu nada), mas agora ele sai de DTOs -- e a
// seção "Schemas" do Swagger não mostra mais as entidades Responsavel e Tarefa.
//
// Exercício 6: documentado no Swagger. Antes, o grupo aparecia como
// "responsavel-controller", sem resumo nenhum, e o POST aparecia como 200 --
// o springdoc não tem como saber que o código devolve 201 lá dentro.
// Agrupa os endpoints desta classe sob "Responsáveis" no Swagger UI.
@Tag(name = "Responsáveis", description = "Cadastro de responsáveis e consulta das tarefas de cada um")
// Permite que o frontend local faça chamadas para a API.
@CrossOrigin(origins = "*")
// Informa ao Spring que a classe atende requisições REST.
@RestController
// Adiciona /responsaveis antes de todas as rotas abaixo.
@RequestMapping("/responsaveis")
public class ResponsavelController {

    // Guarda a camada de serviço usada pelas rotas.
    private final ResponsavelService service;

    // Recebe o service injetado pelo Spring.
    public ResponsavelController(ResponsavelService service) {
        // Guarda o service para uso nos métodos HTTP.
        this.service = service;
    }

    // Mapeia GET /responsaveis.
    @Operation(summary = "Lista os responsáveis", description = "Em ordem alfabética de nome.")
    @ApiResponse(responseCode = "200", description = "Lista de responsáveis (pode vir vazia)")
    @GetMapping
    public List<ResponsavelResponseDTO> listar() {
        // Delega a listagem para a camada de serviço.
        return service.listarTodos();
    }

    // Mapeia POST /responsaveis -- @Valid devolve 400 se nome/e-mail forem inválidos.
    @Operation(summary = "Cadastra um responsável")
    @ApiResponse(responseCode = "201", description = "Responsável criado -- o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Nome vazio ou e-mail vazio/inválido",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<ResponsavelResponseDTO> criar(@Valid @RequestBody ResponsavelRequestDTO dto) {
        // Pede ao service para criar e persistir o responsável validado.
        ResponsavelResponseDTO criado = service.criar(dto);
        // Retorna 201 Created junto do responsável criado.
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // Exercício 4: GET /responsaveis/{id} -- o responsável com as tarefas dele.
    @Operation(summary = "Detalha um responsável",
            description = "Traz os dados do responsável, os totais e o resumo de cada tarefa vinculada a ele.")
    @ApiResponse(responseCode = "200", description = "Responsável com as tarefas vinculadas")
    @ApiResponse(responseCode = "404", description = "Não existe responsável com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}")
    public ResponsavelDetalheDTO buscarDetalhe(@Parameter(description = "Id do responsável", example = "1") @PathVariable Long id) {
        // Delega a montagem do DTO composto para o service.
        return service.buscarDetalhe(id);
    }

    // Mapeia GET /responsaveis/{id}/tarefas.
    @Operation(summary = "Lista as tarefas de um responsável",
            description = "Tarefas completas, no mesmo formato do GET /tarefas.")
    @ApiResponse(responseCode = "200", description = "Tarefas vinculadas (pode vir vazia)")
    @ApiResponse(responseCode = "404", description = "Não existe responsável com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}/tarefas")
    public List<TarefaResponseDTO> listarTarefas(@Parameter(description = "Id do responsável", example = "1") @PathVariable Long id) {
        // Delega a consulta para o service (404 se o responsável não existir).
        return service.listarTarefas(id);
    }
}
