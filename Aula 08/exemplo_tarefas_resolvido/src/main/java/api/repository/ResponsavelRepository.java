// Define o pacote responsável pelo acesso a dados.
package api.repository;

// Importa a lista devolvida pelo método derivado.
import java.util.List;

// Importa a entidade que este repositório irá persistir.
import api.model.Responsavel;
// Importa a interface pronta de operações CRUD do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Exercício 6: repositório da entidade Responsavel -- mesmo modelo do TarefaRepository.
public interface ResponsavelRepository extends JpaRepository<Responsavel, Long> {

    // O contrato pede a lista ordenada por nome: ORDER BY nome ASC.
    List<Responsavel> findAllByOrderByNomeAsc();
}
