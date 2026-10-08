// Define o pacote da camada de regras de negócio.
package api.service;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o mapper e o repositório que o service vai usar.
import api.mapper.EmprestimoMapper;
import api.repository.EmprestimoRepository;

// CAMADA: Service
// RESPONSABILIDADE: regras de negócio dos empréstimos.
// REGRAS: R1 (no máximo 3 empréstimos em aberto por leitor),
//         R2 (livro emprestado não pode ser emprestado de novo) e
//         R3 (prazo de devolução de 14 dias).
// ROTAS ATENDIDAS: POST /emprestimos e PUT /emprestimos/{id}/devolver.
// SITUAÇÃO: montada. Implementação na próxima etapa.
@Service
public class EmprestimoService {

    // Acesso à tabela emprestimos.
    private final EmprestimoRepository repository;

    // Converte entidade em DTO, já com os campos da regra R3.
    private final EmprestimoMapper mapper;

    // Já recebe as dependências que vai usar: o Spring injeta as duas.
    public EmprestimoService(EmprestimoRepository repository, EmprestimoMapper mapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.mapper = mapper;
    }
}
