package api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import api.dto.CadastroUsuarioDTO;
import api.dto.LoginDTO;
import api.dto.LoginRespostaDTO;
import api.dto.SegundoFatorDTO;
import api.model.Usuario;
import api.repository.UsuarioRepository;
import api.seguranca.ContaBloqueadaException;
import api.seguranca.CredenciaisInvalidasException;
import api.seguranca.TokenService;
import api.seguranca.Totp;

import static org.mockito.Mockito.mock;

// Aula 15: testes UNITÁRIOS das regras de autenticação. Sem banco e sem Spring:
// o repositório e o TokenService são mocks, o BCrypt é o de verdade (custo 4,
// o mínimo, para o teste ser rápido) e o relógio é PARADO, para controlar o tempo.
class AutenticacaoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-18T12:00:10Z");
    private static final String SENHA = "uma frase bem comprida";

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final TokenService tokens = mock(TokenService.class);
    private final PasswordEncoder senhas = new BCryptPasswordEncoder(4);
    private final Clock relogio = Clock.fixed(AGORA, ZoneOffset.UTC);
    private AutenticacaoService service;
    private Usuario ana;

    @BeforeEach
    void preparar() {
        service = new AutenticacaoService(usuarios, senhas, tokens, relogio, 5, Duration.ofMinutes(15));
        ana = new Usuario("Ana", "ana@satc.edu.br", senhas.encode(SENHA), AGORA);
        ReflectionTestUtils.setField(ana, "id", 1L);
        when(usuarios.findByEmail("ana@satc.edu.br")).thenReturn(Optional.of(ana));
        when(usuarios.findById(1L)).thenReturn(Optional.of(ana));
        when(tokens.gerarAcesso(any())).thenReturn(jwt("token-de-acesso"));
        when(tokens.gerarDesafio(any())).thenReturn(jwt("desafio"));
    }

    private static Jwt jwt(String valor) {
        return Jwt.withTokenValue(valor).header("alg", "HS256").claim("sub", "ana")
                .issuedAt(AGORA).expiresAt(AGORA.plusSeconds(1800)).build();
    }

    @Test
    @DisplayName("Cadastro guarda o HASH BCrypt, nunca a senha")
    void cadastroComHash() {
        when(usuarios.existsByEmail("bia@satc.edu.br")).thenReturn(false);
        when(usuarios.saveAndFlush(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 2L);
            return u;
        });

        var dto = service.cadastrar(new CadastroUsuarioDTO("Bia", "  Bia@SATC.edu.br ", SENHA));

        assertThat(dto.email()).isEqualTo("bia@satc.edu.br");  // normalizado
        verify(usuarios).saveAndFlush(org.mockito.ArgumentMatchers.argThat(u ->
                u.getSenhaHash().startsWith("$2a$") && !u.getSenhaHash().contains(SENHA)));
    }

    @Test
    @DisplayName("Cadastro recusa senha da lista das mais comuns e e-mail repetido")
    void cadastroRecusado() {
        assertThatThrownBy(() -> service.cadastrar(new CadastroUsuarioDTO("Bia", "bia@satc.edu.br", "Senha123")))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("mais usadas");
        when(usuarios.existsByEmail("ana@satc.edu.br")).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar(new CadastroUsuarioDTO("Ana", "ana@satc.edu.br", SENHA)))
                .isInstanceOf(RegraDeNegocioException.class).hasMessageContaining("Já existe");
    }

    @Test
    @DisplayName("Login sem 2FA devolve o token")
    void loginSemSegundoFator() {
        LoginRespostaDTO resposta = service.entrar(new LoginDTO("ANA@satc.edu.br", SENHA));
        assertThat(resposta.segundoFatorPendente()).isFalse();
        assertThat(resposta.token()).isEqualTo("token-de-acesso");
    }

    @Test
    @DisplayName("E-mail sem conta e senha errada dão a MESMA mensagem")
    void mesmaMensagem() {
        assertThatThrownBy(() -> service.entrar(new LoginDTO("ninguem@satc.edu.br", SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class).hasMessage("E-mail ou senha inválidos.");
        assertThatThrownBy(() -> service.entrar(new LoginDTO("ana@satc.edu.br", "errada errada")))
                .isInstanceOf(CredenciaisInvalidasException.class).hasMessage("E-mail ou senha inválidos.");
    }

    @Test
    @DisplayName("5 senhas erradas bloqueiam a conta, até com a senha certa depois")
    void bloqueio() {
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.entrar(new LoginDTO("ana@satc.edu.br", "errada errada")))
                    .isInstanceOf(CredenciaisInvalidasException.class);
        }
        assertThatThrownBy(() -> service.entrar(new LoginDTO("ana@satc.edu.br", SENHA)))
                .isInstanceOf(ContaBloqueadaException.class);
        assertThat(ana.getBloqueadoAte()).isEqualTo(AGORA.plus(Duration.ofMinutes(15)));
        verify(tokens, never()).gerarAcesso(any());
    }

    @Test
    @DisplayName("Com 2FA: a senha certa devolve só o desafio; o código certo devolve o token")
    void loginComSegundoFator() {
        String segredo = Totp.novoSegredo();
        ana.prepararSegundoFator(segredo);
        ana.ativarSegundoFator();
        when(tokens.lerDesafio("desafio")).thenReturn(1L);

        LoginRespostaDTO etapa1 = service.entrar(new LoginDTO("ana@satc.edu.br", SENHA));
        assertThat(etapa1.segundoFatorPendente()).isTrue();
        assertThat(etapa1.token()).isNull();
        assertThat(etapa1.desafio()).isEqualTo("desafio");

        String codigo = Totp.codigo(segredo, Totp.passo(AGORA));
        LoginRespostaDTO etapa2 = service.confirmarSegundoFator(new SegundoFatorDTO("desafio", codigo));
        assertThat(etapa2.token()).isEqualTo("token-de-acesso");
    }

    @Test
    @DisplayName("Com 2FA: o mesmo código não serve duas vezes")
    void codigoNaoReutiliza() {
        String segredo = Totp.novoSegredo();
        ana.prepararSegundoFator(segredo);
        ana.ativarSegundoFator();
        when(tokens.lerDesafio("desafio")).thenReturn(1L);
        String codigo = Totp.codigo(segredo, Totp.passo(AGORA));

        service.confirmarSegundoFator(new SegundoFatorDTO("desafio", codigo));
        assertThatThrownBy(() -> service.confirmarSegundoFator(new SegundoFatorDTO("desafio", codigo)))
                .isInstanceOf(CredenciaisInvalidasException.class).hasMessageContaining("já usado");
    }

    @Test
    @DisplayName("Ativar o 2FA exige um código certo do aplicativo")
    void ativar() {
        var configuracao = service.configurarSegundoFator("ana@satc.edu.br");
        assertThat(configuracao.qrCodeSvg()).startsWith("<svg");
        assertThat(configuracao.uri()).startsWith("otpauth://totp/");
        assertThat(ana.isTotpAtivo()).isFalse();

        assertThatThrownBy(() -> service.ativarSegundoFator("ana@satc.edu.br", "000000"))
                .isInstanceOf(RegraDeNegocioException.class);
        service.ativarSegundoFator("ana@satc.edu.br", Totp.codigo(configuracao.segredo(), Totp.passo(AGORA)));
        assertThat(ana.isTotpAtivo()).isTrue();
    }

    @Test
    @DisplayName("No log, o e-mail sai mascarado e sem quebra de linha")
    void mascara() {
        assertThat(AutenticacaoService.mascarar("Ana.Souza@satc.edu.br")).isEqualTo("an***@satc.edu.br");
        assertThat(AutenticacaoService.mascarar("x\nFALSO@a.b")).doesNotContain("\n");
    }
}
