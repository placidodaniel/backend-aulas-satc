# Exercícios: Integração de Serviços

## Objetivo

Na aula, a nossa API aprendeu a consumir outra API: o `GET /previsao` chama o Open-Meteo e devolve a previsão do dia, traduzida. Nos exercícios ela passa a usar a previsão em mais lugares e a lidar melhor com o que pode dar errado **do outro lado**. Cada exercício treina uma coisa:

| Exercício | O que você pratica |
|---|---|
| 1. O tempo de agora | Fazer outra chamada à mesma API, com outro formato de resposta |
| 2. Previsão para o prazo da tarefa | Juntar dado próprio (a tarefa) com dado externo (a previsão) numa resposta só |
| 3. Tempo esgotado vira 504 | Separar "o serviço falhou" (502) de "o serviço demorou" (504) |
| 4. Cache de 30 minutos | Não perguntar de novo o que acabou de ser respondido |
| 5. Tarefas em dias de chuva | Buscar tudo de uma vez, em vez de uma chamada por tarefa |

## Como rodar

```bash
docker compose up -d          # PostgreSQL na porta 5439
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run

# em outro terminal (ou abra http://localhost:8080/ no navegador)
curl "http://localhost:8080/previsao?latitude=-28.6775&longitude=-49.3697&data=2026-10-24"
```

> **Atenção:** este projeto exige **JDK 25** e **internet** (a API consulta o Open-Meteo de verdade). Troque a data do exemplo por uma data entre hoje e daqui a 15 dias.

As coordenadas `-28.6775, -49.3697` são as da SATC, em Criciúma. Use as mesmas nos testes pelo terminal, pelo Postman e pelo Swagger.

## O que já vem pronto

Leia estas classes antes de começar. Os exercícios repetem os mesmos modelos:

| Classe | O que observar |
|---|---|
| `client/OpenMeteoClient.java` | O `RestClient` montado com o `baseUrl`; a chamada `get()`, `uri(...)` (com `queryParam` para cada parâmetro), `retrieve()`, `body(...)`; e o método `chamar()`, que troca qualquer exceção do `RestClient` por `PrevisaoIndisponivelException` |
| `client/OpenMeteoRespostaDTO.java` | O formato do Open-Meteo, com os nomes dele (`weather_code`, `temperature_2m_max`) e uma lista por medida |
| `mapper/PrevisaoMapper.java` | A tradução: o item `i` de cada lista vira um `PrevisaoDTO`; o código do tempo vira texto e ícone |
| `service/PrevisaoService.java` | As regras R1 a R3 **antes** da chamada, e a conferência de que a resposta trouxe o dia pedido |
| `controller/ApiExceptionHandler.java` | `RegraDeNegocioException` vira `400`; `PrevisaoIndisponivelException` vira `502` |

Com a API rodando, abra <http://localhost:8080/>. No topo, a barra **"Aula 12, segundo o /v3/api-docs"** mostra ⏳/✅ para cada exercício. A página muda sozinha conforme você implementa: a caixa da previsão ganha o tempo de agora (Exercício 1), cada tarefa ganha o botão ☁️ (Exercício 2), aparece "veio do cache" (Exercício 4) e surge o botão ☔ (Exercício 5). Depois de mudar o Java, reinicie a API e dê F5.

A página <http://localhost:8080/contrato.html> testa cada item destes exercícios e marca ✅ / ⏳ / ❌.

> **Faça os exercícios em ordem.** O 2 usa o que o 1 ensina sobre o Open-Meteo, e o 4 muda o mesmo `buscar()` que o 2 usa.

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

O `GET /previsao` diz como vai estar o tempo **num dia**. Neste exercício você cria `GET /previsao/agora`, que diz como está o tempo **neste momento**. É a mesma API do Open-Meteo, no mesmo caminho, com outro parâmetro, e com uma resposta em outro formato:

```text
https://api.open-meteo.com/v1/forecast?latitude=-28.6775&longitude=-49.3697&current=temperature_2m,wind_speed_10m,weather_code&timezone=auto
```

```json
{
  "latitude": -28.64675,
  "longitude": -49.36145,
  "current": { "time": "2026-10-21T23:15", "temperature_2m": 14.1, "wind_speed_10m": 11.9, "weather_code": 3 }
}
```

Abra esse endereço no navegador e compare com o `daily` da aula: aqui não há listas, porque "agora" é um momento só.

### O que fazer

1. Crie o record `client/OpenMeteoAgoraDTO`, no formato do Open-Meteo: `latitude`, `longitude` e `current`, com um record dentro (`Atual`) que tem `time` (`LocalDateTime`), `temperature_2m`, `wind_speed_10m` e `weather_code`. Use o `OpenMeteoRespostaDTO` como modelo.
2. No `OpenMeteoClient`, crie `buscarAgora(double latitude, double longitude)`, igual ao `buscarPrevisao()`, trocando o `daily` (e as datas) por `current=temperature_2m,wind_speed_10m,weather_code`.
3. Crie o record `dto/AgoraDTO` com `horario`, `descricao`, `icone`, `temperatura` e `vento`, documentado com `@Schema`.
4. No `PrevisaoMapper`, crie `toAgora(OpenMeteoAgoraDTO resposta)`, reaproveitando os métodos `descricao()` e `icone()` que já existem.
5. No `PrevisaoService`, crie `agora(double latitude, double longitude)`: confere a R1 (não há data aqui), chama o client e, se o `current` vier `null`, lança `PrevisaoIndisponivelException`.
6. No `PrevisaoController`, crie o `@GetMapping("/agora")`, documentado com `200`, `400` e `502`.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// client/OpenMeteoAgoraDTO.java
public record OpenMeteoAgoraDTO(Double latitude, Double longitude, Atual current) {
    public record Atual(LocalDateTime time, Double temperature_2m, Double wind_speed_10m, Integer weather_code) {
    }
}
```

```java
// OpenMeteoClient
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
            .body(OpenMeteoAgoraDTO.class));
}
```

```java
// PrevisaoMapper
public AgoraDTO toAgora(OpenMeteoAgoraDTO resposta) {
    OpenMeteoAgoraDTO.Atual atual = resposta.current();
    return new AgoraDTO(atual.time(), descricao(atual.weather_code()), icone(atual.weather_code()),
            atual.temperature_2m(), atual.wind_speed_10m());
}
```

O `PrevisaoController` já tem o `@RequestMapping("/previsao")`: o novo método só precisa de `@GetMapping("/agora")`.

</details>

### Resultado esperado

| chamada | resultado |
|---|---|
| `GET /previsao/agora?latitude=-28.6775&longitude=-49.3697` | `200` com `horario`, `descricao`, `icone`, `temperatura` e `vento` |
| `GET /previsao/agora?latitude=999&longitude=-49.3697` | `400` (R1), sem chamar o Open-Meteo |
| `GET /previsao/agora?longitude=-49.3697` | `400`: parâmetro obrigatório ausente |

Na `index.html`, a caixa da previsão ganha uma linha a mais: "Agora: ☁️ Nublado, 14°, vento 12 km/h".

### Subexercícios

**1.1**: O Open-Meteo devolve o `daily` como um objeto com **uma lista por medida**, e a nossa API devolve **um objeto por dia**. Por que a nossa API não repassa as listas do jeito que vieram?

**1.2**: O Open-Meteo aceita ser chamado direto pelo navegador. Por que, então, a página pede a previsão à **nossa** API, que pede ao Open-Meteo? O que se ganha com esse passo a mais?

---

## Exercício 2: Previsão para o prazo da tarefa

Toda tarefa tem um prazo. Neste exercício você cria `GET /tarefas/{id}/previsao`, que devolve a tarefa **e** a previsão do tempo para o dia do prazo dela, numa resposta só. A tarefa vem do **nosso** banco; a previsão vem do Open-Meteo. Integrar é isso: juntar o que é nosso com o que é dos outros.

E se o prazo for daqui a 30 dias, ou já tiver passado? Não há previsão para esse dia, mas a tarefa existe. O pedido não deve falhar: a resposta traz a tarefa, `"previsao": null` e um `"aviso"` explicando. Isso é um **fallback**: uma resposta alternativa quando uma parte não está disponível.

### O que fazer

1. Crie o record `dto/TarefaComPrevisaoDTO` com `TarefaResponseDTO tarefa`, `PrevisaoDTO previsao` e `String aviso`. É um DTO composto, como o `ResponsavelDetalheDTO` da Aula 09.
2. No `PrevisaoService`:
   - deixe o `validarCoordenadas()` **público**;
   - crie `boolean temPrevisao(LocalDate data)`: `true` quando a data está entre hoje e hoje + 15 dias.
3. No `TarefaService`, receba o `PrevisaoService` no construtor e crie `buscarComPrevisao(Long id, double latitude, double longitude)`:
   - confere as coordenadas (R1);
   - busca a tarefa (`404` se não existir);
   - se o prazo **não** tem previsão: devolve a tarefa, `previsao` `null` e um aviso ("O prazo já passou" ou "Ainda não há previsão: ela só cobre até <data>");
   - se tem: devolve a tarefa e o `previsaoService.buscar(latitude, longitude, prazo)`, com `aviso` `null`.
4. No `TarefaController`, crie o `@GetMapping("/{id}/previsao")`, com `latitude` e `longitude` como `@RequestParam`. Documente `200`, `400`, `404` e `502`.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// PrevisaoService
public boolean temPrevisao(LocalDate data) {
    LocalDate hoje = LocalDate.now();
    return !data.isBefore(hoje) && !data.isAfter(hoje.plusDays(DIAS_DE_PREVISAO));
}
```

```java
// TarefaService
public TarefaComPrevisaoDTO buscarComPrevisao(Long id, double latitude, double longitude) {
    previsaoService.validarCoordenadas(latitude, longitude);
    Tarefa tarefa = buscarEntidade(id);
    TarefaResponseDTO dto = mapper.toResponse(tarefa);
    LocalDate prazo = tarefa.getDataPrazo();
    if (!previsaoService.temPrevisao(prazo)) {
        String aviso = prazo.isBefore(LocalDate.now())
                ? "O prazo (" + prazo + ") já passou: não há previsão para dias passados"
                : "Ainda não há previsão para o prazo (" + prazo + "): ela só cobre até "
                        + LocalDate.now().plusDays(PrevisaoService.DIAS_DE_PREVISAO);
        return new TarefaComPrevisaoDTO(dto, null, aviso);
    }
    return new TarefaComPrevisaoDTO(dto, previsaoService.buscar(latitude, longitude, prazo), null);
}
```

O `PrevisaoService` não depende do `TarefaService`, então não há dependência circular: o Spring cria o `PrevisaoService` primeiro e entrega ao `TarefaService`.

</details>

### Resultado esperado

| chamada | resultado |
|---|---|
| `GET /tarefas/{id}/previsao?latitude=...&longitude=...` de uma tarefa com prazo daqui a 3 dias | `200`: a tarefa, a previsão do dia do prazo e `"aviso": null` |
| A mesma chamada para uma tarefa com prazo daqui a 30 dias | `200`: a tarefa, `"previsao": null` e um aviso |
| `GET /tarefas/999999/previsao?...` | `404` |
| Sem `latitude` | `400` |

Na `index.html`, cada tarefa ganha o botão ☁️, e a previsão do prazo aparece na caixa embaixo do campo de data.

### Subexercícios

**2.1**: Pedir ao `GET /previsao` uma data daqui a 30 dias responde `400`. Pedir ao `GET /tarefas/{id}/previsao` uma tarefa com prazo daqui a 30 dias responde `200` com aviso. Por que a mesma situação tem respostas diferentes nas duas rotas?

**2.2**: O `TarefaService` pede a previsão ao `PrevisaoService`, e não direto ao `OpenMeteoClient`. O que se perderia se ele chamasse o `OpenMeteoClient` direto?

---

## Exercício 3: Tempo esgotado vira 504

Hoje, o Open-Meteo fora do ar e o Open-Meteo lento dão a mesma resposta: `502`. Para quem chama a nossa API, são situações diferentes. Fora do ar, o serviço recusou a conexão ou respondeu erro. Lento, ele pode até ter recebido o pedido, mas não respondeu dentro do tempo que a nossa API espera (`spring.http.client.read-timeout=5s`). O HTTP tem um status para cada caso: **`502 Bad Gateway`** e **`504 Gateway Timeout`**.

### Como simular um Open-Meteo lento

O Open-Meteo responde rápido demais para passar de 5 segundos. Então o teste é ao contrário: diminua a paciência da nossa API. No `application.properties`, troque temporariamente para:

```properties
spring.http.client.read-timeout=1ms
```

Nenhuma resposta chega em 1 milissegundo. **Lembre de voltar para `5s` depois.**

### O que fazer

1. Crie `client/PrevisaoTempoEsgotadoException`, **filha** da `PrevisaoIndisponivelException` (`extends PrevisaoIndisponivelException`), com o mesmo construtor `(String mensagem, Throwable causa)`.
2. No `OpenMeteoClient.indisponivel()`, antes das outras verificações: se a causa da exceção for um `HttpTimeoutException` (`e.getCause() instanceof HttpTimeoutException`, do pacote `java.net.http`), devolva uma `PrevisaoTempoEsgotadoException` com a mensagem `"... demorou demais para responder"`.
3. No `ApiExceptionHandler`, crie um `@ExceptionHandler(PrevisaoTempoEsgotadoException.class)` que responde `HttpStatus.GATEWAY_TIMEOUT` (`504`).
4. Documente o `504` nas rotas que chamam o Open-Meteo: pelo menos o `GET /previsao`.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// client/PrevisaoTempoEsgotadoException.java
public class PrevisaoTempoEsgotadoException extends PrevisaoIndisponivelException {
    public PrevisaoTempoEsgotadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
```

```java
// OpenMeteoClient.indisponivel() -- import java.net.http.HttpTimeoutException;
if (e.getCause() instanceof HttpTimeoutException) {
    return new PrevisaoTempoEsgotadoException("Não foi possível " + operacao
            + ": " + url + " demorou demais para responder", e);
}
```

```java
// ApiExceptionHandler
@ExceptionHandler(PrevisaoTempoEsgotadoException.class)
public ResponseEntity<ErroDTO> tratarPrevisaoLenta(PrevisaoTempoEsgotadoException e, HttpServletRequest request) {
    return responder(HttpStatus.GATEWAY_TIMEOUT, e.getMessage(), request, List.of());
}
```

O tratador do `502` continua lá. Quando a exceção é uma `PrevisaoTempoEsgotadoException`, os dois serviriam, e o Spring escolhe o mais específico: o da filha.

</details>

### Resultado esperado

| teste | resultado |
|---|---|
| `GET /previsao?...` com `read-timeout=1ms` | `504`, com a mensagem "demorou demais para responder" |
| `GET /previsao?...` com `previsao.url=https://endereco-que-nao-existe.invalid` | `502`, como antes (a pista no fim da mensagem é `ConnectException`) |
| `GET /previsao?...` com tudo normal | `200` |
| Swagger UI → `GET /previsao` | Mostra `200`, `400`, `502` e `504` |

### Subexercícios

**3.1**: Por que a `PrevisaoTempoEsgotadoException` estende a `PrevisaoIndisponivelException`, e não a `RuntimeException`? Imagine um código que trate a falha do Open-Meteo com `catch (PrevisaoIndisponivelException e)`, devolvendo um aviso em vez de erro. O que aconteceria com ele num tempo esgotado, se a exceção nova estendesse a `RuntimeException`?

**3.2**: Se o `read-timeout` fosse de 60 segundos e o Open-Meteo ficasse lento, o que aconteceria com a NOSSA API quando 200 pessoas abrissem o cadastro ao mesmo tempo? Pense também nas rotas que nem usam o Open-Meteo, como o `GET /tarefas`.

---

## Exercício 4: Cache de 30 minutos

A `index.html` pede a previsão a cada troca de data. Trocou para o dia 24, voltou para o 23, voltou para o 24: três chamadas ao Open-Meteo, duas delas com uma pergunta que acabou de ser respondida. A previsão do tempo não muda de um minuto para o outro.

Neste exercício a nossa API **guarda** cada previsão por 30 minutos (um **cache**) e, nesse tempo, responde sem chamar o Open-Meteo. Para quem chama ficar sabendo, a previsão ganha o campo `origem`: `"open-meteo"` quando acabou de ser consultada, `"cache"` quando veio da memória.

### O que fazer

1. No `PrevisaoDTO`, acrescente o componente `String origem` no fim (documentado com `@Schema`) e um método `comOrigem(String novaOrigem)`, que devolve uma cópia do record com outra origem.
2. No `PrevisaoMapper.toPrevisao()`, preencha a origem com `"open-meteo"`.
3. No `PrevisaoService`:
   - crie um `record Guardada(PrevisaoDTO previsao, Instant quando)` dentro da classe;
   - crie o cache: `Map<String, Guardada> cache = new ConcurrentHashMap<>()`;
   - no `buscar()`, depois das regras: monte a chave com a latitude e a longitude arredondadas para 2 casas e a data; se a chave estiver no cache há menos de 30 minutos, devolva a previsão com `comOrigem("cache")`, sem chamar o Open-Meteo; senão, chame, guarde no cache e devolva.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// PrevisaoDTO -- o record ganha um componente e um método
        Double longitude,
        String origem
) {
    public PrevisaoDTO comOrigem(String novaOrigem) {
        return new PrevisaoDTO(data, descricao, icone, temperaturaMinima, temperaturaMaxima,
                chanceDeChuva, ventoMaximo, latitude, longitude, novaOrigem);
    }
}
```

```java
// PrevisaoService
private static final Duration VALIDADE_DO_CACHE = Duration.ofMinutes(30);
private record Guardada(PrevisaoDTO previsao, Instant quando) { }
private final Map<String, Guardada> cache = new ConcurrentHashMap<>();

// no buscar(), depois de validarCoordenadas() e validarData():
String chave = String.format(Locale.ROOT, "%.2f;%.2f;%s", latitude, longitude, data);
Guardada guardada = cache.get(chave);
if (guardada != null && guardada.quando().plus(VALIDADE_DO_CACHE).isAfter(Instant.now())) {
    return guardada.previsao().comOrigem("cache");
}
// ... chama o Open-Meteo e monta a "previsao", como antes ...
cache.put(chave, new Guardada(previsao, Instant.now()));
return previsao;
```

`Locale.ROOT` faz o `%.2f` usar ponto como separador decimal em qualquer computador. `ConcurrentHashMap`, e não `HashMap`, porque várias requisições podem mexer no cache ao mesmo tempo.

</details>

### Resultado esperado

| teste | resultado |
|---|---|
| `GET /previsao?...` pela primeira vez | `200` com `"origem": "open-meteo"` |
| O mesmo pedido de novo, logo depois | `200` com `"origem": "cache"`, mais rápido, e nenhuma chamada nova ao Open-Meteo |
| Outra data | `"origem": "open-meteo"` |
| Reiniciar a API e pedir de novo | `"origem": "open-meteo"`: o cache fica na memória e some quando a API para |

Na `index.html`, a caixa mostra "veio do cache da API", e o log não mostra a chamada ao Open-Meteo.

### Subexercícios

**4.1**: Trinta alunos, no mesmo laboratório, trocam a data do prazo 10 vezes cada um, entre os mesmos 3 dias. Quantas chamadas ao Open-Meteo a API de **cada aluno** faz sem o cache? E com o cache? Qual é o preço de usar o cache?

**4.2**: A chave do cache arredonda a latitude e a longitude para **2 casas**. O que aconteceria se usasse as coordenadas inteiras, com todas as casas que o navegador manda? E se arredondasse para **0 casas**?

---

## Exercício 5: Tarefas em dias de chuva

Quais tarefas pendentes têm prazo num dia de chuva? Neste exercício você cria `GET /tarefas/chuvosas`, que responde isso para os próximos 16 dias.

O jeito ingênuo seria chamar o `GET /previsao` uma vez para cada tarefa. Com 50 tarefas, 50 requisições pela internet. O Open-Meteo deixa pedir **um período inteiro** numa chamada só (`start_date` e `end_date`), e o `buscarPrevisao()` do client já recebe as duas datas. Então: uma chamada para os 16 dias, e o cruzamento com as tarefas é feito na nossa API.

### O que fazer

1. No `TarefaRepository`, crie `findByConcluidaFalseAndDataPrazoBetweenOrderByDataPrazoAsc(LocalDate inicio, LocalDate fim)`: as pendentes com prazo no período, em ordem de prazo.
2. No `PrevisaoService`, crie `Map<LocalDate, PrevisaoDTO> buscarJanela(double latitude, double longitude)`: confere a R1, chama o `buscarPrevisao()` de hoje até hoje + 15 e transforma cada dia da resposta num `PrevisaoDTO` (o item `i` de cada lista), guardando num mapa dia → previsão.
3. No `TarefaService`, crie `listarEmDiasDeChuva(double latitude, double longitude)`:
   - busca as pendentes com prazo de hoje até hoje + 15;
   - sem nenhuma, devolve lista vazia **sem chamar o Open-Meteo**;
   - com o mapa do `buscarJanela()`, fica só com as que têm `chanceDeChuva` de **60 ou mais** no dia do prazo;
   - devolve cada uma como `TarefaComPrevisaoDTO` (o do Exercício 2), com `aviso` `null`.
4. No `TarefaController`, crie o `@GetMapping("/chuvosas")`. **Atenção:** `/chuvosas` é um caminho literal, como `/atrasadas`, e por isso não conflita com o `/{id}`.

<details>
<summary>💡 Dica de código (tente sozinho antes de abrir)</summary>

```java
// PrevisaoService
public Map<LocalDate, PrevisaoDTO> buscarJanela(double latitude, double longitude) {
    validarCoordenadas(latitude, longitude);
    LocalDate hoje = LocalDate.now();
    OpenMeteoRespostaDTO resposta = client.buscarPrevisao(latitude, longitude, hoje, hoje.plusDays(DIAS_DE_PREVISAO));
    Map<LocalDate, PrevisaoDTO> porDia = new LinkedHashMap<>();
    for (int i = 0; i < resposta.daily().time().size(); i++) {
        PrevisaoDTO previsao = mapper.toPrevisao(resposta, i);
        porDia.put(previsao.data(), previsao);
    }
    return porDia;
}
```

```java
// TarefaService
public List<TarefaComPrevisaoDTO> listarEmDiasDeChuva(double latitude, double longitude) {
    LocalDate hoje = LocalDate.now();
    List<Tarefa> pendentes = repository.findByConcluidaFalseAndDataPrazoBetweenOrderByDataPrazoAsc(
            hoje, hoje.plusDays(PrevisaoService.DIAS_DE_PREVISAO));
    if (pendentes.isEmpty()) {
        return List.of();
    }
    Map<LocalDate, PrevisaoDTO> previsaoPorDia = previsaoService.buscarJanela(latitude, longitude);
    return pendentes.stream()
            .filter(tarefa -> {
                PrevisaoDTO previsao = previsaoPorDia.get(tarefa.getDataPrazo());
                return previsao != null && previsao.chanceDeChuva() != null && previsao.chanceDeChuva() >= 60;
            })
            .map(tarefa -> new TarefaComPrevisaoDTO(mapper.toResponse(tarefa),
                    previsaoPorDia.get(tarefa.getDataPrazo()), null))
            .toList();
}
```

</details>

### Resultado esperado

| teste | resultado |
|---|---|
| `GET /tarefas/chuvosas?latitude=...&longitude=...` | `200`, lista de `{tarefa, previsao, aviso}`, em ordem de prazo |
| Cada item | Tarefa pendente; `previsao.data` igual ao `dataPrazo`; `chanceDeChuva` de 60 ou mais |
| Conferir pelo `GET /previsao` o prazo de uma tarefa pendente que **não** veio | A chance de chuva daquele dia é menor que 60 (ou o prazo está fora dos 16 dias) |
| Sem nenhuma tarefa pendente nos próximos 16 dias | `200` com `[]`, sem chamar o Open-Meteo |

Na `index.html`, aparece o botão ☔ **Em dias de chuva** em cima da lista.

### Subexercícios

**5.1**: Com 50 tarefas pendentes, quantas chamadas ao Open-Meteo faria o jeito ingênuo (uma por tarefa), e quantas faz o jeito deste exercício? Além do número, o que mais piora no jeito ingênuo?

**5.2**: O `GET /tarefas/chuvosas` aproveita o cache do Exercício 4? Responda olhando a chave do cache e o pedido que o `buscarJanela()` faz ao Open-Meteo.
