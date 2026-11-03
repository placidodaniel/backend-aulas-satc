package api.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// A observabilidade também é comportamento da aplicação, e também se testa:
// se alguém remover uma dependência ou um endpoint, o Prometheus fica cego sem ninguém perceber.
class ObservabilidadeE2ETest extends AbstractE2ETest {

    @Test
    @DisplayName("/actuator/health: UP, com banco e RabbitMQ")
    void health() {
        ResponseEntity<String> r = http.getForEntity("/actuator/health", String.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody()).contains("\"status\":\"UP\"").contains("\"db\"").contains("\"rabbit\"");
    }

    @Test
    @DisplayName("/actuator/prometheus expõe as métricas HTTP e as nossas, com a etiqueta da aplicação")
    void metricasPrometheus() {
        http.getForEntity("/tarefas", String.class);
        http.postForEntity("/tarefas", java.util.Map.of("titulo", "m", "responsavel", "Ana",
                "dataPrazo", java.time.LocalDate.now().toString(), "prioridade", 1), String.class);

        String texto = http.getForEntity("/actuator/prometheus", String.class).getBody();

        assertThat(texto)
                .contains("http_server_requests_seconds_count")
                .contains("uri=\"/tarefas\"")
                .contains("tarefas_eventos_publicados_total")
                .contains("jvm_memory_used_bytes")
                .contains("application=\"exemplo-tarefas\"");
    }

    @Test
    @DisplayName("toda resposta traz o X-Request-Id, e um id enviado pelo cliente é reaproveitado")
    void requisicaoId() {
        var semId = http.getForEntity("/tarefas", String.class);
        assertThat(semId.getHeaders().getFirst("X-Request-Id")).isNotBlank();

        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-Request-Id", "meu-id-123");
        var comId = http.exchange("/tarefas", org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers), String.class);
        assertThat(comId.getHeaders().getFirst("X-Request-Id")).isEqualTo("meu-id-123");
    }
}
