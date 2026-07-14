// Define o pacote ao qual esta classe pertence
package org.example;

// Importa a classe Scanner para leitura de dados do teclado
import java.util.Scanner;

// Declara a classe principal do programa
public class Main {
    // Declara o método principal que contém a lógica do programa
    public static void main(String[] args) {
        // Cria um objeto Scanner para ler entradas do usuário pelo teclado
        Scanner leitor = new Scanner(System.in);

        // Exibe uma mensagem pedindo ao usuário que digite o seu nome
        System.out.println("Digite o seu nome agora: ");

        // Lê a linha digitada pelo usuário e armazena na variável nome
        String nome = leitor.nextLine();

        System.out.println("Digite o seu sobrenome agora: ");
        // Lê a linha digitada pelo usuário e armazena na variável sobrenome
        String sobrenome = leitor.nextLine();

        // Declara uma variável inteira com o ano atual
        int ano = 2026;

        // Exibe uma saudação personalizada com o nome digitado
        System.out.println("Olá " + nome + " " + sobrenome + "!");
        // Exibe o ano atual na tela
        System.out.println("O ano atual é: " + ano);
        // Exibe a quantidade de caracteres do nome digitado
        System.out.println("O tamanho do seu nome é: " + (nome.length() + sobrenome.length()));

        // Verifica se o nome possui mais de 5 caracteres
        if ((nome.length() + sobrenome.length()) > 5) {
            // Exibe mensagem informando que o nome é longo
            System.out.println("Seu nome é longo");
        } else {
            // Exibe mensagem informando que o nome é curto
            System.out.println("Seu nome é curto");
        }

        // Cria um array de inteiros (Integer) com capacidade para 3 elementos
        Integer[] notas = new Integer[3];

        // Atribui o valor 10 à primeira posição do array
        notas[0] = 10;
        // Atribui o valor 8 à segunda posição do array
        notas[1] = 8;
        // Atribui o valor 6 à terceira posição do array
        notas[2] = 6;

        // Inicializa a variável soma com zero para acumular os valores das notas
        int soma = 0;

        // Laço que percorre as 3 posições do array de notas
        for (int i = 0; i < 3; i++) {
            // Acumula o valor da nota atual na variável soma
            soma = soma + notas[i];
        }
        // Exibe a média das notas dividindo a soma pelo número de notas
        System.out.println("Média final: " + (soma / 3));

        // Inicializa a variável notaFinal com zero
        int notaFinal = 0;
        // Laço que incrementa notaFinal enquanto for menor que 10
        while(notaFinal < 10) {
            // Incrementa notaFinal em 1 a cada iteração
            notaFinal += 1;
        }
        // Exibe o valor final de notaFinal após o laço
        System.out.println("Nota Final: " + notaFinal );

        // Chama o método exercicio01
        exercicio01();
    }

    // Declara o método exercicio01 que exibe uma mensagem simples
    static void exercicio01() {
        // Exibe a mensagem "Olá mundo!" na tela
        System.out.println("Olá mundo!");
    }
}
