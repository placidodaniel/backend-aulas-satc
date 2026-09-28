// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa a interface usada para devolver vários registros.
import java.util.Collection;

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

// Importa o DTO usado no corpo da criação.
import api.dto.ResponsavelDTO;
// Importa as entidades devolvidas nas respostas.
import api.model.Responsavel;
import api.model.Tarefa;
// Importa a camada que contém as regras de responsáveis.
import api.service.ResponsavelService;

// Exercício 6: endpoints 1 a 3 do contrato. Os endpoints 4 e 5 (vínculo)
// ficam no TarefaController, porque a rota começa em /tarefas/{id}.
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
    @GetMapping
    public Collection<Responsavel> listar() {
        // Delega a listagem para a camada de serviço.
        return service.listarTodos();
    }

    // Mapeia POST /responsaveis -- @Valid devolve 400 se nome/e-mail forem inválidos.
    @PostMapping
    public ResponseEntity<Responsavel> criar(@Valid @RequestBody ResponsavelDTO dto) {
        // Pede ao service para criar e persistir o responsável validado.
        Responsavel criado = service.criar(dto);
        // Retorna 201 Created junto do responsável criado.
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    // Mapeia GET /responsaveis/{id}/tarefas.
    @GetMapping("/{id}/tarefas")
    public Collection<Tarefa> listarTarefas(@PathVariable Long id) {
        // Delega a consulta para o service (404 se o responsável não existir).
        return service.listarTarefas(id);
    }
}
