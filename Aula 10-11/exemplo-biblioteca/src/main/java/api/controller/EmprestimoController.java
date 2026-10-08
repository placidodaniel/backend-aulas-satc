// Define o pacote responsável pelas rotas HTTP da aplicação.
package api.controller;

// Importa as anotações que registram o controller e o prefixo das rotas.
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa a camada que vai conter as regras dos empréstimos.
import api.service.EmprestimoService;

// CAMADA: Controller
// RESPONSABILIDADE: receber as requisições de /emprestimos.
// ROTAS ATENDIDAS: POST /emprestimos e PUT /emprestimos/{id}/devolver.
// SITUAÇÃO: montada. Implementação na próxima etapa. Sem métodos, nenhuma
//           rota responde ainda, e o Swagger UI não mostra /emprestimos.
@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    // Guarda a camada de serviço que as rotas vão usar.
    private final EmprestimoService service;

    // Já recebe o service: o Spring injeta.
    public EmprestimoController(EmprestimoService service) {
        // Guarda o service para uso nos métodos HTTP da próxima etapa.
        this.service = service;
    }
}
