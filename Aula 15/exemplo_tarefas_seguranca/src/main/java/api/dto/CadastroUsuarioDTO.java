package api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Aula 15: o corpo de POST /auth/cadastro.
//
// Regra da senha (a recomendação atual do NIST, SP 800-63B): o que mais pesa é o
// TAMANHO. Exigir "uma maiúscula, um número e um símbolo" só gera "Senha@123".
// Por isso: mínimo de 8, máximo de 64 (o BCrypt só usa 72 bytes) e uma lista
// de senhas comuns proibidas, conferida no AutenticacaoService.
@Schema(description = "Dados para criar uma conta")
public record CadastroUsuarioDTO(

        @Schema(example = "Ana Souza")
        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String nome,

        @Schema(example = "ana@satc.edu.br")
        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 255, message = "deve ter no máximo 255 caracteres")
        String email,

        @Schema(example = "uma frase longa e facil de lembrar")
        @NotBlank(message = "é obrigatório")
        @Size(min = 8, max = 64, message = "deve ter entre 8 e 64 caracteres")
        String senha
) {
    // toString próprio: o padrão do record imprimiria a senha se alguém logasse o DTO.
    @Override
    public String toString() {
        return "CadastroUsuarioDTO[nome=" + nome + ", email=" + email + ", senha=***]";
    }
}
