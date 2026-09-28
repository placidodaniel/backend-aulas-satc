# Respostas dos subexercícios — Aula 08

Gabarito dos 11 subexercícios do `EXERCICIOS.md`. O código de cada exercício está
resolvido nesta mesma pasta (ver tabela no `README.md`).

---

## Exercício 1: Buscar tarefas por responsável

**1.1**: Por que `/tarefas/buscar` não é capturado por `@GetMapping("/{id}")` do `buscarPorId`, mesmo os dois começando com `/tarefas/`?

Quando mais de um mapeamento serve para a mesma URL, o Spring MVC escolhe o **mais específico**. `/tarefas/buscar` é um caminho literal, sem variável, e `/tarefas/{id}` tem uma variável que aceita qualquer segmento. O literal ganha sempre, e a ordem em que os métodos aparecem na classe não importa. Se não houvesse o `/buscar`, a requisição cairia no `/{id}` e daria `400 Bad Request`, porque `"buscar"` não pode ser convertido em `Long`.

**1.2**: Por que devolver uma lista vazia faz mais sentido aqui do que lançar `TarefaNaoEncontradaException`, como `buscarPorId` faz?

`GET /tarefas/{id}` aponta para **um recurso específico**. Se ele não existe, `404` é a resposta certa. Já `/tarefas/buscar?responsavel=...` é uma **consulta** sobre uma coleção. A consulta existe e funcionou, só não encontrou ninguém, e "zero resultados" é uma resposta válida. Responder `404` faria o cliente tratar como erro uma situação normal, e usaria exceção para controlar o fluxo de um caso que não é excepcional.

---

## Exercício 2: Tarefas atrasadas

**2.1**: Qual é a vantagem de deixar o filtro no método derivado do Repository em vez de buscar todas as tarefas e filtrar no Service?

Com o método derivado, o Hibernate gera `SELECT ... WHERE concluida = false AND data_prazo < ?` e o **PostgreSQL filtra**. Só as linhas atrasadas trafegam pela rede e viram objetos Java. Filtrando no Service, o banco mandaria **todas** as tarefas, e a aplicação gastaria memória e CPU criando objetos que vão ser descartados. Com 10 tarefas a diferença não aparece, mas com 1 milhão aparece. Além disso, o banco pode usar índices para o filtro, e a aplicação não.

---

## Exercício 3: Prazo não pode ser no passado

**3.1**: Qual a diferença entre `@Future` e `@FutureOrPresent`? Por que `@FutureOrPresent` é o certo para este campo?

`@Future` exige uma data **estritamente depois** de hoje. `@FutureOrPresent` aceita **hoje ou depois**. Uma tarefa que precisa ser feita hoje é perfeitamente válida, e com `@Future` ela seria recusada com `400`. Obs.: as duas anotações consideram `null` válido, por isso o `@NotNull` continua no campo.

**3.2**: Essa validação também vale para `PUT /tarefas/{id}` (atualizar)? Por quê?

Vale. `TarefaController.atualizar()` recebe `@Valid @RequestBody TarefaDTO`, o mesmo DTO de `criar()`, e as anotações ficam no DTO, não no endpoint. Consequência prática: para editar o título de uma tarefa cujo prazo já passou, também é preciso mandar um prazo de hoje em diante. Se isso não fosse desejado, seria preciso um DTO separado para atualização, ou grupos de validação.

---

## Exercício 4: Ordenar por prazo

**4.1**: Por que faz mais sentido o Controller pedir a lista ordenada ao Service, em vez de ordenar a resposta HTTP dentro do próprio Controller?

"A mais urgente primeiro" é uma **regra de negócio**, e regra de negócio fica no Service. O Controller só traduz HTTP (rota, status, JSON). Com a regra no Service, qualquer outro ponto de entrada que chame `listarTodas()` (outro Controller, um job agendado, um teste) recebe a mesma ordem, sem duplicar código. E como o Service delega ao Repository, quem executa o `ORDER BY` é o PostgreSQL, que faz isso de forma eficiente.

---

## Exercício 5: Novo campo de prioridade

**5.1**: Por que foi necessário criar uma migration V2 em vez de editar `V1__criar_tarefas.sql`?

O Flyway registra cada migration aplicada na tabela `flyway_schema_history`, com a versão e um **checksum** do arquivo. Nos bancos onde a V1 já rodou, ela **nunca roda de novo**: editar a V1 não criaria a coluna nesses bancos, e ainda mudaria o checksum, o que faz o Flyway **falhar na validação** durante o startup. Migrations são um histórico, e toda mudança de estrutura vira uma versão nova, aplicada em ordem em todos os ambientes.

**5.2**: Por que não foi necessário criar uma implementação manual de `save()` no `TarefaRepository` depois de adicionar o atributo na entidade?

`TarefaRepository` estende `JpaRepository`, e o Spring Data gera a implementação de `save()` em tempo de execução. Essa implementação é genérica: repassa a entidade para o Hibernate, que lê o **mapeamento** da classe `Tarefa` (`@Column`) para montar o `INSERT`/`UPDATE`. Como `prioridade` agora está mapeado, o Hibernate inclui a coluna sozinho. Só foi preciso mudar a entidade, não o Repository.

---

## Exercício 6: Cadastro de responsáveis e vínculo com a tarefa

**6.1**: Qual a vantagem de guardar `responsavel_id` (chave estrangeira) em vez do nome como texto?

- **Fonte única:** os dados da Ana ficam em **uma linha** de `responsaveis`. Se ela muda de e-mail, basta um `UPDATE`, e todas as tarefas dela passam a mostrar o e-mail novo. Com texto, seria preciso atualizar tarefa por tarefa.
- **Sem variações de digitação:** "Ana", "ana " e "Anna" viram três pessoas diferentes em texto. Com o vínculo, o usuário **escolhe** um responsável que já existe.
- **Integridade:** a chave estrangeira impede que uma tarefa aponte para alguém que não existe.
- **Mais informação:** o responsável pode ter e-mail e, no futuro, outros campos (telefone, setor), sem repetir esses dados em cada tarefa.

**6.2**: Por que o `PUT /tarefas/{id}/responsavel` recebe só `{"responsavelId": 1}`, e não o Responsável inteiro?

O responsável **já existe** no banco. Para vincular, a API só precisa saber **qual** ele é. Se o cliente mandasse o objeto inteiro:

- surgiria a dúvida sobre qual valor vale se o nome enviado for diferente do que está no banco. A API teria que ignorar esses campos (e então eles são inúteis) ou alterar o cadastro do responsável por uma rota de tarefa, sem a validação do `ResponsavelDTO`;
- o cliente poderia mandar um `id` junto com nome e e-mail de outra pessoa, gerando dado inconsistente;
- o JSON ficaria maior, e o contrato mais acoplado à estrutura do Responsável.

Cada rota cuida de uma coisa: `/responsaveis` cadastra, e `/tarefas/{id}/responsavel` só liga os dois.

**6.3**: O que o PostgreSQL faria se a aplicação tentasse gravar `responsavel_id = 999` sem a busca prévia? Por que é melhor o Service verificar antes e devolver `404`?

O PostgreSQL **recusaria o UPDATE** por violação da chave estrangeira (`violates foreign key constraint "tarefas_responsavel_id_fkey"`). No Java, isso chega como `DataIntegrityViolationException`, que sem tratamento vira `500 Internal Server Error`, com uma mensagem técnica.

Verificando antes no Service:

- o erro é do **cliente** (mandou um id que não existe), então o status certo é `4xx`, não `500`, que indica defeito do servidor;
- a mensagem é clara e segue o contrato: `Responsável não encontrado: 999`;
- o frontend consegue distinguir "tarefa não existe" de "responsável não existe".

A chave estrangeira continua no banco como **última linha de defesa**, caso algum outro caminho do código esqueça de validar.
