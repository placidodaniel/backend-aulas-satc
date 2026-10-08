# Autoavaliação

Os 10 critérios que serão avaliados na revisão do código, descritos em `Aula 10-11/AVALIACAO.md`.

| Critério | Marca | Evidência |
|---|---|---|
| P1 A API sobe seguindo o README | ✅ | `README.md`, seção "Como executar" |
| P2 Cada classe no pacote da sua camada | ✅ | `src/main/java/api/` |
| P3 Migrations e `@Entity` de todas as tabelas, e a API sobe | ✅ | `V1` a `V3` em `db/migration/` (as chaves estrangeiras estão na `V3`); `Livro`, `Leitor` e `Emprestimo` em `model/` |
| P4 DTOs de entrada e de saída de todas as entidades | ✅ | Seis records em `dto/` |
| P5 Repository, Mapper, Service e Controller de todas, com o comentário no topo, e o mapa da estrutura | ✅ | As classes das quatro pastas; mapa no `README.md`, seção "Arquitetura" |
| P6 API simples: `POST`, `GET` e `GET /{id}`, com `400` e `404` | ✅ | `LivroController`; o `404` vem de `LivroNaoEncontradoException` |
| P7 No mínimo 3 regras de negócio implementadas, com `400` quando violadas | ✅ | R1, R2 e R3 em `EmprestimoService`, usadas por `POST /emprestimos` e `PUT /emprestimos/{id}/devolver` |
| P8 As regras atendem ao problema e ao tema | ✅ | `README.md`, seção "O problema": as três regras vêm do caderno de empréstimos que a biblioteca usa hoje |
| P9 Código igual ao desenho | ✅ | `docs/modelo-de-dominio.md` e `docs/contrato-da-api.md` |
| P10 README completo e bem escrito | ✅ | `README.md`, conferido no GitHub |

**Pontos que o grupo espera no projeto:** 5,0

A apresentação vale os outros 5,0 pontos: são 3 perguntas, feitas na Aula 11.
