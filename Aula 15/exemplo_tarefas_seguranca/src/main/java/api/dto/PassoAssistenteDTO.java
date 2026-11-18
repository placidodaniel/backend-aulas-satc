// Declara o pacote dos DTOs: as classes que formam o contrato da API.
package api.dto;

// Importa a documentação do Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Aula 14: uma ferramenta que o modelo pediu e o host executou pelo MCP.
// A tela mostra a lista, para ver o que a IA fez por baixo da resposta.
@Schema(description = "Uma chamada de ferramenta MCP feita durante a resposta")
public record PassoAssistenteDTO(

        @Schema(description = "Nome da ferramenta MCP", example = "estatisticas_tarefas")
        String ferramenta,

        @Schema(description = "Argumentos que o modelo escolheu, em JSON", example = "{}")
        String argumentos,

        @Schema(description = "O que o servidor MCP devolveu (texto ou JSON)")
        String resultado,

        @Schema(description = "true quando a ferramenta respondeu com erro (isError)", example = "false")
        boolean erro
) {
}
