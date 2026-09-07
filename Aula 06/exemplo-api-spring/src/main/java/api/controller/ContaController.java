// Este arquivo pertence ao pacote "api.controller" e mora em src/main/java/api/controller/.
// Nome completo desta classe: api.controller.ContaController
//
// Declara o pacote; a pasta api/controller/ precisa existir e ter exatamente este nome.
package api.controller;

// Anotações do Spring Web que mapeiam método HTTP + URL para um método Java.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// O DTO que este controller devolve, e o service que ele consulta.
import api.dto.SaldoResponse;
import api.service.ContaService;

// Conta: o modelo devolvido pelo service, antes de virar SaldoResponse.
import contas.Conta;

// >>> SPRING: @RestController = @Controller + @ResponseBody. @Controller já marcaria
// a classe como bean que responde requisições HTTP; o +@ResponseBody diz que o retorno
// de cada método NÃO é o nome de uma página HTML (como seria numa aplicação web
// tradicional), e sim o próprio CORPO da resposta -- o Spring serializa o objeto
// devolvido (aqui, SaldoResponse) em JSON sozinho, usando Jackson por baixo.
//
// @RequestMapping("/contas") define o PREFIXO de rota de toda a classe: qualquer
// @GetMapping/@PostMapping/etc. aqui dentro começa com "/contas" antes do resto do
// caminho declarado no método.
@RestController
@RequestMapping("/contas")
public class ContaController {

    // ---------------------------------------------------------------- atributos

    // O service que faz o trabalho de verdade -- o Controller só recebe a requisição
    // e devolve a resposta, sem regra de negócio (Aula 7, "Boas práticas").
    private final ContaService service;

    // ---------------------------------------------------------------- construtor

    // >>> SPRING: injeção de dependência por construtor, igual em ContaService --
    // o Spring já tem um bean ContaService pronto (criado a partir do @Service lá)
    // e passa ele aqui sozinho.
    public ContaController(ContaService service) {
        this.service = service;
    }

    // ---------------------------------------------------------------- endpoints

    // >>> SPRING: @GetMapping("/{numero}/saldo") = "isto responde requisições GET
    // em /contas/{numero}/saldo" (o /contas vem do @RequestMapping da classe).
    // {numero} entre chaves é um PLACEHOLDER da URL -- um pedaço variável do caminho.
    //
    // @PathVariable String numero: pega esse pedaço variável da URL e entrega pronto
    // como parâmetro do método. Numa chamada GET /contas/1234-5/saldo, numero chega
    // aqui já valendo "1234-5" -- sem nenhum parsing manual de String.
    @GetMapping("/{numero}/saldo")
    public SaldoResponse consultarSaldo(@PathVariable String numero) {

        // Busca a conta pelo número. Se não existir, buscarPorNumero() já lança
        // ContaNaoEncontradaException aqui dentro -- as duas linhas de baixo nem
        // chegam a rodar, e quem responde ao cliente é o ApiExceptionHandler.
        Conta conta = service.buscarPorNumero(numero);

        // Monta o DTO de resposta só com o que a API deve expor (numero + saldo).
        // O "return" deste método é o que vira o corpo JSON da resposta HTTP --
        // efeito do @ResponseBody escondido dentro do @RestController lá em cima.
        return new SaldoResponse(conta.getNumero(), conta.getSaldo());

    // Fim de consultarSaldo().
    }

// Fim da classe ContaController.
}
