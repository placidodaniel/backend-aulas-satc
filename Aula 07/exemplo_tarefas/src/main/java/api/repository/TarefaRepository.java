package api.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import api.model.Tarefa;

// "Banco de dados" em memória: guarda as tarefas enquanto a API estiver rodando.
@Repository
public class TarefaRepository {

    private final Map<Long, Tarefa> tarefas = new ConcurrentHashMap<>();
    private final AtomicLong proximoId = new AtomicLong(1);

    public TarefaRepository() {
        salvar(new Tarefa(null, "Preparar slides da Aula 08", "Daniel", LocalDate.now().plusDays(7)));
        salvar(new Tarefa(null, "Corrigir exercícios da Aula 07", "Daniel", LocalDate.now().plusDays(3)));
    }

    public Collection<Tarefa> listarTodas() {
        return tarefas.values();
    }

    // Gera um novo id e guarda a tarefa -- o id do parâmetro sempre é ignorado.
    // dataCadastro nasce de novo aqui dentro (é o construtor de Tarefa quem seta
    // pra "agora") -- é este o momento em que a tarefa passa a existir de verdade.
    public Tarefa salvar(Tarefa tarefa) {
        Tarefa comId = new Tarefa(proximoId.getAndIncrement(), tarefa.getTitulo(), tarefa.getResponsavel(), tarefa.getDataPrazo());
        comId.setConcluida(tarefa.isConcluida());
        tarefas.put(comId.getId(), comId);
        return comId;
    }

    public Optional<Tarefa> buscarPorId(Long id) {
        return Optional.ofNullable(tarefas.get(id));
    }

    // true se removeu, false se o id não existia.
    public boolean remover(Long id) {
        return tarefas.remove(id) != null;
    }
}
