// Pacote do assistente da tela (Aula 14).
package api.assistente;

// Importa o tempo de espera e as listas.
import java.time.Duration;
import java.util.List;

// Importa o que o Spring usa para ler a configuração e montar o cliente HTTP.
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

// =====================================================================
// AULA 14: o cliente HTTP do servidor de modelos de IA.
// =====================================================================
//
// É o MESMO padrão do OpenMeteoClient da Aula 12: um RestClient apontado
// para a API de fora, e os erros de lá traduzidos em uma exceção nossa.
//
// Para qual servidor ele aponta? Depende da configuração (application.properties
// ou variáveis de ambiente):
//   - PADRÃO: o Ollama, que roda o modelo num container do docker-compose.
//     Não precisa de chave nem de conta. URL: http://localhost:11434/v1
//   - OPCIONAL: o OpenRouter, na nuvem, com modelos maiores. Precisa de chave.
//     ASSISTENTE_URL=https://openrouter.ai/api/v1 e ASSISTENTE_CHAVE=sk-or-...
//
// Dois cuidados:
//   1. A CHAVE (quando existe) vai no cabeçalho "Authorization: Bearer <chave>".
//      Ela vem de uma variável de ambiente e NUNCA vai para o navegador nem para o Git.
//   2. TEMPO: um modelo pode levar dezenas de segundos para responder, ainda
//      mais rodando no processador do computador. O tempo de leitura global
//      da aplicação (5 s, Aula 12) é curto demais: este cliente usa 3 minutos.
@Component
public class ModeloClient {

    private final RestClient restClient;
    private final String url;
    private final String chave;
    private final String modeloPadrao;

    public ModeloClient(RestClient.Builder builder,
                        @Value("${assistente.url}") String url,
                        @Value("${assistente.chave:}") String chave,
                        @Value("${assistente.modelo}") String modeloPadrao) {
        this.url = url;
        this.chave = chave;
        this.modeloPadrao = modeloPadrao;

        // Fábrica de conexões só deste cliente, com tempos próprios.
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofMinutes(3));

        RestClient.Builder configurado = builder.baseUrl(url).requestFactory(fabrica);
        // Só manda o cabeçalho de autenticação se houver chave (o Ollama não usa).
        if (chave != null && !chave.isBlank()) {
            configurado = configurado.defaultHeader("Authorization", "Bearer " + chave);
        }
        this.restClient = configurado.build();
    }

    public String url() {
        return url;
    }

    public String modeloPadrao() {
        return modeloPadrao;
    }

    // GET /models: quais modelos o servidor tem. Só os que aceitam ferramentas,
    // quando o servidor informa isso (o OpenRouter informa; o Ollama, não).
    public List<String> listarModelos() {
        try {
            ChatCompletions.ListaDeModelos lista = restClient.get().uri("/models")
                    .retrieve().body(ChatCompletions.ListaDeModelos.class);
            if (lista == null || lista.data() == null) {
                return List.of();
            }
            return lista.data().stream()
                    .filter(m -> m.supportedParameters() == null || m.supportedParameters().contains("tools"))
                    .map(ChatCompletions.InfoModelo::id)
                    .sorted()
                    .toList();
        } catch (RestClientException e) {
            throw traduzir(e);
        }
    }

    // POST /chat/completions: manda a conversa e as ferramentas; devolve o que o modelo gerou.
    public ChatCompletions.Resposta conversar(String modelo, List<ChatCompletions.Mensagem> mensagens,
                                              List<ChatCompletions.Ferramenta> ferramentas) {
        ChatCompletions.Pedido pedido = new ChatCompletions.Pedido(modelo, mensagens,
                ferramentas.isEmpty() ? null : ferramentas);
        try {
            ChatCompletions.Resposta resposta = restClient.post()
                    .uri("/chat/completions")
                    .body(pedido)
                    .retrieve()
                    .body(ChatCompletions.Resposta.class);

            // Alguns servidores respondem 200 com um "error" dentro, ou sem escolhas.
            if (resposta == null || resposta.choices() == null || resposta.choices().isEmpty()) {
                String motivo = resposta != null && resposta.error() != null
                        ? resposta.error().message() : "resposta sem conteúdo";
                throw new AssistenteIndisponivelException("O servidor de modelos não devolveu resposta: " + motivo);
            }
            return resposta;
        } catch (RestClientException e) {
            throw traduzir(e);
        }
    }

    // Erros do lado de lá viram a nossa exceção (502), com uma mensagem que ajuda a resolver.
    private AssistenteIndisponivelException traduzir(RestClientException e) {
        if (e instanceof RestClientResponseException resposta) {
            // Exemplos: 404 = modelo não baixado/inexistente; 401 = chave errada (OpenRouter);
            // 429 = limite de uso; 400 = o modelo não aceita ferramentas.
            return new AssistenteIndisponivelException("O servidor de modelos (" + url + ") respondeu "
                    + resposta.getStatusCode().value() + ": " + resposta.getResponseBodyAsString(), e);
        }
        if (e instanceof ResourceAccessException) {
            return new AssistenteIndisponivelException("Não foi possível falar com o servidor de modelos em " + url
                    + ". Ele está rodando? (docker compose --profile ia up -d)", e);
        }
        return new AssistenteIndisponivelException("Resposta inesperada do servidor de modelos: " + e.getMessage(), e);
    }
}
