// Este arquivo pertence ao pacote "contas" e mora em src/main/java/contas/.
// Nome completo desta classe: contas.Conta
//
// Declara o pacote; a pasta contas/ precisa existir e ter exatamente este nome.
package contas;

// ArrayList vem de java.util: outro pacote, mesma regra de import das classes do projeto.
// É uma lista que cresce sozinha, usada aqui para guardar os lançamentos do extrato.
import java.util.ArrayList;
import java.util.Locale;

import tributaveis.Tributavel;

// >>> CLASSE: a forma, a planta baixa. Descreve o que toda conta TEM (titular, saldo)
// e o que toda conta FAZ (depositar, sacar). Sozinha ela não é uma conta de ninguém --
// vira conta de verdade quando o Main faz "new" e nasce um OBJETO.
//
// >>> INTERFACE: Conta implementa Tributavel, ou seja, é obrigada a fornecer calcularImposto().
// "implements" = assino o contrato e me comprometo a cumpri-lo.
//
// A ordem dos membros daqui para baixo segue a convenção do Java:
// constantes -> atributos -> construtores -> métodos de negócio -> métodos privados ->
// getters/setters -> toString(). Não é obrigatório para compilar, mas todo projeto real segue.
// >>> ABSTRAÇÃO (Exercício 1): "abstract" significa "este tipo existe, mas ninguém pode
// instanciar". Conta é um CONCEITO: no banco você abre uma corrente ou uma poupança,
// nunca uma "conta genérica". A partir daqui, "new Conta(...)" não compila mais --
// mas Conta continua servindo como TIPO de variável, de array e de parâmetro.
public abstract class Conta implements Tributavel {

    // ---------------------------------------------------------------- constantes

    // Usamos double deixando o exemplo mais simples. Sistema financeiro de verdade usa BigDecimal,
    // porque double tem erro de arredondamento (0.1 + 0.2 != 0.3 em double).
    //
    // "static final" = constante: uma só para a classe inteira (static) e que não muda (final).
    // Aqui, 0,5% sobre o saldo -- regra fictícia da aula.
    private static final double ALIQUOTA_IOF = 0.005;

    // Locale do Brasil: faz o valor sair como 1.234,56 em vez de 1,234.56.
    private static final Locale BR = Locale.of("pt", "BR");

    // Largura do extrato em caracteres. Estando numa constante, muda tudo de uma vez só.
    private static final int LARGURA = 45;

    // ---------------------------------------------------------------- atributos

    // >>> ENCAPSULAMENTO: todo atributo daqui para baixo é private. Ninguém de fora lê nem
    // escreve direto; o acesso passa obrigatoriamente pelos métodos, que é onde ficam as regras.
    //
    // Nome do dono da conta. private = só o código desta classe enxerga o atributo.
    private String titular;

    // Número da conta (ex: "1234-5"). Depois de aberta, a conta não troca de número:
    // por isso existe getNumero(), mas NÃO existe setNumero().
    private final String numero;

        // >>> ENCAPSULAMENTO (o caso mais importante do exemplo):
    // saldo é private e NÃO tem setSaldo().
    // Só depositar() e sacar() mexem nele, e os dois validam antes. Se saldo fosse public,
    // qualquer parte do sistema poderia fazer "conta.saldo = -5000" e quebrar a regra do banco.
    // Sem valor inicial, um double começa em 0.0.
    private double saldo;

    // Uma Conta TEM lançamentos -> isso é COMPOSIÇÃO, e é diferente de herança:
    // ContaCorrente É uma Conta (herança), Conta TEM lançamentos (composição).
    //
    // O "final" aqui congela a REFERÊNCIA, não o conteúdo: a lista não pode ser trocada
    // por outra, mas continua aceitando add() normalmente.
    private final ArrayList<String> lancamentos = new ArrayList<>();

    // ---------------------------------------------------------------- construtor

    // >>> OBJETO: é aqui que a CLASSE vira objeto. Cada "new Conta(...)" produz um exemplar
    // novo, com memória própria: a conta do Natan e a do Wesley usam este mesmo código,
    // mas cada uma tem o seu titular e o seu saldo.
    //
    // CONSTRUTOR: método especial, com o mesmo nome da classe e SEM tipo de retorno.
    // Roda uma única vez, no "new", e existe para o objeto já nascer utilizável.
    //
    // Antes o Main fazia "new Conta()" e três setters depois: entre o new e o último setter
    // existia uma conta sem dono e sem número. Com construtor, ou nasce completa ou não nasce.
    //
    // Repare que declarar este construtor FAZ SUMIR o construtor vazio que o Java dava de graça:
    // a partir daqui, "new Conta()" não compila mais.
    public Conta(String titular, String numero) {

        // this.titular é o atributo; titular (sem this) é o parâmetro que chegou.
        this.titular = titular;

        // numero é final: esta é a única atribuição possível na vida do objeto.
        this.numero = numero;
    // Fim do construtor.
    }

    // ---------------------------------------------------------------- métodos de negócio

    // Versão simples de depositar: é a única que o Main enxerga.
    public void depositar(double valor) {

        // Repassa para a versão de 2 parâmetros, usando a descrição padrão.
        depositar(valor, "Depósito");

    // Fim de depositar(double).
    }

    // SOBRECARGA (overload): mesmo nome, lista de parâmetros diferente. Não confundir com
    // sobrescrita (override), que é a subclasse trocando a implementação de um método herdado.
    //
    // >>> HERANÇA + ENCAPSULAMENTO juntos no mesmo modificador:
    // protected é visível para as SUBCLASSES (ContaPoupanca usa para lançar "Rendimento"),
    // mas invisível para o Main, que continua enxergando só o depositar(double).
    // Ou seja: existe um nível de acesso que é só para a família.
    // void = não devolve nada.
    protected void depositar(double valor, String descricao) {

        // Validação: não existe depósito de valor zero ou negativo.
        if (valor <= 0) return;

        // Só aqui o saldo cresce; nenhum código de fora da classe alcança esta linha.
        this.saldo += valor;

        // Valor positivo: entrada de dinheiro no extrato.
        registrar(descricao, valor);

    // Fim de depositar(double, String).
    }

    // >>> POLIMORFISMO: sacar() é o método que as SUBCLASSES sobrescrevem para mudar a regra.
    // É ESTE que ContaCorrente troca por uma versão com taxa. Quem chama escreve sempre
    // "conta.sacar(100)" e nem precisa saber qual das duas versões vai rodar.
    //
    // >>> EXCEÇÃO (Exercício 5): "conta sem saldo" é uma regra DESTE código, não um erro que
    // a JVM detecta sozinha (como dividir por zero) -- por isso quem recusa o saque é um
    // throw explícito. void: quem chama só precisa saber SE o saque aconteceu; quando não
    // acontece, a exceção já carrega essa notícia (e o motivo, em getMessage()), então não
    // sobra razão para também devolver um boolean.
    public void sacar(double valor) {

        // Repassa para a versão de 2 parâmetros, usando a descrição padrão.
        sacar(valor, "Saque");

    // Fim de sacar(double).
    }

    // Sobrecarga protected, igual à de depositar(): quem chama informa a descrição da linha.
    protected void sacar(double valor, String descricao) {

        // >>> EXCEÇÃO: duas regras, duas mensagens. Quem capturar sabe exatamente qual foi
        // quebrada (lendo e.getMessage()), em vez de só saber que "deu false".
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor de saque precisa ser positivo");
        }

        // Invariante do banco: o saldo nunca fica negativo. O throw interrompe o método
        // aqui -- as duas linhas de baixo (saldo -= valor, registrar) nunca chegam a rodar.
        if (valor > this.saldo) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        // Só aqui o saldo diminui.
        this.saldo -= valor;

        // Valor negativo: saída de dinheiro no extrato.
        registrar(descricao, -valor);

    // Fim de sacar(double, String).
    }

    // >>> HERANÇA + POLIMORFISMO (Exercício 2): transferência entre DUAS contas quaisquer.
    //
    // Repare no que este método NÃO sabe: ele não sabe que a corrente cobra taxa, não sabe
    // que a conta em dólar converte moeda, e não tem um único "if" perguntando o tipo.
    // Mesmo assim as duas regras são aplicadas, porque as duas chamadas abaixo são
    // POLIMÓRFICAS: quem decide o código que roda é o objeto, não este método.
    //
    // O parâmetro é do tipo Conta (a mãe), então serve qualquer subclasse: corrente para
    // poupança, corrente para dólar, investimento para corrente...
    //
    // Por que este método é public, e não protected como as sobrecargas acima? O critério
    // do modificador é sempre "quem chama": quem transfere é o CLIENTE (o Main, no pacote
    // app), e protected só alcança o mesmo pacote e as subclasses. Troque por protected e
    // o Main para de compilar -- vale testar, o compilador é a melhor resposta aqui.
    // As sobrecargas de depositar()/sacar() são protected pelo motivo inverso: só a família
    // as chama. Não é o método que escolhe o modificador, é o público que precisa alcançá-lo.
    // >>> EXCEÇÃO se propagando (Exercício 5): repare que este método não tem try/catch
    // nenhum. Se sacar() lançar, transferir() não captura -- só deixa o throw subir, e a
    // linha "destino.depositar(valor)" nem chega a rodar. Quem decide o que fazer com o
    // erro é quem chamou transferir() (o Main), lá no topo da pilha de chamadas.
    public void transferir(Conta destino, double valor) {

        // Regra de saque DA ORIGEM: se for ContaCorrente, a taxa entra aqui sozinha.
        // Se sacar() lançar (saldo insuficiente), a linha de baixo nunca executa.
        sacar(valor);

        // Regra de depósito DO DESTINO: se for ContaEstrangeira, a conversão acontece aqui.
        destino.depositar(valor);

    // Fim de transferir().
    }

    // >>> ABSTRAÇÃO (Exercício 3): MÉTODO abstrato -- assinatura sem corpo, terminada em ";".
    // Não confundir com a CLASSE abstrata lá em cima: são dois usos diferentes da palavra.
    //
    // Aqui a Conta declara "toda conta sabe dizer o próprio tipo", mas se recusa a
    // responder por todas. Quem responde é cada subclasse -- e o compilador OBRIGA:
    // subclasse concreta que não implementar este método não compila.
    //
    // Antes o toString() usava getClass().getSimpleName(), que devolvia o nome da classe
    // ("ContaPoupanca", sem cedilha e sem espaço). Agora quem escolhe o texto que o cliente
    // lê é a própria classe, e não o nome que o programador deu ao arquivo.
    public abstract String tipoDeConta();

    // >>> INTERFACE: este é o método exigido pelo contrato Tributavel. Sem ele, a classe
    // nem compila. @Override avisa o compilador que estou cumprindo o contrato, e com isso
    // um erro de digitação no nome do método vira erro em vez de método novo e inútil.
    @Override
    // public porque a interface exige; a assinatura é idêntica à do contrato.
    public double calcularImposto() {

        // Regra DESTA classe: o imposto incide sobre o saldo parado.
        return saldo * ALIQUOTA_IOF;

    // Fim de calcularImposto().
    }

    // ---------------------------------------------------------------- métodos privados

    // private: uso interno da classe, ninguém de fora chama este método.
    private void registrar(String descricao, double valor) {

        // add() põe a linha já formatada no fim da lista de lançamentos.
        this.lancamentos.add(linha(descricao, valor));

    // Fim de registrar().
    }

    // Formatador único de todas as linhas do extrato: um lugar só para mexer no layout.
    private String linha(String descricao, double valor) {

        // %-28.28s -> descrição à esquerda em 28 colunas; o ".28" corta o que passar disso,
        //             senão uma descrição longa empurraria o valor e quebraria o alinhamento.
        // %,16.2f  -> valor à direita em 16 colunas, com separador de milhar e 2 casas decimais.
        // O BR na frente é o que faz sair 1.000,00 em vez de 1,000.00.
        return String.format(BR, "%-28.28s %,16.2f", descricao, valor);

    // Fim de linha().
    }

    // ---------------------------------------------------------------- getters e setters

    // >>> ENCAPSULAMENTO: getters e setters são a "portaria" da classe. Compare os três
    // atributos e repare que cada um tem um nível de abertura diferente, de propósito:
    //   saldo   -> só getter, porque quem muda são depositar()/sacar(), que validam antes
    //   numero  -> só getter, porque a conta nunca troca de número depois de aberta
    //   titular -> getter e setter, porque o titular pode mudar de verdade
    //
    // Repare também no "public" de cada método: como Main está no pacote app (fora de
    // contas), só o que é public fica visível para ele.
    //
    // Getter SEM setter correspondente.
    public double getSaldo() {

        // Dá para LER o saldo de fora, mas não para escrever direto nele.
        return saldo;

    // Fim de getSaldo().
    }

    // Getter comum.
    public String getTitular() {

        // Devolve o titular atual.
        return titular;

    // Fim de getTitular().
    }

    // Único setter que sobrou: o titular pode mudar depois da conta aberta.
    // Saldo e número não têm setter, cada um por um motivo diferente -- vale comparar os três.
    public void setTitular(String titular) {

        // Validação simples: ignora titular vazio.
        if (titular.isEmpty()) return;

        // this.titular é o atributo; titular (sem this) é o parâmetro que chegou.
        this.titular = titular;

    // Fim de setTitular().
    }

    // Getter sem setter: o número é definido no construtor e nunca mais muda.
    public String getNumero() {

        // Devolve o número da conta.
        return numero;

    // Fim de getNumero().
    }

    // ---------------------------------------------------------------- toString

    // toString() já existe em Object; aqui estamos SOBRESCREVENDO a versão herdada.
    @Override
    // Chamado automaticamente por System.out.println(conta).
    public String toString() {

        // repeat() repete o caractere N vezes; \n quebra a linha.
        String extrato = "=".repeat(LARGURA) + "\n";

        // O operador += vai concatenando texto no que já estava acumulado.
        extrato += "            BANCO SATC - EXTRATO\n";

        // Segunda linha dupla: fecha o cabeçalho.
        extrato += "=".repeat(LARGURA) + "\n";

        // this.titular = o atributo DESTE objeto (cada conta tem o seu).
        extrato += "Titular : " + this.titular + "\n";

        // >>> POLIMORFISMO visível na tela: este código está escrito dentro de Conta, mas
        // tipoDeConta() é abstrato aqui -- quem responde é a subclasse do objeto, decidido
        // em tempo de execução. Sai "Conta Corrente", "Poupança", "Conta em Dólar" ou
        // "Investimento", conforme quem chamou.
        extrato += "Conta   : " + this.numero + "  (" + tipoDeConta() + ")\n";

        // Linha simples separa o cabeçalho dos lançamentos.
        extrato += "-".repeat(LARGURA) + "\n";

        // Cabeçalho das colunas, alinhado com o mesmo esquema usado em linha().
        extrato += String.format("%-28s %16s", "DESCRIÇÃO", "VALOR (R$)") + "\n";

        // for-each: percorre a lista do começo ao fim sem precisar de índice.
        for (String lancamento : this.lancamentos) {

            // Cada movimentação já foi formatada quando entrou na lista; aqui é só empilhar.
            extrato += lancamento + "\n";

        // Fim do for-each.
        }

        // Separa os lançamentos dos totais.
        extrato += "-".repeat(LARGURA) + "\n";

        // Reaproveita o mesmo formatador usado nas movimentações.
        extrato += linha("SALDO", this.saldo) + "\n";

        // Chama o método do contrato: o imposto aparece no rodapé do extrato.
        extrato += linha("Imposto (IOF 0,5%)", calcularImposto()) + "\n";

        // Rodapé. Sem \n no fim para não sobrar uma linha em branco depois.
        extrato += "=".repeat(LARGURA);

        // Devolve o texto pronto; quem chamou decide o que fazer com ele.
        return extrato;

    // Fim de toString().
    }

// Fim da classe Conta.
}
