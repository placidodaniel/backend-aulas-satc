// Define o pacote onde a entidade Tarefa fica organizada.
package api.model;

// Importa o tipo de data usado para prazo e cadastro.
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
// Importa o callback executado antes do INSERT.
import jakarta.persistence.PrePersist;
// Importa a anotação que define o nome da tabela.
import jakarta.persistence.Table;

// Uma tarefa: id é gerado pelo JPA, nunca pelo cliente da API. dataCadastro é
// preenchida pela própria entidade no momento em que ela é persistida.
// Diz ao JPA que esta classe será persistida no banco.
@Entity
// Liga a entidade à tabela tarefas criada pela migration.
@Table(name = "tarefas")
public class Tarefa {

    // Marca o atributo que representa a chave primária.
    @Id
    // Pede ao banco para gerar o id no momento do INSERT.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Guarda o identificador único da tarefa.
    private Long id;

    // Exige que o valor correspondente nunca seja nulo no banco.
    @Column(nullable = false)
    // Guarda o título que será persistido na coluna titulo.
    private String titulo;

    // Exige que o estado da tarefa sempre tenha um valor.
    @Column(nullable = false)
    // Guarda true para concluída e false para pendente.
    private boolean concluida;

    // Exige que toda tarefa tenha um responsável.
    @Column(nullable = false)
    // Guarda o nome da pessoa responsável pela tarefa.
    private String responsavel;

    // Informa explicitamente o nome snake_case usado no banco.
    @Column(name = "data_prazo", nullable = false)
    // Guarda a data limite para concluir a tarefa.
    private LocalDate dataPrazo;

    // Mapeia o atributo Java para a coluna data_cadastro.
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    // Guarda a data em que a tarefa foi criada.
    private LocalDate dataCadastro;

    // Construtor sem argumentos exigido pelo Hibernate para reconstruir entidades.
    protected Tarefa() {
    }

    // Construtor usado pela aplicação para criar uma tarefa nova.
    public Tarefa(Long id, String titulo, String responsavel, LocalDate dataPrazo) {
        // Recebe o id; normalmente ele será null e o banco irá gerá-lo.
        this.id = id;
        // Copia o título recebido para a entidade.
        this.titulo = titulo;
        // Toda tarefa nova começa pendente.
        this.concluida = false;
        // Copia o responsável recebido para a entidade.
        this.responsavel = responsavel;
        // Copia o prazo recebido para a entidade.
        this.dataPrazo = dataPrazo;
        // Define a data de cadastro no momento da criação do objeto.
        this.dataCadastro = LocalDate.now();
    }

    // Executa antes do Hibernate inserir a entidade no banco.
    @PrePersist
    // Garante uma data mesmo quando o Hibernate usa o construtor protegido.
    private void definirDataCadastro() {
        // Só define a data se ela ainda não tiver sido preenchida.
        if (dataCadastro == null) {
            // Usa a data atual como valor de cadastro.
            dataCadastro = LocalDate.now();
        }
    }

    // Devolve o id da tarefa para o service e para o Jackson.
    public Long getId() {
        // Retorna o valor armazenado no atributo id.
        return id;
    }

    // Devolve o título da tarefa.
    public String getTitulo() {
        // Retorna o valor armazenado no atributo titulo.
        return titulo;
    }

    // Permite alterar o título de uma tarefa existente.
    public void setTitulo(String titulo) {
        // Substitui o título antigo pelo valor recebido.
        this.titulo = titulo;
    }

    // atributo boolean usa "is", não "get" (convenção Java).
    // Devolve o estado atual da tarefa.
    public boolean isConcluida() {
        // Retorna true ou false conforme o estado persistido.
        return concluida;
    }

    // Altera o estado de conclusão da tarefa.
    public void setConcluida(boolean concluida) {
        // Guarda o novo estado no atributo da entidade.
        this.concluida = concluida;
    }

    // Devolve o responsável pela tarefa.
    public String getResponsavel() {
        // Retorna o valor armazenado no atributo responsavel.
        return responsavel;
    }

    // Permite alterar o responsável da tarefa.
    public void setResponsavel(String responsavel) {
        // Guarda o novo responsável na entidade.
        this.responsavel = responsavel;
    }

    // Devolve o prazo da tarefa.
    public LocalDate getDataPrazo() {
        // Retorna o valor armazenado no atributo dataPrazo.
        return dataPrazo;
    }

    // Permite alterar o prazo da tarefa.
    public void setDataPrazo(LocalDate dataPrazo) {
        // Guarda o novo prazo na entidade.
        this.dataPrazo = dataPrazo;
    }

    // Sem setter -- só o construtor define, uma vez, no momento em que a
    // tarefa nasce (igual o "id"). Ninguém deveria poder mudar essa data depois.
    // Devolve a data de cadastro sem permitir alteração externa.
    public LocalDate getDataCadastro() {
        // Retorna o valor armazenado no atributo dataCadastro.
        return dataCadastro;
    }
}
