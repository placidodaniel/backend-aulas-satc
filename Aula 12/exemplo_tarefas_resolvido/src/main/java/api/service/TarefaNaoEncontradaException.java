// Define o pacote da camada de serviço.
package api.service;

// Exceção própria para "tarefa não existe" -- o ApiExceptionHandler usa o tipo
// dela para saber que a resposta certa é 404.
public class TarefaNaoEncontradaException extends RuntimeException {

    // Cria a exceção usando o id que não foi encontrado.
    public TarefaNaoEncontradaException(Long id) {
        // Envia uma mensagem explicativa para o tratador de erros.
        super("Tarefa não encontrada: " + id);
    }
}
