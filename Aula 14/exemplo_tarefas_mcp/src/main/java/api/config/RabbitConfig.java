package api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Declara a topologia do RabbitMQ: exchange, fila e a ligação (binding) entre as duas.
// O Spring cria tudo no broker na primeira conexão, se ainda não existir.
//
//   publisher ──> [exchange tarefas.eventos] ──(tarefa.*)──> [fila tarefas.notificacoes] ──> listener
@Configuration
public class RabbitConfig {

    // Nome da exchange: quem publica manda para ela, nunca direto para a fila.
    public static final String EXCHANGE = "tarefas.eventos";

    // Nome da fila lida pelo TarefaEventoListener.
    public static final String FILA_NOTIFICACOES = "tarefas.notificacoes";

    // Padrão de roteamento: "tarefa.criada", "tarefa.concluida"... todos casam com "tarefa.*".
    public static final String ROUTING_KEY_PADRAO = "tarefa.*";

    // Exchange do tipo topic: roteia pela routing key, aceitando curingas (* e #).
    @Bean
    public TopicExchange tarefasExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // Fila durável: sobrevive a um restart do broker.
    @Bean
    public Queue notificacoesQueue() {
        return QueueBuilder.durable(FILA_NOTIFICACOES).build();
    }

    // Liga a fila à exchange: "toda mensagem tarefa.* que chegar na exchange vai para a fila".
    @Bean
    public Binding notificacoesBinding(Queue notificacoesQueue, TopicExchange tarefasExchange) {
        return BindingBuilder.bind(notificacoesQueue).to(tarefasExchange).with(ROUTING_KEY_PADRAO);
    }

    // Serializa/desserializa as mensagens como JSON (o padrão seria serialização Java, ilegível no painel).
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
