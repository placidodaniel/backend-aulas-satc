package api.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

// Aula 15: a resposta do login. Dois formatos possíveis:
//
//  1. Conta SEM 2FA (ou depois do código certo):
//     { "segundoFatorPendente": false, "token": "eyJ...", "expiraEm": "...", "usuario": {...} }
//
//  2. Conta COM 2FA, senha certa:
//     { "segundoFatorPendente": true, "desafio": "eyJ...", "expiraEm": "..." }
//     O desafio não abre a API: só serve para POST /auth/login/2fa junto com o código.
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado do login")
public record LoginRespostaDTO(
        @Schema(description = "true: falta o código do aplicativo (use o desafio em /auth/login/2fa)")
        boolean segundoFatorPendente,
        @Schema(description = "Token de acesso: envie em Authorization: Bearer <token>")
        String token,
        @Schema(description = "Token curto da 2ª etapa (só quando segundoFatorPendente = true)")
        String desafio,
        @Schema(description = "Quando o token (ou o desafio) deixa de valer")
        Instant expiraEm,
        UsuarioDTO usuario
) {
    public static LoginRespostaDTO concluido(String token, Instant expiraEm, UsuarioDTO usuario) {
        return new LoginRespostaDTO(false, token, null, expiraEm, usuario);
    }

    public static LoginRespostaDTO pendente(String desafio, Instant expiraEm) {
        return new LoginRespostaDTO(true, null, desafio, expiraEm, null);
    }
}
