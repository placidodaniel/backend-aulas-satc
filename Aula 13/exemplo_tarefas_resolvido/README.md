# Exemplo de tarefas: Aula 13, Observabilidade e Testes

Este projeto é o `exemplo_tarefas_resolvido` da Aula 12 (Open-Meteo + RabbitMQ) com duas camadas novas:

1. **Testes**: unitários (rápidos, sem infraestrutura) e E2E (a aplicação inteira, com PostgreSQL e RabbitMQ reais em containers).
2. **Observabilidade**: Actuator + Micrometer expõem métricas, o **Prometheus** coleta e o **Grafana** desenha, tudo no `docker-compose.yml`.

Portas deste projeto (diferentes das da Aula 12, para rodar lado a lado): PostgreSQL `5441`, RabbitMQ `5673` (painel `15673`), Prometheus `9090`, Grafana `3000`, API `8080`.

## Pré-requisitos

Java 25, Docker e Docker Compose. Os testes E2E também precisam do Docker rodando.

---

## Parte 1: Testes

```bash
docker compose --profile testes run --rm testes
```

45 testes: 31 unitários + 14 E2E. Sem precisar subir nada antes: os E2E criam os próprios containers.

### Sem script (computadores do laboratório)

Nas máquinas do laboratório (Windows, travadas) não dá para rodar `./mvnw` nem `mvnw.cmd`. Por isso o `docker-compose.yml` tem serviços que rodam o **Maven dentro de um container**, só com o `docker compose`:

| Para | Comando |
|---|---|
| **Todos os testes** (31 unitários + 14 E2E) | `docker compose --profile testes run --rm testes` |
| **Só os unitários** (31, não usam o Docker) | `docker compose --profile testes run --rm testes mvn -B -ntp test "-Dtest=!*E2ETest"` |
| **Subir a API** (com PostgreSQL e RabbitMQ) | `docker compose --profile app up -d api` |
| **Testes de tela** (interface do Playwright em `localhost:8081`) | `docker compose --profile ui up playwright` |
| Testes de tela, só o resultado no terminal | `docker compose --profile ui run --rm playwright-run` |
| Ver o log da API | `docker compose logs -f api` |
| Parar tudo | `docker compose --profile app --profile testes --profile ui down` |

- A **primeira** execução baixa o Maven e as dependências (cerca de 1 minuto); as seguintes usam o cache (volume `maven_repo`).
- Os testes E2E precisam do socket do Docker (`/var/run/docker.sock`, já montado no serviço `testes`) para o Testcontainers criar o PostgreSQL e o RabbitMQ.
- Na IDE (IntelliJ ou VS Code) também dá para rodar sem script: botão direito em `src/test/java` → *Run All Tests* (os E2E continuam precisando do Docker).
- Se o computador permitir, `./mvnw test` (Linux/Mac) ou `.\mvnw.cmd test` (Windows) continuam funcionando.

### A pirâmide de testes

| Tipo | O que sobe | Velocidade | Para que serve |
|---|---|---|---|
| **Unitário** | Só a classe testada; as dependências viram *mocks* | milissegundos | Regras de negócio, tradução, casos de borda |
| **E2E** (ponta a ponta) | A aplicação inteira numa porta aleatória + PostgreSQL + RabbitMQ reais | segundos | Provar que as peças funcionam **juntas**: HTTP, JSON, banco, migrations, mensageria |

Muitos unitários, poucos E2E: o unitário aponta *onde* quebrou, o E2E garante que *o conjunto* funciona.

### Testes unitários (`src/test/java/api/...`)

| Teste | O que mostra |
|---|---|
| `mapper/PrevisaoMapperTest` | Teste puro (`new` e assert), `@ParameterizedTest` para a tabela de códigos do tempo, dado de fora (`null`, código desconhecido) |
| `service/PrevisaoServiceTest` | Mockito: `OpenMeteoClient` é um dublê. Regras R1 a R3, cache (`verify(times(1))`), falha que não vai para o cache |
| `service/TarefaServiceTest` | Repositórios e publisher mockados: cria/conclui/reabre/remove publicam o evento certo; erro não publica nada |
| `messaging/TarefaEventoPublisherTest` | Exchange e routing key corretas; broker fora do ar **não** derruba o cadastro |
| `client/OpenMeteoClientTest` | `MockRestServiceServer`: confere a URL montada e transforma `500`/JSON quebrado em `PrevisaoIndisponivelException`, sem internet |

### Testes E2E (`src/test/java/api/e2e/`)

| Teste | O que mostra |
|---|---|
| `AbstractE2ETest` | `@SpringBootTest(RANDOM_PORT)`, containers **singleton** com `@ServiceConnection`, `@MockitoBean` só no Open-Meteo |
| `TarefaApiE2ETest` | CRUD real no PostgreSQL, `400` com lista de campos, `404`, e o **evento passando pelo RabbitMQ** (Awaitility espera o consumo assíncrono) |
| `PrevisaoApiE2ETest` | `200`, `400` (regra e formato), `502` e `504` com o Open-Meteo simulado |
| `ObservabilidadeE2ETest` | `/actuator/health` com banco e RabbitMQ, `/actuator/prometheus` com as nossas métricas, `X-Request-Id` |
| `CoberturaObservabilidadeE2ETest` | Chama **todas** as rotas e confere que cada uma tem métricas e que cada operação de tarefa é contada |

Por que o Open-Meteo é mock no E2E? É um serviço de fora, lento e instável: um teste que depende dele falha "sem motivo". Já o banco e o broker são nossos, então ficam reais.

> **Armadilhas que apareceram ao escrever estes testes** (e estão comentadas no código):
> - Containers com `@Container` static são derrubados no fim de **cada classe**, mas o Spring reaproveita o contexto nas classes seguintes e fica apontando para containers mortos. Solução: container singleton (`static { ... .start(); }`).
> - Em `@SpringBootTest` a exportação de métricas vem **desligada**: o `/actuator/prometheus` dá `404`. Solução: `@AutoConfigureObservability`.
> - Os testes do `TarefaService` pegaram um bug real: concluir/reabrir uma tarefa não publicava o evento.

### Testes de tela com Playwright (no navegador)

Os testes acima testam a **API**. O [Playwright](https://playwright.dev) testa a **tela**: abre um navegador de verdade (Chromium), digita, clica e confere o que aparece, como uma pessoa faria. Os testes ficam em [`playwright/tests/cadastro.spec.ts`](playwright/tests/cadastro.spec.ts) e cobrem o cadastro de tarefas (`index.html`):

| Teste | O que a tela faz |
|---|---|
| a página abre | O formulário de cadastro aparece e o botão está habilitado |
| cadastra uma tarefa | Preenche título, responsável, prazo e prioridade, clica em *Adicionar* e a tarefa aparece em "a fazer" |
| título vazio | A API recusa (400) e a tela mostra o erro |
| escolher a data do prazo | A caixa da previsão aparece, com o tempo de agora |
| concluir e reabrir | A tarefa passa para "concluídas" e volta |
| excluir | A tarefa some da lista |

A previsão do tempo é **simulada** (`page.route`): o teste não depende da internet nem do Open-Meteo.

**Para mostrar em aula (modo UI):** sem instalar nada, sem script. O Playwright roda num container e serve a interface dele numa página web:

```bash
docker compose --profile ui up playwright
```

Quando aparecer `Listening on http://0.0.0.0:8081`, abra **<http://localhost:8081>** no navegador. A primeira vez demora (baixa a imagem do Playwright e instala as dependências). Na página:

1. Clique em **▶** (*Run all*) para rodar todos, ou escolha um teste e rode só ele.
2. Clique em um passo da lista (*Fill*, *Click*, *Expect*...): à direita aparece **a tela do navegador naquele instante**, e embaixo o código da linha, a rede e o console.
3. A linha do tempo no topo mostra cada instante; passe o mouse para ver a tela.
4. Altere o teste e salve: o modo UI roda de novo sozinho (*watch*, o ícone do olho).

Só o resultado, sem interface (imprime no terminal):

```bash
docker compose --profile ui run --rm playwright-run
```

Para parar: `Ctrl+C` e `docker compose --profile ui down`. As dependências ficam em um volume, então só a primeira execução demora.

> **A localização é fixa (Criciúma/SC).** A página não pede mais a localização ao navegador (`navigator.geolocation`): isso exige a permissão da pessoa e um contexto seguro (`http://localhost` ou HTTPS), e **não funciona nos computadores do laboratório**. As coordenadas (`-28.6775, -49.3697`, a SATC) ficam na constante `LOCALIZACAO` do `index.html`; para usar outra cidade, troque esses dois números. O teste confere que o pedido sai com essas coordenadas.

> **O que o Playwright achou na página:** o formulário aparece antes de a lista inicial (`GET /tarefas`) terminar de carregar. Se uma tarefa é cadastrada nesse intervalo, a lista que chega já a contém e o `tarefas.push(criada)` do `index.html` a insere **uma segunda vez** (a mesma `#id` aparece duas vezes). Uma pessoa dificilmente cadastra tão rápido; um robô sim. O `beforeEach` do teste espera a lista carregar (`waitForResponse`) para não esbarrar nisso. A correção na página seria não inserir se o `id` já estiver em `tarefas`.

---

## Parte 2: Observabilidade

```text
API :8080 ──/actuator/prometheus──> Prometheus :9090 ──> Grafana :3000
```

**Observabilidade** é conseguir descobrir *o que está acontecendo dentro* do sistema olhando só para o que ele emite. Os três sinais clássicos:

| Sinal | Pergunta que responde | Neste projeto |
|---|---|---|
| **Métricas** | *Quanto? Com que frequência? Está piorando?* | Micrometer + Prometheus + Grafana |
| **Logs** | *O que aconteceu naquela requisição?* | Cada linha leva o `requisicaoId` (`RequisicaoIdFilter`, `X-Request-Id`) |
| **Traces** | *Por onde a requisição passou e onde demorou?* | Fora do escopo (próximo passo: OpenTelemetry + Tempo/Jaeger) |

### Subir

```bash
docker compose up -d            # PostgreSQL, RabbitMQ, Prometheus e Grafana
docker compose --profile app up -d api   # a API, em container (ou ./mvnw spring-boot:run, se o PC permitir)
```

| Endereço | O que é |
|---|---|
| <http://localhost:8080/actuator/health> | A aplicação está viva? Banco e RabbitMQ também? |
| <http://localhost:8080/actuator/prometheus> | Todas as métricas, em texto, como o Prometheus as lê |
| <http://localhost:9090> | Prometheus: *Status → Targets* deve mostrar `exemplo-tarefas` **UP**; aba *Graph* aceita PromQL |
| <http://localhost:3000> | Grafana (sem login): pasta **Aula 13**, dashboard **Exemplo Tarefas: visão geral** |
| <http://localhost:15673> | Painel do RabbitMQ (usuário/senha `tarefas`) |

### Métricas

Vêm de graça (Actuator): requisições HTTP (`http_server_requests_*`), JVM, pool do banco (`hikaricp_*`), RabbitMQ. E as **nossas**, que respondem perguntas do negócio:

| Métrica | Onde nasce | Pergunta |
|---|---|---|
| `tarefas_eventos_publicados_total{tipo,resultado}` | `TarefaEventoPublisher` | Quantos eventos saem? Quantos falharam? |
| `tarefas_eventos_consumidos_total{tipo}` | `TarefaEventoListener` | O consumo acompanha a publicação? |
| `openmeteo_chamadas_seconds{resultado}` | `OpenMeteoClient` | Quantas chamadas ao Open-Meteo, quanto demoram, quantas dão erro? |
| `previsao_cache_total{resultado}` | `PrevisaoService` | O cache está funcionando (hit x miss)? |

### Todas as rotas da API são observadas

Não há nada para registrar rota por rota: o Micrometer mede **toda** requisição HTTP (`http_server_requests_*`), com o **padrão da rota** como etiqueta (`/tarefas/{id}`, e não `/tarefas/7`). Uma rota nova aparece sozinha. Cobre os 3 controllers:

| Controller | Rotas |
|---|---|
| `/tarefas` | `GET /`, `POST /`, `GET /buscar`, `GET /atrasadas`, `GET /chuvosas`, `GET`, `PUT`, `PATCH` e `DELETE /{id}`, `PUT /{id}/concluir`, `PUT` e `DELETE /{id}/responsavel`, `GET /{id}/previsao` |
| `/responsaveis` | `GET /`, `POST /`, `GET /{id}`, `GET /{id}/tarefas` |
| `/previsao` | `GET /`, `GET /agora` |

Em cada sinal:

| Sinal | O que mostra, por rota |
|---|---|
| **Métricas HTTP** | Requisições, latência (p95) e erros por método + rota (painel **Todas as rotas da API**, uma linha por rota) |
| **Métrica de negócio** | `tarefas_operacoes_total{operacao}`: `criar`, `atualizar`, `atualizar_parcial`, `concluir`, `reabrir`, `vincular_responsavel`, `desvincular_responsavel`, `remover` |
| **Log** | Uma linha por requisição, em toda rota: `PUT /tarefas/{id}/concluir -> 200 (12 ms)`, com o `requisicaoId` |

O teste `CoberturaObservabilidadeE2ETest` **chama as 19 rotas** e falha se alguma não tiver série no `/actuator/prometheus` ou se alguma operação de tarefa não for contada.

### O dashboard

Já provisionado (`observabilidade/grafana/`), com os **quatro sinais de ouro**: tráfego, latência (p95), erros e saturação (heap e pool do banco), mais as nossas métricas (Open-Meteo, cache, RabbitMQ).

### Gerando movimento para ver os gráficos

```bash
# tráfego HTTP
for i in $(seq 1 50); do curl -s -o /dev/null localhost:8080/tarefas; done
# um erro 400 (aparece no painel de erros)
curl -s "localhost:8080/previsao?latitude=-28.6&longitude=-49.3&data=2999-01-01"
# carga na fila do RabbitMQ: 2 produtores x 300 msg/s por 30s, a API consome
# (PowerShell: $env:CARGA_TAXA=300; $env:CARGA_PRODUTORES=2)
CARGA_TAXA=300 CARGA_PRODUTORES=2 docker compose --profile carga run --rm perftest
```

Durante a carga, compare no painel *RabbitMQ: eventos publicados x consumidos*: se o consumido não acompanha o publicado, a fila está acumulando.

### Configuração

| Arquivo | O que faz |
|---|---|
| `application.properties` (seção Aula 13) | Quais endpoints do Actuator ficam expostos, etiqueta `application`, histograma para p95, formato do log |
| `observabilidade/prometheus/prometheus.yml` | De 5 em 5 s, pede `/actuator/prometheus` a `host.docker.internal:8080` |
| `observabilidade/grafana/provisioning/` | Datasource Prometheus e a pasta de dashboards, sem clicar em nada |
| `observabilidade/grafana/dashboards/exemplo-tarefas.json` | O dashboard |
| `api/observability/RequisicaoIdFilter.java` | `requisicaoId` no MDC e uma linha de log por requisição |

> ⚠️ **Só para aula.** O Grafana está com acesso anônimo de administrador, e o `/actuator` está aberto. Em produção: autenticação, e os endpoints do Actuator fora da internet pública.

---

## Teste de carga completo (alimenta todos os gráficos)

Um único comando liga **duas cargas ao mesmo tempo**: o k6 chama **todas as rotas HTTP** (`carga/http.js`) e o PerfTest enche a **fila do RabbitMQ**. Sem script, só `docker compose`. Antes: a API da Aula 13 rodando na `8080` e o Grafana aberto em *Last 5 minutes*.

```bash
docker compose --profile carga up --attach carga-http --attach perftest carga-http perftest
```

Só o HTTP: `docker compose --profile carga run --rm carga-http`. Só a fila: veja a seção seguinte.

| Variável | O que é | Padrão |
|---|---|---|
| `CARGA_VUS` | usuários virtuais do k6 (HTTP) | `5` |
| `CARGA_SEGUNDOS` | duração das duas cargas | `60` |
| `CARGA_TAXA` | msg/s por produtor (fila) | `200` |
| `CARGA_URL` | endereço da API | `http://host.docker.internal:8080` |

(PowerShell: `$env:CARGA_VUS=10; docker compose ...`.)

Cada iteração do k6 cria uma tarefa e percorre o ciclo inteiro: listar, buscar, atrasadas, alterar (PUT e PATCH), vincular e desvincular responsável, concluir, reabrir, previsão (Open-Meteo), responsáveis, `/v3/api-docs`, a página, **erros 400 e 404 de propósito** e, por fim, excluir. Resultado de uma execução de 60 s (5 VUs): ≈ 250 requisições/s, 100% dos checks passando, e todos os painéis com dados:

| Painel | O que alimenta a carga |
|---|---|
| Requisições por segundo, Latência p95, Todas as rotas | as 19 rotas da API |
| Erros (4xx e 5xx), Erros por rota | os 400 e 404 provocados |
| Chamadas ao Open-Meteo, Cache hit x miss | `/previsao`, `/previsao/agora`, `/tarefas/chuvosas`, `/tarefas/{id}/previsao` |
| RabbitMQ publicados x consumidos | criada, concluida, reaberta, removida (HTTP) e a carga do PerfTest |
| Operações de tarefa | criar, atualizar, concluir, reabrir, desvincular... |
| Memória e pool do banco | efeito da carga sobre a JVM e o Hikari |

> As chamadas de previsão usam o Open-Meteo de verdade (precisa de internet). Sem internet, elas viram erros 5xx, que também aparecem nos painéis de erro.

---

## Teste de carga na fila (herdado da Aula 12)

Sem script: é só o `docker compose`. Os parâmetros vão em variáveis de ambiente (todas opcionais):

| Variável | Padrão | O que é |
|---|---|---|
| `CARGA_TAXA` | `200` | Mensagens por segundo, **por produtor** (`0` = o mais rápido possível) |
| `CARGA_SEGUNDOS` | `30` | Duração do teste |
| `CARGA_PRODUTORES` | `1` | Quantos produtores publicando |
| `CARGA_CONSUMIDORES` | `0` | `0` = quem consome é a API (o listener) |

```powershell
# Windows (PowerShell): 2 produtores x 500 msg/s por 30 s
$env:CARGA_TAXA=500; $env:CARGA_SEGUNDOS=30; $env:CARGA_PRODUTORES=2
docker compose --profile carga run --rm perftest
```

```bash
# Linux / Mac
CARGA_TAXA=500 CARGA_SEGUNDOS=30 CARGA_PRODUTORES=2 docker compose --profile carga run --rm perftest
```

A exchange, a fila e o binding já são criados quando o RabbitMQ sobe (`carga/definitions.json`), então o teste funciona com a API desligada. Para ver quantas mensagens sobraram: painel do RabbitMQ (aba *Queues*) ou `docker compose exec rabbitmq rabbitmqctl list_queues name messages consumers`.

Detalhes no README da Aula 12. Aqui ele ganha sentido novo: você **vê** a carga nos gráficos.

## Parar e recriar

```bash
docker compose down        # mantém os dados (volumes)
docker compose down -v     # apaga também o banco, as métricas e o Grafana
```
