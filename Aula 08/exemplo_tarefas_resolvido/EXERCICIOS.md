# Exercícios: Consultas JPA, Validação e Fluxo Completo

Todos os exercícios usam o projeto `exemplo_tarefas`. Para rodar e conferir:

```bash
docker compose up -d                         # sobe o PostgreSQL
./mvnw spring-boot:run                       # Linux/Mac  (Windows: mvnw.cmd spring-boot:run)

# em outro terminal (ou abra http://localhost:8080/ no navegador)
curl http://localhost:8080/tarefas
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

O projeto já vem com o CRUD completo **pronto e funcionando** (`GET /tarefas`, `POST /tarefas`, `GET /tarefas/{id}`, `PUT /tarefas/{id}`, `DELETE /tarefas/{id}`, `PUT /tarefas/{id}/concluir`), nas classes `TarefaController` → `TarefaService` → `TarefaRepository` → Spring Data JPA/Hibernate → PostgreSQL. Leia esse fluxo inteiro antes de começar — é o modelo que você vai repetir nos exercícios abaixo, em lugares onde ele ainda não existe. A página em `http://localhost:8080/` (depois de subir a API) também serve pra testar visualmente, com um log mostrando cada requisição, o Repository e o SQL enviado ao banco.

## Como executar uma migration

As migrations ficam em `src/main/resources/db/migration` e são executadas
automaticamente pelo **Flyway** durante o startup do Spring Boot. Não existe um
comando separado de migration neste projeto.

Para executar a migration inicial:

1. Suba o PostgreSQL:

   ```bash
   docker compose up -d
   docker compose ps
   ```

2. Inicie a aplicação:

   ```bash
   ./mvnw spring-boot:run       # Linux/Mac
   # Windows PowerShell: .\mvnw.cmd spring-boot:run
   ```

3. Confira no console do Spring a aplicação de `V1__criar_tarefas.sql`.
   O Flyway registra o resultado na tabela `flyway_schema_history`.

Para executar uma nova migration, como a `V2__adicionar_prioridade.sql` do
Exercício 5:

1. Crie o arquivo na pasta de migrations, sem editar `V1__criar_tarefas.sql`.
2. Pare e inicie a aplicação novamente. O Flyway encontra a versão pendente e
   executa o SQL antes de o Spring Boot terminar de subir.
3. Verifique o histórico:

   ```bash
   docker compose exec postgres psql -U tarefas -d tarefas \
     -c "SELECT installed_rank, version, description, success FROM flyway_schema_history;"
   ```

Se precisar testar todas as migrations desde o começo, apague o volume local e
suba a aplicação novamente. Isso apaga os dados do banco:

```bash
docker compose down -v
docker compose up -d
./mvnw spring-boot:run
```

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

(e assim por diante, até o 6.3)
```

São **11 subexercícios** no total, distribuídos entre os 6 exercícios. Responda todos.

---

## Exercício 1: Buscar tarefas por responsável

Hoje só existe uma forma de buscar uma tarefa específica: por id (`GET /tarefas/{id}`). Não dá pra perguntar "quais tarefas são da Ana?" sem baixar a lista inteira e procurar na mão.

### O que fazer

1. Crie `GET /tarefas/buscar?responsavel=Ana` em `TarefaController`, usando `@RequestParam String responsavel`.
2. Crie `TarefaService.buscarPorResponsavel(String responsavel)`, que chama um método novo no Repository.
3. Declare em `TarefaRepository` o método derivado `findByResponsavelIgnoreCase(String responsavel)`. O Spring Data JPA transforma o nome do método em uma consulta no PostgreSQL; não filtre uma lista em memória.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

O "encanamento" (Controller → Service → Repository → banco) é sempre o mesmo dos outros endpoints -- a novidade é o nome do método derivado:

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
    return repository.findByResponsavelIgnoreCase(responsavel);
}
```

```java
// TarefaRepository -- o Spring Data JPA gera o SELECT no PostgreSQL
List<Tarefa> findByResponsavelIgnoreCase(String responsavel);
```

Você precisará dos imports de `List` e `RequestParam`.

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

"Atrasada" é uma tarefa cujo prazo (`dataPrazo`) já passou **e** ela ainda não foi concluída. Isso não existe em lugar nenhum do projeto ainda.

### O que fazer

1. Crie `GET /tarefas/atrasadas` em `TarefaController`.
2. Crie em `TarefaRepository` o método derivado `findByConcluidaFalseAndDataPrazoBefore(LocalDate data)`.
3. Crie `TarefaService.listarAtrasadas()`, que chama o método do Repository com `LocalDate.now()`.

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
// TarefaService -- o Repository transforma a consulta em SQL
public Collection<Tarefa> listarAtrasadas() {
    return repository.findByConcluidaFalseAndDataPrazoBefore(LocalDate.now());
}
```

No Repository, declare:

```java
List<Tarefa> findByConcluidaFalseAndDataPrazoBefore(LocalDate data);
```

Precisa de `import java.time.LocalDate;` no Service e no Repository.

</details>

### Regras

- A consulta fica no **Repository**, onde o Spring Data pode executá-la no PostgreSQL. O Service continua sendo a porta da regra para o Controller; o Controller não acessa o Repository diretamente.

### Resultado esperado

| tarefa | está em `/tarefas/atrasadas`? |
|---|---|
| `dataPrazo` ontem, `concluida=false` | Sim |
| `dataPrazo` ontem, `concluida=true` | Não |
| `dataPrazo` amanhã, `concluida=false` | Não |

### Subexercícios

**2.1**: Qual é a vantagem de deixar o filtro no método derivado do Repository em vez de buscar todas as tarefas e filtrar no Service? Pense na quantidade de dados transferida do PostgreSQL para a aplicação.

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

Hoje `GET /tarefas` devolve as tarefas na ordem escolhida pelo banco -- não necessariamente a ordem que importa pra quem está usando um cadastro de tarefas (a mais urgente primeiro).

### O que fazer

1. Crie em `TarefaRepository` o método derivado `findAllByOrderByDataPrazoAsc()`.
2. Em `TarefaService.listarTodas()`, chame esse método para devolver o prazo mais próximo primeiro.
3. Confirme testando `GET /tarefas` com tarefas de prazos diferentes.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// TarefaRepository -- o ORDER BY será executado pelo PostgreSQL
List<Tarefa> findAllByOrderByDataPrazoAsc();

// TarefaService
public Collection<Tarefa> listarTodas() {
    return repository.findAllByOrderByDataPrazoAsc();
}
```

O nome do método instrui o Spring Data a gerar `ORDER BY data_prazo ASC`. Você precisará do import de `List` no Repository.

</details>

### Regras

- A ordenação é pedida pelo **Service** através do Repository; não ordene no Controller nem no JavaScript do frontend.

### Subexercícios

**4.1**: Por que faz mais sentido o Controller pedir a lista ordenada ao Service, em vez de ordenar a resposta HTTP dentro do próprio Controller?

---

## Exercício 5: Novo campo de prioridade — fluxo completo

Agora você vai alterar um dado que percorre o caminho inteiro: formulário do frontend → JSON → DTO → entidade → Repository → Hibernate → PostgreSQL → resposta.

Adicione o campo `prioridade`, um número inteiro de **1 a 5**, em uma tarefa. O valor padrão deve ser `3` quando uma linha antiga do banco for atualizada pela migration.

### O que fazer

1. Crie `V2__adicionar_prioridade.sql` com `ALTER TABLE tarefas ADD COLUMN prioridade INTEGER NOT NULL DEFAULT 3;`. Não edite a migration V1.
2. Adicione `prioridade` à entidade `Tarefa`, mapeado como coluna não nula, incluindo construtor, getter e setter.
3. Adicione `prioridade` ao `TarefaDTO` com `@NotNull`, `@Min(1)` e `@Max(5)`.
4. Faça `TarefaService.criar()` e `TarefaService.atualizar()` copiarem o valor do DTO para a entidade. O `TarefaRepository` já persiste o novo atributo por ser um `JpaRepository`; não crie um Repository novo.
5. No `index.html`, crie um campo numérico de prioridade no formulário de inclusão e no formulário de edição. Inclua o valor no JSON de `POST` e `PUT` e mostre-o na lista.
6. Atualize os logs do frontend para identificar a prioridade no DTO, no model, no Repository e no SQL (`INSERT`/`UPDATE`).

### Resultado esperado

| teste | resultado |
|---|---|
| Criar pela página com prioridade `1` | `201 Created` e a tarefa aparece com prioridade 1 |
| Atualizar pela página para prioridade `5` | `200 OK` e o PostgreSQL fica com prioridade 5 |
| Enviar prioridade `0` ou `6` | `400 Bad Request`, sem gravar a tarefa |
| Reiniciar a API | A prioridade permanece, pois foi persistida no banco |

Confira o caminho completo no log da página e no console da aplicação. Para conferir diretamente no banco:

```bash
docker compose exec postgres psql -U tarefas -d tarefas \\
  -c "SELECT id, titulo, prioridade FROM tarefas ORDER BY id;"
```

### Subexercícios

**5.1**: Por que foi necessário criar uma migration V2 em vez de editar `V1__criar_tarefas.sql`?

**5.2**: Por que não foi necessário criar uma implementação manual de `save()` no `TarefaRepository` depois de adicionar o atributo na entidade?

---

## Exercício 6: Cadastro de responsáveis e vínculo com a tarefa

Hoje o responsável de uma tarefa é só um **texto** (`responsavel: "Daniel"`). Não existe cadastro de pessoas: se alguém digitar "daniel", "Daniel " ou "Dani", o banco trata como três pessoas diferentes, e não há onde guardar o e-mail de ninguém.

Neste exercício você vai criar a entidade **`Responsavel`**, com rota própria (`/responsaveis`), e ligar cada tarefa a um responsável cadastrado por meio de uma **chave estrangeira**.

A parte do frontend **já está pronta**: abra `http://localhost:8080/responsaveis.html` (link também no topo da página principal). Essa página não sabe nada do seu código Java — ela só conhece o **contrato da API** logo abaixo. Se a sua API seguir o contrato, a página funciona; se não seguir, ela mostra no painel **📜 Contrato** qual endpoint está faltando (⏳) ou respondendo diferente do combinado (❌).

> **Faça o Exercício 5 antes.** Este exercício cria a migration `V3`. Se a `V2` ainda não existir quando a `V3` rodar, o Flyway não aceita criar a `V2` depois.

> **Não apague o campo texto `responsavel`.** Ele continua existindo (a página principal e o Exercício 1 usam). O vínculo é um campo **novo**, `responsavelVinculado`.

### Contrato da API

O contrato é o acordo entre quem faz o backend (você) e quem faz o frontend (já feito). **Nomes de rotas, campos JSON e status HTTP têm que ser exatamente estes.** Os nomes das classes Java ficam por sua conta (as sugestões estão em "O que fazer").

#### Formato de um Responsável

```json
{ "id": 1, "nome": "Ana Souza", "email": "ana@satc.edu.br" }
```

#### Formato de uma Tarefa (muda em **todas** as respostas que devolvem tarefa)

Todos os campos que já existem continuam iguais. Entra um campo novo, `responsavelVinculado`, que é o Responsável completo ou `null`:

```json
{
  "id": 1,
  "titulo": "Preparar slides da Aula 08",
  "concluida": false,
  "responsavel": "Daniel",
  "dataPrazo": "2026-10-05",
  "dataCadastro": "2026-09-28",
  "prioridade": 3,
  "responsavelVinculado": { "id": 1, "nome": "Ana Souza", "email": "ana@satc.edu.br" }
}
```

Uma tarefa sem vínculo devolve `"responsavelVinculado": null` (o campo aparece mesmo quando é `null`).

#### Endpoints

| # | Método e rota | Corpo da requisição | Sucesso | Erros |
|---|---|---|---|---|
| 1 | `GET /responsaveis` | — | `200` com array de Responsável, **ordenado por nome** (A→Z) | — |
| 2 | `POST /responsaveis` | `{"nome": "...", "email": "..."}` | `201` com o Responsável criado (com `id`) | `400` se `nome` vazio ou `email` vazio/inválido |
| 3 | `GET /responsaveis/{id}/tarefas` | — | `200` com array das tarefas vinculadas a ele (pode ser `[]`) | `404` se o responsável não existe |
| 4 | `PUT /tarefas/{id}/responsavel` | `{"responsavelId": 1}` | `200` com a Tarefa já com `responsavelVinculado` preenchido | `400` se `responsavelId` ausente/`null`; `404` se a tarefa **ou** o responsável não existe |
| 5 | `DELETE /tarefas/{id}/responsavel` | — | `204` sem corpo; a tarefa fica com `responsavelVinculado: null` | `404` se a tarefa não existe |

Regras gerais do contrato:

- Os erros `400` são os de validação do Spring (`@Valid`), com `errors[].defaultMessage` — igual ao `POST /tarefas`.
- Os erros `404` devolvem **texto puro** pelo `ApiExceptionHandler`, igual à tarefa: `Responsável não encontrado: 999` ou `Tarefa não encontrada: 999`.
- Vincular uma tarefa que já tem responsável **troca** o responsável (não é erro).

### O que fazer

1. **Migration** `V3__criar_responsaveis.sql`: crie a tabela `responsaveis` (`id`, `nome`, `email`, todos `NOT NULL`) e acrescente em `tarefas` a coluna `responsavel_id BIGINT`, que aceita `NULL` e referencia `responsaveis(id)`.
2. **Model**: crie a entidade `Responsavel` (`@Entity`, `@Table(name = "responsaveis")`). Em `Tarefa`, adicione o atributo `responsavelVinculado` do tipo `Responsavel`, mapeado com `@ManyToOne` e `@JoinColumn(name = "responsavel_id")`, com getter e setter.
3. **DTOs**: `ResponsavelDTO` (`nome` com `@NotBlank`; `email` com `@NotBlank` e `@Email`) e `VinculoResponsavelDTO` (`responsavelId` com `@NotNull`).
4. **Repository**: `ResponsavelRepository extends JpaRepository<Responsavel, Long>` com `findAllByOrderByNomeAsc()`. Em `TarefaRepository`, o método derivado `findByResponsavelVinculadoId(Long responsavelId)`.
5. **Service**: `ResponsavelService` (listar, criar, listar tarefas de um responsável) e, em `TarefaService`, `vincularResponsavel(Long tarefaId, Long responsavelId)` e `desvincularResponsavel(Long tarefaId)`. Crie `ResponsavelNaoEncontradoException` e trate-a no `ApiExceptionHandler` com `404`.
6. **Controller**: `ResponsavelController` com `@RequestMapping("/responsaveis")` (endpoints 1 a 3) e, em `TarefaController`, os endpoints 4 e 5.
7. Abra `http://localhost:8080/responsaveis.html` e deixe **todos os itens do painel 📜 Contrato com ✅**. Para o `404` do item 3 e o `400` do item 4, use o painel 🧪 da página.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```sql
-- V3__criar_responsaveis.sql
CREATE TABLE responsaveis (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

ALTER TABLE tarefas ADD COLUMN responsavel_id BIGINT REFERENCES responsaveis (id);
```

```java
// Tarefa -- o lado "muitos" (várias tarefas → um responsável) guarda a FK.
@ManyToOne
@JoinColumn(name = "responsavel_id")
private Responsavel responsavelVinculado;
```

```java
// TarefaService -- busca os dois lados antes de ligar um no outro.
public Tarefa vincularResponsavel(Long tarefaId, Long responsavelId) {
    Tarefa tarefa = buscarPorId(tarefaId);
    Responsavel responsavel = responsavelRepository.findById(responsavelId)
            .orElseThrow(() -> new ResponsavelNaoEncontradoException(responsavelId));
    tarefa.setResponsavelVinculado(responsavel);
    return repository.save(tarefa);
}
```

```java
// TarefaController -- o corpo é um DTO com um único campo.
@PutMapping("/{id}/responsavel")
public Tarefa vincularResponsavel(@PathVariable Long id, @Valid @RequestBody VinculoResponsavelDTO dto) {
    return service.vincularResponsavel(id, dto.getResponsavelId());
}
```

Lembre de injetar o `ResponsavelRepository` no construtor do `TarefaService`. Não crie uma lista de tarefas (`@OneToMany`) dentro de `Responsavel`: o Jackson entraria em loop (tarefa → responsável → tarefas → responsável...) ao gerar o JSON. As tarefas de um responsável vêm do método derivado do `TarefaRepository`.

</details>

### Resultado esperado

| teste | resultado |
|---|---|
| Cadastrar "Bruno" e depois "Ana" pela página | As duas aparecem, **Ana primeiro** |
| Cadastrar com e-mail `ana` (sem @) | `400`, mensagem de validação na página |
| Vincular a tarefa #1 à Ana | `200`, a tarefa mostra `🔗 Ana` |
| 📋 na Ana | Lista a tarefa #1 |
| Desvincular a tarefa #1 | `204`, a tarefa volta a "sem responsável vinculado" |
| 🧪 tarefa `1`, responsável `999` → PUT | `404` com `Responsável não encontrado: 999` |
| 🧪 responsável `999` → GET tarefas | `404` |
| Reiniciar a API | Os vínculos continuam lá (estão no PostgreSQL) |

Para conferir no banco:

```bash
docker compose exec postgres psql -U tarefas -d tarefas \
  -c "SELECT t.id, t.titulo, t.responsavel_id, r.nome FROM tarefas t LEFT JOIN responsaveis r ON r.id = t.responsavel_id ORDER BY t.id;"
```

### Subexercícios

**6.1**: Qual a vantagem de guardar `responsavel_id` (chave estrangeira) em vez do nome como texto, como o campo `responsavel` faz? Pense no que acontece quando a Ana muda de e-mail, ou quando alguém digita o nome dela errado.

**6.2**: Por que o `PUT /tarefas/{id}/responsavel` recebe só `{"responsavelId": 1}`, e não o Responsável inteiro (`nome`, `email`)? O que poderia dar errado se o cliente mandasse o objeto inteiro?

**6.3**: O `TarefaService` busca o Responsável pelo id **antes** de vincular. O que o PostgreSQL faria se a aplicação tentasse gravar `responsavel_id = 999` sem essa busca? Por que é melhor o Service verificar antes e devolver `404`?
