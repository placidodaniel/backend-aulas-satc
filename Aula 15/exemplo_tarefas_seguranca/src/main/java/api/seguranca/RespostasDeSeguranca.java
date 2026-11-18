package api.seguranca;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.dto.ErroDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Aula 15: as respostas 401 e 403 no MESMO formato de erro da API (ErroDTO).
//
// Estes erros acontecem nos FILTROS do Spring Security, ANTES de a requisição
// chegar a qualquer Controller. Por isso o ApiExceptionHandler (@RestControllerAdvice)
// não os enxerga, e precisamos deste tratador separado.
//
//   401 Unauthorized: "não sei quem você é" (sem token, token vencido ou adulterado).
//   403 Forbidden:    "sei quem você é, mas isto você não pode" (ex.: usar o
//                     token de desafio do 2FA para listar tarefas).
@Component
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    // Os tratadores padrão do Spring: escrevem o cabeçalho WWW-Authenticate: Bearer ...,
    // que diz ao cliente COMO se autenticar. Nós só acrescentamos o corpo em JSON.
    private final BearerTokenAuthenticationEntryPoint padrao401 = new BearerTokenAuthenticationEntryPoint();
    private final BearerTokenAccessDeniedHandler padrao403 = new BearerTokenAccessDeniedHandler();
    private final ObjectMapper json;

    public RespostasDeSeguranca(ObjectMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException erro) throws IOException {
        padrao401.commence(request, response, erro);
        String mensagem = erro instanceof InvalidBearerTokenException
                ? "Token inválido ou expirado. Entre de novo para receber outro."
                : "É preciso entrar no sistema: envie o token no cabeçalho Authorization: Bearer <token>.";
        escrever(response, HttpStatus.UNAUTHORIZED, mensagem, request);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException erro) throws IOException {
        padrao403.handle(request, response, erro);
        escrever(response, HttpStatus.FORBIDDEN, "Este token não dá acesso a esta rota.", request);
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String mensagem,
                          HttpServletRequest request) throws IOException {
        ErroDTO erro = new ErroDTO(status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getOutputStream(), erro);
    }
}
