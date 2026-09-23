// Define o pacote dos controllers e seus tratadores de erro.
package api.controller;

// Importa o enum com os status HTTP conhecidos pelo Spring.
import org.springframework.http.HttpStatus;
// Importa o tipo usado para montar uma resposta HTTP completa.
import org.springframework.http.ResponseEntity;
// Importa a anotação que marca um método como tratador de exceção.
import org.springframework.web.bind.annotation.ExceptionHandler;
// Importa a anotação que aplica o tratador a todos os controllers.
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Importa a exceção de tarefa inexistente.
import api.service.TarefaNaoEncontradaException;

// Trata exceções de qualquer controller da API, num único lugar.
// Registra esta classe para tratar exceções lançadas pelos controllers.
@RestControllerAdvice
public class ApiExceptionHandler {

    // Roda sempre que essa exceção escapar de um controller; devolve 404 em vez do
    // 500 padrão do Spring.
    // Diz ao Spring qual exceção deve entrar neste método.
    @ExceptionHandler(TarefaNaoEncontradaException.class)
    // Recebe a exceção e devolve uma resposta HTTP com texto.
    public ResponseEntity<String> tratarTarefaNaoEncontrada(TarefaNaoEncontradaException e) {
        // Monta o status 404 e usa a mensagem da exceção como corpo.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
