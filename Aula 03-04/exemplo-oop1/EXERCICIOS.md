# Exercícios: POO (Aula 03/04)

Todos os exercícios usam o projeto `exemplo-oop`. Para rodar e conferir:

```bash
./mvnw test                                  # Linux/Mac  (Windows: mvnw.cmd test)
java -cp target/classes app.Main             # executa o programa
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

---

# Entrega

A entrega tem **duas partes**. As duas são obrigatórias.

### 1. Código no GitHub

Suba o projeto para o **seu** repositório no GitHub. Certifique-se de que o repositório está **público** (ou que o professor tem acesso) e de que `./mvnw test` roda sem erro no que você subiu.

### 2. Respostas por e-mail

Envie um e-mail para **daniel.placido@satc.edu.br** com:

- **Assunto:** `POO Aula 03/04 - <seu nome completo>`
- **Link do repositório** do GitHub no início do corpo do e-mail.
- **As respostas dos subexercícios no CORPO do e-mail**, com a **pergunta copiada em cima** de cada resposta.

Não envie as respostas em anexo, nem só o link do repositório. As respostas têm que estar no corpo do e-mail.

### Modelo do corpo do e-mail

```
Nome: Fulano de Tal
Repositório: https://github.com/fulano/exemplo-oop

--------------------------------------------------
Exercício 1.1
Pergunta: Por que "new Conta("Fulano", "0000-0")" para de compilar e
          "new ContaCorrente(...)" continua funcionando?
Resposta: ...

--------------------------------------------------
Exercício 2.1
Pergunta: O método transferir() tem três linhas e não sabe que a corrente
          cobra taxa nem que a estrangeira converte moeda. Como as duas
          regras foram aplicadas então? Qual conceito de POO está agindo aí?
Resposta: ...

(e assim por diante, até o 4.3)
```

São **12 subexercícios** no total: 1.1, 2.1 a 2.4, 3.1 a 3.4 e 4.1 a 4.3. Responda todos.

---

## Exercício 1: Tornar `Conta` abstrata

O enunciado está dentro de [`Main.java`](src/main/java/app/Main.java), no bloco comentado.

1. Acrescente `abstract` na declaração da classe `Conta`.
2. Descomente o bloco do Exercício 1 no `Main` e adicione `import contas.Conta;`.

### Subexercícios

**1.1**: Por que `new Conta("Fulano", "0000-0")` para de compilar e `new ContaCorrente(...)` continua funcionando?

---

## Exercício 2: `ContaEstrangeira` e transferência entre contas

O banco passou a oferecer conta em dólar. O cliente deposita **reais**, e a conta guarda **dólares**.

### O que criar

Uma classe `ContaEstrangeira`, no pacote `contas`, que **herda** de `Conta` e tem:

| item | detalhe |
|---|---|
| constante | `private static final double COTACAO_DOLAR = 5.40;` |
| construtor | recebe `titular` e `numero`, repassa para a mãe com `super(...)` |
| conversão | ao depositar, o valor em reais é dividido pela cotação antes de virar saldo |
| método novo | `getSaldoEmReais()`: devolve o saldo em dólares multiplicado pela cotação |

E, na classe `Conta`, um método novo:

```java
public boolean transferir(Conta destino, double valor)
```

que **saca** da conta de origem e **deposita** na de destino. Se o saque for recusado, a transferência não acontece e o método devolve `false`.

### Regras

- **O saldo herdado guarda DÓLARES.** Quem chama `depositar()` entrega reais; a conversão é responsabilidade da `ContaEstrangeira`.
- `transferir()` deve funcionar entre **quaisquer** duas contas: corrente → poupança, corrente → estrangeira, etc.
- `transferir()` **não pode** ter `if` perguntando o tipo da conta. Nada de `if (destino instanceof ContaEstrangeira)`.

### Não vale

Alterar `ContaCorrente` ou `ContaPoupanca`. Se você precisou mexer nelas, a solução está no caminho errado: reveja qual método a subclasse deve sobrescrever.

### Teste no `Main`

```java
ContaCorrente ccNatan = new ContaCorrente("Natan", "1234-5");
ccNatan.depositar(1000);
ccNatan.sacar(100);                          // saldo: 899,50

ContaEstrangeira ceNatan = new ContaEstrangeira("Natan", "9999-9");
ccNatan.transferir(ceNatan, 500);

System.out.println(ccNatan);
System.out.println(ceNatan);
```

### Resultado esperado

| verificação | valor |
|---|---|
| `ccNatan.transferir(ceNatan, 500)` | `true` |
| saldo da corrente depois | `399.00`: saíram R$ 500,00 **+ R$ 0,50 de taxa** |
| saldo da estrangeira | `92.59` dólares: 500 ÷ 5,40 |
| `ceNatan.getSaldoEmReais()` | `499.99999999999994` |
| `ccNatan.transferir(ceNatan, 99999)` | `false`, e o saldo continua `399.00` |

Extrato esperado da conta em dólar:

```
=============================================
            BANCO SATC - EXTRATO
=============================================
Titular : Natan
Conta   : 9999-9  (ContaEstrangeira)
---------------------------------------------
DESCRIÇÃO                          VALOR (R$)
Depósito (US$)                          92,59
---------------------------------------------
SALDO                                   92,59
Imposto (IOF 0,5%)                       0,46
=============================================
```

### Subexercícios

**2.1**: O método `transferir()` tem três linhas e **não sabe** que a corrente cobra taxa nem que a estrangeira converte moeda. Como as duas regras foram aplicadas então? Qual conceito de POO está agindo aí?

**2.2**: Por que `getSaldoEmReais()` devolveu `499.99999999999994` em vez de `500.00`? (Dica: o valor voltou de uma divisão por 5,40.)

**2.3**: O cabeçalho do extrato diz `VALOR (R$)`, mas os valores da `ContaEstrangeira` são dólares. Como você consertaria isso **sem** copiar o `toString()` inteiro para a subclasse?

**2.4**: A transferência de R$ 99.999,00 foi recusada e o saldo ficou intacto. Que atributo garantiu isso, e por que o `Main` não conseguiu forçar?

### Desafio (opcional)

Escreva `ContaEstrangeiraTest` em `src/test/java/contas/`, cobrindo:

- a conversão do depósito (R$ 540 viram US$ 100);
- a transferência bem-sucedida, conferindo os **dois** saldos;
- a transferência recusada por falta de saldo, conferindo que nenhum dos dois saldos mudou.

Lembre de comparar `double` com margem: `assertEquals(esperado, real, 0.001)`.

---

## Exercício 3: Método abstrato para obrigar a subclasse a se identificar

> Faça o **Exercício 1** antes: este continua de onde ele parou.

Hoje o extrato descobre o tipo da conta com `getClass().getSimpleName()`, e por isso imprime `(ContaPoupanca)`: nome de classe, com erro de português e tudo. Queremos `(Poupança)`.

A tentação é criar um método comum e torcer para toda subclasse implementar. O jeito certo é o compilador **obrigar**.

### O que fazer

1. Em `Conta` (já abstrata, do Exercício 1), declare um método **sem corpo**:

   ```java
   public abstract String tipoDeConta();
   ```

2. Troque `getClass().getSimpleName()` por `tipoDeConta()` no `toString()`.

3. Tente compilar **antes** de mexer nas subclasses. Anote os erros: o compilador vai listar **toda** classe que herda de `Conta` e ainda não implementou o método.

4. Implemente em cada subclasse:

   | classe | devolve |
   |---|---|
   | `ContaCorrente` | `"Conta Corrente"` |
   | `ContaPoupanca` | `"Poupança"` |
   | `ContaEstrangeira` | `"Conta em Dólar"` |
   | `ContaInvestimento` | `"Investimento"` |

### Resultado esperado

```
Conta   : 1234-5  (Conta Corrente)
Conta   : 6789-0  (Poupança)
```

### Um teste vai quebrar (de propósito)

Depois da mudança, `mvn test` acusa exatamente isto:

```
[ERROR] contas.ContaTest.extratoMostraClasseReal:157 expected: <true> but was: <false>
```

O teste procurava `(ContaPoupanca)` no extrato e agora encontra `(Poupança)`.

**Não apague o teste.** Corrija a expectativa e explique: o teste não estava errado: ele estava **defendendo** o comportamento antigo, e avisou na hora que você mudou. É para isso que ele existe.

### Subexercícios

**3.1**: Qual a diferença entre **classe** abstrata e **método** abstrato? O exemplo tem os dois agora: aponte cada um.

**3.2**: O que acontece se uma subclasse **não** implementar `tipoDeConta()`? Teste, leia o erro e copie a mensagem do compilador na resposta.

**3.3**: Por que `tipoDeConta()` é melhor que `getClass().getSimpleName()`, se os dois funcionam? (Pense em: quem controla o texto que aparece para o cliente?)

**3.4**: `Conta` agora tem um método **com** corpo (`sacar`) e um **sem** corpo (`tipoDeConta`). Uma interface poderia ter os dois? Por que a `Conta` não é uma interface?

---

## Exercício 4: `ContaInvestimento` com imposto só sobre o lucro

O arquivo [`ContaInvestimento.java`](src/main/java/contas/ContaInvestimento.java) já existe e está **vazio**: só herda de `Conta` e não acrescenta nada. Uma subclasse assim não justifica existir. Vamos dar uma razão para ela.

Na conta de investimento o imposto **não** incide sobre o saldo, e sim sobre o **lucro**: se você aplicou R$ 1.000 e o saldo está em R$ 1.100, o IR de 22,5% incide sobre os R$ 100 de rendimento. Sem lucro, imposto zero.

### O que fazer

1. Constante `private static final double ALIQUOTA_IR = 0.225;`
2. Atributo `private double totalAplicado;`: soma de tudo que o cliente depositou.
3. Sobrescreva `depositar(double, String)` para somar em `totalAplicado`.
4. Crie `aplicarRendimento(double percentual)`: diferente da poupança, aqui o percentual é informado por quem chama.
5. Sobrescreva `calcularImposto()`: `(saldo - totalAplicado) * ALIQUOTA_IR`, nunca negativo.

### A pegadinha

O rendimento **não** é dinheiro que o cliente aplicou: é lucro. Se `aplicarRendimento()` somar em `totalAplicado`, o lucro fica sempre zero e o imposto some.

Resolva sem duplicar código. Dica: `depositar(...)` e `super.depositar(...)` **não** são a mesma chamada quando existe sobrescrita.

### Resultado esperado

| passo | saldo | imposto |
|---|---|---|
| `depositar(1000)` | `1000.00` | `0.00`: não há lucro ainda |
| `aplicarRendimento(0.10)` | `1100.00` | `22.50`: 22,5% sobre R$ 100 |

```
=============================================
            BANCO SATC - EXTRATO
=============================================
Titular : Natan
Conta   : 7777-7  (Investimento)
---------------------------------------------
DESCRIÇÃO                          VALOR (R$)
Depósito                             1.000,00
Rendimento (10.0%)                     100,00
---------------------------------------------
SALDO                                1.100,00
Imposto (IOF 0,5%)                      22,50
=============================================
```

### Subexercícios

**4.1**: Olhe o rodapé do extrato acima com atenção. O valor `22,50` está certo, mas o **texto** está errado. Por quê? Onde está escrito, e por que a `ContaInvestimento` não conseguiu mudá-lo? (É o mesmo problema do `VALOR (R$)` na `ContaEstrangeira`.)

**4.2**: `Acao` e `ContaInvestimento` cobram imposto sobre o lucro, e as duas implementam `Tributavel`. Por que uma **herda** de `Conta` e a outra não?

**4.3**: `calcularImposto()` veio da interface, foi implementado na `Conta` e agora foi sobrescrito de novo aqui. Quantas versões desse método existem no projeto? Qual roda quando o objeto está guardado numa variável do tipo `Tributavel`?

### Desafio (opcional)

Acrescente a `ContaInvestimento` na lista `ArrayList<Tributavel>` do `Main` e confira que o resumo de impostos passa a somar quatro origens, **sem nenhuma alteração no laço**.
