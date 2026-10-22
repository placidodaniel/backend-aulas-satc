// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data do dia previsto.
import java.time.LocalDate;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// >>> AULA 12: a previsão de UM dia, como a NOSSA API devolve.
//
// Não é cópia da resposta do Open-Meteo (o OpenMeteoRespostaDTO). É o nosso
// contrato: em português, um objeto por dia (lá são listas por medida), com
// o código do tempo já traduzido em texto ("Chuva fraca") e num ícone.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Previsão do tempo de um dia, num lugar")
public record PrevisaoDTO(

        // O dia previsto.
        @Schema(description = "Dia da previsão (yyyy-MM-dd)", example = "2026-10-24")
        LocalDate data,

        // "weather_code" do Open-Meteo, traduzido pelo PrevisaoMapper.
        @Schema(description = "Como vai estar o tempo, em português", example = "Chuva fraca")
        String descricao,

        // Um emoji para a tela não precisar conhecer os códigos WMO.
        @Schema(description = "Ícone (emoji) do tempo", example = "🌧️")
        String icone,

        // "temperature_2m_min" do Open-Meteo.
        @Schema(description = "Temperatura mínima, em °C", example = "17.0")
        Double temperaturaMinima,

        // "temperature_2m_max" do Open-Meteo.
        @Schema(description = "Temperatura máxima, em °C", example = "28.0")
        Double temperaturaMaxima,

        // "precipitation_probability_max": pode vir null (dado de fora não é confiável).
        @Schema(description = "Maior chance de chuva no dia, em % (pode vir null)", example = "75")
        Integer chanceDeChuva,

        // "wind_speed_10m_max" do Open-Meteo.
        @Schema(description = "Vento mais forte do dia, em km/h", example = "15.9")
        Double ventoMaximo,

        // O ponto que o Open-Meteo usou (ele arredonda para a grade dele).
        @Schema(description = "Latitude usada pelo serviço de previsão", example = "-28.64")
        Double latitude,

        // Idem, para a longitude.
        @Schema(description = "Longitude usada pelo serviço de previsão", example = "-49.36")
        Double longitude

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
