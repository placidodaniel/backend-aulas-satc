package api.model;

import java.time.LocalDate;

// Uma tarefa: id é gerado pelo Repository, nunca pelo cliente da API. O mesmo
// vale pra dataCadastro -- é sempre "agora" (o momento em que a tarefa nasce),
// nunca informada por quem chama a API.
public class Tarefa {

    private final Long id;
    private String titulo;
    private boolean concluida;
    private String responsavel;
    private LocalDate dataPrazo;
    private final LocalDate dataCadastro;

    public Tarefa(Long id, String titulo, String responsavel, LocalDate dataPrazo) {
        this.id = id;
        this.titulo = titulo;
        this.concluida = false;
        this.responsavel = responsavel;
        this.dataPrazo = dataPrazo;
        this.dataCadastro = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // atributo boolean usa "is", não "get" (convenção Java).
    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    // Sem setter -- só o construtor define, uma vez, no momento em que a
    // tarefa nasce (igual o "id"). Ninguém deveria poder mudar essa data depois.
    public LocalDate getDataCadastro() {
        return dataCadastro;
    }
}
