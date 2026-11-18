package api.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

// =====================================================================
// AULA 15: a configuração de segurança da aplicação inteira.
// =====================================================================
//
// Toda requisição passa por uma CORRENTE DE FILTROS do Spring Security antes
// de chegar ao Controller (ou ao servidor MCP):
//
//   requisição ──> [cabeçalhos] ──> [lê o token Bearer] ──> [pode acessar esta rota?] ──> Controller
//                                        │ inválido: 401              │ não pode: 403
//
// A regra de ouro é NEGAR POR PADRÃO: listamos o pouco que é público e
// TODO o resto exige um token de acesso válido. Uma rota nova criada amanhã
// já nasce protegida, sem ninguém lembrar de proteger.
@Configuration
public class SegurancaConfig {

    private static final Logger log = LoggerFactory.getLogger(SegurancaConfig.class);

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, RespostasDeSeguranca respostas) throws Exception {
        http
                // CSRF: ataque em que OUTRO site faz o navegador da vítima mandar uma
                // requisição usando o COOKIE de login dela. Aqui não há cookie de login:
                // o token vai no cabeçalho Authorization, que o navegador nunca envia
                // sozinho. Por isso a proteção de CSRF pode ser desligada.
                .csrf(csrf -> csrf.disable())

                // Sem sessão no servidor (STATELESS): cada requisição traz o token e é
                // conferida sozinha. Nada de JSESSIONID.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(rotas -> rotas
                        // Entrar e criar conta: públicas, claro (ainda não há token).
                        .requestMatchers(HttpMethod.POST, "/auth/cadastro", "/auth/login", "/auth/login/2fa").permitAll()
                        // As páginas e os scripts. O HTML não tem dado nenhum: os dados vêm da
                        // API, e a API exige token. A própria página manda para o login.
                        .requestMatchers("/", "/*.html", "/js/**", "/css/**", "/favicon.ico", "/error").permitAll()
                        // O contrato da API (Swagger). Em produção, avalie fechar também.
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Saúde (para o Docker/healthcheck) e métricas (para o Prometheus).
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
                        // TODO o resto (tarefas, responsáveis, previsão, assistente, /mcp, /auth/eu...)
                        // exige um token com scope "tarefas". O token de desafio do 2FA tem outro
                        // scope ("segundo-fator") e recebe 403 aqui.
                        .anyRequest().hasAuthority("SCOPE_" + TokenService.ESCOPO_ACESSO))

                // "Resource server": aceita Authorization: Bearer <jwt> e confere com o JwtDecoder abaixo.
                .oauth2ResourceServer(rs -> rs
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas))

                // Cabeçalhos de segurança que o NAVEGADOR obedece. O Spring já manda alguns
                // sozinho (X-Content-Type-Options: nosniff, X-Frame-Options: DENY,
                // Cache-Control: no-store). Acrescentamos:
                .headers(h -> h
                        // CSP: de onde a página pode carregar scripts, estilos, imagens e para
                        // onde pode mandar requisições. Só do próprio servidor ('self'): um
                        // script injetado não consegue nem enviar o token para outro site.
                        // 'unsafe-inline' é uma concessão: as páginas da disciplina têm o
                        // <script> dentro do HTML (veja SEGURANCA.md, item sobre XSS).
                        .contentSecurityPolicy(csp -> csp.policyDirectives(String.join("; ",
                                "default-src 'self'",
                                "script-src 'self' 'unsafe-inline'",
                                "style-src 'self' 'unsafe-inline'",
                                "img-src 'self' data:",
                                "connect-src 'self'",
                                "object-src 'none'",
                                "base-uri 'self'",
                                "form-action 'self'",
                                "frame-ancestors 'none'")))
                        // Não manda o endereço da nossa página para outros sites.
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER))
                        // Câmera e microfone: ninguém. Localização: só o nosso site (a previsão usa).
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                                "geolocation=(self), camera=(), microphone=(), payment=()"))
                        // HSTS ("só fale comigo por HTTPS"): o Spring só envia em conexões HTTPS.
                        .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31_536_000)));
        return http.build();
    }

    // BCrypt: o hash de senha recomendado pelo Spring. Três propriedades importam:
    //  1. tem SAL: cada hash leva bytes aleatórios, então duas pessoas com a mesma
    //     senha têm hashes diferentes (tabelas prontas de hash não servem);
    //  2. é LENTO de propósito: custo 12 = 2^12 rodadas, cerca de 0,2 s por senha.
    //     Para o usuário, nada; para quem testa milhões de senhas, uma eternidade;
    //  3. só usa os primeiros 72 bytes da senha (por isso o limite no cadastro).
    // NUNCA use MD5, SHA-1 ou SHA-256 puro para senha: são rápidos demais.
    @Bean
    public PasswordEncoder codificadorDeSenha(@Value("${seguranca.bcrypt.custo:12}") int custo) {
        return new BCryptPasswordEncoder(custo);
    }

    // A chave que assina os tokens. Quem tem esta chave consegue fabricar um token
    // de QUALQUER usuário: ela é o segredo mais importante da aplicação.
    //  - vem de variável de ambiente (JWT_SEGREDO), nunca escrita no código ou no Git;
    //  - precisa ter pelo menos 32 bytes (256 bits) para o HS256;
    //  - sem a variável, sorteamos uma chave a cada subida (bom para aula: os tokens
    //    antigos param de valer quando a aplicação reinicia).
    @Bean
    public SecretKey chaveDosTokens(@Value("${seguranca.jwt.segredo:}") String segredo) {
        byte[] bytes;
        if (segredo == null || segredo.isBlank()) {
            log.warn("JWT_SEGREDO não definido: usando uma chave aleatória. Os tokens deixam de valer quando a aplicação reiniciar.");
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = segredo.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                // Falha na subida: melhor não subir do que subir com uma chave fraca.
                throw new IllegalStateException("JWT_SEGREDO precisa ter pelo menos 32 caracteres.");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    // Quem ASSINA os tokens (usado no login, pelo TokenService).
    @Bean
    public JwtEncoder jwtEncoder(SecretKey chave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    // Quem CONFERE os tokens em toda requisição: assinatura (HS256 com a nossa chave),
    // validade (exp/nbf) e emissor (iss). Token de outro emissor é recusado.
    @Bean
    public JwtDecoder jwtDecoder(SecretKey chave) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.EMISSOR));
        return decoder;
    }

    // O relógio da aplicação. Injetado (e não Instant.now() espalhado pelo código)
    // para os testes conseguirem "parar o tempo" ou "andar 30 segundos".
    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }
}
