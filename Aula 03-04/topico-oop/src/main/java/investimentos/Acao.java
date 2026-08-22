// Este arquivo pertence ao pacote "investimentos" e mora em src/main/java/investimentos/.
// Nome completo desta classe: investimentos.Acao
//
// Declara o pacote; a pasta investimentos/ precisa existir e ter exatamente este nome.
package investimentos;

// Locale vem de java.util. Representa idioma/país e serve para formatar o dinheiro.
import java.util.Locale;

import tributaveis.Tributavel;

// >>> INTERFACE (sem HERANÇA): repare no "implements" SEM "extends". Acao não herda nada de
// Conta -- não tem titular, saldo nem saque. A única coisa em comum é o contrato assinado.
// Este é o exemplo que separa os dois conceitos: herança é parentesco, interface é compromisso.
//
// >>> CLASSE: uma classe independente, em outro pacote, com atributos totalmente diferentes.
//
// A ordem dos membros segue a mesma convenção usada em Conta:
// constantes -> atributos -> construtor -> métodos de negócio -> getters -> toString().
public class Acao implements Tributavel {

    // ---------------------------------------------------------------- constantes

    // "static final" = constante: uma só para a classe e que não muda. 15% (regra fictícia).
    private static final double ALIQUOTA_IR = 0.15;

    // Locale do Brasil: faz o valor sair como 1.234,56 em vez de 1,234.56.
    private static final Locale BR = Locale.of("pt", "BR");

    // Mesma largura do extrato, para os dois documentos combinarem na tela.
    private static final int LARGURA = 45;

    // ---------------------------------------------------------------- atributos

    // >>> ENCAPSULAMENTO: private aqui também, igual nas contas. E como os dois atributos são
    // final e só têm getter, esta classe é imutável: depois do "new", nada muda de valor.
    //
    // Código de negociação na bolsa (ex: "PETR4"). final: uma ação não troca de código.
    private final String codigo;

    // Lucro obtido na venda. Também final: é o resultado da operação que já aconteceu.
    private final double lucro;

    // ---------------------------------------------------------------- construtor

    // >>> OBJETO: cada "new Acao(...)" cria um exemplar próprio a partir desta classe.
    // Note que a assinatura do construtor não tem nada a ver com a das contas -- classes
    // sem parentesco não precisam combinar em nada, só em cumprir o contrato.
    //
    // CONSTRUTOR: mesmo nome da classe, sem tipo de retorno, roda uma vez no "new".
    // Como os dois atributos são final, este é o único lugar onde eles podem ser gravados.
    public Acao(String codigo, double lucro) {

        // this.codigo é o atributo; codigo (sem this) é o parâmetro que chegou.
        this.codigo = codigo;

        // Mesma coisa para o lucro.
        this.lucro = lucro;

    // Fim do construtor.
    }

    // ---------------------------------------------------------------- métodos de negócio

    // >>> INTERFACE: método exigido pelo contrato Tributavel. @Override avisa o compilador.
    @Override
    // >>> POLIMORFISMO: MESMA assinatura que existe em Conta...
    public double calcularImposto() {

        // ...e cálculo completamente diferente: 15% do lucro, contra 0,5% do saldo lá.
        // Uma linha só no Main -- tributavel.calcularImposto() -- dispara um ou outro.
        return lucro * ALIQUOTA_IR;

    // Fim de calcularImposto().
    }

    // ---------------------------------------------------------------- getters

    // Getter sem setter: o código é definido no construtor e nunca mais muda.
    public String getCodigo() {

        // Devolve o código atual.
        return codigo;

    // Fim de getCodigo().
    }

    // Getter sem setter, pelo mesmo motivo.
    public double getLucro() {

        // Devolve o lucro apurado.
        return lucro;

    // Fim de getLucro().
    }

    // ---------------------------------------------------------------- toString

    // Sobrescreve o toString() herdado de Object.
    @Override
    // Chamado automaticamente por System.out.println(acao).
    public String toString() {

        // Acao também sabe se apresentar, mas o documento dela NÃO é um extrato bancário:
        // é uma nota de corretagem. Mesma ideia de toString(), formato próprio.
        //
        // repeat() repete o caractere N vezes; \n quebra a linha.
        String nota = "=".repeat(LARGURA) + "\n";

        // Título diferente do extrato, de propósito: é outro tipo de documento.
        nota += "       BANCO SATC - NOTA DE CORRETAGEM\n";

        // Segunda linha dupla: fecha o cabeçalho.
        nota += "=".repeat(LARGURA) + "\n";

        // Aqui não existe titular nem número de conta -- Acao tem atributos próprios.
        nota += "Ativo   : " + this.codigo + "\n";

        // Linha simples separa o cabeçalho do conteúdo.
        nota += "-".repeat(LARGURA) + "\n";

        // Cabeçalho das colunas, mesmo alinhamento usado nos extratos.
        nota += String.format("%-28s %16s", "DESCRIÇÃO", "VALOR (R$)") + "\n";

        // Única movimentação da nota: o lucro apurado na venda.
        nota += String.format(BR, "%-28.28s %,16.2f", "Lucro na venda", this.lucro) + "\n";

        // Separa o conteúdo do total.
        nota += "-".repeat(LARGURA) + "\n";

        // Chama o método do contrato: 15% do lucro.
        nota += String.format(BR, "%-28.28s %,16.2f", "Imposto (IR 15%)", calcularImposto()) + "\n";

        // Rodapé. Sem \n no fim para não sobrar uma linha em branco depois.
        nota += "=".repeat(LARGURA);

        // Devolve o texto pronto.
        return nota;

    // Fim de toString().
    }

// Fim da classe Acao.
}
