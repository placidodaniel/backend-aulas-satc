// Define o pacote da camada de serviço.
package api.service;

// Classe-mãe de todo "não encontrado" do projeto -- o ApiExceptionHandler usa o
// tipo dela para saber que a resposta certa é 404.
//
// Cada entidade do grupo ganha a sua exceção, herdando desta (Aula 03-04):
//
//   public class LivroNaoEncontradoException extends RecursoNaoEncontradoException {
//       public LivroNaoEncontradoException(Long id) {
//           super("Livro não encontrado: " + id);
//       }
//   }
//
// Como o handler trata a classe-mãe, uma exceção nova já sai como 404 sem
// ninguém precisar mexer no ApiExceptionHandler.
public class RecursoNaoEncontradoException extends RuntimeException {

    // Cria a exceção com a mensagem que vai aparecer no campo "mensagem" do ErroDTO.
    public RecursoNaoEncontradoException(String mensagem) {
        // Envia a mensagem explicativa para o tratador de erros.
        super(mensagem);
    }
}
