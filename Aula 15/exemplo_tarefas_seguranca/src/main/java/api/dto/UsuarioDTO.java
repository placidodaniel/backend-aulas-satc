package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Aula 15: o usuário, para FORA da API. Sem hash de senha, sem segredo do 2FA,
// sem contador de tentativas: o DTO decide o que sai (Aula 09).
@Schema(description = "Usuário do sistema")
public record UsuarioDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "Ana Souza") String nome,
        @Schema(example = "ana@satc.edu.br") String email,
        @Schema(description = "Se a conta pede o código do aplicativo no login") boolean segundoFatorAtivo
) {
}
