// Mesmo pacote de Conta (contas), por isso NÃO precisa de "import contas.Conta;".
//
// Declara o pacote; a pasta contas/ precisa existir e ter exatamente este nome.
package contas;

// >>> HERANÇA: "extends" de novo, mas usada de um jeito diferente da ContaCorrente.
// Esta subclasse não sobrescreve NADA -- em vez disso, ADICIONA um comportamento que a mãe
// não tem (aplicarRendimento). São os dois caminhos que a herança oferece:
//   ContaCorrente -> TROCA uma regra herdada (sobrescreve)
//   ContaPoupanca -> SOMA uma regra nova (acrescenta)
//
// >>> CLASSE: ContaPoupanca É uma Conta, mas é uma classe distinta, com constante própria.
public class ContaPoupanca extends Conta {

    // Constante só desta subclasse: 0,5% ao mês (regra fictícia da aula).
    private static final double RENDIMENTO_MENSAL = 0.005;

    // >>> OBJETO + HERANÇA: construtor NÃO se herda; cada classe declara o seu.
    public ContaPoupanca(String titular, String numero) {

        // >>> HERANÇA: "mãe, monta a SUA parte do objeto com estes dois dados".
        // A Conta grava titular e numero (que são private e ninguém mais alcança);
        // depois a execução volta para cá. Tem que ser a PRIMEIRA linha do construtor.
        //
        // A explicação passo a passo está em ContaCorrente.java, no super(...) de lá --
        // as duas subclasses fazem exatamente a mesma coisa.
        super(titular, numero);

    // Fim do construtor.
    }

    // Método NOVO: não existe em Conta nem em ContaCorrente.
    public void aplicarRendimento() {

        // Reaproveita depositar() da mãe, na versão protected que aceita a descrição --
        // assim o extrato mostra "Rendimento" e não "Depósito".
        // >>> ENCAPSULAMENTO: nem a subclasse consegue tocar em "saldo" direto -- ele é private
        // na mãe, e private não abre nem para a família. Até aqui dentro o caminho é passar
        // pelos métodos: lê com getSaldo(), escreve com depositar().
        depositar(getSaldo() * RENDIMENTO_MENSAL, "Rendimento (0,5% a.m.)");

    // Fim de aplicarRendimento().
    }

    // >>> ABSTRAÇÃO (Exercício 3): implementação obrigatória do método abstrato da mãe.
    @Override
    public String tipoDeConta() {

        // Repare: agora sai "Poupança", com cedilha e til. O nome da CLASSE não permitia isso.
        return "Poupança";

    // Fim de tipoDeConta().
    }

// Fim da classe ContaPoupanca.
}
