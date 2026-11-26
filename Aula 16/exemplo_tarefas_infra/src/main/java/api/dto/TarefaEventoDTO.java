package api.dto;

import java.time.Instant;

// O evento que vai para o RabbitMQ: "algo aconteceu com uma tarefa".
// É um fato já ocorrido (por isso o nome no passado: CRIADA, CONCLUIDA...), e não uma ordem.
public record TarefaEventoDTO(

        // O que aconteceu: CRIADA, CONCLUIDA, REABERTA ou REMOVIDA.
        String tipo,

        // O id da tarefa envolvida.
        Long tarefaId,

        // O título, para o consumidor não precisar voltar ao banco só para mostrar uma frase.
        String titulo,

        // Quando o evento foi gerado.
        Instant ocorridoEm

) {
}
