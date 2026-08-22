// Testes ficam em src/test/java/, espelhando os MESMOS pacotes de src/main/java/.
// Este arquivo está no pacote contas, igual à classe que ele testa -- por isso não precisa
// de "import contas.Conta;". É a mesma regra de pacote que vale no código principal.
package contas;

// @Test marca cada método que o JUnit deve executar.
import org.junit.jupiter.api.Test;

// @DisplayName dá um nome legível ao teste no relatório.
import org.junit.jupiter.api.DisplayName;

// "import static" traz os métodos (assertEquals, assertTrue...) direto, sem escrever Assertions.
import static org.junit.jupiter.api.Assertions.*;

// Testa as regras que moram em Conta: depósito, saque, imposto e extrato.
//
// Repare que instanciamos ContaPoupanca, e não Conta. Dois motivos:
// 1) ContaPoupanca não sobrescreve depositar() nem sacar(), então exercita o código da mãe;
// 2) no Exercício 1 a Conta vira abstract -- se testássemos com "new Conta(...)",
//    o exercício quebraria a compilação dos testes.
class ContaTest {

    // Tolerância para comparar double. Comparar double com == é furada por causa do
    // erro de arredondamento; o terceiro parâmetro do assertEquals é a margem aceita.
    private static final double DELTA = 0.001;

    @Test
    @DisplayName("a conta nasce pronta pelo construtor, com saldo zero")
    void contaNasceComDadosDoConstrutor() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");

        assertEquals("Ana", conta.getTitular());
        assertEquals("1111-1", conta.getNumero());
        assertEquals(0.0, conta.getSaldo(), DELTA);
    }

    @Test
    @DisplayName("depositar aumenta o saldo")
    void depositarAumentaSaldo() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");

        conta.depositar(1000);

        assertEquals(1000.0, conta.getSaldo(), DELTA);
    }

    // Validação de entrada: depósito de valor zero ou negativo é ignorado.
    @Test
    @DisplayName("depositar valor zero ou negativo não mexe no saldo")
    void depositarValorInvalidoNaoMudaSaldo() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(100);

        conta.depositar(0);
        conta.depositar(-50);

        assertEquals(100.0, conta.getSaldo(), DELTA);
    }

    @Test
    @DisplayName("saque válido diminui o saldo")
    void saqueValidoDiminuiSaldo() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(1000);

        conta.sacar(300);

        assertEquals(700.0, conta.getSaldo(), DELTA);
    }

    // >>> EXCEÇÃO (Exercício 5): antes este teste chamava sacar() e conferia um boolean.
    // Agora "recusado" significa "lançou exceção" -- e é isso que assertThrows confere:
    // que o throw ACONTECEU, com a classe e a mensagem certas. Este é O teste do
    // encapsulamento: a regra "saldo nunca fica negativo" é da classe, e ninguém de fora
    // consegue burlar porque não existe setSaldo().
    @Test
    @DisplayName("saque maior que o saldo lança exceção e o saldo fica intacto")
    void saqueMaiorQueSaldoLancaExcecao() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(100);

        // assertThrows roda o saque DENTRO do try/catch escondido nele: se sacar() não
        // lançar IllegalArgumentException, o teste falha aqui mesmo.
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> conta.sacar(999999)
        );

        // getMessage() devolve o texto que o throw carregou -- dá pra conferir qual regra quebrou.
        assertEquals("Saldo insuficiente", erro.getMessage());
        assertEquals(100.0, conta.getSaldo(), DELTA);
    }

    // Caso de fronteira: sacar exatamente todo o saldo é permitido (a regra é "maior que", não "maior ou igual").
    @Test
    @DisplayName("sacar exatamente todo o saldo é permitido e zera a conta")
    void saqueDoSaldoInteiroEhPermitido() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(100);

        conta.sacar(100);

        assertEquals(0.0, conta.getSaldo(), DELTA);
    }

    @Test
    @DisplayName("saque de valor zero ou negativo lança exceção")
    void saqueDeValorInvalidoLancaExcecao() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(100);

        assertThrows(IllegalArgumentException.class, () -> conta.sacar(0));
        assertThrows(IllegalArgumentException.class, () -> conta.sacar(-10));
        assertEquals(100.0, conta.getSaldo(), DELTA);
    }

    // Comportamento documentado no exemplo: operação recusada nunca aconteceu,
    // então não pode aparecer no extrato.
    @Test
    @DisplayName("saque recusado não vira linha no extrato")
    void saqueRecusadoNaoEntraNoExtrato() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(100);

        assertThrows(IllegalArgumentException.class, () -> conta.sacar(999999));

        assertFalse(conta.toString().contains("Saque"));
    }

    @Test
    @DisplayName("o imposto da conta é 0,5% do saldo")
    void impostoEhMeioPorCentoDoSaldo() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(1000);

        assertEquals(5.0, conta.calcularImposto(), DELTA);
    }

    // O extrato é gerado pelo toString(): quem imprime não precisa saber formatar nada.
    @Test
    @DisplayName("o extrato traz titular, número, lançamentos e saldo")
    void extratoTrazOsDadosDaConta() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");
        conta.depositar(1000);
        conta.sacar(100);

        String extrato = conta.toString();

        assertTrue(extrato.contains("Titular : Ana"));
        assertTrue(extrato.contains("1111-1"));
        assertTrue(extrato.contains("Depósito"));
        assertTrue(extrato.contains("1.000,00"));
        assertTrue(extrato.contains("SALDO"));
        assertTrue(extrato.contains("900,00"));
    }

    // ATUALIZADO NO EXERCÍCIO 3.
    //
    // Antes este teste procurava "(ContaPoupanca)", porque o toString() usava
    // getClass().getSimpleName(). Depois que Conta ganhou o método abstrato
    // tipoDeConta(), o extrato passou a mostrar "(Poupança)" e o teste falhou:
    //
    //     [ERROR] ContaTest.extratoMostraClasseReal:157 expected: <true> but was: <false>
    //
    // O teste não estava errado: ele estava DEFENDENDO o comportamento antigo e avisou
    // no primeiro "mvn test" depois da mudança. Corrigir a expectativa é a resposta certa;
    // apagar o teste seria jogar fora o alarme por ele ter tocado.
    //
    // tipoDeConta() é resolvido em tempo de execução: mesmo o código do toString()
    // estando dentro de Conta, quem responde é a subclasse real do objeto.
    @Test
    @DisplayName("o extrato mostra o tipo definido pela subclasse, não a classe onde o toString() foi escrito")
    void extratoMostraClasseReal() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");

        assertTrue(conta.toString().contains("(Poupança)"));
    }

    @Test
    @DisplayName("setTitular ignora nome vazio")
    void setTitularIgnoraNomeVazio() {
        Conta conta = new ContaPoupanca("Ana", "1111-1");

        conta.setTitular("");

        assertEquals("Ana", conta.getTitular());
    }
}
