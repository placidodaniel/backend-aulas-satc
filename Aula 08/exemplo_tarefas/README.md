# Exemplo de tarefas — Aula 08

API REST de tarefas usando Spring Boot, JPA, Hibernate, PostgreSQL e Flyway.
Os comandos abaixo foram escritos para o laboratório em máquinas Windows usando
PowerShell e Docker Desktop.

## Pré-requisitos

- Docker Desktop instalado e aberto.
- JDK 25 instalado e disponível no `PATH`.
- Git instalado para baixar o repositório.

O projeto já possui o Maven Wrapper (`mvnw.cmd`), portanto não é necessário
instalar o Maven separadamente.

## Abrir a pasta do projeto

Abra o PowerShell e execute:

```powershell
cd "C:\caminho\do\repositorio\Aula 08\exemplo_tarefas"
```

Substitua `C:\caminho\do\repositorio` pelo local em que o repositório foi
baixado.

## Subir o PostgreSQL

Na pasta que contém o arquivo `docker-compose.yml`, execute:

```powershell
docker compose up -d
docker compose ps
```

O serviço deve aparecer como `healthy`. O banco fica disponível com estes
dados:

| Configuração | Valor |
|---|---|
| Host | `localhost` |
| Porta | `5433` |
| Banco | `tarefas` |
| Usuário | `tarefas` |
| Senha | `tarefas` |

A porta `5433` é usada porque a porta padrão `5432` já estava ocupada no
ambiente de desenvolvimento.

## Executar a aplicação e a migration

Ainda no PowerShell, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Ao iniciar, o Flyway procura os arquivos em:

```text
src/main/resources/db/migration
```

Depois aplica automaticamente a migration:

```text
V1__criar_tarefas.sql
```

Essa migration cria a tabela `tarefas` e insere os dois registros de exemplo.
O Hibernate usa o mapeamento da classe `Tarefa` e apenas valida a estrutura,
porque o projeto está configurado com `spring.jpa.hibernate.ddl-auto=validate`.

Não é necessário executar um comando separado para a migration. O Flyway roda
as migrations pendentes durante o startup da aplicação e registra cada execução
na tabela `flyway_schema_history`.

## Verificar a migration

Abra outro PowerShell na mesma pasta e execute:

```powershell
docker compose exec postgres psql -U tarefas -d tarefas -c "SELECT installed_rank, version, description, success FROM flyway_schema_history;"
```

Para consultar as tarefas criadas:

```powershell
docker compose exec postgres psql -U tarefas -d tarefas -c "SELECT id, titulo, concluida, responsavel, data_prazo, data_cadastro FROM tarefas ORDER BY id;"
```

## Testar a API

Com a aplicação em execução, abra no navegador:

```text
http://localhost:8080
```

Ou consulte a API pelo PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/tarefas
```

Para criar uma tarefa:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/tarefas `
  -ContentType "application/json" `
  -Body '{"titulo":"Estudar JPA","responsavel":"Aluno","dataPrazo":"2026-10-01"}'
```

## Parar os serviços

Para parar somente a aplicação, volte ao terminal onde ela está rodando e
pressione `Ctrl+C`.

Para parar o PostgreSQL mantendo os dados:

```powershell
docker compose stop
```

Para parar e remover o container e a rede, mantendo o volume:

```powershell
docker compose down
```

## Recriar o banco do zero

Use este comando somente quando quiser apagar todos os dados locais e executar
as migrations novamente:

```powershell
docker compose down -v
docker compose up -d
.\mvnw.cmd spring-boot:run
```

O parâmetro `-v` remove o volume `postgres_data`. Portanto, tarefas criadas
manualmente serão apagadas.

## Criar uma nova migration

Para alterar a estrutura do banco, crie um novo arquivo na pasta de migrations,
seguindo o padrão:

```text
V2__descricao_da_alteracao.sql
```

Nunca edite uma migration que já foi executada em um banco compartilhado.
Crie outra versão e deixe o Flyway aplicá-la no próximo startup da aplicação.
