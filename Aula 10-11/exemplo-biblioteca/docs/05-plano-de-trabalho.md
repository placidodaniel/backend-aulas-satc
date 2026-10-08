# 5. Plano de trabalho

## Quem fez o quê nesta etapa

| Integrante | O que fez |
|---|---|
| Integrante 1 | Problema, regras de negócio, modelo de domínio, diagrama ER e migrations |
| Integrante 2 | Entidades, repositories, DTOs e as classes montadas de Leitor e Emprestimo |
| Integrante 3 | API simples de livros, Swagger, mapa da estrutura e README |

## Autoavaliação

Os 10 critérios que o professor confere na revisão do código, descritos em `Aula 10-11/AVALIACAO.md`.

| Critério | Marca | Evidência |
|---|---|---|
| P1 A API sobe seguindo o README | ✅ | `README.md`, seção "Como executar" |
| P2 Cada classe no pacote da sua camada | ✅ | `src/main/java/api/` |
| P3 Migrations de todas as tabelas, com chaves estrangeiras | ✅ | `V1` a `V3` em `db/migration/`; as chaves estrangeiras estão na `V3` |
| P4 `@Entity` de todas as tabelas, batendo com as migrations | ✅ | `Livro`, `Leitor` e `Emprestimo` em `model/`; a API sobe com `ddl-auto=validate` |
| P5 DTOs de entrada e de saída de todas as entidades | ✅ | Seis records em `dto/` |
| P6 Repository, Mapper, Service e Controller de todas, com o comentário no topo | ✅ | As classes de `repository/`, `mapper/`, `service/` e `controller/` |
| P7 `POST`, `GET` e `GET /{id}` funcionando | ✅ | `LivroController` |
| P8 `400` e `404` no formato único, com exceção própria no Service | ✅ | `LivroNaoEncontradoException`, filha de `RecursoNaoEncontradoException` |
| P9 Código igual ao desenho de `docs/` | ✅ | `docs/02`, `docs/03` e o mapa em `docs/04` |
| P10 README completo e bem escrito | ✅ | `README.md`, conferido no GitHub |

**Pontos que o grupo espera no projeto:** 5,0

A apresentação vale os outros 5,0 pontos: são 3 perguntas, feitas na Aula 11.

## O que fica para as próximas etapas

| Item | Entidade ou rota | Quem |
|---|---|---|
| Implementar `LeitorMapper`, `LeitorService` e `LeitorController` | `/leitores` | Integrante 2 |
| Implementar `POST /emprestimos` com as regras R1 e R2 | `/emprestimos` | Integrante 1 |
| Implementar `PUT /emprestimos/{id}/devolver` e o cálculo da R3 | `/emprestimos` | Integrante 3 |
| `PUT` e `DELETE` de livros | `/livros` | Integrante 3 |

## Dúvidas e riscos

- Ainda não sabemos como contar os empréstimos em aberto de um leitor (R1) sem trazer todos do banco. A ideia é uma consulta pelo nome do método no `EmprestimoRepository`, como na Aula 8.
- A troca de `disponivel` no livro precisa acontecer junto com a gravação do empréstimo: se uma das duas falhar, a outra não pode ficar gravada.
