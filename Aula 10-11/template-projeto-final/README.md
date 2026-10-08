# {{Nome do Projeto}}

> **Este é o ponto de partida do projeto do grupo.** Apague este bloco antes de entregar.
>
> **Objetivo do exercício:** planejar e organizar um backend antes de programar. O grupo entrega o desenho do sistema, a estrutura de todas as entidades, uma API simples funcionando e este README. O enunciado completo está em `Aula 10-11/README.md`, no repositório da disciplina.
>
> **O que fazer, na ordem:**
>
> 1. Copie esta pasta inteira para fora do repositório da disciplina, com o nome do projeto, e publique no GitHub. Os comandos estão no enunciado (`Aula 10-11/README.md`, seção "Como começar pelo template").
> 2. Procure por `TROQUE` no projeto (`pom.xml`, `docker-compose.yml`, `OpenApiConfig.java`) e coloque o nome do projeto.
> 3. Preencha o desenho: `docs/01`, `docs/02` e `docs/03`.
> 4. Crie as tabelas e as classes de todas as entidades. O passo a passo está em [src/main/java/api/README.md](src/main/java/api/README.md).
> 5. Faça as três rotas da API simples.
> 6. Por último, escreva este README: troque tudo o que está entre `{{ }}` e apague o que está marcado com *(exemplo)*. Mantenha as seções e a ordem.
> 7. Abra o repositório no GitHub, leia o README como quem nunca viu o projeto e preencha a autoavaliação em `docs/05-plano-de-trabalho.md`.
>
> Na primeira vez que a API sobe, antes de existir tabela e rota, o Flyway avisa `No migrations found` e o Swagger UI mostra `No operations defined in spec!`. É o esperado.

{{Uma ou duas frases: o que o sistema faz e para quem. Quem nunca viu o projeto tem que entender o que ele é lendo só este parágrafo.}}

Projeto Final da disciplina de Backend (Engenharia de Software, SATC). **Etapa 1: arquitetura do backend.**

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

| Nome | GitHub | O que fez nesta etapa |
|---|---|---|
| {{Nome completo}} | [@{{usuario}}](https://github.com/{{usuario}}) | {{ex.: modelo de domínio e migrations}} |
| {{Nome completo}} | [@{{usuario}}](https://github.com/{{usuario}}) | {{ex.: entidades, repositories e DTOs}} |
| {{Nome completo}} | [@{{usuario}}](https://github.com/{{usuario}}) | {{ex.: API simples e documentação}} |

## O problema

**Tema:** {{o tema sorteado}}

{{Em 3 a 5 linhas: o problema que o grupo criou, quem sofre com ele hoje e o que o sistema muda.}}

As regras de negócio que o grupo criou para esse problema:

| # | Regra |
|---|---|
| R1 *(exemplo)* | Um leitor não pode ter mais de 3 empréstimos em aberto |
| R2 *(exemplo)* | Um livro emprestado só pode ser emprestado de novo depois da devolução |
| R3 | ... |

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
| Controller | `controller/` | Recebe a requisição HTTP e devolve status e JSON |
| Service | `service/` | Aplica as regras de negócio |
| Mapper | `mapper/` | Converte DTO em entidade e entidade em DTO |
| Repository | `repository/` | Lê e grava no PostgreSQL |
| Model | `model/` | As entidades, uma por tabela |
| DTO | `dto/` | O JSON que entra e o JSON que sai |

Onde está cada classe *(exemplo: troque pelas classes do grupo)*:

```text
src/main/
├── java/api/
│   ├── controller/   LivroController, LeitorController, EmprestimoController, ApiExceptionHandler
│   ├── service/      LivroService, LeitorService, EmprestimoService
│   ├── mapper/       LivroMapper, LeitorMapper, EmprestimoMapper
│   ├── repository/   LivroRepository, LeitorRepository, EmprestimoRepository
│   ├── model/        Livro, Leitor, Emprestimo
│   ├── dto/          um RequestDTO e um ResponseDTO por entidade, ErroDTO, CampoErroDTO
│   └── config/       OpenApiConfig
└── resources/
    ├── application.properties
    └── db/migration/ V1__criar_livros.sql, V2__criar_leitores.sql, V3__criar_emprestimos.sql
```

O mapa completo, classe por classe, e o caminho de uma requisição estão em [docs/04-arquitetura.md](docs/04-arquitetura.md).

## Entidades

| Entidade | O que representa | Como se relaciona |
|---|---|---|
| Livro *(exemplo)* | Um exemplar do acervo que pode ser emprestado | Aparece em vários empréstimos |
| Leitor *(exemplo)* | Uma pessoa cadastrada na biblioteca | Faz vários empréstimos |
| Emprestimo *(exemplo)* | A retirada de um livro por um leitor, com a data de devolução | Pertence a um leitor e a um livro |

Atributos, tipos e o diagrama ER estão em [docs/02-modelo-de-dominio.md](docs/02-modelo-de-dominio.md).

## Rotas

### As que já funcionam

| Verbo | Caminho | O que faz | Sucesso | Erro |
|---|---|---|---|---|
| `POST` | `/{{recurso}}` | {{Cadastra ...}} | `201` | `400` |
| `GET` | `/{{recurso}}` | {{Lista ...}} | `200` | Nenhum |
| `GET` | `/{{recurso}}/{id}` | {{Busca ... pelo id}} | `200` | `404` |

Exemplo de requisição *(exemplo: troque pelo recurso do grupo)*:

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

Todo erro sai no mesmo formato. Este é o `400` de um cadastro sem título *(exemplo)*:

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

Um id que não existe devolve `404`, com a `mensagem` dizendo o que não foi encontrado e `campos` vazio.

### As planejadas

Estão desenhadas no contrato, com as classes já montadas, mas ainda não respondem.

| Recurso | O que vai fazer | Regras envolvidas |
|---|---|---|
| `/leitores` *(exemplo)* | Cadastro e consulta de leitores | Nenhuma |
| `/emprestimos` *(exemplo)* | Registrar empréstimo e devolução | R1 e R2 |

O contrato completo, com todas as rotas e os DTOs, está em [docs/03-contrato-da-api.md](docs/03-contrato-da-api.md).

## Como executar

Pré-requisitos: JDK 25, Docker Desktop aberto e Git.

```bash
git clone https://github.com/{{usuario}}/{{repositorio}}.git
cd {{repositorio}}

docker compose up -d          # sobe o PostgreSQL na porta 5437
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
| Desenho | {{ex.: completo, em docs/01 a docs/03}} |
| Estrutura | {{ex.: as 3 entidades têm migration, entidade, Repository e DTOs; Mapper, Service e Controller de Leitor e Emprestimo estão montados}} |
| API | {{ex.: as 3 rotas de /livros funcionam}} |

**Ainda não funciona:** {{o que está montado mas não responde, e as regras de negócio que ainda não foram implementadas}}

**Fora do escopo:** {{o que o grupo decidiu não fazer}}

## Documentação

| Documento | O que tem |
|---|---|
| [01. Visão geral](docs/01-visao-geral.md) | Tema, problema, quem usa, regras de negócio |
| [02. Modelo de domínio](docs/02-modelo-de-dominio.md) | Entidades, relacionamentos, diagrama ER, migrations |
| [03. Contrato da API](docs/03-contrato-da-api.md) | Rotas, DTOs, exemplos de JSON, erros |
| [04. Arquitetura](docs/04-arquitetura.md) | Mapa da estrutura, caminho de uma requisição, infraestrutura, decisões |
| [05. Plano de trabalho](docs/05-plano-de-trabalho.md) | Quem fez o quê, autoavaliação e próximos passos |
