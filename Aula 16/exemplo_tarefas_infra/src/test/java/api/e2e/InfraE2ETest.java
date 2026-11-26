package api.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// Aula 16: o que a INFRAESTRUTURA espera da API também se testa.
// O Docker, o proxy e o Prometheus dependem destas respostas: se alguém desligar uma
// delas sem querer, o container nunca fica "healthy" e o sistema não sobe.
class InfraE2ETest extends AbstractE2ETest {

    @Test
    @DisplayName("liveness e readiness respondem UP, sem token (o Docker e o proxy não têm token)")
    void sondasDeSaude() {
        // Sem o token do usuário de teste: um cliente "anônimo", como o HEALTHCHECK.
        var anonimo = new RestTemplate();
        String base = "http://localhost:" + porta;

        ResponseEntity<String> vivo = anonimo.getForEntity(base + "/actuator/health/liveness", String.class);
        ResponseEntity<String> pronto = anonimo.getForEntity(base + "/actuator/health/readiness", String.class);

        assertThat(vivo.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(vivo.getBody()).contains("\"status\":\"UP\"");
        assertThat(pronto.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pronto.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("toda resposta diz qual cópia da API respondeu (X-Instancia)")
    void cabecalhoDaInstancia() {
        ResponseEntity<String> r = http.getForEntity("/tarefas", String.class);

        assertThat(r.getHeaders().getFirst("X-Instancia")).isNotBlank();
    }

    @Test
    @DisplayName("atrás do proxy em HTTPS, a API monta os endereços com https (X-Forwarded-*)")
    void cabecalhosDoProxy() {
        // É isto que o Caddy manda quando o navegador abriu https://localhost:8443
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.set("X-Forwarded-Proto", "https");
        cabecalhos.set("X-Forwarded-Port", "8443");

        ResponseEntity<String> contrato = http.exchange("/v3/api-docs", HttpMethod.GET,
                new HttpEntity<>(cabecalhos), String.class);

        // O Swagger usa este endereço para fazer as chamadas do "Try it out".
        assertThat(contrato.getBody()).contains("https://localhost:8443");
    }
}
