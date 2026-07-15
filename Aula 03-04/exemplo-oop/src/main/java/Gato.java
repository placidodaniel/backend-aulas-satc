// Sem "package ...;" no topo -> classe cai no pacote default (sem nome), igual às outras
// deste projeto. Por isso Main consegue usar Gato sem import. Em produção, todo projeto
// real declara um pacote nomeado (ex: "package com.empresa.projeto;"); pacote default é
// aceitável só em exemplo/exercício de aula.

// Gato é uma subclasse de Animal: herda nome, raca, dono, latitude, longitude
// e os métodos (getters/setters, rastrear(), emitirSom(), toString()) sem reescrevê-los
public class Gato extends Animal {
    // corpo vazio: Gato não adiciona nem sobrescreve nada, só reaproveita o de Animal
}
