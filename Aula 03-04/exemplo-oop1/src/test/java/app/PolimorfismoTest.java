// Este teste vive no pacote app, o mesmo do Main -- porque, como o Main, ele é o único
// que precisa conhecer TODOS os pacotes ao mesmo tempo.
package app;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

// Classes de três pacotes diferentes, todas importadas aqui.
import contas.ContaCorrente;
import contas.ContaPoupanca;
import investimentos.Acao;
import tributaveis.Tributavel;

import static org.junit.jupiter.api.Assertions.*;

// Os testes anteriores olham cada classe isolada. Este olha o que só aparece quando
// elas se juntam: a MESMA chamada produzindo cálculos diferentes, atravessando pacotes.
class PolimorfismoTest {

    private static final double DELTA = 0.001;

    // Monta a mesma lista que o Main monta: duas contas e uma ação, sem parentesco entre si,
    // unidas só pelo fato de assinarem tributaveis.Tributavel.
    private ArrayList<Tributavel> listaDoExemplo() {
        ContaCorrente cc = new ContaCorrente("Natan", "1234-5");
        cc.depositar(1000);
        cc.sacar(100);

        ContaPoupanca cp = new ContaPoupanca("Wesley", "6789-0");
        cp.depositar(1000);
        cp.sacar(100);
        cp.aplicarRendimento();

        Acao acao = new Acao("PETR4", 500);

        ArrayList<Tributavel> tributaveis = new ArrayList<>();
        tributaveis.add(cc);
        tributaveis.add(cp);
        tributaveis.add(acao);
        return tributaveis;
    }

    // Classes de pacotes diferentes cabem na mesma lista porque implementam a mesma interface.
    @Test
    @DisplayName("contas e ação convivem na mesma lista de Tributavel")
    void listaAceitaTiposSemParentesco() {
        ArrayList<Tributavel> tributaveis = listaDoExemplo();

        assertEquals(3, tributaveis.size());
    }

    // O coração do polimorfismo: uma linha de código, três implementações diferentes.
    // 0,5% de 899,50 + 0,5% de 904,50 + 15% de 500,00
    @Test
    @DisplayName("a mesma chamada calcularImposto() produz três resultados diferentes")
    void mesmaChamadaTresResultados() {
        ArrayList<Tributavel> tributaveis = listaDoExemplo();

        double total = 0;
        for (Tributavel tributavel : tributaveis) {
            total += tributavel.calcularImposto();
        }

        assertEquals(4.4975, tributaveis.get(0).calcularImposto(), DELTA);
        assertEquals(4.5225, tributaveis.get(1).calcularImposto(), DELTA);
        assertEquals(75.0, tributaveis.get(2).calcularImposto(), DELTA);
        assertEquals(84.02, total, DELTA);
    }

    // A variável é do tipo Tributavel, mas quem responde é sempre a classe real do objeto.
    @Test
    @DisplayName("getSimpleName revela a classe real por trás do tipo Tributavel")
    void tipoDeclaradoNaoEscondeClasseReal() {
        ArrayList<Tributavel> tributaveis = listaDoExemplo();

        assertEquals("ContaCorrente", tributaveis.get(0).getClass().getSimpleName());
        assertEquals("ContaPoupanca", tributaveis.get(1).getClass().getSimpleName());
        assertEquals("Acao", tributaveis.get(2).getClass().getSimpleName());
    }

    // Mesmo println(), documentos diferentes: cada classe tem seu próprio toString().
    @Test
    @DisplayName("cada objeto se imprime com o documento da sua classe")
    void cadaObjetoTemSeuDocumento() {
        ArrayList<Tributavel> tributaveis = listaDoExemplo();

        assertTrue(tributaveis.get(0).toString().contains("EXTRATO"));
        assertTrue(tributaveis.get(1).toString().contains("EXTRATO"));
        assertTrue(tributaveis.get(2).toString().contains("NOTA DE CORRETAGEM"));
    }
}
