package api.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import api.client.OpenMeteoAgoraDTO;
import api.client.OpenMeteoRespostaDTO;
import api.dto.ResponsavelResponseDTO;
import api.dto.TarefaResponseDTO;

// "Toda rota da API aparece na observabilidade?" Este teste chama TODAS as rotas de /tarefas,
// /responsaveis e /previsao e confere, no /actuator/prometheus, que cada uma tem a sua série
// (método + rota). Se alguém criar uma rota nova e ela sumir do painel, o teste acusa.
class CoberturaObservabilidadeE2ETest extends AbstractE2ETest {

    // Coordenadas próprias: o cache da previsão vive no contexto compartilhado e não pode vazar para outros testes.
    private static final double LAT = -23.5505, LON = -46.6333;

    private void chamar(HttpMethod metodo, String url, Object corpo) {
        http.exchange(url, metodo, corpo == null ? null : new HttpEntity<>(corpo), String.class);
    }

    @Test
    @DisplayName("toda rota de tarefas, responsáveis e previsão gera métricas HTTP, e cada operação de tarefa é contada")
    void todasAsRotasAparecemNasMetricas() {
        // O Open-Meteo é mock: devolve um dia para cada data pedida.
        when(openMeteoClient.buscarPrevisao(anyDouble(), anyDouble(), any(), any())).thenAnswer(inv -> {
            LocalDate ini = inv.getArgument(2), fim = inv.getArgument(3);
            List<LocalDate> dias = new ArrayList<>();
            for (LocalDate d = ini; !d.isAfter(fim); d = d.plusDays(1)) dias.add(d);
            int n = dias.size();
            return new OpenMeteoRespostaDTO(LAT, LON, "America/Sao_Paulo", new OpenMeteoRespostaDTO.Diario(
                    dias, dias.stream().map(d -> 61).toList(), dias.stream().map(d -> 25.0).toList(),
                    dias.stream().map(d -> 15.0).toList(), dias.stream().map(d -> 80).toList(),
                    dias.stream().map(d -> 10.0).toList()));
        });
        when(openMeteoClient.buscarAgora(anyDouble(), anyDouble())).thenReturn(new OpenMeteoAgoraDTO(LAT, LON,
                new OpenMeteoAgoraDTO.Atual(LocalDateTime.now(), 21.0, 10.0, 3)));

        String prazo = LocalDate.now().plusDays(3).toString();
        Map<String, Object> corpo = Map.of("titulo", "Cobertura", "responsavel", "Ana", "dataPrazo", prazo, "prioridade", 2);

        // ---- responsáveis
        var resp = http.postForEntity("/responsaveis", Map.of("nome", "Ana Souza", "email", "ana@satc.edu.br"),
                ResponsavelResponseDTO.class).getBody();
        chamar(HttpMethod.GET, "/responsaveis", null);
        chamar(HttpMethod.GET, "/responsaveis/" + resp.id(), null);
        chamar(HttpMethod.GET, "/responsaveis/" + resp.id() + "/tarefas", null);

        // ---- tarefas
        var t = http.postForEntity("/tarefas", corpo, TarefaResponseDTO.class).getBody();
        long id = t.id();
        chamar(HttpMethod.GET, "/tarefas", null);
        chamar(HttpMethod.GET, "/tarefas/buscar?responsavel=Ana", null);
        chamar(HttpMethod.GET, "/tarefas/atrasadas", null);
        chamar(HttpMethod.GET, "/tarefas/" + id, null);
        chamar(HttpMethod.PUT, "/tarefas/" + id, corpo);
        chamar(HttpMethod.PATCH, "/tarefas/" + id, Map.of("titulo", "Cobertura (patch)"));
        chamar(HttpMethod.PUT, "/tarefas/" + id + "/concluir", null); // concluir
        chamar(HttpMethod.PUT, "/tarefas/" + id + "/concluir", null); // reabrir
        chamar(HttpMethod.PUT, "/tarefas/" + id + "/responsavel", Map.of("responsavelId", resp.id()));
        chamar(HttpMethod.DELETE, "/tarefas/" + id + "/responsavel", null);
        chamar(HttpMethod.GET, "/tarefas/" + id + "/previsao?latitude=" + LAT + "&longitude=" + LON, null);
        chamar(HttpMethod.GET, "/tarefas/chuvosas?latitude=" + LAT + "&longitude=" + LON, null);
        chamar(HttpMethod.DELETE, "/tarefas/" + id, null);

        // ---- previsão
        chamar(HttpMethod.GET, "/previsao?latitude=" + LAT + "&longitude=" + LON + "&data=" + prazo, null);
        chamar(HttpMethod.GET, "/previsao/agora?latitude=" + LAT + "&longitude=" + LON, null);

        String texto = http.getForEntity("/actuator/prometheus", String.class).getBody();

        // Cada rota (método + padrão da rota) precisa ter a sua série de métricas HTTP.
        List<String[]> rotas = List.of(
                new String[]{"GET", "/responsaveis"}, new String[]{"POST", "/responsaveis"},
                new String[]{"GET", "/responsaveis/{id}"}, new String[]{"GET", "/responsaveis/{id}/tarefas"},
                new String[]{"GET", "/tarefas"}, new String[]{"POST", "/tarefas"},
                new String[]{"GET", "/tarefas/buscar"}, new String[]{"GET", "/tarefas/atrasadas"},
                new String[]{"GET", "/tarefas/chuvosas"}, new String[]{"GET", "/tarefas/{id}"},
                new String[]{"PUT", "/tarefas/{id}"}, new String[]{"PATCH", "/tarefas/{id}"},
                new String[]{"DELETE", "/tarefas/{id}"}, new String[]{"PUT", "/tarefas/{id}/concluir"},
                new String[]{"PUT", "/tarefas/{id}/responsavel"}, new String[]{"DELETE", "/tarefas/{id}/responsavel"},
                new String[]{"GET", "/tarefas/{id}/previsao"},
                new String[]{"GET", "/previsao"}, new String[]{"GET", "/previsao/agora"});
        List<String> semMetrica = new ArrayList<>();
        for (String[] r : rotas) {
            Pattern p = Pattern.compile("http_server_requests_seconds_count\\{[^}]*method=\"" + r[0]
                    + "\"[^}]*uri=\"" + Pattern.quote(r[1]) + "\"");
            if (!p.matcher(texto).find()) semMetrica.add(r[0] + " " + r[1]);
        }
        assertThat(semMetrica).as("rotas sem métrica HTTP").isEmpty();

        // Cada operação de negócio de tarefa foi contada.
        for (String op : List.of("criar", "atualizar", "atualizar_parcial", "concluir", "reabrir",
                "vincular_responsavel", "desvincular_responsavel", "remover")) {
            assertThat(texto).as("operação " + op).contains("tarefas_operacoes_total{")
                    .containsPattern("tarefas_operacoes_total\\{[^}]*operacao=\"" + op + "\"");
        }
    }
}
