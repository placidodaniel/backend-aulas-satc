# Exercícios: Integração de Serviços

## Objetivo

Na aula, a nossa API aprendeu a consumir outra API: o `GET /previsao` chama o Open-Meteo e devolve a previsão do dia, traduzida para o nosso formato. Nos exercícios, você vai usar o Open-Meteo em mais lugares e preparar a API para o que pode dar errado **do outro lado**.

| Exercício | O que você vai construir | O que você pratica |
|---|---|---|
| 1. O tempo de agora | `GET /previsao/agora` | Repetir, do começo ao fim, o caminho da aula: formato do outro, chamada, tradução, regra e rota |
| 2. Previsão para o prazo da tarefa | `GET /tarefas/{id}/previsao` | Juntar um dado nosso (a tarefa) com um dado de fora (a previsão) numa resposta só |
| 3. Tempo esgotado vira 504 | Uma exceção nova e um status novo | Separar "o serviço falhou" (`502`) de "o serviço demorou" (`504`) |
| 4. Cache de 30 minutos | O campo `origem` na previsão | Não perguntar de novo o que acabou de ser respondido |
| 5. Tarefas em dias de chuva | `GET /tarefas/chuvosas` | Buscar tudo de uma vez, em vez de uma chamada por tarefa |

**Faça os exercícios em ordem.** Cada um usa o que o anterior construiu.

---

## Antes de começar

### Como rodar

```bash
docker compose up -d          # PostgreSQL na porta 5439
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

> **Atenção:** este projeto exige **JDK 25** e **internet** (a API consulta o Open-Meteo de verdade). Depois de mudar qualquer arquivo Java, **pare a API (Ctrl+C) e rode de novo**: o código novo só vale depois de reiniciar.

### Como testar sem a tela

Nos testes pelo terminal, pelo Postman ou pelo Swagger UI (`http://localhost:8080/swagger-ui.html`), use sempre as coordenadas da SATC, em Criciúma: **latitude `-28.6775`** e **longitude `-49.3697`**. A data precisa estar **entre hoje e daqui a 15 dias**, no formato `ano-mês-dia`. Exemplo (troque a data):

```bash
curl "http://localhost:8080/previsao?latitude=-28.6775&longitude=-49.3697&data=2026-11-03"
```

### O que já vem pronto: o caminho de um `GET /previsao`

Os exercícios repetem este caminho. Abra cada arquivo e acompanhe:

| Passo | Arquivo | O que acontece |
|---|---|---|
| 1 | `controller/PrevisaoController.java` | Recebe `latitude`, `longitude` e `data` da URL e chama o `PrevisaoService` |
| 2 | `service/PrevisaoService.java` | Confere as regras R1 a R3. Se alguma falhar, responde `400` e **não chama** o Open-Meteo |
| 3 | `client/OpenMeteoClient.java` | Monta o endereço do Open-Meteo, faz a requisição e transforma o JSON de lá num `OpenMeteoRespostaDTO` |
| 4 | `client/OpenMeteoRespostaDTO.java` | O formato do Open-Meteo, com os nomes dele (`weather_code`, `temperature_2m_max`) e uma lista por medida |
| 5 | `mapper/PrevisaoMapper.java` | Traduz a resposta do Open-Meteo para o nosso `PrevisaoDTO`: o código `95` vira "Trovoada" e ⛈️ |
| 6 | `controller/ApiExceptionHandler.java` | Regra violada vira `400`; falha do Open-Meteo (`PrevisaoIndisponivelException`) vira `502` |

Duas palavras que aparecem em todo o enunciado:

- **Client:** a classe que faz a requisição para a outra API. Só ela sabe o endereço, os parâmetros e o formato do JSON de lá.
- **Contrato do outro:** o formato do JSON do Open-Meteo. Não dá para mudar, então ele fica num record próprio (no pacote `client`), e o mapper traduz para o nosso.

### Como saber se deu certo

- Em <http://localhost:8080/>, a barra **"Aula 12, segundo o /v3/api-docs"** marca ✅ em cada exercício que já aparece no Swagger. A página também muda sozinha: a caixa da previsão ganha o tempo de agora (Exercício 1), cada tarefa ganha o botão ☁️ (Exercício 2), aparece "veio do cache" (Exercício 4) e surge o botão ☔ (Exercício 5).
- Em <http://localhost:8080/contrato.html>, cada item dos exercícios é testado de verdade e marcado com ✅ / ⏳ / ❌.

### Como cada exercício está escrito

1. **O que você vai construir:** a rota (ou a mudança) e um exemplo da resposta.
2. **Como vai funcionar:** o caminho da requisição, passo a passo.
3. **Passo a passo:** cada passo diz **em qual arquivo**, **o que fazer** e **por quê**. Embaixo de cada passo há o **código pronto**, escondido: tente escrever antes de abrir. Se abrir, leia os comentários do código, porque é neles que está a explicação de cada linha.
4. **Como testar** e os **subexercícios** (as perguntas do e-mail).

> "Dentro da classe" quer dizer: entre a primeira `{` e a última `}` do arquivo. A ordem dos métodos não importa para o Java.

---

# Entrega

A entrega tem **duas partes**. As duas são obrigatórias.

### 1. Código no GitHub

Suba o projeto para o **seu** repositório no GitHub. O repositório precisa estar **público**, e `./mvnw spring-boot:run` precisa subir sem erro no que você subiu. É a página `contrato.html` do seu projeto que será aberta na correção.

### 2. Respostas por e-mail

Envie um e-mail para **daniel.placido@satc.edu.br** com:

- **Assunto:** `Integração - <seu nome completo>`
- **Link do repositório** do GitHub no início do corpo do e-mail.
- **As respostas dos subexercícios no CORPO do e-mail**, com a **pergunta copiada em cima** de cada resposta.

Não envie as respostas em anexo, nem só o link do repositório. As respostas têm que estar no corpo do e-mail.

### Modelo do corpo do e-mail

```
Nome: Fulano de Tal
Repositório: https://github.com/fulano/exemplo_tarefas

--------------------------------------------------
Exercício 1.1
Pergunta: ...
Resposta: ...

(e assim por diante, até o 5.2)
```

São **10 subexercícios** no total, dois em cada exercício. Responda todos.

---

## Exercício 1: O tempo de agora

### O que você vai construir

Uma rota nova, que diz como está o tempo **neste momento** num lugar:

```text
GET /previsao/agora?latitude=-28.6775&longitude=-49.3697
```

```json
{ "horario": "2026-10-29T14:15:00", "descricao": "Nublado", "icone": "☁️", "temperatura": 21.4, "vento": 11.9 }
```

O `GET /previsao` da aula responde como vai estar o tempo **num dia**. Este responde como está **agora**. É a mesma API do Open-Meteo, no mesmo endereço, com outro parâmetro, e com uma resposta em outro formato.

### Antes de programar: veja o que o Open-Meteo responde

Abra este endereço no navegador:

<https://api.open-meteo.com/v1/forecast?latitude=-28.6775&longitude=-49.3697&current=temperature_2m,wind_speed_10m,weather_code&timezone=auto>

```json
{
  "latitude": -28.64675,
  "longitude": -49.36145,
  "current": { "time": "2026-10-29T14:15", "temperature_2m": 21.4, "wind_speed_10m": 11.9, "weather_code": 3 }
}
```

Compare com o `daily` da aula: lá, cada medida é uma **lista** (um item por dia). Aqui, `current` é um **objeto só**, porque "agora" é um momento só.

### Como vai funcionar

1. O navegador chama `GET /previsao/agora?latitude=...&longitude=...`.
2. O `PrevisaoController.agora()` recebe os dois números e chama o `PrevisaoService.agora()`.
3. O service confere a regra R1 (coordenadas válidas) e chama o `OpenMeteoClient.buscarAgora()`.
4. O client faz a requisição ao endereço acima, e o Jackson transforma o JSON num `OpenMeteoAgoraDTO`.
5. O `PrevisaoMapper.toAgora()` traduz o `OpenMeteoAgoraDTO` (formato do Open-Meteo) para o `AgoraDTO` (o nosso formato).
6. O controller devolve o `AgoraDTO`, e o Jackson o transforma em JSON.

São seis passos, um por arquivo: os mesmos seis que o `GET /previsao` já tem.

### Passo a passo

**Passo 1. O formato do Open-Meteo: crie o arquivo `client/OpenMeteoAgoraDTO.java`**

Este record é o "contrato do outro": ele copia o JSON do Open-Meteo do jeito que ele é, **com os mesmos nomes**, para o Jackson saber onde colocar cada valor. Use o `OpenMeteoRespostaDTO` como modelo. Duas diferenças:

- `current` é um objeto, então ele vira um record dentro do record (`Atual`), com `time`, `temperature_2m`, `wind_speed_10m` e `weather_code`;
- `time` vem como `"2026-10-29T14:15"` (data **e** hora), então o tipo é `LocalDateTime`, e não `LocalDate`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

```java
package api.client;

import java.time.LocalDateTime;

// Exercício 1: a resposta do Open-Meteo para "current=...", do jeito que ELE manda:
//
//   {"latitude": -28.64, "longitude": -49.36,
//    "current": {"time": "2026-10-21T23:15", "temperature_2m": 14.1,
//                "wind_speed_10m": 11.9, "weather_code": 3}}
//
// Aqui não há listas: "agora" é um momento só, então "current" é um objeto.
public record OpenMeteoAgoraDTO(

        // A latitude do ponto que o Open-Meteo usou.
        Double latitude,

        // A longitude do ponto que o Open-Meteo usou.
        Double longitude,

        // As medidas de agora (o record Atual, logo abaixo).
        Atual current

) {

    // As medidas do momento, com os mesmos nomes do JSON.
    public record Atual(

            // Quando foi a medição (hora do lugar).
            LocalDateTime time,

            // Temperatura, em °C.
            Double temperature_2m,

            // Vento, em km/h.
            Double wind_speed_10m,

            // Código do tempo (a mesma tabela da previsão diária).
            Integer weather_code

    ) {
    }
}
```

</details>

**Passo 2. A chamada: crie o método `buscarAgora()` no `client/OpenMeteoClient.java`**

O `buscarPrevisao()` já faz uma chamada ao Open-Meteo. O `buscarAgora()` é uma cópia dele com três mudanças:

1. recebe só `latitude` e `longitude` (não há datas);
2. no lugar de `.queryParam("daily", ...)`, `start_date` e `end_date`, vai `.queryParam("current", "temperature_2m,wind_speed_10m,weather_code")`;
3. no `.body(...)`, o tipo da resposta é `OpenMeteoAgoraDTO.class`.

Ele também passa pelo `chamar()`, que transforma qualquer falha do Open-Meteo em `PrevisaoIndisponivelException` (e, portanto, em `502`). Coloque o método dentro da classe, logo depois do `buscarPrevisao()`. Não precisa de import novo: o `OpenMeteoAgoraDTO` está no mesmo pacote.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

```java
    // Exercício 1: GET /v1/forecast?latitude=...&longitude=...&current=...&timezone=auto
    // O tempo de agora. Mesmo caminho do buscarPrevisao(), outro parâmetro:
    // "current" no lugar de "daily" (e sem datas).
    public OpenMeteoAgoraDTO buscarAgora(double latitude, double longitude) {
        return chamar("buscar o tempo de agora", () -> restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,wind_speed_10m,weather_code")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                // Agora o JSON vira um OpenMeteoAgoraDTO.
                .body(OpenMeteoAgoraDTO.class));
    }
```

</details>

**Passo 3. O nosso formato: crie o arquivo `dto/AgoraDTO.java`**

O formato que a **nossa** API devolve, em português, com cinco campos: `horario`, `descricao`, `icone`, `temperatura` e `vento`. Cada campo leva um `@Schema` com descrição e exemplo, como os outros DTOs, para aparecer bem no Swagger.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

```java
package api.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 1: o tempo de AGORA, como a NOSSA API devolve (em português).
@Schema(description = "O tempo de agora, num lugar")
public record AgoraDTO(

        // "current.time" do Open-Meteo.
        @Schema(description = "Quando foi a medição (hora do lugar)", example = "2026-10-21T23:15:00")
        LocalDateTime horario,

        // "current.weather_code", traduzido em texto.
        @Schema(description = "Como está o tempo, em português", example = "Nublado")
        String descricao,

        // O mesmo ícone da previsão diária.
        @Schema(description = "Ícone (emoji) do tempo", example = "☁️")
        String icone,

        // "current.temperature_2m".
        @Schema(description = "Temperatura agora, em °C", example = "14.1")
        Double temperatura,

        // "current.wind_speed_10m".
        @Schema(description = "Vento agora, em km/h", example = "11.9")
        Double vento

) {
}
```

</details>

**Passo 4. A tradução: crie o método `toAgora()` no `mapper/PrevisaoMapper.java`**

O mapper recebe o `OpenMeteoAgoraDTO` e monta o `AgoraDTO`. O código do tempo (`weather_code`) vira texto e ícone com os métodos `descricao()` e `icone()`, que **já existem** no mapper: é só chamar.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `PrevisaoMapper`, acrescente:

```java
import api.client.OpenMeteoAgoraDTO;
import api.dto.AgoraDTO;
```

E o método, dentro da classe, logo depois do `toPrevisao()`:

```java
    // Exercício 1: "current" do Open-Meteo → AgoraDTO. Reaproveita os
    // métodos descricao() e icone(), que já traduzem o código do tempo.
    public AgoraDTO toAgora(OpenMeteoAgoraDTO resposta) {
        // Atalho para não escrever resposta.current() a cada linha.
        OpenMeteoAgoraDTO.Atual atual = resposta.current();
        return new AgoraDTO(
                atual.time(),
                descricao(atual.weather_code()),
                icone(atual.weather_code()),
                atual.temperature_2m(),
                atual.wind_speed_10m());
    }
```

</details>

**Passo 5. A regra: crie o método `agora()` no `service/PrevisaoService.java`**

O service faz o mesmo que o `buscar()` já faz, com menos regras: só a R1 (coordenadas válidas), porque aqui não há data. Depois chama o client e o mapper. Se o Open-Meteo responder `200` sem o bloco `current`, o service lança `PrevisaoIndisponivelException` (vira `502`): dado de fora não é confiável.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `PrevisaoService`, acrescente:

```java
import api.client.OpenMeteoAgoraDTO;
import api.dto.AgoraDTO;
```

E o método, dentro da classe, logo depois do `buscar()`:

```java
    // Exercício 1: GET /previsao/agora -- o tempo de agora, num lugar.
    public AgoraDTO agora(double latitude, double longitude) {
        // Só a R1 vale aqui: não há data para conferir.
        validarCoordenadas(latitude, longitude);
        // A chamada ao Open-Meteo.
        OpenMeteoAgoraDTO resposta = client.buscarAgora(latitude, longitude);
        // Dado de fora não é confiável: 200 sem o bloco "current" é falha do serviço (502).
        if (resposta == null || resposta.current() == null) {
            throw new PrevisaoIndisponivelException("O serviço de previsão respondeu sem o tempo de agora", null);
        }
        // Traduz para o nosso formato.
        return mapper.toAgora(resposta);
    }
```

</details>

**Passo 6. A rota: crie o método `agora()` no `controller/PrevisaoController.java`**

A classe já tem `@RequestMapping("/previsao")`, então o método novo só precisa de `@GetMapping("/agora")`: os dois juntos formam `/previsao/agora`. Os parâmetros são os mesmos do `buscar()`, sem a data. Documente com `@Operation` e `@ApiResponse` (`200`, `400` e `502`), como o `buscar()`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `PrevisaoController`, acrescente:

```java
import api.dto.AgoraDTO;
```

E o método, dentro da classe, logo depois do `buscar()`:

```java
    // Exercício 1: GET /previsao/agora?latitude=...&longitude=...
    // O @RequestMapping("/previsao") da classe já vale: aqui só entra "/agora".
    @Operation(summary = "O tempo de agora", description = "Temperatura, vento e tempo do momento, pelo Open-Meteo.")
    @ApiResponse(responseCode = "200", description = "O tempo de agora")
    @ApiResponse(responseCode = "400", description = "Coordenada ausente ou fora do planeta",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "502", description = "O serviço de previsão não respondeu",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/agora")
    public AgoraDTO agora(
            @Parameter(description = "Latitude, de -90 a 90", example = "-28.6775") @RequestParam double latitude,
            @Parameter(description = "Longitude, de -180 a 180", example = "-49.3697") @RequestParam double longitude) {
        // Delega a consulta para o service.
        return service.agora(latitude, longitude);
    }
```

</details>

### Como testar

Reinicie a API e confira:

| chamada | resultado esperado |
|---|---|
| `GET /previsao/agora?latitude=-28.6775&longitude=-49.3697` | `200` com `horario`, `descricao`, `icone`, `temperatura` e `vento` |
| `GET /previsao/agora?latitude=999&longitude=-49.3697` | `400` (R1), sem chamar o Open-Meteo |
| `GET /previsao/agora?longitude=-49.3697` | `400`: parâmetro obrigatório ausente |
| Swagger UI, grupo **Previsão do tempo** | A rota nova aparece, com o `AgoraDTO` em *Schemas* |

Na `index.html`, ao escolher a data do prazo, a caixa da previsão ganha uma linha a mais: "Agora: ☁️ Nublado, 21°, vento 12 km/h".

### Subexercícios

**1.1**: O Open-Meteo devolve o `daily` como um objeto com **uma lista por medida**, e a nossa API devolve **um objeto por dia**. Por que a nossa API não repassa as listas do jeito que vieram?

**1.2**: O Open-Meteo aceita ser chamado direto pelo navegador. Por que, então, a página pede a previsão à **nossa** API, que pede ao Open-Meteo? O que se ganha com esse passo a mais?

---

## Exercício 2: Previsão para o prazo da tarefa

### O que você vai construir

Uma rota que devolve uma tarefa **e** a previsão do tempo para o dia do prazo dela, numa resposta só:

```text
GET /tarefas/3/previsao?latitude=-28.6775&longitude=-49.3697
```

```json
{
  "tarefa":   { "id": 3, "titulo": "Entregar o trabalho", "dataPrazo": "2026-11-01", "...": "..." },
  "previsao": { "data": "2026-11-01", "descricao": "Garoa", "icone": "🌦️", "chanceDeChuva": 70, "...": "..." },
  "aviso": null
}
```

A tarefa vem do **nosso** banco; a previsão vem do Open-Meteo. Integrar é isso: juntar o que é nosso com o que é dos outros.

E se o prazo for daqui a 30 dias, ou já tiver passado? Não existe previsão para esse dia, mas a tarefa existe, e o pedido está certo. Então a resposta **não é erro**: vem a tarefa, `"previsao": null` e um `"aviso"` explicando:

```json
{ "tarefa": { "id": 6, "...": "..." }, "previsao": null, "aviso": "Ainda não há previsão para o prazo (2026-11-28): ela só cobre até 2026-11-13" }
```

Isso tem nome: **fallback**, uma resposta alternativa quando uma parte não está disponível, em vez de falhar o pedido inteiro.

### Como vai funcionar

1. O navegador chama `GET /tarefas/{id}/previsao?latitude=...&longitude=...`.
2. O `TarefaController` chama o `TarefaService.buscarComPrevisao()`.
3. O `TarefaService` confere as coordenadas (pedindo ao `PrevisaoService`) e busca a tarefa no banco (`404` se não existir).
4. Ele pergunta ao `PrevisaoService` se existe previsão para o dia do prazo (`temPrevisao()`):
   - **não existe:** monta a resposta com a tarefa, `previsao` `null` e o aviso;
   - **existe:** pede a previsão ao `PrevisaoService.buscar()`, o mesmo do `GET /previsao`, e monta a resposta com a tarefa e a previsão.

Repare: o `TarefaService` **não** fala com o `OpenMeteoClient`. Quem sabe tudo sobre previsão (regras, tradução) é o `PrevisaoService`; o `TarefaService` só pede a ele.

### Passo a passo

**Passo 1. A resposta composta: crie o arquivo `dto/TarefaComPrevisaoDTO.java`**

Um record com três campos: a tarefa (`TarefaResponseDTO`, o mesmo do `GET /tarefas/{id}`), a previsão (`PrevisaoDTO`, o mesmo do `GET /previsao`) e o aviso (`String`). É um DTO composto, como o `ResponsavelDetalheDTO` da Aula 09, que juntava o responsável com as tarefas dele.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

```java
package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 2: DTO COMPOSTO -- uma tarefa (do NOSSO banco) com a previsão do
// tempo para o prazo dela (do Open-Meteo), numa resposta só.
// Sem previsão para o prazo, "previsao" vem null e "aviso" explica o motivo.
@Schema(description = "Uma tarefa com a previsão do tempo para o prazo dela")
public record TarefaComPrevisaoDTO(

        // A tarefa, no mesmo formato do GET /tarefas/{id}.
        @Schema(description = "A tarefa, igual ao GET /tarefas/{id}")
        TarefaResponseDTO tarefa,

        // A previsão do dia do prazo, ou null.
        @Schema(description = "Previsão para o dia do prazo; null quando não há previsão para esse dia")
        PrevisaoDTO previsao,

        // Por que não veio previsão, ou null quando veio.
        @Schema(description = "Motivo de não haver previsão; null quando a previsão veio", example = "null")
        String aviso

) {
}
```

</details>

**Passo 2. Duas mudanças no `service/PrevisaoService.java`**

O `TarefaService` vai precisar de duas coisas do `PrevisaoService`:

1. **conferir as coordenadas:** o método `validarCoordenadas()` já existe, mas é `private` (só a própria classe enxerga). Troque `private` por `public`;
2. **saber se uma data tem previsão:** crie o método `temPrevisao(LocalDate data)`, que devolve `true` quando a data está entre hoje e hoje + 15 dias. Ele faz a mesma conta da R2 e da R3, mas **devolve `true` ou `false`** em vez de lançar exceção.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Troque, no `PrevisaoService`, a linha

```java
    private void validarCoordenadas(double latitude, double longitude) {
```

por

```java
    public void validarCoordenadas(double latitude, double longitude) {
```

E crie o método `temPrevisao()`, dentro da classe, logo depois do `buscar()`:

```java
    // Exercício 2: o Open-Meteo tem previsão para este dia? (R2 e R3, sem lançar exceção.)
    public boolean temPrevisao(LocalDate data) {
        LocalDate hoje = LocalDate.now();
        // Nem antes de hoje, nem depois de hoje + 15.
        return !data.isBefore(hoje) && !data.isAfter(hoje.plusDays(DIAS_DE_PREVISAO));
    }
```

</details>

**Passo 3. A composição: o `service/TarefaService.java` recebe o `PrevisaoService` e ganha o `buscarComPrevisao()`**

Primeiro, o `TarefaService` precisa **receber** o `PrevisaoService`, do mesmo jeito que já recebe o repository e o mapper: um atributo novo e um parâmetro novo no construtor. O Spring vê o parâmetro novo e entrega o `PrevisaoService` sozinho.

Depois, o método `buscarComPrevisao()` segue os quatro passos do "Como vai funcionar". Ele usa o `buscarEntidade(id)`, que já existe no `TarefaService` e lança `404` quando a tarefa não existe.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `TarefaService`, acrescente:

```java
import api.dto.TarefaComPrevisaoDTO;
```

Troque o atributo `mapper` e o construtor (o trecho que vai de `private final TarefaMapper mapper;` até o `}` do construtor) por este trecho, que já traz o método novo logo depois do construtor:

```java
    // Aula 09: converte DTO <-> entidade. Injetado pelo Spring, igual aos repositórios.
    private final TarefaMapper mapper;

    // Aula 12, Exercício 2: a previsão do tempo para os prazos.
    private final PrevisaoService previsaoService;

    // Recebe as dependências que o Spring injeta automaticamente.
    public TarefaService(TarefaRepository repository, ResponsavelRepository responsavelRepository, TarefaMapper mapper,
                         PrevisaoService previsaoService) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.repository = repository;
        this.responsavelRepository = responsavelRepository;
        this.mapper = mapper;
        this.previsaoService = previsaoService;
    }

    // Aula 12, Exercício 2: a tarefa com a previsão do tempo para o prazo dela.
    // Dado próprio (a tarefa, do banco) + dado externo (a previsão, do Open-Meteo).
    public TarefaComPrevisaoDTO buscarComPrevisao(Long id, double latitude, double longitude) {
        // 1. Coordenada inválida é erro de quem chamou, tenha previsão ou não (400).
        previsaoService.validarCoordenadas(latitude, longitude);
        // 2. A tarefa (404 se não existir), já no formato de saída.
        Tarefa tarefa = buscarEntidade(id);
        TarefaResponseDTO dto = mapper.toResponse(tarefa);
        LocalDate prazo = tarefa.getDataPrazo();
        // 3. Sem previsão para o prazo: a tarefa vem do mesmo jeito, com um aviso.
        if (!previsaoService.temPrevisao(prazo)) {
            String aviso = prazo.isBefore(LocalDate.now())
                    ? "O prazo (" + prazo + ") já passou: não há previsão para dias passados"
                    : "Ainda não há previsão para o prazo (" + prazo + "): ela só cobre até "
                            + LocalDate.now().plusDays(PrevisaoService.DIAS_DE_PREVISAO);
            return new TarefaComPrevisaoDTO(dto, null, aviso);
        }
        // 4. Com previsão: a mesma do GET /previsao, para o dia do prazo.
        return new TarefaComPrevisaoDTO(dto, previsaoService.buscar(latitude, longitude, prazo), null);
    }
```

</details>

**Passo 4. A rota: crie o método `buscarComPrevisao()` no `controller/TarefaController.java`**

A classe já tem `@RequestMapping("/tarefas")`. O método novo usa `@GetMapping("/{id}/previsao")`: o `id` vem do caminho (`@PathVariable`) e a latitude e a longitude vêm da query string (`@RequestParam`). Documente com `200`, `400`, `404` e `502`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `TarefaController`, acrescente:

```java
import api.dto.TarefaComPrevisaoDTO;
```

E o método, dentro da classe (por exemplo, logo antes do `listarAtrasadas()`):

```java
    // Aula 12, Exercício 2: GET /tarefas/{id}/previsao?latitude=...&longitude=...
    @Operation(summary = "Tarefa com a previsão do tempo para o prazo",
            description = "Quando não há previsão para o prazo (passado ou longe demais), previsao vem null e aviso explica.")
    @ApiResponse(responseCode = "200", description = "A tarefa e a previsão (ou o aviso)")
    @ApiResponse(responseCode = "400", description = "Coordenada ausente ou fora do planeta",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "404", description = "Não existe tarefa com esse id",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "502", description = "O serviço de previsão não respondeu",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/{id}/previsao")
    public TarefaComPrevisaoDTO buscarComPrevisao(
            @Parameter(description = "Id da tarefa", example = "1") @PathVariable Long id,
            @Parameter(description = "Latitude, de -90 a 90", example = "-28.6775") @RequestParam double latitude,
            @Parameter(description = "Longitude, de -180 a 180", example = "-49.3697") @RequestParam double longitude) {
        // Delega a composição para o service.
        return service.buscarComPrevisao(id, latitude, longitude);
    }
```

</details>

> O `PrevisaoService` não depende do `TarefaService`, então não há dependência circular: o Spring cria o `PrevisaoService` primeiro e o entrega ao `TarefaService`.

### Como testar

Cadastre duas tarefas pela `index.html` (ou pelo `POST /tarefas`): uma com prazo daqui a 3 dias e outra com prazo daqui a 30 dias. Anote os ids.

| chamada | resultado esperado |
|---|---|
| `GET /tarefas/{id}/previsao?latitude=-28.6775&longitude=-49.3697` da tarefa de **3 dias** | `200`: a tarefa, a previsão do dia do prazo e `"aviso": null` |
| A mesma chamada para a tarefa de **30 dias** | `200`: a tarefa, `"previsao": null` e um aviso |
| `GET /tarefas/999999/previsao?latitude=-28.6775&longitude=-49.3697` | `404` |
| Sem `latitude` | `400` |

Na `index.html`, cada tarefa ganha o botão ☁️, e a previsão do prazo aparece na caixa embaixo do campo de data.

### Subexercícios

**2.1**: Pedir ao `GET /previsao` uma data daqui a 30 dias responde `400`. Pedir ao `GET /tarefas/{id}/previsao` uma tarefa com prazo daqui a 30 dias responde `200` com aviso. Por que a mesma situação tem respostas diferentes nas duas rotas?

**2.2**: O `TarefaService` pede a previsão ao `PrevisaoService`, e não direto ao `OpenMeteoClient`. O que se perderia se ele chamasse o `OpenMeteoClient` direto?

---

## Exercício 3: Tempo esgotado vira 504

### O que você vai construir

Hoje, toda falha do Open-Meteo vira `502 Bad Gateway`. Mas são duas situações diferentes:

| Situação | O que aconteceu | Status certo |
|---|---|---|
| **Falhou** | O Open-Meteo está fora do ar, o endereço está errado ou ele respondeu com erro | `502 Bad Gateway` |
| **Demorou** | O Open-Meteo não respondeu dentro do tempo que a nossa API espera (`spring.http.client.read-timeout=5s`) | `504 Gateway Timeout` |

Neste exercício, o "demorou" passa a responder `504`, com a mensagem "demorou demais para responder". O "falhou" continua `502`.

### Como vai funcionar

1. Quando o tempo de espera estoura, o `RestClient` lança uma `RestClientException` cuja **causa** é uma `HttpTimeoutException` (do Java).
2. O método `indisponivel()` do `OpenMeteoClient`, que hoje transforma qualquer falha em `PrevisaoIndisponivelException`, passa a conferir a causa primeiro: se for `HttpTimeoutException`, cria uma **`PrevisaoTempoEsgotadoException`**.
3. A `PrevisaoTempoEsgotadoException` é **filha** da `PrevisaoIndisponivelException` (`extends`). Ou seja, ela também é uma "previsão indisponível", só que mais específica.
4. No `ApiExceptionHandler`, um tratador novo responde `504` para a filha. O Spring escolhe sempre o tratador **mais específico**: a filha vai para o `504`, e as outras falhas continuam no `502`.

### Como simular um Open-Meteo lento

O Open-Meteo responde rápido demais para passar de 5 segundos. Então o teste é ao contrário: diminua a paciência da nossa API. No `src/main/resources/application.properties`, troque **temporariamente**:

```properties
spring.http.client.read-timeout=1ms
```

Nenhuma resposta chega em 1 milissegundo, então toda chamada ao Open-Meteo vai estourar o tempo. **Lembre de voltar para `5s` depois do teste.**

### Passo a passo

**Passo 1. A exceção nova: crie o arquivo `client/PrevisaoTempoEsgotadoException.java`**

Uma classe que estende `PrevisaoIndisponivelException`, com o mesmo construtor `(String mensagem, Throwable causa)`. O construtor só repassa os dois valores para a classe mãe com `super(...)`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

```java
package api.client;

// Exercício 3: o serviço de previsão não respondeu dentro do read-timeout.
//
// É FILHA da PrevisaoIndisponivelException: continua sendo "o serviço de
// previsão falhou", só que de um jeito mais específico. Quem já trata a mãe
// continua funcionando. No ApiExceptionHandler, o Spring escolhe o tratador
// mais específico: esta vira 504, e as outras falhas continuam 502.
public class PrevisaoTempoEsgotadoException extends PrevisaoIndisponivelException {

    // Mesmo construtor da mãe: a mensagem e a exceção original.
    public PrevisaoTempoEsgotadoException(String mensagem, Throwable causa) {
        // Repassa para o construtor da PrevisaoIndisponivelException.
        super(mensagem, causa);
    }
}
```

</details>

**Passo 2. Reconhecer o tempo esgotado: mude o método `indisponivel()` no `client/OpenMeteoClient.java`**

Logo no começo do método, antes das verificações que já existem, confira se a **causa** da exceção é uma `HttpTimeoutException`. O `getCause()` devolve a exceção que provocou esta, e o `instanceof` pergunta "é desse tipo?". Se for, devolva a exceção nova; se não, o resto do método continua igual.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `OpenMeteoClient`, acrescente:

```java
import java.net.http.HttpTimeoutException;
```

E troque o método `indisponivel()` inteiro por este:

```java
    private PrevisaoIndisponivelException indisponivel(String operacao, RestClientException e) {
        // Exercício 3: tempo esgotado ganha exceção própria (504). Quando o
        // read-timeout estoura, a causa da exceção do RestClient é uma
        // HttpTimeoutException (do pacote java.net.http).
        if (e.getCause() instanceof HttpTimeoutException) {
            return new PrevisaoTempoEsgotadoException("Não foi possível " + operacao
                    + ": " + url + " demorou demais para responder", e);
        }
        // 1. O serviço RESPONDEU, mas com status de erro (4xx ou 5xx).
        if (e instanceof RestClientResponseException resposta) {
            return new PrevisaoIndisponivelException("Não foi possível " + operacao
                    + ": o serviço de previsão respondeu " + resposta.getStatusCode().value(), e);
        }
        // 2. Não chegou uma resposta válida (fora do ar, endereço errado...).
        String pista = e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName();
        return new PrevisaoIndisponivelException("Não foi possível " + operacao + ": sem resposta válida de "
                + url + " (" + pista + ")", e);
    }
```

</details>

**Passo 3. O status 504: crie um tratador no `controller/ApiExceptionHandler.java`**

Um método novo com `@ExceptionHandler(PrevisaoTempoEsgotadoException.class)`, igual ao tratador da `PrevisaoIndisponivelException` que já existe, trocando `HttpStatus.BAD_GATEWAY` (502) por `HttpStatus.GATEWAY_TIMEOUT` (504). **Não apague** o tratador do `502`: os dois convivem.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `ApiExceptionHandler`, acrescente:

```java
import api.client.PrevisaoTempoEsgotadoException;
```

E o método, dentro da classe (por exemplo, logo antes do método `responder()`):

```java
    // Exercício 3 -- 504 Gateway Timeout: o serviço de previsão não respondeu
    // a tempo. A PrevisaoTempoEsgotadoException também é uma
    // PrevisaoIndisponivelException, mas o Spring escolhe o tratador mais
    // específico: ela cai aqui, e as outras falhas continuam no 502.
    @ExceptionHandler(PrevisaoTempoEsgotadoException.class)
    public ResponseEntity<ErroDTO> tratarPrevisaoLenta(PrevisaoTempoEsgotadoException e, HttpServletRequest request) {
        // Mesmo formato de erro; só o status muda.
        return responder(HttpStatus.GATEWAY_TIMEOUT, e.getMessage(), request, List.of());
    }
```

</details>

**Passo 4. Documentar: acrescente o 504 no `controller/PrevisaoController.java`**

O Swagger só mostra os status que estão nas anotações. No método `buscar()`, acrescente um `@ApiResponse` para o `504`, logo abaixo do `@ApiResponse` do `502`. Se o Exercício 1 estiver feito, faça o mesmo no método `agora()`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Coloque estas linhas logo acima do `@GetMapping` do método `buscar()`:

```java
    // Exercício 3: o tempo esgotado tem status próprio.
    @ApiResponse(responseCode = "504", description = "O serviço de previsão demorou demais para responder",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
```

</details>

### Como testar

| teste | resultado esperado |
|---|---|
| `GET /previsao?...` com `read-timeout=1ms` | `504`, com a mensagem "demorou demais para responder" |
| `GET /previsao?...` com `read-timeout=5s` e `previsao.url=https://endereco-que-nao-existe.invalid` | `502`, como antes (a pista no fim da mensagem é `ConnectException`) |
| `GET /previsao?...` com tudo normal | `200` |
| Swagger UI → `GET /previsao` | Mostra `200`, `400`, `502` e `504` |

Depois dos testes, volte o `application.properties` ao normal: `read-timeout=5s` e `previsao.url=https://api.open-meteo.com`.

### Subexercícios

**3.1**: Por que a `PrevisaoTempoEsgotadoException` estende a `PrevisaoIndisponivelException`, e não a `RuntimeException`? Imagine um código que trate a falha do Open-Meteo com `catch (PrevisaoIndisponivelException e)`, devolvendo um aviso em vez de erro. O que aconteceria com ele num tempo esgotado, se a exceção nova estendesse a `RuntimeException`?

**3.2**: Se o `read-timeout` fosse de 60 segundos e o Open-Meteo ficasse lento, o que aconteceria com a NOSSA API quando 200 pessoas abrissem o cadastro ao mesmo tempo? Pense também nas rotas que nem usam o Open-Meteo, como o `GET /tarefas`.

---

## Exercício 4: Cache de 30 minutos

### O que você vai construir

A `index.html` pede a previsão a cada troca de data. Trocou para o dia 24, voltou para o 23, voltou para o 24: três chamadas ao Open-Meteo, e duas delas com uma pergunta que acabou de ser respondida. A previsão do tempo não muda de um minuto para o outro.

Neste exercício, a nossa API **guarda** cada previsão por 30 minutos e, nesse tempo, responde sem chamar o Open-Meteo. Guardar uma resposta para reaproveitar depois tem nome: **cache**.

Para quem chama ficar sabendo de onde a previsão veio, o `PrevisaoDTO` ganha um campo novo, `origem`:

```json
{ "data": "2026-11-03", "descricao": "Garoa", "...": "...", "origem": "open-meteo" }
```

- `"open-meteo"`: a previsão acabou de ser consultada no Open-Meteo;
- `"cache"`: a previsão veio da memória da API, sem chamar o Open-Meteo.

### Como vai funcionar

O cache é um **dicionário** (`Map`) dentro do `PrevisaoService`: para cada **chave** (o lugar e o dia), ele guarda a previsão e a hora em que ela chegou.

1. Chega um `GET /previsao`. O `PrevisaoService.buscar()` confere as regras, como antes.
2. Ele monta a chave: latitude e longitude com **2 casas decimais**, mais a data. Exemplo: `"-28.68;-49.37;2026-11-03"`.
3. Procura a chave no dicionário:
   - **achou, e foi guardada há menos de 30 minutos:** devolve a previsão guardada, com `origem` `"cache"`, e **não chama** o Open-Meteo;
   - **não achou, ou já passou de 30 minutos:** chama o Open-Meteo como antes, guarda a previsão no dicionário (com a hora de agora) e devolve, com `origem` `"open-meteo"`.

### Passo a passo

**Passo 1. O campo novo: troque o `dto/PrevisaoDTO.java` inteiro**

Duas mudanças no record:

1. um componente novo no fim, `String origem`;
2. um método `comOrigem(String novaOrigem)`, que devolve uma **cópia** da previsão com outra origem. Record é imutável (não dá para mudar um campo depois de criado), então para trocar a origem cria-se um record novo, com os mesmos valores e a origem nova.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

```java
package api.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

// A previsão de UM dia, como a NOSSA API devolve.
// Exercício 4: ganhou o campo "origem" e o método comOrigem().
@Schema(description = "Previsão do tempo de um dia, num lugar")
public record PrevisaoDTO(

        @Schema(description = "Dia da previsão (yyyy-MM-dd)", example = "2026-10-24")
        LocalDate data,

        @Schema(description = "Como vai estar o tempo, em português", example = "Chuva fraca")
        String descricao,

        @Schema(description = "Ícone (emoji) do tempo", example = "🌧️")
        String icone,

        @Schema(description = "Temperatura mínima, em °C", example = "17.0")
        Double temperaturaMinima,

        @Schema(description = "Temperatura máxima, em °C", example = "28.0")
        Double temperaturaMaxima,

        @Schema(description = "Maior chance de chuva no dia, em % (pode vir null)", example = "75")
        Integer chanceDeChuva,

        @Schema(description = "Vento mais forte do dia, em km/h", example = "15.9")
        Double ventoMaximo,

        @Schema(description = "Latitude usada pelo serviço de previsão", example = "-28.64")
        Double latitude,

        @Schema(description = "Longitude usada pelo serviço de previsão", example = "-49.36")
        Double longitude,

        // Exercício 4: de onde a previsão veio desta vez.
        @Schema(description = "open-meteo (consultou agora) ou cache (guardada há menos de 30 minutos)", example = "open-meteo")
        String origem

) {

    // Exercício 4: uma cópia desta previsão com outra origem. Record não deixa
    // mudar um campo (é imutável): em vez disso, cria-se um record novo.
    public PrevisaoDTO comOrigem(String novaOrigem) {
        return new PrevisaoDTO(data, descricao, icone, temperaturaMinima, temperaturaMaxima,
                chanceDeChuva, ventoMaximo, latitude, longitude, novaOrigem);
    }
}
```

</details>

**Passo 2. Preencher a origem: mude o `toPrevisao()` no `mapper/PrevisaoMapper.java`**

Assim que o `PrevisaoDTO` ganha um campo, **todo** `new PrevisaoDTO(...)` passa a precisar de um valor a mais. O compilador vai apontar o erro no `toPrevisao()` do mapper: é o único lugar que cria uma previsão. Quem chama o `toPrevisao()` acabou de receber a resposta do Open-Meteo, então a origem é `"open-meteo"`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Troque o método `toPrevisao()` inteiro por este (a única mudança é o `"open-meteo"` no fim):

```java
    public PrevisaoDTO toPrevisao(OpenMeteoRespostaDTO resposta, int indice) {
        // Atalho para não escrever resposta.daily() a cada linha.
        OpenMeteoRespostaDTO.Diario dia = resposta.daily();
        // O código do tempo vira texto e ícone.
        Integer codigo = dia.weather_code().get(indice);
        return new PrevisaoDTO(
                dia.time().get(indice),
                descricao(codigo),
                icone(codigo),
                dia.temperature_2m_min().get(indice),
                dia.temperature_2m_max().get(indice),
                dia.precipitation_probability_max().get(indice),
                dia.wind_speed_10m_max().get(indice),
                resposta.latitude(),
                resposta.longitude(),
                // Exercício 4: acabou de vir do Open-Meteo.
                "open-meteo");
    }
```

</details>

**Passo 3. O cache: mude o `service/PrevisaoService.java`**

Três partes:

1. **os imports** das classes novas (`Duration`, `Instant`, `Locale`, `Map` e `ConcurrentHashMap`);
2. **os campos do cache**, logo abaixo da constante `DIAS_DE_PREVISAO`:
   - `VALIDADE_DO_CACHE`, os 30 minutos;
   - um record `Guardada`, que junta a previsão com o instante (`Instant`) em que ela chegou;
   - o dicionário `cache`. Ele é um `ConcurrentHashMap`, e não um `HashMap`, porque várias requisições podem mexer nele ao mesmo tempo;
3. **o `buscar()`**, que passa a seguir os três passos do "Como vai funcionar".

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `PrevisaoService`, acrescente:

```java
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

Logo abaixo da linha `public static final int DIAS_DE_PREVISAO = 15;`, acrescente os campos do cache:

```java
    // Exercício 4: por quanto tempo uma previsão guardada ainda vale.
    private static final Duration VALIDADE_DO_CACHE = Duration.ofMinutes(30);

    // Exercício 4: uma previsão guardada e o instante em que ela chegou.
    private record Guardada(PrevisaoDTO previsao, Instant quando) {
    }

    // Exercício 4: o cache, um "dicionário" chave → previsão guardada. Fica na
    // memória da API e some quando ela reinicia. ConcurrentHashMap, e não
    // HashMap, porque várias requisições podem mexer nele ao mesmo tempo.
    private final Map<String, Guardada> cache = new ConcurrentHashMap<>();
```

E troque o método `buscar()` inteiro por este:

```java
    // GET /previsao -- a previsão de um dia, num lugar.
    public PrevisaoDTO buscar(double latitude, double longitude, LocalDate data) {
        // As regras vêm ANTES da chamada: pedido inválido não gasta uma
        // requisição ao Open-Meteo (e ele responderia 400 do jeito dele).
        validarCoordenadas(latitude, longitude);
        validarData(data);

        // Exercício 4: a mesma previsão foi pedida há menos de 30 minutos?
        // A chave junta as coordenadas com 2 casas (uns 1 km) e a data.
        // Locale.ROOT: ponto como separador decimal, em qualquer computador.
        String chave = String.format(Locale.ROOT, "%.2f;%.2f;%s", latitude, longitude, data);
        Guardada guardada = cache.get(chave);
        if (guardada != null && guardada.quando().plus(VALIDADE_DO_CACHE).isAfter(Instant.now())) {
            // Ainda vale: responde da memória, sem chamar o Open-Meteo.
            return guardada.previsao().comOrigem("cache");
        }

        // A chamada ao outro sistema: um período de um dia só.
        OpenMeteoRespostaDTO resposta = client.buscarPrevisao(latitude, longitude, data, data);

        // Dado de fora não é confiável: o serviço respondeu 200, mas sem o dia
        // pedido. Para quem chamou, é falha do serviço de previsão (502).
        if (resposta == null || resposta.daily() == null || resposta.daily().time() == null
                || resposta.daily().time().isEmpty()) {
            throw new PrevisaoIndisponivelException("O serviço de previsão respondeu sem a previsão de " + data, null);
        }

        // O dia pedido é o primeiro (e único) item de cada lista.
        PrevisaoDTO previsao = mapper.toPrevisao(resposta, 0);
        // Exercício 4: guarda no cache, com a hora de agora.
        cache.put(chave, new Guardada(previsao, Instant.now()));
        return previsao;
    }
```

</details>

> **Por que `Locale.ROOT`?** O `String.format("%.2f", ...)` usa o separador decimal do idioma do computador. Num Windows em português, `-28.68` viraria `-28,68`. Com `Locale.ROOT`, é sempre ponto.

### Como testar

| teste | resultado esperado |
|---|---|
| `GET /previsao?...` com uma data, pela primeira vez | `200` com `"origem": "open-meteo"` |
| O mesmo pedido de novo, logo depois | `200` com `"origem": "cache"`, e mais rápido |
| A mesma localização com **outra** data | `"origem": "open-meteo"` (outra chave) |
| Reiniciar a API e repetir o primeiro pedido | `"origem": "open-meteo"`: o cache fica na memória e some quando a API para |

Na `index.html`, ao voltar para uma data já escolhida, a caixa mostra "veio do cache da API", e o log não mostra a chamada ao Open-Meteo.

### Subexercícios

**4.1**: Trinta alunos, no mesmo laboratório, trocam a data do prazo 10 vezes cada um, entre os mesmos 3 dias. Quantas chamadas ao Open-Meteo a API de **cada aluno** faz sem o cache? E com o cache? Qual é o preço de usar o cache?

**4.2**: A chave do cache arredonda a latitude e a longitude para **2 casas**. O que aconteceria se usasse as coordenadas inteiras, com todas as casas que o navegador manda? E se arredondasse para **0 casas**?

---

## Exercício 5: Tarefas em dias de chuva

### O que você vai construir

Uma rota que responde: **quais tarefas pendentes têm prazo num dia de chuva**, nos próximos 16 dias?

```text
GET /tarefas/chuvosas?latitude=-28.6775&longitude=-49.3697
```

```json
[
  { "tarefa": { "id": 3, "titulo": "Entregar o trabalho", "dataPrazo": "2026-11-01", "...": "..." },
    "previsao": { "data": "2026-11-01", "descricao": "Garoa", "chanceDeChuva": 70, "...": "..." },
    "aviso": null }
]
```

"Dia de chuva" é um dia com **chance de chuva de 60% ou mais**. Cada item da lista tem o mesmo formato do Exercício 2 (`TarefaComPrevisaoDTO`).

### Uma chamada só, e não uma por tarefa

O jeito ingênuo seria pedir a previsão de cada tarefa, uma por uma: com 50 tarefas, 50 requisições pela internet. Mas o Open-Meteo deixa pedir **um período inteiro** numa chamada só (`start_date` e `end_date`), e o `buscarPrevisao()` do client já recebe as duas datas.

Então: **uma** chamada traz os 16 dias, e o cruzamento com as tarefas é feito na nossa API.

### Como vai funcionar

1. O `TarefaController` chama o `TarefaService.listarEmDiasDeChuva()`.
2. O `TarefaService` busca no banco as tarefas **pendentes** com prazo **de hoje até hoje + 15 dias**. Se não houver nenhuma, devolve uma lista vazia sem chamar o Open-Meteo.
3. Ele pede ao `PrevisaoService.buscarJanela()` a previsão dos 16 dias. Esse método faz **uma** chamada ao Open-Meteo e devolve um dicionário **dia → previsão**.
4. Para cada tarefa, o `TarefaService` procura no dicionário a previsão do dia do prazo e fica só com as que têm chance de chuva de 60% ou mais.

### Passo a passo

**Passo 1. A consulta no banco: crie um método no `repository/TarefaRepository.java`**

Uma consulta pelo nome do método (Aula 08). O nome diz tudo o que a consulta faz: pendentes (`ConcluidaFalse`), com prazo entre duas datas (`DataPrazoBetween`), em ordem de prazo (`OrderByDataPrazoAsc`). O Spring Data monta o SQL sozinho.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Dentro da interface, logo depois do último método:

```java
    // Exercício 5: WHERE concluida = false AND data_prazo BETWEEN ? AND ?
    // ORDER BY data_prazo -- as pendentes com prazo dentro do período.
    List<Tarefa> findByConcluidaFalseAndDataPrazoBetweenOrderByDataPrazoAsc(LocalDate inicio, LocalDate fim);
```

</details>

**Passo 2. Os 16 dias de uma vez: crie o método `buscarJanela()` no `service/PrevisaoService.java`**

O método chama o `buscarPrevisao()` do client com o período de hoje até hoje + 15. A resposta traz as listas do `daily` com 16 itens, e o dia `i` é o item `i` de cada lista. O mapper já sabe montar um `PrevisaoDTO` a partir de um índice (`toPrevisao(resposta, i)`), então o método só percorre os índices e guarda cada previsão no dicionário, usando a data como chave.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `PrevisaoService`, acrescente (o `Map` já veio no Exercício 4):

```java
import java.util.LinkedHashMap;
```

E o método, dentro da classe, logo depois do `buscar()`:

```java
    // Exercício 5: a previsão de TODOS os dias da janela (hoje + 15), com UMA
    // chamada só ao Open-Meteo. Devolve um "dicionário" dia → previsão.
    public Map<LocalDate, PrevisaoDTO> buscarJanela(double latitude, double longitude) {
        validarCoordenadas(latitude, longitude);
        LocalDate hoje = LocalDate.now();
        // Um período de 16 dias: cada lista do "daily" vem com 16 itens.
        OpenMeteoRespostaDTO resposta = client.buscarPrevisao(latitude, longitude, hoje, hoje.plusDays(DIAS_DE_PREVISAO));
        // Dado de fora não é confiável: sem os dias, é falha do serviço (502).
        if (resposta == null || resposta.daily() == null || resposta.daily().time() == null) {
            throw new PrevisaoIndisponivelException("O serviço de previsão respondeu sem os dias pedidos", null);
        }
        // LinkedHashMap: guarda os dias na ordem em que entraram.
        Map<LocalDate, PrevisaoDTO> porDia = new LinkedHashMap<>();
        for (int i = 0; i < resposta.daily().time().size(); i++) {
            // O dia i é o item i de cada lista: o mapper já sabe fazer isso.
            PrevisaoDTO previsao = mapper.toPrevisao(resposta, i);
            porDia.put(previsao.data(), previsao);
        }
        return porDia;
    }
```

</details>

**Passo 3. O cruzamento: crie o método `listarEmDiasDeChuva()` no `service/TarefaService.java`**

O método segue os passos 2 a 4 do "Como vai funcionar". O `TarefaService` já recebe o `PrevisaoService` desde o Exercício 2. O limite de 60% fica numa constante, `CHANCE_DE_CHUVA`, para não ser um número solto no meio do código.

O filtro usa `stream()`, como os mappers da Aula 09: o `.filter(...)` deixa passar só as tarefas cujo prazo tem chance de chuva de 60% ou mais, e o `.map(...)` transforma cada tarefa que passou num `TarefaComPrevisaoDTO`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Nos imports do `TarefaService`, acrescente:

```java
import java.util.Map;

import api.dto.PrevisaoDTO;
```

Logo abaixo da linha `private final PrevisaoService previsaoService;`, acrescente a constante:

```java
    // Aula 12, Exercício 5: chance de chuva a partir da qual o dia conta como "de chuva".
    public static final int CHANCE_DE_CHUVA = 60;
```

E o método, dentro da classe, logo depois do `buscarComPrevisao()`:

```java
    // Aula 12, Exercício 5: tarefas pendentes cujo prazo cai num dia de chuva.
    // UMA chamada ao Open-Meteo para todas, e não uma por tarefa.
    public List<TarefaComPrevisaoDTO> listarEmDiasDeChuva(double latitude, double longitude) {
        LocalDate hoje = LocalDate.now();
        // 1. Do banco: as pendentes com prazo dentro da janela da previsão.
        List<Tarefa> pendentes = repository.findByConcluidaFalseAndDataPrazoBetweenOrderByDataPrazoAsc(
                hoje, hoje.plusDays(PrevisaoService.DIAS_DE_PREVISAO));
        // Sem tarefa, nem chama o Open-Meteo.
        if (pendentes.isEmpty()) {
            return List.of();
        }
        // 2. Do Open-Meteo: os 16 dias de uma vez, num dicionário dia → previsão.
        Map<LocalDate, PrevisaoDTO> previsaoPorDia = previsaoService.buscarJanela(latitude, longitude);
        // 3. Cruza os dois: fica só quem tem chance de chuva de 60% ou mais no prazo.
        return pendentes.stream()
                .filter(tarefa -> {
                    PrevisaoDTO previsao = previsaoPorDia.get(tarefa.getDataPrazo());
                    return previsao != null && previsao.chanceDeChuva() != null
                            && previsao.chanceDeChuva() >= CHANCE_DE_CHUVA;
                })
                .map(tarefa -> new TarefaComPrevisaoDTO(mapper.toResponse(tarefa),
                        previsaoPorDia.get(tarefa.getDataPrazo()), null))
                .toList();
    }
```

</details>

**Passo 4. A rota: crie o método `listarEmDiasDeChuva()` no `controller/TarefaController.java`**

`@GetMapping("/chuvosas")`, com latitude e longitude como `@RequestParam`. O caminho `/chuvosas` é **literal**, como o `/atrasadas` que já existe, e por isso o Spring não o confunde com o `/{id}`.

<details>
<summary>💡 Código pronto (tente sozinho antes de abrir)</summary>

Dentro da classe (por exemplo, logo antes do `listarAtrasadas()`):

```java
    // Aula 12, Exercício 5: GET /tarefas/chuvosas?latitude=...&longitude=...
    @Operation(summary = "Tarefas pendentes com prazo em dia de chuva",
            description = "Prazo nos próximos 16 dias e chance de chuva de 60% ou mais. Uma chamada só ao Open-Meteo.")
    @ApiResponse(responseCode = "200", description = "Tarefas com a previsão do dia do prazo (pode vir vazia)")
    @ApiResponse(responseCode = "400", description = "Coordenada ausente ou fora do planeta",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "502", description = "O serviço de previsão não respondeu",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @GetMapping("/chuvosas")
    public List<TarefaComPrevisaoDTO> listarEmDiasDeChuva(
            @Parameter(description = "Latitude, de -90 a 90", example = "-28.6775") @RequestParam double latitude,
            @Parameter(description = "Longitude, de -180 a 180", example = "-49.3697") @RequestParam double longitude) {
        // Delega o cruzamento para o service.
        return service.listarEmDiasDeChuva(latitude, longitude);
    }
```

</details>

### Como testar

Cadastre algumas tarefas com prazos espalhados pelos próximos 15 dias e confira a chance de chuva de cada dia pelo `GET /previsao`.

| teste | resultado esperado |
|---|---|
| `GET /tarefas/chuvosas?latitude=-28.6775&longitude=-49.3697` | `200`, uma lista de `{tarefa, previsao, aviso}`, em ordem de prazo |
| Cada item da lista | Tarefa pendente; `previsao.data` igual ao `dataPrazo`; `chanceDeChuva` de 60 ou mais |
| Uma tarefa pendente que **não** veio na lista | O `GET /previsao` do prazo dela mostra chance de chuva menor que 60 (ou o prazo está fora dos 16 dias) |
| Sem nenhuma tarefa pendente nos próximos 16 dias | `200` com `[]`, sem chamar o Open-Meteo |

Na `index.html`, aparece o botão ☔ **Em dias de chuva** em cima da lista.

### Subexercícios

**5.1**: Com 50 tarefas pendentes, quantas chamadas ao Open-Meteo faria o jeito ingênuo (uma por tarefa), e quantas faz o jeito deste exercício? Além do número, o que mais piora no jeito ingênuo?

**5.2**: O `GET /tarefas/chuvosas` aproveita o cache do Exercício 4? Responda olhando a chave do cache e o pedido que o `buscarJanela()` faz ao Open-Meteo.
