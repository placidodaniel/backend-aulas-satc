# Cadeia de suprimentos: de onde vêm as bibliotecas e como confiar nelas

## Objetivo

Entender **o caminho que uma biblioteca percorre** até rodar dentro da nossa API, onde nesse caminho alguém pode trocar uma biblioteca boa por uma corrompida, e **o que o Java e o Maven oferecem** para o build recusar a peça adulterada.

Por que isso importa: o nosso `pom.xml` declara 20 dependências, mas o `.jar` final leva **127 bibliotecas**. As outras 107 vieram "de carona" (são dependências das dependências). Todo esse código roda com o mesmo poder do nosso: lê o banco, a chave do JWT, as variáveis de ambiente. Quem envenena uma biblioteca popular entra em milhares de sistemas de uma vez. Isso é um **ataque à cadeia de suprimentos** (*supply chain attack*).

Casos reais:

| Ano | Caso | O que aconteceu |
|---|---|---|
| 2018 | event-stream (npm) | Um voluntário "ajudou" a manter a biblioteca, ganhou acesso e publicou uma versão que roubava carteiras de bitcoin |
| 2021 | Log4Shell (Java) | Não foi ataque, foi uma falha grave numa biblioteca usada em quase todo sistema Java. Mostrou que ninguém sabia **onde** usava o Log4j: daí a importância do SBOM |
| 2024 | xz utils (Linux) | Um mantenedor ganhou confiança por **dois anos** e escondeu uma porta dos fundos no código de compressão usado pelo SSH |
| 2024 | MavenGate (Java) | Pesquisadores acharam centenas de bibliotecas Maven cujo domínio do `groupId` tinha expirado: quem comprasse o domínio poderia pedir para publicar no nome delas |
| 2024 | polyfill.io (web) | O domínio de um script de CDN foi vendido, e o novo dono passou a entregar código malicioso para mais de 100 mil sites |

---

## 1. O caminho de uma biblioteca

Siga a biblioteca `com.google.zxing:core:3.5.4` (a que desenha o QR Code do 2FA):

```text
 pom.xml                       Maven Central                     sua máquina                        a aplicação
┌──────────────────┐  baixa   ┌──────────────────────┐  guarda  ┌──────────────────────────┐ monta ┌───────────────────┐
│ <groupId>        │ ───────> │ repo.maven.apache.org│ ───────> │ ~/.m2/repository/com/    │ ────> │ classpath         │
│  com.google.zxing│  HTTPS   │ /maven2/com/google/  │          │  google/zxing/core/3.5.4/│       │ (compilar, testar)│
│ <artifactId>core │          │  zxing/core/3.5.4/   │          │   core-3.5.4.jar         │       │ BOOT-INF/lib/     │
│ <version>3.5.4   │          │   core-3.5.4.jar     │          │   core-3.5.4.jar.sha1    │       │ (dentro do .jar)  │
└──────────────────┘          │   core-3.5.4.jar.sha1│          │   _remote.repositories   │       └───────────────────┘
                              │   core-3.5.4.jar.asc │          └──────────────────────────┘
                              └──────────────────────┘
```

1. **As coordenadas** (`groupId:artifactId:version`) no `pom.xml` dizem **o que** queremos. O `groupId` costuma ser um domínio ao contrário (`com.google`): é por isso que um domínio abandonado é perigoso.
2. **O repositório remoto** diz **de onde** vem. Sem configuração, o Maven usa só o **Maven Central** (`https://repo.maven.apache.org/maven2`). Cada arquivo vem acompanhado de um `.sha1` (o "resumo" do arquivo) e de um `.asc` (a assinatura PGP de quem publicou).
3. **O repositório local** (`~/.m2/repository`, ou `C:\Users\<você>\.m2\repository` no Windows) é um cache: baixou uma vez, nunca mais baixa. O arquivo `_remote.repositories` anota de qual repositório cada arquivo veio.
4. **O classpath** é a lista de `.jar` que a JVM usa para achar as classes. Para compilar e testar, o Maven monta essa lista apontando para o `~/.m2`. No `mvn package`, o Spring Boot **copia** todos para dentro do nosso `.jar`, na pasta `BOOT-INF/lib/`.

Para ver cada etapa com os próprios olhos:

```bash
./mvnw dependency:tree                                   # 1. a árvore: o que veio de carona e trazido por quem
./mvnw dependency:tree -Dincludes=com.nimbusds           #    só uma biblioteca: quem a trouxe?
ls ~/.m2/repository/com/google/zxing/core/3.5.4/         # 3. o cache local: o .jar e o .sha1
./mvnw dependency:build-classpath                        # 4. o classpath, caminho por caminho
./mvnw package -DskipTests && jar tf target/*.jar | grep BOOT-INF/lib/   # 4. o que foi para dentro do .jar
```

Sem Java instalado (laboratório), troque `./mvnw` por `docker compose --profile testes run --rm testes mvn -B -ntp`.

**Um detalhe do classpath que vira risco:** se dois `.jar` têm uma classe com o mesmo nome completo, a JVM usa a **primeira** que encontrar na lista. Uma biblioteca maliciosa pode "sombrear" uma classe legítima. Por isso a árvore de dependências deve ser conhecida e enxuta: cada biblioteca a menos é uma porta a menos.

---

## 2. Onde o ataque entra

| Ponto do caminho | Ataque | Exemplo |
|---|---|---|
| Coordenadas (nome) | **Typosquatting**: nome quase igual ao verdadeiro | `com.goggle.zxing`, `jackson-databimd` |
| Coordenadas (nome) | **Slopsquatting**: a IA "inventa" uma biblioteca que não existe e alguém registra o nome | O assistente sugere `spring-boot-starter-totp`; ela não existe, até alguém criar |
| Coordenadas (versão) | **Versão dinâmica**: `[1.0,)`, `LATEST`, `RELEASE` baixam "a mais nova", inclusive uma maliciosa publicada hoje | Uma faixa de versões no `pom.xml` |
| Repositório | **Repositório a mais** no `pom.xml` ou no `settings.xml`, menos cuidado que o Central | `<repositories>` com um endereço qualquer achado num fórum |
| Repositório | **Dependency confusion**: uma biblioteca interna da empresa e uma pública com o mesmo nome; o Maven escolhe a errada | `br.com.empresa:utils` publicada no Central por outra pessoa |
| Rede | **Download sem HTTPS** (alguém no meio troca o arquivo) | O Maven 3.8.1+ já bloqueia repositórios `http://` |
| Publicação | **Conta do mantenedor roubada** ou **mantenedor malicioso** | xz utils, event-stream |
| Publicação | **Domínio do `groupId` expirado** | MavenGate |
| Cache local | **`~/.m2` adulterado** (outro programa, outro projeto, um plugin de build) | Um `.jar` trocado no cache vale para todos os projetos da máquina |
| Build | **Plugin malicioso**: plugins Maven **executam código** durante o build, com acesso a tudo | Um plugin "de formatação" que lê `~/.ssh` |
| Build | **Ferramenta de build adulterada** (o próprio Maven, baixado pelo `mvnw`) | Uma distribuição do Maven modificada |
| Imagem Docker | **Tag que muda**: `latest` ou `3.9` hoje é uma imagem, amanhã é outra | `maven:3.9-eclipse-temurin-25` |
| Front-end | **Script de CDN** trocado na origem | polyfill.io |
| Qualquer um | **Falha conhecida** (CVE) numa versão antiga, sem ataque nenhum | Log4Shell em quem não atualizou |

---

## 3. As defesas que estão neste projeto

### 3.1 Versão sempre fixa e regras no build (Maven Enforcer)

O [pom.xml](pom.xml) tem o `maven-enforcer-plugin`, que confere regras **antes** de compilar. Se uma regra falha, o build para:

| Regra | O que barra |
|---|---|
| `banDynamicVersions` | Faixas (`[1.0,2.0)`), `LATEST`, `RELEASE` e `SNAPSHOT` de terceiros |
| `requirePluginVersions` | Plugin sem versão escrita (cada Maven traria uma diferente) |
| `banDuplicatePomDependencyVersions` | A mesma dependência declarada duas vezes |
| `bannedDependencies` | A "lista negra": Log4j antes de 2.17.1 (Log4Shell), Log4j 1.x, commons-collections antes de 3.2.2, commons-text antes de 1.10.0 (Text4Shell). Vale também para dependência que vem **de carona** |
| `requireMavenVersion`, `requireJavaVersion` | Maven antigo (sem as verificações da seção 3.2) e Java errado |

A regra `requirePluginVersions` achou um problema de verdade na primeira execução: o `maven-site-plugin` vinha "da versão padrão do Maven instalado". A versão foi escrita no `pom.xml`.

**Experimente:** troque a versão do ZXing para `[3.5,)` e rode `./mvnw validate`. O Enforcer responde: `Dependency com.google.zxing:core:jar:3.5.4 (compile) is referenced with a banned dynamic version [3.5,)`.

No Docker (modo rígido da seção 3.2), a lista de checksums barra **antes** do Enforcer ("Missing from summaryFile trusted checksum"): para escolher "a mais nova", o Maven tenta ler as versões 3.5.0, 3.5.1..., e nenhuma delas foi aprovada. Defesa em camadas é isso: se uma falha, a outra segura.

### 3.2 Checksums confiáveis, guardados no Git

Um **checksum** (ou *hash*) é o "resumo" de um arquivo: SHA-512 do `.jar` gera 128 caracteres; mudou um único byte do arquivo, o resumo muda completamente.

O Maven já confere o `.sha1` que vem **do mesmo repositório** que o `.jar`. Mas, se alguém controla o repositório (ou o cache), troca os dois juntos. A defesa forte é ter **a nossa própria lista** de resumos, gravada **no Git** e revisada como qualquer código:

```text
.mvn/
├── maven.config                         <- liga as verificações em todo build
├── checksums/
│   └── checksums-central.sha512         <- 1107 linhas: "resumo  caminho/do/arquivo.jar"
└── wrapper/maven-wrapper.properties     <- resumo da distribuição do Maven (seção 3.3)
```

O [.mvn/maven.config](.mvn/maven.config) é lido pelo Maven **em todo build** (no terminal, na IDE, no Docker):

```text
--strict-checksums                                              (1)
-Daether.checksums.algorithms=SHA-512,SHA-1
-Daether.trustedChecksumsSource.summaryFile=true                (2)
-Daether.trustedChecksumsSource.summaryFile.basedir=${session.rootDirectory}/.mvn/checksums/
-Daether.artifactResolver.postProcessor.trustedChecksums=true   (3)
-Daether.artifactResolver.postProcessor.trustedChecksums.checksumAlgorithms=SHA-512
-Daether.artifactResolver.postProcessor.trustedChecksums.failIfMissing=false  (4)
-Daether.system.repositoryKeyFunction=nid                                      (5)
```

1. Se o `.sha1` do repositório não bater, **falha** (o padrão do Maven é só avisar).
2. A lista confiável é o arquivo em `.mvn/checksums/`.
3. Todo arquivo usado no build (dependência **e** plugin, inclusive os que vêm do cache `~/.m2`) é conferido contra a lista.
4. Arquivo que **não está na lista**: no `maven.config`, só um aviso (`false`); no Docker, **falha** (`true`). Veja o quadro abaixo.

> **Por que dois modos?** O IntelliJ lê o `.mvn/maven.config` com um leitor próprio e **não troca** `${session.rootDirectory}` pela pasta do projeto (só o `mvn` de linha de comando faz isso). Para o IntelliJ, a lista de checksums "não existe"; com `failIfMissing=true`, toda biblioteca falharia, a importação do Maven quebraria e o projeto nem abriria direito (o erro aparece como `configured for JVM target 5`). Por isso:
>
> | Onde | Biblioteca **adulterada** | Biblioteca **fora da lista** |
> |---|---|---|
> | Terminal (`./mvnw`) | Falha | Passa (sem conferência) |
> | IntelliJ (importação) | Não confere (não acha a lista) | Passa |
> | Docker Compose (`api`, `testes`) e CI | Falha | **Falha** |
>
> O modo rígido vem da variável `MAVEN_ARGS` no [docker-compose.yml](docker-compose.yml), que vence o `maven.config`. No terminal, use o mesmo: `MAVEN_ARGS="-Daether.artifactResolver.postProcessor.trustedChecksums.failIfMissing=true" ./mvnw verify`. Num servidor de CI (GitHub Actions, por exemplo), a regra rígida é a que vale: é lá que nenhum código entra sem passar.

5. O nome do arquivo da lista. O Maven 3.10 (que usa o Maven Resolver 2.0) passou a chamar a lista de `checksums-central-<código do endereço>.sha512`. Esta linha mantém o nome simples, `checksums-central.sha512`, nas duas versões. Sem ela, ao atualizar o Maven, o build "não acha" a lista e recusa tudo. Foi o que o CI pegou no PR do Dependabot que atualizava o Maven para a 3.10.0.

Esse recurso é do *Maven Resolver* e existe a partir do Maven 3.9 (no Gradle, o equivalente é o `gradle/verification-metadata.xml`).

**Experimente o ataque** (é o que foi feito para testar este projeto):

```bash
# troca o resumo do ZXing na lista, como se o .jar tivesse sido adulterado
sed -i 's/^967023e2aa/000000e2aa/' .mvn/checksums/checksums-central.sha512
./mvnw compile
#   [ERROR] Artifact com.google.zxing:core:jar:3.5.4 trusted checksum mismatch:
#           summaryFile=000000e2aa...; calculated=967023e2aa...
git checkout .mvn/checksums/checksums-central.sha512    # desfaz
```

**Quando você adiciona ou atualiza uma dependência**, o build do Docker (e do CI) falha, de propósito, até a lista ser regravada:

```bash
./mvnw clean verify dependency:tree -Daether.artifactResolver.postProcessor.trustedChecksums.record=true
git diff .mvn/checksums/          # REVISE: só entraram os arquivos que você esperava?
```

A revisão desse `diff` no *pull request* é o momento de perguntar "por que esta biblioteca nova trouxe outras 12?".

### 3.3 O Maven Wrapper confere o próprio Maven

O `./mvnw` baixa o Maven na primeira execução. O [maven-wrapper.properties](.mvn/wrapper/maven-wrapper.properties) agora tem `distributionSha256Sum`: se o arquivo baixado não tiver exatamente aquele resumo, o wrapper recusa:

```text
Error: Failed to validate Maven distribution SHA-256, your Maven distribution might be compromised.
```

O resumo foi conferido com o `.sha512` que a Apache publica ao lado da distribuição no Maven Central.

### 3.4 SBOM: a lista de ingredientes

O `cyclonedx-maven-plugin` gera, no `mvn package`, o **SBOM** (*Software Bill of Materials*) no formato CycloneDX: cada biblioteca, versão, licença e hash. Fica em `target/classes/META-INF/sbom/application.cdx.json` e vai dentro do `.jar`.

Quando sai uma falha nova (um "Log4Shell" da vez), a pergunta "nós usamos isso? em qual versão? em quais sistemas?" é respondida em segundos lendo os SBOMs, em vez de dias abrindo projeto por projeto.

### 3.5 Procurar falhas conhecidas (CVE)

**CVE** é o número de registro de uma falha de segurança conhecida (`CVE-2021-44228` é o Log4Shell). Ferramentas comparam as nossas versões com as bases de CVE:

| Ferramenta | Como rodar | Observação |
|---|---|---|
| **OWASP Dependency-Check** | `./mvnw -P auditoria verify` | Perfil no `pom.xml`. Falha com nota CVSS 7 ou mais. A 1ª execução baixa a base do NVD e demora; com uma chave gratuita do NVD (`NVD_API_KEY`), fica bem mais rápida |
| **OSV-Scanner** (Google) | `docker run --rm -v "$PWD/target/classes/META-INF/sbom:/sbom:ro" ghcr.io/google/osv-scanner:v2.7.0 scan source -L /sbom/application.cdx.json` | Lê o SBOM. **Não foi testado nesta máquina**: confira o comando na documentação do OSV-Scanner antes de usar em aula |
| **GitHub Dependabot** | Já ligado neste repositório (seção 3.8) | Avisa sozinho quando sai CVE de uma dependência e abre o PR de atualização |

### 3.6 Um repositório só, com HTTPS

O `pom.xml` não tem `<repositories>`: tudo vem do Maven Central, por HTTPS. Em empresa, o comum é um repositório interno (Nexus, Artifactory) que **espelha** o Central e guarda as bibliotecas internas. O `~/.m2/settings.xml` manda **tudo** para ele:

```xml
<settings>
  <mirrors>
    <mirror>
      <id>empresa</id>
      <!-- "*": TODO pedido de dependência vai para o repositório da empresa, nenhum vai direto para a internet -->
      <mirrorOf>*</mirrorOf>
      <url>https://nexus.empresa.com.br/repository/maven-todos/</url>
    </mirror>
  </mirrors>
</settings>
```

Contra *dependency confusion*: publique as bibliotecas internas com um `groupId` de um domínio **que a empresa possui** (`br.com.empresa`) e configure o repositório interno para nunca buscar esse grupo no Central.

### 3.7 Imagens Docker e o Playwright

- As imagens do [docker-compose.yml](docker-compose.yml) têm versão: `maven:3.9-eclipse-temurin-25` virou `maven:3.9.12-eclipse-temurin-25`, e `perf-test:latest` virou `perf-test:2.25.0`.
- Mais forte ainda é fixar o **digest** (o resumo da imagem), que nunca muda: `image: postgres:16-alpine@sha256:<digest>`. O digest aparece com `docker buildx imagetools inspect postgres:16-alpine`.
- O Playwright instala com `npm ci` (e não `npm install`): `npm ci` usa **exatamente** o `package-lock.json`, que guarda o resumo (`integrity`) de cada pacote, e falha se algo não bater.

### 3.8 Atualizar sem pressa e sem atraso

Dois erros opostos:

- **Nunca atualizar**: as CVEs se acumulam (Log4Shell atingiu quem estava parado).
- **Atualizar no mesmo minuto**: versões maliciosas costumam ser descobertas e removidas em poucos dias. Quem atualizou na hora já instalou.

O meio-termo é uma **quarentena** (*cooldown*): atualizar sempre, mas só versões publicadas há alguns dias. As versões dos plugins novos deste projeto foram escolhidas assim. O Dependabot e o Renovate fazem isso sozinhos.

Este repositório tem o Dependabot ligado no arquivo [.github/dependabot.yml](../../.github/dependabot.yml), na raiz do repositório (é o único lugar onde o GitHub procura). Toda segunda-feira ele confere quatro coisas deste projeto: o `pom.xml`, o `package.json` do Playwright, as imagens do `docker-compose.yml` e as actions do CI. Todas com quarentena de 7 dias:

```yaml
  - package-ecosystem: "maven"
    directory: "/Aula 15/exemplo_tarefas_seguranca"
    schedule:
      interval: "weekly"
    cooldown:
      default-days: 7        # só sugere versões publicadas há pelo menos 7 dias
```

(No Renovate, a opção é `minimumReleaseAge`.) A quarentena vale para as atualizações **de rotina**. Quando sai uma correção de segurança (CVE), o Dependabot abre o PR na hora.

### 3.8b O CI: quem valida o PR

O Dependabot só **propõe**. Quem **valida** é o CI: [.github/workflows/aula15-ci.yml](../../.github/workflows/aula15-ci.yml), que roda no GitHub (aba **Actions**) a cada push, em qualquer branch, e a cada PR:

| Passo | O que confere |
|---|---|
| `./mvnw verify` com os checksums no modo **rígido** | Compila, roda os 87 testes, as regras do Enforcer, e recusa biblioteca adulterada **ou fora da lista** |
| SBOM guardado | A lista de ingredientes de cada execução fica em *Artifacts* |
| Revisão de dependências (só em PR) | Reprova se o PR trouxer uma biblioteca com CVE de gravidade alta ou crítica |

O próprio CI segue as regras da aula: permissão só de leitura e cada action **fixada pelo hash do commit**, não pela tag. Uma tag (`v7`) pode ser movida para outro código; o hash, nunca. Foi assim que, em 2025, a action `tj-actions/changed-files` comprometida vazou segredos de milhares de repositórios que usavam a tag.

**O que acontece com um PR do Dependabot que atualiza o `pom.xml`:**

1. O Dependabot abre o PR trocando, por exemplo, o ZXing de 3.5.4 para 3.5.5.
2. O CI fica **vermelho**: o `.jar` novo não está em `.mvn/checksums/` ("Missing from summaryFile trusted checksum"). É de propósito: nenhuma biblioteca entra sem uma pessoa olhar.
3. Alguém da equipe baixa a branch do PR, regrava a lista e **revisa** o diff:
   ```bash
   git fetch origin && git switch <branch-do-dependabot>
   ./mvnw clean verify dependency:tree -Daether.artifactResolver.postProcessor.trustedChecksums.record=true
   git diff .mvn/checksums/          # entrou só o ZXing 3.5.5? Ou vieram outras bibliotecas junto?
   git commit -am "Checksums do ZXing 3.5.5" && git push
   ```
4. O CI roda de novo, fica **verde** e o PR pode entrar.

### 3.9 Assinaturas

O Maven Central exige que todo arquivo publicado tenha uma assinatura PGP (o `.asc`). O checksum prova que o arquivo **não mudou**; a assinatura prova **quem publicou**. O Maven não confere assinaturas sozinho; o plugin `org.simplify4u.plugins:pgpverify-maven-plugin` confere todas, a partir de uma lista de chaves confiáveis, também guardada no projeto. Não ligamos neste exemplo porque a primeira configuração (uma chave por biblioteca) é trabalhosa, mas em sistemas críticos vale o esforço.

---

## 4. Antes de adicionar uma dependência

Use esta lista no projeto do grupo:

1. **Precisa mesmo?** O TOTP deste projeto tem 60 linhas sem biblioteca. Para 20 linhas de código, uma dependência (e as dependências dela) não compensa.
2. **É a biblioteca certa?** Copie as coordenadas do site oficial do projeto ou do [central.sonatype.com](https://central.sonatype.com), nunca de um comentário de fórum ou de uma sugestão da IA sem conferir. Confira o `groupId` letra por letra.
3. **Está viva?** Último lançamento, número de mantenedores, issues respondidas. O [deps.dev](https://deps.dev) e o [OpenSSF Scorecard](https://scorecard.dev) mostram isso.
4. **O que ela traz de carona?** `./mvnw dependency:tree` depois de adicionar.
5. **Tem CVE?** `./mvnw -P auditoria verify` ou o Dependabot.
6. **Versão fixa e já com alguns dias de publicada.**
7. **Regrave os checksums** e revise o `git diff .mvn/checksums/`.

## 5. Resumo dos comandos

| Para quê | Comando |
|---|---|
| Árvore de dependências | `./mvnw dependency:tree` |
| Classpath | `./mvnw dependency:build-classpath` |
| O que vai dentro do .jar | `jar tf target/*.jar \| grep BOOT-INF/lib/` |
| Conferir as regras do Enforcer | `./mvnw validate` |
| Regravar os checksums confiáveis | `./mvnw clean verify dependency:tree -Daether.artifactResolver.postProcessor.trustedChecksums.record=true` |
| Gerar o SBOM | `./mvnw package` (sai em `target/classes/META-INF/sbom/`) |
| Procurar CVEs | `./mvnw -P auditoria verify` |
| Sem Java (laboratório) | `docker compose --profile testes run --rm testes mvn -B -ntp <o resto do comando>` |
