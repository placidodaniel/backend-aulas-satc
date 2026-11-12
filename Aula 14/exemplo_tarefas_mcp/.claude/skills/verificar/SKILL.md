---
name: verificar
description: Verifica de verdade uma mudança neste projeto. Roda os testes, sobe a API e confere a rota REST e o servidor MCP. Use antes de dizer que uma tarefa está pronta, ou quando o usuário pedir para "verificar", "testar tudo" ou "conferir se funciona".
---

# Verificar uma mudança

Objetivo: provar que a mudança funciona, e não só que compila.

## Passos

1. **Testes automatizados.** Rode `./mvnw -B -ntp test`.
   - Se algum falhar, pare, mostre o nome do teste e a mensagem, e corrija antes de seguir.
2. **Infraestrutura.** Rode `docker compose up -d postgres rabbitmq` e espere os dois ficarem `healthy`
   (`docker compose ps`).
3. **API.** Se nada responde em `http://localhost:8080/actuator/health`, suba com
   `./mvnw -q spring-boot:run` em segundo plano e espere o `"status":"UP"`.
4. **REST.** Chame a rota que mudou com `curl` e mostre o status HTTP e o corpo.
5. **MCP.** Faça o aperto de mão e liste as ferramentas:
   ```bash
   curl -s -D - -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream' \
     http://localhost:8080/mcp \
     -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"verificar","version":"1"}}}'
   ```
   Pegue o `Mcp-Session-Id` da resposta e mande `tools/list` com ele. Se a mudança for numa
   ferramenta, chame-a com `tools/call` e mostre o resultado (inclusive um caso de erro).
6. **Relatório.** Responda em uma tabela curta: o que foi verificado, como, e o resultado.
   Se algum passo não pôde ser feito, diga qual e por quê. Não diga "funciona" sem ter visto funcionar.
