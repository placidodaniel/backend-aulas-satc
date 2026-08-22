# Gabarito: respostas dos 12 subexercícios

> Material do professor. Não subir junto com o enunciado para os alunos.
> O código resolvido está no próprio projeto (`Conta`, `ContaEstrangeira`, `ContaInvestimento`)
> e coberto por 40 testes.

---

## Exercício 1

**1.1 — Por que `new Conta("Fulano", "0000-0")` para de compilar e `new ContaCorrente(...)` continua funcionando?**

Porque `abstract` não se herda: é um modificador daquela declaração específica, e `ContaCorrente` não o declara. O erro é `Conta is abstract; cannot be instantiated`.

Repare que os argumentos do construtor estão corretos: o problema não é o construtor, é o `new`. Tanto que `Conta[] listaContas = {ccNatan, cpWesley}` continua compilando — `Conta` segue valendo como **tipo** de variável, array, parâmetro e retorno. O que sumiu foi só a instanciação direta.

Vale insistir num ponto: até o Exercício 3, `Conta` estava **completa** (todos os métodos tinham corpo). O Java conseguiria instanciá-la. O `abstract` ali era uma **proibição de projeto** — "não existe conta genérica no banco" — e não uma limitação técnica.

---

## Exercício 2

**2.1 — O `transferir()` não sabe da taxa nem da cotação. Como as duas regras foram aplicadas?**

**Polimorfismo**, em duas chamadas:

```java
public boolean transferir(Conta destino, double valor) {
    if (!sacar(valor)) return false;   // regra da ORIGEM
    destino.depositar(valor);          // regra do DESTINO
    return true;
}
```

`sacar()` e `depositar()` são resolvidos em tempo de execução pelo objeto real, não pelo tipo declarado. Se a origem é `ContaCorrente`, roda o `sacar()` sobrescrito, que soma a taxa. Se o destino é `ContaEstrangeira`, roda o `depositar()` sobrescrito, que converte.

O ganho fica evidente pelo negativo: sem polimorfismo, `transferir()` precisaria de uma cadeia de `if (destino instanceof ...)`, e **cada conta nova exigiria voltar aqui para acrescentar mais um `if`**. Com polimorfismo, `ContaInvestimento` entrou no projeto depois e `transferir()` nem soube.

Detalhe fino que vale mostrar: a sobrescrita foi feita em `depositar(double, String)`, o **protected de dois parâmetros**. É por ele que o `depositar(double)` público passa, então uma sobrescrita só cobre o depósito direto e a transferência. Quem sobrescreve o de um parâmetro deixa a transferência escapar da conversão.

---

**2.2 — Por que `getSaldoEmReais()` devolve `499.99999999999994`?**

Porque `double` é binário e 500 ÷ 5,40 é dízima nessa base. O valor já entra impreciso e a multiplicação de volta não desfaz o erro.

O caminho é `500 / 5.40 = 92.59259259...`, que não cabe nos 64 bits e é truncado; depois `92.59259259... × 5.40` devolve algo perto de 500, não 500.

Ponto importante para a discussão: **não é culpa da divisão**. O mesmo problema aparece em soma (`0.1 + 0.2`), subtração (`0.3 - 0.1`) e multiplicação (`1.1 * 1.1`). O erro nasce ao **representar** o número, não ao operar.

O `Main` imprime as duas formas de propósito:

```
Convertido de volta (R$)               500,00      <- %.2f arredonda e ESCONDE
   valor cru, sem %.2f: 499.99999999999994         <- o que o double guardou
```

Em sistema financeiro real: `BigDecimal` (construído a partir de **String**) ou `long` de centavos.

---

**2.3 — O cabeçalho diz `VALOR (R$)` mas os valores são dólares. Como consertar sem copiar o `toString()`?**

Criando um **gancho** (na literatura de padrões de projeto, "método-gancho"): um método na mãe que não faz nada de especial sozinho, só existe pra subclasse sobrescrever e pendurar ali seu próprio comportamento — exatamente como o `tipoDeConta()` do Exercício 3.

```java
// em Conta
protected String simboloDaMoeda() { return "R$"; }

// no toString(), no lugar do texto fixo
extrato += String.format("%-28s %16s", "DESCRIÇÃO", "VALOR (" + simboloDaMoeda() + ")") + "\n";

// em ContaEstrangeira
@Override
protected String simboloDaMoeda() { return "US$"; }
```

O `toString()` continua existindo **uma vez só**. A subclasse troca um pedaço, não o documento inteiro.

Essa é a resposta que separa quem entendeu herança de quem só decorou: a alternativa de copiar os 20 lançamentos do `toString()` para a subclasse funciona, e é exatamente o que herança existe para evitar — no dia em que o layout mudar, muda em dois lugares e um vai ser esquecido.

---

**2.4 — O saque de R$ 99.999 foi recusado e o saldo ficou intacto. Que atributo garantiu isso?**

O `private double saldo`, combinado com a **ausência** de `setSaldo()`.

A regra vive em uma linha só, dentro de `sacar()`:

```java
if (valor <= 0 || valor > this.saldo) return false;
```

O `Main` não tem como contornar porque não existe caminho até o atributo: `getSaldo()` só lê. Se `saldo` fosse público, bastaria `conta.saldo = 999999` e a regra do banco viraria decoração — cada ponto do sistema poderia inventar a própria versão da regra.

É a definição prática de encapsulamento: **o dado e a regra que o protege moram juntos, e o acesso é obrigado a passar pela regra.**

---

## Exercício 3

**3.1 — Diferença entre classe abstrata e método abstrato?**

| | classe abstrata | método abstrato |
|---|---|---|
| onde | `public abstract class Conta` | `public abstract String tipoDeConta();` |
| o que diz | "este tipo não vira objeto" | "toda subclasse é obrigada a escrever este método" |
| tem corpo? | a classe tem métodos com corpo | o método **não** tem corpo, acaba em `;` |
| quem obriga | proíbe o `new` | obriga a implementação |

São independentes: dá para ter classe abstrata sem nenhum método abstrato (era o caso da `Conta` entre os Exercícios 1 e 3). O contrário não existe — **método abstrato obriga a classe a ser abstrata**, porque uma classe instanciável não pode ter método sem corpo.

---

**3.2 — O que acontece se a subclasse não implementar `tipoDeConta()`?**

Erro de compilação:

```
error: ContaInvestimento is not abstract and does not override abstract method
       tipoDeConta() in Conta
```

Foi exatamente o que aconteceu com a `ContaInvestimento` vazia no momento em que `Conta` ganhou o método — o compilador acusou antes de qualquer teste rodar.

A subclasse tem duas saídas: implementar o método, ou se declarar `abstract` também — e aí o buraco desce para a próxima da cadeia, até alguém tapá-lo.

---

**3.3 — Por que `tipoDeConta()` é melhor que `getClass().getSimpleName()`?**

Porque separa **nome de arquivo** de **texto que o cliente lê**.

`getSimpleName()` devolve o identificador Java: `ContaPoupanca`, sem cedilha, sem espaço, sem acento — Java não permite. E o extrato é documento de banco.

Três problemas concretos:

1. **Não dá para escrever "Poupança".** O identificador não aceita.
2. **Renomear a classe muda o extrato do cliente.** Uma refatoração interna vaza para fora.
3. **Quem decide o texto é o programador que nomeou o arquivo**, não a regra de negócio.

Com `tipoDeConta()`, o compilador ainda **obriga** cada subclasse nova a escolher seu texto — com `getSimpleName()` ela herdaria um nome qualquer em silêncio.

---

**3.4 — `Conta` tem método com corpo e sem corpo. Uma interface poderia ter os dois? Por que `Conta` não é interface?**

Poderia: desde o Java 8 interface aceita `default` com corpo. Então "ter os dois" não é o critério de decisão.

`Conta` não pode ser interface por causa do **estado**:

```java
private String titular;
private double saldo;
private final ArrayList<String> lancamentos = new ArrayList<>();
```

Interface **não tem atributo de instância** — tudo que se declara nela é `public static final`, ou seja, constante. E sem `saldo` privado não existe o encapsulamento que o exemplo inteiro demonstra: seria impossível impedir `conta.saldo = -5000`.

A regra prática: **interface = contrato sem memória; classe abstrata = família incompleta com memória.** `Tributavel` não guarda nada, então é interface. `Conta` guarda saldo e o protege, então é classe abstrata.

---

## Exercício 4

**4.1 — O rodapé diz `Imposto (IOF 0,5%)` mas o valor é IR de 22,5%. Por quê?**

Porque o texto está **fixo no `toString()` da `Conta`**:

```java
extrato += linha("Imposto (IOF 0,5%)", calcularImposto()) + "\n";
```

O **valor** é polimórfico — `calcularImposto()` roda a versão da `ContaInvestimento` e devolve 22,50 corretamente. O **rótulo** não é: é literal, e a subclasse não tem como trocá-lo sem reescrever o `toString()` inteiro.

É o mesmo defeito do `VALOR (R$)` do subexercício 2.3, e a correção é a mesma: transformar o texto fixo em gancho.

```java
// em Conta
protected String descricaoDoImposto() { return "Imposto (IOF 0,5%)"; }

// em ContaInvestimento
@Override
protected String descricaoDoImposto() { return "Imposto (IR 22,5% s/ lucro)"; }
```

A lição geral: **sobrescrever o cálculo não sobrescreve o texto que fala sobre ele.** Todo pedaço que varia por subclasse precisa do seu próprio gancho.

---

**4.2 — `Acao` e `ContaInvestimento` tributam lucro. Por que uma herda de `Conta` e a outra não?**

Porque herança exige a relação **"É UM"**, e ela só vale para uma das duas.

`ContaInvestimento` **é uma** conta: tem titular, número, saldo, aceita depósito e saque, emite extrato, entra numa transferência. Herdar reaproveita tudo isso.

`Acao` **não é** uma conta: não tem titular nem saldo, ninguém saca de uma ação. Tem código de negociação e lucro. Se herdasse de `Conta`, carregaria `sacar()`, `depositar()` e `transferir()` sem sentido algum — e alguém acabaria chamando.

Elas se parecem em **um** ponto só: as duas sabem calcular imposto. Uma semelhança pontual entre coisas sem parentesco é o caso clássico de **interface**, não de herança.

> Herança é parentesco (`extends`, só uma mãe). Interface é compromisso (`implements`, quantos quiser).

Contraprova prática: `Acao` mora em `investimentos`, `Conta` em `contas`, e nenhum dos dois pacotes conhece o outro. Os dois só conhecem `tributaveis`.

---

**4.3 — Quantas versões de `calcularImposto()` existem? Qual roda numa variável do tipo `Tributavel`?**

**Quatro**, contando a declaração:

| onde | o que faz |
|---|---|
| `Tributavel` | declara a assinatura, sem corpo |
| `Conta` | 0,5% sobre o saldo (herdada por corrente, poupança e dólar) |
| `ContaInvestimento` | 22,5% sobre o lucro (sobrescreve a da `Conta`) |
| `Acao` | 15% sobre o lucro (implementa direto da interface) |

Roda sempre a do **objeto**, nunca a do tipo da variável. O tipo declarado só define **quais métodos podem ser chamados**; qual código executa é decidido em tempo de execução pela classe real.

É o que o laço do `Main` demonstra: uma linha de código, cinco origens, quatro regras diferentes, zero `if`.

```java
for (Tributavel tributavel : listaTributaveis) {
    double imposto = tributavel.calcularImposto();
    ...
}
```

E foi por isso que o desafio funcionou: `ContaEstrangeira` e `ContaInvestimento` entraram na lista depois, e **o laço não mudou uma vírgula**.
