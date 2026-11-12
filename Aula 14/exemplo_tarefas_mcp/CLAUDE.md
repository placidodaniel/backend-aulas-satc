# Exemplo Tarefas (Aula 14)

API de tarefas da disciplina de Backend (SATC). Este arquivo é lido pelo Claude Code
no início de toda sessão: é o "manual do projeto" para o assistente.

## Stack
- Java 25, Spring Boot 3.5, Spring Data JPA, Flyway, PostgreSQL 16, RabbitMQ.
- Spring AI 1.1 (servidor MCP em `POST /mcp`, classes em `src/main/java/api/mcp`).
- Assistente da tela (`api/assistente`, `assistente.html`): host MCP que usa um modelo no formato
  chat completions. Padrão: Ollama local (`docker compose --profile ia up -d`), sem chave.
- Testes: JUnit 5, Mockito, AssertJ, Testcontainers. Tela: Playwright (pasta `playwright/`).

## Como rodar
- Infraestrutura: `docker compose up -d` (PostgreSQL em 5443, RabbitMQ em 5674).
- API: `./mvnw spring-boot:run` (porta 8080) ou `docker compose --profile app up -d`.
- Testes: `./mvnw test` (precisa do Docker, por causa do Testcontainers).

## Arquitetura (não quebrar)
- Controller (REST) e TarefaMcpTools (MCP) são PORTAS DE ENTRADA: não têm regra de negócio,
  só traduzem e chamam o Service.
- A entidade `Tarefa` não sai do Service: para fora, sempre DTO (`api.dto`).
- Toda porta de entrada valida os dados (no REST, `@Valid`; no MCP, `Validator`).
- Chave de IA só em variável de ambiente ou `.env` (fora do Git). Nunca no código nem no properties.
- Mudança no banco = nova migration Flyway (`V4__...sql`). Nunca editar uma migration antiga.

## Convenções
- Código, nomes e comentários em português; comentários didáticos, explicando o porquê.
- Ferramentas MCP: nome em snake_case, descrição escrita para a IA (o que faz, quando usar,
  formato dos campos) e `annotations` corretas (readOnlyHint, destructiveHint, idempotentHint).
- Cada regra nova ganha um teste unitário; cada rota ou ferramenta nova, um E2E.

## Antes de dizer "pronto"
- `./mvnw test` passando (hoje: 66 testes).
- Se mexeu no MCP: abrir `http://localhost:8080/mcp.html`, conectar e chamar a ferramenta.
