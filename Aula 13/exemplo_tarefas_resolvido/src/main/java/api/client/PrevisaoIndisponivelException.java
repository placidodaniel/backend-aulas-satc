// Define o pacote dos clientes de APIs externas.
package api.client;

// Aula 12: o serviço de previsão do tempo não respondeu, ou respondeu com erro.
//
// De quem é a culpa? Não é do cliente da nossa API (então não é 400), e não é
// um bug do nosso código (então não é 500). Quem falhou foi o serviço de que a
// nossa API depende. Para isso o HTTP tem o 502 Bad Gateway: "eu sou um
// intermediário, e quem estava atrás de mim falhou". O ApiExceptionHandler
// faz essa tradução.
//
// O OpenMeteoClient lança esta exceção no lugar das exceções do RestClient:
// assim, o Service e o ApiExceptionHandler não precisam saber que existe RestClient.
public class PrevisaoIndisponivelException extends RuntimeException {

    // Cria a exceção com a mensagem pronta e guarda a exceção original
    // ("causa"), que continua aparecendo no stack trace do console.
    public PrevisaoIndisponivelException(String mensagem, Throwable causa) {
        // Envia a mensagem para o tratador de erros e encadeia a causa.
        super(mensagem, causa);
    }
}
