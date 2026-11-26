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
        // Aula 15: a mensagem vem da fila, e qualquer um com acesso ao RabbitMQ pode publicar.
        // "tipo" vira etiqueta de métrica: só aceitamos os tipos conhecidos (um tipo
        // inventado por mensagem criaria uma série nova no Prometheus a cada vez).
        String tipo = TIPOS_CONHECIDOS.contains(evento.tipo()) ? evento.tipo().toLowerCase() : "desconhecido";
        metricas.counter("tarefas.eventos.consumidos", "tipo", tipo).increment();
        // O título é texto do usuário: tiramos quebras de linha para ele não forjar
        // linhas falsas no log ("log injection").
        log.info("[NOTIFICAÇÃO] Tarefa {} \"{}\": {} (em {})",
                evento.tarefaId(), limpar(evento.titulo()), tipo, evento.ocorridoEm());
    }

    private static final java.util.Set<String> TIPOS_CONHECIDOS = java.util.Set.of("CRIADA", "CONCLUIDA", "REABERTA", "REMOVIDA");

    private static String limpar(String texto) {
        return texto == null ? "" : texto.replaceAll("[\\r\\n\\t]", " ");
    }
}
