// Define o pacote dos mappers.
package api.mapper;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// CAMADA: Mapper
// RESPONSABILIDADE: converter Emprestimo em EmprestimoResponseDTO. Monta o
//                   leitor e o livro de dentro da resposta com os outros dois
//                   mappers e calcula os campos da regra R3: dataLimite
//                   (retirada + 14 dias) e atrasado.
// SITUAÇÃO: montada. Implementação na próxima etapa.
@Component
public class EmprestimoMapper {

    // Monta o leitor que aparece dentro de cada empréstimo.
    private final LeitorMapper leitorMapper;

    // Monta o livro que aparece dentro de cada empréstimo.
    private final LivroMapper livroMapper;

    // Já recebe os mappers que vai usar: o Spring injeta os dois.
    public EmprestimoMapper(LeitorMapper leitorMapper, LivroMapper livroMapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.leitorMapper = leitorMapper;
        this.livroMapper = livroMapper;
    }
}
