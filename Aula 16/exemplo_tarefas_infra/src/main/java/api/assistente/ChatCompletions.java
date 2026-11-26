// Pacote do assistente da tela (Aula 14).
package api.assistente;

// Importa as listas e o "dicionário" usados no JSON.
import java.util.List;
import java.util.Map;

// Importa as anotações do Jackson que ajustam o JSON.
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// =====================================================================
// AULA 14: o formato "chat completions", o JSON que os servidores de modelo entendem.
// =====================================================================
//
// Quase todo servidor de modelos de IA aceita o MESMO formato de pedido,
// o "chat completions" (criado pela OpenAI e copiado por todo mundo):
//   - Ollama, rodando o modelo na sua máquina (o padrão deste projeto, sem chave);
//   - OpenRouter, um "balcão único" de modelos na nuvem (precisa de chave);
//   - e vários outros.
// Por isso o assistente troca de servidor mudando só a URL (e a chave, se houver).
//
// Estes records são só o CONTRATO desse JSON, igual aos DTOs do Open-Meteo
// da Aula 12. Ficam juntos nesta classe porque só fazem sentido juntos.
//
// @JsonInclude(NON_NULL): campos null não vão no JSON (alguns servidores
// reclamam de "tool_calls": null, por exemplo).
// @JsonIgnoreProperties(ignoreUnknown = true): campos que a resposta traz
// e nós não usamos são ignorados, em vez de dar erro.
public final class ChatCompletions {

    // Ninguém cria objeto desta classe: ela só agrupa os records.
    private ChatCompletions() {
    }

    // O que mandamos: o modelo, a conversa até agora e as ferramentas disponíveis.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Pedido(String model, List<Mensagem> messages, List<Ferramenta> tools) {
    }

    // Uma mensagem da conversa. "role" pode ser:
    //   system    → instruções gerais para o modelo;
    //   user      → o que a pessoa digitou;
    //   assistant → o que o modelo respondeu (texto OU pedidos de ferramenta);
    //   tool      → o resultado de uma ferramenta, ligado ao pedido pelo tool_call_id.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mensagem(
            String role,
            String content,
            @JsonProperty("tool_calls") List<ChamadaFerramenta> toolCalls,
            @JsonProperty("tool_call_id") String toolCallId) {

        // Atalhos para criar as mensagens mais comuns.
        public static Mensagem sistema(String texto) {
            return new Mensagem("system", texto, null, null);
        }

        public static Mensagem usuario(String texto) {
            return new Mensagem("user", texto, null, null);
        }

        public static Mensagem assistente(String texto) {
            return new Mensagem("assistant", texto, null, null);
        }

        public static Mensagem resultadoDeFerramenta(String idDaChamada, String resultado) {
            return new Mensagem("tool", resultado, null, idDaChamada);
        }

        // true quando o modelo pediu para chamar ferramentas (em vez de responder).
        public boolean pediuFerramentas() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }

    // Um pedido do modelo: "chame a ferramenta X com estes argumentos".
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChamadaFerramenta(String id, String type, Funcao function) {
    }

    // Nome da ferramenta e os argumentos. ATENÇÃO: "arguments" vem como TEXTO
    // com JSON dentro (ex.: "{\"id\": 3}"), e não como objeto.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Funcao(String name, String arguments) {
    }

    // Uma ferramenta oferecida ao modelo. É a tradução de uma ferramenta MCP
    // (nome, descrição e JSON Schema) para o formato do chat completions.
    public record Ferramenta(String type, DefinicaoFerramenta function) {
    }

    public record DefinicaoFerramenta(String name, String description, Map<String, Object> parameters) {
    }

    // O que volta: uma lista de "escolhas" (na prática, sempre uma) ou um erro.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String model, List<Escolha> choices, Erro error) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Escolha(Mensagem message, @JsonProperty("finish_reason") String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Erro(String message, Object code) {
    }

    // GET /models: a lista de modelos que o servidor oferece.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListaDeModelos(List<InfoModelo> data) {
    }

    // "supported_parameters" só vem no OpenRouter; quando vem, diz se o modelo aceita ferramentas.
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InfoModelo(String id, @JsonProperty("supported_parameters") List<String> supportedParameters) {
    }
}
