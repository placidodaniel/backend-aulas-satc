# 5. Plano de trabalho

## Quem fez o quê nesta etapa

| Integrante | O que fez |
|---|---|
| {{Nome}} | {{ex.: modelo de domínio, diagrama ER e migrations}} |
| {{Nome}} | {{ex.: entidades, repositories e DTOs}} |
| {{Nome}} | {{ex.: API simples, mapa da estrutura e README}} |

## Autoavaliação

Os 10 critérios que o professor confere na revisão do código, descritos em `Aula 10-11/AVALIACAO.md`. Para cada um, a marca do grupo e onde está a evidência.

- ✅ atende
- ◐ atende em parte (diga o que falta)
- ❌ não atende

| Critério | Marca | Evidência |
|---|---|---|
| P1 A API sobe seguindo o README | | `README.md` |
| P2 Cada classe no pacote da sua camada | | `src/main/java/api/` |
| P3 Migrations de todas as tabelas, com chaves estrangeiras | | {{arquivos em db/migration/}} |
| P4 `@Entity` de todas as tabelas, batendo com as migrations | | {{classes em model/}} |
| P5 DTOs de entrada e de saída de todas as entidades | | {{classes em dto/}} |
| P6 Repository, Mapper, Service e Controller de todas, com o comentário no topo | | {{classes em repository/, mapper/, service/ e controller/}} |
| P7 `POST`, `GET` e `GET /{id}` funcionando | | {{classe do Controller}} |
| P8 `400` e `404` no formato único, com exceção própria no Service | | {{classe da exceção}} |
| P9 Código igual ao desenho de `docs/` | | `docs/02`, `docs/03` e `docs/04` |
| P10 README completo e bem escrito | | `README.md`, conferido no GitHub |

**Pontos que o grupo espera no projeto:** {{de 0 a 5,0}}

A apresentação vale os outros 5,0 pontos: são 3 perguntas, feitas na Aula 11.

## O que fica para as próximas etapas

| Item | Entidade ou rota | Quem |
|---|---|---|
| {{ex.: implementar LeitorService, LeitorMapper e LeitorController}} | {{Leitor}} | {{Nome}} |
| {{ex.: regra R1, limite de 3 empréstimos em aberto}} | {{Emprestimo}} | {{Nome}} |

## Dúvidas e riscos

O que o grupo ainda não sabe como fazer, ou o que pode atrasar o projeto.

- ...
