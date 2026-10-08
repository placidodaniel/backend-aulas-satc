# Aula 10 e 11: Arquitetura do Backend (Projeto Final, Etapa 1)

**Disciplina:** Backend (Engenharia de Software)
**Professor:** Daniel Plácido
**Contato:** daniel.placido@satc.edu.br

| | |
|---|---|
| **Grupos** | 3 pessoas, formados na Aula 10 |
| **Tema** | Sorteado na Aula 10, depois que todos os grupos estiverem formados |
| **Entrega** | Repositório público no GitHub, até o início da Aula 11 |
| **Apresentação e nota** | Na Aula 11. Vale como N2a e N2b |

## Objetivo

Aprender a **planejar e organizar um backend antes de programar tudo**. Cada grupo recebe um tema, cria o problema que o sistema vai resolver e as regras de negócio, desenha o sistema, monta a estrutura completa do código e faz funcionar uma API simples com as regras implementadas. Não é para entregar um sistema pronto.

**Um exemplo:** num sistema de biblioteca, com livros, leitores e empréstimos, o grupo criou o problema e três regras de negócio, desenhou as três entidades e criou a tabela e as classes das três. Depois fez funcionar o cadastro de livros (cadastrar, listar e buscar) e implementou as regras no empréstimo e na devolução. O que as regras não usam, como as consultas de leitores, fica para as próximas etapas, no mesmo repositório. Esse projeto está pronto na pasta [exemplo-biblioteca](exemplo-biblioteca).

## O tema

Na Aula 10, cada grupo entrega o nome dos três integrantes. Quando todos os grupos estiverem formados, um dos temas abaixo é sorteado para cada grupo. Dentro do tema sorteado, **o grupo cria o problema** que o sistema vai resolver: quem tem a dificuldade, qual é ela e o que o sistema muda. A coluna da direita só traz ideias para começar.

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

Cada tema sai para um grupo só. Se houver mais de 9 grupos, os temas voltam para o sorteio, e os grupos com o mesmo tema criam problemas diferentes. Tarefas e biblioteca não valem: são os exemplos usados na disciplina.

## O que cada grupo deve entregar

Um repositório público no GitHub, criado a partir do template desta aula (veja [Como começar pelo template](#como-começar-pelo-template)), com estas cinco coisas:

1. **O README do projeto**, completo e bem escrito. É nele que ficam:
   - o problema que o grupo criou e quem usa o sistema;
   - no mínimo **3 regras de negócio**, criadas pelo grupo a partir desse problema;
   - o mapa da estrutura: todas as classes, por entidade e por camada;
   - as rotas, como executar e o que já funciona.
2. **O desenho dos dados e das rotas**, em dois documentos da pasta `docs/`:
   - `docs/modelo-de-dominio.md`: no mínimo **3 entidades**, pelo menos **1 relacionamento** entre elas e o diagrama das tabelas;
   - `docs/contrato-da-api.md`: as rotas de todas as entidades, com o JSON de entrada e de saída.
3. **A estrutura completa do código**, para **todas** as entidades: a migration que cria a tabela, a entidade (`@Entity`), o Repository, os DTOs de entrada e de saída, e o Mapper, o Service e o Controller, cada classe com um comentário no topo dizendo o que faz.
4. **A API funcionando**:
   - a **API simples**: em uma entidade, as rotas `POST`, `GET` e `GET /{id}`, com erro `400` para campo inválido e `404` para id que não existe;
   - as **regras de negócio implementadas** no Service, com as rotas que elas precisam. Quando uma regra é violada, a API devolve `400` com uma mensagem que explica a regra.
5. **A autoavaliação**, em `docs/autoavaliacao.md`.

**Não precisa:** `PUT`, `DELETE` nem rotas que nenhuma regra usa. As classes que nenhuma rota usa ainda ficam vazias (*montadas*), com o comentário no topo.

**Frontend é opcional.** O grupo pode fazer uma tela, se quiser, mas não precisa: as rotas podem ser testadas e mostradas pelo **Postman** ou pelo Swagger UI. No Postman, dá para importar todas as rotas de uma vez: **Import** → **Link** → `http://localhost:8080/v3/api-docs`, como na Aula 9.

> **Regra de negócio** é algo que o sistema precisa garantir ou impedir, além de "campo obrigatório". Exemplo: *um leitor não pode ter mais de 3 empréstimos em aberto*.
>
> **O grupo cria o problema e as regras de negócio.** Nada disso vem pronto. Primeiro, dentro do tema sorteado, o grupo cria o problema. Depois, olhando para esse problema, cria as regras: o que o sistema precisa garantir ou impedir.
>
> **As regras precisam estar implementadas e atender à necessidade do tema. Isso é avaliado.** Regra só descrita no README, sem código, não conta. Regra genérica, que serviria para qualquer tema, ou copiada do exemplo da biblioteca, também não. Na revisão do código, será avaliado onde cada regra está, e a API vai precisar barrar uma delas.

## Exemplo pronto: a biblioteca

A pasta [exemplo-biblioteca](exemplo-biblioteca) é o template preenchido do jeito que um grupo entregaria. Dá para subir e testar (banco na porta 5438). Use para comparar com o projeto de vocês, não para copiar: biblioteca não é tema, e as regras de vocês precisam ser do problema que vocês criaram.

| O que entregar | Onde ver no exemplo |
|---|---|
| Problema, regras e mapa da estrutura | [README do exemplo](exemplo-biblioteca/README.md): o problema do caderno de empréstimos e as regras R1, R2 e R3 |
| Desenho dos dados e das rotas | [docs/modelo-de-dominio.md](exemplo-biblioteca/docs/modelo-de-dominio.md) e [docs/contrato-da-api.md](exemplo-biblioteca/docs/contrato-da-api.md) |
| Estrutura completa | `src/main/java/api/`: Livro, Leitor e Emprestimo com todas as peças |
| API simples | `LivroController`: cadastrar, listar e buscar livros |
| Regras implementadas | `EmprestimoService`: o empréstimo barra o quarto livro em aberto (R1) e o livro que já saiu (R2), e calcula o prazo de 14 dias (R3) |
| Autoavaliação | [docs/autoavaliacao.md](exemplo-biblioteca/docs/autoavaliacao.md) |

## Como começar pelo template

Todo grupo começa do mesmo ponto: a pasta [template-projeto-final](template-projeto-final), que está nesta aula. Ela já traz um projeto Spring Boot que sobe, os documentos de `docs/` e o README para preencher.

**Não use** como ponto de partida o `exemplo-biblioteca` nem o `exemplo_tarefas` da Aula 9 (eles são só para consultar), um projeto novo do Spring Initializr ou um fork do repositório da disciplina.

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

**Como saber se está no template certo:** na raiz do repositório do grupo aparecem `README.md`, `docker-compose.yml`, `pom.xml`, `mvnw` e as pastas `docs/` (com `modelo-de-dominio.md`, `contrato-da-api.md` e `autoavaliacao.md`) e `src/`. O README começa com o bloco "Este é o ponto de partida do projeto do grupo", e cada pasta de `src/main/java/api/` tem o seu próprio `README.md`.

## Passo a passo e prazos

**Na Aula 10, em sala**

1. Formem o grupo de 3 pessoas e entreguem o nome dos integrantes.
2. Quando todos os grupos estiverem formados, os temas são sorteados.
3. Um integrante cria o repositório do grupo a partir do template, seguindo [Como começar pelo template](#como-começar-pelo-template), e adiciona os outros dois como colaboradores.
4. Os três clonam o repositório do grupo e conferem que o projeto sobe: `docker compose up -d` e `./mvnw spring-boot:run`. O Swagger UI abre em `http://localhost:8080/swagger-ui.html`, ainda sem rotas.
5. Criem o problema e as regras de negócio, direto no README. Depois comecem `docs/modelo-de-dominio.md` e `docs/contrato-da-api.md`.

**Até o início da Aula 11**

6. Montem a estrutura, uma entidade de cada vez, e subam a API depois de cada uma para conferir.
7. Façam a API simples e implementem as regras de negócio.
8. Terminem o README e preencham a autoavaliação.
9. Testem a entrega: um integrante clona o repositório numa pasta vazia e segue o README. Se não subir assim, não vai subir na correção.
10. Enviem um e-mail para **daniel.placido@satc.edu.br** com o assunto `Projeto Final - Etapa 1 - <nome do projeto>` e, no corpo, o nome completo dos 3 integrantes, o tema e o link do repositório.

Vale o que estiver na branch `main` no início da Aula 11. Commits feitos depois disso não entram.

**Na Aula 11, em sala**

11. Apresentem o projeto e respondam às 3 perguntas.

## Apresentação

O grupo tem até **10 minutos**, e **os três integrantes participam**. Quem faltar à apresentação recebe só a nota do projeto. Roteiro sugerido:

| O que mostrar | Tempo |
|---|---|
| O README aberto no GitHub: o problema e as regras de negócio | 2 min |
| O diagrama das tabelas, em `docs/modelo-de-dominio.md` | 2 min |
| As rotas, em `docs/contrato-da-api.md` | 1 min |
| O mapa da estrutura e as classes no código, com onde está cada regra | 2 min |
| A API funcionando, pelo Postman, pelo Swagger UI ou pelo frontend, se houver: a API simples, um `400`, um `404` e as regras barrando pedidos | 3 min |

Depois, o grupo responde a **3 perguntas**, e qualquer integrante presente pode responder:

| Pergunta | Sobre o quê | Vale |
|---|---|---|
| **1** | O desenho: o problema, as regras de negócio, as entidades e os relacionamentos | 1,5 |
| **2** | A estrutura: as camadas e onde cada coisa mora no código | 1,5 |
| **3** | A API funcionando: com a API rodando, mostrar uma requisição ou uma regra de negócio | 2,0 |

## Nota

A nota desta etapa vale como **N2a e N2b** da disciplina:

| Metade | Como será avaliada | Pontos |
|---|---|---|
| **Projeto backend** | Revisão do código no GitHub: cada item da entrega, inclusive se as regras de negócio estão implementadas e atendem ao tema | 5,0 |
| **Avaliação na apresentação** | 3 perguntas ao grupo | 5,0 |
| **Total** | | **10,0** |

A nota é do grupo. A única diferença é a falta: quem não estiver na apresentação recebe só a nota do projeto, até 5,0.

Os critérios da revisão do código, exemplos das perguntas e a ficha de correção estão em [AVALIACAO.md](AVALIACAO.md). Leia antes de começar: é exatamente isso que será avaliado.

## Outra linguagem

Pode, mas exige adaptação: o template, o exemplo e os critérios da revisão foram escritos para Java + Spring Boot. Quem usar outra linguagem:

- monta sozinho a estrutura equivalente, com as mesmas camadas, PostgreSQL com Docker Compose, migrations, DTO separado da entidade, Swagger e o mesmo formato de erro;
- usa a [seção 8 de ARQUITETURA.md](ARQUITETURA.md#8-em-outra-linguagem) para achar o equivalente de cada ferramenta em Node, Python e C#;
- explica no README como o projeto atende cada critério da revisão, já que eles citam nomes do Java (como `@Entity` e `RecursoNaoEncontradoException`).

Os exemplos das próximas aulas continuam em Java + Spring Boot.

## Onde encontrar cada coisa

| Arquivo | Para que serve |
|---|---|
| [template-projeto-final/](template-projeto-final) | O ponto de partida do repositório: um projeto que já sobe, os documentos e o README para preencher |
| [exemplo-biblioteca/](exemplo-biblioteca) | O exemplo pronto: o template preenchido para uma biblioteca, do jeito que um grupo entregaria |
| [ARQUITETURA.md](ARQUITETURA.md) | Como o código deve ser organizado, com exemplos |
| [AVALIACAO.md](AVALIACAO.md) | Como a nota é definida |
| [GRUPOS.md](GRUPOS.md) | Os grupos, os temas sorteados e a ordem de apresentação |

**Se travar, revise:** Git e GitHub na Aula 2; entidades e exceções nas Aulas 3 e 4; rotas e status HTTP nas Aulas 6 e 7; Controller, Service e erros na Aula 7; migrations, `@Entity`, Repository e Docker Compose na Aula 8; DTOs, Mapper e Swagger na Aula 9.
