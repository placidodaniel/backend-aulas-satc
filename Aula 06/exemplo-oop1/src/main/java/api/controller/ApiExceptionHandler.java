// Este arquivo pertence ao pacote "api.controller" e mora em src/main/java/api/controller/.
// Nome completo desta classe: api.controller.ApiExceptionHandler
//
// Declara o pacote; a pasta api/controller/ precisa existir e ter exatamente este nome.
package api.controller;

// HttpStatus: enum com os códigos HTTP conhecidos (200, 404, 500...) -- em vez de
// escrever o número 404 na mão, usa-se HttpStatus.NOT_FOUND, mais legível.
import org.springframework.http.HttpStatus;

// ResponseEntity: dá controle total sobre a resposta HTTP (status + corpo + headers),
// diferente de simplesmente devolver um objeto (como o Controller faz em
// consultarSaldo()), onde o status vem sempre 200 por padrão.
import org.springframework.http.ResponseEntity;

// @ExceptionHandler e @RestControllerAdvice: as duas anotações que ligam esta classe
// a QUALQUER exceção lançada por QUALQUER @RestController do projeto.
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// A exceção específica que este método sabe tratar.
import api.service.ContaNaoEncontradaException;

// >>> SPRING: @RestControllerAdvice marca esta classe como um "interceptador global"
// de exceções -- não fica presa a UM controller específico, vale para todos. Sem ela,
// uma ContaNaoEncontradaException lançada lá no service subiria sem tratamento até o
// Spring, que devolveria 500 Internal Server Error -- tecnicamente correto (o pedido
// falhou), mas semanticamente errado: a conta não existir não é um erro DO SERVIDOR,
// é o cliente pedindo um recurso que não está lá -- por isso o certo é 404.
@RestControllerAdvice
public class ApiExceptionHandler {

    // >>> SPRING: @ExceptionHandler(ContaNaoEncontradaException.class) diz "quando
    // ESTA exceção (ou uma subclasse dela) escapar de qualquer controller, chame este
    // método em vez de deixar o Spring devolver o 500 padrão".
    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<String> tratarContaNaoEncontrada(ContaNaoEncontradaException e) {

        // ResponseEntity.status(...).body(...): monta a resposta na mão -- status
        // 404 e, como corpo, a própria mensagem guardada na exceção (montada lá em
        // ContaNaoEncontradaException: "Conta não encontrada: <numero>").
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

    // Fim de tratarContaNaoEncontrada().
    }

// Fim da classe ApiExceptionHandler.
}
