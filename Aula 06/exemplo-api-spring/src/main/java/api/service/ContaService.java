// Este arquivo pertence ao pacote "api.service" e mora em src/main/java/api/service/.
// Nome completo desta classe: api.service.ContaService
//
// Declara o pacote; a pasta api/service/ precisa existir e ter exatamente este nome.
package api.service;

// @Service vem do Spring: marca esta classe como bean da camada de regra de negócio.
import org.springframework.stereotype.Service;

// O repositório que este service consulta -- outro bean, de outro pacote.
import api.repository.ContaRepository;

// Conta: o modelo (Aula 3 e 4), devolvido daqui para cima até o Controller.
import contas.Conta;

// >>> SPRING: @Service, assim como @Repository, é um "apelido" de @Component -- o
// @ComponentScan também cria um bean desta classe sozinho. A diferença entre os dois
// é só de INTENÇÃO/leitura: @Repository é para quem acessa dados, @Service é para quem
// aplica regra de negócio -- aqui a "regra" é só "se não achar, lança exceção".
@Service
public class ContaService {

    // ---------------------------------------------------------------- atributos

    // Guarda a referência para o repositório injetado -- nenhum outro método desta
    // classe cria um ContaRepository novo, todos usam este mesmo objeto.
    private final ContaRepository repository;

    // ---------------------------------------------------------------- construtor

    // >>> SPRING: injeção de dependência por construtor. O Spring vê que ContaService
    // precisa de um ContaRepository para nascer, procura entre os beans que já criou
    // um do tipo ContaRepository (o que tem @Repository lá em outro arquivo) e passa
    // ele aqui sozinho -- ninguém escreve "new ContaService(new ContaRepository())".
    public ContaService(ContaRepository repository) {

        // Só guarda a referência que o Spring passou.
        this.repository = repository;

    // Fim do construtor.
    }

    // ---------------------------------------------------------------- métodos de negócio

    // Busca a conta pelo número; se não existir, quem chama nunca recebe null.
    public Conta buscarPorNumero(String numero) {

        // repository.buscarPorNumero(numero) devolve um Optional<Conta>.
        // .orElseThrow(...): se o Optional estiver vazio (conta não existe), executa o
        // lambda -- que cria e LANÇA a exceção -- em vez de devolver um valor.
        // Se tiver valor, orElseThrow() simplesmente devolve a Conta de dentro da caixa.
        return repository.buscarPorNumero(numero)
                .orElseThrow(() -> new ContaNaoEncontradaException(numero));

    // Fim de buscarPorNumero().
    }

// Fim da classe ContaService.
}
