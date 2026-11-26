# Exemplo de tarefas: Aula 16, Infraestrutura

## Objetivo

Até a Aula 15, o sistema de tarefas rodava "do jeito de quem desenvolve": um container com Maven compilava o código e rodava `mvn spring-boot:run`, a API atendia direto na porta 8080 e, se ela caísse, ficava caída.

Nesta aula, o sistema fica pronto para **ir para um servidor**:

1. a API ganha uma **imagem própria** ([Dockerfile](Dockerfile)): pequena, sem Maven nem código-fonte, rodando com um usuário sem poderes;
2. o sistema **inteiro sobe com um comando**: `docker compose up -d`;
3. um **proxy reverso** (Caddy) vira a única porta de entrada, com **HTTPS**, e distribui as requisições entre **várias cópias** da API;
4. cada serviço **volta sozinho** se cair, tem **limite de memória** e desliga **com calma**;
5. o banco ganha **backup e restauração** com um comando;
6. a mesma imagem roda em **aula** ou em **produção**: só muda a configuração;
7. o **CI** do GitHub constrói a imagem e testa o sistema subindo de verdade.

```text
                                  ┌──────────── rede interna do Docker ─────────────┐
navegador ──http://localhost:8080─┤                                                 │
          ──https://localhost:8443┤──> proxy (Caddy) ──rodízio──> api #1 ─┐         │
                                  │                          ├──> api #2 ─┼─> postgres
                                  │                          └──> api #3 ─┘   rabbitmq
                                  │    prometheus ──coleta de cada cópia──┘         │
                                  └─────────────────────────────────────────────────┘
```

Portas deste projeto: proxy `8080` (HTTP) e `8443` (HTTPS), PostgreSQL `5445`, RabbitMQ `5676` (painel `15676`), Prometheus `9090`, Grafana `3000`. A API **não publica porta nenhuma**: só o proxy fala com ela.

---

## 1. Subir o sistema inteiro

```bash
cd "Aula 16/exemplo_tarefas_infra"
docker compose up -d          # a 1ª vez constrói a imagem da API (1 a 3 minutos)
docker compose ps             # espere api e proxy ficarem "healthy"
```

Abra **`http://localhost:8080`**, crie a sua conta e entre, como na Aula 15. Repare no menu, à direita: um código como `🖥️ 0a24346625e6`. É o nome da **cópia da API** que respondeu (o id do container).

Agora o **HTTPS**: abra **`https://localhost:8443`**. O navegador avisa que a conexão "não é particular": o certificado foi assinado por uma autoridade que só existe dentro do Caddy desta máquina. Clique em **Avançado** e **Continuar**. A conexão é cifrada de verdade; o aviso é só porque o navegador não conhece quem assinou.

> Pare antes qualquer outra coisa na porta 8080 (a API da Aula 15, a IDE). Só um programa pode usar cada porta.

---

## 2. A imagem da API (Dockerfile)

O [Dockerfile](Dockerfile) é a receita da imagem, em **duas etapas**:

| Etapa | Imagem base | O que faz | Vai para a imagem final? |
|---|---|---|---|
| `construcao` | `maven:3.9.12-eclipse-temurin-25` (480 MB) | Compila e gera o `.jar` (checksums no modo rígido da Aula 15) | Não |
| final | `eclipse-temurin:25.0.5.1_2-jre-noble` (327 MB) | Só o Java para rodar + o `.jar` aberto em camadas | Sim (413 MB no total) |

Os cuidados, um por linha da receita:

- **Versões fixas** nas imagens base: o build de hoje e o de daqui a um ano usam a mesma base.
- **Camadas na ordem certa**: as bibliotecas (79 MB, quase nunca mudam) entram antes do nosso código (menos de 1 MB, muda a cada commit). Mudou só o código? O Docker reaproveita o resto e o servidor baixa só a camada nova. Veja: `docker history tarefas-api:aula16`.
- **Usuário sem poderes** (`USER tarefas`): se alguém achar uma falha na API, não ganha o `root` do container. Confira: `docker compose exec api id`.
- **`.dockerignore`**: `.env`, `.git` e `target/` nunca entram na imagem.
- **`HEALTHCHECK`**: o Docker pergunta à API se ela está pronta (`/actuator/health/readiness`).
- **`ENTRYPOINT` em forma de lista**: o Java recebe direto o aviso de "desligar" e termina as requisições em andamento.

```bash
docker compose build api                 # reconstrói a imagem
docker image ls tarefas-api              # o tamanho
docker history tarefas-api:aula16        # as camadas, uma por linha do Dockerfile
```

---

## 3. Várias cópias atrás do proxy

```bash
docker compose up -d --scale api=3       # 3 cópias da API
docker compose ps api                    # api-1, api-2, api-3
```

O proxy ([infra/caddy/Caddyfile](infra/caddy/Caddyfile)) pergunta ao DNS do Docker quais são os IPs do serviço `api` e reparte as requisições em **rodízio** (round robin). Clique pelas telas e veja o nome no menu mudar. No terminal:

```bash
curl -s -D - -o /dev/null http://localhost:8080/actuator/health/readiness | grep -i x-instancia
```

### O 401 que aparece e some

Suba 3 cópias **sem** a variável `JWT_SEGREDO` e entre pela tela: de cada 3 cliques, 2 dão `401` e mandam você para o login. Por quê? Sem a variável, cada cópia **sorteia a sua chave** para assinar os tokens. O token feito pela `api-1` é recusado pela `api-2`, que tem outra chave.

A regra: **uma API que vai ter várias cópias não guarda nada só para si**. Tudo que precisa ser igual entre elas (chave dos tokens, dados, sessões) fica num lugar comum: variável de ambiente, banco de dados. A correção:

```bash
cp .env.exemplo .env
# escreva no .env:  JWT_SEGREDO=<32 ou mais caracteres aleatórios>   (openssl rand -base64 48)
docker compose up -d --scale api=3
```

E o servidor MCP (Aula 14)? Ele guarda a sessão na memória da cópia que atendeu o primeiro pedido. Por isso o `Caddyfile` manda o `/mcp` de um mesmo cliente **sempre para a mesma cópia** (`lb_policy client_ip_hash`). É o mesmo problema, resolvido do outro jeito.

### Uma cópia caiu

```bash
docker compose exec api kill 1           # "derruba" o Java de uma das cópias
docker compose ps api                    # ela volta sozinha (restart: unless-stopped)
```

Durante a queda, o proxy manda as requisições para as outras cópias (`lb_try_duration`) e o usuário não percebe. No log aparece o **desligamento gracioso**: `Commencing graceful shutdown. Waiting for active requests to complete`.

### O que o proxy esconde

O proxy também decide o que o mundo vê: `http://localhost:8080/actuator/prometheus` responde `404`. O Prometheus coleta as métricas direto de cada cópia, pela rede interna (veja os alvos em `http://localhost:9090/targets`: um por cópia).

---

## 4. Backup e restauração do banco

```bash
docker compose --profile backup run --rm backup
# Backup salvo em backups/tarefas-2026-11-26_173843.sql.gz
```

Para restaurar (por exemplo, depois de alguém apagar tarefas por engano):

```bash
docker compose stop api
docker compose --profile backup run --rm restaurar tarefas-2026-11-26_173843.sql.gz
docker compose start api
```

O backup é feito pelo `pg_dump`, que escreve o banco inteiro como comandos SQL, comprimidos com `gzip`. A pasta `backups/` está no `.gitignore`: um backup tem os dados das pessoas, inclusive os hashes de senha. **Backup que nunca foi restaurado não é backup**: teste a restauração.

---

## 5. Aula ou produção: a mesma imagem

O **mesmo** `.jar` e a **mesma** imagem rodam em todo lugar. O que muda é a configuração, que vem de variáveis de ambiente (uma das regras do [The Twelve-Factor App](https://12factor.net/pt_br/config)).

O perfil de produção ([application-producao.properties](src/main/resources/application-producao.properties)) liga-se com `PERFIL=producao` no `.env`:

| | Aula (padrão) | Produção (`PERFIL=producao`) |
|---|---|---|
| `JWT_SEGREDO` ausente | Usa a chave de aula (ou sorteia, se vier vazia) | **A API não sobe** |
| Swagger | Aberto | Desligado (`404`) |
| Logs | Texto, para ler na tela | JSON, uma linha por evento, para ferramentas de busca |

```bash
# No .env:  PERFIL=producao   (e, de propósito, sem JWT_SEGREDO)
docker compose up -d
docker compose logs api | grep JWT_SEGREDO     # "JWT_SEGREDO é obrigatório neste ambiente"
```

A API fica reiniciando (o `restart` tenta de novo, e ela recusa de novo) até a variável aparecer. É melhor do que subir com uma chave fraca.

---

## 6. O CI constrói a imagem

O [.github/workflows/aula16-ci.yml](../../.github/workflows/aula16-ci.yml) tem dois trabalhos:

1. **testes**: os 93 testes, como na Aula 15;
2. **imagem**: constrói a imagem, sobe o sistema com 2 cópias da API (`docker compose up -d --wait --scale api=2`) e faz um **teste de fumaça**: o sistema responde pelo proxy, em HTTP e HTTPS, e as duas cópias atendem.

O que mudou no código Java para tudo isso funcionar:

| Onde | Mudança |
|---|---|
| [application.properties](src/main/resources/application.properties) | Sondas `liveness` e `readiness`, desligamento gracioso, cabeçalhos do proxy (`forward-headers-strategy`), nome da instância |
| [application-producao.properties](src/main/resources/application-producao.properties) | Novo: o perfil de produção |
| [RequisicaoIdFilter](src/main/java/api/observability/RequisicaoIdFilter.java) | Devolve o cabeçalho `X-Instancia` |
| [SegurancaConfig](src/main/java/api/seguranca/SegurancaConfig.java) | No perfil de produção, sem `JWT_SEGREDO` a API não sobe |
| [js/sessao.js](src/main/resources/static/js/sessao.js) | Mostra no menu a cópia que respondeu |

E os testes novos: [InfraE2ETest](src/test/java/api/e2e/InfraE2ETest.java) (as sondas respondem sem token, o `X-Instancia`, os links em `https` atrás do proxy) e [ChaveDosTokensTest](src/test/java/api/seguranca/ChaveDosTokensTest.java) (a chave em cada ambiente).

---

## 7. Testes

```bash
docker compose --profile testes run --rm testes          # 93 testes, sem Java instalado
./mvnw test                                                # com Java 25 instalado
docker compose --profile ui run --rm playwright-run       # 10 testes de tela, entrando pelo proxy
```

Para programar pela IDE, suba só o banco e o RabbitMQ (`docker compose up -d postgres rabbitmq`) e rode a API pelo IntelliJ ou com `./mvnw spring-boot:run` (porta 8080: pare o proxy antes, com `docker compose stop proxy`).

---

## 8. Exercícios

1. **Medir e encolher a imagem.** Anote o tamanho da imagem (`docker image ls tarefas-api`). Troque a base final por `eclipse-temurin:25.0.5.1_2-jre-alpine` (o `apt-get` vira `apk add --no-cache curl`, e o `groupadd`/`useradd` vira `addgroup -S`/`adduser -S`). Quanto diminuiu? Rode os testes de tela de novo. Depois, mude só um texto do `index.html`, reconstrua e use `docker history` para mostrar qual camada mudou.
2. **Atualizar sem derrubar.** Com 3 cópias, deixe um terminal chamando a API sem parar (`while true; do curl -s -o /dev/null -w "%{http_code} " http://localhost:8080/actuator/health/readiness; sleep 0.2; done`). Em outro terminal, mude um texto do `index.html` e rode `docker compose up -d --build --scale api=3`. Aparece algum erro no primeiro terminal? Por quê? Depois, troque as cópias **uma de cada vez** (suba 4 com `--scale api=4`, espere a nova ficar `healthy`, pare uma antiga com `docker stop`, e assim por diante) e escreva no README o passo a passo que não derruba ninguém.
3. **Backup todo dia.** Crie um serviço `backup-diario` (sem perfil, sobe com o sistema) que faz o mesmo `pg_dump` a cada 24 horas (`while true; do ...; sleep 86400; done`) e **apaga os backups com mais de 7 dias** (`find /backups -name '*.sql.gz' -mtime +7 -delete`). Para testar, troque 86400 por 60 e `-mtime +7` por `-mmin +3`.
4. **Produção de verdade.** Ligue o perfil de produção (`PERFIL=producao` no `.env`). Primeiro sem `JWT_SEGREDO`: leia o erro no log. Depois com ela: confira o Swagger em `404` e os logs em JSON. Por fim, faça o perfil de produção esconder também os detalhes do health (`management.endpoint.health.show-details=never`) e explique por que isso não atrapalha o Docker nem o proxy.
5. **Limite de memória.** Baixe o limite da API no `docker-compose.yml` para `256M` e suba. O que acontece? Veja com `docker compose ps -a` e `docker inspect <container> --format '{{.State.OOMKilled}}'`. Encontre o menor limite em que a API sobe e passa nos testes de tela, e explique o papel do `-XX:MaxRAMPercentage=75` do Dockerfile.

**Desafio:** publique a imagem no **GitHub Container Registry** (`ghcr.io`) pelo CI, só quando houver push na `main`. Dica: `permissions: packages: write`, `docker/login-action` e `docker/build-push-action`, fixadas por hash como as outras actions. Depois, num outro computador, rode o sistema só com o `docker-compose.yml` trocando `build: .` por `image: ghcr.io/<usuário>/<repositório>:<versão>`.

---

## Parar e recriar

```bash
docker compose down                       # para tudo (os dados ficam nos volumes)
docker compose down -v                    # para e APAGA os dados (banco, certificados, métricas)
```

**Linux:** a pasta `backups/` é criada pelo container e fica com dono `root`. Para apagar um backup: `docker run --rm -v "$PWD/backups:/b" alpine rm /b/<arquivo>`.

O que veio das aulas anteriores continua valendo: segurança no [README da Aula 15](README-aula15.md), no [SEGURANCA.md](SEGURANCA.md) e no [CADEIA-DE-SUPRIMENTOS.md](CADEIA-DE-SUPRIMENTOS.md); MCP e assistente no [README da Aula 14](README-aula14.md); observabilidade e carga no [da Aula 13](README-aula13.md).
