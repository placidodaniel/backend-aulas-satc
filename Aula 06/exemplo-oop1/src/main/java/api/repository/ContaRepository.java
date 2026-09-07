// Este arquivo pertence ao pacote "api.repository" e mora em src/main/java/api/repository/.
// Nome completo desta classe: api.repository.ContaRepository
//
// Declara o pacote; a pasta api/repository/ precisa existir e ter exatamente este nome.
package api.repository;

// Map: guarda pares chave/valor -- aqui, número da conta -> objeto Conta.
import java.util.Map;

// Optional: em vez de devolver "null" quando a conta não existe, devolve uma caixa que
// pode estar vazia. Quem chama é OBRIGADO a tratar os dois casos (tem valor / não tem).
import java.util.Optional;

// ConcurrentHashMap: como o Spring pode atender várias requisições HTTP ao MESMO TEMPO
// (uma thread por requisição), um HashMap comum não é seguro aqui -- duas threads
// mexendo nele ao mesmo tempo podem corromper a estrutura. ConcurrentHashMap resolve
// isso sem precisar de "synchronized" manual.
import java.util.concurrent.ConcurrentHashMap;

// @Repository vem do Spring: é o que marca esta classe como um "bean" da camada de dados.
import org.springframework.stereotype.Repository;

// Classes do domínio (Aula 3 e 4), reaproveitadas sem nenhuma alteração.
import contas.Conta;
import contas.ContaCorrente;
import contas.ContaPoupanca;

// >>> SPRING: @Repository é um "apelido" de @Component -- avisa o @ComponentScan
// (lá em Application) que esta classe deve virar um bean gerenciado pelo Spring, e não
// algo que cada parte do código instancia na mão com "new". Quem quiser usar este
// repositório (o ContaService) recebe a MESMA instância pronta, injetada no construtor.
@Repository
public class ContaRepository {

    // ---------------------------------------------------------------- atributos

    // "Banco de dados" em memória: existe só enquanto o processo da API estiver rodando.
    // final: a REFERÊNCIA ao Map não muda -- o conteúdo dele continua mudando com put().
    private final Map<String, Conta> contas = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------- construtor

    // >>> SPRING: como este é o ÚNICO construtor da classe, o Spring o chama sozinho,
    // sem argumento nenhum, na hora de criar o bean -- é aqui que os dados de exemplo
    // nascem, uma única vez, quando a API sobe.
    public ContaRepository() {

        // Mesma sequência do bloco inicial de app.Main: cria a conta, deposita, saca.
        // ContaCorrente cobra taxa no saque (Aula 3 e 4) -- por isso o saldo final é 899.50.
        ContaCorrente natan = new ContaCorrente("Natan", "1234-5");
        natan.depositar(1000);
        natan.sacar(100);

        // put(chave, valor): guarda a conta no Map, usando o NÚMERO dela como chave --
        // é por esse número que o Controller vai buscar depois.
        contas.put(natan.getNumero(), natan);

        // ContaPoupanca não cobra taxa no saque -- por isso o saldo final é 900.00.
        ContaPoupanca wesley = new ContaPoupanca("Wesley", "6789-0");
        wesley.depositar(1000);
        wesley.sacar(100);

        contas.put(wesley.getNumero(), wesley);

    // Fim do construtor.
    }

    // ---------------------------------------------------------------- métodos de negócio

    // Devolve a conta se existir, ou um Optional vazio se não existir -- nunca "null".
    public Optional<Conta> buscarPorNumero(String numero) {

        // get(numero): se a chave não existir no Map, devolve null (comportamento normal
        // de Map). Optional.ofNullable() embrulha esse possível null numa caixa segura,
        // que quem chamar é obrigado a abrir explicitamente (com orElseThrow, por exemplo).
        return Optional.ofNullable(contas.get(numero));

    // Fim de buscarPorNumero().
    }

// Fim da classe ContaRepository.
}
