package api.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import api.dto.ErroDTO;
import api.dto.TarefaResponseDTO;
import io.micrometer.core.instrument.MeterRegistry;

class TarefaApiE2ETest extends AbstractE2ETest {

    @Autowired
    MeterRegistry metricas;

    private Map<String, Object> corpo(String titulo, int prioridade) {
        return Map.of("titulo", titulo, "responsavel", "Ana",
                "dataPrazo", LocalDate.now().plusDays(5).toString(), "prioridade", prioridade);
    }

    private TarefaResponseDTO criar(String titulo) {
        ResponseEntity<TarefaResponseDTO> r = http.postForEntity("/tarefas", corpo(titulo, 3), TarefaResponseDTO.class);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return r.getBody();
    }

    @Test
    @DisplayName("cria, busca por id e lista: os dados passam pelo PostgreSQL de verdade")
    void criaEBusca() {
        TarefaResponseDTO criada = criar("E2E: criar e buscar");

        assertThat(criada.id()).isNotNull();
        assertThat(criada.concluida()).isFalse();

        ResponseEntity<TarefaResponseDTO> busca = http.getForEntity("/tarefas/" + criada.id(), TarefaResponseDTO.class);
        assertThat(busca.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(busca.getBody().titulo()).isEqualTo("E2E: criar e buscar");

        ResponseEntity<TarefaResponseDTO[]> lista = http.getForEntity("/tarefas", TarefaResponseDTO[].class);
        assertThat(lista.getBody()).extracting(TarefaResponseDTO::id).contains(criada.id());
    }

    @Test
    @DisplayName("dados inválidos: 400 no formato ErroDTO, com a lista de campos")
    void validacao() {
        ResponseEntity<ErroDTO> r = http.postForEntity("/tarefas",
                Map.of("titulo", "", "responsavel", "Ana", "dataPrazo", LocalDate.now().toString(), "prioridade", 9),
                ErroDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().campos()).extracting("campo").contains("titulo", "prioridade");
    }

    @Test
    @DisplayName("id inexistente: 404")
    void naoEncontrada() {
        ResponseEntity<ErroDTO> r = http.getForEntity("/tarefas/999999", ErroDTO.class);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("concluir alterna o estado e remover apaga de verdade")
    void concluiERemove() {
        TarefaResponseDTO t = criar("E2E: concluir e remover");

        ResponseEntity<TarefaResponseDTO> concluida = http.exchange("/tarefas/" + t.id() + "/concluir",
                HttpMethod.PUT, null, TarefaResponseDTO.class);
        assertThat(concluida.getBody().concluida()).isTrue();

        ResponseEntity<Void> removida = http.exchange("/tarefas/" + t.id(), HttpMethod.DELETE, null, Void.class);
        assertThat(removida.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(http.getForEntity("/tarefas/" + t.id(), ErroDTO.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("criar uma tarefa publica um evento no RabbitMQ e o listener o consome")
    void eventoPassaPeloRabbit() {
        double antes = metricas.counter("tarefas.eventos.consumidos", "tipo", "criada").count();

        criar("E2E: evento no RabbitMQ");

        // O consumo é assíncrono (outra thread): Awaitility tenta até dar certo, ou estoura o prazo.
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(metricas.counter("tarefas.eventos.consumidos", "tipo", "criada").count())
                        .isGreaterThan(antes));
        assertThat(metricas.counter("tarefas.eventos.publicados", "tipo", "criada", "resultado", "ok").count())
                .isGreaterThanOrEqualTo(1.0);
    }
}
