// Declara o pacote dos mappers -- as classes que convertem DTO <-> entidade.
package api.mapper;

// Importa os tipos de coleção usados na conversão de listas.
import java.util.Collection;
import java.util.List;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa os DTOs (o lado "contrato da API").
import api.dto.ResponsavelDetalheDTO;
import api.dto.ResponsavelRequestDTO;
import api.dto.ResponsavelResponseDTO;
import api.dto.TarefaResumoDTO;
// Importa a entidade (o lado "banco de dados").
import api.model.Responsavel;

// Exercício 1: o único lugar que sabe converter Responsavel <-> DTO. O
// TarefaMapper também usa esta classe para montar o "responsavelVinculado".
//
// Exercício 4: não injeta o TarefaMapper! O TarefaMapper já depende deste
// mapper; se este dependesse dele de volta, o Spring não conseguiria criar
// nenhum dos dois (dependência circular) e a aplicação não subiria. Por isso
// toDetalhe() recebe as tarefas JÁ convertidas pelo Service.
// Registra a classe como bean para poder ser injetada.
@Component
public class ResponsavelMapper {

    // POST: DTO de entrada (já validado) → entidade nova, ainda sem id.
    public Responsavel toEntity(ResponsavelRequestDTO dto) {
        // Usa o construtor público da entidade, o mesmo da Aula 08.
        return new Responsavel(dto.nome(), dto.email());
    }

    // Entidade → DTO de saída {id, nome, email}. Aceita null e devolve null:
    // é o caso de uma tarefa sem responsável vinculado.
    public ResponsavelResponseDTO toResponse(Responsavel responsavel) {
        // Sem responsável, não há o que converter.
        if (responsavel == null) {
            return null;
        }
        // Copia os três campos do contrato.
        return new ResponsavelResponseDTO(responsavel.getId(), responsavel.getNome(), responsavel.getEmail());
    }

    // Converte uma lista inteira reaproveitando o toResponse() de um item.
    public List<ResponsavelResponseDTO> toResponseList(Collection<Responsavel> responsaveis) {
        // stream().map(...): aplica toResponse em cada item; toList() junta de novo.
        return responsaveis.stream().map(this::toResponse).toList();
    }

    // Exercício 4: monta o DTO composto a partir do responsável e do resumo
    // das tarefas dele. Os dois totais são calculados aqui, a partir da lista.
    public ResponsavelDetalheDTO toDetalhe(Responsavel responsavel, List<TarefaResumoDTO> tarefas) {
        // Conta quantas tarefas da lista ainda não foram concluídas.
        int pendentes = (int) tarefas.stream().filter(tarefa -> !tarefa.concluida()).count();
        // Junta os dados do responsável, os totais e a lista num único record.
        return new ResponsavelDetalheDTO(
                responsavel.getId(),
                responsavel.getNome(),
                responsavel.getEmail(),
                tarefas.size(),
                pendentes,
                tarefas);
    }
}
