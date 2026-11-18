// Mesmo pacote dos outros testes E2E.
package api.e2e;

// Importa as asserções do AssertJ e os utilitários do Mockito.
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import api.assistente.AssistenteIndisponivelException;
import api.assistente.ChatCompletions;
import api.assistente.ChatCompletions.ChamadaFerramenta;
import api.assistente.ChatCompletions.Escolha;
import api.assistente.ChatCompletions.Funcao;
import api.assistente.ChatCompletions.Mensagem;
import api.assistente.ChatCompletions.Resposta;
import api.assistente.ModeloClient;
import api.dto.AssistenteRespostaDTO;
import api.dto.AssistenteStatusDTO;

// Aula 14: teste E2E da tela do assistente (POST /assistente).
//
// O que é FALSO: só o servidor de modelos (ModeloClient). Ele vira um mock que
// devolve respostas "roteirizadas": primeiro pede uma ferramenta, depois responde.
// O que é DE VERDADE: o AssistenteService (o host), o cliente MCP, a rota /mcp,
// as ferramentas, o TarefaService, o PostgreSQL e o RabbitMQ.
//
// É a regra da Aula 13: mockar o que não é nosso (o modelo de IA) e deixar o
// resto real. Assim o teste não depende de internet, de chave nem de sorte:
// um modelo de verdade pode responder diferente a cada vez.
class AssistenteE2ETest extends AbstractE2ETest {

    @MockitoBean
    ModeloClient modelos;

    // Resposta em que o modelo PEDE uma ferramenta.
    private Resposta pedeFerramenta(String nome, String argumentosJson) {
        var chamada = new ChamadaFerramenta("chamada-1", "function", new Funcao(nome, argumentosJson));
        return new Resposta("modelo-falso", List.of(new Escolha(
                new Mensagem("assistant", null, List.of(chamada), null), "tool_calls")), null);
    }

    // Resposta em que o modelo RESPONDE em texto.
    private Resposta responde(String texto) {
        return new Resposta("modelo-falso", List.of(new Escolha(Mensagem.assistente(texto), "stop")), null);
    }

    // Corpo do POST /assistente com uma pergunta só.
    private Map<String, Object> pedido(String texto, boolean somenteLeitura) {
        return Map.of("mensagens", List.of(Map.of("papel", "usuario", "texto", texto)),
                "somenteLeitura", somenteLeitura);
    }

    @Test
    @DisplayName("o modelo pede estatisticas_tarefas, o host chama o MCP e devolve o resultado ao modelo")
    @SuppressWarnings("unchecked")
    void cicloComFerramenta() {
        when(modelos.modeloPadrao()).thenReturn("modelo-falso");
        when(modelos.conversar(anyString(), anyList(), anyList()))
                .thenReturn(pedeFerramenta("estatisticas_tarefas", "{}"))
                .thenReturn(responde("Tudo em dia."));

        var resposta = http.postForEntity("/assistente", pedido("Como está o andamento?", false), AssistenteRespostaDTO.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().resposta()).isEqualTo("Tudo em dia.");
        assertThat(resposta.getBody().passos()).hasSize(1);
        assertThat(resposta.getBody().passos().get(0).ferramenta()).isEqualTo("estatisticas_tarefas");
        // O resultado veio do servidor MCP de verdade (o JSON do EstatisticasDTO).
        assertThat(resposta.getBody().passos().get(0).resultado()).contains("\"total\"");

        // A conversa mandada ao modelo terminou com o resultado da ferramenta, ligado ao id do pedido.
        ArgumentCaptor<List<Mensagem>> conversa = ArgumentCaptor.forClass(List.class);
        verify(modelos, org.mockito.Mockito.times(2)).conversar(eq("modelo-falso"), conversa.capture(), anyList());
        Mensagem ultima = conversa.getValue().get(conversa.getValue().size() - 1);
        assertThat(ultima.role()).isEqualTo("tool");
        assertThat(ultima.toolCallId()).isEqualTo("chamada-1");
        // E começou com as instruções, que trazem a data de hoje.
        assertThat(conversa.getValue().get(0).content()).contains(java.time.LocalDate.now().toString());
    }

    @Test
    @DisplayName("argumento inválido: a ferramenta responde isError e o erro volta para o modelo corrigir")
    void erroDaFerramentaVoltaParaOModelo() {
        when(modelos.modeloPadrao()).thenReturn("modelo-falso");
        when(modelos.conversar(anyString(), anyList(), anyList()))
                .thenReturn(pedeFerramenta("cadastrar_tarefa",
                        "{\"titulo\":\"X\",\"responsavel\":\"Ana\",\"dataPrazo\":\"2099-01-01\",\"prioridade\":9}"))
                .thenReturn(responde("A prioridade vai só até 5."));

        var resposta = http.postForEntity("/assistente", pedido("Cadastre X", false), AssistenteRespostaDTO.class);

        var passo = resposta.getBody().passos().get(0);
        assertThat(passo.erro()).isTrue();
        assertThat(passo.resultado()).contains("Prioridade máxima é 5");
    }

    @Test
    @DisplayName("só leitura: o modelo nem recebe as ferramentas que alteram dados")
    @SuppressWarnings("unchecked")
    void somenteLeitura() {
        when(modelos.modeloPadrao()).thenReturn("modelo-falso");
        when(modelos.conversar(anyString(), anyList(), anyList())).thenReturn(responde("ok"));

        http.postForEntity("/assistente", pedido("Oi", true), AssistenteRespostaDTO.class);

        ArgumentCaptor<List<ChatCompletions.Ferramenta>> ferramentas = ArgumentCaptor.forClass(List.class);
        verify(modelos).conversar(anyString(), anyList(), ferramentas.capture());
        assertThat(ferramentas.getValue()).extracting(f -> f.function().name())
                .containsExactlyInAnyOrder("listar_tarefas", "buscar_tarefa", "estatisticas_tarefas");
    }

    @Test
    @DisplayName("servidor de modelos fora do ar: 502 no formato ErroDTO, e o status avisa")
    void servidorDeModelosFora() {
        when(modelos.modeloPadrao()).thenReturn("modelo-falso");
        when(modelos.url()).thenReturn("http://localhost:11434/v1");
        when(modelos.conversar(anyString(), anyList(), anyList()))
                .thenThrow(new AssistenteIndisponivelException("Não foi possível falar com o servidor de modelos"));
        when(modelos.listarModelos())
                .thenThrow(new AssistenteIndisponivelException("Não foi possível falar com o servidor de modelos"));

        var resposta = http.postForEntity("/assistente", pedido("Oi", false), Map.class);
        assertThat(resposta.getStatusCode().value()).isEqualTo(502);
        assertThat(resposta.getBody()).containsKey("mensagem");

        var status = http.getForObject("/assistente/status", AssistenteStatusDTO.class);
        assertThat(status.disponivel()).isFalse();
        assertThat(status.mensagem()).contains("servidor de modelos");
    }

    @Test
    @DisplayName("conversa vazia: 400")
    void conversaVazia() {
        var resposta = http.postForEntity("/assistente", Map.of("mensagens", List.of()), Map.class);
        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }
}
