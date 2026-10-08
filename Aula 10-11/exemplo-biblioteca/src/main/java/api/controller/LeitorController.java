// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa as anotações que registram o controller e o prefixo das rotas.
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa a camada que vai conter as operações do leitor.
import api.service.LeitorService;

// CAMADA: Controller
// RESPONSABILIDADE: receber as requisições de /leitores.
// ROTAS ATENDIDAS: POST /leitores, GET /leitores e GET /leitores/{id}.
// SITUAÇÃO: montada. Implementação na próxima etapa. Sem métodos, nenhuma
//           rota responde ainda, e o Swagger UI não mostra /leitores.
@RestController
@RequestMapping("/leitores")
public class LeitorController {

    // Guarda a camada de serviço que as rotas vão usar.
    private final LeitorService service;

    // Já recebe o service: o Spring injeta.
    public LeitorController(LeitorService service) {
        // Guarda o service para uso nos métodos HTTP da próxima etapa.
        this.service = service;
    }
}
