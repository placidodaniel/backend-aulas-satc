// Mesmo pacote da classe testada (contas), espelhando src/main/java/contas/.
package contas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// GABARITO do desafio do Exercício 2.
//
// Testa as duas coisas que a ContaEstrangeira acrescenta (conversão de moeda e consulta
// em reais) e a transferência entre contas de tipos diferentes.
class ContaEstrangeiraTest {

    private static final double DELTA = 0.001;

    // A conversão do depósito: reais entram, dólares ficam guardados.
    // R$ 540,00 dividido por 5,40 = US$ 100,00 exatos, sem sujeira de arredondamento.
    @Test
    @DisplayName("depósito em reais é convertido para dólares pela cotação")
    void depositoEmReaisViraDolares() {
        ContaEstrangeira conta = new ContaEstrangeira("Ana", "9999-9");

        conta.depositar(540);

        assertEquals(100.0, conta.getSaldo(), DELTA);
    }

    // getSaldoEmReais() é só o caminho de volta: dólares vezes a cotação.
    @Test
    @DisplayName("getSaldoEmReais converte o saldo de volta para reais")
    void saldoConvertidoDeVolta() {
        ContaEstrangeira conta = new ContaEstrangeira("Ana", "9999-9");

        conta.depositar(540);

        assertEquals(540.0, conta.getSaldoEmReais(), DELTA);
    }

    // O extrato tem que registrar a movimentação, com a marca da moeda na descrição.
    @Test
    @DisplayName("o extrato marca o lançamento como US$")
    void extratoMarcaAMoeda() {
        ContaEstrangeira conta = new ContaEstrangeira("Ana", "9999-9");

        conta.depositar(540);

        assertTrue(conta.toString().contains("(US$)"));
        assertTrue(conta.toString().contains("(Conta em Dólar)"));
    }

    // O TESTE PRINCIPAL do exercício: uma transferência, duas regras polimórficas.
    // Sai da corrente o valor MAIS a taxa de R$ 0,50; entra na estrangeira o valor
    // convertido. Nem transferir() nem este teste precisam saber disso de antemão --
    // basta conferir os dois saldos.
    @Test
    @DisplayName("transferência aplica a taxa da origem e a conversão do destino")
    void transferenciaAplicaAsDuasRegras() {
        ContaCorrente origem = new ContaCorrente("Natan", "1234-5");
        origem.depositar(1000);
        ContaEstrangeira destino = new ContaEstrangeira("Natan", "9999-9");

        boolean autorizada = origem.transferir(destino, 500);

        assertTrue(autorizada);
        // 1000,00 menos 500,00 menos 0,50 de taxa.
        assertEquals(499.50, origem.getSaldo(), DELTA);
        // 500 / 5,40 = 92,5925...
        assertEquals(92.592, destino.getSaldo(), 0.001);
    }

    // Transferência recusada é operação que não aconteceu: NENHUM dos dois saldos muda.
    // É o encapsulamento defendendo a regra dos dois lados de uma vez.
    @Test
    @DisplayName("transferência sem saldo é recusada e não mexe em nenhuma das duas contas")
    void transferenciaSemSaldoNaoMexeEmNada() {
        ContaCorrente origem = new ContaCorrente("Natan", "1234-5");
        origem.depositar(100);
        ContaEstrangeira destino = new ContaEstrangeira("Natan", "9999-9");

        boolean autorizada = origem.transferir(destino, 99999);

        assertFalse(autorizada);
        assertEquals(100.0, origem.getSaldo(), DELTA);
        assertEquals(0.0, destino.getSaldo(), DELTA);
    }

    // Caso de fronteira que só existe por causa da taxa: o saldo cobre o valor pedido,
    // mas não cobre valor + taxa. A validação continua sendo a da classe Conta.
    @Test
    @DisplayName("transferir exatamente todo o saldo é recusado por causa da taxa")
    void transferirTodoOSaldoEhRecusadoPelaTaxa() {
        ContaCorrente origem = new ContaCorrente("Natan", "1234-5");
        origem.depositar(100);
        ContaEstrangeira destino = new ContaEstrangeira("Natan", "9999-9");

        assertFalse(origem.transferir(destino, 100));
        assertEquals(100.0, origem.getSaldo(), DELTA);
    }
}
