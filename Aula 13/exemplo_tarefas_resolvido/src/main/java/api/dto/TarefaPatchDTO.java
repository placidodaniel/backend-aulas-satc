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
// Importa a validação por expressão regular.
import jakarta.validation.constraints.Pattern;

// Exercício 3: corpo do PATCH /tarefas/{id} -- atualização PARCIAL.
//
// Mesmos cinco campos do TarefaRequestDTO, mas todos opcionais: o cliente manda só o
// que quer mudar. Campo ausente no JSON chega aqui como null, e null significa
// "não mexer" (o TarefaMapper.applyPatch() pula esses campos).
//
// Por isso não existe nenhum @NotNull/@NotBlank aqui. As outras validações
// continuam valendo -- mas só quando o campo vem preenchido, porque @Min, @Max,
// @FutureOrPresent e @Pattern consideram null válido. É isso que permite
// corrigir o título de uma tarefa atrasada sem reenviar o prazo (que seria
// barrado pelo @FutureOrPresent no PUT).
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Campos que o cliente quer alterar numa tarefa; campo ausente (ou null) não é alterado")
public record TarefaPatchDTO(

        // @NotBlank barraria o null ("não mexer"). A regex exige pelo menos um
        // caractere que não seja espaço -- rejeita "" e "   ", aceita null.
        // (?s) deixa o "." casar também com quebra de linha.
        @Schema(description = "Novo título (opcional)", example = "Estudar DTO, Swagger e PATCH")
        @Pattern(regexp = "(?s).*\\S.*", message = "Título não pode ficar em branco")
        String titulo,

        // Mesma regra do título.
        @Schema(description = "Novo responsável em texto (opcional)", example = "Bruno")
        @Pattern(regexp = "(?s).*\\S.*", message = "Responsável não pode ficar em branco")
        String responsavel,

        // Se vier, não pode ser no passado; se não vier, o prazo atual fica.
        @Schema(description = "Novo prazo, hoje ou depois (opcional)", example = "2026-12-15")
        @FutureOrPresent(message = "Data de prazo não pode ser no passado")
        LocalDate dataPrazo,

        // Se vier, precisa estar entre 1 e 5.
        @Schema(description = "Nova prioridade de 1 a 5 (opcional)", example = "5")
        @Min(value = 1, message = "Prioridade mínima é 1")
        @Max(value = 5, message = "Prioridade máxima é 5")
        Integer prioridade,

        // Se vier, a tarefa passa a ser deste responsável (404 se não existir).
        // Repare no limite do "null = não mexer": {"responsavelId": null} é igual
        // a {} -- o PATCH não consegue DESVINCULAR (subexercício 3.2). Para isso
        // existe o DELETE /tarefas/{id}/responsavel.
        @Schema(description = "Novo responsável vinculado (opcional); para desvincular, use DELETE /tarefas/{id}/responsavel", example = "null")
        Long responsavelId

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
