// Declara o pacote dos DTOs -- as classes que formam o contrato da API.
package api.dto;

// Importa o tipo de data e hora do momento do erro.
import java.time.LocalDateTime;
// Importa a lista de erros por campo.
import java.util.List;

// Importa a anotação que documenta cada campo no Swagger.
import io.swagger.v3.oas.annotations.media.Schema;

// Formato ÚNICO de erro da API -- todo 400 e todo 404 saem assim (Aula 09,
// Exercício 5). O cliente lê qualquer erro do mesmo jeito: "mensagem" para
// mostrar ao usuário e "campos" para marcar cada campo inválido do formulário.
//
// Um DTO de saída como outro qualquer: é o ApiExceptionHandler que monta.
// Nunca inclui stack trace nem nome de classe Java -- isso é detalhe interno
// do servidor e não interessa (nem deve chegar) ao cliente.
// Descreve o DTO inteiro na seção "Schemas" do Swagger.
@Schema(description = "Formato padrão de erro da API")
public record ErroDTO(

        // O mesmo número da linha de status HTTP, repetido no corpo.
        @Schema(description = "Status HTTP", example = "404")
        int status,

        // Nome padrão do status (reason phrase).
        @Schema(description = "Nome do status HTTP", example = "Not Found")
        String erro,

        // Mensagem pensada para o usuário final.
        @Schema(description = "O que deu errado, em português", example = "Livro não encontrado: 999")
        String mensagem,

        // Qual URL foi chamada.
        @Schema(description = "Caminho da requisição", example = "/livros/999")
        String caminho,

        // Quando aconteceu (horário do servidor).
        @Schema(description = "Momento do erro", example = "2026-10-06T19:30:00")
        LocalDateTime timestamp,

        // Um item por campo inválido; lista vazia quando o erro não é de validação.
        @Schema(description = "Erros de validação por campo (vazio quando não se aplica)")
        List<CampoErroDTO> campos

// O corpo do record fica vazio: tudo o que ele precisa, o compilador gera.
) {
}
