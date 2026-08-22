// Mesmo pacote da classe testada (contas), espelhando src/main/java/contas/.
package contas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// Testa só o que ContaCorrente ACRESCENTA: a taxa no saque.
// O resto do comportamento já está coberto em ContaTest, porque é herdado.
class ContaCorrenteTest {

    private static final double DELTA = 0.001;

    // A prova de que o @Override funcionou: mesma chamada de ContaTest, resultado diferente.
    @Test
    @DisplayName("o saque cobra R$ 0,50 de taxa além do valor pedido")
    void saqueCobraTaxa() {
        ContaCorrente conta = new ContaCorrente("Natan", "1234-5");
        conta.depositar(1000);

        conta.sacar(100);

        assertEquals(899.50, conta.getSaldo(), DELTA);
    }

    // Caso de fronteira que só existe por causa da taxa: o saldo cobre o valor pedido,
    // mas não cobre valor + taxa. A validação de saldo continua sendo a da classe mãe.
    @Test
    @DisplayName("saque é recusado quando o saldo cobre o valor mas não cobre a taxa")
    void saqueRecusadoQuandoSaldoNaoCobreATaxa() {
        ContaCorrente conta = new ContaCorrente("Natan", "1234-5");
        conta.depositar(100);

        boolean autorizado = conta.sacar(100);

        assertFalse(autorizado);
        assertEquals(100.0, conta.getSaldo(), DELTA);
    }

    // A descrição passada para super.sacar() é o que faz a taxa aparecer no extrato.
    @Test
    @DisplayName("o extrato explica a taxa em vez de mostrar um valor misterioso")
    void extratoExplicaATaxa() {
        ContaCorrente conta = new ContaCorrente("Natan", "1234-5");
        conta.depositar(1000);

        conta.sacar(100);

        assertTrue(conta.toString().contains("Saque (inclui taxa R$ 0,50)"));
    }

    // Herança: ContaCorrente É uma Conta, então serve em qualquer lugar que peça Conta.
    @Test
    @DisplayName("ContaCorrente é uma Conta e herda o cálculo de imposto")
    void ehUmaContaEHerdaOImposto() {
        ContaCorrente conta = new ContaCorrente("Natan", "1234-5");
        conta.depositar(1000);

        assertInstanceOf(Conta.class, conta);
        assertEquals(5.0, conta.calcularImposto(), DELTA);
    }
}
