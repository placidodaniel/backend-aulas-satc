// Define o pacote raiz da aplicação.
package api;

// Importa o inicializador padrão do Spring Boot.
import org.springframework.boot.SpringApplication;
// Importa a anotação que liga a configuração automática e o component scan.
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// Ponto de partida do projeto. O que acontece em "./mvnw spring-boot:run":
//
// 1. A JVM chama o main() lá embaixo e entrega o controle para o Spring.
// 2. O Spring varre este pacote (api) e os de baixo dele -- config, controller,
//    service, mapper, repository, model, dto -- e cria os beans, resolvendo as
//    dependências de cada construtor:
//      Repository e Mapper -> Service -> Controller
// 3. O Flyway aplica as migrations de src/main/resources/db/migration no
//    PostgreSQL e o JPA confere se cada @Entity bate com a sua tabela.
// 4. O springdoc lê os Controllers e os DTOs e monta o contrato OpenAPI
//    (/v3/api-docs e /swagger-ui.html).
// 5. O Tomcat embutido sobe na porta 8080 e a API passa a atender requisições.
//
// Por isso toda classe do projeto precisa ficar DENTRO do pacote api: o que
// estiver fora dele o Spring não encontra.
// ============================================================================

// Marca esta classe como ponto inicial da aplicação Spring Boot.
@SpringBootApplication
public class Application {

    // Método chamado pela JVM quando o programa começa.
    public static void main(String[] args) {
        // Inicializa o contexto Spring e mantém o servidor web em execução.
        SpringApplication.run(Application.class, args);
    }
}
