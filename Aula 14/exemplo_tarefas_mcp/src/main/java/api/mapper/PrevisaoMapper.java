// Declara o pacote dos mappers -- as classes que convertem DTO <-> entidade.
package api.mapper;

// Importa a anotação que registra a classe como bean do Spring.
import org.springframework.stereotype.Component;

// Importa o formato do Open-Meteo (o contrato do OUTRO sistema).
import api.client.OpenMeteoAgoraDTO;
import api.client.OpenMeteoRespostaDTO;
// Importa o nosso contrato.
import api.dto.AgoraDTO;
import api.dto.PrevisaoDTO;

// >>> AULA 12: o tradutor entre o Open-Meteo e a NOSSA API. É o único lugar
// que conhece os dois vocabulários ao mesmo tempo:
//
//   Open-Meteo                         nossa API
//   daily.time[i]                 -->  data
//   daily.weather_code[i]  (95)   -->  descricao ("Trovoada") e icone
//   daily.temperature_2m_min[i]   -->  temperaturaMinima
//   daily.temperature_2m_max[i]   -->  temperaturaMaxima
//   daily.precipitation_probability_max[i] --> chanceDeChuva
//   daily.wind_speed_10m_max[i]   -->  ventoMaximo
//
// Igual aos outros mappers: só converte. Não chama o Open-Meteo nem o banco.
// Registra a classe como bean para poder ser injetada.
@Component
public class PrevisaoMapper {

    // Um dia da resposta do Open-Meteo → PrevisaoDTO. "indice" diz qual dia:
    // as medidas vêm em listas, e o dia N é o item N de cada lista.
    public PrevisaoDTO toPrevisao(OpenMeteoRespostaDTO resposta, int indice) {
        // Atalho para não escrever resposta.daily() a cada linha.
        OpenMeteoRespostaDTO.Diario dia = resposta.daily();
        // O código do tempo vira texto e ícone.
        Integer codigo = dia.weather_code().get(indice);
        return new PrevisaoDTO(
                dia.time().get(indice),
                descricao(codigo),
                icone(codigo),
                dia.temperature_2m_min().get(indice),
                dia.temperature_2m_max().get(indice),
                dia.precipitation_probability_max().get(indice),
                dia.wind_speed_10m_max().get(indice),
                resposta.latitude(),
                resposta.longitude(),
                // Exercício 4: acabou de vir do Open-Meteo.
                "open-meteo");
    }

    // Exercício 1: "current" do Open-Meteo → AgoraDTO. Aqui não há listas:
    // "agora" é um objeto só.
    public AgoraDTO toAgora(OpenMeteoAgoraDTO resposta) {
        OpenMeteoAgoraDTO.Atual atual = resposta.current();
        return new AgoraDTO(atual.time(), descricao(atual.weather_code()), icone(atual.weather_code()),
                atual.temperature_2m(), atual.wind_speed_10m());
    }

    // Códigos WMO (a tabela que o Open-Meteo usa) → texto em português.
    // "switch" com setas (Java 14+): cada "case" devolve um valor, sem break.
    private String descricao(Integer codigo) {
        // Dado de fora não é confiável: o código pode nem vir.
        if (codigo == null) {
            return "Sem informação";
        }
        return switch (codigo) {
            case 0 -> "Céu limpo";
            case 1 -> "Poucas nuvens";
            case 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45, 48 -> "Neblina";
            case 51, 53, 55 -> "Garoa";
            case 56, 57 -> "Garoa congelante";
            case 61 -> "Chuva fraca";
            case 63 -> "Chuva moderada";
            case 65 -> "Chuva forte";
            case 66, 67 -> "Chuva congelante";
            case 71, 73, 75, 77 -> "Neve";
            case 80, 81, 82 -> "Pancadas de chuva";
            case 85, 86 -> "Pancadas de neve";
            case 95 -> "Trovoada";
            case 96, 99 -> "Trovoada com granizo";
            // Um código que a tabela não conhece: mostra o número, não quebra.
            default -> "Código do tempo " + codigo;
        };
    }

    // Mesmos códigos → um emoji para a tela.
    private String icone(Integer codigo) {
        if (codigo == null) {
            return "❔";
        }
        return switch (codigo) {
            case 0 -> "☀️";
            case 1 -> "🌤️";
            case 2 -> "⛅";
            case 3 -> "☁️";
            case 45, 48 -> "🌫️";
            case 51, 53, 55, 80, 81, 82 -> "🌦️";
            case 56, 57, 61, 63, 65, 66, 67 -> "🌧️";
            case 71, 73, 75, 77, 85, 86 -> "❄️";
            case 95, 96, 99 -> "⛈️";
            default -> "❔";
        };
    }
}
