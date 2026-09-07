# Gabarito: respostas dos 3 subexercícios

> Material do professor. Não subir junto com o enunciado para os alunos.
> O código resolvido está no próprio projeto (`ContaInvestimento.aplicarRendimento()`,
> `Main.java`) e coberto por testes (`ContaInvestimentoTest`).

---

## Exercício 1

**1.1 — Hoje, `ciNatan.aplicarRendimento(-0.10)` não trava e não avisa nada — o saldo continua igual e nenhuma linha aparece no extrato. Por que isso é pior do que lançar uma exceção?**

Porque o erro fica **invisível**. Quem chamou `aplicarRendimento(-0.10)` não recebe `false`, não vê exceção, não vê diferença nenhuma no `System.out.println` seguinte — o programa segue rodando como se tudo tivesse dado certo. Só descobre o problema quem for conferir o saldo depois, linha por linha, e perceber que ele não mudou.

Comparar com a versão anterior do projeto (`sacar()` devolvendo `boolean`): mesmo ali, o chamador pelo menos **tinha a chance** de checar o retorno. Aqui nem isso — `aplicarRendimento()` é `void`, então não existe retorno para checar. Sem `throw`, o "saldo não mudou" é indistinguível de "não tinha nada a mudar mesmo".

Um `throw` transforma um bug silencioso em um erro barulhento **no exato lugar onde a regra foi quebrada**, com uma mensagem (`getMessage()`) dizendo qual foi a regra. É a troca de "descobrir o problema por acaso, depois" por "ser avisado na hora, com o motivo".

---

**1.2 — Por que faz mais sentido validar dentro de `aplicarRendimento()` do que dentro de `Conta.depositar()`, que já existe e já é chamado por todo depósito do projeto?**

Porque "percentual precisa ser positivo" **não é uma regra de depósito** — é uma regra de rendimento. `Conta.depositar(double valor, String descricao)` recebe um **valor em reais já calculado**; ele não sabe (e não devia saber) que aquele valor veio de `saldo * percentual`. Se a validação fosse movida para lá, `depositar()` passaria a rejeitar qualquer valor `<= 0`, inclusive casos onde isso já é o comportamento correto por design — por exemplo, um depósito de R$ 0 sendo silenciosamente ignorado continua fazendo sentido para um depósito comum, mas não faz sentido dizer "percentual inválido" para quem só está depositando dinheiro.

A regra pertence à classe que **entende o domínio** da regra: `ContaInvestimento` é quem sabe o que é um "percentual de rendimento"; `Conta` só sabe o que é "um valor a somar no saldo". Colocar a validação em `aplicarRendimento()` mantém `depositar()` genérico (continua servindo para `ContaCorrente`, `ContaPoupanca`, `ContaEstrangeira`, todo mundo) e a regra específica fica isolada onde ela se aplica.

---

**1.3 — Se `aplicarRendimento()` capturasse a própria exceção com um `try/catch` dentro dele mesmo, o que mudaria para quem chama o método? Por que isso anularia o propósito do exercício?**

```java
public void aplicarRendimento(double percentual) {
    try {
        if (percentual <= 0) {
            throw new IllegalArgumentException("Percentual de rendimento precisa ser positivo");
        }
        super.depositar(getSaldo() * percentual, "Rendimento (" + (percentual * 100) + "%)");
    } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());   // ou qualquer coisa aqui dentro
    }
}
```

Se o `catch` estivesse aqui dentro, a exceção **nunca sairia** de `aplicarRendimento()`. Quem chama o método (o `Main`, ou qualquer outro código) voltaria a ficar exatamente na situação do começo do exercício: chama `ciNatan.aplicarRendimento(-0.10)`, o método "resolve" o problema sozinho lá dentro, e quem chamou não tem como saber que algo deu errado — nem viu a mensagem, nem pode decidir o que fazer com o erro (cancelar uma operação maior, avisar o usuário de um jeito diferente, tentar de novo, etc.).

É a mesma ideia do `transferir()` no exemplo de saque: o motivo dele **não** ter `try/catch` próprio é deixar a decisão para quem está no topo da pilha de chamadas. Capturar a exceção no meio do caminho, sem repassá-la, é abafar o problema em vez de tratá-lo — o "tratamento" de uma exceção não é fazer ela desaparecer, é decidir **onde** e **como** reagir a ela, e quem decide isso deveria ser o código que tem contexto suficiente para uma resposta sensata, geralmente mais perto do usuário final do que o método que descobriu o erro.
