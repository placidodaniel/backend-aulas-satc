# Biblioteca

Sistema para a biblioteca controlar os empréstimos de livros: quem pegou, quando pegou e quando precisa devolver.

Projeto Final da disciplina de Backend (Engenharia de Software, SATC). **Etapa 1: arquitetura do backend.**

> **Este é o exemplo pronto da Etapa 1.** Mostra como fica um projeto entregue a partir do template: desenho completo, estrutura de todas as entidades montada, API simples funcionando e README preenchido. Biblioteca não é um dos temas do sorteio.

## Índice

- [Integrantes](#integrantes)
- [O problema](#o-problema)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Entidades](#entidades)
- [Rotas](#rotas)
- [Como executar](#como-executar)
- [Situação do projeto](#situação-do-projeto)
- [Documentação](#documentação)

## Integrantes

| Nome | O que fez nesta etapa |
|---|---|
| Integrante 1 | Problema, regras de negócio, modelo de domínio e migrations |
| Integrante 2 | Entidades, repositories, DTOs e as classes montadas de Leitor e Emprestimo |
| Integrante 3 | API simples de livros, Swagger, mapa da estrutura e README |

## O problema

A biblioteca do bairro controla os empréstimos num caderno. Quando um leitor pede um livro, o bibliotecário folheia páginas para descobrir se o exemplar está na estante ou com alguém, e não tem como saber quem está atrasado. O sistema guarda o acervo, os leitores e cada empréstimo, e responde na hora se um livro está disponível e quais empréstimos passaram do prazo.

As regras de negócio que o grupo criou para esse problema:

| # | Regra |
|---|---|
| R1 | Um leitor não pode ter mais de 3 empréstimos em aberto |
| R2 | Um livro emprestado só pode ser emprestado de novo depois da devolução |
| R3 | O prazo de devolução é de 14 dias; depois disso, o empréstimo fica atrasado |

Quem usa o sistema e o que ficou fora do escopo estão em [docs/01-visao-geral.md](docs/01-visao-geral.md).

## Tecnologias

| O quê | Qual |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 3.5 (Web, Validation, Data JPA) |
| Banco de dados | PostgreSQL 16, em Docker |
| Migrations | Flyway |
| Documentação da API | springdoc-openapi (Swagger UI) |

## Arquitetura

A API é organizada em camadas. Uma requisição entra pelo Controller, passa pelo Service e chega ao banco pelo Repository; a resposta faz o caminho de volta.

| Camada | Pasta | O que faz neste projeto |
|---|---|---|
| Controller | `controller/` | Recebe as requisições de `/livros` e devolve status e JSON |
| Service | `service/` | Cadastra, lista e busca livros; vai aplicar as regras R1 a R3 |
| Mapper | `mapper/` | Converte os DTOs de livro em entidade e a entidade em DTO |
| Repository | `repository/` | Lê e grava livros, leitores e empréstimos no PostgreSQL |
| Model | `model/` | `Livro`, `Leitor` e `Emprestimo`, uma por tabela |
| DTO | `dto/` | O JSON que entra e o que sai de cada entidade |

Onde está cada classe:

```text
src/main/
├── java/api/
│   ├── controller/   LivroController, LeitorController, EmprestimoController, ApiExceptionHandler
│   ├── service/      LivroService, LeitorService, EmprestimoService, LivroNaoEncontradoException
│   ├── mapper/       LivroMapper, LeitorMapper, EmprestimoMapper
│   ├── repository/   LivroRepository, LeitorRepository, EmprestimoRepository
│   ├── model/        Livro, Leitor, Emprestimo
│   ├── dto/          um RequestDTO e um ResponseDTO por entidade, ErroDTO, CampoErroDTO
│   └── config/       OpenApiConfig
└── resources/
    ├── application.properties
    └── db/migration/ V1__criar_livros.sql, V2__criar_leitores.sql, V3__criar_emprestimos.sql
```

As classes de Livro estão implementadas. As de Leitor e Emprestimo estão montadas: já existem e estão ligadas às outras, mas ainda não têm lógica. O mapa completo, classe por classe, e o caminho de uma requisição estão em [docs/04-arquitetura.md](docs/04-arquitetura.md).

## Entidades

| Entidade | O que representa | Como se relaciona |
|---|---|---|
| Livro | Um exemplar do acervo, que pode ser emprestado | Aparece em vários empréstimos |
| Leitor | Uma pessoa cadastrada na biblioteca | Faz vários empréstimos |
| Emprestimo | A retirada de um livro por um leitor, com a data de devolução | Pertence a um leitor e a um livro |

Atributos, tipos e o diagrama ER estão em [docs/02-modelo-de-dominio.md](docs/02-modelo-de-dominio.md).

## Rotas

### As que já funcionam

| Verbo | Caminho | O que faz | Sucesso | Erro |
|---|---|---|---|---|
| `POST` | `/livros` | Cadastra um livro | `201` | `400` |
| `GET` | `/livros` | Lista os livros | `200` | Nenhum |
| `GET` | `/livros/{id}` | Busca um livro pelo id | `200` | `404` |

Exemplo de requisição:

```http
POST /livros
Content-Type: application/json

{
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5"
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "disponivel": true
}
```

### Erros

Todo erro sai no mesmo formato. Este é o `400` de um cadastro sem título:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos: confira a lista de campos",
  "caminho": "/livros",
  "timestamp": "2026-10-06T19:30:00",
  "campos": [
    { "campo": "titulo", "mensagem": "Título é obrigatório" }
  ]
}
```

Um id que não existe devolve `404`, com a mensagem `Livro não encontrado: 999` e `campos` vazio.

### As planejadas

Estão desenhadas no contrato, com as classes já montadas, mas ainda não respondem.

| Recurso | O que vai fazer | Regras envolvidas |
|---|---|---|
| `/leitores` | Cadastro e consulta de leitores | Nenhuma |
| `/emprestimos` | Registrar empréstimo e devolução | R1, R2 e R3 |

O contrato completo, com todas as rotas e os DTOs, está em [docs/03-contrato-da-api.md](docs/03-contrato-da-api.md).

## Como executar

Pré-requisitos: JDK 25, Docker Desktop aberto e Git.

```bash
cd "Aula 10-11/exemplo-biblioteca"

docker compose up -d          # sobe o PostgreSQL na porta 5438
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Com a API no ar:

| Endereço | O que é |
|---|---|
| <http://localhost:8080/swagger-ui.html> | Swagger UI: as rotas, com **Try it out** para enviar requisições |
| <http://localhost:8080/v3/api-docs> | O contrato OpenAPI em JSON |

Para testar as rotas, use o Swagger UI ou o **Postman**. No Postman, importe todas as rotas de uma vez: **Import** → **Link** → `http://localhost:8080/v3/api-docs`.

Para parar: `Ctrl+C` na API e `docker compose stop` no banco. Para apagar o banco e rodar as migrations do zero: `docker compose down -v` e suba de novo.

## Situação do projeto

| Parte | Situação |
|---|---|
| Desenho | Completo, em `docs/01` a `docs/03` |
| Estrutura | As 3 entidades têm migration, entidade, Repository e DTOs. Mapper, Service e Controller de Leitor e Emprestimo estão montados |
| API | As 3 rotas de `/livros` funcionam |

**Ainda não funciona:** as rotas de `/leitores` e de `/emprestimos`, e as regras R1, R2 e R3, que estão escritas no cabeçalho do `EmprestimoService` e do `EmprestimoMapper`.

**Fora do escopo:** multa por atraso, reserva de livros e tela de frontend.

## Documentação

| Documento | O que tem |
|---|---|
| [01. Visão geral](docs/01-visao-geral.md) | Problema, quem usa, regras de negócio |
| [02. Modelo de domínio](docs/02-modelo-de-dominio.md) | Entidades, relacionamentos, diagrama ER, migrations |
| [03. Contrato da API](docs/03-contrato-da-api.md) | Rotas, DTOs, exemplos de JSON, erros |
| [04. Arquitetura](docs/04-arquitetura.md) | Mapa da estrutura, caminho de uma requisição, infraestrutura, decisões |
| [05. Plano de trabalho](docs/05-plano-de-trabalho.md) | Quem fez o quê, autoavaliação e próximos passos |
