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

// Exercício 6: uma pessoa cadastrada que pode ser responsável por tarefas.
// Não existe aqui uma List<Tarefa> (@OneToMany): o Jackson entraria em loop
// (tarefa → responsável → tarefas → responsável...) ao gerar o JSON. As
// tarefas de um responsável vêm de TarefaRepository.findByResponsavelVinculadoId().
// Diz ao JPA que esta classe será persistida no banco.
@Entity
// Liga a entidade à tabela responsaveis criada pela migration V3.
@Table(name = "responsaveis")
public class Responsavel {

    // Marca o atributo que representa a chave primária.
    @Id
    // Pede ao banco para gerar o id no momento do INSERT.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Guarda o identificador único do responsável.
    private Long id;

    // Exige que o nome nunca seja nulo no banco.
    @Column(nullable = false)
    // Guarda o nome do responsável.
    private String nome;

    // Exige que o e-mail nunca seja nulo no banco.
    @Column(nullable = false)
    // Guarda o e-mail do responsável.
    private String email;

    // Construtor sem argumentos exigido pelo Hibernate para reconstruir entidades.
    protected Responsavel() {
    }

    // Construtor usado pela aplicação para criar um responsável novo (id = null,
    // o banco gera no INSERT).
    public Responsavel(String nome, String email) {
        // Copia o nome recebido para a entidade.
        this.nome = nome;
        // Copia o e-mail recebido para a entidade.
        this.email = email;
    }

    // Devolve o id do responsável.
    public Long getId() {
        // Retorna o valor armazenado no atributo id.
        return id;
    }

    // Devolve o nome do responsável.
    public String getNome() {
        // Retorna o valor armazenado no atributo nome.
        return nome;
    }

    // Devolve o e-mail do responsável.
    public String getEmail() {
        // Retorna o valor armazenado no atributo email.
        return email;
    }
}
