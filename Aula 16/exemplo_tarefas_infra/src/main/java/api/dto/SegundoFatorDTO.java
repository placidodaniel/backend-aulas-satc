package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Aula 15: o corpo de POST /auth/login/2fa: o desafio recebido no login + o código do aplicativo.
@Schema(description = "Segunda etapa do login, para contas com 2FA")
public record SegundoFatorDTO(
        @Schema(description = "O 'desafio' devolvido por POST /auth/login")
        @NotBlank(message = "é obrigatório")
        String desafio,

        @Schema(description = "Os 6 dígitos que aparecem no aplicativo autenticador", example = "123456")
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "\\d{6}", message = "deve ter 6 dígitos")
        String codigo
) {
}
