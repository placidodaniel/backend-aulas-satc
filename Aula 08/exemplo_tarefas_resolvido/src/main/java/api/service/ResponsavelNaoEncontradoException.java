// Define o pacote da camada de serviço.
package api.service;

// Exercício 6: exceção própria para "responsável não existe" -- o
// ApiExceptionHandler usa o tipo dela para responder 404.
public class ResponsavelNaoEncontradoException extends RuntimeException {

    // Cria a exceção usando o id que não foi encontrado.
    public ResponsavelNaoEncontradoException(Long id) {
        // Mensagem exata pedida pelo contrato.
        super("Responsável não encontrado: " + id);
    }
}
