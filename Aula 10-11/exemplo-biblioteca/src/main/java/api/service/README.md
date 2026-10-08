# service: as regras do sistema (Aula 07)

É aqui que o sistema decide o que pode e o que não pode acontecer. O Service recebe o pedido do Controller, aplica as regras de negócio e usa o Repository para ler e gravar.

**Nesta etapa:** crie um Service para cada entidade. Precisam ter métodos o da API simples (criar, listar e buscar por id) e os que aplicam as regras de negócio: **as regras precisam estar implementadas aqui**. Quando uma regra é violada, lance `RegraDeNegocioException`, e o cliente recebe `400` com a mensagem da regra.

| Faz | Não faz |
|---|---|
| Aplica as regras de negócio que estão no README | Conhecer HTTP (status, `ResponseEntity`) |
| Recebe DTO, pede a conversão ao Mapper e devolve DTO | Devolver entidade para o Controller |
| Busca as outras entidades de que precisa, pelo id | Devolver `null` quando não acha: lança exceção |

**Já vem pronto:** `RecursoNaoEncontradoException`, a classe-mãe dos erros `404` (cada entidade cria a sua, herdando dela), e `RegraDeNegocioException`, para as regras violadas (`400`).

**Para consultar:** `TarefaService` em `Aula 09/exemplo_tarefas_resolvido`.
