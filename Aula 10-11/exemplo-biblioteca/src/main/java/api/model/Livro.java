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
// RESPONSABILIDADE: um exemplar do acervo, gravado na tabela livros (migration V1).
// SITUAÇÃO: implementada.
// Diz ao JPA que esta classe será persistida no banco.
@Entity
// Liga a entidade à tabela livros criada pela migration V1.
@Table(name = "livros")
public class Livro {

    // Marca o atributo que representa a chave primária.
    @Id
    // Pede ao banco para gerar o id no momento do INSERT.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Guarda o identificador único do exemplar.
    private Long id;

    // Exige que o título nunca seja nulo no banco.
    @Column(nullable = false)
    // Guarda o título do livro.
    private String titulo;

    // Exige o ISBN e limita o tamanho ao da coluna.
    @Column(nullable = false, length = 20)
    // Guarda o ISBN. Exemplares do mesmo título repetem o mesmo ISBN.
    private String isbn;

    // Exige que a disponibilidade sempre tenha valor.
    @Column(nullable = false)
    // Diz se o exemplar está na estante. Quem muda é a API, nunca o cliente.
    private boolean disponivel = true;

    // Construtor sem argumentos exigido pelo Hibernate para reconstruir entidades.
    protected Livro() {
    }

    // Construtor usado para cadastrar um livro novo: id = null (o banco gera no
    // INSERT) e disponivel = true (todo exemplar novo chega na estante).
    public Livro(String titulo, String isbn) {
        // Copia o título recebido para a entidade.
        this.titulo = titulo;
        // Copia o ISBN recebido para a entidade.
        this.isbn = isbn;
    }

    // Devolve o id do exemplar.
    public Long getId() {
        // Retorna o valor guardado no atributo id.
        return id;
    }

    // Devolve o título do livro.
    public String getTitulo() {
        // Retorna o valor guardado no atributo titulo.
        return titulo;
    }

    // Devolve o ISBN do livro.
    public String getIsbn() {
        // Retorna o valor guardado no atributo isbn.
        return isbn;
    }

    // Diz se o exemplar está disponível para empréstimo.
    public boolean isDisponivel() {
        // Retorna o valor guardado no atributo disponivel.
        return disponivel;
    }

    // Tira o exemplar da estante. Não existe setDisponivel: de fora, só dá para
    // emprestar ou devolver (encapsulamento, Aulas 03-04). Quem chama é o EmprestimoService.
    public void emprestar() {
        // Marca o exemplar como emprestado.
        this.disponivel = false;
    }

    // Põe o exemplar de volta na estante.
    public void devolver() {
        // Marca o exemplar como disponível de novo.
        this.disponivel = true;
    }
}
