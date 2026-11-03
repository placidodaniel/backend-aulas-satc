package api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

// Testa o cliente HTTP SEM internet: o MockRestServiceServer faz o papel do Open-Meteo,
// confere a requisição que o client montou e devolve a resposta que a gente escolher.
class OpenMeteoClientTest {

    private static final String URL = "http://open-meteo.teste";

    private MockRestServiceServer servidor;
    private OpenMeteoClient client;
    private SimpleMeterRegistry metricas;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        metricas = new SimpleMeterRegistry();
        client = new OpenMeteoClient(builder, URL, metricas);
    }

    @Test
    @DisplayName("monta a URL com os parâmetros certos e converte o JSON no record")
    void buscaPrevisao() {
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL + "/v1/forecast")))
                .andExpect(queryParam("latitude", "-28.6775"))
                .andExpect(queryParam("start_date", "2026-10-24"))
                .andExpect(queryParam("timezone", "auto"))
                .andRespond(withSuccess("""
                        {"latitude":-28.64,"longitude":-49.36,"generationtime_ms":0.3,
                         "daily":{"time":["2026-10-24"],"weather_code":[95],"temperature_2m_max":[28.0],
                                  "temperature_2m_min":[17.0],"precipitation_probability_max":[75],
                                  "wind_speed_10m_max":[15.9]}}""", MediaType.APPLICATION_JSON));

        OpenMeteoRespostaDTO r = client.buscarPrevisao(-28.6775, -49.3697,
                LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 24));

        assertThat(r.daily().weather_code()).containsExactly(95);
        assertThat(r.daily().time()).containsExactly(LocalDate.of(2026, 10, 24));
        servidor.verify();
        assertThat(metricas.timer("openmeteo.chamadas", "resultado", "ok").count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Open-Meteo respondendo 500 vira PrevisaoIndisponivelException (502) e conta como erro")
    void erroDoServidor() {
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL))).andRespond(withServerError());

        assertThatThrownBy(() -> client.buscarAgora(-28.6, -49.3))
                .isExactlyInstanceOf(PrevisaoIndisponivelException.class)
                .hasMessageContaining("500");
        assertThat(metricas.timer("openmeteo.chamadas", "resultado", "erro").count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("JSON que não bate com o contrato também vira PrevisaoIndisponivelException")
    void jsonQuebrado() {
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL)))
                .andRespond(withSuccess("isto não é json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.buscarAgora(-28.6, -49.3))
                .isInstanceOf(PrevisaoIndisponivelException.class);
    }
}
