// Sem "package ...;" no topo -> classe cai no pacote default (sem nome), igual às outras
// deste projeto. Por isso Main usa Robo e Rastreavel sem import. Em produção, todo projeto
// real declara um pacote nomeado (ex: "package com.empresa.projeto;"); pacote default é
// aceitável só em exemplo/exercício de aula.

// Robo não herda de Animal (não tem raça/dono/latitude/longitude), mas implementa
// Rastreavel do seu próprio jeito, guardando a posição como texto (ex: "Janela")
public class Robo implements Rastreavel {
    private String nome; // nome/identificação do robô
    private String posicao; // posição descrita como texto, ao contrário de Animal (lat/long)

    @Override
    public String rastrear() {
        return posicao; // aqui rastrear() simplesmente devolve o texto da posição
    }

    public String getNome() {
        return nome; // devolve o nome atual
    }

    public void setNome(String nome) {
        this.nome = nome; // grava o novo nome
    }

    public String getPosicao() {
        return posicao; // devolve a posição atual
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao; // grava a nova posição
    }
}
