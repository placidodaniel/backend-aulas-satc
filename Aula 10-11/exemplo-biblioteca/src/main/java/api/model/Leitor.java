// Define o pacote onde as entidades ficam organizadas.
package api.model;

// Importa a anotação que configura colunas da tabela.
import jakarta.persistence.Column;
// Importa a anotação que transforma a classe em entidade JPA.
import jakarta.persistence.Entity;
// Importa a anotação que ativa a geração automática do id.
import jakarta.persistence.GeneratedValue;
// Importa as estratégias disponíveis para gerar ids.
import jakarta.persistence.GenerationType;
// Importa a anotação que marca a chave primária.
import jakarta.persistence.Id;
// Importa a anotação que define o nome da tabela.
import jakarta.persistence.Table;

// CAMADA: Model
// RESPONSABILIDADE: uma pessoa cadastrada que pode pegar livros emprestados,
//                   gravada na tabela leitores (migration V2).
// SITUAÇÃO: implementada.
// Diz ao JPA que esta classe será persistida no banco.
@Entity
// Liga a entidade à tabela leitores criada pela migration V2.
@Table(name = "leitores")
public class Leitor {

    // Marca o atributo que representa a chave primária.
    @Id
    // Pede ao banco para gerar o id no momento do INSERT.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Guarda o identificador único do leitor.
    private Long id;

    // Exige que o nome nunca seja nulo no banco.
    @Column(nullable = false)
    // Guarda o nome do leitor.
    private String nome;

    // Exige que o e-mail nunca seja nulo no banco.
    @Column(nullable = false)
    // Guarda o e-mail do leitor.
    private String email;

    // Construtor sem argumentos exigido pelo Hibernate para reconstruir entidades.
    protected Leitor() {
    }

    // Construtor usado para cadastrar um leitor novo (id = null, o banco gera no INSERT).
    public Leitor(String nome, String email) {
        // Copia o nome recebido para a entidade.
        this.nome = nome;
        // Copia o e-mail recebido para a entidade.
        this.email = email;
    }

    // Devolve o id do leitor.
    public Long getId() {
        // Retorna o valor guardado no atributo id.
        return id;
    }

    // Devolve o nome do leitor.
    public String getNome() {
        // Retorna o valor guardado no atributo nome.
        return nome;
    }

    // Devolve o e-mail do leitor.
    public String getEmail() {
        // Retorna o valor guardado no atributo email.
        return email;
    }
}
