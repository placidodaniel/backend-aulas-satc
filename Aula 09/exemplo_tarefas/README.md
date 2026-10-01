# Exemplo de tarefas — Aula 09 (DTO, Mapeamento e Swagger)

API REST de tarefas e responsáveis usando Spring Boot, JPA, Hibernate,
PostgreSQL, Flyway e **springdoc-openapi (Swagger)**. Os comandos abaixo foram
escritos para o laboratório em máquinas Windows usando PowerShell e Docker
Desktop (no Linux/Mac, troque `.\mvnw.cmd` por `./mvnw`).

Este projeto é o gabarito da Aula 08 com quatro mudanças:

| Mudança | Arquivos |
|---|---|
| Entrada e saída de tarefa viraram **records** separados: `TarefaRequestDTO` e `TarefaResponseDTO`. O `TarefaController` não conhece mais a entidade `Tarefa`. | `src/main/java/api/dto` |
| A conversão DTO ↔ entidade saiu do Service e foi para o **`TarefaMapper`**. | `src/main/java/api/mapper` |
| **Vínculo já no cadastro:** `responsavelId` opcional no `TarefaRequestDTO`. Entra um id, sai o objeto `responsavelVinculado`; o `TarefaService` transforma o id em entidade (404 se não existir). | `TarefaRequestDTO`, `TarefaService`, `TarefaMapper` |
| **Swagger**: dependência `springdoc-openapi-starter-webmvc-ui`, cabeçalho do documento em `OpenApiConfig` e anotações no `TarefaController`. | `pom.xml`, `src/main/java/api/config`, `application.properties` |

O lado de responsáveis ficou no formato da Aula 08 de propósito: é o ponto de
partida do [`EXERCICIOS.md`](EXERCICIOS.md). As páginas mostram, numa barra no
topo, quais exercícios já aparecem no contrato OpenAPI (`/v3/api-docs`) e mudam
de comportamento conforme você implementa.

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
cd "C:\caminho\do\repositorio\Aula 09\exemplo_tarefas"
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
| Porta | `5435` |
| Banco | `tarefas` |
| Usuário | `tarefas` |
| Senha | `tarefas` |

A porta `5435` e o nome de projeto `aula09-tarefas` (primeira linha do
`docker-compose.yml`) separam este banco dos bancos da Aula 08 (portas `5433`
e `5434`). Sem o `name:`, o Docker usaria o nome da pasta (`exemplo_tarefas`),
o mesmo da Aula 08, e as duas aulas dividiriam o mesmo volume de dados.

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
| <http://localhost:8080/contrato.html> | **Contrato da Aula 09**: testa a API contra cada exercício do `EXERCICIOS.md` |

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
