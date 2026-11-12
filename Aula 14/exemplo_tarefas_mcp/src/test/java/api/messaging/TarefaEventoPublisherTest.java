package api.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import api.config.RabbitConfig;
import api.dto.TarefaEventoDTO;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class TarefaEventoPublisherTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final SimpleMeterRegistry metricas = new SimpleMeterRegistry();
    private final TarefaEventoPublisher publisher = new TarefaEventoPublisher(rabbitTemplate, metricas);

    @Test
    @DisplayName("publica na exchange certa, com routing key tarefa.<tipo> e o evento preenchido")
    void publicaNaExchange() {
        publisher.publicar("criada", 10L, "Estudar");

        ArgumentCaptor<Object> corpo = forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq("tarefa.criada"), corpo.capture());
        TarefaEventoDTO evento = (TarefaEventoDTO) corpo.getValue();
        assertThat(evento.tipo()).isEqualTo("CRIADA");
        assertThat(evento.tarefaId()).isEqualTo(10L);
        assertThat(evento.titulo()).isEqualTo("Estudar");
        assertThat(evento.ocorridoEm()).isNotNull();
        assertThat(metricas.counter("tarefas.eventos.publicados", "tipo", "criada", "resultado", "ok").count())
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("RabbitMQ fora do ar não propaga erro (a tarefa já foi salva) e conta como falha")
    void brokerForaDoAr() {
        doThrow(new AmqpConnectException(new RuntimeException("recusado")))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));

        assertThatCode(() -> publisher.publicar("criada", 1L, "x")).doesNotThrowAnyException();
        assertThat(metricas.counter("tarefas.eventos.publicados", "tipo", "criada", "resultado", "falha").count())
                .isEqualTo(1.0);
    }
}
