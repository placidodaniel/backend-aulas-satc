package api.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import api.client.OpenMeteoRespostaDTO;
import api.client.PrevisaoIndisponivelException;
import api.client.PrevisaoTempoEsgotadoException;
import api.dto.ErroDTO;
import api.dto.PrevisaoDTO;

// O Open-Meteo é mock (veja AbstractE2ETest): aqui testamos o que a NOSSA API faz com cada resposta dele.
class PrevisaoApiE2ETest extends AbstractE2ETest {

    private static final String URL = "/previsao?latitude=-28.6775&longitude=-49.3697&data=";

    private OpenMeteoRespostaDTO resposta(LocalDate dia) {
        var diario = new OpenMeteoRespostaDTO.Diario(List.of(dia), List.of(95), List.of(28.0), List.of(17.0),
                List.of(75), List.of(15.9));
        return new OpenMeteoRespostaDTO(-28.64, -49.36, "America/Sao_Paulo", diario);
    }

    @Test
    @DisplayName("200: traduz a resposta do Open-Meteo para o nosso formato")
    void previsaoOk() {
        LocalDate dia = LocalDate.now().plusDays(1);
        when(openMeteoClient.buscarPrevisao(anyDouble(), anyDouble(), any(), any())).thenReturn(resposta(dia));

        ResponseEntity<PrevisaoDTO> r = http.getForEntity(URL + dia, PrevisaoDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().descricao()).isEqualTo("Trovoada");
        assertThat(r.getBody().chanceDeChuva()).isEqualTo(75);
    }

    @Test
    @DisplayName("400: data fora da janela de 16 dias (e o Open-Meteo nem é chamado)")
    void dataForaDaJanela() {
        ResponseEntity<ErroDTO> r = http.getForEntity(URL + LocalDate.now().plusDays(40), ErroDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("400: parâmetro em formato errado também sai como ErroDTO")
    void parametroInvalido() {
        ResponseEntity<ErroDTO> r = http.getForEntity(URL + "amanha", ErroDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().mensagem()).contains("data");
    }

    @Test
    @DisplayName("502: o Open-Meteo falhou")
    void openMeteoFalha() {
        when(openMeteoClient.buscarPrevisao(anyDouble(), anyDouble(), any(), any()))
                .thenThrow(new PrevisaoIndisponivelException("fora do ar", null));

        ResponseEntity<ErroDTO> r = http.getForEntity(URL + LocalDate.now().plusDays(2), ErroDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    @DisplayName("504: o Open-Meteo demorou demais")
    void openMeteoLento() {
        when(openMeteoClient.buscarPrevisao(anyDouble(), anyDouble(), any(), any()))
                .thenThrow(new PrevisaoTempoEsgotadoException("lento", null));

        ResponseEntity<ErroDTO> r = http.getForEntity(URL + LocalDate.now().plusDays(3), ErroDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }
}
