// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a lista de tarefas do responsável.
import java.util.List;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Exercício 4: DTO COMPOSTO -- um responsável com as tarefas dele dentro.
//
// Na Aula 08, colocar List<Tarefa> na entidade Responsavel faria o Jackson
// entrar em loop (responsável → tarefas → responsável → ...). Aqui não existe
// loop porque o formato é montado à mão, de cima para baixo: a lista é de
// TarefaResumoDTO, que não aponta de volta para o responsável. A entidade
// Responsavel continua sem @OneToMany -- as tarefas vêm de uma segunda
// consulta (TarefaRepository.findByResponsavelVinculadoId).
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Responsável com o resumo das tarefas vinculadas a ele")
public record ResponsavelDetalheDTO(

        // Mesmos três campos do ResponsavelResponseDTO.
        @Schema(description = "Identificador gerado pelo banco", example = "1")
        Long id,

        // Nome do responsável.
        @Schema(description = "Nome do responsável", example = "Ana Souza")
        String nome,

        // E-mail do responsável.
        @Schema(description = "E-mail do responsável", example = "ana@satc.edu.br")
        String email,

        // Calculado: tamanho da lista abaixo.
        @Schema(description = "Quantidade de tarefas vinculadas", example = "2")
        int totalTarefas,

        // Calculado: quantas da lista ainda não foram concluídas.
        @Schema(description = "Quantas dessas tarefas ainda não foram concluídas", example = "1")
        int tarefasPendentes,

        // Resumo de cada tarefa vinculada (pode ser uma lista vazia).
        @Schema(description = "Tarefas vinculadas ao responsável")
        List<TarefaResumoDTO> tarefas

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
