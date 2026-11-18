package api.messaging;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

import api.config.RabbitConfig;
import api.dto.TarefaEventoDTO;
import api.model.Tarefa;

// O lado que PUBLICA: o Service avisa "isto aconteceu" e segue a vida, sem saber quem escuta.
@Component
public class TarefaEventoPublisher {

    private static final Logger log = LoggerFactory.getLogger(TarefaEventoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MeterRegistry metricas;

    public TarefaEventoPublisher(RabbitTemplate rabbitTemplate, MeterRegistry metricas) {
        this.rabbitTemplate = rabbitTemplate;
        this.metricas = metricas;
    }

    // tipo = "criada", "concluida", "reaberta" ou "removida". Vira a routing key "tarefa.<tipo>".
    public void publicar(String tipo, Tarefa tarefa) {
        publicar(tipo, tarefa.getId(), tarefa.getTitulo());
    }

    public void publicar(String tipo, Long tarefaId, String titulo) {
        TarefaEventoDTO evento = new TarefaEventoDTO(tipo.toUpperCase(), tarefaId, titulo, Instant.now());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "tarefa." + tipo, evento);
            metricas.counter("tarefas.eventos.publicados", "tipo", tipo, "resultado", "ok").increment();
            log.info("Evento publicado: tarefa.{} (tarefa {})", tipo, tarefaId);
        } catch (AmqpException e) {
            // A tarefa já foi salva no banco: o broker fora do ar não pode derrubar o cadastro.
            metricas.counter("tarefas.eventos.publicados", "tipo", tipo, "resultado", "falha").increment();
            log.warn("Não foi possível publicar tarefa.{} (tarefa {}): {}", tipo, tarefaId, e.getMessage());
        }
    }
}
