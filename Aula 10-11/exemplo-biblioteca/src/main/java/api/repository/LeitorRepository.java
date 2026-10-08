// Define o pacote dos repositórios.
package api.repository;

// Importa o repositório do Spring Data JPA que já traz o CRUD pronto.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa a entidade que este repositório acessa.
import api.model.Leitor;

// CAMADA: Repository
// RESPONSABILIDADE: ler e gravar leitores na tabela leitores.
// SITUAÇÃO: implementada. Ainda ninguém usa: o LeitorService está só montado.
// <Leitor, Long>: a entidade e o tipo do id. O Spring escreve a implementação.
public interface LeitorRepository extends JpaRepository<Leitor, Long> {
}
