// Este arquivo pertence ao pacote "api.dto" e mora em src/main/java/api/dto/.
// Nome completo desta classe: api.dto.SaldoResponse
//
// Declara o pacote; a pasta api/dto/ precisa existir e ter exatamente este nome.
package api.dto;

// >>> DTO (Data Transfer Object): uma classe que existe só para MOLDAR o que trafega
// entre a API e quem a consome -- não é o modelo de domínio (Conta), é um retrato dela,
// com só os campos que fazem sentido expor. Comparando com Conta: lá tem titular, saldo,
// numero E a lista inteira de lançamentos; aqui só numero + saldo.
//
// Por que não devolver a própria Conta direto no Controller? Porque Conta é abstract
// (Aula 3 e 4, Exercício 1) e carrega lançamentos, titular, regra de imposto -- coisa
// que não é da conta desta API. Se um dia Conta ganhar um campo novo (uma senha, um CPF),
// o DTO continua devolvendo só o que foi decidido aqui, sem vazar informação por engano.
public class SaldoResponse {

    // ---------------------------------------------------------------- atributos

    // >>> SPRING: quando o Controller devolve um SaldoResponse, o Spring usa o Jackson
    // (Aula 6, Parte 3 -- o mesmo ObjectMapper do exercicio-json) por baixo dos panos
    // para transformar este objeto em JSON automaticamente. Os getters lá embaixo são
    // o que o Jackson lê para montar {"numero": "...", "saldo": ...}.
    private final String numero;
    private final double saldo;

    // ---------------------------------------------------------------- construtor

    // Só recebe os dois valores prontos e guarda -- este objeto nasce completo, e como
    // os dois atributos são final, nunca mudam depois (imutável).
    public SaldoResponse(String numero, double saldo) {
        this.numero = numero;
        this.saldo = saldo;
    }

    // ---------------------------------------------------------------- getters

    // Sem estes getters, o Jackson não encontraria como ler os valores para gerar o
    // JSON -- é a mesma convenção getX()/isX() já vista na Aula 3 e 4 e na Aula 6.
    public String getNumero() {
        return numero;
    }

    public double getSaldo() {
        return saldo;
    }

// Fim da classe SaldoResponse.
}
