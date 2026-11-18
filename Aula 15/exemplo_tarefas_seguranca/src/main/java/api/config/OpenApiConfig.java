// Declara o pacote das classes de configuração da aplicação.
package api.config;

// Importa a anotação que permite declarar beans por método.
import org.springframework.context.annotation.Bean;
// Importa a anotação que marca a classe como fonte de configuração.
import org.springframework.context.annotation.Configuration;

// Importa o modelo Java do documento OpenAPI.
import io.swagger.v3.oas.models.OpenAPI;
// Importa o bloco de contato exibido no topo do Swagger UI.
import io.swagger.v3.oas.models.info.Contact;
// Importa o bloco "info" (título, versão, descrição) do documento.
import io.swagger.v3.oas.models.info.Info;
// Aula 15: o esquema de segurança (token Bearer) e a exigência dele nas rotas.
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Aula 09 -- O springdoc monta o documento OpenAPI sozinho, lendo os
// Controllers (rotas, verbos, status) e os DTOs (formato do JSON). Este bean
// só completa o cabeçalho do documento: o que aparece no topo do Swagger UI
// em http://localhost:8080/swagger-ui.html.
// Diz ao Spring que esta classe declara beans.
@Configuration
public class OpenApiConfig {

    // O objeto devolvido aqui vira a raiz do JSON em /v3/api-docs.
    @Bean
    public OpenAPI apiDeTarefas() {
        // Monta o documento com o bloco "info" preenchido.
        return new OpenAPI()
                // Aula 15: declara o tipo de autenticação da API (token JWT no cabeçalho
                // Authorization). O Swagger UI ganha o botão "Authorize": cole ali o token
                // devolvido por POST /auth/login e as próximas chamadas já vão com ele.
                .components(new Components().addSecuritySchemes("token", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Token de POST /auth/login (ou /auth/login/2fa)")))
                // Todas as rotas exigem o token; as públicas tiram com @SecurityRequirements vazio.
                .addSecurityItem(new SecurityRequirement().addList("token"))
                .info(new Info()
                        // Nome da API, em destaque no Swagger UI.
                        .title("API de Tarefas")
                        // Versão do contrato -- aqui, a aula em que ele foi publicado.
                        .version("Aula 15")
                        // Texto de apresentação (aceita Markdown).
                        .description("API REST de tarefas e responsáveis da disciplina de Backend (SATC 2026.1). "
                                + "Todo corpo de requisição e de resposta é um **DTO** (veja a seção *Schemas*, "
                                + "lá embaixo) e todo erro sai no formato `ErroDTO`. "
                                + "A rota **/previsao** consome outra API (o Open-Meteo): quando ela falha, "
                                + "a resposta é `502 Bad Gateway`. "
                                + "**Aula 15:** quase todas as rotas exigem login. Crie a conta em `POST /auth/cadastro`, "
                                + "entre em `POST /auth/login`, clique em **Authorize** e cole o `token`.")
                        // Quem responde pela API.
                        .contact(new Contact()
                                .name("Prof. Daniel Plácido")
                                .email("daniel.placido@satc.edu.br")));
    }
}
