// Define o pacote da camada de regras de negócio.
package api.service;

// Importa o tipo de data usado na consulta de atrasadas.
import java.time.LocalDate;
// Importa a interface Collection usada no retorno da listagem.
import java.util.Collection;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o DTO recebido pela API.
import api.dto.TarefaDTO;
// Importa o responsável cadastrado (Exercício 6).
import api.model.Responsavel;
// Importa a entidade persistida.
import api.model.Tarefa;
// Importa o repositório de responsáveis (Exercício 6).
import api.repository.ResponsavelRepository;
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

    // Exercício 6: usado para buscar o responsável antes de vincular.
    private final ResponsavelRepository responsavelRepository;

    // Recebe os repositórios que o Spring injeta automaticamente.
    public TarefaService(TarefaRepository repository, ResponsavelRepository responsavelRepository) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.responsavelRepository = responsavelRepository;
    }

    // Exercício 4: lista todas as tarefas, prazo mais próximo primeiro.
    public Collection<Tarefa> listarTodas() {
        // O Spring Data gera o SELECT com ORDER BY data_prazo ASC.
        return repository.findAllByOrderByDataPrazoAsc();
    }

    // Exercício 1: tarefas cujo responsável (texto) bate com o nome, sem
    // diferenciar maiúsculas/minúsculas. Nenhum resultado = lista vazia.
    public Collection<Tarefa> buscarPorResponsavel(String responsavel) {
        // O filtro roda no PostgreSQL, não em memória.
        return repository.findByResponsavelIgnoreCase(responsavel);
    }

    // Exercício 2: tarefas não concluídas cujo prazo já passou.
    public Collection<Tarefa> listarAtrasadas() {
        // "Before" é estrito: prazo = hoje ainda não está atrasado.
        return repository.findByConcluidaFalseAndDataPrazoBefore(LocalDate.now());
    }

    // Cria uma entidade a partir dos dados validados do DTO.
    public Tarefa criar(TarefaDTO dto) {
        // Monta uma nova tarefa sem id; o banco irá gerar a chave.
        Tarefa tarefa = new Tarefa(null, dto.getTitulo(), dto.getResponsavel(), dto.getDataPrazo(), dto.getPrioridade());
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
        // Exercício 5: substitui a prioridade antiga pelo novo valor.
        tarefa.setPrioridade(dto.getPrioridade());
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

    // Exercício 6: liga a tarefa a um responsável cadastrado. Busca os dois
    // lados antes -- assim um id inexistente vira 404 com mensagem clara, em
    // vez de o PostgreSQL recusar a chave estrangeira (e virar 500).
    public Tarefa vincularResponsavel(Long tarefaId, Long responsavelId) {
        // Busca a tarefa ou lança TarefaNaoEncontradaException (404).
        Tarefa tarefa = buscarPorId(tarefaId);
        // Busca o responsável ou lança ResponsavelNaoEncontradoException (404).
        Responsavel responsavel = responsavelRepository.findById(responsavelId)
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(responsavelId));
        // Troca o vínculo (se já existia um, é substituído).
        tarefa.setResponsavelVinculado(responsavel);
        // O Hibernate gera UPDATE tarefas SET responsavel_id = ? WHERE id = ?
        return repository.save(tarefa);
    }

    // Exercício 6: remove o vínculo -- responsavel_id volta a ser NULL.
    public void desvincularResponsavel(Long tarefaId) {
        // Busca a tarefa ou lança TarefaNaoEncontradaException (404).
        Tarefa tarefa = buscarPorId(tarefaId);
        // null na entidade vira NULL na coluna responsavel_id.
        tarefa.setResponsavelVinculado(null);
        // Persiste a alteração.
        repository.save(tarefa);
    }
}
