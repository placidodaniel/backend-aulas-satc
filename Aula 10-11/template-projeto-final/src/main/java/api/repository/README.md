# repository: a conversa com o banco (Aula 08)

É a única camada que lê e grava no banco. Cada Repository é uma interface: quem escreve a implementação é o Spring.

**Nesta etapa:** crie o Repository de todas as entidades. É uma linha só:

```java
public interface LivroRepository extends JpaRepository<Livro, Long> { }
```

Com isso a interface já tem `save`, `findById`, `findAll` e `deleteById`. Só o Service chama o Repository.

**Para consultar:** `TarefaRepository` em `Aula 09/exemplo_tarefas_resolvido`.
