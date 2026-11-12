package api.e2e;

import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

import api.client.OpenMeteoClient;

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

    @Autowired
    protected TestRestTemplate http;
}
