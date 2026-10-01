# Respostas dos subexercícios — Aula 09

Gabarito dos 12 subexercícios do `EXERCICIOS.md`. O código de cada exercício está
resolvido nesta mesma pasta (ver tabela no `README.md`).

---

## Exercício 1: Responsável sem entidade no contrato

**1.1**: O JSON de `GET /responsaveis` é exatamente o mesmo antes e depois deste exercício. Então o que se ganhou com a troca?

O ganho é o **controle** sobre o que sai. Com a entidade exposta, o contrato da API era "tudo o que `Responsavel` tiver de getter". Se amanhã a entidade ganhasse `senha` ou `cpf`, esse campo apareceria **sozinho** em `GET /responsaveis`, no `POST`, dentro do `responsavelVinculado` de **toda** tarefa e em `/responsaveis/{id}/tarefas`. Ninguém precisaria errar de propósito, e a senha (ou um dado pessoal protegido pela LGPD) estaria vazando.

Com o `ResponsavelResponseDTO`, um campo novo na entidade **não aparece** na resposta até alguém decidir colocá-lo no DTO. Outros ganhos:

- a entidade pode mudar (renomear atributo, trocar tipo) e o mapper absorve a diferença, sem quebrar os clientes;
- o Swagger passa a mostrar o contrato de verdade: a seção *Schemas* só tem DTOs;
- o Controller deixa de depender do modelo JPA; ele só fala HTTP e DTO.

**1.2**: Por que o `TarefaMapper` deve usar o `ResponsavelMapper` para montar o `responsavelVinculado`?

Para existir **um lugar só** que decide como um responsável aparece na API. Com duas conversões (uma no `TarefaMapper`, outra no `ResponsavelMapper`), qualquer mudança precisaria ser feita nas duas: um campo novo no `ResponsavelResponseDTO`, mascarar parte do e-mail, tratar o `null`... A que fosse esquecida faria o mesmo responsável aparecer diferente em `GET /responsaveis` e dentro de uma tarefa. Um mapper usar outro é o mesmo princípio do Service usar o Repository: cada classe cuida de uma coisa, e quem precisa **reaproveita** em vez de copiar.

---

## Exercício 2: Campos calculados no DTO de saída

**2.1**: Por que `atrasada` e `diasRestantes` não viram colunas da tabela? O que daria errado se `atrasada` fosse gravada no cadastro?

Os dois dependem do **dia de hoje**, e o dia muda sem ninguém escrever nada no banco. Gravado no cadastro, `atrasada` seria sempre `false`, porque o `@FutureOrPresent` só deixa cadastrar prazo de hoje em diante. Nunca viraria `true` sozinho: seria preciso um job rodando todo dia para atualizar todas as linhas. `diasRestantes` estaria errado já no dia seguinte.

Além disso, seria um dado **redundante**: `dataPrazo` e `concluida` já determinam a resposta. Dado redundante sai de sincronia. Concluir a tarefa, por exemplo, obrigaria a lembrar de atualizar `atrasada` também. Calculando na hora de montar a resposta, o valor está sempre certo, e o custo é uma subtração de datas.

Não precisa de migration porque **a tabela não mudou**. O que mudou foi o contrato (o DTO), e o DTO não é a tabela.

**2.2**: Por que calcular no backend, e não deixar cada frontend fazer a conta?

- **Uma regra, um lugar.** A página web, o app de celular e outro sistema mostram exatamente o mesmo resultado. Se a regra mudar (por exemplo, "atrasada só depois das 18h" ou "contar só dias úteis"), muda-se o backend e todos os clientes passam a seguir a regra nova. Um app de celular levaria dias para ser atualizado nas lojas, e parte dos usuários nunca atualiza.
- **Um relógio só.** Cada cliente tem o seu relógio e o seu fuso horário. Um celular com a data errada calcularia outro valor, e o servidor usa sempre o mesmo relógio.
- **Clientes mais simples.** Sem a conta, nenhum cliente reescreve a regra em JavaScript, Kotlin, Swift...
- A regra de "atrasada" também está na consulta `findByConcluidaFalseAndDataPrazoBefore` (usada em `GET /tarefas/atrasadas`). Mantê-la no backend deixa as duas versões no mesmo projeto, onde dá para garantir que continuem iguais.

---

## Exercício 3: Atualização parcial com PATCH

**3.1**: Por que não dá para reaproveitar o `TarefaRequestDTO` no `PATCH`?

Porque as **regras** são outras, mesmo com os campos iguais. O `TarefaRequestDTO` tem `@NotBlank`/`@NotNull` em todos os campos obrigatórios. Um `PATCH` com `{"titulo": "x"}` voltaria `400` reclamando que faltam `responsavel`, `dataPrazo` e `prioridade`, e o cliente teria que mandar tudo de novo, que é exatamente o `PUT`. E como o `@FutureOrPresent` continua lá, o problema da tarefa atrasada (Aula 08, 3.2) continuaria o mesmo.

No `PUT`, campo ausente é **erro**. No `PATCH`, campo ausente quer dizer **"mantenha"**. Regras diferentes significam contratos diferentes, e cada contrato tem o seu DTO. O Swagger mostra a diferença: no schema `TarefaRequestDTO` os quatro campos aparecem como *required*, e no `TarefaPatchDTO`, nenhum.

**3.2**: Com a regra "campo `null` = não mexer", existe alguma alteração que o `PATCH` não consegue fazer?

Sim: **apagar um valor**, ou seja, gravar `null` de propósito. Com a regra atual, `{"campo": null}` e `{}` chegam iguais no DTO, e a API não tem como saber se o cliente quis limpar o campo ou só não o mandou.

O exemplo está no próprio projeto: o **vínculo com o responsável**. `{"responsavelId": 2}` troca o vínculo, mas `{"responsavelId": null}` não desvincula, porque chega igual a `{}` e quer dizer "não mexer". Por isso o desvínculo tem rota própria (`DELETE /tarefas/{id}/responsavel`). É o que a `index.html` faz quando você escolhe "sem vínculo" na edição: o log avisa e a página chama o `DELETE`. Os outros quatro campos não têm o problema só porque são `NOT NULL` no banco; bastaria a tarefa ganhar uma `descricao` opcional para o cliente conseguir trocá-la pelo `PATCH`, mas nunca apagá-la.

Quando isso importa, as saídas são diferenciar "ausente" de "nulo": o formato **JSON Merge Patch** (RFC 7396), em que `null` significa "apague"; receber o corpo como `Map<String, Object>` e olhar quais chaves vieram; ou usar tipos como `JsonNullable<T>`. Outra opção é o **JSON Patch** (RFC 6902), com operações explícitas (`{"op": "remove", "path": "/descricao"}`).

---

## Exercício 4: DTO composto

**4.1**: Por que o `ResponsavelDetalheDTO` pode ter uma lista de tarefas sem cair no loop do Jackson?

O Jackson gera o JSON seguindo os getters (ou componentes) de cada objeto até chegar em valores simples. Com as **entidades**, a referência seria de mão dupla: `Responsavel.getTarefas()` → cada `Tarefa.getResponsavelVinculado()` → o mesmo `Responsavel` → `getTarefas()` de novo... O grafo de objetos tem um **ciclo**, e o Jackson o percorre sem fim.

O DTO é uma **árvore montada à mão, de cima para baixo**: `ResponsavelDetalheDTO` → `List<TarefaResumoDTO>` → `id`, `titulo`, `concluida`, `dataPrazo`. Nenhum componente do `TarefaResumoDTO` aponta de volta para o responsável, então todo caminho termina num texto, número ou data. O relacionamento continua existindo no banco (`responsavel_id`); o DTO só escolhe navegar por ele numa direção. E a entidade `Responsavel` continua sem `@OneToMany`: as tarefas vêm de uma segunda consulta (`findByResponsavelVinculadoId`).

**4.2**: Por que a lista usa `TarefaResumoDTO`, e não o `TarefaResponseDTO` completo?

- **Repetição:** cada item traria `responsavelVinculado` com o mesmo `{id, nome, email}` que já está no topo do JSON. Com 50 tarefas, seriam 50 cópias do mesmo responsável.
- **Tamanho:** `diasRestantes`, `atrasada`, `dataCadastro`, `prioridade`, `responsavel` (texto) não são necessários para uma listagem dentro de outro recurso. Menos campos, resposta menor e mais rápida. Quem precisar do resto chama `GET /tarefas/{id}`, e é para isso que o `id` está no resumo.
- **Estabilidade:** se o `TarefaResponseDTO` crescer (ganhar outro objeto aninhado, por exemplo), o formato do detalhe do responsável não muda junto.

Cada DTO é desenhado para um uso. Não existe "o DTO da tarefa"; existem o completo, o resumo, o de entrada, o de patch...

---

## Exercício 5: Erro padronizado

**5.1**: Antes deste exercício, o cliente da API precisava de dois jeitos diferentes de ler um erro. Por que isso é um problema, e o que o `ErroDTO` resolve?

Todo cliente precisava descobrir **qual** formato veio (texto no 404, JSON do Spring no 400, outro JSON quando o corpo não era legível) e ter um código para cada um. A função `lerErro()` da `index.html` mostra isso: ela trata três casos. E o formato do 400 nem era nosso: ele depende do Spring e de propriedades do `application.properties`. O `errors[]` com as mensagens só aparece porque o projeto liga `server.error.include-binding-errors=always`. Bastaria alguém apagar essa linha, ou uma atualização do Spring mudar o padrão, para o frontend perder as mensagens de validação sem que nenhum código Java tivesse mudado.

O `ErroDTO` resolve os dois lados:

- **um formato só**, para qualquer erro: `mensagem` para mostrar ao usuário, `campos` para marcar os campos inválidos do formulário, `status`/`caminho`/`timestamp` para log e suporte;
- **formato nosso**: definido no código, estável, e **documentado no Swagger** em cada endpoint.

**5.2**: Por que o `ErroDTO` nunca devolve o stack trace nem a mensagem interna da exceção?

Por **segurança** e porque essa informação não é para o cliente. Um stack trace mostra nomes de classes e pacotes, bibliotecas e versões (Spring, Jackson, Hibernate), às vezes SQL, nomes de tabelas e caminhos de arquivos no servidor. Para um atacante, isso é um mapa: com a versão exata de uma biblioteca, basta procurar vulnerabilidades conhecidas dela. A OWASP chama isso de *information disclosure* (exposição de informação).

A mensagem interna tem o mesmo problema em menor escala. A de uma `HttpMessageNotReadableException` é algo como ``Cannot deserialize value of type `java.time.LocalDate` from String "amanhã"...``: expõe a classe Java e não ajuda o usuário a corrigir nada. Por isso o handler troca por uma mensagem pensada para quem usa a API ("confira o JSON e o formato das datas"). O detalhe técnico vai para o **log do servidor**, onde o desenvolvedor lê. O próprio Spring Boot já esconde o stack trace da resposta por padrão (`server.error.include-stacktrace=never`) pelo mesmo motivo.

---

## Exercício 6: Documentando no Swagger

**6.1**: De onde o springdoc tirou o `200` do `POST /responsaveis`, e por que ele não consegue descobrir o `201` sozinho?

O springdoc monta o documento **lendo a estrutura do código** (anotações e assinaturas dos métodos, por reflexão), sem executar os métodos e sem ler o corpo deles. Da assinatura `ResponseEntity<ResponsavelResponseDTO> criar(...)` ele tira o **formato** da resposta (o tipo genérico), mas o **status** só é decidido em tempo de execução, dentro do método: `ResponseEntity.status(HttpStatus.CREATED)`. Para o springdoc, esse `CREATED` é uma linha de código como outra qualquer. Sem nenhuma indicação, ele assume o padrão de um método que termina sem erro: `200 OK`.

Por isso a documentação precisa declarar o que a assinatura não mostra: `@ApiResponse(responseCode = "201")`, ou `@ResponseStatus(HttpStatus.CREATED)` no método, que o springdoc também lê. Vale o mesmo para os erros: nada na assinatura diz que o método pode terminar em `404`, porque isso acontece numa exceção lançada lá no Service.

**6.2**: OpenAPI e Swagger são a mesma coisa? E para que serve o JSON de `/v3/api-docs`?

Não são a mesma coisa, mas estão ligados:

- **OpenAPI** é a **especificação**: um formato padrão (JSON ou YAML) para descrever uma API HTTP, com rotas, métodos, parâmetros, schemas dos corpos e respostas possíveis. Ela nasceu com o nome "Swagger Specification" e em 2015/2016 foi doada para a OpenAPI Initiative (Linux Foundation), onde ganhou o nome atual.
- **Swagger** é a **família de ferramentas** em volta da especificação: o **Swagger UI** (a página que desenha o documento), o Swagger Editor, o Swagger Codegen. O springdoc gera o documento OpenAPI a partir do código Spring e embute o Swagger UI.

O JSON de `/v3/api-docs` é o **contrato legível por máquina**, e a página do Swagger UI é só um dos programas que o leem, o feito para pessoas. O mesmo arquivo serve para:

- importar no **Postman**/Insomnia e ganhar uma collection pronta;
- **gerar código cliente** (TypeScript para o frontend, Kotlin para o app) com ferramentas como o `openapi-generator`, sem digitar os DTOs de novo do outro lado;
- **testes de contrato** e validação automática. A `contrato.html` usa esse JSON nos Exercícios 1 e 6;
- configurar **API gateways**;
- **comparar versões** do contrato e descobrir, antes do deploy, se uma mudança quebra os clientes.
