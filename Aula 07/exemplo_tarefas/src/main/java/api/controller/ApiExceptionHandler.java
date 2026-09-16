package api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import api.service.TarefaNaoEncontradaException;

// Trata exceções de qualquer controller da API, num único lugar.
@RestControllerAdvice
public class ApiExceptionHandler {

    // Roda sempre que essa exceção escapar de um controller; devolve 404 em vez do
    // 500 padrão do Spring.
    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<String> tratarTarefaNaoEncontrada(TarefaNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
