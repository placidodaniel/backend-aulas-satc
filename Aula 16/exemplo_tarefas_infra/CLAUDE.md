# Exemplo Tarefas (Aula 16)

API de tarefas da disciplina de Backend (SATC). Este arquivo é lido pelo Claude Code
no início de toda sessão: é o "manual do projeto" para o assistente.

## Stack
- Java 25, Spring Boot 3.5, Spring Data JPA, Flyway, PostgreSQL 16, RabbitMQ.
- Spring AI 1.1 (servidor MCP em `POST /mcp`, classes em `src/main/java/api/mcp`).
- Assistente da tela (`api/assistente`, `assistente.html`): host MCP que usa um modelo no formato
  chat completions. Padrão: Ollama local (`docker compose --profile ia up -d`), sem chave.
- Spring Security (Aula 15): login próprio com JWT (HS256), 2FA por aplicativo (TOTP), BCrypt.
  Configuração em `api/seguranca/SegurancaConfig.java`; regras em `api/service/AutenticacaoService.java`.
- Testes: JUnit 5, Mockito, AssertJ, Testcontainers. Tela: Playwright (pasta `playwright/`).

## Como rodar
- Sistema inteiro: `docker compose up -d` (imagem da API pelo `Dockerfile`, proxy Caddy em 8080/8443,
  PostgreSQL em 5445, RabbitMQ em 5676). Várias cópias: `docker compose up -d --scale api=3`.
- Pela IDE: `docker compose up -d postgres rabbitmq` e `./mvnw spring-boot:run` (pare o `proxy`, que usa a 8080).
- Testes: `./mvnw test` (precisa do Docker, por causa do Testcontainers).

## Infraestrutura (Aula 16, não quebrar)
- A API não publica porta: só o `proxy` (infra/caddy/Caddyfile). Rota que não deve ser vista de fora se fecha no proxy.
- Nada de estado só na memória de uma cópia (a API roda em várias): o que precisa ser igual entre
  cópias vai para o banco ou para variável de ambiente. Exceção conhecida: sessões do `/mcp` (presas por IP no proxy).
- Configuração por variável de ambiente; o perfil `producao` (application-producao.properties) exige `JWT_SEGREDO`.
- O `HEALTHCHECK` do Dockerfile usa `/actuator/health/readiness`: precisa continuar público e sem token.
- Imagens base sempre com versão fixa. Se mudar o nome do `.jar` (artifactId/version), ajuste o Dockerfile.

## Arquitetura (não quebrar)
- Controller (REST) e TarefaMcpTools (MCP) são PORTAS DE ENTRADA: não têm regra de negócio,
  só traduzem e chamam o Service.
- A entidade `Tarefa` não sai do Service: para fora, sempre DTO (`api.dto`).
- Toda porta de entrada valida os dados (no REST, `@Valid`; no MCP, `Validator`).
- Chave de IA só em variável de ambiente ou `.env` (fora do Git). Nunca no código nem no properties.
- Mudança no banco = nova migration Flyway (`V5__...sql`). Nunca editar uma migration antiga.

## Segurança (Aula 15, não quebrar)
- Negar por padrão: toda rota nova já exige token. Só abra rota pública em `SegurancaConfig`, com motivo.
- "Quem é o usuário" vem do token (`@AuthenticationPrincipal Jwt`), nunca de um campo do JSON.
- Senha só como hash BCrypt. Nunca logar senha, token, segredo do 2FA nem e-mail completo.
- Segredos (`JWT_SEGREDO`, senhas, chave de IA) só em variável de ambiente ou `.env`.
- Nas páginas: dado de fora entra com `textContent`, nunca com `innerHTML`.
- Dependência nova: versão fixa, conferir se é a biblioteca certa (nome, grupo, popularidade) e
  regravar os checksums (veja `CADEIA-DE-SUPRIMENTOS.md`). Sem isso o build do Docker falha, de propósito.
- O `.mvn/maven.config` precisa continuar com `failIfMissing=false`: o IntelliJ não resolve `${session.rootDirectory}`
  e, com `true`, a importação do Maven quebra ("JVM target 5"). O modo rígido fica no `MAVEN_ARGS` do Compose.

## Convenções
- Código, nomes e comentários em português; comentários didáticos, explicando o porquê.
- Ferramentas MCP: nome em snake_case, descrição escrita para a IA (o que faz, quando usar,
  formato dos campos) e `annotations` corretas (readOnlyHint, destructiveHint, idempotentHint).
- Cada regra nova ganha um teste unitário; cada rota ou ferramenta nova, um E2E.

## Antes de dizer "pronto"
- `./mvnw test` passando (hoje: 93 testes). Tela: `docker compose --profile ui run --rm playwright-run` (10 testes).
- Se mexeu no MCP: abrir `http://localhost:8080/mcp.html`, conectar e chamar a ferramenta.
- Se mexeu em Dockerfile, Compose ou Caddyfile: `docker compose up -d --build --wait --scale api=2` e o teste de
  fumaça do `.github/workflows/aula16-ci.yml`.
