---
name: nova-ferramenta-mcp
description: Cria uma nova ferramenta MCP no servidor de tarefas seguindo o padrão do projeto (Service, TarefaMcpTools, testes e documentação). Use quando o usuário pedir uma nova ferramenta, tool ou ação para a IA usar no sistema de tarefas.
---

# Nova ferramenta MCP

Objetivo: adicionar uma ferramenta que a IA possa chamar, sem colocar regra de negócio
fora do Service e sem esquecer teste e documentação.

## Antes de escrever código
- Pergunte (ou deduza do pedido): o nome da ferramenta, o que ela recebe, o que devolve,
  e se ela **só lê** ou **altera** dados.
- Procure em `TarefaService` um método que já faz o trabalho. Só crie método novo no Service se não houver.

## Passos
1. **Service** (`api/service/TarefaService.java`): a regra fica aqui. Devolva DTO, nunca a entidade.
2. **Ferramenta** (`api/mcp/TarefaMcpTools.java`): um método com `@McpTool`.
   - `name` em snake_case, em português (`listar_atrasadas`).
   - `description` escrita para a IA: o que faz, quando usar, formato de cada campo.
   - Cada parâmetro com `@McpToolParam(description = ..., required = ...)`.
   - `annotations`: `readOnlyHint` (só lê?), `destructiveHint` (apaga ou sobrescreve?),
     `idempotentHint` (chamar duas vezes dá no mesmo?), `openWorldHint = false`.
   - Se receber dados de cadastro, valide com o `Validator` (método `validar`).
3. **Teste unitário** em `src/test/java/api/mcp/TarefaMcpToolsTest.java`: caminho feliz e um erro.
4. **Teste E2E** em `src/test/java/api/e2e/McpE2ETest.java`: atualize a lista esperada em
   `listaFerramentas` e chame a ferramenta nova com o cliente MCP.
5. **Documentação**: acrescente a ferramenta na tabela "Ferramentas" do `README.md`.
6. Rode a skill `verificar`.

## Não fazer
- Não acessar o `TarefaRepository` direto da ferramenta.
- Não criar ferramenta que apaga dados sem `destructiveHint = true` e sem o usuário ter pedido.
