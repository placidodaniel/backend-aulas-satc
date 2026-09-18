package api.service;
import java.util.Comparator;
import java.util.Collection;

import org.springframework.stereotype.Service;

import api.dto.TarefaDTO;
import api.model.Tarefa;
import api.repository.TarefaRepository;

import java.time.LocalDate;
// @Service: camada de regra de negócio -- o Controller fala com o Service,
// nunca direto com o Repository.
@Service
public class TarefaService {

    // Guarda o Repository recebido no construtor -- é o que os métodos abaixo usam
    // para consultar/salvar tarefas, em vez de cada um criar o seu próprio.
    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public Tarefa criar(TarefaDTO dto) {
        Tarefa tarefa = new Tarefa(null, dto.getTitulo(), dto.getResponsavel(), dto.getDataPrazo());
        return repository.salvar(tarefa);
    }

    // Lança TarefaNaoEncontradaException se o id não existir -- nunca devolve null.
    public Tarefa buscarPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    public void remover(Long id) {
        if (!repository.remover(id)) {
            throw new TarefaNaoEncontradaException(id);
        }
    }

    // Bônus -- fora do CRUD oficial da Aula 07 (que é só listar/criar/buscar/excluir).
    // Atualiza título, responsável e prazo de uma tarefa existente -- usa o MESMO
    // TarefaDTO (e a mesma validação) da criação. Reaproveita buscarPorId() pra já
    // lançar TarefaNaoEncontradaException se o id não existir -- mesmo raciocínio
    // de alternarConcluida() logo abaixo: tarefa é a MESMA instância guardada no
    // Map do Repository, então só alterar os campos aqui já é suficiente.
    public Tarefa atualizar(Long id, TarefaDTO dto) {
        Tarefa tarefa = buscarPorId(id);
        tarefa.setTitulo(dto.getTitulo());
        tarefa.setResponsavel(dto.getResponsavel());
        tarefa.setDataPrazo(dto.getDataPrazo());
        return tarefa;
    }

    // Bônus -- fora do CRUD oficial da Aula 07 (que é só listar/criar/buscar/excluir).
    // Reaproveita buscarPorId() pra já lançar TarefaNaoEncontradaException se o id
    // não existir. tarefa é a MESMA instância guardada no Map do Repository, então
    // só alterar o campo aqui já é o suficiente -- não precisa "salvar" de novo.
    public Tarefa alternarConcluida(Long id) {
        Tarefa tarefa = buscarPorId(id);
        tarefa.setConcluida(!tarefa.isConcluida());
        return tarefa;
    }
    // TarefaService -- não existe método novo no Repository; reaproveita listarTodas()
    public Collection<Tarefa> listarAtrasadas() {
        return repository.listarTodas().stream()
                .filter(tarefa -> tarefa.getDataPrazo().isBefore(LocalDate.now()))
                .filter(tarefa -> !tarefa.isConcluida())
                .toList();
    }

    // TarefaService -- só repassa pro Repository, igual listarTodas()
    public Collection<Tarefa> buscarPorResponsavel(String responsavel) {
        return repository.buscarPorResponsavel(responsavel);
    }

    // TarefaService
    public Collection<Tarefa> listarTodas() {
        return repository.listarTodas().stream()
                .sorted(Comparator.comparing(Tarefa::getDataPrazo))
                .toList();
    }
}
