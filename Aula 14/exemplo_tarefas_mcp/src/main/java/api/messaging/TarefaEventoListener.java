package api.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

import api.config.RabbitConfig;
import api.dto.TarefaEventoDTO;

// O lado que CONSOME: lê a fila sozinho, em outra thread, e não bloqueia a requisição HTTP.
// Aqui só registramos no log; num sistema real seria e-mail, push, auditoria, etc.
@Component
public class TarefaEventoListener {

    private static final Logger log = LoggerFactory.getLogger(TarefaEventoListener.class);

    private final MeterRegistry metricas;

    public TarefaEventoListener(MeterRegistry metricas) {
        this.metricas = metricas;
    }

    @RabbitListener(queues = RabbitConfig.FILA_NOTIFICACOES)
    public void receber(TarefaEventoDTO evento) {
        metricas.counter("tarefas.eventos.consumidos", "tipo", evento.tipo().toLowerCase()).increment();
        log.info("[NOTIFICAÇÃO] Tarefa {} \"{}\": {} (em {})",
                evento.tarefaId(), evento.titulo(), evento.tipo(), evento.ocorridoEm());
    }
}
