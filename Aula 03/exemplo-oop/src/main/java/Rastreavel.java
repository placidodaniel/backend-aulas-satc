// Sem "package ...;" no topo -> interface cai no pacote default (sem nome), igual às outras
// deste projeto. Por isso Main, Animal e Robo usam Rastreavel sem import. Em produção,
// todo projeto real declara um pacote nomeado (ex: "package com.empresa.projeto;");
// pacote default é aceitável só em exemplo/exercício de aula.

// Interface: só define o "contrato" rastrear(), sem implementação.
// Quem implementa (Animal e Robo) é obrigado a fornecer o próprio código do método.
public interface Rastreavel {
    public String rastrear(); // assinatura que toda classe implementadora deve preencher
}
