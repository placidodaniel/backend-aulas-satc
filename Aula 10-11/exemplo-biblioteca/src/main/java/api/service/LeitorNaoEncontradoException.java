// Define o pacote da camada de serviço.
package api.service;

// CAMADA: Service
// RESPONSABILIDADE: avisar que um leitor não existe. Herda de
//                   RecursoNaoEncontradoException, por isso vira 404.
// SITUAÇÃO: implementada.
public class LeitorNaoEncontradoException extends RecursoNaoEncontradoException {

    // Cria a exceção usando o id que não foi encontrado.
    public LeitorNaoEncontradoException(Long id) {
        // Envia a mensagem que vai aparecer no campo "mensagem" do ErroDTO.
        super("Leitor não encontrado: " + id);
    }
}
