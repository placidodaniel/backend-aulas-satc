// Define o pacote dos controllers.
package api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import api.dto.CadastroUsuarioDTO;
import api.dto.CodigoDTO;
import api.dto.DesativarSegundoFatorDTO;
import api.dto.ErroDTO;
import api.dto.LoginDTO;
import api.dto.LoginRespostaDTO;
import api.dto.SegundoFatorConfiguracaoDTO;
import api.dto.SegundoFatorDTO;
import api.dto.UsuarioDTO;
import api.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Aula 15: as rotas de conta e login. Como todo Controller do projeto, não tem
// regra nenhuma: valida o DTO (@Valid) e chama o AutenticacaoService.
//
// As três primeiras são PÚBLICAS (veja SegurancaConfig). As outras exigem o token,
// e o "quem sou eu" vem DE DENTRO do token (@AuthenticationPrincipal), nunca de
// um campo que o cliente manda: senão bastaria trocar o e-mail no JSON para
// mexer na conta de outra pessoa.
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Criar conta, entrar (com ou sem 2FA) e configurar o aplicativo autenticador")
public class AutenticacaoController {

    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Cria uma conta (pública)")
    @SecurityRequirements // sem cadeado no Swagger: rota pública
    @ApiResponse(responseCode = "201", description = "Conta criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos, senha fraca ou e-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO cadastrar(@Valid @RequestBody CadastroUsuarioDTO dados) {
        return service.cadastrar(dados);
    }

    @Operation(summary = "Entra com e-mail e senha (pública)",
            description = "Sem 2FA, devolve o token. Com 2FA, devolve um desafio para POST /auth/login/2fa.")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Token de acesso ou desafio do 2FA")
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @ApiResponse(responseCode = "429", description = "Conta bloqueada por muitas tentativas",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping("/login")
    public LoginRespostaDTO entrar(@Valid @RequestBody LoginDTO dados) {
        return service.entrar(dados);
    }

    @Operation(summary = "Segunda etapa do login: desafio + código do aplicativo (pública)")
    @SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Token de acesso")
    @ApiResponse(responseCode = "401", description = "Desafio vencido ou código inválido",
            content = @Content(schema = @Schema(implementation = ErroDTO.class)))
    @PostMapping("/login/2fa")
    public LoginRespostaDTO confirmarSegundoFator(@Valid @RequestBody SegundoFatorDTO dados) {
        return service.confirmarSegundoFator(dados);
    }

    @Operation(summary = "Quem sou eu: o usuário dono do token")
    @GetMapping("/eu")
    public UsuarioDTO eu(@AuthenticationPrincipal Jwt token) {
        // token.getSubject(): o "sub" do JWT, que o TokenService preencheu com o e-mail.
        return service.buscar(token.getSubject());
    }

    @Operation(summary = "2FA, passo 1: gera o segredo e o QR Code (ainda não ativa)")
    @PostMapping("/2fa/configurar")
    public SegundoFatorConfiguracaoDTO configurar(@AuthenticationPrincipal Jwt token) {
        return service.configurarSegundoFator(token.getSubject());
    }

    @Operation(summary = "2FA, passo 2: confere um código do aplicativo e ativa")
    @PostMapping("/2fa/ativar")
    public UsuarioDTO ativar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody CodigoDTO dados) {
        return service.ativarSegundoFator(token.getSubject(), dados.codigo());
    }

    @Operation(summary = "Desativa o 2FA (pede a senha e um código)")
    @PostMapping("/2fa/desativar")
    public UsuarioDTO desativar(@AuthenticationPrincipal Jwt token, @Valid @RequestBody DesativarSegundoFatorDTO dados) {
        return service.desativarSegundoFator(token.getSubject(), dados);
    }
}
