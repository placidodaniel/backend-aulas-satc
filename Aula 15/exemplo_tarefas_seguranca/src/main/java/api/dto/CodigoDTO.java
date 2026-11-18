package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Aula 15: um código de 6 dígitos do aplicativo (usado em POST /auth/2fa/ativar).
@Schema(description = "Código do aplicativo autenticador")
public record CodigoDTO(
        @Schema(example = "123456")
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "\\d{6}", message = "deve ter 6 dígitos")
        String codigo
) {
}
