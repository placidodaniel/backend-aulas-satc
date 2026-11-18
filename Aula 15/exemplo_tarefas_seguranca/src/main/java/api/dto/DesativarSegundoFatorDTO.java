package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Aula 15: desligar o 2FA pede a senha E um código. Só o token não basta:
// se alguém pegar o token (ou o computador destravado), não consegue tirar a proteção.
@Schema(description = "Senha atual e código do aplicativo")
public record DesativarSegundoFatorDTO(
        @NotBlank(message = "é obrigatório")
        @Size(max = 128, message = "deve ter no máximo 128 caracteres")
        String senha,

        @Schema(example = "123456")
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "\\d{6}", message = "deve ter 6 dígitos")
        String codigo
) {
    @Override
    public String toString() {
        return "DesativarSegundoFatorDTO[senha=***, codigo=***]";
    }
}
