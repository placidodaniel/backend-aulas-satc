# Projeto Final (N3) — API REST com Spring Boot

**Disciplina:** Backend — Engenharia de Software
**Professor:** Daniel Plácido
**Contato:** daniel.placido@satc.edu.br
**SATC 2026.2**

Sistema completo, construído em Spring Boot, que integra um dos tópicos avançados das Aulas 12 a 16 através de uma carta-desafio sorteada — em vez de deixar o "requisito extra" solto e sem critério.

| | |
|---|---|
| **Avaliação** | N3 |
| **Equipes** | As mesmas da Etapa 1 (3 pessoas) |
| **Janela no cronograma** | Aulas 16 → 19 |

---

## Índice

- [1. Onde isso entra no cronograma](#1-onde-isso-entra-no-cronograma)
- [2. O Projeto](#2-o-projeto)
- [3. Baralho de cartas-desafio](#3-baralho-de-cartas-desafio--uma-por-aula-avançada)
- [4. Regra de nota das cartas](#4-regra-de-nota--igual-para-todas-as-cartas)
- [5. Entregas](#5-entregas)
- [6. Documentação — checklist do README](#6-documentação--checklist-do-readme)
- [7. Apresentação](#7-apresentação)
- [8. Rubrica geral — 10 pontos](#8-rubrica-geral--10-pontos)

---

## 1. Onde isso entra no cronograma

A [Etapa 1](<../Aula 10-11/README.md>) (Aulas 10 e 11, vale N2a e N2b) já fez cada equipe criar o problema e as regras de negócio, desenhar o sistema, montar a estrutura completa e fazer funcionar a API simples com as regras implementadas. O N3 pega essa mesma base, no mesmo repositório, e faz a equipe integrar **um** tópico avançado de verdade, sorteado, não escolhido.

| Quando | O que acontece |
|---|---|
| Aulas 12–16 | Tópicos avançados são ensinados: Integração de Serviços, Observabilidade e Testes, MCP, Autenticação e Segurança, Infraestrutura — nessa ordem. |
| Fim da Aula 16 | Sorteio da carta-desafio: um card por equipe, sem repetição na 1ª rodada. Registrado publicamente (planilha/quadro da turma). |
| Aulas 17–18 | Desenvolvimento do Projeto Final: a equipe evolui o repositório da Etapa 1, com o mesmo tema, integrando a carta sorteada. |
| Aula 19 | Entrega e apresentação (N3): demonstração funcionando + README + carta-desafio implementada. |

> Ajustável: se preferir que o N3 seja um projeto novo (não uma evolução da Etapa 1), a mecânica das cartas não muda — só o ponto de partida.

---

## 2. O Projeto

A equipe continua a API da Etapa 1, com o tema sorteado nas Aulas 10 e 11 e as regras de negócio que ela criou, e escolhe pelo menos uma **aplicação prática**. O recomendado é **Spring Boot** (Web + Data JPA); a equipe que usou outra linguagem na Etapa 1 precisa adaptar as ferramentas das cartas.

### Temas

O tema é o mesmo sorteado na Etapa 1. A lista e as regras do sorteio estão no [enunciado da Etapa 1](<../Aula 10-11/README.md#o-tema>).

### Aplicação — escolha pelo menos uma, além do tema

| Aplicação | Exemplo |
|---|---|
| 📡 Integração com Sistemas Físicos (IoT) | Rota recebendo leituras simuladas de sensores |
| 📈 Visualização de Dados | Endpoints de estatísticas/agregações para alimentar um dashboard |
| ⏱️ Processamento em Tempo Real | Fila de eventos processados assim que chegam |
| 🧬 Simulação de Cenários | Simular picos de demanda, falhas ou decisões do domínio |

> O tema define as entidades; a aplicação define um recorte técnico sobre elas. Nenhuma entidade deve ser criada só para "encaixar" a aplicação escolhida — ela deve usar o que já existe no domínio do tema.

### Requisitos obrigatórios

- [ ] Estruturado em camadas **Controller → Service → Repository**.
- [ ] Mínimo de **3 entidades JPA**, com pelo menos duas relacionadas (`@OneToMany` ou `@ManyToMany`).
- [ ] Cada entidade com **Service** próprio, com métodos CRUD (`getOne`, `getAll`, `create`, `update`, `delete`).
- [ ] **DTOs** de entrada e saída — nenhuma rota expõe a `@Entity` diretamente.
- [ ] Validação com Bean Validation (`@Valid`, `@NotBlank`, `@Positive`...), erros tratados via `@RestControllerAdvice`.
- [ ] Paginação e ordenação no `GET` de listagem (`Pageable` do Spring Data).
- [ ] Filtros de busca no `GET` de listagem (parâmetros opcionais).
- [ ] Persistência real em **PostgreSQL**, subindo pelo `docker-compose.yml`, como desde a Etapa 1.
- [ ] Documentação automatizada com **Swagger/OpenAPI** (`springdoc-openapi`), que também vem da Etapa 1.
- [ ] Arquivo `.env` (ou `application.properties` equivalente) com as variáveis de conexão do banco (usuário, senha, URL) — commitado no repositório, fora do `.gitignore`. Sem credenciais de produção, só valores locais de desenvolvimento.
- [ ] `README.md`: tema, integrantes, entidades, rotas com exemplo de requisição/resposta, erros HTTP, como rodar localmente.
- [ ] Repositório público no GitHub, clonável, com todo o código e o `.env` já commitados e enviados (`git push`) — ninguém deve precisar de configuração extra fora do repositório.

> Frontend não é obrigatório — fica a critério da equipe. A API pode ser entregue e demonstrada inteiramente via Postman/curl, sem nenhuma tela.

### Extras (fora da carta sorteada, pontuam à parte)

- [ ] Implementar um **segundo** tópico avançado além do sorteado pela carta.
- [ ] Deploy real em ambiente de produção.

---

## 3. Baralho de cartas-desafio — uma por Aula avançada

Cinco cartas, na mesma ordem em que os tópicos aparecem no cronograma. Cada uma usa exatamente a ferramenta que a Aula correspondente ensina — nada genérico.

### C1 — 🔗 Elo Externo
**Origem:** Aula 12: Integração de Serviços

> "Como sistema, quero consumir um serviço externo de verdade, para agregar valor sem reconstruir o que já existe pronto."

**Requisito:** consumir uma API pública real via `RestTemplate` ou `WebClient`.

- Pelo menos uma rota consome dados de uma API externa real (clima, câmbio, geocodificação, etc.)
- Falha do serviço externo é tratada (timeout, erro, fallback) sem derrubar a aplicação
- Dado externo é combinado com dado próprio na resposta — não é um simples repasse

### C2 — 🔬 Caixa de Vidro
**Origem:** Aula 13: Observabilidade e Testes

> "Como responsável pela operação, quero enxergar o que a aplicação está fazendo e confiar que ela não quebrou sem eu perceber."

**Requisito:** logging estruturado + suíte de testes automatizados.

- Operações críticas (criar, atualizar, excluir) geram log com nível apropriado (INFO/WARN/ERROR)
- Existe suíte de testes (JUnit) cobrindo um Service, com casos de sucesso e de erro
- Os testes rodam via `./mvnw test` sem intervenção manual

### C3 — 🤖 Segundo Cérebro
**Origem:** Aula 14: MCP + IA no Desenvolvimento

> "Como desenvolvedor, quero expor parte da minha API para um agente de IA operar, para automatizar tarefas do domínio."

**Requisito:** servidor MCP expondo ao menos uma funcionalidade real da API.

- Existe um servidor MCP com pelo menos uma tool ligada a uma funcionalidade real da API
- A tool tem descrição clara o bastante para um agente de IA saber quando usá-la
- Equipe demonstra (print, vídeo ou log) um agente de IA chamando a tool com sucesso

### C4 — 🔐 Guardião
**Origem:** Aula 15: Autenticação e Segurança

> "Como responsável pelo sistema, quero que só usuários autenticados acessem rotas sensíveis, para proteger os dados do domínio."

**Requisito:** autenticação JWT (Spring Security) nas rotas de escrita.

- Existe rota de login que devolve um token JWT válido
- Rotas de escrita (`POST`/`PUT`/`DELETE`) exigem token válido, respondendo 401 sem ele
- Senhas são armazenadas com hash (ex.: BCrypt), nunca em texto puro

### C5 — 📦 Arca
**Origem:** Aula 16: Infraestrutura

> "Como responsável pelo deploy, quero empacotar a aplicação para que ela rode em qualquer máquina sem 'na minha máquina funciona'."

**Requisito:** containerização com Docker.

- Existe um `Dockerfile` que builda e roda a aplicação Spring Boot
- Existe um `docker-compose.yml` subindo a aplicação junto do banco de dados
- `docker compose up` sobe o sistema completo funcionando, sem passos manuais extras

### 🌌 Carta secreta — não sorteada

Ninguém recebe essa por sorteio. Fica aberta pra qualquer equipe que quiser ir além por conta própria — implementar mensageria de verdade é claramente mais difícil que as outras cinco cartas juntas.

**C★ — 🚀 Mensageiro Interestelar** *(Ultra Galáxia — não faz parte do currículo desta disciplina)*

> "Como sistema, quero desacoplar quem produz um evento de quem o processa, através de uma fila de mensagens de verdade, para que um pico de carga não derrube a aplicação."

**Requisito:** mensageria real (RabbitMQ, Kafka ou equivalente) processando ao menos um fluxo de forma assíncrona — não vale fila em memória.

- Existe um producer publicando mensagens em uma fila/tópico de um broker real, rodando via Docker
- Existe um consumer separado, processando essas mensagens de forma assíncrona
- Se o broker atrasar ou cair, a rota que recebeu a requisição original continua respondendo — nunca trava esperando o processamento

> Vale como Excelente automático na linha "Extra" da rubrica — não acumula com os outros extras, é o teto.

---

## 4. Regra de nota — igual para todas as cartas

Nenhuma carta é julgada no "achismo": a nota depende de quantos dos 3 critérios de aceite (acima, por carta) foram cumpridos, mais um critério qualitativo de integração ao domínio.

| Nível | Nota base | Critério |
|---|---|---|
| Excelente | 1,0 | Cumpre os 3 critérios de aceite **e** a integração faz sentido dentro do tema escolhido pela equipe |
| Satisfatório | 0,6 | Cumpre os 3 critérios, mas de forma genérica — funciona, porém parece um módulo à parte |
| Insatisfatório | 0,2 | Cumpre apenas 1 ou 2 dos 3 critérios de aceite |
| Não atende | 0,0 | Nenhum critério cumprido, ou a carta sorteada não foi implementada |

| Nível na carta sorteada | Nota base | Peso ×2 |
|---|---|---|
| Excelente | 1,0 | 2,0 pts |
| Satisfatório | 0,6 | 1,2 pts |
| Insatisfatório | 0,2 | 0,4 pts |
| Não atende | 0,0 | 0,0 pt |

> Baralho fechado em 5 cartas de propósito — uma por Aula avançada, então uma turma com mais de 5 equipes reembaralha, mas nunca duas equipes com a mesma carta na mesma rodada.

---

## 5. Entregas

A Etapa 1 (N2a e N2b) já cobriu o tema, a arquitetura, a API simples e as regras de negócio. O N3 não repete essas etapas, só formaliza o plano da carta sorteada antes de começar a implementar.

| Entrega | Quando | O que entregar |
|---|---|---|
| **Entrega 01 — Plano da Carta** | Início da Aula 17, logo após o sorteio | Documento curto (Markdown): carta sorteada, quais entidades/rotas serão afetadas, dependências novas a adicionar no `pom.xml`, maior dúvida ou risco identificado. |
| **Entrega Final** | Aula 19 | Endereço do repositório público: API da Etapa 1 evoluída, carta-desafio implementada, `README.md` completo (checklist abaixo). |

---

## 6. Documentação — checklist do README

Documentação incompleta é a forma mais comum de perder ponto sem perceber. O `README.md` final precisa cobrir, nesta ordem:

- [ ] Descrição do projeto e integrantes do grupo
- [ ] Descrição do problema — o domínio (tema) e a aplicação prática escolhidos
- [ ] Tecnologias utilizadas — Spring Boot, Spring Data JPA, PostgreSQL, Docker, e qualquer biblioteca extra trazida pela carta sorteada
- [ ] Limitações do projeto — o que ficou de fora ou é conhecido como incompleto
- [ ] Descrição de cada entidade
- [ ] Descrição de cada rota, com exemplo de requisição e resposta
- [ ] Exemplos de erros HTTP retornados pela API
- [ ] Como executar o projeto localmente (via `docker-compose up`)
- [ ] Qual carta-desafio foi sorteada e como foi implementada

---

## 7. Apresentação

**Tempo:** 5 min de organização + **15 min** de apresentação + 5 min de perguntas, por equipe. Ajustável conforme o número de equipes na turma.

Todos os integrantes precisam estar presentes para receber nota — quem faltar não pontua nessa linha, mesmo que o restante da equipe apresente.

Pontos que precisam aparecer na apresentação:

- [ ] Integrantes da equipe e o papel de cada um no projeto
- [ ] Tema e aplicação escolhidos — contextualização do domínio
- [ ] Demonstração funcionando ao vivo, **incluindo a carta-desafio sorteada** — via Postman/curl; se a equipe tiver construído um frontend (opcional), a demonstração também pode ser feita por inspeção do navegador (`F12` → aba Network, como na Aula 2)
- [ ] Breve descrição da arquitetura (camadas, entidades, relacionamentos)
- [ ] Apresentação breve do `README.md`

---

## 8. Rubrica geral — 10 pontos

A linha de Cartas-Desafio já foi definida na [Seção 4](#4-regra-de-nota--igual-para-todas-as-cartas). As demais seis linhas seguem a mesma lógica: nota por critérios concretos, não por impressão geral.

### Criatividade (peso 2×)

| Nível | Nota | Critério |
|---|---|---|
| Excelente | 1,0 | A aplicação escolhida resolve algo do domínio que não seria óbvio só com CRUD básico — regra de negócio, cálculo ou fluxo específico daquele tema |
| Satisfatório | 0,6 | A aplicação está implementada, mas de forma genérica — poderia ser copiada para qualquer outro tema sem quase mudar nada |
| Insatisfatório | 0,2 | A aplicação aparece de forma superficial — ex.: um único endpoint decorativo |
| Não atende | 0,0 | Nenhuma aplicação prática foi implementada |

### Tema e Aplicação (peso 1×)

| Nível | Nota | Critério |
|---|---|---|
| Excelente | 1,0 | Entidades, nomes e regras de negócio pertencem claramente ao tema, e a aplicação prática usa os dados reais do domínio |
| Satisfatório | 0,6 | Tema e aplicação presentes, mas com conexão superficial — nomes genéricos, pouca regra de negócio real |
| Insatisfatório | 0,2 | Tema ou aplicação parecem colados por fora, sem integração com o resto do sistema |
| Não atende | 0,0 | Tema não é reconhecível, ou a aplicação não foi implementada |

### Código (peso 1×)

| Nível | Nota | Critério |
|---|---|---|
| Excelente | 1,0 | Controller/Service/Repository bem separados, nomes claros, sem regra de negócio no Controller, sem duplicação evidente |
| Satisfatório | 0,6 | Camadas presentes, mas com alguma inconsistência — ex.: regra de negócio vazando para o Controller |
| Insatisfatório | 0,2 | Separação existe só de nome — Controller faz o trabalho do Service, ou padrão inconsistente entre entidades |
| Não atende | 0,0 | Código concentrado em um único lugar, sem separação de responsabilidades |

### Documentação (peso 2×)

| Nível | Nota | Critério |
|---|---|---|
| Excelente | 1,0 | README cobre 100% do checklist da Seção 6, bem formatado |
| Satisfatório | 0,6 | README cobre a maior parte do checklist, faltando 1 ou 2 itens, ou com formatação confusa |
| Insatisfatório | 0,2 | README existe, mas cobre menos da metade do checklist |
| Não atende | 0,0 | README ausente ou vazio |

### Apresentação (peso 2×)

| Nível | Nota | Critério |
|---|---|---|
| Excelente | 1,0 | Cobriu todos os pontos obrigatórios da Seção 7, dentro do tempo, com demo ao vivo funcionando (incluindo a carta sorteada) |
| Satisfatório | 0,6 | Cobriu os pontos obrigatórios, mas ultrapassou o tempo ou teve falha pontual na demo |
| Insatisfatório | 0,2 | Faltou cobrir claramente algum ponto obrigatório — ex.: não mostrou a carta-desafio funcionando |
| Não atende | 0,0 | Apresentação não ocorreu, ou não demonstrou o projeto funcionando |

### Extra — bônus adicional (peso 2×)

| Nível | Nota | Critério |
|---|---|---|
| Excelente | 1,0 | Implementou mais de um item extra, ou a carta secreta 🌌 Ultra Galáxia (mensageria) |
| Satisfatório | 0,6 | Implementou um item extra por completo |
| Insatisfatório | 0,2 | Implementou um item extra parcialmente |
| Não atende | 0,0 | Nenhum item extra implementado |

### Como fecha os 10 pontos

| Critério | Peso | Pontos máximos |
|---|---|---|
| Criatividade | 2× | 2,0 |
| Tema e Aplicação | 1× | 1,0 |
| Código | 1× | 1,0 |
| Documentação | 2× | 2,0 |
| Apresentação | 2× | 2,0 |
| Carta-Desafio (Seção 4) | 2× | 2,0 |
| **Total** | **10×** | **10,0** |
| Extra (bônus, não soma ao denominador) | 2× | até 2,0 adicionais |

> Extra é bônus de verdade: compensa perdas em outras linhas, mas a nota final nunca passa de 10,0.
