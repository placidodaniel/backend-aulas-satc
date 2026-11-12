# Exemplo de tarefas: Aula 14, MCP e IA no desenvolvimento

## Objetivo

Fazer um **assistente de IA usar o nosso sistema de tarefas**: cadastrar, listar, concluir e ver estatísticas conversando em português, sem a gente escrever uma tela nova.

Para isso, a API ganha uma segunda porta de entrada, ao lado das rotas REST: um **servidor MCP** (*Model Context Protocol*, o "USB das IAs"). O assistente se conecta em `http://localhost:8080/mcp`, descobre as ferramentas que existem e decide sozinho qual chamar.

```text
Navegador / Postman ──HTTP/JSON──> TarefaController ──┐
                                                       ├──> TarefaService ──> PostgreSQL
Assistente de IA    ──MCP (/mcp)─> TarefaMcpTools  ────┘                  └─> RabbitMQ
```

O projeto é o gabarito da Aula 13 (Open-Meteo, RabbitMQ, testes e observabilidade) com o MCP por cima. **Nenhuma regra de negócio foi duplicada**: as ferramentas chamam o mesmo `TarefaService` que o REST.

Portas deste projeto (diferentes das da Aula 13, para rodar lado a lado): PostgreSQL `5443`, RabbitMQ `5674` (painel `15674`), API e MCP `8080`, Ollama `11434`.

---

## 1. Subir e ver funcionando (só com Docker)

Funciona nos computadores do laboratório: não precisa de Java, Maven nem de assistente de IA.

```bash
cd "Aula 14/exemplo_tarefas_mcp"
docker compose --profile app up -d       # banco, RabbitMQ, Prometheus, Grafana e a API
docker compose --profile app ps          # espere a api ficar "healthy" (1 a 2 minutos na 1ª vez)
```

Abra **`http://localhost:8080/mcp.html`**, o *cliente MCP de bolso*. Ele faz, botão por botão, o que um assistente de IA faz:

1. **Conectar**: o aperto de mão (`initialize`). O servidor diz o nome, o que sabe fazer e devolve um `Mcp-Session-Id`.
2. **Listar ferramentas** (`tools/list`): aparecem as 5 ferramentas, com a descrição e um formulário gerado do JSON Schema de cada uma. Preencha e clique em **Chamar** (`tools/call`).
3. **Ler um recurso** (`resources/read`): as regras de negócio, em `tarefas://regras`.
4. **Usar um prompt pronto** (`prompts/get`): `planejar_semana`.

O painel da direita mostra cada mensagem JSON-RPC que foi e voltou. Tente cadastrar com prioridade `9` ou com a data `01/12/2026`: a resposta vem com `"isError": true` e uma mensagem que a IA usaria para se corrigir.

Depois, abra `http://localhost:8080/` (o cadastro de tarefas da Aula 12): o que você cadastrou pelo MCP está lá. É o mesmo banco.

---

## 2. Conectar um assistente de IA de verdade

O servidor é o mesmo para qualquer cliente que fale MCP. O projeto já traz a configuração dos dois mais comuns:

| Cliente | Arquivo no projeto | O que fazer |
|---|---|---|
| **Claude Code** (terminal ou extensão do VS Code) | [`.mcp.json`](.mcp.json) | Abra o Claude Code nesta pasta e aprove os servidores `tarefas` e `context7`. Confira com `/mcp`. |
| **GitHub Copilot** no VS Code (modo *Agent*) | [`.vscode/mcp.json`](.vscode/mcp.json) | Abra a pasta no VS Code, abra o arquivo e clique em **Start** acima de `tarefas`. |
| Outro cliente (Cursor, Gemini CLI, Postman...) | Nenhum | Cadastre um servidor do tipo **HTTP** com a URL `http://localhost:8080/mcp`. |

Também dá para cadastrar pelo terminal do Claude Code, sem o arquivo:

```bash
claude mcp add --transport http tarefas http://localhost:8080/mcp
```

Peça em português e veja quais ferramentas ele escolhe:

- "Cadastra para a Ana: revisar o README do projeto, prazo sexta que vem, prioridade alta."
- "Quem está com mais tarefas abertas? Tem alguma atrasada?"
- "Conclui a tarefa 3 e me mostra como ficou o percentual concluído."
- `/mcp__tarefas__planejar_semana` com `responsavel = Ana` (o prompt pronto aparece como comando).

> ⚠️ A rota `/mcp` não tem autenticação: quem alcança a porta 8080 chama qualquer ferramenta. Serve para aula, em `localhost`. Em produção, coloque Spring Security (ou OAuth, que o MCP suporta) na frente.

### O Context7, um MCP de documentação

O `.mcp.json` também liga o **Context7** (`https://mcp.context7.com/mcp`): ele entrega ao assistente a documentação **atual** de bibliotecas (Spring Boot, Spring AI, Hibernate...), em vez do que o modelo decorou no treino. Peça, por exemplo: *"use o context7 e me mostre como declarar um @McpResource no Spring AI 1.1"*. Sem chave funciona com limite de uso; com conta gratuita, o limite aumenta.

---

## 2b. O assistente na tela de tarefas (sem chave)

Em vez de um assistente externo, a **própria aplicação** pode fazer o papel de host: você digita o pedido numa tela, um modelo de IA escolhe as ferramentas e a API executa pelo MCP.

```text
assistente.html ──POST /assistente──> AssistenteService (host)
                                         ├──> modelo de IA: "o que fazer agora?"   (Ollama, local, sem chave)
                                         └──> cliente MCP ──POST /mcp──> TarefaMcpTools ──> TarefaService
```

O modelo roda **na sua máquina**, num container do **Ollama**: sem conta, sem chave e, depois do primeiro download, sem internet.

```bash
docker compose --profile ia up -d                  # Ollama + baixa o modelo qwen2.5:3b (~2 GB, só na 1ª vez)
docker compose --profile ia logs -f ollama-modelo  # acompanhe o download; termina com "success"
docker compose --profile app --profile ia up -d    # ou: tudo junto, inclusive a API
```

Abra **`http://localhost:8080/assistente.html`** e peça, por exemplo, *"Como está o andamento das tarefas?"* ou *"Cadastre Estudar MCP para a Ana, prazo sexta que vem, prioridade 4"*. À direita aparecem as ferramentas MCP que o modelo chamou, com os argumentos e o resultado.

- **Só leitura**: o modelo só recebe as ferramentas com `readOnlyHint = true`. É a dica das `annotations` virando regra no host.
- **Modelo**: dá para trocar na tela. Precisa ser um modelo que aceita ferramentas (`qwen2.5`, `qwen3`, `llama3.1`, `llama3.2`...). Para baixar outro: `docker compose exec ollama ollama pull qwen3:4b`.
- **Tempo**: no processador, um modelo de 3B leva de 10 a 60 segundos por resposta. Modelos pequenos também erram mais: compare as respostas e veja no painel da direita o que ele chamou.

**Quer um modelo maior, na nuvem?** O mesmo código fala com o **OpenRouter**, que precisa de chave (gratuita em [openrouter.ai/keys](https://openrouter.ai/keys)). Copie o [`.env.exemplo`](.env.exemplo) para `.env` (que não vai para o Git), preencha `ASSISTENTE_URL`, `ASSISTENTE_CHAVE` e `ASSISTENTE_MODELO` e recrie a API. Rodando pela IDE, use as mesmas variáveis na configuração de execução.

| Arquivo | O que tem |
|---|---|
| [`assistente/AssistenteService.java`](src/main/java/api/assistente/AssistenteService.java) | O **host**: lista as ferramentas pelo MCP, chama o modelo, executa o que ele pedir e repete até vir texto |
| [`assistente/ModeloClient.java`](src/main/java/api/assistente/ModeloClient.java) | O `RestClient` do servidor de modelos (Ollama ou OpenRouter), no mesmo padrão do `OpenMeteoClient` |
| [`assistente/ChatCompletions.java`](src/main/java/api/assistente/ChatCompletions.java) | O formato JSON "chat completions", que os dois servidores entendem |
| [`assistente/AssistenteController.java`](src/main/java/api/assistente/AssistenteController.java) | `POST /assistente` e `GET /assistente/status` |
| [`static/assistente.html`](src/main/resources/static/assistente.html) | A tela |
| [`test/.../e2e/AssistenteE2ETest.java`](src/test/java/api/e2e/AssistenteE2ETest.java) | 5 testes E2E: só o modelo é falso; MCP, banco e RabbitMQ são reais |

---

## 3. Onde está o código novo

| Arquivo | O que tem |
|---|---|
| [`pom.xml`](pom.xml) | O BOM do Spring AI 1.1.8 e o `spring-ai-starter-mcp-server-webmvc` |
| [`application.properties`](src/main/resources/application.properties) | Nome do servidor, transporte **Streamable HTTP** em `/mcp` e as instruções para a IA |
| [`mcp/TarefaMcpTools.java`](src/main/java/api/mcp/TarefaMcpTools.java) | As 5 **ferramentas** (`@McpTool`). É um "Controller para a IA" |
| [`mcp/TarefaMcpContexto.java`](src/main/java/api/mcp/TarefaMcpContexto.java) | O **recurso** `tarefas://regras` e o **prompt** `planejar_semana` |
| [`service/TarefaService.java`](src/main/java/api/service/TarefaService.java) | O método novo `estatisticas()` |
| [`controller/TarefaController.java`](src/main/java/api/controller/TarefaController.java) | A rota nova `GET /tarefas/estatisticas` (mesmo cálculo da ferramenta) |
| [`static/mcp.html`](src/main/resources/static/mcp.html) | O cliente MCP de bolso |
| [`test/.../mcp/TarefaMcpToolsTest.java`](src/test/java/api/mcp/TarefaMcpToolsTest.java) | 8 testes unitários das ferramentas |
| [`test/.../e2e/McpE2ETest.java`](src/test/java/api/e2e/McpE2ETest.java) | 6 testes E2E com o **cliente MCP oficial do SDK Java** contra a API real |

### Ferramentas

| Ferramenta | Faz | Só lê? |
|---|---|---|
| `listar_tarefas` | Lista, com filtro de `situacao` (todas, abertas, concluidas, atrasadas) e de `responsavel` | Sim |
| `buscar_tarefa` | Uma tarefa pelo `id` | Sim |
| `cadastrar_tarefa` | Cria (`titulo`, `responsavel`, `dataPrazo` yyyy-MM-dd, `prioridade` 1 a 5) | Não |
| `concluir_tarefa` | Conclui; se já estiver concluída, não reabre (idempotente) | Não |
| `estatisticas_tarefas` | Total, abertas, concluídas, atrasadas, % concluído, abertas por responsável e por prioridade, próximo prazo | Sim |

### Três decisões que valem para qualquer servidor MCP

1. **A descrição é para a IA.** O modelo escolhe a ferramenta lendo `description`. Diga o que faz, quando usar e o formato de cada campo (`yyyy-MM-dd`, `1 a 5`).
2. **Toda porta de entrada valida.** No REST, quem valida é o `@Valid`; na ferramenta, chamamos o `Validator` na mão. A IA também erra (data em outro formato, prioridade 9).
3. **Erro é mensagem, não exceção perdida.** Uma exceção na ferramenta volta como `isError: true` com o texto. Mensagem clara = a IA corrige sozinha e tenta de novo.

E uma de projeto: o `PUT /tarefas/{id}/concluir` do REST **alterna** (conclui ou reabre). Para a IA, isso seria perigoso: "concluir" duas vezes reabriria a tarefa. Por isso `concluir_tarefa` só conclui.

---

## 4. IA no desenvolvimento: o que já está no projeto

Além de ser **usado** por uma IA, o projeto mostra como **trabalhar com** uma IA:

| Arquivo | Para quê |
|---|---|
| [`CLAUDE.md`](CLAUDE.md) | O "manual do projeto" que o Claude Code lê no início de toda sessão: stack, como rodar, regras de arquitetura, convenções e o que conferir antes de dizer "pronto" |
| [`.claude/skills/verificar/SKILL.md`](.claude/skills/verificar/SKILL.md) | Skill de projeto: roda os testes, sobe a API e confere REST e MCP de verdade |
| [`.claude/skills/nova-ferramenta-mcp/SKILL.md`](.claude/skills/nova-ferramenta-mcp/SKILL.md) | Skill de projeto: o passo a passo para criar uma ferramenta MCP no padrão do projeto |
| [`.claude/settings.json`](.claude/settings.json) | Permissões: o que o assistente pode rodar sem perguntar (testes, `docker compose ps`, ferramentas só de leitura) |

No Claude Code, as skills aparecem como `/verificar` e `/nova-ferramenta-mcp`, e também são usadas sozinhas quando o pedido combina com a descrição.

---

## 5. Testes

```bash
docker compose --profile testes run --rm testes     # sem Java instalado (laboratório)
./mvnw test                                           # com Java 25 instalado
```

**66 testes**: os 45 da Aula 13 + 2 das estatísticas + 8 unitários das ferramentas + 6 E2E do MCP + 5 E2E do assistente. Os E2E sobem PostgreSQL e RabbitMQ em containers (Testcontainers), então o Docker precisa estar rodando.

O resto (Playwright, Prometheus, Grafana, carga) continua igual ao da Aula 13: veja o [README da Aula 13](README-aula13.md).

---

## 6. Exercícios

1. **Ferramenta `reabrir_tarefa`.** Use a skill `nova-ferramenta-mcp` (ou siga os passos dela à mão). Ela só reabre: se a tarefa já estiver aberta, não muda nada.
2. **Ferramenta `vincular_responsavel`.** Recebe `tarefaId` e `responsavelId` e usa o `vincularResponsavel` que já existe no Service. Teste pelo `mcp.html` com um id que não existe: a mensagem de erro ajuda a IA?
3. **Uma descrição ruim.** Troque a descrição de `listar_tarefas` por `"lista"` e peça ao assistente "quais tarefas da Ana estão atrasadas?". Compare quais ferramentas ele chama antes e depois.
4. **`CLAUDE.md` do Projeto Final.** Escreva o `CLAUDE.md` do projeto do seu grupo: stack, como rodar, as regras de negócio e o que não pode ser quebrado.

## Parar e recriar

```bash
docker compose --profile app --profile ia down        # para tudo (os dados e o modelo ficam nos volumes)
docker compose --profile app --profile ia down -v     # para e APAGA os dados (e o modelo baixado)
```

Se a `api` sair logo depois de subir (por exemplo, porque a porta 8080 estava ocupada na primeira tentativa), libere a porta e recrie só o container dela:

```bash
docker compose --profile app up -d --force-recreate api
```
