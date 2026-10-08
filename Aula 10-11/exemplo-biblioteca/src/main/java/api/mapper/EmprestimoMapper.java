// Define o pacote dos mappers.
package api.mapper;

// Importa o tipo de data dos campos calculados.
import java.time.LocalDate;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa o DTO de saída do empréstimo.
import api.dto.EmprestimoResponseDTO;
// Importa a entidade convertida.
import api.model.Emprestimo;

// CAMADA: Mapper
// RESPONSABILIDADE: converter Emprestimo em EmprestimoResponseDTO, montando o
//                   leitor e o livro de dentro da resposta com os outros dois
//                   mappers. dataLimite e atrasado chegam prontos: quem calcula
//                   é o EmprestimoService, porque é regra de negócio (R3) e
//                   o mapper não tem regra de negócio.
// SITUAÇÃO: implementada.
@Component
public class EmprestimoMapper {

    // Monta o leitor que aparece dentro de cada empréstimo.
    private final LeitorMapper leitorMapper;

    // Monta o livro que aparece dentro de cada empréstimo.
    private final LivroMapper livroMapper;

    // Recebe os mappers que vai usar: o Spring injeta os dois.
    public EmprestimoMapper(LeitorMapper leitorMapper, LivroMapper livroMapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.leitorMapper = leitorMapper;
        this.livroMapper = livroMapper;
    }

    // Toda resposta de empréstimo: entra a entidade e os dois campos da regra R3.
    public EmprestimoResponseDTO toResponse(Emprestimo emprestimo, LocalDate dataLimite, boolean atrasado) {
        // Entra um id, sai um objeto: leitor e livro voltam completos.
        return new EmprestimoResponseDTO(
                emprestimo.getId(),
                leitorMapper.toResponse(emprestimo.getLeitor()),
                livroMapper.toResponse(emprestimo.getLivro()),
                emprestimo.getDataRetirada(),
                dataLimite,
                emprestimo.getDataDevolucao(),
                atrasado);
    }
}
