// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o mapper e o repositório que o service vai usar.
import api.mapper.LeitorMapper;
import api.repository.LeitorRepository;

// CAMADA: Service
// RESPONSABILIDADE: cadastrar, listar e buscar leitores.
// REGRAS: nenhuma específica de leitor. O limite de empréstimos por leitor
//         (R1) mora no EmprestimoService.
// ROTAS ATENDIDAS: POST /leitores, GET /leitores e GET /leitores/{id}.
// SITUAÇÃO: montada. Implementação na próxima etapa.
@Service
public class LeitorService {

    // Acesso à tabela leitores.
    private final LeitorRepository repository;

    // Converte DTO <-> entidade.
    private final LeitorMapper mapper;

    // Já recebe as dependências que vai usar: o Spring injeta as duas.
    public LeitorService(LeitorRepository repository, LeitorMapper mapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.mapper = mapper;
    }
}
