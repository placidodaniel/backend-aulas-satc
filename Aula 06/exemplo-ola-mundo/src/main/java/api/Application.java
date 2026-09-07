// Este arquivo pertence ao pacote "api" e mora em src/main/java/api/.
// Nome completo desta classe: api.Application
package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Exemplo mínimo, tudo numa classe só, para focar em três ideias:
//
// 1. @RestController marca a classe como responsável por responder requisições HTTP
//    (por baixo dos panos: @Controller + @ResponseBody, então o retorno de cada
//    método vira o CORPO da resposta, não o nome de uma página HTML).
// 2. @GetMapping/@PostMapping mapeiam um método para responder GET/POST numa rota.
// 3. O valor retornado pelo método vira automaticamente o corpo da resposta --
//    Strings viram texto puro, objetos (como o record Nome, abaixo) o Spring
//    serializa em JSON sozinho, usando Jackson por baixo.
@SpringBootApplication
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // GET /ola -> não recebe nada, sempre devolve o mesmo texto.
    // Testar no navegador: http://localhost:8080/ola
    @GetMapping("/ola")
    public String olaMundoGet() {
        return "Olá, mundo!";
    }

    // POST /ola -> recebe um corpo JSON, ex.: {"nome": "Maria"}
    // @RequestBody pega o corpo da requisição e converte pro record Nome sozinho.
    // Testar via curl:
    //   curl -X POST http://localhost:8080/ola -H "Content-Type: application/json" -d "{\"nome\":\"Maria\"}"
    @PostMapping("/ola")
    public String olaMundoPost(@RequestBody Nome nome) {
        return "Olá, " + nome.nome() + "!";
    }

    // Record = classe de dados minimalista do Java; aqui só existe para dar forma
    // ao JSON esperado no corpo do POST ({"nome": "..."}).
    record Nome(String nome) {}

}
