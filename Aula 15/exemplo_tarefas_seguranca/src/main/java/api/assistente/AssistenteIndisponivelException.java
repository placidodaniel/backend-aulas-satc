// Pacote do assistente da tela (Aula 14).
package api.assistente;

// O servidor de modelos (Ollama ou OpenRouter) falhou, demorou demais ou respondeu algo inesperado.
// O ApiExceptionHandler transforma em 502 Bad Gateway, como o Open-Meteo na Aula 12:
// o erro não é nosso nem do cliente, é do serviço de fora.
public class AssistenteIndisponivelException extends RuntimeException {

    public AssistenteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public AssistenteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
