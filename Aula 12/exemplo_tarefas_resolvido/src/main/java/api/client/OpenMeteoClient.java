// Define o pacote dos clientes de APIs externas.
package api.client;

// Importa a exceção de tempo esgotado do cliente HTTP do Java (Exercício 3).
import java.net.http.HttpTimeoutException;
// Importa o tipo de data do período pedido.
import java.time.LocalDate;
// Importa a interface "algo que devolve um valor", usada no método chamar().
import java.util.function.Supplier;

// Importa a anotação que lê um valor do application.properties.
import org.springframework.beans.factory.annotation.Value;
// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;
// Importa o cliente HTTP do Spring.
import org.springframework.web.client.RestClient;
// Importa a exceção-mãe de toda falha do RestClient.
import org.springframework.web.client.RestClientException;
// Importa a exceção de "o servidor respondeu, mas com status de erro".
import org.springframework.web.client.RestClientResponseException;

// >>> AULA 12: o CLIENTE do Open-Meteo. Até aqui a nossa API só RESPONDIA
// requisições. Esta classe faz a nossa API FAZER uma requisição para outra API:
//
//   Navegador ──HTTP──> Nossa API ──OpenMeteoClient──HTTP──> api.open-meteo.com
//                       (servidor)  (aqui ela é cliente)
//
// É o ÚNICO lugar do projeto que sabe:
//   - o endereço do Open-Meteo (previsao.url, no application.properties);
//   - o caminho (/v1/forecast), os nomes dos parâmetros e o formato do JSON;
//   - que a requisição é feita com o RestClient do Spring.
// O resto do projeto só chama os métodos daqui. Se um dia a previsão vier de
// outro serviço, só o pacote client muda.
//
// RestClient: o cliente HTTP do Spring Framework 6.1+ (Spring Boot 3.2+). Já
// vem no spring-boot-starter-web: nenhuma dependência nova no pom.xml.
// Registra a classe como bean para poder ser injetada no PrevisaoService.
@Component
public class OpenMeteoClient {

    // As medidas diárias que a nossa API usa, separadas por vírgula, do jeito
    // que o parâmetro "daily" do Open-Meteo espera.
    private static final String MEDIDAS_DIARIAS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max";

    // O endereço do Open-Meteo: vai para as mensagens de erro.
    private final String url;

    // O cliente HTTP, já apontado para o endereço do Open-Meteo.
    private final RestClient restClient;

    // O Spring injeta duas coisas:
    //   - RestClient.Builder: um "montador" de RestClient que o Spring Boot já
    //     deixa configurado com os timeouts (spring.http.client.*) e com o
    //     mesmo Jackson que a nossa API usa para ler e escrever JSON;
    //   - @Value("${previsao.url}"): o valor da linha previsao.url do application.properties.
    public OpenMeteoClient(RestClient.Builder builder, @Value("${previsao.url}") String url) {
        // Guarda o endereço para as mensagens.
        this.url = url;
        // baseUrl: o caminho usado abaixo ("/v1/forecast") é somado a este endereço.
        this.restClient = builder.baseUrl(url).build();
    }

    // GET /v1/forecast?latitude=...&longitude=...&daily=...&timezone=auto&start_date=...&end_date=...
    // A previsão dia a dia de um lugar, entre duas datas. Para um dia só,
    // inicio e fim são a mesma data.
    public OpenMeteoRespostaDTO buscarPrevisao(double latitude, double longitude, LocalDate inicio, LocalDate fim) {
        return chamar("buscar a previsão do tempo", () -> restClient.get()
                // Com muitos parâmetros, o UriBuilder fica mais legível que
                // um texto enorme: cada queryParam vira "&nome=valor" na URL,
                // já codificado. O builder começa do baseUrl.
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("daily", MEDIDAS_DIARIAS)
                        // timezone=auto: os dias são os do fuso do lugar, não os de Londres.
                        .queryParam("timezone", "auto")
                        // LocalDate vira "2026-10-24", o formato que o Open-Meteo espera.
                        .queryParam("start_date", inicio)
                        .queryParam("end_date", fim)
                        .build())
                // retrieve(): envia a requisição e recebe a resposta.
                .retrieve()
                // body(): converte o JSON da resposta no record.
                .body(OpenMeteoRespostaDTO.class));
    }

    // Exercício 1: GET /v1/forecast?latitude=...&longitude=...&current=...&timezone=auto
    // O tempo de agora. Mesmo caminho, outro parâmetro: "current" no lugar de "daily".
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

    // ---------------------------------------------------------------- falhas

    // Toda chamada passa por aqui. "chamada" é o código da requisição, escrito
    // como lambda -- () -> restClient.get()... --, o mesmo formato do
    // orElseThrow(() -> new ...) que os Services já usam. O <T> diz que o
    // método devolve o que a chamada devolver.
    //
    // Se o RestClient lançar qualquer exceção, ela vira PrevisaoIndisponivelException.
    private <T> T chamar(String operacao, Supplier<T> chamada) {
        try {
            // get(): executa a requisição de verdade e devolve o resultado.
            return chamada.get();
        } catch (RestClientException e) {
            // Troca a exceção do RestClient pela nossa.
            throw indisponivel(operacao, e);
        }
    }

    // Monta a PrevisaoIndisponivelException com uma mensagem que ajuda a
    // descobrir o problema. O RestClient falha de dois jeitos:
    private PrevisaoIndisponivelException indisponivel(String operacao, RestClientException e) {
        // Exercício 3: tempo esgotado ganha exceção própria (504). Com o
        // cliente HTTP do Java (o que o Spring Boot usa quando não há outra
        // biblioteca no pom.xml), o read-timeout estoura como HttpTimeoutException,
        // e o connect-timeout como HttpConnectTimeoutException, que é filha dela.
        if (e.getCause() instanceof HttpTimeoutException) {
            return new PrevisaoTempoEsgotadoException("Não foi possível " + operacao
                    + ": " + url + " demorou demais para responder", e);
        }
        // 1. O serviço RESPONDEU, mas com status de erro (4xx ou 5xx).
        // "instanceof ... resposta" (Java 16+): se a exceção for desse tipo,
        // ela já fica disponível na variável "resposta", com o tipo certo.
        if (e instanceof RestClientResponseException resposta) {
            return new PrevisaoIndisponivelException("Não foi possível " + operacao
                    + ": o serviço de previsão respondeu " + resposta.getStatusCode().value(), e);
        }
        // 2. Não chegou uma resposta válida: serviço fora do ar, endereço
        // errado, sem internet, tempo esgotado, JSON que não bate com o
        // record... O nome da exceção que causou esta (ConnectException,
        // HttpTimeoutException...) é a melhor pista do que aconteceu.
        String pista = e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName();
        return new PrevisaoIndisponivelException("Não foi possível " + operacao + ": sem resposta válida de "
                + url + " (" + pista + ")", e);
    }
}
