// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data aceito no JSON da API.
import java.time.LocalDate;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;
// Importa a validação que rejeita datas no passado.
import jakarta.validation.constraints.FutureOrPresent;
// Importa as validações de valor mínimo e máximo.
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
// Importa a validação que rejeita texto nulo, vazio ou só com espaços.
import jakarta.validation.constraints.NotBlank;
// Importa a validação que rejeita valores nulos.
import jakarta.validation.constraints.NotNull;

// >>> DTO DE ENTRADA (request): o JSON que o cliente MANDA no POST e no PUT.
//
// Na Aula 08 existia um TarefaDTO só, usado para entrar -- e a resposta era a
// própria entidade Tarefa. Agora o contrato tem dois lados, cada um com o seu
// DTO:
//   - TarefaRequestDTO  (este arquivo): o que o cliente PODE enviar;
//   - TarefaResponseDTO: o que a API DEVOLVE.
//
// Aqui não existem "id", "concluida" nem "dataCadastro": quem decide esses
// valores é a API (o banco gera o id, a entidade preenche a data e a tarefa
// nasce pendente). Se o cliente mandar esses campos no JSON, o Jackson
// simplesmente ignora -- não existe onde guardar. Isso bloqueia o "mass
// assignment" (o cliente forçar um valor que não deveria controlar).
//
// >>> ENTRADA x SAÍDA: o vínculo com o responsável entra como um ID
// ("responsavelId": 1) e sai como um OBJETO ("responsavelVinculado":
// {id, nome, email}, no TarefaResponseDTO). O cliente só diz QUAL responsável;
// nome e e-mail já estão no banco. Quem transforma o id em entidade é o
// TarefaService (o mapper não acessa o banco).
//
// >>> RECORD: a partir desta aula os DTOs são records (Java 16+). Um record é
// uma classe imutável feita só para carregar dados: o compilador gera sozinho
// o construtor, os "getters" (titulo(), e não getTitulo()), equals, hashCode e
// toString. É exatamente o que um DTO precisa -- sem setter, ninguém altera o
// dado no meio do caminho entre o Controller e o Service.
//
// As anotações de validação continuam as mesmas da Aula 08, agora escritas
// direto em cada componente do record. @Schema não valida nada: só descreve o
// campo e dá o exemplo que aparece no Swagger UI.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Dados que o cliente envia para criar ou atualizar uma tarefa")
public record TarefaRequestDTO(

        // Título obrigatório -- @NotBlank rejeita null, "" e "   ".
        @Schema(description = "Título da tarefa", example = "Estudar DTO e Swagger")
        @NotBlank(message = "Título é obrigatório")
        String titulo,

        // Nome do responsável em texto livre (campo da Aula 07).
        @Schema(description = "Nome de quem vai fazer a tarefa", example = "Ana")
        @NotBlank(message = "Responsável é obrigatório")
        String responsavel,

        // @NotNull porque @FutureOrPresent sozinho considera null válido.
        @Schema(description = "Data limite, no formato yyyy-MM-dd (hoje ou depois)", example = "2026-12-01")
        @NotNull(message = "Data de prazo é obrigatória")
        @FutureOrPresent(message = "Data de prazo não pode ser no passado")
        LocalDate dataPrazo,

        // Integer (e não int) para que a ausência do campo chegue como null e
        // seja barrada pelo @NotNull, em vez de virar 0 sem ninguém perceber.
        @Schema(description = "Prioridade de 1 (mais baixa) a 5 (mais alta)", example = "3")
        @NotNull(message = "Prioridade é obrigatória")
        @Min(value = 1, message = "Prioridade mínima é 1")
        @Max(value = 5, message = "Prioridade máxima é 5")
        Integer prioridade,

        // Opcional: id de um responsável cadastrado, para a tarefa já nascer
        // vinculada (sem precisar do PUT /tarefas/{id}/responsavel depois).
        // Sem @NotNull: ausente ou null = tarefa sem vínculo. Id inexistente = 404.
        @Schema(description = "Id de um responsável cadastrado (opcional): a tarefa já nasce vinculada a ele", example = "null")
        Long responsavelId

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
