// Define o pacote da camada de serviço.
package api.service;

// CAMADA: Service
// RESPONSABILIDADE: avisar que um empréstimo não existe. Herda de
//                   RecursoNaoEncontradoException, por isso vira 404.
// SITUAÇÃO: implementada.
public class EmprestimoNaoEncontradoException extends RecursoNaoEncontradoException {

    // Cria a exceção usando o id que não foi encontrado.
    public EmprestimoNaoEncontradoException(Long id) {
        // Envia a mensagem que vai aparecer no campo "mensagem" do ErroDTO.
        super("Empréstimo não encontrado: " + id);
    }
}
