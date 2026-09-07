// Mesmo pacote de Conta (contas), por isso NÃO precisa de "import contas.Conta;".
//
// Declara o pacote; a pasta contas/ precisa existir e ter exatamente este nome.
package contas;

// GABARITO DO EXERCÍCIO 2.
//
// >>> HERANÇA: ContaEstrangeira É uma Conta -- herda titular, numero, saldo, sacar(),
// transferir(), toString() e o extrato inteiro, sem repetir uma linha.
//
// >>> CLASSE: o que ela acrescenta é a moeda. O saldo herdado guarda DÓLARES; quem
// deposita entrega REAIS, e a conversão é responsabilidade desta classe.
public class ContaEstrangeira extends Conta {

    // ---------------------------------------------------------------- constantes

    // Cotação fixa, só para o exemplo. Num sistema real viria de uma consulta diária.
    // "static final" = constante: uma só para a classe e que não muda.
    private static final double COTACAO_DOLAR = 5.40;

    // ---------------------------------------------------------------- construtor

    // >>> OBJETO + HERANÇA: construtor não se herda; cada classe declara o seu.
    public ContaEstrangeira(String titular, String numero) {

        // >>> HERANÇA: "mãe, monta a SUA parte do objeto com estes dois dados".
        super(titular, numero);

    // Fim do construtor.
    }

    // ---------------------------------------------------------------- métodos de negócio

    // >>> POLIMORFISMO: o pulo do gato do exercício.
    //
    // Sobrescrevemos a versão de DOIS parâmetros, e não o depositar(double) público.
    // Por que? Porque o depositar(double) da mãe repassa para esta versão -- então
    // sobrescrever aqui pega TODOS os caminhos de uma vez:
    //
    //     Main chama depositar(500)          -> cai aqui, converte
    //     transferir() chama depositar(500)  -> cai aqui, converte
    //
    // Se tivéssemos sobrescrito o de 1 parâmetro, a transferência escaparia da conversão.
    @Override
    protected void depositar(double valorEmReais, String descricao) {

        // A conversão: reais que chegaram, divididos pela cotação, viram dólares.
        double emDolares = valorEmReais / COTACAO_DOLAR;

        // super.depositar() = a versão da mãe, que valida, soma no saldo e lança no extrato.
        // Ela recebe o valor JÁ convertido -- a mãe nem sabe que existe moeda estrangeira.
        super.depositar(emDolares, descricao + " (US$)");

    // Fim de depositar(double, String).
    }

    // Método NOVO, que não existe em Conta: converte o saldo de volta para reais.
    // É só uma consulta -- não altera o saldo, que continua guardado em dólares.
    public double getSaldoEmReais() {

        // getSaldo() devolve dólares; multiplicando pela cotação, temos o valor em reais.
        return getSaldo() * COTACAO_DOLAR;

    // Fim de getSaldoEmReais().
    }

    // >>> ABSTRAÇÃO (Exercício 3): implementação obrigatória do método abstrato da mãe.
    @Override
    public String tipoDeConta() {

        // O texto que aparece no cabeçalho do extrato.
        return "Conta em Dólar";

    // Fim de tipoDeConta().
    }

// Fim da classe ContaEstrangeira.
}
