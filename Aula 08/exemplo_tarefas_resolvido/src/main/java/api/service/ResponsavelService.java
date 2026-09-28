// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a interface Collection usada nos retornos de listagem.
import java.util.Collection;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o DTO recebido pela API.
import api.dto.ResponsavelDTO;
// Importa as entidades persistidas.
import api.model.Responsavel;
import api.model.Tarefa;
// Importa os repositórios usados pelo service.
import api.repository.ResponsavelRepository;
import api.repository.TarefaRepository;

// Exercício 6: regras de negócio de responsáveis -- mesmo papel do TarefaService.
@Service
public class ResponsavelService {

    // Repositório da tabela responsaveis.
    private final ResponsavelRepository repository;

    // Repositório de tarefas, usado para listar as tarefas de um responsável.
    private final TarefaRepository tarefaRepository;

    // Recebe os repositórios que o Spring injeta automaticamente.
    public ResponsavelService(ResponsavelRepository repository, TarefaRepository tarefaRepository) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.tarefaRepository = tarefaRepository;
    }

    // Lista todos os responsáveis em ordem alfabética (exigência do contrato).
    public Collection<Responsavel> listarTodos() {
        // O Spring Data gera SELECT ... ORDER BY nome ASC.
        return repository.findAllByOrderByNomeAsc();
    }

    // Cria um responsável a partir dos dados validados do DTO.
    public Responsavel criar(ResponsavelDTO dto) {
        // Persiste a entidade e devolve o objeto com o id preenchido.
        return repository.save(new Responsavel(dto.getNome(), dto.getEmail()));
    }

    // Lista as tarefas vinculadas a um responsável. Responsável inexistente é
    // 404; responsável sem tarefas é 200 com lista vazia.
    public Collection<Tarefa> listarTarefas(Long responsavelId) {
        // Confere a existência antes de consultar as tarefas.
        if (!repository.existsById(responsavelId)) {
            // Mantém o contrato da API retornando 404 para id inexistente.
            throw new ResponsavelNaoEncontradoException(responsavelId);
        }
        // O Spring Data gera SELECT ... FROM tarefas WHERE responsavel_id = ?
        return tarefaRepository.findByResponsavelVinculadoId(responsavelId);
    }
}
