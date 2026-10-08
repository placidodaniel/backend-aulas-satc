// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa os DTOs de entrada, de saída e de erro.
import api.dto.EmprestimoRequestDTO;
import api.dto.EmprestimoResponseDTO;
import api.dto.ErroDTO;
// Importa a camada que contém as regras dos empréstimos.
import api.service.EmprestimoService;

// CAMADA: Controller
// RESPONSABILIDADE: receber as requisições de /emprestimos. As regras R1, R2 e
//                   R3 não estão aqui: ficam no EmprestimoService.
// ROTAS ATENDIDAS: POST /emprestimos e PUT /emprestimos/{id}/devolver.
// SITUAÇÃO: implementada.
// Agrupa os endpoints desta classe sob "Empréstimos" no Swagger UI.
@Tag(name = "Empréstimos", description = "Empréstimo e devolução de livros, com as regras R1, R2 e R3")
// Informa ao Spring que a classe atende requisições REST.
@RestController
// Adiciona /emprestimos antes de todas as rotas abaixo.
@RequestMapping("/emprestimos")
public class EmprestimoController {

    // Guarda a camada de serviço usada pelas rotas.
    private final EmprestimoService service;

    // Recebe o service injetado pelo Spring.
    public EmprestimoController(EmprestimoService service) {
        // Guarda o service para uso nos métodos HTTP.
        this.service = service;
    }

    // @Valid confere o formato; as regras de negócio quem confere é o Service.
    @Operation(summary = "Registra um empréstimo",
            description = "R1: o leitor pode ter no máximo 3 empréstimos em aberto. "
                    + "R2: o livro precisa estar disponível. R3: o prazo de devolução é de 14 dias.")
    @ApiResponse(responseCode = "201", description = "Empréstimo registrado, com a data limite de devolução")
    @ApiResponse(responseCode = "400", description = "Corpo inválido, ou regra R1 ou R2 violada (a mensagem diz qual)",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "O leitor ou o livro não existe",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<EmprestimoResponseDTO> criar(@Valid @RequestBody EmprestimoRequestDTO dto) {
        // Delega para o service, que aplica as regras, e devolve 201 Created.
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    // Mapeia PUT /emprestimos/{id}/devolver.
    @Operation(summary = "Registra a devolução", description = "O livro volta para a estante. "
            + "A resposta diz se a devolução passou do prazo (R3).")
    @ApiResponse(responseCode = "200", description = "Devolução registrada")
    @ApiResponse(responseCode = "400", description = "O empréstimo já tinha sido devolvido",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe empréstimo com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PutMapping("/{id}/devolver")
    public EmprestimoResponseDTO devolver(@Parameter(description = "Id do empréstimo", example = "1") @PathVariable Long id) {
        // Delega para o service.
        return service.devolver(id);
    }
}
