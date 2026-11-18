package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Aula 15: o corpo de POST /auth/login.
@Schema(description = "E-mail e senha")
public record LoginDTO(
        @Schema(example = "ana@satc.edu.br")
        @NotBlank(message = "é obrigatório")
        @Size(max = 255, message = "deve ter no máximo 255 caracteres")
        String email,

        @Schema(example = "uma frase longa e facil de lembrar")
        @NotBlank(message = "é obrigatório")
        @Size(max = 128, message = "deve ter no máximo 128 caracteres")
        String senha
) {
    @Override
    public String toString() {
        return "LoginDTO[email=" + email + ", senha=***]";
    }
}
