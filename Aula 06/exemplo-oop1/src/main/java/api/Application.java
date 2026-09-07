// Este arquivo pertence ao pacote "api" e mora em src/main/java/api/.
// Nome completo desta classe: api.Application
//
// Declara o pacote; a pasta api/ precisa existir e ter exatamente este nome.
package api;

// SpringApplication: a classe do framework que sabe subir toda a aplicação -- ler
// configuração, criar o servidor embutido (Tomcat), instanciar os beans (Controller,
// Service, Repository) e ligar tudo antes de aceitar a primeira requisição HTTP.
import org.springframework.boot.SpringApplication;

// @SpringBootApplication mora neste pacote: "autoconfigure" porque é ela quem decide,
// sozinha, o que configurar (Tomcat, Jackson para JSON, etc.) a partir do que encontra
// no classpath -- é a "mágica" por trás do Spring Boot.
import org.springframework.boot.autoconfigure.SpringBootApplication;

// >>> SPRING: @SpringBootApplication é, na prática, três anotações escondidas em uma:
//   1. @Configuration           -> esta classe pode declarar configuração do Spring
//   2. @EnableAutoConfiguration -> liga a autoconfiguração (Tomcat, JSON, etc.)
//   3. @ComponentScan           -> varre este pacote (api) e os de baixo dele
//      (api.controller, api.service, api.repository, api.dto) procurando classes
//      anotadas com @RestController, @Service, @Repository -- e cria um objeto (bean)
//      de cada uma, automaticamente.
//
// É por isso que Application precisa estar num pacote ACIMA de controller/service/
// repository/dto: se estivesse ao lado deles (como um pacote irmão), o @ComponentScan
// não os enxergaria, e nenhum bean seria criado.
@SpringBootApplication
public class Application {

    // >>> SPRING: main(String[] args) continua sendo o ponto de entrada de todo
    // programa Java -- igual ao Main.java do pacote "app". A diferença é o que roda
    // dentro dele: em vez de imprimir algo no console e terminar, aqui a JVM fica viva,
    // com um servidor HTTP escutando, até alguém parar o processo (Ctrl+C).
    public static void main(String[] args) {

        // SpringApplication.run(): recebe a classe principal (para saber de onde
        // começar o @ComponentScan) e os argumentos de linha de comando (args) --
        // é assim que dá para rodar "java -jar app.jar --server.port=8081", por
        // exemplo. É esta linha que sobe o Tomcat embutido e deixa a API no ar.
        SpringApplication.run(Application.class, args);

    // Fim de main().
    }

// Fim da classe Application.
}
