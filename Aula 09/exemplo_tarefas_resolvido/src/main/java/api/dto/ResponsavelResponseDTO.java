// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// DTO de saída de um responsável: {id, nome, email}.
//
// Exercício 1: agora é o formato de TODA resposta de responsável -- no
// GET/POST /responsaveis e dentro do "responsavelVinculado" de cada tarefa.
// Quem monta é sempre o ResponsavelMapper.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Responsável como a API devolve para o cliente")
public record ResponsavelResponseDTO(

        // Gerado pelo banco no INSERT.
        @Schema(description = "Identificador gerado pelo banco", example = "1")
        Long id,

        // Nome do responsável.
        @Schema(description = "Nome do responsável", example = "Ana Souza")
        String nome,

        // E-mail do responsável.
        @Schema(description = "E-mail do responsável", example = "ana@satc.edu.br")
        String email

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
