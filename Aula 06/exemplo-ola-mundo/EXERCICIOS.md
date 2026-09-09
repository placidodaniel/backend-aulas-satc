# Exercícios: criar outras APIs

Todos os exercícios usam o projeto `exemplo-ola-mundo`. Para rodar e conferir:

```bash
./mvnw spring-boot:run                       # Linux/Mac  (Windows: mvnw.cmd spring-boot:run)
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

O projeto já vem com três rotas prontas em [`Application.java`](src/main/java/api/Application.java): `GET /ola`, `GET /ola1` e `POST /ola`. Leia esse arquivo antes de começar — os comentários explicam `@RestController`, `@GetMapping`/`@PostMapping` e como o valor de retorno vira o corpo da resposta. Os exercícios abaixo pedem para você acrescentar **novas rotas**, no mesmo arquivo, seguindo o mesmo padrão.

---

# Entrega

A entrega tem **duas partes**. As duas são obrigatórias.

### 1. Código no GitHub

Suba o projeto para o **seu** repositório no GitHub. Certifique-se de que o repositório está **público** (ou que o professor tem acesso) e de que `./mvnw spring-boot:run` sobe sem erro no que você subiu.

### 2. Respostas por e-mail

Envie um e-mail para **daniel.placido@satc.edu.br** com:

- **Assunto:** `APIs - <seu nome completo>`
- **Link do repositório** do GitHub no início do corpo do e-mail.
- **As respostas dos subexercícios no CORPO do e-mail**, com a **pergunta copiada em cima** de cada resposta.

Não envie as respostas em anexo, nem só o link do repositório. As respostas têm que estar no corpo do e-mail.

### Modelo do corpo do e-mail

```
Nome: Fulano de Tal
Repositório: https://github.com/fulano/exemplo-ola-mundo

--------------------------------------------------
Exercício 1.1
Pergunta: ...
Resposta: ...

(e assim por diante, até o 3.1)
```

São **3 subexercícios** no total: 1.1, 2.1 e 3.1. Responda todos.

---

## Exercício 1: `GET /tchau`

Aquecimento: crie uma rota igual a `/ola1`, só que se despedindo.

### O que fazer

Em `Application.java`, adicione um método novo (não mexa nos que já existem):

```java
@GetMapping("/tchau")
public String tchauMundoGet() {
    return "Tchau, mundo!";
}
```

### Resultado esperado

`http://localhost:8080/tchau` no navegador (ou `curl http://localhost:8080/tchau`) devolve:

```
Tchau, mundo!
```

---

## Exercício 2: `GET /saudacao/{nome}` com `@PathVariable`

As rotas atuais sempre devolvem o mesmo texto fixo. Agora a rota vai receber o nome **pela própria URL**, não pelo corpo da requisição.

### O que fazer

1. Adicione o import `org.springframework.web.bind.annotation.PathVariable`.
2. Crie o método:

   ```java
   @GetMapping("/saudacao/{nome}")
   public String saudacaoGet(@PathVariable String nome) {
       return "Olá, " + nome + "!";
   }
   ```

O `{nome}` na rota vira o valor do parâmetro `nome` do método — é o Spring casando o pedaço da URL com o parâmetro pelo nome.

### Resultado esperado

| chamada | resultado |
|---|---|
| `curl http://localhost:8080/saudacao/Maria` | `Olá, Maria!` |
| `curl http://localhost:8080/saudacao/Joao` | `Olá, Joao!` |

---

## Exercício 3: `POST /soma` devolvendo um objeto JSON

Até agora todo retorno foi uma `String` pura. Aqui a rota vai receber dois números no corpo e devolver um **objeto**, que o Spring serializa em JSON sozinho (igual ao record `Nome`, usado no `POST /ola`).

### O que fazer

1. Crie um record para o corpo recebido:

   ```java
   record Numeros(int a, int b) {}
   ```

2. Crie um record para a resposta:

   ```java
   record Resultado(int soma) {}
   ```

3. Crie o método:

   ```java
   @PostMapping("/soma")
   public Resultado somaPost(@RequestBody Numeros numeros) {
       return new Resultado(numeros.a() + numeros.b());
   }
   ```

### Resultado esperado

```bash
curl -X POST http://localhost:8080/soma -H "Content-Type: application/json" -d "{\"a\":3,\"b\":5}"
```

```json
{"soma":8}
```

---

## Subexercícios

**1.1**: No Exercício 2, por que faz mais sentido receber o nome como parte da URL (`@PathVariable`) do que como corpo de um POST (`@RequestBody`), já que o objetivo é só devolver uma saudação?

**2.1**: No Exercício 3, o método devolve um `Resultado` (record), não uma `String`. O que o Spring faz de diferente na resposta HTTP quando o retorno é um objeto em vez de uma `String`?

**3.1**: Se você chamar `curl http://localhost:8080/saudacao` (sem nenhum nome depois da barra), o que acontece? Por quê?
