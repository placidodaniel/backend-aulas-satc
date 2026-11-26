package api.observability;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
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

    // Aula 16: qual CÓPIA da API respondeu. Com várias cópias atrás do proxy, é por este
    // cabeçalho que se vê o rodízio acontecendo (F12 > Network, ou curl -i).
    public static final String CABECALHO_INSTANCIA = "X-Instancia";

    private static final Logger log = LoggerFactory.getLogger(RequisicaoIdFilter.class);

    // O nome da cópia (application.properties: infra.instancia). No Docker, é o id do container.
    private final String instancia;

    public RequisicaoIdFilter(@Value("${infra.instancia:local}") String instancia) {
        this.instancia = instancia;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String id = request.getHeader(CABECALHO);
        // Aula 15: o cabeçalho vem do CLIENTE, então não confiamos nele. Só aceitamos
        // letras, números e hífen, até 36 caracteres (um UUID). Sem isso, alguém mandaria
        // um id gigante ou com caracteres estranhos que iria parar em TODA linha de log.
        if (id == null || !id.matches("[A-Za-z0-9-]{1,36}")) {
            id = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(CHAVE_MDC, id);
        response.setHeader(CABECALHO, id);
        response.setHeader(CABECALHO_INSTANCIA, instancia);
        long inicio = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            registrarAcesso(request, response, inicio);
            // A thread volta para o pool: sem limpar, o id vazaria para a próxima requisição.
            MDC.remove(CHAVE_MDC);
        }
    }

    // Uma linha de log por requisição, em TODA rota da API: método, rota (com o padrão, ex.: /tarefas/{id}),
    // status e duração. O /actuator fica de fora: o Prometheus consulta a cada 5 s e só faria ruído.
    private void registrarAcesso(HttpServletRequest request, HttpServletResponse response, long inicio) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/actuator")) {
            return;
        }
        Object padrao = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        log.info("{} {} -> {} ({} ms)", request.getMethod(), padrao != null ? padrao : uri, response.getStatus(), ms);
    }
}
