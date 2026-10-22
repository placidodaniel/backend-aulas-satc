// Define o pacote dos clientes de APIs externas.
package api.client;

// Importa o tipo de data e hora da medição.
import java.time.LocalDateTime;

// Exercício 1: a resposta do Open-Meteo para "current=...", do jeito que ELE manda:
//
//   {"latitude": -25.41, "longitude": -49.24,
//    "current": {"time": "2026-10-21T23:15", "temperature_2m": 14.1,
//                "wind_speed_10m": 11.9, "weather_code": 3}}
//
// Repare na diferença para o "daily": aqui não há listas, é um objeto só,
// porque "agora" é um momento só.
public record OpenMeteoAgoraDTO(

        // A latitude do ponto que o Open-Meteo usou.
        Double latitude,

        // A longitude do ponto que o Open-Meteo usou.
        Double longitude,

        // As medidas de agora (veja o record Atual, logo abaixo).
        Atual current

) {

    // As medidas do momento, com os nomes do JSON.
    public record Atual(

            // Quando foi a medição (hora do lugar, com timezone=auto).
            LocalDateTime time,

            // Temperatura, em °C.
            Double temperature_2m,

            // Vento, em km/h.
            Double wind_speed_10m,

            // Código do tempo no padrão WMO, o mesmo da previsão diária.
            Integer weather_code

    ) {
    }
}
