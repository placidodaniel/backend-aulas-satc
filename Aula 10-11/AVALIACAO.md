# Como a nota é definida (Projeto Final, Etapa 1)

**Disciplina:** Backend (Engenharia de Software)
**Professor:** Daniel Plácido

A nota vai de 0 a 10 e é do grupo: os três integrantes recebem a mesma nota. Ela tem duas metades:

| Metade | Como o professor avalia | Pontos |
|---|---|---|
| **Projeto** | Faz a revisão do código no repositório do GitHub | 5,0 |
| **Apresentação** | Faz 3 perguntas ao grupo durante a apresentação | 5,0 |
| **Total** | | **10,0** |

Ninguém é avaliado separadamente. A apresentação precisa da participação dos três integrantes, mas isso é uma regra da apresentação, não uma nota por pessoa.

## Índice

- [1. Projeto: a revisão do código](#1-projeto-a-revisão-do-código)
- [2. Apresentação: as 3 perguntas](#2-apresentação-as-3-perguntas)
- [3. Passo a passo da avaliação](#3-passo-a-passo-da-avaliação)
- [4. Situações especiais](#4-situações-especiais)
- [5. Ficha de correção em branco](#5-ficha-de-correção-em-branco)

---

## 1. Projeto: a revisão do código

O professor clona o repositório, sobe a API seguindo o README e lê o código. Ele confere 10 pontos, e cada um vale 0,5:

| Marca | Significado | Vale |
|---|---|---|
| ✅ | **Atende:** está como o critério descreve | 0,5 |
| ◐ | **Parcial:** existe, mas incompleto ou com erro | 0,25 |
| ❌ | **Não atende:** não existe | 0 |

Um critério que pede "todas as entidades" e cobre duas de três é parcial.

| Critério | O que o professor procura no código |
|---|---|
| **P1** Sobe pelo README | Seguindo os comandos do README, o banco e a API sobem sem erro |
| **P2** Camadas | Cada classe está no pacote da sua camada (`controller`, `service`, `mapper`, `repository`, `model`, `dto`), com os nomes da convenção |
| **P3** Migrations | As tabelas de todas as entidades são criadas por migrations, com as chaves estrangeiras. Nenhuma tabela é criada de outro jeito |
| **P4** Entidades | Cada tabela tem a sua `@Entity`, com atributos `private` e os relacionamentos mapeados, batendo com a migration |
| **P5** DTOs | Todas as entidades têm DTO de entrada, com as validações, e DTO de saída. Nenhuma entidade aparece no Controller nem no JSON |
| **P6** Estrutura montada e documentada | Todas as entidades têm Repository, Mapper, Service e Controller, ligados pelo construtor, e toda classe tem o comentário no topo dizendo camada, responsabilidade e situação |
| **P7** API simples | `POST`, `GET` e `GET /{id}` funcionam, e a requisição passa por Controller, Service, Mapper e Repository, sem regra de negócio no Controller |
| **P8** Erros | Campo inválido devolve `400` com a lista `campos`; id que não existe devolve `404`. O Service lança uma exceção própria, filha de `RecursoNaoEncontradoException`, em vez de devolver `null` |
| **P9** Código igual ao desenho | As entidades, as colunas e as rotas do código são as de `docs/02` e `docs/03`, e o mapa da estrutura em `docs/04` bate com as classes |
| **P10** README | O README tem todas as seções do template, explica o projeto para quem nunca o viu, diz o que funciona e o que não, e não tem sobra do template |

O que é uma estrutura montada, e o comentário que vai no topo de cada classe, estão na [seção 6 de ARQUITETURA.md](ARQUITETURA.md#6-a-estrutura-montada).

---

## 2. Apresentação: as 3 perguntas

O grupo apresenta o projeto em até 10 minutos, seguindo o roteiro do [enunciado](README.md#apresentação). Depois, o professor faz **3 perguntas**, uma sobre cada parte do projeto. As perguntas são para o grupo, e qualquer integrante pode responder.

| Pergunta | Sobre o quê | Vale |
|---|---|---|
| **1** | O desenho: o problema, as regras de negócio, as entidades e os relacionamentos | 1,5 |
| **2** | A estrutura: as camadas e onde cada coisa mora no código | 1,5 |
| **3** | A API funcionando: o grupo responde com a API rodando e mostra o caminho de uma requisição | 2,0 |

Cada resposta recebe uma de três marcas:

| Resposta | Quando | Vale |
|---|---|---|
| **Completa** | Está correta, e o grupo mostra no projeto: no desenho, no código ou na API rodando | O valor da pergunta |
| **Parcial** | Está correta só em parte, ou está correta mas o grupo não consegue mostrar onde aquilo está no projeto | Metade |
| **Sem resposta** | Está errada, ou o grupo não responde | 0 |

### Exemplos de pergunta

O professor escolhe uma de cada grupo e adapta às entidades e às regras do projeto que está sendo apresentado.

**Pergunta 1: o desenho**

- Qual problema o sistema resolve, e qual regra de negócio é a mais importante?
- Mostre no diagrama ER o relacionamento entre estas duas entidades. Em qual tabela fica a chave estrangeira?
- Por que esta informação virou uma entidade, e não um atributo de outra?

**Pergunta 2: a estrutura**

- Em qual classe vai morar a regra R1? Por que não no Controller?
- Por que o Controller não importa nenhuma classe de `model`?
- O que falta implementar nesta classe montada, e onde isso está escrito?
- Quem cria as tabelas quando a API sobe? O que acontece se alguém editar uma migration que já rodou?

**Pergunta 3: a API funcionando**

- Faça um `POST` e mostre, classe por classe, por onde a requisição passa.
- Busque um id que não existe. Em qual classe nasce o `404`?
- Mande um campo inválido. Quem barra a requisição, e onde está escrita essa validação?
- Qual é a diferença entre o DTO de entrada e o de saída desta rota? Por que não são iguais?

---

## 3. Passo a passo da avaliação

1. **Antes da entrega:** o grupo confere o próprio projeto com os 10 critérios da revisão e marca o resultado na autoavaliação de `docs/05-plano-de-trabalho.md`.
2. **Início da Aula 11:** vale o que estiver na branch `main`. É esse código que o professor revisa.
3. **Na Aula 11:** o grupo apresenta em até 10 minutos e responde às 3 perguntas.
4. **Revisão do código:** o professor clona o repositório, sobe a API pelo README e confere os 10 critérios, usando a autoavaliação como guia.
5. **Nota:** projeto (revisão do código, até 5) + apresentação (3 perguntas, até 5).

---

## 4. Situações especiais

| Situação | O que acontece |
|---|---|
| A API não sobe seguindo o README | P1 fica zerado, e P7 e P8 valem no máximo parcial: o professor ainda lê o código, mas não consegue ver funcionando. Na apresentação, a pergunta 3 vale no máximo parcial |
| Commits feitos depois do início da Aula 11 | Não entram na revisão. Vale o que estava na branch `main` |
| Um integrante não pode participar da apresentação | O grupo avisa o professor antes da Aula 11 |
| Projeto em outra linguagem | Os mesmos critérios. Mudam os nomes das classes e das anotações |
| O grupo fez um frontend | É opcional. A revisão é do backend, e as rotas podem ser mostradas pelo Postman ou pelo Swagger UI |

---

## 5. Ficha de correção em branco

Uma por grupo. As fichas preenchidas ficam com o professor: nota não vai para o repositório.

**Grupo:** ____ **Projeto:** ________________ **Repositório:** ________________

**Projeto: revisão do código**

| Critério | ✅ ◐ ❌ | Pontos |
|---|---|---|
| P1 Sobe pelo README | ☐ | ____ |
| P2 Camadas | ☐ | ____ |
| P3 Migrations | ☐ | ____ |
| P4 Entidades | ☐ | ____ |
| P5 DTOs | ☐ | ____ |
| P6 Estrutura montada e documentada | ☐ | ____ |
| P7 API simples | ☐ | ____ |
| P8 Erros | ☐ | ____ |
| P9 Código igual ao desenho | ☐ | ____ |
| P10 README | ☐ | ____ |
| **Projeto** | | **____ / 5,0** |

**Apresentação: 3 perguntas**

| Pergunta | Qual foi | Completa · Parcial · Sem resposta | Pontos |
|---|---|---|---|
| 1. O desenho | | ☐ | ____ / 1,5 |
| 2. A estrutura | | ☐ | ____ / 1,5 |
| 3. A API funcionando | | ☐ | ____ / 2,0 |
| **Apresentação** | | | **____ / 5,0** |

**Os três integrantes participaram da apresentação:** ☐ sim ☐ não

**Nota do grupo (projeto + apresentação):** ____ / 10,0
