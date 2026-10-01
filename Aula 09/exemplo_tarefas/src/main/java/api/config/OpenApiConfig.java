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
                .info(new Info()
                        // Nome da API, em destaque no Swagger UI.
                        .title("API de Tarefas")
                        // Versão do contrato -- aqui, a aula em que ele foi publicado.
                        .version("Aula 09")
                        // Texto de apresentação (aceita Markdown).
                        .description("API REST de tarefas e responsáveis da disciplina de Backend (SATC 2026.1). "
                                + "Todo corpo de requisição e de resposta deve ser um **DTO**. Se uma entidade "
                                + "JPA (`Tarefa`, `Responsavel`) aparecer na seção *Schemas*, lá embaixo, "
                                + "é o banco vazando para o contrato.")
                        // Quem responde pela API.
                        .contact(new Contact()
                                .name("Prof. Daniel Plácido")
                                .email("daniel.placido@satc.edu.br")));
    }
}
