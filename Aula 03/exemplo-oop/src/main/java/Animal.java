// Sem "package ...;" no topo -> classe cai no pacote default (sem nome), igual às outras
// deste projeto. Por isso Main, Cachorro, Gato e Rastreavel são usados aqui sem import.
// Em produção, todo projeto real declara um pacote nomeado (ex: "package com.empresa.projeto;")
// e organiza as pastas espelhando esse nome; pacote default é aceitável só em exemplo/exercício de aula.

// Animal implementa a interface Rastreavel, ou seja, é obrigado a fornecer rastrear()
public class Animal implements Rastreavel {
    private String nome; // nome do animal
    private String raca; // raça do animal
    private String dono; // nome do dono/tutor
    private int latitude; // coordenada de posição (eixo 1)
    private int longitude; // coordenada de posição (eixo 2)

    @Override
    public String rastrear() {
        // implementação do contrato de Rastreavel: devolve a posição como "lat / long"
        return latitude + " / " + longitude;
    }

    @Override
    public String toString() {
        // sobrescreve o toString() padrão do Object para exibir os dados formatados
        return "Nome: " + this.nome + "\nRaça: " + this.raca + "\nDono: " + this.dono;
    }

    public void emitirSom() {
        System.out.println("Som silencioso"); // som genérico, subclasses podem sobrescrever
    }

    public String getNome() {
        return nome; // devolve o nome atual
    }

    public void setNome(String nome) {
        if (nome.isEmpty()) return; // validação simples: ignora nome vazio
        this.nome = nome; // grava o novo nome
    }

    public String getRaca() {
        return raca; // devolve a raça atual
    }

    public void setRaca(String raca) {
        this.raca = raca; // grava a nova raça
    }

    public String getDono() {
        return dono; // devolve o dono atual
    }

    public void setDono(String dono) {
        this.dono = dono; // grava o novo dono
    }

    public int getLatitude() {
        return latitude; // devolve a latitude atual
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude; // grava a nova latitude
    }

    public int getLongitude() {
        return longitude; // devolve a longitude atual
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude; // grava a nova longitude
    }

    public void setPosicao(int latitude, int longitude) {
        // atalho para setar latitude e longitude de uma vez só
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
