// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a interface Collection usada no retorno da listagem.
import java.util.Collection;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o DTO recebido pela API.
import api.dto.TarefaDTO;
// Importa a entidade persistida.
import api.model.Tarefa;
// Importa o repositório JPA usado pelo service.
import api.repository.TarefaRepository;

// @Service: camada de regra de negócio -- o Controller fala com o Service,
// nunca direto com o Repository.
// Registra a classe como bean da camada de serviço.
@Service
public class TarefaService {

    // Guarda o Repository recebido no construtor -- é o que os métodos abaixo usam
    // para consultar/salvar tarefas, em vez de cada um criar o seu próprio.
    // Mantém a dependência do repositório disponível para os métodos.
    private final TarefaRepository repository;

    // Recebe o repositório que o Spring injeta automaticamente.
    public TarefaService(TarefaRepository repository) {
        // Guarda a dependência recebida no atributo da classe.
        this.repository = repository;
    }

    // Lista todas as tarefas armazenadas no PostgreSQL.
    public Collection<Tarefa> listarTodas() {
        // O Spring Data gera e executa o SELECT correspondente.
        return repository.findAll();
    }

    // Cria uma entidade a partir dos dados validados do DTO.
    public Tarefa criar(TarefaDTO dto) {
        // Monta uma nova tarefa sem id; o banco irá gerar a chave.
        Tarefa tarefa = new Tarefa(null, dto.getTitulo(), dto.getResponsavel(), dto.getDataPrazo());
        // Persiste a entidade e devolve o objeto com o id preenchido.
        return repository.save(tarefa);
    }

    // Lança TarefaNaoEncontradaException se o id não existir -- nunca devolve null.
    // Busca uma tarefa pelo id informado na URL.
    public Tarefa buscarPorId(Long id) {
        // Consulta o banco e transforma ausência em Optional vazio.
        return repository.findById(id)
                // Lança a exceção de negócio quando o registro não existe.
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    // Remove a tarefa identificada pelo id.
    public void remover(Long id) {
        // Confere a existência antes de tentar excluir.
        if (!repository.existsById(id)) {
            // Mantém o contrato da API retornando 404 para id inexistente.
            throw new TarefaNaoEncontradaException(id);
        }
        // Executa o DELETE gerado pelo Spring Data JPA.
        repository.deleteById(id);
    }

    // Bônus -- fora do CRUD oficial da Aula 07 (que é só listar/criar/buscar/excluir).
    // Atualiza título, responsável e prazo e salva a entidade alterada.
    // Atualiza os campos permitidos pelo DTO.
    public Tarefa atualizar(Long id, TarefaDTO dto) {
        // Reutiliza a busca para validar que o id existe.
        Tarefa tarefa = buscarPorId(id);
        // Substitui o título antigo pelo novo valor.
        tarefa.setTitulo(dto.getTitulo());
        // Substitui o responsável antigo pelo novo valor.
        tarefa.setResponsavel(dto.getResponsavel());
        // Substitui o prazo antigo pelo novo valor.
        tarefa.setDataPrazo(dto.getDataPrazo());
        // Persiste as alterações e devolve a entidade atualizada.
        return repository.save(tarefa);
    }

    // Bônus -- fora do CRUD oficial da Aula 07 (que é só listar/criar/buscar/excluir).
    // Reaproveita buscarPorId() e salva o estado alternado.
    // Alterna o campo concluida entre true e false.
    public Tarefa alternarConcluida(Long id) {
        // Busca a entidade existente ou lança 404.
        Tarefa tarefa = buscarPorId(id);
        // Inverte o valor atual do booleano.
        tarefa.setConcluida(!tarefa.isConcluida());
        // Persiste o novo estado no PostgreSQL.
        return repository.save(tarefa);
    }
}
