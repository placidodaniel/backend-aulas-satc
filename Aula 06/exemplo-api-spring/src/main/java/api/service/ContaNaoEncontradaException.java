// Este arquivo pertence ao pacote "api.service" e mora em src/main/java/api/service/.
// Nome completo desta classe: api.service.ContaNaoEncontradaException
//
// Declara o pacote; a pasta api/service/ precisa existir e ter exatamente este nome.
package api.service;

// >>> HERANÇA + EXCEÇÃO: "extends RuntimeException" cria um tipo de exceção NOVO e
// PRÓPRIO deste projeto, em vez de usar algo genérico como IllegalArgumentException --
// assim, o ApiExceptionHandler consegue capturar SÓ este erro específico (conta não
// encontrada), sem acidentalmente capturar qualquer outro RuntimeException do sistema.
//
// RuntimeException (e não Exception) = UNCHECKED: quem chama buscarPorNumero() não é
// obrigado a escrever try/catch -- o compilador não cobra. Quem trata de verdade é o
// ApiExceptionHandler, mais abaixo na pilha de chamadas, no pacote api.controller.
public class ContaNaoEncontradaException extends RuntimeException {

    // >>> CONSTRUTOR: só monta a mensagem de erro e repassa para a classe mãe.
    public ContaNaoEncontradaException(String numero) {

        // super(...) chama o construtor de RuntimeException que guarda a mensagem --
        // é o texto que volta em e.getMessage(), lá no ApiExceptionHandler.
        super("Conta não encontrada: " + numero);

    // Fim do construtor.
    }

// Fim da classe ContaNaoEncontradaException.
}
