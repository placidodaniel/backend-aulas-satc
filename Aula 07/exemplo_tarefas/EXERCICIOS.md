# Exercícios: Filtro, Regra de Negócio e Validação

Todos os exercícios usam o projeto `exemplo_tarefas`. Para rodar e conferir:

```bash
./mvnw spring-boot:run                       # Linux/Mac  (Windows: mvnw.cmd spring-boot:run)

# em outro terminal (ou abra http://localhost:8080/ no navegador)
curl http://localhost:8080/tarefas
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

O projeto já vem com o CRUD completo **pronto e funcionando** (`GET /tarefas`, `POST /tarefas`, `GET /tarefas/{id}`, `PUT /tarefas/{id}`, `DELETE /tarefas/{id}`, `PUT /tarefas/{id}/concluir`), nas classes `TarefaController` → `TarefaService` → `TarefaRepository`. Leia esse fluxo inteiro antes de começar — é o modelo que você vai repetir nos exercícios abaixo, em lugares onde ele ainda não existe. A página em `http://localhost:8080/` (depois de subir a API) também serve pra testar visualmente, com um log mostrando cada requisição.

---

# Entrega

A entrega tem **duas partes**. As duas são obrigatórias.

### 1. Código no GitHub

Suba o projeto para o **seu** repositório no GitHub. Certifique-se de que o repositório está **público** (ou que o professor tem acesso) e de que `./mvnw spring-boot:run` sobe sem erro no que você subiu.

### 2. Respostas por e-mail

Envie um e-mail para **daniel.placido@satc.edu.br** com:

- **Assunto:** `Tarefas - <seu nome completo>`
- **Link do repositório** do GitHub no início do corpo do e-mail.
- **As respostas dos subexercícios no CORPO do e-mail**, com a **pergunta copiada em cima** de cada resposta.

Não envie as respostas em anexo, nem só o link do repositório. As respostas têm que estar no corpo do e-mail.

### Modelo do corpo do e-mail

```
Nome: Fulano de Tal
Repositório: https://github.com/fulano/exemplo_tarefas

--------------------------------------------------
Exercício 1.1
Pergunta: ...
Resposta: ...

(e assim por diante, até o 4.2)
```

São **6 subexercícios** no total, distribuídos entre os 4 exercícios. Responda todos.

---

## Exercício 1: Buscar tarefas por responsável

Hoje só existe uma forma de buscar uma tarefa específica: por id (`GET /tarefas/{id}`). Não dá pra perguntar "quais tarefas são da Ana?" sem baixar a lista inteira e procurar na mão.

### O que fazer

1. Crie `GET /tarefas/buscar?responsavel=Ana` em `TarefaController`, usando `@RequestParam String responsavel` (o mesmo jeito de ler query string já mostrado no README da Aula 07, na Parte 2).
2. Crie `TarefaService.buscarPorResponsavel(String responsavel)`, que chama um método novo no Repository.
3. Crie `TarefaRepository.buscarPorResponsavel(String responsavel)`, filtrando o `Map` com Stream (`.stream().filter(...).toList()`) -- comparando os nomes **sem diferenciar maiúsculas/minúsculas** (`equalsIgnoreCase`).

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

O "encanamento" (Controller → Service → Repository) é sempre o mesmo dos outros endpoints -- só falta escrever o `filter(...)` de dentro do Repository:

```java
// TarefaController
@GetMapping("/buscar")
public Collection<Tarefa> buscarPorResponsavel(@RequestParam String responsavel) {
    return service.buscarPorResponsavel(responsavel);
}
```

```java
// TarefaService -- só repassa pro Repository, igual listarTodas()
public Collection<Tarefa> buscarPorResponsavel(String responsavel) {
    return repository.buscarPorResponsavel(responsavel);
}
```

```java
// TarefaRepository -- aqui está a parte que você precisa completar
public Collection<Tarefa> buscarPorResponsavel(String responsavel) {
    return tarefas.values().stream()
            .filter(tarefa -> /* TODO: tarefa.getResponsavel() "bate" com o parâmetro? use equalsIgnoreCase */)
            .toList();
}
```

`@RequestParam` já vem importado (mesmo pacote `org.springframework.web.bind.annotation` dos outros -- só falta adicionar o import).

</details>

### Regras

- O método novo devolve uma **coleção** (pode estar vazia se ninguém bater com o nome) -- não lança exceção nem devolve 404 aqui; "zero resultados" é uma resposta válida, diferente de "id não encontrado".

### Resultado esperado

| chamada | resultado |
|---|---|
| `GET /tarefas/buscar?responsavel=Daniel` | `200 OK` com as tarefas do Daniel |
| `GET /tarefas/buscar?responsavel=ninguem` | `200 OK` com `[]` (lista vazia) |

### Subexercícios

**1.1**: Por que `/tarefas/buscar` não é capturado por `@GetMapping("/{id}")` do `buscarPorId`, mesmo os dois começando com `/tarefas/`? (Dica: pense em qual dos dois caminhos é mais específico.)

**1.2**: Por que devolver uma lista vazia faz mais sentido aqui do que lançar `TarefaNaoEncontradaException`, como `buscarPorId` faz?

---

## Exercício 2: Tarefas atrasadas

"Atrasada" é uma regra de negócio: uma tarefa está atrasada quando o prazo (`dataPrazo`) já passou **e** ela ainda não foi concluída. Isso não existe em lugar nenhum do projeto ainda.

### O que fazer

1. Crie `GET /tarefas/atrasadas` em `TarefaController`.
2. Crie `TarefaService.listarAtrasadas()`, que pega `repository.listarTodas()` e filtra com Stream: `dataPrazo` antes de `LocalDate.now()` **e** `concluida == false`.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// TarefaController
@GetMapping("/atrasadas")
public Collection<Tarefa> listarAtrasadas() {
    return service.listarAtrasadas();
}
```

```java
// TarefaService -- não existe método novo no Repository; reaproveita listarTodas()
public Collection<Tarefa> listarAtrasadas() {
    return repository.listarTodas().stream()
            .filter(tarefa -> /* TODO: tarefa.getDataPrazo().isBefore(LocalDate.now()) e !tarefa.isConcluida() */)
            .toList();
}
```

Precisa de `import java.time.LocalDate;` no `TarefaService` (o `Tarefa` já importa, mas o `Service` ainda não usa `LocalDate` diretamente).

</details>

### Regras

- A regra de "o que é atrasada" mora **inteira dentro do Service**. Não crie um método `listarAtrasadas()` dentro do `TarefaRepository` -- o Repository só sabe guardar e devolver dados, quem decide o que é "atrasada" é o Service.

### Resultado esperado

| tarefa | está em `/tarefas/atrasadas`? |
|---|---|
| `dataPrazo` ontem, `concluida=false` | Sim |
| `dataPrazo` ontem, `concluida=true` | Não |
| `dataPrazo` amanhã, `concluida=false` | Não |

### Subexercícios

**2.1**: Se essa mesma regra estivesse dentro do `TarefaRepository` em vez do `TarefaService`, o que teoricamente ficaria mais difícil de fazer no futuro (pense em trocar o Map em memória por um banco de dados de verdade, na Aula 08)?

---

## Exercício 3: Prazo não pode ser no passado

Hoje o `TarefaDTO` só exige que `dataPrazo` exista (`@NotNull`) -- mas aceita qualquer data, inclusive uma no passado, o que não faz sentido pra um prazo.

### O que fazer

1. No campo `dataPrazo` de `TarefaDTO`, troque (ou acrescente) a anotação `@NotNull` por `@FutureOrPresent(message = "Data de prazo não pode ser no passado")`.
2. Teste criando uma tarefa com `dataPrazo` de ontem -- deve vir `400 Bad Request`.
3. Teste criando uma tarefa com `dataPrazo` de hoje -- deve funcionar normalmente.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
import jakarta.validation.constraints.FutureOrPresent;
// (pode até deixar o import de @NotNull, se usar em outro lugar -- aqui ele sai)

@FutureOrPresent(message = "Data de prazo não pode ser no passado")
private LocalDate dataPrazo;
```

Só isso -- a anotação troca de lugar, o resto do `TarefaDTO` (getter/setter) continua igual. Teste com:

```bash
curl -X POST http://localhost:8080/tarefas -H "Content-Type: application/json" \
  -d '{"titulo":"Teste","responsavel":"Ana","dataPrazo":"2020-01-01"}'
```

</details>

### Resultado esperado

| `dataPrazo` enviado | resultado |
|---|---|
| ontem | `400 Bad Request` |
| hoje | `201 Created` |
| daqui a 7 dias | `201 Created` |

### Subexercícios

**3.1**: Qual a diferença entre `@Future` e `@FutureOrPresent`? Por que `@FutureOrPresent` é o certo pra este campo, e não `@Future`?

**3.2**: Essa validação também vale pra `PUT /tarefas/{id}` (atualizar)? Por quê? (Dica: `TarefaController.atualizar()` usa o mesmo `TarefaDTO` de `criar()`.)

---

## Exercício 4: Ordenar por prazo

Hoje `GET /tarefas` devolve as tarefas na ordem em que foram guardadas no Map -- não necessariamente a ordem que importa pra quem está usando um cadastro de tarefas (a mais urgente primeiro).

### O que fazer

1. Em `TarefaService.listarTodas()`, ordene a coleção antes de devolver, usando `Comparator.comparing(Tarefa::getDataPrazo)` -- prazo mais próximo primeiro.
2. Confirme testando `GET /tarefas` com tarefas de prazos diferentes.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
import java.util.Comparator;

// TarefaService
public Collection<Tarefa> listarTodas() {
    return repository.listarTodas().stream()
            .sorted(Comparator.comparing(Tarefa::getDataPrazo))
            .toList();
}
```

`Tarefa::getDataPrazo` é uma *method reference* -- o mesmo que escrever `tarefa -> tarefa.getDataPrazo()`, só mais curto. O `Comparator` usa o `LocalDate` devolvido por esse método pra decidir a ordem (`LocalDate` já sabe comparar datas sozinho, do mais antigo pro mais novo).

</details>

### Regras

- A ordenação acontece no **Service**, não no `TarefaRepository.listarTodas()` nem no `TarefaController`.

### Subexercícios

**4.1**: Por que faz mais sentido ordenar dentro do Service do que dentro do Controller (que já tem a lista em mãos e também poderia chamar `.sorted()`)?
