// Declara o pacote das classes de configuração da aplicação.
package api.config;

// Importa a anotação que permite declarar beans por método.
import org.springframework.context.annotation.Bean;
// Importa a anotação que marca a classe como fonte de configuração.
import org.springframework.context.annotation.Configuration;

// Importa o modelo Java do documento OpenAPI.
import io.swagger.v3.oas.models.OpenAPI;
// Importa o bloco "info" (título, versão, descrição) do documento.
import io.swagger.v3.oas.models.info.Info;

// Aula 09 -- O springdoc monta o documento OpenAPI sozinho, lendo os
// Controllers (rotas, verbos, status) e os DTOs (formato do JSON). Este bean
// só completa o cabeçalho do documento: o que aparece no topo do Swagger UI
// em http://localhost:8080/swagger-ui.html.
// Diz ao Spring que esta classe declara beans.
@Configuration
public class OpenApiConfig {

    // O objeto devolvido aqui vira a raiz do JSON em /v3/api-docs.
    @Bean
    public OpenAPI apiDoProjeto() {
        // Monta o documento com o bloco "info" preenchido.
        return new OpenAPI()
                .info(new Info()
                        // TROQUE: nome da API do grupo, em destaque no Swagger UI.
                        .title("Projeto Final - NOME DO PROJETO")
                        // Versão do contrato -- aqui, a etapa em que ele foi publicado.
                        .version("Etapa 1")
                        // TROQUE: tema, integrantes e o que a API faz (aceita Markdown).
                        .description("API REST do Projeto Final da disciplina de Backend (SATC). "
                                + "Todo corpo de requisição e de resposta é um **DTO** "
                                + "e todo erro 400/404 sai no formato `ErroDTO`."));
    }
}
