// Pacote da "porta MCP" da aplicação (Aula 14).
package api.mcp;

// Importa a lista de mensagens do prompt.
import java.util.List;

// Importa as anotações de RECURSO e de PROMPT do MCP.
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.McpResource;
// Importa a anotação que registra a classe como bean.
import org.springframework.stereotype.Component;

// Importa os tipos do protocolo MCP usados para montar um prompt.
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

// =====================================================================
// AULA 14: as outras duas "primitivas" do MCP, além das ferramentas.
// =====================================================================
//
//   Ferramenta (tool)  → uma AÇÃO que a IA decide chamar (TarefaMcpTools).
//   Recurso (resource) → um TEXTO/DADO que o usuário ou o cliente anexa ao contexto,
//                        identificado por uma URI (aqui: tarefas://regras).
//   Prompt             → um MODELO DE PEDIDO pronto, que o usuário escolhe num menu
//                        (no Claude Code aparece como /mcp__tarefas__planejar_semana).
@Component
public class TarefaMcpContexto {

    // Recurso: as regras de negócio do sistema, em Markdown.
    // A IA lê isto para saber o que é permitido ANTES de tentar cadastrar.
    @McpResource(uri = "tarefas://regras",
            name = "regras-de-negocio",
            title = "Regras de negócio das tarefas",
            description = "As regras que a API aplica ao cadastrar e concluir tarefas",
            mimeType = "text/markdown")
    public String regras() {
        return """
                # Regras de negócio das tarefas

                1. Título e responsável são obrigatórios (não podem ficar em branco).
                2. O prazo (dataPrazo) é obrigatório, no formato yyyy-MM-dd, e não pode ser no passado.
                3. A prioridade vai de 1 (mais baixa) a 5 (mais alta).
                4. Uma tarefa está ATRASADA quando não foi concluída e o prazo já passou.
                5. Concluir uma tarefa já concluída não muda nada.
                6. Cada tarefa criada ou concluída gera um evento no RabbitMQ (tarefas.eventos).
                """;
    }

    // Prompt: um pedido pronto para planejar a semana de alguém.
    // O argumento "responsavel" aparece como campo para o usuário preencher.
    @McpPrompt(name = "planejar_semana",
            title = "Planejar a semana",
            description = "Monta um plano da semana para um responsável, a partir das tarefas abertas")
    public GetPromptResult planejarSemana(
            @McpArg(name = "responsavel", description = "Nome de quem vai seguir o plano", required = true)
            String responsavel) {

        // O texto abaixo é o que vai para a IA, como se o usuário tivesse digitado.
        String pedido = """
                Use as ferramentas do servidor "tarefas" para planejar a semana de %s:
                1. Chame listar_tarefas com situacao=abertas e responsavel=%s.
                2. Separe as atrasadas e coloque-as primeiro.
                3. Distribua o resto de segunda a sexta, por prazo e depois por prioridade (5 primeiro).
                4. Responda com uma tabela (dia, id, título, prazo, prioridade) e uma frase de alerta
                   se houver mais de 3 tarefas no mesmo dia.
                Não cadastre nem conclua nada: este pedido é só de leitura.
                """.formatted(responsavel, responsavel);

        return new GetPromptResult("Plano da semana de " + responsavel,
                List.of(new PromptMessage(Role.USER, new TextContent(pedido))));
    }
}
