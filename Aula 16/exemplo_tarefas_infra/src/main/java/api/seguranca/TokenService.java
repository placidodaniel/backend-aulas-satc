package api.seguranca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import api.model.Usuario;

// =====================================================================
// AULA 15: emite e confere os tokens JWT.
// =====================================================================
//
// Um JWT tem três partes separadas por ponto: CABEÇALHO.CONTEÚDO.ASSINATURA
//
//   cabeçalho  {"alg":"HS256"}                         (como foi assinado)
//   conteúdo   {"sub":"ana@satc.edu.br","exp":...}     (as "claims")
//   assinatura HMAC-SHA256(cabeçalho.conteúdo, SEGREDO DO SERVIDOR)
//
// O conteúdo NÃO é criptografado, só codificado em Base64: qualquer um lê
// (cole um token em jwt.io e veja). Por isso nunca vai senha nem dado
// sensível dentro dele. O que o servidor garante é que ninguém ALTERA o
// conteúdo: mudou uma letra, a assinatura não bate e o token é recusado.
//
// Usamos dois tipos de token, separados pelo "scope":
//   - "tarefas":        o token de acesso, que abre a API (30 minutos);
//   - "segundo-fator":  o "desafio", entregue depois da senha certa quando a
//                       conta tem 2FA. Só serve para trocar pelo de acesso,
//                       junto com o código do aplicativo (5 minutos).
@Service
public class TokenService {

    public static final String EMISSOR = "exemplo-tarefas";
    public static final String ESCOPO_ACESSO = "tarefas";
    public static final String ESCOPO_DESAFIO = "segundo-fator";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Clock relogio;
    private final Duration validadeAcesso;
    private final Duration validadeDesafio;

    public TokenService(JwtEncoder encoder, JwtDecoder decoder, Clock relogio,
                        @Value("${seguranca.jwt.validade:30m}") Duration validadeAcesso,
                        @Value("${seguranca.jwt.validade-desafio:5m}") Duration validadeDesafio) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.relogio = relogio;
        this.validadeAcesso = validadeAcesso;
        this.validadeDesafio = validadeDesafio;
    }

    // O token que abre a API. Devolve o JWT pronto (texto e data de expiração).
    public Jwt gerarAcesso(Usuario usuario) {
        return gerar(usuario, ESCOPO_ACESSO, validadeAcesso);
    }

    // O "desafio" do segundo fator: prova que a SENHA já foi conferida, e nada mais.
    public Jwt gerarDesafio(Usuario usuario) {
        return gerar(usuario, ESCOPO_DESAFIO, validadeDesafio);
    }

    // Confere a assinatura, a validade e o tipo do desafio. Devolve o id do usuário.
    public long lerDesafio(String desafio) {
        try {
            Jwt jwt = decoder.decode(desafio);
            if (!ESCOPO_DESAFIO.equals(jwt.getClaimAsString("scope"))) {
                throw new CredenciaisInvalidasException("Desafio inválido. Faça o login de novo.");
            }
            return Long.parseLong(jwt.getClaimAsString("uid"));
        } catch (JwtException | IllegalArgumentException e) {
            // Assinatura errada, expirado, malformado: tudo vira a mesma resposta.
            throw new CredenciaisInvalidasException("Desafio inválido ou expirado. Faça o login de novo.");
        }
    }

    private Jwt gerar(Usuario usuario, String escopo, Duration validade) {
        Instant agora = relogio.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)                          // iss: quem emitiu
                .subject(usuario.getEmail())              // sub: de quem é o token
                .issuedAt(agora)                          // iat: quando foi emitido
                .expiresAt(agora.plus(validade))          // exp: até quando vale
                .id(UUID.randomUUID().toString())         // jti: identificador único
                .claim("uid", usuario.getId().toString())
                .claim("nome", usuario.getNome())
                .claim("scope", escopo)                   // vira a autoridade SCOPE_<escopo>
                .build();
        // HS256 = HMAC com SHA-256: a mesma chave assina e confere (chave simétrica).
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecalho, claims));
    }
}
