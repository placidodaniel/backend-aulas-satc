# Exercício extra: JSON com múltiplos campos (Aula 06)

Usa o projeto `exercicio-json`. Para rodar:

```bash
mvn compile exec:java -Dexec.mainClass=org.example.Main
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

---

## Exercício: adicionando um novo campo ao `Produto`

O projeto já serializa e desserializa um `Produto` (`nome` + `preco`) usando o Jackson (`ObjectMapper`), o exercício 2 da Aula 6. Agora vamos acrescentar um novo campo: `peso` (em kg).

### O que fazer

1. Em `Produto.java`, adicione o atributo `private double peso;`.
2. Atualize o construtor para receber o `peso`, e crie o getter/setter (`getPeso()` / `setPeso(double)`).
3. Atualize o `toString()` para incluir o `peso`.
4. Em `Main.java`, informe um valor de `peso` ao criar o `Produto` e inclua o campo na verificação de round-trip (compare `produto.getPeso()` com `produtoConvertido.getPeso()`).
5. Rode o programa e confira que o JSON impresso agora tem três campos: `nome`, `preco` e `peso`.

### Resultado esperado

```json
{"nome":"Mouse Gamer","preco":149.9,"peso":27.33}
```

```
OK: round-trip Produto <-> JSON bateu certinho.
```

### Desafio (opcional)

Acrescente também um campo `disponivel` (`boolean`), como no exemplo de sintaxe JSON do início da Parte 3. Repare que o Jackson espera o getter no padrão `isDisponivel()`, não `getDisponivel()` — é a mesma convenção de atributo booleano vista na Aula 3 e 4.
