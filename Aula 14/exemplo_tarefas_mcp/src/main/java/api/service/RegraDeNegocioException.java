// Define o pacote da camada de serviço.
package api.service;

// Aula 12: exceção para uma regra de negócio violada -- o ApiExceptionHandler
// usa o tipo dela para saber que a resposta certa é 400. É a mesma classe do
// template do Projeto Final (Aulas 10 e 11).
//
// O Service lança esta exceção quando o pedido é válido no formato, mas o
// sistema não pode aceitá-lo. Exemplo do PrevisaoService:
//
//   if (data.isBefore(hoje)) {
//       throw new RegraDeNegocioException("A previsão é para hoje em diante");
//   }
//
// A mensagem vai para o campo "mensagem" do ErroDTO: escreva para o usuário
// entender qual regra barrou o pedido.
public class RegraDeNegocioException extends RuntimeException {

    // Cria a exceção com a mensagem que explica a regra violada.
    public RegraDeNegocioException(String mensagem) {
        // Envia a mensagem explicativa para o tratador de erros.
        super(mensagem);
    }
}
