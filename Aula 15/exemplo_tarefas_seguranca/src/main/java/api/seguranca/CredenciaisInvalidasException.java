package api.seguranca;

// Aula 15: e-mail, senha, código ou desafio errado. Vira 401 no ApiExceptionHandler.
//
// A mensagem é sempre GENÉRICA ("e-mail ou senha inválidos"): dizer "este
// e-mail não existe" ensinaria a quem ataca quais e-mails têm conta
// (enumeração de usuários).
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
