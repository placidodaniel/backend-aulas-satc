// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data usado nos campos do empréstimo.
import java.time.LocalDate;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// CAMADA: DTO
// RESPONSABILIDADE: o que a API devolve sobre um empréstimo. Entra um id, sai
//                   um objeto: leitor e livro voltam completos. dataLimite e
//                   atrasado não existem no banco: o mapper calcula a partir
//                   da regra R3 (prazo de 14 dias).
// SITUAÇÃO: implementada. Será usado pelas rotas de empréstimos na próxima etapa.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Um empréstimo")
public record EmprestimoResponseDTO(

        // Gerado pelo banco no registro.
        @Schema(description = "Id do empréstimo", example = "1")
        Long id,

        // Quem pegou o livro.
        @Schema(description = "O leitor do empréstimo")
        LeitorResponseDTO leitor,

        // Qual livro foi emprestado.
        @Schema(description = "O livro emprestado")
        LivroResponseDTO livro,

        // Dia em que o livro saiu.
        @Schema(description = "Dia da retirada", example = "2026-10-06")
        LocalDate dataRetirada,

        // Dia em que o livro precisa voltar: retirada + 14 dias (R3).
        @Schema(description = "Prazo de devolução", example = "2026-10-20")
        LocalDate dataLimite,

        // Dia em que o livro voltou; null enquanto está em aberto.
        @Schema(description = "Dia da devolução (vazio enquanto está em aberto)", example = "2026-10-15")
        LocalDate dataDevolucao,

        // Verdadeiro quando passou da data limite e o livro ainda não voltou (R3).
        @Schema(description = "Se o empréstimo está atrasado", example = "false")
        boolean atrasado

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
