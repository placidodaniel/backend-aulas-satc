# Como a nota é definida (Projeto Final, Etapa 1)

**Disciplina:** Backend (Engenharia de Software)
**Professor:** Daniel Plácido

A nota desta etapa vale como **N2a e N2b** da disciplina e vai de 0 a 10. Ela tem duas metades:

| Metade | Como será avaliada | Pontos |
|---|---|---|
| **Projeto backend** | Revisão do código no repositório do GitHub | 5,0 |
| **Avaliação na apresentação** | 3 perguntas ao grupo | 5,0 |
| **Total** | | **10,0** |

A nota do projeto é a mesma para os três integrantes. A da apresentação também, para quem estiver presente: **quem faltar à apresentação recebe só a nota do projeto**, até 5,0.

## Índice

- [1. Projeto: a revisão do código](#1-projeto-a-revisão-do-código)
- [2. Apresentação: as 3 perguntas](#2-apresentação-as-3-perguntas)
- [3. Passo a passo da avaliação](#3-passo-a-passo-da-avaliação)
- [4. Situações especiais](#4-situações-especiais)
- [5. Ficha de correção em branco](#5-ficha-de-correção-em-branco)

---

## 1. Projeto: a revisão do código

Na revisão, o repositório é clonado, a API sobe seguindo o README e o código é lido. São avaliados 10 critérios, e cada um vale 0,5:

| Marca | Significado | Vale |
|---|---|---|
| ✅ | **Atende:** está como o critério descreve | 0,5 |
| ◐ | **Parcial:** existe, mas incompleto ou com erro | 0,25 |
| ❌ | **Não atende:** não existe | 0 |

Um critério que pede "todas as entidades" e cobre duas de três é parcial.

| Critério | O que será avaliado no código |
|---|---|
| **P1** Sobe pelo README | Seguindo os comandos do README, o banco e a API sobem sem erro |
| **P2** Camadas | Cada classe está no pacote da sua camada (`controller`, `service`, `mapper`, `repository`, `model`, `dto`), com os nomes da convenção |
| **P3** Banco e entidades | As migrations criam as tabelas de todas as entidades, com as chaves estrangeiras, e cada tabela tem a sua `@Entity` batendo com a migration. Nenhuma tabela é criada de outro jeito |
| **P4** DTOs | Todas as entidades têm DTO de entrada, com as validações, e DTO de saída. Nenhuma entidade aparece no Controller nem no JSON |
| **P5** Estrutura completa e documentada | Todas as entidades têm Repository, Mapper, Service e Controller, ligados pelo construtor; toda classe tem o comentário no topo; o mapa da estrutura no README bate com as classes |
| **P6** API simples | `POST`, `GET` e `GET /{id}` de uma entidade funcionam passando por todas as camadas. Campo inválido devolve `400`; id que não existe devolve `404`, com uma exceção própria no Service |
| **P7** Regras de negócio implementadas | As regras de negócio do README, no mínimo 3, estão **implementadas** no Service e funcionam pela API. Quando uma regra é violada, a API devolve `400` com uma mensagem que explica a regra. Regra só descrita, sem código, não conta |
| **P8** Regras que atendem ao tema | As regras resolvem o problema que o grupo criou, dentro do tema sorteado: fazem sentido para quem usa o sistema. Regras genéricas, que serviriam para qualquer tema, ou copiadas do exemplo da biblioteca, não atendem |
| **P9** Código igual ao desenho | As entidades, as colunas e as rotas do código são as de `docs/modelo-de-dominio.md` e `docs/contrato-da-api.md` |
| **P10** README | O README tem todas as seções do template, explica o projeto para quem nunca o viu, diz o que funciona e o que não, e não tem sobra do template |

As regras de negócio (P7 e P8) valem 1 ponto dos 5 do projeto: é onde fica claro se o projeto é do grupo. As duas serão avaliadas lendo o código e fazendo a API barrar uma regra.

O que é uma estrutura montada, e o comentário que vai no topo de cada classe, estão na [seção 6 de ARQUITETURA.md](ARQUITETURA.md#6-a-estrutura-montada).

---

## 2. Apresentação: as 3 perguntas

O grupo apresenta o projeto em até 10 minutos, seguindo o roteiro do [enunciado](README.md#apresentação). Depois, o grupo responde a **3 perguntas**, uma sobre cada parte do projeto. As perguntas são para o grupo, e qualquer integrante presente pode responder.

| Pergunta | Sobre o quê | Vale |
|---|---|---|
| **1** | O desenho: o problema, as regras de negócio, as entidades e os relacionamentos | 1,5 |
| **2** | A estrutura: as camadas e onde cada coisa mora no código | 1,5 |
| **3** | A API funcionando: o grupo responde com a API rodando, mostrando uma requisição ou uma regra de negócio | 2,0 |

Cada resposta recebe uma de três marcas:

| Resposta | Quando | Vale |
|---|---|---|
| **Completa** | Está correta, e o grupo mostra no projeto: no desenho, no código ou na API rodando | O valor da pergunta |
| **Parcial** | Está correta só em parte, ou está correta mas o grupo não consegue mostrar onde aquilo está no projeto | Metade |
| **Sem resposta** | Está errada, ou o grupo não responde | 0 |

### Exemplos de pergunta

Para cada grupo sai uma pergunta de cada tipo, adaptada às entidades e às regras do projeto que está sendo apresentado.

**Pergunta 1: o desenho**

- Qual problema o sistema resolve, e por que esta regra de negócio é importante para ele?
- Mostre no diagrama ER o relacionamento entre estas duas entidades. Em qual tabela fica a chave estrangeira?
- Por que esta informação virou uma entidade, e não um atributo de outra?

**Pergunta 2: a estrutura**

- Em qual classe está a regra R1? Por que ela não está no Controller?
- Por que o Controller não importa nenhuma classe de `model`?
- Quem cria as tabelas quando a API sobe? O que acontece se alguém editar uma migration que já rodou?
- Qual é a diferença entre o DTO de entrada e o de saída desta rota? Por que não são iguais?

**Pergunta 3: a API funcionando**

- Faça um `POST` e mostre, classe por classe, por onde a requisição passa.
- Faça uma requisição que viole uma regra de negócio. Onde ela é barrada, e o que o cliente recebe?
- Busque um id que não existe. Em qual classe nasce o `404`?
- Mande um campo inválido. Quem barra a requisição, e onde está escrita essa validação?

---

## 3. Passo a passo da avaliação

1. **Antes da entrega:** o grupo confere o próprio projeto com os 10 critérios e marca o resultado em `docs/autoavaliacao.md`.
2. **Início da Aula 11:** vale o que estiver na branch `main`. É esse código que será revisado.
3. **Na Aula 11:** o grupo apresenta em até 10 minutos e responde às 3 perguntas.
4. **Revisão do código:** o repositório é clonado, a API sobe pelo README e os 10 critérios são avaliados, com a autoavaliação como guia.
5. **Nota:** projeto (até 5) + apresentação (até 5). Quem faltou à apresentação fica com a nota do projeto.

---

## 4. Situações especiais

| Situação | O que acontece |
|---|---|
| Um integrante falta à apresentação | Recebe só a nota do projeto, até 5,0. Os outros dois apresentam e recebem a nota completa |
| A API não sobe seguindo o README | P1 fica zerado, e P6 e P7 valem no máximo parcial: o código ainda é lido, mas não dá para ver funcionando. Na apresentação, a pergunta 3 vale no máximo parcial |
| Commits feitos depois do início da Aula 11 | Não entram na revisão. Vale o que estava na branch `main` |
| O grupo fez um frontend | É opcional. A revisão é do backend, e as rotas podem ser mostradas pelo Postman ou pelo Swagger UI |
| Projeto em outra linguagem | Os critérios citam nomes do Java e do Spring. Em outra linguagem, o grupo adapta: monta a mesma estrutura com as ferramentas equivalentes e explica no README onde cada critério aparece no projeto. Veja a seção "Outra linguagem" do [enunciado](README.md#outra-linguagem) |

---

## 5. Ficha de correção em branco

Uma por grupo. As fichas preenchidas não vão para o repositório: a nota não é publicada.

**Grupo:** ____ **Projeto:** ________________ **Repositório:** ________________

**Projeto backend: revisão do código**

| Critério | ✅ ◐ ❌ | Pontos |
|---|---|---|
| P1 Sobe pelo README | ☐ | ____ |
| P2 Camadas | ☐ | ____ |
| P3 Banco e entidades | ☐ | ____ |
| P4 DTOs | ☐ | ____ |
| P5 Estrutura completa e documentada | ☐ | ____ |
| P6 API simples | ☐ | ____ |
| P7 Regras de negócio implementadas | ☐ | ____ |
| P8 Regras que atendem ao tema | ☐ | ____ |
| P9 Código igual ao desenho | ☐ | ____ |
| P10 README | ☐ | ____ |
| **Projeto** | | **____ / 5,0** |

**Avaliação na apresentação: 3 perguntas**

| Pergunta | Qual foi | Completa · Parcial · Sem resposta | Pontos |
|---|---|---|---|
| 1. O desenho | | ☐ | ____ / 1,5 |
| 2. A estrutura | | ☐ | ____ / 1,5 |
| 3. A API funcionando | | ☐ | ____ / 2,0 |
| **Apresentação** | | | **____ / 5,0** |

| Integrante | Presente na apresentação | Nota (projeto + apresentação) |
|---|---|---|
| | ☐ sim ☐ não | ____ / 10,0 |
| | ☐ sim ☐ não | ____ / 10,0 |
| | ☐ sim ☐ não | ____ / 10,0 |
