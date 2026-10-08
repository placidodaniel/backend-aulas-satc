# dto: o JSON que entra e o que sai (Aula 09)

Um DTO descreve o formato do JSON de uma rota. É o que o cliente da API enxerga; a entidade fica escondida atrás dele.

**Nesta etapa:** crie o DTO de entrada e o de saída de todas as entidades, com os campos e as validações de `docs/03-contrato-da-api.md`.

| DTO | Para que serve | Exemplo |
|---|---|---|
| `...RequestDTO` | O que o cliente pode enviar. É onde ficam as validações (`@NotBlank`, `@NotNull`, `@Positive`...) | `LivroRequestDTO` |
| `...ResponseDTO` | O que a API devolve. Inclui o `id` e os campos que a API calcula | `LivroResponseDTO` |

- Use `record`.
- Nada de anotação de JPA aqui, e nenhuma entidade como campo de um DTO.
- Relacionamento entra como id (`"leitorId": 1`) e sai como objeto (`"leitor": {...}`).

**Já vem pronto:** `ErroDTO` e `CampoErroDTO`, o formato único de erro.

**Para consultar:** `TarefaRequestDTO` e `TarefaResponseDTO` em `Aula 09/exemplo_tarefas_resolvido`.
