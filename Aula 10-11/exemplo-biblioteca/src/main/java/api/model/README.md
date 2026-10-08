# model: as entidades (Aulas 03-04 e 08)

Cada classe daqui representa uma tabela do banco. São as entidades de `docs/modelo-de-dominio.md`.

**Nesta etapa:** crie a entidade de todas as tabelas, completa. A API só sobe se cada entidade bater com a sua migration.

O que toda entidade tem:

- `@Entity` e `@Table(name = "livros")`, com o nome da tabela que a migration criou;
- `@Id` e `@GeneratedValue(strategy = GenerationType.IDENTITY)`: quem gera o id é o banco;
- atributos `private`, com getters (encapsulamento);
- `@ManyToOne` ou `@OneToMany` nos relacionamentos, com a chave estrangeira na migration;
- um construtor sem argumentos `protected`, que o Hibernate exige.

A entidade nunca aparece no Controller nem no JSON: quem viaja pela API é o DTO.

**Para consultar:** `Tarefa` e `Responsavel` em `Aula 09/exemplo_tarefas_resolvido`.
