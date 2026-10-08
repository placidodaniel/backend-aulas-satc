// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa a lista devolvida na listagem.
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
// Importa as anotações de rota, de parâmetro e de corpo.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa os DTOs de entrada, de saída e de erro -- e só eles: nenhum import de api.model.
import api.dto.ErroDTO;
import api.dto.LivroRequestDTO;
import api.dto.LivroResponseDTO;
// Importa a camada que contém as operações do livro.
import api.service.LivroService;

// CAMADA: Controller
// RESPONSABILIDADE: receber as requisições de /livros, validar o DTO de
//                   entrada e devolver o status certo. Não tem regra de negócio.
// ROTAS ATENDIDAS: POST /livros, GET /livros e GET /livros/{id}.
// SITUAÇÃO: implementada. PUT e DELETE ficam para a próxima etapa.
// Agrupa todos os endpoints desta classe sob "Livros" no Swagger UI.
@Tag(name = "Livros", description = "Cadastro e consulta dos livros do acervo")
// Informa ao Spring que a classe atende requisições REST.
@RestController
// Adiciona /livros antes de todas as rotas abaixo.
@RequestMapping("/livros")
public class LivroController {

    // Guarda a camada de serviço usada pelas rotas.
    private final LivroService service;

    // Recebe o service injetado pelo Spring.
    public LivroController(LivroService service) {
        // Guarda o service para uso nos métodos HTTP.
        this.service = service;
    }

    // @Valid dispara a validação do LivroRequestDTO antes deste método rodar.
    @Operation(summary = "Cadastra um livro", description = "id e disponivel são definidos pela API, não pelo cliente.")
    @ApiResponse(responseCode = "201", description = "Livro cadastrado: o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido: a lista \"campos\" diz o que corrigir",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<LivroResponseDTO> criar(@Valid @RequestBody LivroRequestDTO dto) {
        // Delega o cadastro para o service.
        LivroResponseDTO criado = service.criar(dto);
        // Devolve 201 Created com o livro no corpo.
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // Mapeia GET /livros.
    @Operation(summary = "Lista os livros", description = "Sem livros cadastrados, a lista vem vazia, não 404.")
    @ApiResponse(responseCode = "200", description = "Lista de livros (pode vir vazia)")
    @GetMapping
    public List<LivroResponseDTO> listar() {
        // Delega a listagem para o service.
        return service.listarTodos();
    }

    // Mapeia GET /livros/{id}.
    @Operation(summary = "Busca um livro pelo id")
    @ApiResponse(responseCode = "200", description = "Livro encontrado")
    @ApiResponse(responseCode = "404", description = "Não existe livro com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}")
    public LivroResponseDTO buscarPorId(@Parameter(description = "Id do livro", example = "1") @PathVariable Long id) {
        // Delega a busca para o service; se não existir, a exceção vira 404.
        return service.buscarPorId(id);
    }
}
