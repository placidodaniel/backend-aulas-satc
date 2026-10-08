# controller: a porta de entrada da API (Aula 07)

É aqui que a requisição HTTP chega. O Controller recebe o pedido, repassa para o Service e devolve a resposta.

**Nesta etapa:** crie um Controller para cada entidade. Só o da API simples precisa ter rotas (`POST`, `GET` e `GET /{id}`). Os outros ficam montados: a classe criada, com o comentário no topo dizendo quais rotas vai atender.

| Faz | Não faz |
|---|---|
| Liga verbo e caminho a um método (`@GetMapping`, `@PostMapping`...) | Regra de negócio |
| Dispara a validação do DTO de entrada (`@Valid`) | Acessar o Repository |
| Escolhe o status HTTP (`200`, `201`, `204`) | Importar qualquer classe de `api.model` |
| Descreve a rota para o Swagger (`@Tag`, `@Operation`) | Montar resposta de erro: isso é do `ApiExceptionHandler` |

**Já vem pronto:** `ApiExceptionHandler`, que transforma os erros em `400` e `404` no formato único.

**Para consultar:** `TarefaController` em `Aula 09/exemplo_tarefas_resolvido`.
