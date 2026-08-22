// ============================================================================
//  COLA RÁPIDA -- CONCEITO DE POO  =  PALAVRA-CHAVE DO JAVA
// ============================================================================
//
//  Classe é o molde; 
//  CLASSE          =  class                  -> contas/Conta.java: a "planta baixa"
//  ------------------------------------------------------
//  Objeto é a instância;
//  OBJETO          =  new                    -> new ContaCorrente("Natan", "1234-5")
//  ------------------------------------------------------
//  Abstração e interfaces ajudam a definir modelos e contratos
//  Abstração       =  abstract               -> ContaCorrente, ContaPoupanca
//  ------------------------------------------------------
//  Encapsulamento: proteger atributos com private e acessá-los por getters e setters quandonecessário.
//  ENCAPSULAMENTO  =  private                -> private double saldo (e nenhum setSaldo)
//  ------------------------------------------------------
//  Herança: uma classe filha herda atributos e métodos da classe mãe usando extends.
//  HERANÇA         =  extends  +  super      -> ContaCorrente extends Conta
//  ------------------------------------------------------
//  INTERFACE       =  interface + implements -> Conta implements Tributavel
//  ------------------------------------------------------
//  Polimorfismo permite tratar objetos de subclasses por uma referência da superclasse.
//  POLIMORFISMO    =  @Override              -> conta.sacar(100): a MESMA linha roda
//                                               código diferente conforme o objeto
//  ------------------------------------------------------
//  Outros termos que aparecem no código:
//  ------------------------------------------------------
//  Atributos representam características; 
//  ATRIBUTO        =  private                -> private String titular
//  ------------------------------------------------------
//  métodos representam comportamentos.
//  MÉTODO          =  public                 -> public void sacar(double valor)
//  ------------------------------------------------------
//  CONSTRUTOR      =  método com o nome da classe e SEM tipo de retorno
//  ------------------------------------------------------
//  SOBRECARGA      =  mesmo nome, parâmetros diferentes  -> depositar(double)
//                                                           depositar(double, String)
//  ------------------------------------------------------
//  Sobrescrita ocorre quando a subclasse reimplementa um método herdado.
//  SOBRESCRITA     =  @Override na subclasse             -> ContaCorrente.sacar()
//  ------------------------------------------------------
//  COMPOSIÇÃO      =  atributo que é outro objeto        -> Conta TEM lançamentos
//  ------------------------------------------------------
//  CONSTANTE       =  static final                       -> TAXA_SAQUE = 0.50
//  ------------------------------------------------------
//  PACOTE          =  package  +  import                 -> package contas;
//  ------------------------------------------------------
//  Cuidado para não confundir:
//    HERANÇA  = parentesco  ("ContaCorrente É uma Conta")     -> extends, só 1 mãe
//    INTERFACE= compromisso ("Acao SABE calcular imposto")    -> implements, quantas quiser
//
//  Para achar cada conceito dentro do código:
//    grep -rn ">>> POLIMORFISMO" src/main/java     (troque pelo conceito que quiser)
//    grep -rn ">>> " src/main/java                 (lista as 36 marcações de uma vez)
//
// ============================================================================

// COMO RODAR (a partir da pasta que contém o pom.xml):
//   Linux/Mac:  ./mvnw test && java -cp target/classes app.Main
//   Windows:    mvnw.cmd test && java -cp target/classes app.Main
//
// O "mvnw" é o Maven Wrapper: um script que baixa e usa o Maven certo sozinho.
// Quem clonar o projeto precisa ter só o JDK instalado -- Maven não precisa estar na máquina.
// Acrescente -o para rodar offline depois do primeiro download: ./mvnw -o test
//
// Main fica no pacote "app" (src/main/java/app/). Repare no mapa do projeto:
//
//   src/main/java/
//     ├── tributaveis/   -> Tributavel                        (o contrato)
//     ├── contas/        -> Conta, ContaCorrente, ContaPoupanca (implementa o contrato)
//     ├── investimentos/ -> Acao                              (implementa o contrato)
//     └── app/           -> Main                              (usa todo mundo)
//
// Dependências: contas -> tributaveis <- investimentos, e app -> todos.
// Nenhum pacote de domínio conhece o outro; os dois só conhecem a interface.
//
// Antes, TODAS as classes ficavam soltas em src/main/java/ sem declarar pacote: caíam no
// "pacote default" (unnamed package), enxergavam umas às outras sem import e nenhum projeto
// real faz isso. Agora cada classe tem um endereço completo: contas.ContaCorrente,
// investimentos.Acao, app.Main.
//
// Declara o pacote; a pasta app/ precisa existir e ter exatamente este nome.
package app;

// Como Main está em app, TUDO que vem de fora precisa de import -- inclusive as classes
// do próprio projeto. É a mesma regra do ArrayList, que vem do pacote java.util.
//
// ArrayList: lista que cresce sozinha; guarda a lista polimórfica lá embaixo.
import java.util.ArrayList;
import java.util.Locale;

import contas.Conta;
import contas.ContaCorrente;
import contas.ContaEstrangeira;
import contas.ContaInvestimento;
import contas.ContaPoupanca;
import investimentos.Acao;
import tributaveis.Tributavel;

// Classe principal, ponto de entrada do programa.
// O nome da classe tem que ser igual ao nome do arquivo (Main.java) -- regra do Java.
public class Main {

    // Locale do Brasil: faz o valor sair como 1.234,56 em vez de 1,234.56.
    // Locale é uma classe da API Java que representa uma região e idioma.
    // private: Significa que a variável BR só pode ser acessada dentro da própria classe. Outras classes não conseguem acessar BR diretamente.
    // static: A variável pertence à classe, e não aos objetos.
    // Final: Significa que a referencia não pode ser alterada.
    private static final Locale BR = Locale.of("pt", "BR");

    // Mesma largura usada nos extratos, para tudo alinhar na tela.
    private static final int LARGURA = 45;

    // main(): método que a JVM chama para iniciar a execução.
    // static = roda sem precisar de "new Main()".
    public static void main() {

        // >>> OBJETO: "ContaCorrente" é a CLASSE (a forma); "ccNatan" é o OBJETO (o exemplar).
        // Uma classe só existe uma vez no projeto; objetos você cria quantos quiser, e cada um
        // tem seus próprios valores. É a diferença entre a receita e o bolo.
        //
        // Os dados vão no CONSTRUTOR: a conta já nasce com dono e número, sem passar por
        // um estado inválido. Compare com a versão antiga, que era "new" mais três setters.
        ContaCorrente ccNatan = new ContaCorrente("Natan", "1234-5");

        // Saldo: 1000.00 -- e já entra a primeira linha do extrato.
        ccNatan.depositar(1000);
        ccNatan.depositar(999999);

        // Cria uma ContaPoupanca (também subclasse de contas.Conta).
        // Outro objeto, com memória separada da conta acima.
        ContaPoupanca cpWesley = new ContaPoupanca("Wesley", "6789-0");

        // Saldo: 1000.00 -- mesmo método herdado, outro objeto.
        cpWesley.depositar(1000);

        // >>> HERANÇA + POLIMORFISMO: nenhuma das duas classes escreveu "sacar" do zero.
        // Uma herdou e a outra sobrescreveu -- e a chamada aqui é idêntica nas duas linhas.
        // Mesmo código, resultados diferentes: R$ 0,50 de taxa numa, nada na outra.
        //
        // Saldo: 899.50 -- cai na versão de ContaCorrente (100 + 0.50 de taxa).
        ccNatan.sacar(100);

        // Saldo: 900.00 -- cai na versão de Conta (sem taxa).
        cpWesley.sacar(100);

        // >>> HERANÇA (o outro caminho): método que só a poupança tem. A subclasse pode
        // ADICIONAR comportamento, não só sobrescrever. Saldo: 904.50 (0,5% sobre 900.00).
        // A ccNatan nem enxerga este método -- herança não é troca simétrica.
        cpWesley.aplicarRendimento();

        // >>> ENCAPSULAMENTO na prática: o saque é recusado e o saldo continua intacto.
        // Como "saldo" é private e não existe setSaldo(), o Main NÃO TEM como forçar.
        // Se o atributo fosse público, bastaria "cpWesley.saldo = 999999" e a regra do banco
        // viraria decoração. A operação recusada nem entra no extrato -- ela nunca aconteceu.
        //
        // sacar() devolve false quando falta saldo.
        boolean deuCerto = cpWesley.sacar(999999);

        // Monta a mensagem. O "? :" é o operador ternário: um if de uma linha só.
        System.out.println(">> Saque de R$ 999.999,00 na conta " + cpWesley.getNumero()
                + (deuCerto ? " AUTORIZADO" : " RECUSADO (saldo insuficiente)"));

        // println() sem argumento imprime só uma linha em branco.
        System.out.println();
        // ------------------------------------------------------------------------------------------------------
        // EXERCÍCIO 1 (RESOLVIDO): Conta virou abstract.
        //
        // Repare no que MUDOU e no que NÃO mudou:
        //   - "new Conta(...)" parou de compilar (linha comentada logo abaixo)
        //   - Conta como TIPO de array e de variável continua funcionando normalmente
        //
        // Array de Conta guardando duas SUBCLASSES diferentes.
        Conta[] listaContas = {ccNatan, cpWesley};

        // DESCOMENTE esta linha para ver o erro do Exercício 1:
        //     error: Conta is abstract; cannot be instantiated
        // Repare que os argumentos estão certos -- o problema não é o construtor, é o "new".
        //Conta contaGenerica = new Conta("Fulano", "0000-0");

        // Polimorfismo puro: a variável é do tipo Conta, mas cada objeto aplica a SUA regra.
        // Linha separadora.
        System.out.println("-".repeat(LARGURA));

        // Título do bloco.
        System.out.println("MOVIMENTAÇÃO: saque de R$ 50,00 em cada conta");

        // Linha separadora.
        System.out.println("-".repeat(LARGURA));

        // for clássico: i vai de 0 até o fim do array.
        for (int i = 0; i < listaContas.length; i++) {

            // Tipo declarado é Conta; o objeto real é ContaCorrente ou ContaPoupanca.
            Conta conta = listaContas[i];

            // ContaCorrente cobra taxa aqui, ContaPoupanca não -- decidido em tempo de execução.
            conta.sacar(50);

            // Linha alinhada com o mesmo formato dos extratos.
            System.out.println(String.format(BR, "%-28.28s %,16.2f", conta.getTitular(), conta.getSaldo()));

        // Fim do for.
        }
        // ------------------------------------------------------------------------------------------------------
        // Linha em branco separando os blocos da saída.
        System.out.println();

        // ======================================================================================
        // EXERCÍCIO 2 (RESOLVIDO): transferência de uma conta corrente para uma conta em dólar.
        // ======================================================================================
        //
        // Contas novas, separadas das de cima, para os números baterem com o EXERCICIOS.md.
        ContaCorrente ccOrigem = new ContaCorrente("Natan", "1234-5");

        // Saldo: 1000.00
        ccOrigem.depositar(1000);

        // Saldo: 899.50 (100 + 0.50 de taxa)
        ccOrigem.sacar(100);

        // >>> OBJETO: a conta em dólar. O saldo dela é guardado em DÓLARES.
        ContaEstrangeira ceNatan = new ContaEstrangeira("Natan", "9999-9");

        // >>> POLIMORFISMO: esta única linha dispara DUAS regras que ela não conhece.
        // Saem R$ 500,50 da corrente (500 + taxa) e entram US$ 92,59 na estrangeira (500 / 5,40).
        // O método transferir() não tem um "if" sequer perguntando o tipo das contas.
        boolean transferiu = ccOrigem.transferir(ceNatan, 500);

        // Cabeçalho do bloco.
        System.out.println("-".repeat(LARGURA));
        System.out.println("TRANSFERÊNCIA: R$ 500,00 da corrente para o dólar");
        System.out.println("-".repeat(LARGURA));

        // Confirmação da operação.
        System.out.println("Autorizada?         " + transferiu);

        // Saldo da origem: 399.00 -- saíram os 500 mais a taxa de 0,50.
        System.out.println(String.format(BR, "%-28.28s %,16.2f", "Saldo da corrente (R$)", ccOrigem.getSaldo()));

        // Saldo do destino: 92.59 DÓLARES.
        System.out.println(String.format(BR, "%-28.28s %,16.2f", "Saldo da estrangeira (US$)", ceNatan.getSaldo()));

        // O mesmo saldo convertido de volta para reais.
        //
        // ATENÇÃO ao contraste das duas linhas abaixo: é o subexercício 2.2 na tela.
        // Formatado com %.2f o valor sai "500,00" -- o arredondamento ESCONDE o problema.
        // Impresso cru, aparece o 499.99999999999994 que o double realmente guardou.
        System.out.println(String.format(BR, "%-28.28s %,16.2f", "Convertido de volta (R$)", ceNatan.getSaldoEmReais()));

        // O mesmo número, sem formatação nenhuma.
        System.out.println("   valor cru, sem %.2f: " + ceNatan.getSaldoEmReais());

        // Transferência sem saldo: recusada, e nenhum dos dois saldos se mexe.
        System.out.println("Transferir R$ 99.999? " + ccOrigem.transferir(ceNatan, 99999));

        // Linha em branco separando os blocos da saída.
        System.out.println();

        // ======================================================================================
        // EXERCÍCIO 4 (RESOLVIDO): conta de investimento, com imposto só sobre o lucro.
        // ======================================================================================
        ContaInvestimento ciNatan = new ContaInvestimento("Natan", "7777-7");

        // Aplica R$ 1.000. Saldo 1000.00, e imposto 0.00 -- ainda não houve lucro.
        ciNatan.depositar(1000);

        // Rende 10%. Saldo 1100.00, lucro 100.00, imposto 22.50 (22,5% sobre o lucro).
        ciNatan.aplicarRendimento(0.10);

        // Linha em branco separando os blocos da saída.
        System.out.println();

        // >>> OBJETO + INTERFACE: mais um objeto, agora de uma classe que também implementa
        // Tributavel mas NÃO é uma Conta. Outro pacote, zero parentesco, construtor de
        // assinatura totalmente diferente -- e mesmo assim vai caber na mesma lista lá embaixo.
        Acao petrobras = new Acao("PETR4", 500);

        // >>> POLIMORFISMO + ENCAPSULAMENTO: println(objeto) chama o toString() de cada classe
        // automaticamente. Os três println() abaixo são idênticos, e saem três documentos
        // diferentes: dois extratos e uma nota de corretagem.
        // O Main não sabe formatar nada disso -- cada objeto sabe se apresentar sozinho.
        //
        // Extrato da conta corrente (com a taxa nas linhas de saque).
        System.out.println(ccNatan);

        // Linha em branco entre os documentos.
        System.out.println();

        // Extrato da poupança (com a linha de rendimento).
        System.out.println(cpWesley);

        // Linha em branco entre os documentos.
        System.out.println();

        // Extrato da conta em dólar (Exercício 2). Repare no "(Conta em Dólar)" do cabeçalho:
        // é o tipoDeConta() do Exercício 3 respondendo.
        System.out.println(ceNatan);

        // Linha em branco entre os documentos.
        System.out.println();

        // Extrato da conta de investimento (Exercício 4), com o IR sobre o lucro no rodapé.
        System.out.println(ciNatan);

        // Linha em branco entre os documentos.
        System.out.println();

        // Nota de corretagem: outro toString(), outro formato de documento.
        System.out.println(petrobras);

        // Linha em branco entre os documentos.
        System.out.println();

        // >>> INTERFACE + POLIMORFISMO (o ponto alto do exemplo): a lista é de Tributavel,
        // não de Conta. Entra qualquer objeto que assine o contrato -- e entram três de
        // dois pacotes diferentes, dois deles parentes entre si e o terceiro sem nenhum
        // parentesco. É a interface, e não a herança, que permite isso.
        //
        // O <Tributavel> entre os sinais de menor/maior diz: só entra quem assina o contrato.
        ArrayList<Tributavel> listaTributaveis = new ArrayList<>();

        // Adiciona a conta corrente à lista.
        listaTributaveis.add(ccNatan);

        // Adiciona a poupança à lista.
        listaTributaveis.add(cpWesley);

        // Adiciona a ação à lista, mesmo sem nenhum parentesco com as contas.
        listaTributaveis.add(petrobras);

        // Desafio do Exercício 4: entram mais duas origens na lista. O laço lá embaixo
        // NÃO precisou de uma única alteração para lidar com elas -- é isso que o
        // polimorfismo compra: código novo entra sem mexer no código que já existe.
        listaTributaveis.add(ceNatan);
        listaTributaveis.add(ciNatan);

        // Percorre a lista chamando calcularImposto() de cada objeto -- cada classe implementa
        // esse método do seu próprio jeito (0,5% do saldo nas contas, 15% do lucro na ação).
        //
        // Linha dupla abre o resumo.
        System.out.println("=".repeat(LARGURA));

        // Título do resumo.
        System.out.println("       BANCO SATC - RESUMO DE IMPOSTOS");

        // Linha dupla fecha o cabeçalho.
        System.out.println("=".repeat(LARGURA));

        // Cabeçalho das colunas.
        System.out.println(String.format("%-28s %16s", "ORIGEM", "IMPOSTO (R$)"));

        // Acumulador: começa zerado e cresce a cada volta do laço.
        double total = 0;

        // for-each: percorre a lista do começo ao fim sem precisar de índice.
        for (Tributavel tributavel : listaTributaveis) {

            // >>> POLIMORFISMO em uma linha: esta chamada é UMA só, escrita uma vez, e
            // dispara três códigos diferentes conforme o objeto da vez. O Main não tem
            // nenhum if perguntando "é conta ou é ação?" -- e é exatamente esse if que
            // o polimorfismo elimina.
            double imposto = tributavel.calcularImposto();

            // Soma no acumulador.
            total += imposto;

            // getSimpleName() mostra qual classe respondeu à chamada.
            String origem = tributavel.getClass().getSimpleName();

            // Linha alinhada com o mesmo formato dos extratos.
            System.out.println(String.format(BR, "%-28.28s %,16.2f", origem, imposto));

        // Fim do for-each.
        }

        // Separa as linhas do total.
        System.out.println("-".repeat(LARGURA));

        // Imprime a linha do TOTAL, alinhada com todas as outras. Tem DUAS coisas
        // acontecendo aqui, e vale separar:
        //
        //   String.format(...)  MONTA um texto e devolve -- não imprime nada.
        //   System.out.println  recebe esse texto pronto e imprime.
        //
        // Poderia ser em duas linhas, e daria no mesmo:
        //     String linha = String.format(BR, "%-28.28s %,16.2f", "TOTAL", total);
        //     System.out.println(linha);
        //
        // ----------------------------------------------------------------------
        // COMO LER O MOLDE "%-28.28s %,16.2f"
        // ----------------------------------------------------------------------
        // O texto entre aspas é um MOLDE com buracos. Cada "%" abre um buraco, e os
        // valores que vêm depois preenchem os buracos NA ORDEM em que aparecem:
        //
        //     "%-28.28s   %,16.2f"
        //        ↑buraco1   ↑buraco2
        //         "TOTAL"    total
        //
        // O que NÃO faz parte de um "%" é copiado literalmente -- aqui, só o espaço
        // que separa os dois buracos.
        //
        // Buraco 1 -> %-28.28s   (o texto da esquerda)
        //     %     começa a instrução
        //     -     alinha à ESQUERDA. Sem o "-", o texto iria para a direita.
        //     28    largura MÍNIMA: se faltar, completa com espaços até 28 colunas
        //     .28   largura MÁXIMA: se sobrar, CORTA no caractere 28
        //     s     o valor é uma String
        //
        // Buraco 2 -> %,16.2f    (o valor da direita)
        //     %     começa a instrução
        //     ,     usa separador de milhar (1.234.567,89 em vez de 1234567.89)
        //     16    largura mínima 16, e sem "-" fica alinhado à DIREITA
        //     .2    duas casas decimais
        //     f     o valor é um número com vírgula (double)
        //
        // CUIDADO com uma pegadinha: o ponto significa coisas DIFERENTES nos dois.
        // Em %s o ".28" CORTA o texto; em %f o ".2" define quantas CASAS DECIMAIS.
        //
        // A conta da largura: 28 + 1 espaço + 16 = 45, que é exatamente LARGURA.
        // É por isso que a linha do TOTAL encaixa nas linhas de "=" e "-".
        //
        // O BR na frente é o Locale: ele decide qual símbolo separa o quê.
        //     Locale.US -> 1,234,567.89      (vírgula milhar, ponto decimal)
        //     BR        -> 1.234.567,89      (ponto milhar, vírgula decimal)
        //
        // Sem nada disso, "TOTAL " + total imprimiria "TOTAL 84.02": sem alinhamento,
        // com ponto no lugar da vírgula e sem garantia das duas casas decimais.
        //
        // Resultado desta linha (as réguas são só para conferir as colunas):
        //              1         2         3         4
        //     123456789012345678901234567890123456789012345
        //     TOTAL                                   84,02
        System.out.println(String.format(BR, "%-28.28s %,16.2f", "TOTAL", total));

        // Rodapé do resumo.
        System.out.println("=".repeat(LARGURA));

    // Fim do método main().
    }

// Fim da classe Main.
}
