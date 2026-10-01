// Define o pacote dos controllers e seus tratadores de erro.
package api.controller;

// Importa o tipo de data e hora usado no timestamp do erro.
import java.time.LocalDateTime;
// Importa a unidade usada para cortar os nanossegundos do timestamp.
import java.time.temporal.ChronoUnit;
// Importa o comparador usado para ordenar os erros por campo.
import java.util.Comparator;
// Importa a lista de erros por campo.
import java.util.List;

// Importa o enum com os status HTTP conhecidos pelo Spring.
import org.springframework.http.HttpStatus;
// Importa o tipo usado para montar uma resposta HTTP completa.
import org.springframework.http.ResponseEntity;
// Importa a exceção lançada quando o corpo não é um JSON legível.
import org.springframework.http.converter.HttpMessageNotReadableException;
// Importa a exceção lançada quando o @Valid reprova o DTO.
import org.springframework.web.bind.MethodArgumentNotValidException;
// Importa a anotação que marca um método como tratador de exceção.
import org.springframework.web.bind.annotation.ExceptionHandler;
// Importa a anotação que aplica o tratador a todos os controllers.
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Importa a requisição HTTP, de onde sai o "caminho" do erro.
import jakarta.servlet.http.HttpServletRequest;

// Importa os DTOs de erro (Exercício 5).
import api.dto.CampoErroDTO;
import api.dto.ErroDTO;
// Importa as exceções de "não encontrado".
import api.service.ResponsavelNaoEncontradoException;
import api.service.TarefaNaoEncontradaException;

// Trata exceções de qualquer controller da API, num único lugar.
//
// Exercício 5: toda resposta de erro sai como ErroDTO. O ApiExceptionHandler
// passa a ser o "mapper dos erros": recebe uma exceção (mundo Java) e devolve
// um DTO (mundo do contrato).
//
// Cuidado para não criar um @ExceptionHandler(Exception.class) "pega-tudo":
// ele também capturaria os 404 e 405 que o próprio Spring gera para rotas que
// não existem, e tudo viraria 500.
// Registra esta classe para tratar exceções lançadas pelos controllers.
@RestControllerAdvice
public class ApiExceptionHandler {

    // 404: um método só para as duas exceções -- a resposta é a mesma, só a
    // mensagem muda (e a mensagem já vem pronta de cada exceção).
    @ExceptionHandler({TarefaNaoEncontradaException.class, ResponsavelNaoEncontradoException.class})
    public ResponseEntity<ErroDTO> tratarNaoEncontrado(RuntimeException e, HttpServletRequest request) {
        // Sem erros de campo: a lista vai vazia, mas vai (o campo sempre existe).
        return responder(HttpStatus.NOT_FOUND, e.getMessage(), request, List.of());
    }

    // 400 de validação: o @Valid reprovou o DTO antes do Controller rodar.
    // Antes deste exercício, quem respondia era o Spring, com o JSON padrão dele.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> tratarValidacao(MethodArgumentNotValidException e, HttpServletRequest request) {
        // Cada FieldError vira um CampoErroDTO {campo, mensagem}.
        List<CampoErroDTO> campos = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErroDTO(erro.getField(), erro.getDefaultMessage()))
                // Ordem fixa (por campo): o Bean Validation não garante ordem nenhuma.
                .sorted(Comparator.comparing(CampoErroDTO::campo).thenComparing(CampoErroDTO::mensagem))
                .toList();
        // A mensagem geral é fixa; o detalhe de cada campo está na lista.
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos: confira a lista de campos", request, campos);
    }

    // 400 de JSON ilegível: vírgula faltando, data em formato errado
    // ("dataPrazo": "amanhã"), texto onde deveria ser número...
    // Acontece ANTES do @Valid: o Jackson nem conseguiu montar o DTO.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> tratarCorpoIlegivel(HttpMessageNotReadableException e, HttpServletRequest request) {
        // e.getMessage() traria detalhes internos do Jackson -- não vai para o cliente.
        return responder(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido: confira o JSON e o formato das datas (yyyy-MM-dd)", request, List.of());
    }

    // Monta o ErroDTO e a resposta HTTP -- um lugar só, igual a um mapper.
    private ResponseEntity<ErroDTO> responder(HttpStatus status, String mensagem, HttpServletRequest request,
                                              List<CampoErroDTO> campos) {
        // getReasonPhrase(): "Not Found", "Bad Request"... O timestamp é cortado
        // nos segundos: "2026-10-06T19:30:00", sem os nanossegundos do relógio.
        ErroDTO erro = new ErroDTO(status.value(), status.getReasonPhrase(), mensagem,
                request.getRequestURI(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), campos);
        // O status vai na linha de status HTTP e também dentro do corpo.
        return ResponseEntity.status(status).body(erro);
    }
}
