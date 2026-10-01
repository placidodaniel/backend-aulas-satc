// Define o pacote dos controllers e seus tratadores de erro.
package api.controller;

// Importa o charset usado no corpo de texto.
import java.nio.charset.StandardCharsets;

// Importa o enum com os status HTTP conhecidos pelo Spring.
import org.springframework.http.HttpStatus;
// Importa as constantes de tipo de conteúdo (Content-Type).
import org.springframework.http.MediaType;
// Importa o tipo usado para montar uma resposta HTTP completa.
import org.springframework.http.ResponseEntity;
// Importa a anotação que marca um método como tratador de exceção.
import org.springframework.web.bind.annotation.ExceptionHandler;
// Importa a anotação que aplica o tratador a todos os controllers.
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Importa a exceção de responsável inexistente.
import api.service.ResponsavelNaoEncontradoException;
// Importa a exceção de tarefa inexistente.
import api.service.TarefaNaoEncontradaException;

// Trata exceções de qualquer controller da API, num único lugar.
//
// Os 404 continuam em texto puro, como na Aula 08. Repare que cada tipo de
// erro sai num formato diferente: o 404 é texto ("Tarefa não encontrada: 1"),
// o 400 do @Valid é o JSON padrão do Spring (timestamp, status, errors...).
// O cliente precisa de dois jeitos de ler erro -- o Exercício 5 padroniza os
// dois com um DTO de erro.
// Registra esta classe para tratar exceções lançadas pelos controllers.
@RestControllerAdvice
public class ApiExceptionHandler {

    // Content-Type fixo dos 404: texto puro em UTF-8. Sem ele, se o cliente
    // pedir "Accept: application/json" (o Swagger UI pede), a String sairia
    // rotulada como JSON sem ser JSON; e sem o charset, o "ã" de "não"
    // aparece quebrado quando o navegador abre a URL direto.
    private static final MediaType TEXTO_UTF8 = new MediaType("text", "plain", StandardCharsets.UTF_8);

    // Roda sempre que essa exceção escapar de um controller; devolve 404 em vez do
    // 500 padrão do Spring.
    // Diz ao Spring qual exceção deve entrar neste método.
    @ExceptionHandler(TarefaNaoEncontradaException.class)
    // Recebe a exceção e devolve uma resposta HTTP com texto.
    public ResponseEntity<String> tratarTarefaNaoEncontrada(TarefaNaoEncontradaException e) {
        // Monta o status 404 e usa a mensagem da exceção como corpo.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(TEXTO_UTF8).body(e.getMessage());
    }

    // Mesmo tratamento da tarefa -- 404 com a mensagem em texto puro.
    @ExceptionHandler(ResponsavelNaoEncontradoException.class)
    // Recebe a exceção e devolve uma resposta HTTP com texto.
    public ResponseEntity<String> tratarResponsavelNaoEncontrado(ResponsavelNaoEncontradoException e) {
        // Monta o status 404 e usa a mensagem da exceção como corpo.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(TEXTO_UTF8).body(e.getMessage());
    }
}
