// Nenhuma classe deste projeto (Main, Cachorro, Gato, Animal, Robo, Rastreavel) declara
// "package ...;" no topo do arquivo. Por isso todas caem no chamado "pacote default"
// (default/unnamed package) do Java: um pacote sem nome, comum a todo arquivo .java que
// não declara pacote. Classes do mesmo pacote se enxergam automaticamente, sem import
// -- é por isso que Cachorro, Gato, Robo, Animal e Rastreavel são usados aqui sem import,
// enquanto ArrayList (que é de java.util, outro pacote) precisa do import abaixo.
// Em produção isso NÃO é feito: todo projeto real declara um pacote nomeado
// (ex: "package com.empresa.projeto;") e organiza as pastas em src/main/java/com/empresa/projeto/,
// espelhando o nome do pacote. O pacote default só é aceitável em exemplos/exercícios de aula.
import java.util.ArrayList; // import de classe de outro pacote (java.util), por isso precisa do import

// Classe principal, ponto de entrada do programa
public class Main {
    // main(): método que a JVM chama para iniciar a execução
    public static void main() {
        // Cria um Cachorro (subclasse de Animal)
        Cachorro galactus = new Cachorro();
        galactus.setNome("Galactus"); // define o nome
        galactus.setDono("Natan"); // define o dono
        galactus.setRaca("Pincher"); // define a raça
        galactus.setPosicao(10, 10); // setPosicao(int, int) definido em Animal

        // Cria um Gato (subclasse de Animal)
        Gato fumaca = new Gato();
        fumaca.setNome("Fumaça"); // define o nome
        fumaca.setDono("Wesley"); // define o dono
        fumaca.setRaca("Siames"); // define a raça
        fumaca.setPosicao(50, 20); // define latitude/longitude

        // Exercício 1: depois de tornar Animal abstract e sobrescrever emitirSom() em Cachorro/Gato,
        // descomente as linhas abaixo para ver cada subtipo emitir seu próprio som.
        //Animal[] listaAnimais = {galactus, fumaca};

        //Animal fantasma = new Animal(); // deve dar erro de compilação: Animal virou abstract

        /*
        for(int i = 0; i < listaAnimais.length; i++) {
            Animal animal = listaAnimais[i];
            animal.emitirSom();
        }
         */

        // Cria um Robo, que também implementa Rastreavel mas não é um Animal
        Robo robo = new Robo();
        robo.setNome("R2D2"); // define o nome
        robo.setPosicao("Janela"); // Robo.setPosicao(String), assinatura diferente da de Animal

        // Lista polimórfica: guarda qualquer objeto que implemente a interface Rastreavel
        // (Cachorro e Gato via Animal, e Robo diretamente), mesmo sendo classes sem parentesco
        ArrayList<Rastreavel> listaRastreaveis = new ArrayList<>();
        listaRastreaveis.add(galactus); // adiciona o cachorro à lista
        listaRastreaveis.add(fumaca); // adiciona o gato à lista
        listaRastreaveis.add(robo); // adiciona o robô à lista

        // Percorre a lista chamando rastrear() de cada objeto -- cada classe implementa
        // esse método do seu próprio jeito (polimorfismo)
        for (Rastreavel rastreavel : listaRastreaveis) {
            String posicao = rastreavel.rastrear(); // pega a posição de cada um
            System.out.println(posicao); // imprime a posição no console
        }
    }
}
