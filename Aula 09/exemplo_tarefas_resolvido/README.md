# Exemplo de tarefas — Aula 09 (RESOLVIDO)

> **Gabarito.** Esta pasta é o projeto `exemplo_tarefas` da Aula 09 com os
> **Exercícios 1 a 6** do `EXERCICIOS.md` já resolvidos (inclusive o passo
> opcional do Exercício 5). As respostas dos subexercícios estão em
> [`RESPOSTAS.md`](RESPOSTAS.md). A página `contrato.html` deste projeto fica
> toda ✅ (25 de 25).
>
> O banco deste projeto usa a porta **5436** e o container
> `aula09-tarefas-resolvido-postgres`, para não misturar com o banco do projeto
> dos alunos (porta 5435). Rode os dois ao mesmo tempo só se trocar a porta da
> API (`--server.port=8081`), pois ambos usam a 8080.

| Exercício | Onde está a solução |
|---|---|
| 1. Responsável sem entidade no contrato | `ResponsavelRequestDTO` (record), `ResponsavelMapper`, `TarefaMapper` (recebe o `ResponsavelMapper` no construtor), `ResponsavelService` e `ResponsavelController` só com DTOs |
| 2. Campos calculados | `TarefaResponseDTO.diasRestantes`/`atrasada`, calculados em `TarefaMapper.toResponse()` |
| 3. PATCH | `TarefaPatchDTO` (cinco campos, inclusive `responsavelId`), `TarefaMapper.applyPatch()`, `TarefaService.atualizarParcial()`, `TarefaController.atualizarParcial()` (`@PatchMapping`) |
| 4. Detalhe do responsável | `TarefaResumoDTO`, `ResponsavelDetalheDTO`, `TarefaMapper.toResumo()`, `ResponsavelMapper.toDetalhe()`, `ResponsavelService.buscarDetalhe()`, `GET /responsaveis/{id}` |
| 5. Erro padronizado | `ErroDTO`, `CampoErroDTO`, `ApiExceptionHandler` (404, validação e JSON ilegível), `@ApiResponse` com `ErroDTO` nos dois Controllers |
| 6. Swagger do `ResponsavelController` | `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter` no `ResponsavelController`; `@Schema` em todos os DTOs |

API REST de tarefas e responsáveis usando Spring Boot, JPA, Hibernate,
PostgreSQL, Flyway e **springdoc-openapi (Swagger)**. Os comandos abaixo foram
escritos para o laboratório em máquinas Windows usando PowerShell e Docker
Desktop (no Linux/Mac, troque `.\mvnw.cmd` por `./mvnw`).

## Pré-requisitos

- Docker Desktop instalado e aberto.
- JDK 25 instalado e disponível no `PATH`.
- Git instalado para baixar o repositório.

O projeto já possui o Maven Wrapper (`mvnw.cmd`), portanto não é necessário
instalar o Maven separadamente. Na primeira execução ele baixa o springdoc
(e as outras dependências); é preciso estar com internet.

## Abrir a pasta do projeto

Abra o PowerShell e execute:

```powershell
cd "C:\caminho\do\repositorio\Aula 09\exemplo_tarefas_resolvido"
```

Substitua `C:\caminho\do\repositorio` pelo local em que o repositório foi
baixado.

## Subir o PostgreSQL

Na pasta que contém o arquivo `docker-compose.yml`, execute:

```powershell
docker compose up -d
docker compose ps
```

O serviço deve aparecer como `healthy`. O banco fica disponível com estes
dados:

| Configuração | Valor |
|---|---|
| Host | `localhost` |
| Porta | `5436` |
| Banco | `tarefas` |
| Usuário | `tarefas` |
| Senha | `tarefas` |

A porta `5436` e o nome de projeto `aula09-tarefas-resolvido` (primeira linha
do `docker-compose.yml`) separam este banco do banco dos alunos (`5435`) e dos
bancos da Aula 08 (`5433` e `5434`).

## Executar a aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

No startup, o Flyway aplica as migrations `V1`, `V2` e `V3` (as mesmas da Aula
08) e o springdoc monta o contrato OpenAPI a partir dos Controllers e DTOs.
Nenhuma migration nova nesta aula: DTO e mapper não mudam o banco.

## Abrir o Swagger

Com a aplicação em execução, abra no navegador:

| Endereço | O que é |
|---|---|
| <http://localhost:8080/swagger-ui.html> | **Swagger UI**: documentação interativa. Clique num endpoint → **Try it out** → **Execute** para enviar a requisição de verdade. |
| <http://localhost:8080/v3/api-docs> | O contrato **OpenAPI** em JSON (é o que o Swagger UI desenha). |

Para gerar uma collection do Postman a partir do contrato: **Import** → **Link**
→ `http://localhost:8080/v3/api-docs`.

As propriedades `springdoc.*` do `application.properties` estão comentadas
linha a linha. Todas são opcionais: sem elas, o Swagger funciona igual, só com
os padrões do springdoc.

## Páginas de teste

| Endereço | Para que serve |
|---|---|
| <http://localhost:8080/> | Cadastro de tarefas com o select **🔗** de vínculo, o teste de mass assignment (🧪) e o log de cada requisição (Controller → Service → **Mapper** → Repository → PostgreSQL) |
| <http://localhost:8080/responsaveis.html> | Responsáveis e vínculo; o painel **📜 Contrato** confere o formato da Aula 08 |
| <http://localhost:8080/contrato.html> | **Contrato da Aula 09**: testa a API contra cada exercício do `EXERCICIOS.md` (aqui, tudo ✅) |

## Testar pelo PowerShell

```powershell
Invoke-RestMethod http://localhost:8080/tarefas
```

Para criar uma tarefa (repare que `id` e `concluida` enviados no corpo são
ignorados, porque não existem no `TarefaRequestDTO`; acrescente
`"responsavelId": 1` para ela já nascer vinculada a um responsável cadastrado):

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/tarefas `
  -ContentType "application/json" `
  -Body '{"titulo":"Estudar DTO","responsavel":"Aluno","dataPrazo":"2026-12-01","prioridade":4,"id":999,"concluida":true}'
```

## Parar os serviços

Para parar somente a aplicação, volte ao terminal onde ela está rodando e
pressione `Ctrl+C`.

Para parar o PostgreSQL mantendo os dados:

```powershell
docker compose stop
```

Para parar e remover o container e a rede, mantendo o volume:

```powershell
docker compose down
```

## Recriar o banco do zero

Use este comando somente quando quiser apagar todos os dados locais e executar
as migrations novamente:

```powershell
docker compose down -v
docker compose up -d
.\mvnw.cmd spring-boot:run
```

O parâmetro `-v` remove o volume `postgres_data` deste projeto. Portanto,
tarefas e responsáveis criados manualmente serão apagados.
