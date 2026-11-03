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
// Importa a exceção de parâmetro obrigatório ausente na URL (Aula 12).
import org.springframework.web.bind.MissingServletRequestParameterException;
// Importa a anotação que marca um método como tratador de exceção.
import org.springframework.web.bind.annotation.ExceptionHandler;
// Importa a anotação que aplica o tratador a todos os controllers.
import org.springframework.web.bind.annotation.RestControllerAdvice;
// Importa a exceção de parâmetro da URL que não converte (Aula 12).
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Importa a requisição HTTP, de onde sai o "caminho" do erro.
import jakarta.servlet.http.HttpServletRequest;

// Importa as exceções do serviço de previsão (Aula 12).
import api.client.PrevisaoIndisponivelException;
import api.client.PrevisaoTempoEsgotadoException;
// Importa os DTOs de erro (Exercício 5).
import api.dto.CampoErroDTO;
import api.dto.ErroDTO;
// Importa a exceção de regra de negócio (Aula 12).
import api.service.RegraDeNegocioException;
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

    // Aula 12 -- 400 de parâmetro ausente na URL: GET /previsao sem latitude,
    // por exemplo. Sem este tratador, o Spring responderia com o JSON padrão dele.
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroDTO> tratarParametroAusente(MissingServletRequestParameterException e, HttpServletRequest request) {
        // getParameterName(): o nome do parâmetro que faltou.
        return responder(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + e.getParameterName(), request,
                List.of(new CampoErroDTO(e.getParameterName(), "é obrigatório")));
    }

    // Aula 12 -- 400 de parâmetro da URL que não converte: latitude=abc ou
    // data=amanhã. Acontece antes do Controller rodar, como o JSON ilegível.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDTO> tratarParametroInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        // getName(): o nome do parâmetro; o valor recusado não volta (pode ser qualquer coisa).
        return responder(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + e.getName()
                + " (números com ponto, datas no formato yyyy-MM-dd)", request,
                List.of(new CampoErroDTO(e.getName(), "valor em formato inválido")));
    }

    // Aula 12 -- 400 de regra de negócio: o pedido veio no formato certo, mas
    // o sistema não pode aceitar (ex.: previsão para daqui a 30 dias).
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroDTO> tratarRegraDeNegocio(RegraDeNegocioException e, HttpServletRequest request) {
        // A mensagem já explica a regra; não há erro de campo.
        return responder(HttpStatus.BAD_REQUEST, e.getMessage(), request, List.of());
    }

    // Aula 12 -- 502 Bad Gateway: a nossa API funcionou, quem falhou foi o
    // serviço de previsão, que ela chamou. Não é 400 (o cliente não errou) nem
    // 500 (não é bug nosso). A mensagem diz o que se tentou fazer e a pista do
    // motivo (o status que o serviço respondeu, ou o tipo da falha).
    @ExceptionHandler(PrevisaoIndisponivelException.class)
    public ResponseEntity<ErroDTO> tratarPrevisaoIndisponivel(PrevisaoIndisponivelException e, HttpServletRequest request) {
        // Responde no mesmo formato de todo erro da API.
        return responder(HttpStatus.BAD_GATEWAY, e.getMessage(), request, List.of());
    }

    // Aula 12, Exercício 3 -- 504 Gateway Timeout: o serviço de previsão não
    // respondeu a tempo. PrevisaoTempoEsgotadoException é filha da
    // PrevisaoIndisponivelException; o Spring escolhe o tratador mais
    // específico, então ela cai aqui, e as outras falhas continuam no 502.
    @ExceptionHandler(PrevisaoTempoEsgotadoException.class)
    public ResponseEntity<ErroDTO> tratarPrevisaoLenta(PrevisaoTempoEsgotadoException e, HttpServletRequest request) {
        // Mesmo formato de erro; só o status muda.
        return responder(HttpStatus.GATEWAY_TIMEOUT, e.getMessage(), request, List.of());
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
