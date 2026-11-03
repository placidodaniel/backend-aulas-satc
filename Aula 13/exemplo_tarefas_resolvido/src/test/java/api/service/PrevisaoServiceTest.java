package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import api.client.OpenMeteoClient;
import api.client.OpenMeteoRespostaDTO;
import api.client.PrevisaoIndisponivelException;
import api.dto.PrevisaoDTO;
import api.mapper.PrevisaoMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

// Teste unitário do Service: o OpenMeteoClient é um MOCK (um dublê), então não existe internet aqui.
// O que se testa é a REGRA do PrevisaoService: validações, cache e tratamento de resposta ruim.
class PrevisaoServiceTest {

    private static final double LAT = -28.6775;
    private static final double LON = -49.3697;

    private OpenMeteoClient client;
    private SimpleMeterRegistry metricas;
    private PrevisaoService service;

    @BeforeEach
    void preparar() {
        client = mock(OpenMeteoClient.class);
        metricas = new SimpleMeterRegistry();
        service = new PrevisaoService(client, new PrevisaoMapper(), metricas);
    }

    private OpenMeteoRespostaDTO respostaPara(LocalDate dia) {
        var diario = new OpenMeteoRespostaDTO.Diario(List.of(dia), List.of(61), List.of(25.0), List.of(15.0),
                List.of(80), List.of(10.0));
        return new OpenMeteoRespostaDTO(LAT, LON, "America/Sao_Paulo", diario);
    }

    @Test
    @DisplayName("R1: latitude fora de -90..90 vira RegraDeNegocioException e o Open-Meteo nem é chamado")
    void latitudeInvalida() {
        assertThatThrownBy(() -> service.buscar(91, LON, LocalDate.now()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Latitude");
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("R1: longitude fora de -180..180")
    void longitudeInvalida() {
        assertThatThrownBy(() -> service.buscar(LAT, -181, LocalDate.now()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    @DisplayName("R2: data no passado é recusada")
    void dataNoPassado() {
        assertThatThrownBy(() -> service.buscar(LAT, LON, LocalDate.now().minusDays(1)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("hoje em diante");
        verifyNoInteractions(client);
    }

    @Test
    @DisplayName("R3: o 15º dia depois de hoje é aceito, o 16º não")
    void limiteDaJanela() {
        LocalDate limite = LocalDate.now().plusDays(PrevisaoService.DIAS_DE_PREVISAO);
        when(client.buscarPrevisao(anyDouble(), anyDouble(), any(), any())).thenReturn(respostaPara(limite));

        assertThat(service.buscar(LAT, LON, limite).data()).isEqualTo(limite);
        assertThatThrownBy(() -> service.buscar(LAT, LON, limite.plusDays(1)))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("a segunda consulta igual vem do cache e não chama o Open-Meteo de novo")
    void cacheEvitaSegundaChamada() {
        LocalDate hoje = LocalDate.now();
        when(client.buscarPrevisao(LAT, LON, hoje, hoje)).thenReturn(respostaPara(hoje));

        PrevisaoDTO primeira = service.buscar(LAT, LON, hoje);
        PrevisaoDTO segunda = service.buscar(LAT, LON, hoje);

        assertThat(primeira.origem()).isEqualTo("open-meteo");
        assertThat(segunda.origem()).isEqualTo("cache");
        verify(client, times(1)).buscarPrevisao(LAT, LON, hoje, hoje);
        // Aula 13: o comportamento do cache também vira métrica.
        assertThat(metricas.counter("previsao.cache", "resultado", "hit").count()).isEqualTo(1.0);
        assertThat(metricas.counter("previsao.cache", "resultado", "miss").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("lugares diferentes não compartilham cache")
    void cachePorLugar() {
        LocalDate hoje = LocalDate.now();
        when(client.buscarPrevisao(anyDouble(), anyDouble(), any(), any())).thenReturn(respostaPara(hoje));

        service.buscar(LAT, LON, hoje);
        service.buscar(10.0, 10.0, hoje);

        verify(client, times(2)).buscarPrevisao(anyDouble(), anyDouble(), any(), any());
    }

    @Test
    @DisplayName("200 do Open-Meteo, mas sem o dia pedido: vira PrevisaoIndisponivelException (502)")
    void respostaSemDias() {
        LocalDate hoje = LocalDate.now();
        when(client.buscarPrevisao(anyDouble(), anyDouble(), any(), any()))
                .thenReturn(new OpenMeteoRespostaDTO(LAT, LON, "x", null));

        assertThatThrownBy(() -> service.buscar(LAT, LON, hoje))
                .isInstanceOf(PrevisaoIndisponivelException.class);
    }

    @Test
    @DisplayName("falha do client não é guardada no cache")
    void falhaNaoVaiParaOCache() {
        LocalDate hoje = LocalDate.now();
        when(client.buscarPrevisao(anyDouble(), anyDouble(), any(), any()))
                .thenThrow(new PrevisaoIndisponivelException("fora do ar", null))
                .thenReturn(respostaPara(hoje));

        assertThatThrownBy(() -> service.buscar(LAT, LON, hoje)).isInstanceOf(PrevisaoIndisponivelException.class);
        assertThat(service.buscar(LAT, LON, hoje).origem()).isEqualTo("open-meteo");
    }

    @Test
    void temPrevisaoSoParaAJanela() {
        assertThat(service.temPrevisao(LocalDate.now())).isTrue();
        assertThat(service.temPrevisao(LocalDate.now().plusDays(15))).isTrue();
        assertThat(service.temPrevisao(LocalDate.now().plusDays(16))).isFalse();
        assertThat(service.temPrevisao(LocalDate.now().minusDays(1))).isFalse();
        verify(client, never()).buscarPrevisao(anyDouble(), anyDouble(), any(), any());
    }
}
