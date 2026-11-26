# syntax=docker/dockerfile:1
#
# Aula 16: a RECEITA da imagem da API.
#
# Uma imagem é um "pacote fechado": o Java, a aplicação e tudo de que ela precisa
# para rodar. Quem tem a imagem roda a API em qualquer máquina com Docker, sem
# instalar Java nem Maven e sem "na minha máquina funciona".
#
# Esta receita tem DUAS etapas (multi-stage build):
#
#   1. "construcao": uma imagem GRANDE, com Maven e JDK, que compila o projeto e gera o .jar;
#   2. a final:      uma imagem PEQUENA, só com o Java para rodar (JRE) e o resultado da etapa 1.
#
# O Maven, o código-fonte e o JDK ficam na etapa 1 e NÃO vão para a imagem final.
#
# Construir:   docker compose build api        (ou: docker build -t tarefas-api:aula16 .)
# Ver o tamanho: docker image ls tarefas-api

# ============================================================================
# Etapa 1: compilar e empacotar
# ============================================================================
# Versão SEMPRE fixa (Aula 15): "maven:latest" muda sozinha e o build de amanhã
# pode sair diferente do de hoje. É a mesma imagem do serviço "testes" do Compose.
FROM maven:3.9.12-eclipse-temurin-25 AS construcao

# Pasta de trabalho dentro da imagem (é criada se não existir).
WORKDIR /app

# Copia só o que o Maven precisa para saber QUAIS bibliotecas baixar:
# o pom.xml e a pasta .mvn (com os checksums confiáveis da Aula 15).
COPY pom.xml ./
COPY .mvn/ .mvn/

# Depois, o código. O .dockerignore deixa de fora target/, .git, .env e companhia.
COPY src/ src/

# Compila e gera o .jar. Detalhes de cada parte:
#
# --mount=type=cache,target=/root/.m2
#     Guarda as bibliotecas baixadas num CACHE do Docker, fora da imagem.
#     Só o primeiro build baixa da internet; os próximos reaproveitam.
#
# -Daether...failIfMissing=true
#     Modo RÍGIDO dos checksums (Aula 15, CADEIA-DE-SUPRIMENTOS.md): biblioteca
#     adulterada ou fora da lista em .mvn/checksums/ derruba o build da imagem.
#
# -DskipTests
#     Os testes rodam ANTES, no CI e no serviço "testes" do Compose. Aqui dentro
#     não existe Docker para o Testcontainers subir o banco dos testes E2E.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp \
        -Daether.artifactResolver.postProcessor.trustedChecksums.failIfMissing=true \
        -DskipTests \
        package

# Abre o .jar em CAMADAS (recurso do Spring Boot). Saem 4 pastas:
#
#   dependencies/           as bibliotecas (Spring, Hibernate...): ~95% do tamanho, quase nunca mudam
#   spring-boot-loader/     o "lançador" do Spring Boot
#   snapshot-dependencies/  bibliotecas em versão SNAPSHOT (aqui, nenhuma)
#   application/            o NOSSO código e os arquivos estáticos: muda a cada commit
#
# Por que separar? Veja a etapa 2.
RUN java -Djarmode=tools -jar target/exemplo-tarefas-infra-1.0-SNAPSHOT.jar \
        extract --layers --launcher --destination extraido

# ============================================================================
# Etapa 2: a imagem que vai rodar
# ============================================================================
# Só o JRE (o Java que RODA programas), sem compilador e sem Maven.
# "noble" = Ubuntu 24.04. Versão fixa, de novo.
FROM eclipse-temurin:25.0.5.1_2-jre-noble

# O curl é usado só pelo HEALTHCHECK, lá embaixo (a imagem do JRE não traz curl).
# --no-install-recommends e apagar a lista do apt: imagem menor, menos coisa instalada.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Um usuário SEM PODERES para rodar a API. Por padrão, o processo do container roda
# como root: se alguém achar uma falha na API, ganha o root do container.
RUN groupadd --system tarefas && useradd --system --gid tarefas --no-create-home tarefas

WORKDIR /app

# Cada COPY vira uma CAMADA da imagem, e o Docker guarda cada camada em cache.
# Ordem: do que quase nunca muda para o que muda sempre. Quando só o nosso código
# muda, o Docker reaproveita as camadas de cima (as ~150 MB de bibliotecas) e
# refaz só a última (menos de 1 MB). O build fica rápido e o "docker pull" no
# servidor baixa só o que mudou.
COPY --from=construcao /app/extraido/dependencies/ ./
COPY --from=construcao /app/extraido/spring-boot-loader/ ./
COPY --from=construcao /app/extraido/snapshot-dependencies/ ./
COPY --from=construcao /app/extraido/application/ ./

# Daqui para baixo, tudo roda como o usuário sem poderes.
USER tarefas

# Documenta a porta em que a API escuta. (Não publica nada: quem publica é o Compose.)
EXPOSE 8080

# O Java respeita o limite de memória do container. 75%: o resto fica para a
# memória "fora do heap" (threads, classes, buffers de rede).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# O Docker pergunta de tempos em tempos: "você está pronta?". A resposta vem do
# Actuator: /actuator/health/readiness só diz UP quando a API pode receber
# requisições (banco conectado, migrations aplicadas).
#   --start-period: tempo de tolerância enquanto o Spring sobe.
HEALTHCHECK --interval=10s --timeout=3s --start-period=90s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health/readiness > /dev/null || exit 1

# O comando que o container roda ao subir. Forma de lista ["..."], sem shell no meio:
# o Java vira o processo principal e recebe direto o sinal de "desligar" (SIGTERM)
# do "docker compose stop", para terminar as requisições em andamento com calma.
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
