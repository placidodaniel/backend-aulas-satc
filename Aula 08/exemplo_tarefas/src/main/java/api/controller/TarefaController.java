// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa a interface usada para devolver várias tarefas.
import java.util.Collection;

// Importa a anotação que dispara a validação do DTO.
import jakarta.validation.Valid;

// Importa o status HTTP usado na criação.
import org.springframework.http.HttpStatus;
// Importa o tipo usado para responder com status e corpo.
import org.springframework.http.ResponseEntity;
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
// Importa a anotação que libera chamadas de outras origens.
import org.springframework.web.bind.annotation.CrossOrigin;
// Importa a anotação que define o prefixo das rotas.
import org.springframework.web.bind.annotation.RequestMapping;
// Importa a anotação que registra a classe como controller REST.
import org.springframework.web.bind.annotation.RestController;

// Importa o DTO usado nos corpos de criação e atualização.
import api.dto.TarefaDTO;
// Importa a entidade devolvida nas respostas.
import api.model.Tarefa;
// Importa a camada que contém as regras da tarefa.
import api.service.TarefaService;

// @RequestMapping("/tarefas") = prefixo de rota para todos os endpoints da classe.
//
// @CrossOrigin(origins = "*") libera chamadas vindas de qualquer origem -- só serve
// pra deixar a página em static/index.html funcionar também quando aberta direto
// (file://), fora do próprio Spring Boot. Numa API de verdade isso seria restrito
// a domínios específicos, não "*".
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
    @GetMapping
    public Collection<Tarefa> listar() {
        // Delega a listagem para a camada de serviço.
        return service.listarTodas();
    }

    // @Valid dispara a validação do TarefaDTO antes deste método rodar; título
    // vazio já vira 400 Bad Request sozinho, sem código extra aqui.
    @PostMapping
    public ResponseEntity<Tarefa> criar(@Valid @RequestBody TarefaDTO dto) {
        // Pede ao service para criar e persistir a tarefa validada.
        Tarefa criada = service.criar(dto);
        // Retorna 201 Created junto da tarefa criada.
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    // Mapeia GET /tarefas/{id}.
    @GetMapping("/{id}")
    public Tarefa buscarPorId(@PathVariable Long id) {
        // Delega a busca para o service.
        return service.buscarPorId(id);
    }

    // Bônus -- fora do CRUD oficial da Aula 07. Atualiza título, responsável e
    // prazo de uma tarefa existente; usa o mesmo TarefaDTO (e a mesma validação)
    // da criação -- dataCadastro nunca muda, concluida só muda pelo /concluir.
    @PutMapping("/{id}")
    public Tarefa atualizar(@PathVariable Long id, @Valid @RequestBody TarefaDTO dto) {
        // Delega a atualização da tarefa para o service.
        return service.atualizar(id, dto);
    }

    // Mapeia DELETE /tarefas/{id}.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        // Pede ao service para excluir a tarefa.
        service.remover(id);
        // Retorna 204 sem corpo quando a exclusão termina.
        return ResponseEntity.noContent().build();
    }

    // Bônus -- fora do CRUD oficial da Aula 07 (que é só listar/criar/buscar/excluir).
    // Alterna o campo "concluida" da tarefa e devolve o estado atualizado.
    @PutMapping("/{id}/concluir")
    public Tarefa concluir(@PathVariable Long id) {
        // Pede ao service para inverter o estado de conclusão.
        return service.alternarConcluida(id);
    }
}
