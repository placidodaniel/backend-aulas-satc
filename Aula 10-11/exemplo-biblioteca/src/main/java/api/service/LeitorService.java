// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa os DTOs de entrada e de saída do leitor.
import api.dto.LeitorRequestDTO;
import api.dto.LeitorResponseDTO;
// Importa o mapper e o repositório que o service usa.
import api.mapper.LeitorMapper;
import api.repository.LeitorRepository;

// CAMADA: Service
// RESPONSABILIDADE: cadastrar leitores.
// REGRAS: nenhuma específica de leitor. O limite de empréstimos por leitor
//         (R1) mora no EmprestimoService.
// ROTAS ATENDIDAS: POST /leitores. GET /leitores e GET /leitores/{id} estão no
//                  contrato e entram na próxima etapa.
// SITUAÇÃO: implementada em parte. Só o cadastro, que as regras precisam: sem
//           leitor cadastrado, não há empréstimo.
@Service
public class LeitorService {

    // Acesso à tabela leitores.
    private final LeitorRepository repository;

    // Converte DTO <-> entidade.
    private final LeitorMapper mapper;

    // Recebe as dependências que o Spring injeta automaticamente.
    public LeitorService(LeitorRepository repository, LeitorMapper mapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.mapper = mapper;
    }

    // POST /leitores: monta a entidade, grava e devolve o leitor com o id gerado.
    public LeitorResponseDTO criar(LeitorRequestDTO dto) {
        // Grava o leitor novo e converte o resultado, já com o id.
        return mapper.toResponse(repository.save(mapper.toEntity(dto)));
    }
}
