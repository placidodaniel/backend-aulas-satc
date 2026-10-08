// Define o pacote dos repositórios.
package api.repository;

// Importa o repositório do Spring Data JPA que já traz o CRUD pronto.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa a entidade que este repositório acessa.
import api.model.Emprestimo;

// CAMADA: Repository
// RESPONSABILIDADE: ler e gravar empréstimos na tabela emprestimos.
// SITUAÇÃO: implementada. Na próxima etapa ganha as consultas das regras, por
//           exemplo contar os empréstimos em aberto de um leitor (R1).
// <Emprestimo, Long>: a entidade e o tipo do id. O Spring escreve a implementação.
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
}
