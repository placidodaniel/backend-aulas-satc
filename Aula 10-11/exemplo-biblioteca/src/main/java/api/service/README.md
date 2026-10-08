# service: as regras do sistema (Aula 07)

É aqui que o sistema decide o que pode e o que não pode acontecer. O Service recebe o pedido do Controller, aplica as regras de negócio e usa o Repository para ler e gravar.

**Nesta etapa:** crie um Service para cada entidade. Só o da API simples precisa ter métodos (criar, listar e buscar por id). Nos outros, o comentário no topo lista as regras de negócio que vão morar ali.

| Faz | Não faz |
|---|---|
| Aplica as regras de negócio de `docs/01-visao-geral.md` | Conhecer HTTP (status, `ResponseEntity`) |
| Recebe DTO, pede a conversão ao Mapper e devolve DTO | Devolver entidade para o Controller |
| Busca as outras entidades de que precisa, pelo id | Devolver `null` quando não acha: lança exceção |

**Já vem pronto:** `RecursoNaoEncontradoException`, a classe-mãe dos erros `404`. Cada entidade cria a sua, herdando dela.

**Para consultar:** `TarefaService` em `Aula 09/exemplo_tarefas_resolvido`.
