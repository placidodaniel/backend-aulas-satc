// Mesmo pacote da classe testada (contas), espelhando src/main/java/contas/.
package contas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// GABARITO do Exercício 4.
//
// O que diferencia esta conta: o imposto incide sobre o LUCRO, não sobre o saldo.
class ContaInvestimentoTest {

    private static final double DELTA = 0.001;

    // Aplicou e ainda não rendeu: saldo = totalAplicado, lucro zero, imposto zero.
    @Test
    @DisplayName("sem rendimento não há lucro, e portanto não há imposto")
    void semRendimentoNaoHaImposto() {
        ContaInvestimento conta = new ContaInvestimento("Natan", "7777-7");

        conta.depositar(1000);

        assertEquals(1000.0, conta.getSaldo(), DELTA);
        assertEquals(0.0, conta.calcularImposto(), DELTA);
    }

    // O caso central: 10% sobre 1000 = 100 de lucro; 22,5% de 100 = 22,50 de IR.
    @Test
    @DisplayName("o imposto é 22,5% do lucro, não do saldo")
    void impostoIncideSobreOLucro() {
        ContaInvestimento conta = new ContaInvestimento("Natan", "7777-7");
        conta.depositar(1000);

        conta.aplicarRendimento(0.10);

        assertEquals(1100.0, conta.getSaldo(), DELTA);
        assertEquals(22.50, conta.calcularImposto(), DELTA);
    }

    // ESTE é o teste que pega a pegadinha do exercício.
    //
    // Se aplicarRendimento() chamar depositar() em vez de super.depositar(), o rendimento
    // entra em totalAplicado, o lucro fica zero e este teste falha com imposto 0.0.
    // É a diferença entre a solução certa e a que "parece funcionar".
    @Test
    @DisplayName("rendimento não conta como valor aplicado pelo cliente")
    void rendimentoNaoContaComoAplicacao() {
        ContaInvestimento conta = new ContaInvestimento("Natan", "7777-7");
        conta.depositar(1000);

        // Dois rendimentos seguidos: 1000 -> 1100 -> 1210. Lucro total de 210.
        conta.aplicarRendimento(0.10);
        conta.aplicarRendimento(0.10);

        assertEquals(1210.0, conta.getSaldo(), DELTA);
        assertEquals(210.0 * 0.225, conta.calcularImposto(), DELTA);
    }

    // Depósito novo depois do rendimento: entra como aplicação e REDUZ o lucro tributável.
    @Test
    @DisplayName("novo depósito aumenta o valor aplicado e diminui o lucro tributável")
    void novoDepositoNaoViraLucro() {
        ContaInvestimento conta = new ContaInvestimento("Natan", "7777-7");
        conta.depositar(1000);
        conta.aplicarRendimento(0.10);

        // Aplica mais 500: saldo 1600, aplicado 1500, lucro segue sendo 100.
        conta.depositar(500);

        assertEquals(1600.0, conta.getSaldo(), DELTA);
        assertEquals(22.50, conta.calcularImposto(), DELTA);
    }

    // Saque derruba o saldo abaixo do aplicado: prejuízo não gera imposto negativo.
    @Test
    @DisplayName("prejuízo não gera imposto negativo")
    void prejuizoNaoGeraImpostoNegativo() {
        ContaInvestimento conta = new ContaInvestimento("Natan", "7777-7");
        conta.depositar(1000);

        conta.sacar(400);

        assertEquals(600.0, conta.getSaldo(), DELTA);
        assertEquals(0.0, conta.calcularImposto(), DELTA);
    }
}
