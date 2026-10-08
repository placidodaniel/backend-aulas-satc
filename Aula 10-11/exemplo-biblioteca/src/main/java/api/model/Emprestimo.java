// Define o pacote onde as entidades ficam organizadas.
package api.model;

// Importa o tipo de data usado na retirada e na devolução.
import java.time.LocalDate;

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
// Importa a anotação que diz qual coluna guarda a chave estrangeira.
import jakarta.persistence.JoinColumn;
// Importa a anotação do relacionamento "muitos para um".
import jakarta.persistence.ManyToOne;
// Importa a anotação que define o nome da tabela.
import jakarta.persistence.Table;

// CAMADA: Model
// RESPONSABILIDADE: a retirada de um livro por um leitor, gravada na tabela
//                   emprestimos (migration V3). É aqui que estão os dois
//                   relacionamentos do modelo: muitos empréstimos para um
//                   leitor, e muitos empréstimos para um livro.
// SITUAÇÃO: implementada. A entidade já está pronta; as regras e as rotas de
//           empréstimos ficam para a próxima etapa.
// Diz ao JPA que esta classe será persistida no banco.
@Entity
// Liga a entidade à tabela emprestimos criada pela migration V3.
@Table(name = "emprestimos")
public class Emprestimo {

    // Marca o atributo que representa a chave primária.
    @Id
    // Pede ao banco para gerar o id no momento do INSERT.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Guarda o identificador único do empréstimo.
    private Long id;

    // Muitos empréstimos podem ser do mesmo leitor.
    @ManyToOne
    // A chave estrangeira fica na coluna leitor_id, obrigatória.
    @JoinColumn(name = "leitor_id", nullable = false)
    // Guarda quem pegou o livro.
    private Leitor leitor;

    // Muitos empréstimos podem ser do mesmo livro, um depois do outro.
    @ManyToOne
    // A chave estrangeira fica na coluna livro_id, obrigatória.
    @JoinColumn(name = "livro_id", nullable = false)
    // Guarda qual livro foi emprestado.
    private Livro livro;

    // Liga o atributo à coluna data_retirada, obrigatória.
    @Column(name = "data_retirada", nullable = false)
    // Dia em que o livro saiu. Quem preenche é a API, com a data do dia.
    private LocalDate dataRetirada;

    // Liga o atributo à coluna data_devolucao, que aceita nulo.
    @Column(name = "data_devolucao")
    // Dia em que o livro voltou. Fica null enquanto o empréstimo está em aberto.
    private LocalDate dataDevolucao;

    // Construtor sem argumentos exigido pelo Hibernate para reconstruir entidades.
    protected Emprestimo() {
    }

    // Construtor usado para registrar um empréstimo novo: o livro sai hoje e
    // ainda não tem data de devolução.
    public Emprestimo(Leitor leitor, Livro livro, LocalDate dataRetirada) {
        // Copia o leitor recebido para a entidade.
        this.leitor = leitor;
        // Copia o livro recebido para a entidade.
        this.livro = livro;
        // Copia a data de retirada recebida para a entidade.
        this.dataRetirada = dataRetirada;
    }

    // Devolve o id do empréstimo.
    public Long getId() {
        // Retorna o valor guardado no atributo id.
        return id;
    }

    // Devolve o leitor do empréstimo.
    public Leitor getLeitor() {
        // Retorna o valor guardado no atributo leitor.
        return leitor;
    }

    // Devolve o livro emprestado.
    public Livro getLivro() {
        // Retorna o valor guardado no atributo livro.
        return livro;
    }

    // Devolve o dia da retirada.
    public LocalDate getDataRetirada() {
        // Retorna o valor guardado no atributo dataRetirada.
        return dataRetirada;
    }

    // Devolve o dia da devolução, ou null se o livro ainda não voltou.
    public LocalDate getDataDevolucao() {
        // Retorna o valor guardado no atributo dataDevolucao.
        return dataDevolucao;
    }
}
