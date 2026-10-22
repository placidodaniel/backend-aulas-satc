// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data e hora da medição.
import java.time.LocalDateTime;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 1: o tempo de AGORA, como a nossa API devolve.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "O tempo de agora, num lugar")
public record AgoraDTO(

        // "current.time" do Open-Meteo.
        @Schema(description = "Quando foi a medição (hora do lugar)", example = "2026-10-21T23:15:00")
        LocalDateTime horario,

        // "current.weather_code", traduzido pelo PrevisaoMapper.
        @Schema(description = "Como está o tempo, em português", example = "Nublado")
        String descricao,

        // O mesmo ícone da previsão diária.
        @Schema(description = "Ícone (emoji) do tempo", example = "☁️")
        String icone,

        // "current.temperature_2m".
        @Schema(description = "Temperatura agora, em °C", example = "14.1")
        Double temperatura,

        // "current.wind_speed_10m".
        @Schema(description = "Vento agora, em km/h", example = "11.9")
        Double vento

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
