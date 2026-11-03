package api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import api.client.OpenMeteoAgoraDTO;
import api.client.OpenMeteoRespostaDTO;
import api.dto.AgoraDTO;
import api.dto.PrevisaoDTO;

// Teste UNITÁRIO puro: sem Spring, sem banco, sem rede. Cria o objeto com "new" e confere a saída.
// É o tipo de teste mais barato: roda em milissegundos.
class PrevisaoMapperTest {

    private final PrevisaoMapper mapper = new PrevisaoMapper();

    private OpenMeteoRespostaDTO resposta(Integer codigo, Integer chuva) {
        var diario = new OpenMeteoRespostaDTO.Diario(
                List.of(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 25)),
                java.util.Arrays.asList(95, codigo),
                List.of(28.0, 18.3), List.of(17.0, 14.1),
                java.util.Arrays.asList(75, chuva),
                List.of(15.9, 8.0));
        return new OpenMeteoRespostaDTO(-28.64, -49.36, "America/Sao_Paulo", diario);
    }

    @Test
    @DisplayName("traduz o item i de cada lista para um dia no nosso formato")
    void traduzDiaPeloIndice() {
        PrevisaoDTO dia0 = mapper.toPrevisao(resposta(61, 40), 0);
        PrevisaoDTO dia1 = mapper.toPrevisao(resposta(61, 40), 1);

        assertThat(dia0.data()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(dia0.descricao()).isEqualTo("Trovoada");
        assertThat(dia0.icone()).isEqualTo("⛈️");
        assertThat(dia0.temperaturaMinima()).isEqualTo(17.0);
        assertThat(dia0.temperaturaMaxima()).isEqualTo(28.0);
        assertThat(dia0.chanceDeChuva()).isEqualTo(75);
        assertThat(dia0.origem()).isEqualTo("open-meteo");

        assertThat(dia1.data()).isEqualTo(LocalDate.of(2026, 10, 25));
        assertThat(dia1.descricao()).isEqualTo("Chuva fraca");
        assertThat(dia1.ventoMaximo()).isEqualTo(8.0);
    }

    @ParameterizedTest(name = "código {0} vira \"{1}\"")
    @CsvSource({"0,Céu limpo", "3,Nublado", "63,Chuva moderada", "80,Pancadas de chuva", "99,Trovoada com granizo"})
    void traduzCodigosConhecidos(int codigo, String esperado) {
        assertThat(mapper.toPrevisao(resposta(codigo, 10), 1).descricao()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("código desconhecido não quebra: vira 'Código do tempo N'")
    void codigoDesconhecido() {
        PrevisaoDTO previsao = mapper.toPrevisao(resposta(42, 10), 1);

        assertThat(previsao.descricao()).isEqualTo("Código do tempo 42");
        assertThat(previsao.icone()).isEqualTo("❔");
    }

    @Test
    @DisplayName("chance de chuva null (dado de fora) é aceita")
    void chanceDeChuvaNula() {
        assertThat(mapper.toPrevisao(resposta(1, null), 1).chanceDeChuva()).isNull();
    }

    @Test
    void traduzOTempoDeAgora() {
        var atual = new OpenMeteoAgoraDTO.Atual(LocalDateTime.of(2026, 10, 29, 14, 15), 21.4, 11.9, 3);
        AgoraDTO agora = mapper.toAgora(new OpenMeteoAgoraDTO(-28.6, -49.3, atual));

        assertThat(agora.descricao()).isEqualTo("Nublado");
        assertThat(agora.temperatura()).isEqualTo(21.4);
        assertThat(agora.vento()).isEqualTo(11.9);
        assertThat(agora.horario()).isEqualTo(LocalDateTime.of(2026, 10, 29, 14, 15));
    }
}
