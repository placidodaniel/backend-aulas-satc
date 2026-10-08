# api: o código do projeto

**Objetivo desta pasta:** ter o projeto inteiro organizado em camadas. Todas as entidades com suas tabelas e classes criadas, a API simples funcionando e as regras de negócio implementadas.

## As pastas

Uma pasta por camada. Cada uma tem um `README.md` explicando o que fazer ali.

| Pasta | O que mora ali | Uma classe por entidade |
|---|---|---|
| [controller/](controller) | A porta de entrada da API | `LivroController` |
| [service/](service) | As regras do sistema | `LivroService` |
| [mapper/](mapper) | A conversão entre DTO e entidade | `LivroMapper` |
| [repository/](repository) | A conversa com o banco | `LivroRepository` |
| [model/](model) | As entidades | `Livro` |
| [dto/](dto) | O JSON que entra e o que sai | `LivroRequestDTO`, `LivroResponseDTO` |
| [config/](config) | Configuração | Não se aplica |

## Passo a passo

Para **cada** entidade de `docs/modelo-de-dominio.md`, nesta ordem:

1. **Migration**, em `src/main/resources/db/migration`: a tabela, as colunas e as chaves estrangeiras.
2. **Entidade**, em `model/`: os atributos e os relacionamentos, iguais aos da migration.
3. **Repository**, em `repository/`: a interface que estende `JpaRepository`.
4. **DTOs**, em `dto/`: o de entrada, com as validações, e o de saída.
5. **Mapper**, **Service** e **Controller**: só a classe, com as dependências no construtor e o comentário no topo.

Suba a API depois de cada entidade (`./mvnw spring-boot:run`). Se a entidade não bater com a tabela, a API não sobe e o erro diz qual coluna está errada.

Com todas as entidades criadas:

1. Escolha a mais simples e faça as três rotas da API simples: `POST`, `GET` e `GET /{id}`.
2. Implemente as regras de negócio do README no Service, com as rotas que elas precisam. Quando uma regra é violada, o Service lança `RegraDeNegocioException` e o cliente recebe `400` com a mensagem da regra.

As classes que nenhuma rota usa ainda ficam montadas.

## O comentário no topo de cada classe

Toda classe que o grupo cria começa com um comentário dizendo o que ela é e o que faz. É a documentação da estrutura.

```java
// CAMADA: Service
// RESPONSABILIDADE: regras de negócio dos empréstimos.
// REGRAS: R1 (no máximo 3 empréstimos em aberto por leitor) e
//         R2 (livro emprestado não pode ser emprestado de novo).
// ROTAS ATENDIDAS: as de /emprestimos em docs/contrato-da-api.md.
// SITUAÇÃO: montada. A lógica entra quando a rota for implementada.
@Service
public class EmprestimoService {

    private final EmprestimoRepository repository;
    private final EmprestimoMapper mapper;

    public EmprestimoService(EmprestimoRepository repository, EmprestimoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
```

| Linha | O que escrever | Em quais classes |
|---|---|---|
| `CAMADA` | Controller, Service, Mapper, Repository, Model ou DTO | Todas |
| `RESPONSABILIDADE` | Uma frase: o que esta classe faz no projeto | Todas |
| `REGRAS` | As regras do README que ela aplica | Service |
| `ROTAS ATENDIDAS` | As rotas de `docs/contrato-da-api.md` que ela atende | Controller e Service |
| `SITUAÇÃO` | `montada` ou `implementada` | Todas |

A classe do exemplo está **montada**: existe, está ligada às outras e diz o que vai fazer, mas ainda não tem lógica. Quando ganhar os métodos, a linha `SITUAÇÃO` muda para `implementada`.
