// Define o pacote dos clientes de APIs externas.
package api.client;

// Importa o tipo de data de cada dia da previsão.
import java.time.LocalDate;
// Importa a lista usada em cada medida diária.
import java.util.List;

// >>> AULA 12: a resposta do Open-Meteo do jeito que ELE manda -- o contrato
// do OUTRO sistema, copiado como é:
//
//   {"latitude": -28.64, "longitude": -49.36, "timezone": "America/Sao_Paulo",
//    "daily": {"time": ["2026-10-24"], "weather_code": [95],
//              "temperature_2m_max": [28.0], "temperature_2m_min": [17.0],
//              "precipitation_probability_max": [75], "wind_speed_10m_max": [15.9]}}
//
// Os nomes ficam iguais aos do JSON, em inglês e com "_": o Jackson casa cada
// componente do record com o campo de mesmo nome. Quem traduz para o NOSSO
// contrato (temperaturaMaxima, chanceDeChuva...) é o PrevisaoMapper.
//
// O JSON tem MUITO mais coisa (generationtime_ms, elevation, daily_units...).
// O record só declara o que a nossa API usa; o resto é ignorado (no Spring
// Boot, campo desconhecido não é erro).
//
// Este record não aparece na seção "Schemas" do Swagger: ele nunca entra nem
// sai pelos nossos Controllers. É assunto entre a nossa API e o Open-Meteo.
public record OpenMeteoRespostaDTO(

        // A latitude do ponto que o Open-Meteo usou (arredondada para a grade dele).
        Double latitude,

        // A longitude do ponto que o Open-Meteo usou.
        Double longitude,

        // O fuso horário do lugar (com timezone=auto, o Open-Meteo descobre sozinho).
        String timezone,

        // As medidas por dia (veja o record Diario, logo abaixo).
        Diario daily

) {

    // Atenção ao formato: "daily" NÃO é uma lista de dias. É um objeto com uma
    // LISTA POR MEDIDA, todas do mesmo tamanho: o item 0 de cada lista é do
    // primeiro dia, o item 1 é do segundo, e assim por diante.
    public record Diario(

            // Os dias, no formato yyyy-MM-dd (o Jackson já converte para LocalDate).
            List<LocalDate> time,

            // Código do tempo no padrão WMO (0 = céu limpo, 61 = chuva fraca, 95 = trovoada...).
            List<Integer> weather_code,

            // Temperatura máxima do dia, em °C.
            List<Double> temperature_2m_max,

            // Temperatura mínima do dia, em °C.
            List<Double> temperature_2m_min,

            // Maior chance de chuva no dia, em %.
            List<Integer> precipitation_probability_max,

            // Vento mais forte do dia, em km/h.
            List<Double> wind_speed_10m_max

    ) {
    }
}
