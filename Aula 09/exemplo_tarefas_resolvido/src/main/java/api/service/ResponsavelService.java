// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a lista devolvida nas consultas.
import java.util.List;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa os DTOs de entrada e de saída.
import api.dto.ResponsavelDetalheDTO;
import api.dto.ResponsavelRequestDTO;
import api.dto.ResponsavelResponseDTO;
import api.dto.TarefaResponseDTO;
import api.dto.TarefaResumoDTO;
// Importa os mappers que traduzem DTO <-> entidade.
import api.mapper.ResponsavelMapper;
import api.mapper.TarefaMapper;
// Importa a entidade persistida.
import api.model.Responsavel;
// Importa os repositórios usados pelo service.
import api.repository.ResponsavelRepository;
import api.repository.TarefaRepository;

// Exercício 1: mesma fronteira do TarefaService -- entra DTO, sai DTO, e a
// entidade Responsavel fica daqui para baixo.
@Service
public class ResponsavelService {

    // Repositório da tabela responsaveis.
    private final ResponsavelRepository repository;

    // Repositório de tarefas, usado para listar as tarefas de um responsável.
    private final TarefaRepository tarefaRepository;

    // Converte Responsavel <-> DTO.
    private final ResponsavelMapper mapper;

    // Converte as tarefas do responsável para TarefaResponseDTO / TarefaResumoDTO.
    private final TarefaMapper tarefaMapper;

    // Recebe as dependências que o Spring injeta automaticamente.
    public ResponsavelService(ResponsavelRepository repository, TarefaRepository tarefaRepository,
                              ResponsavelMapper mapper, TarefaMapper tarefaMapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.tarefaRepository = tarefaRepository;
        this.mapper = mapper;
        this.tarefaMapper = tarefaMapper;
    }

    // Lista todos os responsáveis em ordem alfabética (exigência do contrato).
    public List<ResponsavelResponseDTO> listarTodos() {
        // O Spring Data gera SELECT ... ORDER BY nome ASC; o mapper converte.
        return mapper.toResponseList(repository.findAllByOrderByNomeAsc());
    }

    // Cria um responsável a partir dos dados validados do DTO.
    public ResponsavelResponseDTO criar(ResponsavelRequestDTO dto) {
        // DTO → entidade → INSERT → entidade com id → DTO de saída.
        Responsavel salvo = repository.save(mapper.toEntity(dto));
        return mapper.toResponse(salvo);
    }

    // Exercício 4: o responsável com o resumo das tarefas dele.
    public ResponsavelDetalheDTO buscarDetalhe(Long id) {
        // Busca o responsável ou lança ResponsavelNaoEncontradoException (404).
        Responsavel responsavel = repository.findById(id)
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
        // Segunda consulta: SELECT ... FROM tarefas WHERE responsavel_id = ?
        // O TarefaMapper converte cada tarefa em resumo.
        List<TarefaResumoDTO> tarefas = tarefaMapper.toResumoList(tarefaRepository.findByResponsavelVinculadoId(id));
        // O ResponsavelMapper junta os dois lados num DTO só.
        return mapper.toDetalhe(responsavel, tarefas);
    }

    // Lista as tarefas vinculadas a um responsável. Responsável inexistente é
    // 404; responsável sem tarefas é 200 com lista vazia.
    public List<TarefaResponseDTO> listarTarefas(Long responsavelId) {
        // Confere a existência antes de consultar as tarefas.
        if (!repository.existsById(responsavelId)) {
            // Mantém o contrato da API retornando 404 para id inexistente.
            throw new ResponsavelNaoEncontradoException(responsavelId);
        }
        // Exercício 1: o formato das tarefas é o mesmo do GET /tarefas.
        return tarefaMapper.toResponseList(tarefaRepository.findByResponsavelVinculadoId(responsavelId));
    }
}
