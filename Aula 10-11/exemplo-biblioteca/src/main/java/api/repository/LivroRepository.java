// Define o pacote dos repositórios.
package api.repository;

// Importa o repositório do Spring Data JPA que já traz o CRUD pronto.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa a entidade que este repositório acessa.
import api.model.Livro;

// CAMADA: Repository
// RESPONSABILIDADE: ler e gravar livros na tabela livros.
// SITUAÇÃO: implementada. O save, o findById e o findAll que a API simples
//           usa já vêm do JpaRepository.
// <Livro, Long>: a entidade e o tipo do id. O Spring escreve a implementação.
public interface LivroRepository extends JpaRepository<Livro, Long> {
}
