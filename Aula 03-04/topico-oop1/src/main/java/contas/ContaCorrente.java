// Mesmo pacote de Conta (contas), por isso NÃO precisa de "import contas.Conta;".
//
// Declara o pacote; a pasta contas/ precisa existir e ter exatamente este nome.
package contas;

// >>> HERANÇA: "extends" é a palavra que a declara. ContaCorrente É uma Conta, e por isso
// já nasce com titular, numero, saldo, depositar(), getSaldo(), toString() -- tudo herdado,
// sem uma linha de código repetida aqui.
//
// >>> CLASSE: mesmo herdando quase tudo, ContaCorrente é uma classe própria, com constante
// própria (TAXA_SAQUE) e regra própria de saque.
//
// Diferente de uma subclasse vazia, ela MUDA uma regra: todo saque cobra uma taxa.
public class ContaCorrente extends Conta {

    // Constante só desta subclasse: R$ 0,50 cobrados por saque.
    private static final double TAXA_SAQUE = 0.50;

    // >>> OBJETO + HERANÇA: construtor NÃO se herda. Se ContaCorrente não declarasse o dela,
    // não existiria "new ContaCorrente(titular, numero)" -- é a única exceção ao "herda tudo".
    public ContaCorrente(String titular, String numero) {

        // >>> HERANÇA: "super" quer dizer "a classe mãe". Então super(titular, numero) é,
        // em português: "mãe, monta a SUA parte do objeto com estes dois dados".
        //
        // Pense no objeto como um prédio de dois andares:
        //
        //     ANDAR DE CIMA  (ContaCorrente) -> TAXA_SAQUE, o sacar() com taxa
        //     ANDAR DE BAIXO (Conta)         -> titular, numero, saldo, lançamentos
        //
        // Existe UM objeto só, mas ele é montado em duas camadas. Esta linha manda construir
        // o andar de baixo. Só quando ele estiver pronto é que a ContaCorrente continua.
        //
        // "Mas por que a filha não grava titular e numero ela mesma?"
        // Porque os dois são PRIVATE lá na Conta, e private não abre nem para a família.
        // A única parte do programa que consegue preenchê-los é o construtor da Conta --
        // e é exatamente ele que estamos chamando aqui.
        //
        // O que acontece, na ordem, quando o Main executa
        // new ContaCorrente("Natan", "1234-5"):
        //   1. entra neste construtor, com titular="Natan" e numero="1234-5"
        //   2. super(...) repassa os dois para o construtor da Conta
        //   3. a Conta grava this.titular e this.numero (o andar de baixo fica pronto)
        //   4. a execução volta para cá; se houvesse mais linhas abaixo, rodariam agora
        //   5. o objeto está completo e o Main recebe a conta pronta
        //
        // Por que tem que ser a PRIMEIRA linha do construtor?
        // Porque não dá para mobiliar o andar de cima antes de existir o de baixo.
        // O Java obriga: qualquer código antes do super(...) é erro de compilação.
        //
        // E se você apagar esta linha? O Java tenta chamar um super() vazio sozinho,
        // não encontra um "Conta()" sem parâmetros, e o erro é este:
        //
        //     error: constructor Conta in class Conta cannot be applied to given types;
        //       required: String,String
        //       found:    no arguments
        //
        // Vale testar isso ao vivo: apague, compile, mostre o erro e devolva a linha.

        super(titular, numero);

    // Fim do construtor.
    }

    // >>> POLIMORFISMO: aqui está o coração dele. Este método tem a MESMA assinatura do
    // sacar() da mãe, mas comportamento diferente. Quando o Main percorre um array de Conta
    // e chama conta.sacar(50), quem decide qual versão roda é o OBJETO, não o tipo da
    // variável -- e essa decisão só acontece em tempo de execução.
    //
    // @Override avisa o compilador: estou trocando a implementação de um método herdado.
    // Se o nome ou os parâmetros não baterem com os da mãe, vira erro em vez de método novo.
    @Override
    // Mesma assinatura de Conta.sacar(double), corpo diferente.
    public void sacar(double valor) {

        // super.sacar() chama a versão da classe mãe (Conta), que faz a validação de saldo
        // (e lança a exceção quando falta saldo -- ContaCorrente não precisa repetir isso).
        // Aqui só somamos a taxa ao valor pedido -- não reescrevemos a regra do saldo negativo.
        // A descrição vai junto para a taxa aparecer no extrato, em vez de virar valor misterioso.
        // "super" significa exatamente isso: "a versão da mãe".
        super.sacar(valor + TAXA_SAQUE, "Saque (inclui taxa R$ 0,50)");

    // Fim de sacar().
    }

    // >>> ABSTRAÇÃO (Exercício 3): implementação obrigatória do método abstrato da mãe.
    // Sem este método, ContaCorrente não compila -- o compilador cobra.
    @Override
    public String tipoDeConta() {

        // O texto que o cliente lê no extrato. Quem decide é esta classe, não o nome do arquivo.
        return "Conta Corrente";

    // Fim de tipoDeConta().
    }

// Fim da classe ContaCorrente.
}
