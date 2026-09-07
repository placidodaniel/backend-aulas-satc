// Espelha src/main/java/investimentos/: mesmo pacote da classe testada.
package investimentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tributaveis.Tributavel;

// Acao implementa o mesmo contrato das contas, mas com regra e documento próprios.
class AcaoTest {

    private static final double DELTA = 0.001;

    @Test
    @DisplayName("a ação nasce pronta pelo construtor")
    void acaoNasceComDadosDoConstrutor() {
        Acao acao = new Acao("PETR4", 500);

        assertEquals("PETR4", acao.getCodigo());
        assertEquals(500.0, acao.getLucro(), DELTA);
    }

    // Mesma assinatura de Conta.calcularImposto(), alíquota e base de cálculo diferentes.
    @Test
    @DisplayName("o imposto da ação é 15% do lucro")
    void impostoEhQuinzePorCentoDoLucro() {
        Acao acao = new Acao("PETR4", 500);

        assertEquals(75.0, acao.calcularImposto(), DELTA);
    }

    @Test
    @DisplayName("sem lucro não há imposto")
    void semLucroNaoHaImposto() {
        Acao acao = new Acao("PETR4", 0);

        assertEquals(0.0, acao.calcularImposto(), DELTA);
    }

    // O toString() da Acao produz outro documento: nota de corretagem, não extrato.
    @Test
    @DisplayName("a ação se imprime como nota de corretagem, não como extrato")
    void imprimeNotaDeCorretagem() {
        Acao acao = new Acao("PETR4", 500);

        String nota = acao.toString();

        assertTrue(nota.contains("NOTA DE CORRETAGEM"));
        assertTrue(nota.contains("PETR4"));
        assertTrue(nota.contains("Lucro na venda"));
        assertFalse(nota.contains("EXTRATO"));
    }

    // Acao assina o contrato Tributavel sem ter nenhum parentesco com Conta:
    // interface não é herança.
    @Test
    @DisplayName("Acao é Tributavel, mas não é uma Conta")
    void ehTributavelSemSerConta() {
        Acao acao = new Acao("PETR4", 500);

        assertInstanceOf(Tributavel.class, acao);
    }
}
