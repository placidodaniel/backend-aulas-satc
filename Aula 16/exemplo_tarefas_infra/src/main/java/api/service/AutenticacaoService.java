// Define o pacote dos services.
package api.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import api.dto.CadastroUsuarioDTO;
import api.dto.DesativarSegundoFatorDTO;
import api.dto.LoginDTO;
import api.dto.LoginRespostaDTO;
import api.dto.SegundoFatorConfiguracaoDTO;
import api.dto.SegundoFatorDTO;
import api.dto.UsuarioDTO;
import api.model.Usuario;
import api.repository.UsuarioRepository;
import api.seguranca.ContaBloqueadaException;
import api.seguranca.CredenciaisInvalidasException;
import api.seguranca.QrCodeSvg;
import api.seguranca.TokenService;
import api.seguranca.Totp;

// =====================================================================
// AULA 15: as regras de autenticação (cadastro, login, 2FA).
// =====================================================================
//
// O login com dois fatores acontece em duas etapas:
//
//   POST /auth/login     {email, senha}
//        senha certa e conta SEM 2FA ──> token de acesso (fim)
//        senha certa e conta COM 2FA ──> "desafio" (não abre a API)
//   POST /auth/login/2fa {desafio, codigo}
//        código certo ──> token de acesso
//
// "Fator" é uma prova de quem você é. A senha é algo que você SABE; o celular
// com o aplicativo é algo que você TEM. Quem roubar só a senha (vazamento de
// outro site, phishing, alguém olhando por cima do ombro) não entra.
@Service
public class AutenticacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutenticacaoService.class);

    // Nome que aparece no aplicativo autenticador, ao lado do e-mail.
    private static final String EMISSOR_NO_APLICATIVO = "Tarefas SATC";

    // Senhas que aparecem no topo de todo vazamento. Em produção, use uma lista
    // bem maior (ex.: as 100 mil mais comuns) ou consulte o "Have I Been Pwned".
    private static final Set<String> SENHAS_COMUNS = Set.of(
            "12345678", "123456789", "1234567890", "87654321", "11111111", "00000000",
            "password", "password1", "senha123", "senha1234", "qwerty123", "qwertyuiop",
            "abc12345", "iloveyou", "admin123", "mudar123", "brasil123", "satc2026");

    // Hash BCrypt de uma senha qualquer. Usado quando o e-mail NÃO existe: fazemos a
    // mesma conta lenta mesmo assim, para o tempo de resposta não revelar se o
    // e-mail tem conta (um "não existe" responderia em 1 ms; um "senha errada", em 200 ms).
    private final String hashFalso;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final TokenService tokens;
    private final Clock relogio;
    private final int maxTentativas;
    private final Duration bloqueio;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder senhas, TokenService tokens, Clock relogio,
                               @Value("${seguranca.login.max-tentativas:5}") int maxTentativas,
                               @Value("${seguranca.login.bloqueio:15m}") Duration bloqueio) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.tokens = tokens;
        this.relogio = relogio;
        this.maxTentativas = maxTentativas;
        this.bloqueio = bloqueio;
        this.hashFalso = senhas.encode("senha-que-nao-existe");
    }

    // ------------------------------------------------------------------ cadastro

    @Transactional
    public UsuarioDTO cadastrar(CadastroUsuarioDTO dados) {
        String email = normalizar(dados.email());
        validarSenha(dados.senha(), email);
        if (usuarios.existsByEmail(email)) {
            throw new RegraDeNegocioException("Já existe uma conta com este e-mail.");
        }
        // encode(): gera o sal, roda o BCrypt e devolve "$2a$12$<sal><hash>". A senha
        // em texto morre aqui: não vai para o banco, nem para o log, nem para a resposta.
        Usuario novo = new Usuario(dados.nome().trim(), email, senhas.encode(dados.senha()), relogio.instant());
        try {
            // saveAndFlush: força o INSERT agora, para o UNIQUE do banco reclamar AQUI
            // (duas requisições com o mesmo e-mail ao mesmo tempo passam pelo existsByEmail).
            usuarios.saveAndFlush(novo);
        } catch (DataIntegrityViolationException e) {
            throw new RegraDeNegocioException("Já existe uma conta com este e-mail.");
        }
        log.info("Conta criada: {}", mascarar(email));
        return paraDTO(novo);
    }

    // ------------------------------------------------------------------ login

    // noRollbackFor: sem isto, a exceção de senha errada DESFARIA a transação, e o
    // contador de tentativas nunca seria gravado. O bloqueio não funcionaria.
    @Transactional(noRollbackFor = {CredenciaisInvalidasException.class, ContaBloqueadaException.class})
    public LoginRespostaDTO entrar(LoginDTO dados) {
        Instant agora = relogio.instant();
        Usuario usuario = usuarios.findByEmail(normalizar(dados.email())).orElse(null);

        if (usuario == null) {
            senhas.matches(dados.senha(), hashFalso);   // mesmo tempo de resposta (veja hashFalso)
            log.warn("Login recusado (e-mail sem conta): {}", mascarar(dados.email()));
            throw credenciaisInvalidas();
        }
        conferirBloqueio(usuario, agora);

        // matches(): pega o sal de dentro do hash gravado, refaz o BCrypt com a senha
        // digitada e compara. Não existe "descriptografar" um hash.
        if (!senhas.matches(dados.senha(), usuario.getSenhaHash())) {
            usuario.registrarFalha(maxTentativas, agora, bloqueio);
            log.warn("Login recusado (senha errada): {}", mascarar(usuario.getEmail()));
            throw credenciaisInvalidas();
        }

        if (usuario.isTotpAtivo()) {
            // Senha certa, mas ainda NÃO zeramos as tentativas: só no fim do 2FA.
            // Senão, quem tem a senha poderia alternar "senha certa" e "4 códigos
            // chutados" para sempre, sem nunca ser bloqueado.
            Jwt desafio = tokens.gerarDesafio(usuario);
            return LoginRespostaDTO.pendente(desafio.getTokenValue(), desafio.getExpiresAt());
        }
        return concluirLogin(usuario);
    }

    @Transactional(noRollbackFor = {CredenciaisInvalidasException.class, ContaBloqueadaException.class})
    public LoginRespostaDTO confirmarSegundoFator(SegundoFatorDTO dados) {
        Instant agora = relogio.instant();
        long id = tokens.lerDesafio(dados.desafio());
        Usuario usuario = usuarios.findById(id)
                .filter(Usuario::isTotpAtivo)
                .orElseThrow(() -> new CredenciaisInvalidasException("Desafio inválido. Faça o login de novo."));
        conferirBloqueio(usuario, agora);

        if (!codigoValido(usuario, dados.codigo(), agora)) {
            usuario.registrarFalha(maxTentativas, agora, bloqueio);
            log.warn("Código 2FA recusado: {}", mascarar(usuario.getEmail()));
            throw new CredenciaisInvalidasException("Código inválido ou já usado. Espere o próximo código no aplicativo.");
        }
        return concluirLogin(usuario);
    }

    // ------------------------------------------------------------------ minha conta

    @Transactional(readOnly = true)
    public UsuarioDTO buscar(String email) {
        return paraDTO(porEmail(email));
    }

    // Passo 1 do 2FA: sorteia o segredo e devolve o QR Code. Ainda NÃO está ativo.
    @Transactional
    public SegundoFatorConfiguracaoDTO configurarSegundoFator(String email) {
        Usuario usuario = porEmail(email);
        if (usuario.isTotpAtivo()) {
            throw new RegraDeNegocioException("A verificação em duas etapas já está ativa. Desative antes de gerar outro QR Code.");
        }
        String segredo = Totp.novoSegredo();
        usuario.prepararSegundoFator(segredo);
        String uri = Totp.uri(EMISSOR_NO_APLICATIVO, usuario.getEmail(), segredo);
        return new SegundoFatorConfiguracaoDTO(segredo, uri, QrCodeSvg.gerar(uri));
    }

    // Passo 2 do 2FA: a pessoa digita o código que o aplicativo mostrou. Isso prova
    // que o QR Code foi lido certo. Só então o 2FA passa a valer no login; ativar sem
    // conferir poderia trancar a pessoa fora da própria conta.
    @Transactional
    public UsuarioDTO ativarSegundoFator(String email, String codigo) {
        Usuario usuario = porEmail(email);
        if (usuario.isTotpAtivo()) {
            throw new RegraDeNegocioException("A verificação em duas etapas já está ativa.");
        }
        if (usuario.getTotpSegredo() == null) {
            throw new RegraDeNegocioException("Gere o QR Code antes de ativar (POST /auth/2fa/configurar).");
        }
        if (!codigoValido(usuario, codigo, relogio.instant())) {
            throw new RegraDeNegocioException("Código inválido. Confira se o relógio do celular está certo e use o código atual.");
        }
        usuario.ativarSegundoFator();
        log.info("2FA ativado: {}", mascarar(usuario.getEmail()));
        return paraDTO(usuario);
    }

    @Transactional
    public UsuarioDTO desativarSegundoFator(String email, DesativarSegundoFatorDTO dados) {
        Usuario usuario = porEmail(email);
        if (!usuario.isTotpAtivo()) {
            throw new RegraDeNegocioException("A verificação em duas etapas não está ativa.");
        }
        if (!senhas.matches(dados.senha(), usuario.getSenhaHash())
                || !codigoValido(usuario, dados.codigo(), relogio.instant())) {
            throw new RegraDeNegocioException("Senha ou código inválido.");
        }
        usuario.desativarSegundoFator();
        log.info("2FA desativado: {}", mascarar(usuario.getEmail()));
        return paraDTO(usuario);
    }

    // ------------------------------------------------------------------ auxiliares

    private LoginRespostaDTO concluirLogin(Usuario usuario) {
        usuario.registrarSucesso();
        Jwt token = tokens.gerarAcesso(usuario);
        log.info("Login: {}", mascarar(usuario.getEmail()));
        return LoginRespostaDTO.concluido(token.getTokenValue(), token.getExpiresAt(), paraDTO(usuario));
    }

    private void conferirBloqueio(Usuario usuario, Instant agora) {
        if (usuario.bloqueadoEm(agora)) {
            log.warn("Login em conta bloqueada: {}", mascarar(usuario.getEmail()));
            throw new ContaBloqueadaException(usuario.getBloqueadoAte());
        }
    }

    // Confere o código e impede a REUTILIZAÇÃO: um código já aceito (mesmo passo de
    // 30 s, ou anterior) é recusado. Quem espiou o código na tela não consegue usá-lo.
    private boolean codigoValido(Usuario usuario, String codigo, Instant agora) {
        OptionalLong passo = Totp.verificar(usuario.getTotpSegredo(), codigo, agora);
        if (passo.isEmpty() || passo.getAsLong() <= usuario.getTotpUltimoPasso()) {
            return false;
        }
        usuario.usarPasso(passo.getAsLong());
        return true;
    }

    private void validarSenha(String senha, String email) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraDeNegocioException("A senha é longa demais (o limite é de 72 bytes; acentos contam 2).");
        }
        String minuscula = senha.toLowerCase(Locale.ROOT);
        if (SENHAS_COMUNS.contains(minuscula)) {
            throw new RegraDeNegocioException("Esta senha está entre as mais usadas (e vazadas). Escolha outra.");
        }
        String antesDaArroba = email.substring(0, Math.max(0, email.indexOf('@')));
        if (antesDaArroba.length() >= 4 && minuscula.contains(antesDaArroba)) {
            throw new RegraDeNegocioException("A senha não pode conter o seu e-mail.");
        }
    }

    private Usuario porEmail(String email) {
        // O e-mail vem do token, que já foi conferido. Se a conta sumiu, o token não vale mais.
        return usuarios.findByEmail(normalizar(email))
                .orElseThrow(() -> new CredenciaisInvalidasException("Conta não encontrada. Entre de novo."));
    }

    private static CredenciaisInvalidasException credenciaisInvalidas() {
        // A MESMA mensagem para "e-mail não existe" e "senha errada" (veja a exceção).
        return new CredenciaisInvalidasException("E-mail ou senha inválidos.");
    }

    // " Ana@SATC.edu.br " e "ana@satc.edu.br" são a mesma conta.
    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    // No log, só o começo do e-mail: "an***@satc.edu.br". O log é lido por muita
    // gente (e ferramentas); dado pessoal ali deve ser o mínimo necessário (LGPD).
    // Também troca quebras de linha: um "e-mail" com \n forjaria linhas falsas no log.
    static String mascarar(String email) {
        String limpo = normalizar(email).replaceAll("[\\r\\n\\t]", "_");
        int arroba = limpo.indexOf('@');
        if (arroba < 0) {
            return limpo.length() <= 2 ? "***" : limpo.substring(0, 2) + "***";
        }
        return limpo.substring(0, Math.min(2, arroba)) + "***" + limpo.substring(arroba);
    }

    private static UsuarioDTO paraDTO(Usuario usuario) {
        return new UsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.isTotpAtivo());
    }
}
