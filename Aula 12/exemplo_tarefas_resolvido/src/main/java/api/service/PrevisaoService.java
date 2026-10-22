// Define o pacote da camada de regras de negócio.
package api.service;

// Importa os tipos de data e de instante usados nas regras e no cache.
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
// Importa os tipos usados para montar o período e o cache.
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Importa a anotação que registra a classe como serviço Spring.
import org.springframework.stereotype.Service;

// Importa o cliente do Open-Meteo, a exceção dele e o formato das respostas.
import api.client.OpenMeteoAgoraDTO;
import api.client.OpenMeteoClient;
import api.client.OpenMeteoRespostaDTO;
import api.client.PrevisaoIndisponivelException;
// Importa o nosso contrato.
import api.dto.AgoraDTO;
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

    // Exercício 4: por quanto tempo uma previsão guardada ainda vale.
    private static final Duration VALIDADE_DO_CACHE = Duration.ofMinutes(30);

    // Exercício 4: uma previsão guardada e o instante em que ela chegou.
    private record Guardada(PrevisaoDTO previsao, Instant quando) {
    }

    // Exercício 4: o cache, um "dicionário" chave → previsão guardada. Fica na
    // memória da API: some quando ela reinicia. ConcurrentHashMap, e não
    // HashMap, porque várias requisições podem mexer nele ao mesmo tempo.
    private final Map<String, Guardada> cache = new ConcurrentHashMap<>();

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

        // Exercício 4: a mesma previsão foi pedida há menos de 30 minutos?
        // Coordenadas com 2 casas (~1 km): dois pontos tão próximos têm a
        // mesma previsão. Locale.ROOT: ponto como separador decimal, sempre.
        String chave = String.format(Locale.ROOT, "%.2f;%.2f;%s", latitude, longitude, data);
        Guardada guardada = cache.get(chave);
        if (guardada != null && guardada.quando().plus(VALIDADE_DO_CACHE).isAfter(Instant.now())) {
            // Ainda vale: responde sem chamar o Open-Meteo.
            return guardada.previsao().comOrigem("cache");
        }

        // A chamada ao outro sistema: um período de um dia só.
        OpenMeteoRespostaDTO resposta = client.buscarPrevisao(latitude, longitude, data, data);
        exigirDias(resposta, data);

        // O dia pedido é o primeiro (e único) item de cada lista.
        PrevisaoDTO previsao = mapper.toPrevisao(resposta, 0);
        // Exercício 4: guarda para os próximos 30 minutos.
        cache.put(chave, new Guardada(previsao, Instant.now()));
        return previsao;
    }

    // Exercício 1: GET /previsao/agora -- o tempo de agora, num lugar.
    public AgoraDTO agora(double latitude, double longitude) {
        // Só a R1 vale aqui: não há data.
        validarCoordenadas(latitude, longitude);
        OpenMeteoAgoraDTO resposta = client.buscarAgora(latitude, longitude);
        // Dado de fora não é confiável: 200 sem o bloco "current" é falha do serviço.
        if (resposta == null || resposta.current() == null) {
            throw new PrevisaoIndisponivelException("O serviço de previsão respondeu sem o tempo de agora", null);
        }
        return mapper.toAgora(resposta);
    }

    // Exercício 5: a previsão de TODOS os dias da janela (hoje + 15), com UMA
    // chamada só ao Open-Meteo. Devolve um "dicionário" dia → previsão.
    public Map<LocalDate, PrevisaoDTO> buscarJanela(double latitude, double longitude) {
        validarCoordenadas(latitude, longitude);
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(DIAS_DE_PREVISAO);
        // Um período de 16 dias: as listas do "daily" vêm com 16 itens.
        OpenMeteoRespostaDTO resposta = client.buscarPrevisao(latitude, longitude, hoje, limite);
        exigirDias(resposta, hoje);
        // LinkedHashMap: guarda os dias na ordem em que entraram.
        Map<LocalDate, PrevisaoDTO> porDia = new LinkedHashMap<>();
        for (int i = 0; i < resposta.daily().time().size(); i++) {
            // O dia i é o item i de cada lista.
            PrevisaoDTO previsao = mapper.toPrevisao(resposta, i);
            porDia.put(previsao.data(), previsao);
        }
        return porDia;
    }

    // Exercício 2: o Open-Meteo tem previsão para este dia? (R2 e R3, sem lançar exceção.)
    public boolean temPrevisao(LocalDate data) {
        LocalDate hoje = LocalDate.now();
        return !data.isBefore(hoje) && !data.isAfter(hoje.plusDays(DIAS_DE_PREVISAO));
    }

    // R1: uma coordenada fora do planeta é erro de quem chamou (400).
    // Exercício 2: pública, para o TarefaService conferir antes de tudo.
    public void validarCoordenadas(double latitude, double longitude) {
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

    // Dado de fora não é confiável: o serviço respondeu 200, mas sem os dias.
    // Para quem chamou, é falha do serviço de previsão (502).
    private void exigirDias(OpenMeteoRespostaDTO resposta, LocalDate data) {
        if (resposta == null || resposta.daily() == null || resposta.daily().time() == null
                || resposta.daily().time().isEmpty()) {
            throw new PrevisaoIndisponivelException("O serviço de previsão respondeu sem a previsão de " + data, null);
        }
    }
}
