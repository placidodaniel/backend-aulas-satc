# Aula 10 e 11: Arquitetura do Backend (Projeto Final, Etapa 1)

**Disciplina:** Backend (Engenharia de Software)
**Professor:** Daniel Plácido
**Contato:** daniel.placido@satc.edu.br

| | |
|---|---|
| **Grupos** | 3 pessoas, formados na Aula 10 |
| **Tema** | Sorteado pelo professor na Aula 10, depois que todos os grupos estiverem formados |
| **Entrega** | Repositório público no GitHub, até o início da Aula 11 |
| **Apresentação e nota** | Na Aula 11 |

## Objetivo

Aprender a **planejar e organizar um backend antes de programar tudo**. Cada grupo recebe um tema, desenha o sistema, monta a estrutura completa do código e faz uma parte pequena funcionar. Não é para entregar um sistema pronto.

É como construir uma casa: nesta etapa vocês entregam a planta, a estrutura de todos os cômodos e **um** cômodo pronto. O resto é construído nas próximas etapas, no mesmo repositório.

## O tema

Na Aula 10, cada grupo passa para o professor o nome dos três integrantes. Quando todos os grupos estiverem formados, o professor sorteia um dos temas abaixo para cada grupo. Dentro do tema sorteado, o grupo escolhe o problema que o sistema vai resolver; a coluna da direita traz ideias.

| # | Tema | Ideias de problema |
|---|---|---|
| 1 | 🌍 Sustentabilidade e Meio Ambiente | Monitoramento de resíduos, emissões, consumo de recursos, reciclagem |
| 2 | 🚚 Logística e Cadeia de Suprimentos | Rastreamento de entregas, controle de estoque, rotas, frota |
| 3 | 🏥 Saúde e Bem-Estar | Agendamento clínico, acompanhamento de pacientes, hábitos saudáveis |
| 4 | 🏭 Indústria e Manufatura | Controle de produção, manutenção de equipamentos, controle de qualidade |
| 5 | 🌾 Agronegócio | Manejo de plantio e colheita, controle de rebanho, insumos agrícolas |
| 6 | 🎓 Educação e Aprendizagem | Cursos, matrículas, progresso de estudantes, avaliações |
| 7 | 🎮 Entretenimento e Cultura | Eventos, ingressos, catálogo de conteúdo, avaliações de público |
| 8 | 🏙️ Cidades Inteligentes | Iluminação pública, trânsito, coleta de lixo, ocorrências urbanas |
| 9 | 🛒 Varejo e E-commerce | Catálogo, pedidos, estoque, avaliações de produto |

Cada tema sai para um grupo só. Se houver mais de 9 grupos, os temas voltam para o sorteio, e os grupos com o mesmo tema escolhem problemas diferentes. Tarefas e biblioteca não valem: são os exemplos usados na disciplina.

## O que cada grupo deve entregar

Um repositório público no GitHub, criado a partir do template desta aula (veja [Como começar pelo template](#como-começar-pelo-template)), com estas cinco coisas:

1. **O desenho do sistema**, em três documentos da pasta `docs/`:
   - `docs/01`: o problema, quem usa o sistema e no mínimo **3 regras de negócio**;
   - `docs/02`: no mínimo **3 entidades**, pelo menos **1 relacionamento** entre elas e o diagrama das tabelas;
   - `docs/03`: as rotas de todas as entidades, com o JSON de entrada e de saída.
2. **A estrutura completa do código**, para **todas** as entidades:
   - a migration que cria a tabela;
   - a entidade (`@Entity`), o Repository e os DTOs de entrada e de saída;
   - o Mapper, o Service e o Controller, criados mas ainda vazios, com um comentário no topo dizendo o que cada um vai fazer;
   - o mapa de todas as classes em `docs/04`.
3. **Uma API simples funcionando**, em **uma** entidade: as rotas `POST`, `GET` e `GET /{id}`, com erro `400` para campo inválido e `404` para id que não existe.
4. **O README do projeto**, completo e bem escrito, seguindo o modelo que vem no template.
5. **A autoavaliação**, em `docs/05`.

**Não precisa:** `PUT`, `DELETE` nem as rotas das outras entidades. Isso fica para as próximas etapas.

**Frontend é opcional.** O grupo pode fazer uma tela, se quiser, mas não precisa: as rotas podem ser testadas e mostradas pelo **Postman** ou pelo Swagger UI. No Postman, dá para importar todas as rotas de uma vez: **Import** → **Link** → `http://localhost:8080/v3/api-docs`, como na Aula 9.

> **Regra de negócio** é algo que o sistema precisa garantir ou impedir, além de "campo obrigatório". Exemplo: *um leitor não pode ter mais de 3 empréstimos em aberto*.
>
> **Classe vazia** (que chamamos de *montada*) é a classe que já existe na pasta certa e está ligada às outras, mas ainda não tem a lógica. A [seção 6 de ARQUITETURA.md](ARQUITETURA.md#6-a-estrutura-montada) mostra um exemplo.

## Como começar pelo template

Todo grupo começa do mesmo ponto: a pasta [template-projeto-final](template-projeto-final), que está nesta aula. Ela já traz um projeto Spring Boot que sobe, os documentos de `docs/` e o README para preencher.

**Não use** como ponto de partida o `exemplo_tarefas` da Aula 9 (ele é só para consultar), um projeto novo do Spring Initializr nem um fork do repositório da disciplina.

Um integrante faz os passos abaixo; os outros dois esperam o passo 4.

**1. Atualize o repositório da disciplina.** Se você já tem ele no computador, entre na pasta dele e rode `git pull`. Se não tem, baixe:

```bash
git clone https://github.com/placidodaniel/backend-aulas-satc.git
```

**2. Copie a pasta do template** para **fora** do repositório da disciplina, com o nome do projeto. Pode ser pelo Explorador de Arquivos ou pelo terminal, a partir da pasta onde está o `backend-aulas-satc`:

```powershell
# Windows (PowerShell)
Copy-Item -Recurse "backend-aulas-satc\Aula 10-11\template-projeto-final" "nome-do-projeto"
```

```bash
# Linux ou Mac
cp -r "backend-aulas-satc/Aula 10-11/template-projeto-final" nome-do-projeto
```

**3. Crie o repositório no GitHub, vazio:** público, sem README, sem `.gitignore` e sem licença. Depois, na pasta `nome-do-projeto`, envie o projeto para ele:

```bash
cd nome-do-projeto
git init
git add .
git update-index --chmod=+x mvnw
git commit -m "Projeto criado a partir do template"
git branch -M main
git remote add origin https://github.com/<usuario>/<repositorio>.git
git push -u origin main
```

A linha `git update-index` deixa o `mvnw` executável para quem usa Linux ou Mac.

**4. Adicione os outros dois integrantes** como colaboradores (**Settings** → **Collaborators**). Cada um clona o repositório do grupo e confere se o projeto sobe.

**Como saber se está no template certo:** na raiz do repositório do grupo aparecem `README.md`, `docker-compose.yml`, `pom.xml`, `mvnw` e as pastas `docs/` (com os documentos 01 a 05) e `src/`. O README começa com o bloco "Este é o ponto de partida do projeto do grupo", e cada pasta de `src/main/java/api/` tem o seu próprio `README.md`.

## Passo a passo e prazos

**Na Aula 10, em sala**

1. Formem o grupo de 3 pessoas e passem o nome dos integrantes para o professor.
2. Quando todos os grupos estiverem formados, o professor sorteia os temas.
3. Um integrante cria o repositório do grupo a partir do template, seguindo [Como começar pelo template](#como-começar-pelo-template), e adiciona os outros dois como colaboradores.
4. Os três clonam o repositório do grupo e conferem que o projeto sobe: `docker compose up -d` e `./mvnw spring-boot:run`. O Swagger UI abre em `http://localhost:8080/swagger-ui.html`, ainda sem rotas.
5. Comecem o desenho: `docs/01`, depois `docs/02`, depois `docs/03`.

**Até o início da Aula 11**

6. Montem a estrutura, uma entidade de cada vez, e subam a API depois de cada uma para conferir.
7. Façam a API simples, escrevam o README e preencham a autoavaliação.
8. Testem a entrega: um integrante clona o repositório numa pasta vazia e segue o README. Se não subir assim, não vai subir na correção.
9. Enviem um e-mail para **daniel.placido@satc.edu.br** com o assunto `Projeto Final - Etapa 1 - <nome do projeto>` e, no corpo, o nome completo dos 3 integrantes, o tema e o link do repositório.

Vale o que estiver na branch `main` no início da Aula 11. Commits feitos depois disso não entram.

**Na Aula 11, em sala**

10. Apresentem o projeto e respondam às 3 perguntas do professor.

## Apresentação

O grupo tem até **10 minutos**, e **os três integrantes participam**. Roteiro sugerido:

| O que mostrar | Tempo |
|---|---|
| O README aberto no GitHub: o que é o projeto e que problema ele resolve | 2 min |
| O diagrama das tabelas, em `docs/02` | 2 min |
| As rotas, em `docs/03` | 1 min |
| As pastas e as classes no código: uma entidade com as classes ainda vazias e a entidade da API simples | 3 min |
| A API funcionando, pelo Postman, pelo Swagger UI ou pelo frontend, se houver: criar, listar, buscar, um `400` e um `404` | 2 min |

Depois, o professor faz **3 perguntas** ao grupo, e qualquer integrante pode responder:

| Pergunta | Sobre o quê | Vale |
|---|---|---|
| **1** | O desenho: o problema, as regras de negócio, as entidades e os relacionamentos | 1,5 |
| **2** | A estrutura: as camadas e onde cada coisa mora no código | 1,5 |
| **3** | A API funcionando: com a API rodando, mostrar o caminho de uma requisição | 2,0 |

## Nota

A nota é do grupo: os três integrantes recebem a mesma nota.

| Metade | Como o professor avalia | Pontos |
|---|---|---|
| **Projeto** | Revisa o código no GitHub e confere cada item da entrega | 5,0 |
| **Apresentação** | Faz as 3 perguntas | 5,0 |
| **Total** | | **10,0** |

Os critérios da revisão do código, exemplos das perguntas e a ficha de correção estão em [AVALIACAO.md](AVALIACAO.md). Leia antes de começar: é exatamente isso que será conferido.

## Outra linguagem

Pode. A organização é a mesma: as mesmas camadas, PostgreSQL com Docker Compose, migrations, DTO separado da entidade, Swagger e o mesmo formato de erro. A [seção 8 de ARQUITETURA.md](ARQUITETURA.md#8-em-outra-linguagem) mostra o equivalente de cada ferramenta em Node, Python e C#. Os exemplos das próximas aulas continuam em Java + Spring Boot.

## Onde encontrar cada coisa

| Arquivo | Para que serve |
|---|---|
| [template-projeto-final/](template-projeto-final) | O ponto de partida do repositório: um projeto que já sobe, os documentos e o README para preencher |
| [ARQUITETURA.md](ARQUITETURA.md) | Como o código deve ser organizado, com exemplos |
| [AVALIACAO.md](AVALIACAO.md) | Como a nota é definida |
| [GRUPOS.md](GRUPOS.md) | Os grupos, os temas sorteados e a ordem de apresentação |
| [exemplo_tarefas_resolvido](<../Aula 09/exemplo_tarefas_resolvido>) | O projeto da Aula 9, para consultar: as mesmas camadas, com tudo implementado |

**Se travar, revise:** Git e GitHub na Aula 2; entidades e exceções nas Aulas 3 e 4; rotas e status HTTP nas Aulas 6 e 7; Controller, Service e erros na Aula 7; migrations, `@Entity`, Repository e Docker Compose na Aula 8; DTOs, Mapper e Swagger na Aula 9.
