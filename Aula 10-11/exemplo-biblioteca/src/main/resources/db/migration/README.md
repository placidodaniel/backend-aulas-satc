# db/migration: a criação das tabelas (Aula 08)

Cada arquivo SQL daqui cria ou altera tabelas. O Flyway executa os arquivos em ordem quando a API sobe.

**Nesta etapa:** crie as migrations de todas as tabelas do modelo, uma tabela por arquivo.

- O nome é `V<número>__<o_que_faz>.sql`, com **dois** sublinhados. Exemplo: `V1__criar_livros.sql`.
- A ordem é a do número: crie primeiro a tabela que as outras referenciam.
- Tabela no plural e em `snake_case` (`livros`, `data_retirada`); chave estrangeira com `REFERENCES`.
- **Nunca edite uma migration que já rodou.** Para corrigir, crie a próxima (`V2__...`). Se o banco ainda está só na sua máquina, recrie do zero com `docker compose down -v`.

**Para consultar:** `V1`, `V2` e `V3` em `Aula 09/exemplo_tarefas_resolvido/src/main/resources/db/migration`.
