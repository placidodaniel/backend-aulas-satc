// Define o pacote da camada de regras de negócio.
package api.service;

// Importa o tipo de data do dia pedido.
import java.time.LocalDate;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o cliente do Open-Meteo, a exceção dele e o formato da resposta.
import api.client.OpenMeteoClient;
import api.client.OpenMeteoRespostaDTO;
import api.client.PrevisaoIndisponivelException;
// Importa o nosso contrato.
import api.dto.PrevisaoDTO;
// Importa o mapper que traduz Open-Meteo <-> nossa API.
import api.mapper.PrevisaoMapper;

// >>> AULA 12: as regras da previsão do tempo. Mesma posição de qualquer
// Service: o Controller chama, e ele coordena os outros. A novidade é que, em
// vez do Repository (o NOSSO banco), ele usa o OpenMeteoClient (os dados de
// OUTRO sistema, atrás de uma API):
//
//   PrevisaoController ──> PrevisaoService ──> OpenMeteoClient ──HTTP──> Open-Meteo
//                                │
//                                └──> PrevisaoMapper (traduz)
//
// Regras (violar qualquer uma = 400, e o Open-Meteo nem é chamado):
//   R1. Latitude entre -90 e 90; longitude entre -180 e 180.
//   R2. A previsão é para hoje em diante.
//   R3. Só até 15 dias depois de hoje: o Open-Meteo prevê 16 dias, contando hoje.
// Registra a classe como bean da camada de serviço.
@Service
public class PrevisaoService {

    // Quantos dias depois de hoje o Open-Meteo consegue prever.
    public static final int DIAS_DE_PREVISAO = 15;

    // Faz as requisições HTTP para o Open-Meteo.
    private final OpenMeteoClient client;

    // Traduz a resposta do Open-Meteo para o nosso formato.
    private final PrevisaoMapper mapper;

    // Recebe as dependências que o Spring injeta automaticamente.
    public PrevisaoService(OpenMeteoClient client, PrevisaoMapper mapper) {
        // Guarda as dependências recebidas nos atributos da classe.
        this.client = client;
        this.mapper = mapper;
    }

    // GET /previsao -- a previsão de um dia, num lugar.
    public PrevisaoDTO buscar(double latitude, double longitude, LocalDate data) {
        // As regras vêm ANTES da chamada: pedido inválido não gasta uma
        // requisição ao Open-Meteo (e ele responderia 400 do jeito dele).
        validarCoordenadas(latitude, longitude);
        validarData(data);

        // A chamada ao outro sistema: um período de um dia só.
        OpenMeteoRespostaDTO resposta = client.buscarPrevisao(latitude, longitude, data, data);

        // Dado de fora não é confiável: o serviço respondeu 200, mas sem o dia
        // pedido. Para quem chamou, é falha do serviço de previsão (502).
        if (resposta == null || resposta.daily() == null || resposta.daily().time() == null
                || resposta.daily().time().isEmpty()) {
            throw new PrevisaoIndisponivelException("O serviço de previsão respondeu sem a previsão de " + data, null);
        }

        // O dia pedido é o primeiro (e único) item de cada lista.
        return mapper.toPrevisao(resposta, 0);
    }

    // R1: uma coordenada fora do planeta é erro de quem chamou (400).
    private void validarCoordenadas(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new RegraDeNegocioException("Latitude deve estar entre -90 e 90 (veio " + latitude + ")");
        }
        if (longitude < -180 || longitude > 180) {
            throw new RegraDeNegocioException("Longitude deve estar entre -180 e 180 (veio " + longitude + ")");
        }
    }

    // R2 e R3: a janela de datas que o Open-Meteo consegue prever.
    private void validarData(LocalDate data) {
        LocalDate hoje = LocalDate.now();
        if (data.isBefore(hoje)) {
            throw new RegraDeNegocioException("A previsão é para hoje em diante (veio " + data + ")");
        }
        LocalDate limite = hoje.plusDays(DIAS_DE_PREVISAO);
        if (data.isAfter(limite)) {
            throw new RegraDeNegocioException("A previsão só cobre os próximos " + (DIAS_DE_PREVISAO + 1)
                    + " dias, até " + limite + " (veio " + data + ")");
        }
    }
}
