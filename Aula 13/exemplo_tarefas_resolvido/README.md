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
./mvnw test        # Windows: .\mvnw.cmd test
```

44 testes: 34 unitários + 10 E2E. Sem precisar subir nada antes: os E2E criam os próprios containers.

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

Por que o Open-Meteo é mock no E2E? É um serviço de fora, lento e instável: um teste que depende dele falha "sem motivo". Já o banco e o broker são nossos, então ficam reais.

> **Armadilhas que apareceram ao escrever estes testes** (e estão comentadas no código):
> - Containers com `@Container` static são derrubados no fim de **cada classe**, mas o Spring reaproveita o contexto nas classes seguintes e fica apontando para containers mortos. Solução: container singleton (`static { ... .start(); }`).
> - Em `@SpringBootTest` a exportação de métricas vem **desligada**: o `/actuator/prometheus` dá `404`. Solução: `@AutoConfigureObservability`.
> - Os testes do `TarefaService` pegaram um bug real: concluir/reabrir uma tarefa não publicava o evento.

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
./mvnw spring-boot:run          # a API roda no host
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
| `api/observability/RequisicaoIdFilter.java` | `requisicaoId` no MDC de cada requisição |

> ⚠️ **Só para aula.** O Grafana está com acesso anônimo de administrador, e o `/actuator` está aberto. Em produção: autenticação, e os endpoints do Actuator fora da internet pública.

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
