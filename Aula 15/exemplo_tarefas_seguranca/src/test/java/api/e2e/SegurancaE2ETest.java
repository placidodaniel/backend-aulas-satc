package api.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import api.dto.CadastroUsuarioDTO;
import api.dto.CodigoDTO;
import api.dto.ErroDTO;
import api.dto.LoginDTO;
import api.dto.LoginRespostaDTO;
import api.dto.SegundoFatorConfiguracaoDTO;
import api.dto.SegundoFatorDTO;
import api.dto.UsuarioDTO;
import api.seguranca.Totp;

// Aula 15: testes E2E da autenticação, com a aplicação inteira de pé.
// Cada teste é um "ataque" ou um caminho feliz, feito por HTTP de verdade.
class SegurancaE2ETest extends AbstractE2ETest {

    @Autowired
    JdbcTemplate banco;

    private static String novoEmail() {
        return "seg-" + UUID.randomUUID() + "@teste.com";
    }

    private HttpEntity<Object> comToken(String token, Object corpo) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setBearerAuth(token);
        return new HttpEntity<>(corpo, cabecalhos);
    }

    @Test
    @DisplayName("Sem token: leitura e escrita respondem 401 no formato ErroDTO, com WWW-Authenticate")
    void semToken401() {
        ResponseEntity<ErroDTO> leitura = semToken().getForEntity("/tarefas", ErroDTO.class);
        ResponseEntity<ErroDTO> escrita = semToken().postForEntity("/tarefas", "{}", ErroDTO.class);
        ResponseEntity<String> mcp = semToken().postForEntity("/mcp", "{}", String.class);

        assertThat(leitura.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(leitura.getBody().mensagem()).contains("Authorization: Bearer");
        assertThat(leitura.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer");
        assertThat(escrita.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(mcp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Cadastro + login: o token abre a API; no banco só existe o hash BCrypt")
    void caminhoFeliz() {
        String email = novoEmail();
        var cadastro = semToken().postForEntity("/auth/cadastro",
                new CadastroUsuarioDTO("Ana", email, SENHA_DE_TESTE), UsuarioDTO.class);
        assertThat(cadastro.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String hash = banco.queryForObject("select senha_hash from usuarios where email = ?", String.class, email);
        assertThat(hash).startsWith("$2a$12$").doesNotContain(SENHA_DE_TESTE);

        LoginRespostaDTO login = semToken().postForObject("/auth/login", new LoginDTO(email, SENHA_DE_TESTE), LoginRespostaDTO.class);
        assertThat(login.segundoFatorPendente()).isFalse();
        assertThat(login.expiraEm()).isAfter(Instant.now());

        var tarefas = semToken().exchange("/tarefas", HttpMethod.GET, comToken(login.token(), null), String.class);
        assertThat(tarefas.getStatusCode()).isEqualTo(HttpStatus.OK);
        var eu = semToken().exchange("/auth/eu", HttpMethod.GET, comToken(login.token(), null), UsuarioDTO.class);
        assertThat(eu.getBody().email()).isEqualTo(email);
    }

    @Test
    @DisplayName("Token adulterado (troquei o e-mail dentro dele) é recusado: a assinatura não bate")
    void tokenAdulterado() {
        String[] partes = token().split("\\.");
        String conteudo = new String(Base64.getUrlDecoder().decode(partes[1]));
        String falso = conteudo.replaceAll("\"sub\":\"[^\"]+\"", "\"sub\":\"admin@satc.edu.br\"");
        String tokenFalso = partes[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(falso.getBytes())
                + "." + partes[2];

        var resposta = semToken().exchange("/tarefas", HttpMethod.GET, comToken(tokenFalso, null), ErroDTO.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resposta.getBody().mensagem()).contains("inválido ou expirado");
    }

    @Test
    @DisplayName("Fluxo completo do 2FA: configurar, ativar, login em duas etapas; o desafio não abre a API")
    void segundoFator() {
        String email = novoEmail();
        String token = criarContaEEntrar(email);

        // Passo 1 e 2: QR Code e ativação com um código "do celular" (calculado aqui).
        SegundoFatorConfiguracaoDTO qr = semToken().exchange("/auth/2fa/configurar", HttpMethod.POST,
                comToken(token, null), SegundoFatorConfiguracaoDTO.class).getBody();
        assertThat(qr.qrCodeSvg()).startsWith("<svg");
        var ativado = semToken().exchange("/auth/2fa/ativar", HttpMethod.POST,
                comToken(token, new CodigoDTO(Totp.codigo(qr.segredo(), Totp.passo(Instant.now())))), UsuarioDTO.class);
        assertThat(ativado.getBody().segundoFatorAtivo()).isTrue();

        // Login: agora a senha sozinha devolve só o desafio.
        LoginRespostaDTO etapa1 = semToken().postForObject("/auth/login", new LoginDTO(email, SENHA_DE_TESTE), LoginRespostaDTO.class);
        assertThat(etapa1.segundoFatorPendente()).isTrue();
        assertThat(etapa1.token()).isNull();

        // O desafio NÃO serve como token de acesso (outro "scope"): 403.
        var comDesafio = semToken().exchange("/tarefas", HttpMethod.GET, comToken(etapa1.desafio(), null), ErroDTO.class);
        assertThat(comDesafio.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Código errado: 401. Código do PRÓXIMO passo (o de ativação já foi usado): token.
        var errado = semToken().postForEntity("/auth/login/2fa", new SegundoFatorDTO(etapa1.desafio(), "000000"), ErroDTO.class);
        assertThat(errado.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        String proximo = Totp.codigo(qr.segredo(), Totp.passo(Instant.now()) + 1);
        LoginRespostaDTO etapa2 = semToken().postForObject("/auth/login/2fa", new SegundoFatorDTO(etapa1.desafio(), proximo), LoginRespostaDTO.class);
        assertThat(etapa2.token()).isNotBlank();
    }

    @Test
    @DisplayName("Força bruta: depois de 5 senhas erradas, 429 com Retry-After")
    void forcaBruta() {
        String email = novoEmail();
        criarContaEEntrar(email);
        for (int i = 0; i < 5; i++) {
            assertThat(semToken().postForEntity("/auth/login", new LoginDTO(email, "chute numero " + i), ErroDTO.class)
                    .getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
        var bloqueado = semToken().postForEntity("/auth/login", new LoginDTO(email, SENHA_DE_TESTE), ErroDTO.class);
        assertThat(bloqueado.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(bloqueado.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isNotBlank();
    }

    @Test
    @DisplayName("Cabeçalhos de segurança chegam ao navegador; health sem token não mostra detalhes")
    void cabecalhosEHealth() {
        ResponseEntity<String> pagina = semToken().getForEntity("/entrar.html", String.class);
        HttpHeaders h = pagina.getHeaders();
        assertThat(pagina.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(h.getFirst("Content-Security-Policy")).contains("default-src 'self'").contains("frame-ancestors 'none'");
        assertThat(h.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(h.getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(h.getFirst("Referrer-Policy")).isEqualTo("no-referrer");

        String health = semToken().getForObject("/actuator/health", String.class);
        assertThat(health).contains("UP").doesNotContain("db").doesNotContain("rabbit");
    }

    @Test
    @DisplayName("CORS: outro site não recebe permissão para ler a API")
    void semCorsAberto() {
        HttpHeaders h = new HttpHeaders();
        h.setOrigin("https://site-malicioso.example");
        var resposta = http.exchange("/tarefas", HttpMethod.GET, new HttpEntity<>(h), String.class);
        assertThat(resposta.getHeaders().getAccessControlAllowOrigin()).isNull();
    }
}
