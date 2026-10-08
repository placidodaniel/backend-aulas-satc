# mapper: a conversão entre DTO e entidade (Aula 09)

O Mapper transforma o DTO que chegou em entidade, e a entidade em DTO para devolver.

**Nesta etapa:** crie um Mapper para cada entidade. Precisam ter métodos o da API simples (`toEntity`, `toResponse` e `toResponseList`) e os que as rotas das regras de negócio usam.

| Método | O que converte | Usado em |
|---|---|---|
| `toEntity(requestDTO)` | DTO de entrada em entidade nova | `POST` |
| `toResponse(entidade)` | Entidade em DTO de saída | Toda resposta |
| `toResponseList(entidades)` | Lista de entidades em lista de DTOs | `GET` de listagem |
| `updateEntity(entidade, requestDTO)` | Copia os campos do DTO para uma entidade que já existe | `PUT`, nas próximas etapas |

O Mapper não consulta o banco e não tem regra de negócio. Quando a conversão precisa de outra entidade, o Service busca e entrega pronta.

**Para consultar:** `TarefaMapper` em `Aula 09/exemplo_tarefas_resolvido`.
