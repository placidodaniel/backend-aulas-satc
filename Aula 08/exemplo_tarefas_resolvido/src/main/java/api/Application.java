// Define o pacote raiz da aplicação.
package api;

// Importa o inicializador padrão do Spring Boot.
import org.springframework.boot.SpringApplication;
// Importa a anotação que liga a configuração automática e o component scan.
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// FLUXO 1: subindo o projeto (o que acontece quando você aperta ▶ ou roda
// "mvn spring-boot:run")
//
// 1. A JVM chama o método main() lá embaixo, igual em qualquer programa Java.
// 2. SpringApplication.run(Application.class, args) entrega o controle para o
//    Spring, passando esta classe como ponto de partida.
// 3. O Spring lê @SpringBootApplication e faz o @ComponentScan: varre este
//    pacote (api) e os de baixo dele (controller, service, repository, model,
//    dto) procurando classes marcadas com @RestController, @Service,
//    @Repository, @RestControllerAdvice.
// 4. Para cada uma encontrada, o Spring cria um objeto (bean) e resolve as
//    dependências do construtor sozinho, na ordem certa:
//      TarefaRepository (não depende de nada)
//        -> TarefaService (pede um TarefaRepository no construtor)
//          -> TarefaController (pede um TarefaService no construtor)
//    Ninguém no projeto escreve "new TarefaService(...)" -- é o Spring quem
//    monta essa cadeia.
// 5. Nesse passo, o Flyway aplica as migrations no PostgreSQL e o JPA valida se
//    as entidades correspondem à estrutura criada.
// 6. O Spring Boot sobe o servidor Tomcat embutido na porta configurada
//    (8080 por padrão) e deixa a API escutando por requisições HTTP.
// ============================================================================

// ============================================================================
// FLUXO 2: quando alguém chama a API (ex.: POST /tarefas com um JSON no corpo)
//
// 1. O cliente (curl, Postman, navegador) manda uma requisição HTTP para
//    http://localhost:8080/tarefas.
// 2. O Tomcat recebe a conexão e repassa a requisição para o Spring MVC.
// 3. O Spring MVC olha o caminho (/tarefas) e o verbo HTTP (POST) e descobre
//    que quem deve atender é TarefaController.criar(), por causa do
//    @RequestMapping("/tarefas") + @PostMapping.
// 4. Como o método tem @RequestBody TarefaDTO, o Jackson converte o JSON do
//    corpo da requisição em um objeto TarefaDTO. Como também tem @Valid, o
//    Spring valida esse objeto (checa o @NotBlank do título) ANTES do código
//    do método rodar -- título vazio nunca chega a entrar no criar().
// 5. O Controller chama TarefaService.criar(dto), passando o DTO já validado.
// 6. O Service monta uma Tarefa nova e pede para o TarefaRepository salvar.
// 7. O Repository JPA gera um id novo, persiste a tarefa no PostgreSQL e
//    devolve a tarefa salva, com id preenchido.
// 8. Se em vez disso o Service tivesse lançado uma TarefaNaoEncontradaException
//    (como acontece em buscarPorId/remover quando o id não existe), ela subiria
//    até o ApiExceptionHandler (@RestControllerAdvice), que devolve 404 em vez
//    do 500 que o Spring daria por padrão.
// 9. Dando tudo certo, o Controller devolve a Tarefa criada dentro de um
//    ResponseEntity com status 201 Created.
// 10. O Spring usa o Jackson para transformar essa Tarefa em JSON, monta a
//     resposta HTTP completa (status + headers + corpo) e o Tomcat envia de
//     volta para o cliente.
// ============================================================================

// Framework x Boot x Web, em uma frase: 
//      Framework = a base (IoC, DI, os módulos);
//      Boot = o jeito fácil de ligar a base (autoconfiguração + Tomcat embutido,
//          é o que essa anotação ativa); 
//      Web = o módulo da base focado em HTTP
//          (Controller, @GetMapping...), que o Boot já traz configurado.
//
// @SpringBootApplication liga a configuração automática do Spring e escaneia os
// pacotes abaixo (controller, service, repository, model, dto) à procura de beans.
// Marca esta classe como ponto inicial da aplicação Spring Boot.
@SpringBootApplication
public class Application {

    // Método chamado pela JVM quando o programa começa.
    public static void main(String[] args) {
        // Inicializa o contexto Spring e mantém o servidor web em execução.
        SpringApplication.run(Application.class, args);
    }
}
