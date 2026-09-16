package api.service;

// Exceção própria para "tarefa não existe" -- o ApiExceptionHandler usa o tipo
// dela para saber que a resposta certa é 404.
public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(Long id) {
        super("Tarefa não encontrada: " + id);
    }
}
