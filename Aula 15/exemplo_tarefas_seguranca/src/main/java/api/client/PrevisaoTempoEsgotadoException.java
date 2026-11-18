// Define o pacote dos clientes de APIs externas.
package api.client;

// Exercício 3: o serviço de previsão não respondeu dentro do
// spring.http.client.read-timeout (ou nem deixou conectar dentro do connect-timeout).
//
// É FILHA da PrevisaoIndisponivelException: continua sendo "o serviço de
// previsão falhou", só que de um jeito mais específico. Quem já trata a mãe
// continua funcionando sem mudar nada. No ApiExceptionHandler, o Spring
// escolhe o @ExceptionHandler mais específico: esta vira 504 Gateway Timeout,
// e as outras falhas continuam 502.
public class PrevisaoTempoEsgotadoException extends PrevisaoIndisponivelException {

    // Mesmo construtor da mãe: mensagem pronta e a exceção original.
    public PrevisaoTempoEsgotadoException(String mensagem, Throwable causa) {
        // Repassa para o construtor da PrevisaoIndisponivelException.
        super(mensagem, causa);
    }
}
