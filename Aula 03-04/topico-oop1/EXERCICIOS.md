# Exercícios: Tratamento de Exceções

Todos os exercícios usam o projeto `exemplo-oop1`. Para rodar e conferir:

```bash
./mvnw test                                  # Linux/Mac  (Windows: mvnw.cmd test)
java -cp target/classes app.Main             # executa o programa
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

O projeto já vem com um exemplo **pronto e funcionando** em [`Main.java`](src/main/java/app/Main.java): o `sacar()` e o `transferir()` da `Conta` lançam `IllegalArgumentException` quando o saldo não cobre o valor pedido, e o `Main` mostra `try/catch/finally` capturando isso. Leia esse trecho antes de começar — é o modelo que você vai repetir no exercício abaixo, num lugar onde ele ainda não existe.

---

# Entrega

A entrega tem **duas partes**. As duas são obrigatórias.

### 1. Código no GitHub

Suba o projeto para o **seu** repositório no GitHub. Certifique-se de que o repositório está **público** (ou que o professor tem acesso) e de que `./mvnw test` roda sem erro no que você subiu.

### 2. Respostas por e-mail

Envie um e-mail para **daniel.placido@satc.edu.br** com:

- **Assunto:** `Exceções - <seu nome completo>`
- **Link do repositório** do GitHub no início do corpo do e-mail.
- **As respostas dos subexercícios no CORPO do e-mail**, com a **pergunta copiada em cima** de cada resposta.

Não envie as respostas em anexo, nem só o link do repositório. As respostas têm que estar no corpo do e-mail.

### Modelo do corpo do e-mail

```
Nome: Fulano de Tal
Repositório: https://github.com/fulano/exemplo-oop1

--------------------------------------------------
Exercício 1.1
Pergunta: Hoje, "ciNatan.aplicarRendimento(-0.10)" não trava e não avisa nada --
          o saldo continua igual e nenhuma linha aparece no extrato. Por que isso
          é pior do que lançar uma exceção?
Resposta: ...

(e assim por diante, até o 1.3)
```

São **3 subexercícios** no total: 1.1 a 1.3. Responda todos.

---

## Exercício 1: `aplicarRendimento()` não valida o percentual

Hoje, `ContaInvestimento.aplicarRendimento(double percentual)` aceita **qualquer** número, inclusive zero ou negativo, sem reclamar:

```java
public void aplicarRendimento(double percentual) {
    super.depositar(getSaldo() * percentual, "Rendimento (" + (percentual * 100) + "%)");
}
```

Se `percentual` for negativo, `getSaldo() * percentual` vira um valor negativo. `Conta.depositar()` (a versão que este método chama) tem a regra `if (valor <= 0) return;` — ou seja, a chamada simplesmente **não faz nada**: não muda o saldo, não entra no extrato, não avisa ninguém. Quem chamou `aplicarRendimento(-0.10)` não tem como saber que o rendimento não foi aplicado.

### O que fazer

1. No início de `ContaInvestimento.aplicarRendimento(double percentual)`, valide o parâmetro **antes** de chamar `super.depositar(...)`:

   ```java
   if (percentual <= 0) {
       throw new IllegalArgumentException("Percentual de rendimento precisa ser positivo");
   }
   ```

2. No `Main.java`, envolva uma chamada com percentual inválido (ex: `ciNatan.aplicarRendimento(-0.10)`) num `try/catch/finally`, no mesmo formato usado para o `sacar()` mais acima no arquivo: no `catch`, imprima `e.getMessage()`; no `finally`, uma linha que rode sempre.

### Regras

- A validação é **dentro de `aplicarRendimento()`**, não dentro de `Conta.depositar()` — `depositar()` é usado por todo mundo (inclusive por depósitos normais), e a regra "percentual positivo" só faz sentido para rendimento.
- O `throw` tem que acontecer **antes** de `super.depositar(...)` rodar. Se a linha de validação vier depois, o rendimento inválido já teria sido processado antes de você perceber o erro.

### Não vale

Colocar um `try/catch` **dentro** do próprio `aplicarRendimento()` para "tratar" a exceção ali mesmo. Isso capturaria o erro antes dele sair do método — quem chamou `aplicarRendimento()` nunca saberia que algo deu errado, e o objetivo do exercício (deixar o chamador decidir o que fazer com o erro) desapareceria.

### Resultado esperado

| chamada | resultado |
|---|---|
| `ciNatan.aplicarRendimento(0.10)` | funciona normalmente, sem exceção |
| `ciNatan.aplicarRendimento(-0.10)` | lança `IllegalArgumentException`, mensagem `"Percentual de rendimento precisa ser positivo"` |
| `ciNatan.aplicarRendimento(0)` | também lança, mesma mensagem |
| saldo depois da tentativa inválida | **inalterado** |

### Subexercícios

**1.1**: Hoje, `ciNatan.aplicarRendimento(-0.10)` não trava e não avisa nada — o saldo continua igual e nenhuma linha aparece no extrato. Por que isso é pior do que lançar uma exceção?

**1.2**: Por que faz mais sentido validar dentro de `aplicarRendimento()` do que dentro de `Conta.depositar()`, que já existe e já é chamado por todo depósito do projeto?

**1.3**: Se `aplicarRendimento()` capturasse a própria exceção com um `try/catch` dentro dele mesmo, o que mudaria para quem chama o método? Por que isso anularia o propósito do exercício?
