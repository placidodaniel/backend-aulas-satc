// Define o pacote responsável pelo acesso a dados.
package api.repository;

// Importa o tipo de data usado na consulta de atrasadas.
import java.time.LocalDate;
// Importa a lista devolvida pelos métodos derivados.
import java.util.List;

// Importa a entidade que este repositório irá persistir.
import api.model.Tarefa;
// Importa a interface pronta de operações CRUD do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Declara um repositório para a entidade Tarefa.
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    // O Spring cria a implementação em runtime usando Tarefa como entidade e Long como tipo do id.

    // Aula 08 (Ex. 1): WHERE upper(responsavel) = upper(?)
    List<Tarefa> findByResponsavelIgnoreCase(String responsavel);

    // Aula 08 (Ex. 2): WHERE concluida = false AND data_prazo < ?
    List<Tarefa> findByConcluidaFalseAndDataPrazoBefore(LocalDate data);

    // Aula 08 (Ex. 4): ORDER BY data_prazo ASC
    List<Tarefa> findAllByOrderByDataPrazoAsc();

    // Aula 08 (Ex. 6): "ResponsavelVinculado" + "Id" navega pelo relacionamento --
    // vira WHERE responsavel_id = ?
    List<Tarefa> findByResponsavelVinculadoId(Long responsavelId);
}
