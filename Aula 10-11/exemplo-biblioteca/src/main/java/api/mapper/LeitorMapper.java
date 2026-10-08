// Define o pacote dos mappers.
package api.mapper;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa os DTOs de entrada e de saída do leitor.
import api.dto.LeitorRequestDTO;
import api.dto.LeitorResponseDTO;
// Importa a entidade convertida.
import api.model.Leitor;

// CAMADA: Mapper
// RESPONSABILIDADE: converter LeitorRequestDTO em Leitor, e Leitor em LeitorResponseDTO.
//                   Também é usado pelo EmprestimoMapper, para montar o leitor
//                   que aparece dentro de cada empréstimo.
// SITUAÇÃO: implementada. Tem os métodos que POST /leitores e os empréstimos usam.
@Component
public class LeitorMapper {

    // POST /leitores: monta um leitor novo a partir do DTO (id = null, o banco gera).
    public Leitor toEntity(LeitorRequestDTO dto) {
        // Usa o construtor de cadastro da entidade.
        return new Leitor(dto.nome(), dto.email());
    }

    // Toda resposta: copia da entidade só o que o contrato da API mostra.
    public LeitorResponseDTO toResponse(Leitor leitor) {
        // Monta o record com os três campos de saída.
        return new LeitorResponseDTO(leitor.getId(), leitor.getNome(), leitor.getEmail());
    }
}
