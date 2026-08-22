// Mesmo pacote de Conta (contas), por isso NÃO precisa de "import contas.Conta;".
//
// Declara o pacote; a pasta contas/ precisa existir e ter exatamente este nome.
package contas;

// GABARITO DO EXERCÍCIO 4.
//
// >>> HERANÇA: ContaInvestimento É uma Conta.
//
// >>> POLIMORFISMO: e é a única das contas que sobrescreve calcularImposto(). Nas outras,
// o imposto incide sobre o SALDO. Aqui incide só sobre o LUCRO: aplicou R$ 1.000 e o saldo
// está R$ 1.100? O IR de 22,5% morde os R$ 100 de rendimento. Sem lucro, imposto zero.
public class ContaInvestimento extends Conta {

    // ---------------------------------------------------------------- constantes

    // Alíquota do imposto de renda sobre o rendimento (regra fictícia da aula).
    private static final double ALIQUOTA_IR = 0.225;

    // ---------------------------------------------------------------- atributos

    // >>> ENCAPSULAMENTO: soma de tudo que o CLIENTE aplicou, sem contar rendimento.
    // É a memória que permite separar "dinheiro que entrou" de "lucro que rendeu".
    private double totalAplicado;

    // ---------------------------------------------------------------- construtor

    // >>> OBJETO + HERANÇA: construtor não se herda; cada classe declara o seu.
    public ContaInvestimento(String titular, String numero) {

        // >>> HERANÇA: "mãe, monta a SUA parte do objeto com estes dois dados".
        super(titular, numero);

    // Fim do construtor.
    }

    // ---------------------------------------------------------------- métodos de negócio

    // >>> POLIMORFISMO: todo depósito do cliente passa por aqui, e é contabilizado
    // como aplicação. Sobrescrevemos a versão de 2 parâmetros pelo mesmo motivo da
    // ContaEstrangeira: é por ela que o depositar(double) da mãe passa.
    @Override
    protected void depositar(double valor, String descricao) {

        // Primeiro deixa a mãe fazer o trabalho de sempre: validar, somar, lançar no extrato.
        super.depositar(valor, descricao);

        // Só conta como aplicado se o depósito realmente aconteceu (a mãe recusa <= 0).
        if (valor > 0) this.totalAplicado += valor;

    // Fim de depositar(double, String).
    }

    // Método NOVO. Diferente da poupança, aqui o percentual é informado por quem chama --
    // investimento não tem taxa fixa.
    public void aplicarRendimento(double percentual) {

        // A PEGADINHA DO EXERCÍCIO está nesta linha, e é o "super.".
        //
        // Rendimento é LUCRO, não é dinheiro que o cliente aplicou. Se chamássemos
        // depositar(...) sem o "super.", cairíamos na versão sobrescrita logo acima,
        // que somaria o lucro em totalAplicado -- e aí lucro = saldo - totalAplicado
        // daria SEMPRE zero, e o imposto sumiria.
        //
        // Com "super.", pulamos a versão desta classe e vamos direto à da mãe: o saldo
        // cresce e o extrato registra, mas totalAplicado NÃO se mexe.
        super.depositar(getSaldo() * percentual, "Rendimento (" + (percentual * 100) + "%)");

    // Fim de aplicarRendimento().
    }

    // >>> POLIMORFISMO + INTERFACE: calcularImposto() nasceu na interface Tributavel,
    // foi implementado na Conta (0,5% do saldo) e aqui é SOBRESCRITO de novo.
    // Três classes, três regras, uma assinatura só.
    @Override
    public double calcularImposto() {

        // Lucro = o que o saldo cresceu além do que foi aplicado pelo cliente.
        double lucro = getSaldo() - totalAplicado;

        // Sem lucro não há imposto. O ternário evita imposto negativo virar "desconto".
        return lucro > 0 ? lucro * ALIQUOTA_IR : 0;

    // Fim de calcularImposto().
    }

    // >>> ABSTRAÇÃO (Exercício 3): implementação obrigatória do método abstrato da mãe.
    @Override
    public String tipoDeConta() {

        // O texto que aparece no cabeçalho do extrato.
        return "Investimento";

    // Fim de tipoDeConta().
    }

// Fim da classe ContaInvestimento.
}
