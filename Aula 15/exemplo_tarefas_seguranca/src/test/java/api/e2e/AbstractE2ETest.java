package api.e2e;

import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import api.assistente.ModeloClient;
import api.client.OpenMeteoClient;
import api.dto.CadastroUsuarioDTO;
import api.dto.LoginDTO;
import api.dto.LoginRespostaDTO;

// Base dos testes E2E (ponta a ponta): sobe a aplicação INTEIRA numa porta aleatória, com um
// PostgreSQL e um RabbitMQ de verdade em containers. O teste faz requisições HTTP reais e confere
// o resultado, passando por controller, service, banco (com as migrations do Flyway) e mensageria.
//
// Só o Open-Meteo é substituído por um mock: é um serviço de fora, na internet, fora do nosso
// controle (lento, instável e com resposta que muda). Teste que depende dele falha "sem motivo".
//
// Precisa do Docker rodando.
// @AutoConfigureObservability: em @SpringBootTest a exportação de métricas vem DESLIGADA por padrão
// (o /actuator/prometheus daria 404). Esta anotação liga, como na aplicação de verdade.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureObservability
abstract class AbstractE2ETest {

    // Containers SINGLETON: sobem uma vez e servem a suíte inteira (o Testcontainers os derruba
    // quando a JVM termina). Não use @Container aqui: ele derrubaria os containers no fim de cada
    // classe, enquanto o Spring reaproveita o mesmo contexto (cache) nas classes seguintes e
    // ficaria apontando para containers mortos.
    //
    // @ServiceConnection: o Spring Boot aponta spring.datasource.* / spring.rabbitmq.* para o
    // container, por cima do que está no application.properties.
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @ServiceConnection
    static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    static {
        postgres.start();
        rabbit.start();
    }

    // Troca o OpenMeteoClient do contexto por um mock. Cada teste diz o que ele responde.
    @MockitoBean
    protected OpenMeteoClient openMeteoClient;

    // O cliente do modelo de IA (Aula 14), também falso para TODOS os testes E2E.
    // Precisa estar aqui, e não só no AssistenteE2ETest: cada combinação diferente de
    // @MockitoBean faz o Spring subir OUTRO contexto da aplicação, e os dois ficariam
    // vivos ao mesmo tempo, disputando as mensagens da mesma fila do RabbitMQ.
    @MockitoBean
    protected ModeloClient modelos;

    @Autowired
    protected TestRestTemplate http;

    @LocalServerPort
    protected int porta;

    // ------------------------------------------------------------------ Aula 15
    // Desde a Aula 15, quase toda rota exige token. Antes de cada teste, garantimos
    // que existe um usuário de teste e que o "http" manda o token dele em TODA
    // requisição (um "interceptador" acrescenta o cabeçalho Authorization).
    // Assim os testes das aulas anteriores continuam iguais.
    //
    // Um token por PORTA: cada contexto do Spring sobe numa porta e sorteia a
    // própria chave de assinatura, então o token de um não vale no outro.
    private static final Map<Integer, String> TOKENS = new ConcurrentHashMap<>();
    protected static final String SENHA_DE_TESTE = "uma senha longa de teste";

    @BeforeEach
    void entrarNoSistema() {
        String token = TOKENS.computeIfAbsent(porta, p -> criarContaEEntrar("e2e-" + UUID.randomUUID() + "@teste.com"));
        var interceptadores = http.getRestTemplate().getInterceptors();
        if (interceptadores.stream().noneMatch(i -> i instanceof ComToken)) {
            interceptadores.add(new ComToken(token));
        }
    }

    // O token do usuário de teste (para clientes que não são o "http", como o cliente MCP).
    protected String token() {
        return TOKENS.get(porta);
    }

    // Um cliente SEM token, para testar o que acontece com quem não entrou.
    protected TestRestTemplate semToken() {
        return new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + porta));
    }

    // Cria a conta e faz o login, devolvendo o token de acesso.
    protected String criarContaEEntrar(String email) {
        semToken().postForEntity("/auth/cadastro", new CadastroUsuarioDTO("Usuário de Teste", email, SENHA_DE_TESTE), String.class);
        LoginRespostaDTO resposta = semToken()
                .postForObject("/auth/login", new LoginDTO(email, SENHA_DE_TESTE), LoginRespostaDTO.class);
        return resposta.token();
    }

    // Acrescenta "Authorization: Bearer <token>", a menos que o teste já tenha mandado outro.
    private record ComToken(String token) implements ClientHttpRequestInterceptor {
        @Override
        public org.springframework.http.client.ClientHttpResponse intercept(org.springframework.http.HttpRequest requisicao,
                byte[] corpo, org.springframework.http.client.ClientHttpRequestExecution execucao) throws java.io.IOException {
            if (!requisicao.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                requisicao.getHeaders().setBearerAuth(token);
            }
            return execucao.execute(requisicao, corpo);
        }
    }
}
