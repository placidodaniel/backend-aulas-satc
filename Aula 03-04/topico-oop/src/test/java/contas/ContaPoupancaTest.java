// Mesmo pacote da classe testada (contas), espelhando src/main/java/contas/.
package contas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// Testa só o que ContaPoupanca ACRESCENTA: o método aplicarRendimento(),
// que não existe em Conta nem em ContaCorrente.
class ContaPoupancaTest {

    private static final double DELTA = 0.001;

    @Test
    @DisplayName("aplicarRendimento credita 0,5% do saldo")
    void rendimentoCreditaMeioPorCento() {
        ContaPoupanca conta = new ContaPoupanca("Wesley", "6789-0");
        conta.depositar(1000);

        conta.aplicarRendimento();

        assertEquals(1005.0, conta.getSaldo(), DELTA);
    }

    // O rendimento passa por depositar(), então também vira linha no extrato --
    // e com descrição própria, não como "Depósito".
    @Test
    @DisplayName("o rendimento aparece no extrato com nome próprio")
    void rendimentoApareceNoExtrato() {
        ContaPoupanca conta = new ContaPoupanca("Wesley", "6789-0");
        conta.depositar(1000);

        conta.aplicarRendimento();

        assertTrue(conta.toString().contains("Rendimento (0,5% a.m.)"));
    }

    // Conta zerada: 0,5% de zero é zero, e depositar() ignora valor zero.
    @Test
    @DisplayName("aplicar rendimento em conta zerada não muda nada")
    void rendimentoEmContaZeradaNaoMudaNada() {
        ContaPoupanca conta = new ContaPoupanca("Wesley", "6789-0");

        conta.aplicarRendimento();

        assertEquals(0.0, conta.getSaldo(), DELTA);
    }

    // O contraste com ContaCorrente: a poupança usa o sacar() da mãe, sem taxa.
    @Test
    @DisplayName("o saque da poupança não cobra taxa")
    void saqueNaoCobraTaxa() {
        ContaPoupanca conta = new ContaPoupanca("Wesley", "6789-0");
        conta.depositar(1000);

        conta.sacar(100);

        assertEquals(900.0, conta.getSaldo(), DELTA);
    }
}
