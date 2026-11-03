package api.observability;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Dá a cada requisição um id e o coloca no MDC do SLF4J: todo log escrito durante a requisição
// (controller, service, client) sai com o mesmo id (veja logging.pattern.level).
// Se o chamador já mandou X-Request-Id, reaproveita; devolve o id no mesmo cabeçalho da resposta.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequisicaoIdFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Request-Id";
    public static final String CHAVE_MDC = "requisicaoId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id = request.getHeader(CABECALHO);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(CHAVE_MDC, id);
        response.setHeader(CABECALHO, id);
        try {
            chain.doFilter(request, response);
        } finally {
            // A thread volta para o pool: sem limpar, o id vazaria para a próxima requisição.
            MDC.remove(CHAVE_MDC);
        }
    }
}
