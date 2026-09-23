// Define o pacote responsável pelo acesso a dados.
package api.repository;

// Importa a entidade que este repositório irá persistir.
import api.model.Tarefa;
// Importa a interface pronta de operações CRUD do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Declara um repositório para a entidade Tarefa.
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    // O Spring cria a implementação em runtime usando Tarefa como entidade e Long como tipo do id.
}
