# Exercícios: DTO, Mapeamento e Swagger

Todos os exercícios usam o projeto `exemplo_tarefas` da Aula 09. Para rodar e conferir:

```bash
docker compose up -d                         # sobe o PostgreSQL (porta 5435)
./mvnw spring-boot:run                       # Linux/Mac  (Windows: .\mvnw.cmd spring-boot:run)

# em outro terminal (ou abra http://localhost:8080/ no navegador)
curl http://localhost:8080/tarefas
```

> **Atenção:** este projeto exige **JDK 25**. Confira com `java -version` antes de começar.

## O que já vem pronto

O projeto parte do gabarito da Aula 08 (tarefas, prioridade, responsáveis e vínculo) com quatro mudanças. Leia essas classes antes de começar, porque os exercícios repetem o mesmo modelo em lugares onde ele ainda não existe:

| O que mudou | Onde ver |
|---|---|
| O `TarefaDTO` virou **dois records**: `TarefaRequestDTO` (o que o cliente manda) e `TarefaResponseDTO` (o que a API devolve). O Controller de tarefas não conhece mais a entidade `Tarefa`. | `dto/TarefaRequestDTO.java`, `dto/TarefaResponseDTO.java`, `controller/TarefaController.java` |
| Toda conversão DTO ↔ entidade de tarefa mora num **mapper**: `toEntity`, `updateEntity`, `toResponse`, `toResponseList`. | `mapper/TarefaMapper.java`, `service/TarefaService.java` |
| **Vínculo já no cadastro:** o `TarefaRequestDTO` tem um `responsavelId` opcional. **Entra um id** (`"responsavelId": 1`) e **sai um objeto** (`"responsavelVinculado": {id, nome, email}`). Quem transforma o id em entidade é o Service (`buscarResponsavel`, 404 se não existir), porque o mapper não consulta o banco. No `PUT`, sem `responsavelId`, a tarefa fica sem vínculo. | `dto/TarefaRequestDTO.java`, `service/TarefaService.java`, `mapper/TarefaMapper.java` |
| **Swagger** (springdoc-openapi): o contrato da API é gerado a partir do código e fica navegável. | `pom.xml`, `config/OpenApiConfig.java`, anotações `@Tag`/`@Operation`/`@ApiResponse` no `TarefaController` |

Com a API rodando, abra:

| Endereço | O que é |
|---|---|
| `http://localhost:8080/swagger-ui.html` | **Swagger UI**: a documentação interativa. Todo endpoint tem **Try it out** para enviar a requisição de verdade. |
| `http://localhost:8080/v3/api-docs` | O contrato **OpenAPI** em JSON, que é o que o Swagger UI desenha. O Postman importa esse arquivo. |
| `http://localhost:8080/` | Cadastro de tarefas: escolha o responsável no select **🔗** e a tarefa já nasce vinculada. Tem também o teste de **mass assignment** (🧪) e o log mostra a camada **MAPPER**. |
| `http://localhost:8080/responsaveis.html` | Responsáveis e vínculo (contrato da Aula 08). |
| `http://localhost:8080/contrato.html` | **Contrato da Aula 09**: testa a sua API contra cada item destes exercícios e marca ✅ / ⏳ / ❌. |

No topo de cada página, a barra **"Sua API, segundo o /v3/api-docs"** mostra ⏳/✅ para cada exercício, lendo o contrato OpenAPI da própria API. As páginas mudam de comportamento sozinhas conforme você implementa: a edição passa a usar `PATCH` (Exercício 3), o 📋 passa a usar o detalhe (Exercício 4), os campos recusados ficam vermelhos com o `ErroDTO` (Exercício 5). Depois de mudar o Java, reinicie a API e dê F5.

O lado de **responsáveis** ficou de propósito no formato da Aula 08: o `ResponsavelController` ainda devolve as entidades. Abra o Swagger UI e role até **Schemas**: `Responsavel` e `Tarefa` aparecem lá, ao lado dos DTOs. É o banco vazando para o contrato da API, e é por aí que começa o Exercício 1.

> **Faça os exercícios em ordem.** O 4 usa o mapper do 1, e o 6 documenta o endpoint do 4 e o erro do 5.

---

# Entrega

A entrega tem **duas partes**. As duas são obrigatórias.

### 1. Código no GitHub

Suba o projeto para o **seu** repositório no GitHub. Certifique-se de que o repositório está **público** (ou que o professor tem acesso) e de que `./mvnw spring-boot:run` sobe sem erro no que você subiu. A página `contrato.html` do seu projeto é o que o professor vai abrir para corrigir.

### 2. Respostas por e-mail

Envie um e-mail para **daniel.placido@satc.edu.br** com:

- **Assunto:** `DTO - <seu nome completo>`
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

(e assim por diante, até o 6.2)
```

São **12 subexercícios** no total, dois em cada exercício. Responda todos.

---

## Exercício 1: Responsável sem entidade no contrato

O `ResponsavelController` ainda faz o que a Aula 08 fazia: devolve `Responsavel` e `Tarefa` (entidades JPA) direto para o Jackson. O JSON sai certo, mas o contrato da API está amarrado às tabelas. Qualquer atributo novo na entidade iria parar na resposta sem ninguém decidir isso.

Neste exercício você aplica no lado de responsáveis o mesmo modelo que o lado de tarefas já usa.

### O que fazer

1. Transforme o `ResponsavelDTO` (classe com getter/setter) no **record** `ResponsavelRequestDTO`, com as mesmas validações. Apague a classe antiga.
2. Crie `mapper/ResponsavelMapper.java` (`@Component`) com:
   - `toEntity(ResponsavelRequestDTO dto)` → `Responsavel`
   - `toResponse(Responsavel responsavel)` → `ResponsavelResponseDTO` (o record já existe). Precisa aceitar `null` e devolver `null`, porque uma tarefa pode não ter responsável vinculado.
   - `toResponseList(Collection<Responsavel>)` → `List<ResponsavelResponseDTO>`
3. No `TarefaMapper`, **apague** o método privado `toResponsavelResponse` e passe a usar o `ResponsavelMapper`, recebido pelo construtor.
4. No `ResponsavelService`, faça os métodos devolverem DTOs: `listarTodos()` → `List<ResponsavelResponseDTO>`, `criar(...)` → `ResponsavelResponseDTO` e `listarTarefas(...)` → `List<TarefaResponseDTO>` (injete o `TarefaMapper` e use o `toResponseList` dele).
5. No `ResponsavelController`, troque os tipos de entrada e de saída pelos DTOs. No fim, **nenhum Controller importa nada de `api.model`**.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// dto/ResponsavelRequestDTO.java -- as anotações vão direto nos componentes do record
public record ResponsavelRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email
) {
}
```

```java
// mapper/ResponsavelMapper.java
@Component
public class ResponsavelMapper {

    public Responsavel toEntity(ResponsavelRequestDTO dto) {
        return new Responsavel(dto.nome(), dto.email());   // record: nome(), não getNome()
    }

    public ResponsavelResponseDTO toResponse(Responsavel responsavel) {
        if (responsavel == null) {
            return null;
        }
        return new ResponsavelResponseDTO(responsavel.getId(), responsavel.getNome(), responsavel.getEmail());
    }

    public List<ResponsavelResponseDTO> toResponseList(Collection<Responsavel> responsaveis) {
        return responsaveis.stream().map(this::toResponse).toList();
    }
}
```

```java
// mapper/TarefaMapper.java -- o mapper de tarefa pede o de responsável no construtor
private final ResponsavelMapper responsavelMapper;

public TarefaMapper(ResponsavelMapper responsavelMapper) {
    this.responsavelMapper = responsavelMapper;
}

// ... e, dentro do toResponse():
responsavelMapper.toResponse(tarefa.getResponsavelVinculado())
```

```java
// service/ResponsavelService.java
public ResponsavelResponseDTO criar(ResponsavelRequestDTO dto) {
    Responsavel salvo = repository.save(mapper.toEntity(dto));
    return mapper.toResponse(salvo);
}
```

</details>

### Regras

- **O JSON não pode mudar.** Nomes de campos, ordem e status continuam os da Aula 08. A página `responsaveis.html` não pode perceber a troca.
- Entidade só entra e sai do **Service para baixo**. Controller e Swagger só enxergam DTOs.

### Resultado esperado

| teste | resultado |
|---|---|
| Swagger UI → seção **Schemas** | Só nomes terminados em `DTO`: `Responsavel` e `Tarefa` sumiram |
| `responsaveis.html` → painel **📜 Contrato** | Todos os itens continuam ✅ |
| `contrato.html` → **Ex. 1** | Três ✅ |
| Log da `responsaveis.html` | No lugar do aviso "⚠ A entidade Responsavel vai direto para o Jackson", aparece o passo **MAPPER** do `ResponsavelMapper` |
| `grep -rn "import api.model" src/main/java/api/controller` | Nenhuma linha |

### Subexercícios

**1.1**: O JSON de `GET /responsaveis` é **exatamente o mesmo** antes e depois deste exercício. Então o que se ganhou com a troca? Pense no que aconteceria se amanhã a entidade `Responsavel` ganhasse um atributo `senha` (ou `cpf`).

**1.2**: Por que o `TarefaMapper` deve usar o `ResponsavelMapper` para montar o `responsavelVinculado`, em vez de manter a sua própria conversão de `Responsavel`?

---

## Exercício 2: Campos calculados no DTO de saída

Quem usa a API quer saber, sem fazer conta, **quantos dias faltam** para o prazo e **se a tarefa está atrasada**. Nenhuma dessas informações existe na tabela, e nem deveria: as duas dependem do dia de hoje.

Um DTO de saída não precisa ser um espelho da tabela. Ele pode ter **menos** campos (esconder) ou **mais** campos (derivar).

### O que fazer

1. Acrescente ao `TarefaResponseDTO`, depois de `prioridade`, os componentes:
   - `long diasRestantes`: dias de hoje até o prazo. `0` = vence hoje; negativo = o prazo já passou.
   - `boolean atrasada`: `true` quando a tarefa **não** foi concluída **e** o prazo é **antes de hoje**.
2. Calcule os dois no `TarefaMapper.toResponse()`. **Não** crie coluna, migration nem atributo na entidade.
3. Documente os dois campos com `@Schema(description = ..., example = ...)`.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// TarefaMapper.toResponse()
long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), tarefa.getDataPrazo());
boolean atrasada = !tarefa.isConcluida() && diasRestantes < 0;

return new TarefaResponseDTO(
        tarefa.getId(),
        // ... os outros campos, na ordem do record ...
        tarefa.getPrioridade(),
        diasRestantes,
        atrasada,
        responsavelMapper.toResponse(tarefa.getResponsavelVinculado()));
```

Imports: `java.time.LocalDate` e `java.time.temporal.ChronoUnit`. Ao acrescentar componentes no record, o compilador vai apontar todo `new TarefaResponseDTO(...)` que ficou com a quantidade errada de argumentos. Deve ser só um lugar, se o Exercício 1 foi feito direito.

</details>

### Resultado esperado

A API não aceita criar uma tarefa com prazo no passado (`@FutureOrPresent`), então, para testar uma atrasada, mude o prazo direto no banco:

```bash
docker compose exec postgres psql -U tarefas -d tarefas \
  -c "UPDATE tarefas SET data_prazo = CURRENT_DATE - 1 WHERE id = 1;"
```

| tarefa | `diasRestantes` | `atrasada` |
|---|---|---|
| prazo daqui a 7 dias, pendente | `7` | `false` |
| prazo hoje, pendente | `0` | `false` |
| prazo ontem, pendente (pelo `UPDATE` acima) | `-1` | `true` |
| prazo ontem, **concluída** (✓ na página) | `-1` | `false` |

A página `index.html` já sabe mostrar esses campos: quando eles aparecem no JSON, cada tarefa ganha a linha "⏳ Faltam N dia(s)" ou "⚠️ Atrasada", com uma borda vermelha.

### Subexercícios

**2.1**: Por que `atrasada` e `diasRestantes` não viram colunas da tabela (e não precisam de migration)? O que daria errado se `atrasada` fosse gravada no banco no momento do cadastro?

**2.2**: Por que calcular esses campos no backend, em vez de deixar cada frontend (a página web, um app de celular, outro sistema) fazer a conta a partir de `dataPrazo`?

---

## Exercício 3: Atualização parcial com PATCH

No Exercício 3.2 da Aula 08 apareceu um problema: o `PUT /tarefas/{id}` usa o mesmo DTO do `POST`, então **todo** campo é obrigatório e o prazo precisa ser hoje ou depois. Para corrigir um erro de digitação no título de uma tarefa atrasada, o cliente é obrigado a mandar um prazo novo.

A saída é outro DTO de entrada, com outro verbo HTTP. O **`PUT`** substitui o recurso inteiro, e o **`PATCH`** altera só o que veio no corpo.

### O que fazer

1. Crie o record `TarefaPatchDTO` com os mesmos cinco campos do `TarefaRequestDTO` (`titulo`, `responsavel`, `dataPrazo`, `prioridade`, `responsavelId`), mas **todos opcionais**:
   - **sem** `@NotNull`/`@NotBlank`, porque campo ausente chega como `null` e `null` quer dizer "não mexer";
   - `titulo` e `responsavel`: `@Pattern(regexp = "(?s).*\\S.*")`, que exige pelo menos um caractere que não seja espaço **se** o campo vier;
   - `dataPrazo`: `@FutureOrPresent`; `prioridade`: `@Min(1)` e `@Max(5)`. Todas essas anotações consideram `null` válido.
2. Crie `TarefaMapper.applyPatch(Tarefa tarefa, TarefaPatchDTO dto, Responsavel responsavel)`, que copia para a entidade **só os campos diferentes de `null`**. O `responsavel` chega pronto (ou `null` = não mexer no vínculo), igual no `toEntity`.
3. Crie `TarefaService.atualizarParcial(Long id, TarefaPatchDTO dto)`: busca a entidade (404 se não existir), resolve o `responsavelId` com o `buscarResponsavel` que já existe, aplica o patch, salva e devolve `TarefaResponseDTO`.
4. Crie `@PatchMapping("/{id}")` no `TarefaController`, com `@Valid @RequestBody TarefaPatchDTO`. Documente com `@Operation` e `@ApiResponse` (200, 400, 404).

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// dto/TarefaPatchDTO.java
public record TarefaPatchDTO(
        @Pattern(regexp = "(?s).*\\S.*", message = "Título não pode ficar em branco")
        String titulo,

        @Pattern(regexp = "(?s).*\\S.*", message = "Responsável não pode ficar em branco")
        String responsavel,

        @FutureOrPresent(message = "Data de prazo não pode ser no passado")
        LocalDate dataPrazo,

        @Min(value = 1, message = "Prioridade mínima é 1")
        @Max(value = 5, message = "Prioridade máxima é 5")
        Integer prioridade,

        Long responsavelId
) {
}
```

```java
// TarefaMapper -- "responsavel" já vem resolvido pelo Service (ou null)
public void applyPatch(Tarefa tarefa, TarefaPatchDTO dto, Responsavel responsavel) {
    if (dto.titulo() != null) {
        tarefa.setTitulo(dto.titulo());
    }
    // ... o mesmo para responsavel (texto), dataPrazo e prioridade
    if (responsavel != null) {
        tarefa.setResponsavelVinculado(responsavel);
    }
}
```

```java
// TarefaService -- o id vira entidade aqui, como no criar()
public TarefaResponseDTO atualizarParcial(Long id, TarefaPatchDTO dto) {
    Tarefa tarefa = buscarEntidade(id);
    mapper.applyPatch(tarefa, dto, buscarResponsavel(dto.responsavelId()));
    return mapper.toResponse(repository.save(tarefa));
}
```

```java
// TarefaController
@PatchMapping("/{id}")
public TarefaResponseDTO atualizarParcial(@PathVariable Long id, @Valid @RequestBody TarefaPatchDTO dto) {
    return service.atualizarParcial(id, dto);
}
```

`prioridade` continua `Integer`, não `int`. Um `int` nunca é `null`, e aí não daria para saber se o cliente mandou `0` ou não mandou nada.

</details>

### Resultado esperado

| chamada | resultado |
|---|---|
| `PATCH /tarefas/1` com `{"titulo": "Novo título"}` | `200`: só o título muda; prazo, responsável e prioridade continuam iguais |
| `PATCH /tarefas/1` com `{"prioridade": 5}` | `200`: só a prioridade muda |
| `PATCH /tarefas/1` com `{}` | `200`: nada muda |
| `PATCH /tarefas/1` com `{"titulo": "   "}` | `400` |
| `PATCH /tarefas/1` com `{"prioridade": 9}` | `400` |
| `PATCH /tarefas/999` com `{"titulo": "x"}` | `404` |
| `PATCH /tarefas/1` com `{"responsavelId": 2}` | `200`: a tarefa passa a ser do responsável #2, e o resto continua igual |
| `PATCH /tarefas/1` com `{"responsavelId": 999}` | `404` |
| Tarefa atrasada (o `UPDATE` do Exercício 2) com `{"titulo": "Corrigido"}` | `200`: o prazo no passado não é validado, porque não foi enviado |

Teste pelo **Swagger UI**: o endpoint novo aparece sozinho, já com o `TarefaPatchDTO` de exemplo no corpo.

**Na página:** assim que o `PATCH` aparece no `/v3/api-docs`, a edição da `index.html` (🔍 → 💾) passa a mandar **só os campos que mudaram** no formulário. Antes disso, ela manda `PUT` com tudo, e numa tarefa atrasada o PUT falha por causa do prazo. Repare também no que a página faz quando você escolhe "sem vínculo" na edição.

### Subexercícios

**3.1**: Por que não dá para reaproveitar o `TarefaRequestDTO` no `PATCH`, trocando só o verbo HTTP?

**3.2**: Com a regra "campo `null` = não mexer", existe alguma alteração que o `PATCH` **não** consegue fazer? Dê um exemplo (pode ser de um campo que ainda não existe na tarefa).

---

## Exercício 4: DTO composto, o detalhe do responsável

Na Aula 08, `Responsavel` não podia ter uma `List<Tarefa>`: o Jackson serializava a entidade direto e entrava em loop (responsável → tarefas → responsável → ...). Com DTOs, o formato da resposta é montado à mão, de cima para baixo, e dá para devolver o responsável **com** as tarefas dele sem loop nenhum.

### Contrato

`GET /responsaveis/{id}` → `200`:

```json
{
  "id": 1,
  "nome": "Ana Souza",
  "email": "ana@satc.edu.br",
  "totalTarefas": 2,
  "tarefasPendentes": 1,
  "tarefas": [
    { "id": 1, "titulo": "Preparar slides da Aula 08", "concluida": false, "dataPrazo": "2026-10-06" },
    { "id": 3, "titulo": "Estudar DTO", "concluida": true, "dataPrazo": "2026-10-10" }
  ]
}
```

- `tarefas`: só as tarefas **vinculadas** ao responsável (`responsavel_id`), em formato **resumido**: `{id, titulo, concluida, dataPrazo}`, sem `responsavelVinculado`. Responsável sem tarefas → `"tarefas": []`.
- `totalTarefas` = tamanho da lista; `tarefasPendentes` = quantas da lista têm `concluida = false`.
- Responsável inexistente → `404` (mesma `ResponsavelNaoEncontradoException`).

### O que fazer

1. Crie os records `TarefaResumoDTO(id, titulo, concluida, dataPrazo)` e `ResponsavelDetalheDTO(id, nome, email, totalTarefas, tarefasPendentes, tarefas)`.
2. No `TarefaMapper`: `toResumo(Tarefa)` e `toResumoList(Collection<Tarefa>)`.
3. No `ResponsavelMapper`: `toDetalhe(Responsavel responsavel, List<TarefaResumoDTO> tarefas)`, que calcula os dois totais a partir da lista.
4. No `ResponsavelService`: `buscarDetalhe(Long id)`, que busca o responsável (ou 404), busca as tarefas com `findByResponsavelVinculadoId`, converte em resumos e monta o detalhe.
5. No `ResponsavelController`: `@GetMapping("/{id}")`.

> ⚠️ **Não injete o `TarefaMapper` dentro do `ResponsavelMapper`.** O `TarefaMapper` já depende do `ResponsavelMapper` (Exercício 1). Se os dois dependerem um do outro, o Spring não consegue criar nenhum e a aplicação **não sobe** (`The dependencies of some of the beans in the application context form a cycle`). Por isso o `toDetalhe` recebe as tarefas **já convertidas**, e quem converte é o Service.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// ResponsavelMapper
public ResponsavelDetalheDTO toDetalhe(Responsavel responsavel, List<TarefaResumoDTO> tarefas) {
    int pendentes = (int) tarefas.stream().filter(tarefa -> !tarefa.concluida()).count();
    return new ResponsavelDetalheDTO(responsavel.getId(), responsavel.getNome(), responsavel.getEmail(),
            tarefas.size(), pendentes, tarefas);
}
```

```java
// ResponsavelService
public ResponsavelDetalheDTO buscarDetalhe(Long id) {
    Responsavel responsavel = repository.findById(id)
            .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
    List<TarefaResumoDTO> tarefas = tarefaMapper.toResumoList(tarefaRepository.findByResponsavelVinculadoId(id));
    return mapper.toDetalhe(responsavel, tarefas);
}
```

`GET /responsaveis/{id}` e `GET /responsaveis/{id}/tarefas` não se confundem: o segundo tem um segmento a mais no caminho.

</details>

### Resultado esperado

| teste | resultado |
|---|---|
| Vincular a tarefa #1 à Ana (pela `responsaveis.html`) e chamar `GET /responsaveis/{id da Ana}` | `200` com a tarefa #1 em `tarefas`, `totalTarefas: 1` |
| Concluir a tarefa #1 e chamar de novo | `tarefasPendentes: 0`; `concluida: true` no resumo |
| Responsável sem nenhuma tarefa | `200` com `"tarefas": []` e os dois totais `0` |
| `GET /responsaveis/999` | `404` |
| 📋 num responsável, na `responsaveis.html` | Passa a usar `GET /responsaveis/{id}` e mostra os totais (antes, a página usava `/{id}/tarefas`) |

### Subexercícios

**4.1**: Por que o `ResponsavelDetalheDTO` pode ter uma lista de tarefas sem cair no loop do Jackson que a Aula 08 evitou, sendo que o Jackson continua sendo quem gera o JSON?

**4.2**: Por que a lista usa `TarefaResumoDTO`, e não o `TarefaResponseDTO` completo?

---

## Exercício 5: Erro padronizado com ErroDTO

Hoje a API tem dois formatos de erro. O **404** sai em texto puro (`Tarefa não encontrada: 999`), e o **400** do `@Valid` sai no JSON padrão do Spring (`timestamp`, `error`, `errors[].defaultMessage`, `path`...). Quem consome a API precisa de dois jeitos de ler erro, e o Swagger não tem como mostrar um formato único.

Um erro também é uma resposta, e toda resposta desta API é um DTO.

### Contrato

**Todo** `400` e **todo** `404` gerados pela API passam a sair assim:

```json
{
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Tarefa não encontrada: 999",
  "caminho": "/tarefas/999",
  "timestamp": "2026-10-06T19:30:00",
  "campos": []
}
```

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos: confira a lista de campos",
  "caminho": "/tarefas",
  "timestamp": "2026-10-06T19:30:00",
  "campos": [
    { "campo": "prioridade", "mensagem": "Prioridade máxima é 5" },
    { "campo": "titulo", "mensagem": "Título é obrigatório" }
  ]
}
```

- `campos` **sempre** existe: lista vazia quando o erro não é de validação.
- `caminho` é a URL chamada (`request.getRequestURI()`).
- Nada de stack trace, nome de classe Java ou mensagem interna do Spring no corpo.

### O que fazer

1. Crie os records `ErroDTO(int status, String erro, String mensagem, String caminho, LocalDateTime timestamp, List<CampoErroDTO> campos)` e `CampoErroDTO(String campo, String mensagem)`.
2. No `ApiExceptionHandler`:
   - troque os dois handlers de 404 por um só, que devolve `ResponseEntity<ErroDTO>` (um `@ExceptionHandler` aceita várias exceções: `@ExceptionHandler({A.class, B.class})`);
   - crie um handler de `MethodArgumentNotValidException` (é a exceção que o `@Valid` lança) que transforma cada `FieldError` num `CampoErroDTO`.
3. Atualize as anotações `@ApiResponse` de 400 e 404 do `TarefaController` para `content = @Content(schema = @Schema(implementation = ErroDTO.class))`, no lugar do `text/plain`/vazio de hoje.
4. **(Opcional)** Trate também `HttpMessageNotReadableException`, lançada quando o JSON nem chega a virar DTO (vírgula faltando, `"dataPrazo": "amanhã"`...), com uma mensagem genérica.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
@ExceptionHandler({TarefaNaoEncontradaException.class, ResponsavelNaoEncontradoException.class})
public ResponseEntity<ErroDTO> tratarNaoEncontrado(RuntimeException e, HttpServletRequest request) {
    return responder(HttpStatus.NOT_FOUND, e.getMessage(), request, List.of());
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErroDTO> tratarValidacao(MethodArgumentNotValidException e, HttpServletRequest request) {
    List<CampoErroDTO> campos = e.getBindingResult().getFieldErrors().stream()
            .map(erro -> new CampoErroDTO(erro.getField(), erro.getDefaultMessage()))
            .sorted(Comparator.comparing(CampoErroDTO::campo))
            .toList();
    return responder(HttpStatus.BAD_REQUEST, "Dados inválidos: confira a lista de campos", request, campos);
}

// um lugar só monta o ErroDTO -- o ApiExceptionHandler vira o "mapper dos erros"
private ResponseEntity<ErroDTO> responder(HttpStatus status, String mensagem, HttpServletRequest request,
                                          List<CampoErroDTO> campos) {
    ErroDTO erro = new ErroDTO(status.value(), status.getReasonPhrase(), mensagem,
            request.getRequestURI(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), campos);
    return ResponseEntity.status(status).body(erro);
}
```

`HttpServletRequest` vem de `jakarta.servlet.http`. O Spring injeta a requisição atual só por ela estar na assinatura do método.

</details>

### Regras

- **Não** crie um `@ExceptionHandler(Exception.class)` "pega-tudo". Ele capturaria também os 404/405 que o próprio Spring gera para rotas que não existem (e a `contrato.html` usa esses 404/405 para saber o que você ainda não implementou).
- As páginas `index.html` e `responsaveis.html` já entendem os dois formatos: os campos recusados ficam **vermelhos** (com a mensagem ao passar o mouse), lidos de `errors[].field` hoje e de `campos[].campo` depois deste exercício. No log, o formato aparece como `(ErroDTO)`.

### Resultado esperado

| chamada | resultado |
|---|---|
| `GET /tarefas/999` | `404`, `ErroDTO` com `"mensagem": "Tarefa não encontrada: 999"` e `"campos": []` |
| `GET /responsaveis/999/tarefas` | `404`, `ErroDTO` com `"mensagem": "Responsável não encontrado: 999"` |
| `POST /tarefas` com `{}` | `400`, `ErroDTO` com quatro itens em `campos` (`dataPrazo`, `prioridade`, `responsavel`, `titulo`) |
| `PATCH /tarefas/1` com `{"prioridade": 9}` | `400`, `ErroDTO` com `campos: [{"campo": "prioridade", ...}]` |
| Swagger UI → qualquer endpoint com 404 | O exemplo do erro mostra o formato do `ErroDTO` |

### Subexercícios

**5.1**: Antes deste exercício, o cliente da API precisava de dois jeitos diferentes de ler um erro. Por que isso é um problema, e o que o `ErroDTO` resolve?

**5.2**: O `ErroDTO` devolve a mensagem de validação e o nome do campo, mas **nunca** o stack trace nem a mensagem interna da exceção (`e.getMessage()` de uma `HttpMessageNotReadableException`, por exemplo). Por quê?

---

## Exercício 6: Documentando o ResponsavelController no Swagger

Abra o Swagger UI e compare os dois grupos. **Tarefas** tem nome, descrição, resumo em cada endpoint e todos os status possíveis. **responsavel-controller** tem só as rotas, sem nenhum texto. Pior: o `POST /responsaveis` aparece respondendo **200**, mas a API devolve **201**. Documentação errada é pior do que nenhuma, porque o cliente confia nela.

### O que fazer

1. No `ResponsavelController`: `@Tag(name = "Responsáveis", description = "...")`.
2. Em **todo** endpoint de `/responsaveis` (inclusive o `GET /responsaveis/{id}` do Exercício 4): `@Operation(summary = "...")` e um `@ApiResponse` para cada status possível. Nos erros, `content = @Content(schema = @Schema(implementation = ErroDTO.class))`.
3. Nos path variables: `@Parameter(description = "...", example = "1")`.
4. Em **cada componente** dos DTOs que você criou (`ResponsavelRequestDTO`, `ResponsavelDetalheDTO`, `TarefaResumoDTO`, `TarefaPatchDTO`, `ErroDTO`, `CampoErroDTO`): `@Schema(description = "...", example = "...")`. O `example` é o que aparece no **Try it out** no lugar de `"string"`.
5. **Importe o contrato no Postman:** *Import* → *Link* → `http://localhost:8080/v3/api-docs`. O Postman monta uma collection com todos os endpoints a partir do OpenAPI.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

Use o `TarefaController` como modelo, porque ele já está todo documentado:

```java
@Tag(name = "Responsáveis", description = "Cadastro de responsáveis e consulta das tarefas de cada um")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/responsaveis")
public class ResponsavelController {

    @Operation(summary = "Cadastra um responsável")
    @ApiResponse(responseCode = "201", description = "Responsável criado -- o corpo já traz o id gerado")
    @ApiResponse(responseCode = "400", description = "Nome vazio ou e-mail vazio/inválido",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping
    public ResponseEntity<ResponsavelResponseDTO> criar(@Valid @RequestBody ResponsavelRequestDTO dto) {
        // ...
    }
}
```

```java
// no record
@Schema(description = "Nome do responsável", example = "Ana Souza")
@NotBlank(message = "Nome é obrigatório")
String nome,
```

</details>

### Resultado esperado

| teste | resultado |
|---|---|
| Swagger UI | Grupo **Responsáveis** com descrição; todo endpoint com resumo ao lado da rota |
| `POST /responsaveis` no Swagger | Respostas documentadas: `201` e `400` (e nenhum `200`) |
| **Try it out** no `POST /responsaveis` | O corpo já vem preenchido com o exemplo (`"Ana Souza"`), não com `"string"` |
| `contrato.html` → **Ex. 6** | Quatro ✅ |
| Postman | Collection gerada a partir de `/v3/api-docs`, com todos os endpoints (nas opções do import, *Folder organization: Tags* agrupa as pastas pelos `@Tag`) |

### Subexercícios

**6.1**: Antes deste exercício, o Swagger dizia que o `POST /responsaveis` devolve `200`, mas a API devolve `201`. De onde o springdoc tirou esse `200`, e por que ele não consegue descobrir o `201` sozinho?

**6.2**: OpenAPI e Swagger são a mesma coisa? E para que serve o JSON de `/v3/api-docs`, se já existe a página do Swagger UI?

---

## Conferindo tudo de uma vez

Abra `http://localhost:8080/contrato.html`. A página roda **25 testes** contra a sua API: cria uma tarefa temporária (já vinculada a um responsável), testa cada exercício e apaga a tarefa no final. O projeto recém-baixado começa com **4 ✅** (o que já vem pronto). O objetivo é chegar em **✅ 24**, ou **✅ 25** se você fizer o passo opcional do Exercício 5 (o item marcado como *(opcional)*).

Se algum item ficar ❌, o detalhe embaixo dele diz o que era esperado e o que veio, e o log ao lado mostra a requisição e a resposta completas.
