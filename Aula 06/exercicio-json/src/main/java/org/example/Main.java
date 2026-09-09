package org.example;

//import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Java -> JSON
        Produto produto = new Produto("Mouse Gamer", 149.90, 27.33);
        String json = mapper.writeValueAsString(produto);
        System.out.println("Java -> JSON: " + json);

        // JSON -> Java

        Produto produtoConvertido = mapper.readValue(json, Produto.class);

        /*JsonNode node = mapper.readTree(json);
        double peso = node.get("peso").asDouble();
        produtoConvertido.setPeso(peso);*/
        System.out.println("JSON -> Java: " + produtoConvertido);

        if (!produto.getNome().equals(produtoConvertido.getNome())
                || produto.getPreco() != produtoConvertido.getPreco()
                || produto.getPeso() != produtoConvertido.getPeso()) {
            throw new AssertionError("Round-trip falhou: valores não batem");
        }
        System.out.println("OK: round-trip Produto <-> JSON bateu certinho.");
    }
}
