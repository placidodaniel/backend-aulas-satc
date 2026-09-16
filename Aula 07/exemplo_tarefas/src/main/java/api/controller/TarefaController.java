package api.controller;

import java.util.Collection;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dto.TarefaDTO;
import api.model.Tarefa;
import api.service.TarefaService;

// @RequestMapping("/tarefas") = prefixo de rota para todos os endpoints da classe.
//
// @CrossOrigin(origins = "*") libera chamadas vindas de qualquer origem -- só serve
// pra deixar a página em static/index.html funcionar também quando aberta direto
// (file://), fora do próprio Spring Boot. Numa API de verdade isso seria restrito
// a domínios específicos, não "*".
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<Tarefa> listar() {
        return service.listarTodas();
    }

    // @Valid dispara a validação do TarefaDTO antes deste método rodar; título
    // vazio já vira 400 Bad Request sozinho, sem código extra aqui.
    @PostMapping
    public ResponseEntity<Tarefa> criar(@Valid @RequestBody TarefaDTO dto) {
        Tarefa criada = service.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping("/{id}")
    public Tarefa buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // Bônus -- fora do CRUD oficial da Aula 07. Atualiza título, responsável e
    // prazo de uma tarefa existente; usa o mesmo TarefaDTO (e a mesma validação)
    // da criação -- dataCadastro nunca muda, concluida só muda pelo /concluir.
    @PutMapping("/{id}")
    public Tarefa atualizar(@PathVariable Long id, @Valid @RequestBody TarefaDTO dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    // Bônus -- fora do CRUD oficial da Aula 07 (que é só listar/criar/buscar/excluir).
    // Alterna o campo "concluida" da tarefa e devolve o estado atualizado.
    @PutMapping("/{id}/concluir")
    public Tarefa concluir(@PathVariable Long id) {
        return service.alternarConcluida(id);
    }
}
