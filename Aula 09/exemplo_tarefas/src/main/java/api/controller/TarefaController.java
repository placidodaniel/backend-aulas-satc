// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa a lista devolvida nas consultas.
import java.util.List;

// Importa as anotações de documentação do Swagger/OpenAPI.
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
// Importa a anotação de exclusão HTTP.
import org.springframework.web.bind.annotation.DeleteMapping;
// Importa a anotação de leitura HTTP.
import org.springframework.web.bind.annotation.GetMapping;
// Importa a anotação para ler parâmetros do caminho.
import org.springframework.web.bind.annotation.PathVariable;
// Importa a anotação de criação HTTP.
import org.springframework.web.bind.annotation.PostMapping;
// Importa a anotação de atualização HTTP.
import org.springframework.web.bind.annotation.PutMapping;
// Importa a anotação que lê o corpo JSON.
import org.springframework.web.bind.annotation.RequestBody;
// Importa a anotação que define o prefixo das rotas.
import org.springframework.web.bind.annotation.RequestMapping;
// Importa a anotação que lê parâmetros da query string (?chave=valor).
import org.springframework.web.bind.annotation.RequestParam;
// Importa a anotação que registra a classe como controller REST.
import org.springframework.web.bind.annotation.RestController;

// Importa os DTOs de entrada e de saída -- e SÓ eles: nenhum import de api.model.
import api.dto.TarefaRequestDTO;
import api.dto.TarefaResponseDTO;
import api.dto.VinculoResponsavelDTO;
// Importa a camada que contém as regras da tarefa.
import api.service.TarefaService;

// >>> AULA 09: o Controller só conhece DTOs. Recebe TarefaRequestDTO,
// devolve TarefaResponseDTO -- a entidade Tarefa ficou do Service para baixo.
//
// >>> SWAGGER: as anotações @Tag, @Operation, @ApiResponse e @Parameter NÃO
// mudam o comportamento da API (tire todas e ela responde igual). Elas só
// completam o documento OpenAPI que o springdoc gera sozinho a partir das
// rotas: dão nome ao grupo, resumo a cada endpoint e listam os status
// possíveis. Resultado em http://localhost:8080/swagger-ui.html.
//
// Os 404 deste projeto ainda saem em texto puro do ApiExceptionHandler; por
// isso a documentação deles usa mediaType "text/plain" (Exercício 5 troca por
// um DTO de erro em JSON).
// Agrupa todos os endpoints desta classe sob "Tarefas" no Swagger UI.
@Tag(name = "Tarefas", description = "CRUD de tarefas, consultas e vínculo com um responsável cadastrado")
// Permite que o frontend local faça chamadas para a API.
@CrossOrigin(origins = "*")
// Informa ao Spring que a classe atende requisições REST.
@RestController
// Adiciona /tarefas antes de todas as rotas abaixo.
@RequestMapping("/tarefas")
public class TarefaController {

    // Guarda a camada de serviço usada pelas rotas.
    private final TarefaService service;

    // Recebe o service injetado pelo Spring.
    public TarefaController(TarefaService service) {
        // Guarda o service para uso nos métodos HTTP.
        this.service = service;
    }

    // Mapeia GET /tarefas.
    @Operation(summary = "Lista todas as tarefas", description = "Ordenadas pelo prazo: a mais urgente primeiro.")
    @ApiResponse(responseCode = "200", description = "Lista de tarefas (pode vir vazia)")
    @GetMapping
    public List<TarefaResponseDTO> listar() {
        // Delega a listagem para a camada de serviço.
        return service.listarTodas();
    }

    // GET /tarefas/buscar?responsavel=Ana -- "/buscar" é literal, mais
    // específico que "/{id}", então o Spring MVC não confunde as duas rotas.
    @Operation(summary = "Busca tarefas pelo nome do responsável (texto)",
            description = "Compara sem diferenciar maiúsculas de minúsculas. Nenhum resultado devolve lista vazia, não 404.")
    @ApiResponse(responseCode = "200", description = "Tarefas encontradas (pode vir vazia)")
    @GetMapping("/buscar")
    public List<TarefaResponseDTO> buscarPorResponsavel(
            @Parameter(description = "Nome do responsável", example = "Daniel") @RequestParam String responsavel) {
        // Delega a busca para o service.
        return service.buscarPorResponsavel(responsavel);
    }

    // Mapeia GET /tarefas/atrasadas.
    @Operation(summary = "Lista as tarefas atrasadas", description = "Não concluídas e com prazo antes de hoje.")
    @ApiResponse(responseCode = "200", description = "Tarefas atrasadas (pode vir vazia)")
    @GetMapping("/atrasadas")
    public List<TarefaResponseDTO> listarAtrasadas() {
        // Delega a consulta para o service.
        return service.listarAtrasadas();
    }

    // @Valid dispara a validação do TarefaRequestDTO antes deste método rodar.
    // content = @Content (vazio) no 400: sem ele, o springdoc repetiria o
    // TarefaResponseDTO como corpo do erro, o que não é verdade.
    @Operation(summary = "Cria uma tarefa", description = "id, concluida e dataCadastro são definidos pela API, não pelo cliente. "
            + "Com responsavelId (opcional), a tarefa já nasce vinculada a um responsável cadastrado.")
    @ApiResponse(responseCode = "201", description = "Tarefa criada -- o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Corpo inválido: falhou em alguma validação do TarefaRequestDTO",
            content = @Content)
    @ApiResponse(responseCode = "404", description = "O responsavelId informado não existe",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Responsável não encontrado: 999")))
    @PostMapping
    public ResponseEntity<TarefaResponseDTO> criar(@Valid @RequestBody TarefaRequestDTO dto) {
        // Pede ao service para criar e persistir a tarefa validada.
        TarefaResponseDTO criada = service.criar(dto);
        // Retorna 201 Created junto da tarefa criada.
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    // Mapeia GET /tarefas/{id}.
    @Operation(summary = "Busca uma tarefa pelo id")
    @ApiResponse(responseCode = "200", description = "Tarefa encontrada")
    @ApiResponse(responseCode = "404", description = "Não existe tarefa com esse id",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Tarefa não encontrada: 999")))
    @GetMapping("/{id}")
    public TarefaResponseDTO buscarPorId(@Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id) {
        // Delega a busca para o service.
        return service.buscarPorId(id);
    }

    // Bônus -- atualiza título, responsável, prazo, prioridade e vínculo; usa o
    // mesmo TarefaRequestDTO (e a mesma validação) da criação.
    @Operation(summary = "Atualiza uma tarefa",
            description = "Substitui título, responsável, prazo, prioridade e o vínculo. Obrigatórios como no POST; "
                    + "sem responsavelId, a tarefa fica sem responsável vinculado.")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada")
    @ApiResponse(responseCode = "400", description = "Corpo inválido: falhou em alguma validação do TarefaRequestDTO",
            content = @Content)
    @ApiResponse(responseCode = "404", description = "A tarefa (id) ou o responsável (responsavelId) não existe",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Tarefa não encontrada: 999")))
    @PutMapping("/{id}")
    public TarefaResponseDTO atualizar(@Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id,
                                       @Valid @RequestBody TarefaRequestDTO dto) {
        // Delega a atualização da tarefa para o service.
        return service.atualizar(id, dto);
    }

    // Mapeia DELETE /tarefas/{id}.
    @Operation(summary = "Exclui uma tarefa")
    @ApiResponse(responseCode = "204", description = "Tarefa excluída (sem corpo)")
    @ApiResponse(responseCode = "404", description = "Não existe tarefa com esse id",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Tarefa não encontrada: 999")))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id) {
        // Pede ao service para excluir a tarefa.
        service.remover(id);
        // Retorna 204 sem corpo quando a exclusão termina.
        return ResponseEntity.noContent().build();
    }

    // Bônus -- alterna o campo "concluida" e devolve o estado atualizado.
    @Operation(summary = "Conclui ou reabre uma tarefa", description = "Inverte o campo concluida (true ↔ false).")
    @ApiResponse(responseCode = "200", description = "Tarefa com o campo concluida invertido")
    @ApiResponse(responseCode = "404", description = "Não existe tarefa com esse id",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Tarefa não encontrada: 999")))
    @PutMapping("/{id}/concluir")
    public TarefaResponseDTO concluir(@Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id) {
        // Pede ao service para inverter o estado de conclusão.
        return service.alternarConcluida(id);
    }

    // PUT /tarefas/{id}/responsavel com {"responsavelId": 1}.
    // @Valid barra o corpo sem responsavelId com 400 antes de chegar no service.
    @Operation(summary = "Vincula um responsável cadastrado à tarefa",
            description = "Se a tarefa já tinha um responsável vinculado, ele é trocado.")
    @ApiResponse(responseCode = "200", description = "Tarefa com responsavelVinculado preenchido")
    @ApiResponse(responseCode = "400", description = "Corpo sem responsavelId", content = @Content)
    @ApiResponse(responseCode = "404", description = "A tarefa ou o responsável não existe",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Responsável não encontrado: 999")))
    @PutMapping("/{id}/responsavel")
    public TarefaResponseDTO vincularResponsavel(@Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id,
                                                 @Valid @RequestBody VinculoResponsavelDTO dto) {
        // Record: o "getter" de responsavelId é responsavelId(), sem o "get".
        return service.vincularResponsavel(id, dto.responsavelId());
    }

    // DELETE /tarefas/{id}/responsavel -- remove só o vínculo, não a tarefa
    // nem o responsável.
    @Operation(summary = "Remove o responsável vinculado da tarefa", description = "A tarefa e o responsável continuam existindo.")
    @ApiResponse(responseCode = "204", description = "Vínculo removido (sem corpo)")
    @ApiResponse(responseCode = "404", description = "Não existe tarefa com esse id",
            content = @Content(mediaType = "text/plain", examples = @ExampleObject("Tarefa não encontrada: 999")))
    @DeleteMapping("/{id}/responsavel")
    public ResponseEntity<Void> desvincularResponsavel(@Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id) {
        // Pede ao service para limpar o vínculo.
        service.desvincularResponsavel(id);
        // Retorna 204 sem corpo, igual ao DELETE /tarefas/{id}.
        return ResponseEntity.noContent().build();
    }
}
