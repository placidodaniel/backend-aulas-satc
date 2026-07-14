# Backend — Engenharia de Software

**Disciplina:** Backend — Engenharia de Software
**Professor:** Daniel Plácido
**Contato:** phpsistemas@gmail.com
**SATC 2026.1**

## Índice

- [Aula 1 — Introdução à Disciplina e Arquitetura da Web](#aula-1--introdução-à-disciplina-e-arquitetura-da-web)
  - [Tópico 00 — Introdução à Disciplina](#tópico-00--introdução-à-disciplina)
  - [Tópico 01 — Introdução à Web](#tópico-01--introdução-à-web)
  - [Tópico 02 — Arquitetura da Web](#tópico-02--arquitetura-da-web)
- [Aula 2 — Ambiente de Desenvolvimento](#aula-2--ambiente-de-desenvolvimento)

---

## Aula 1 — Introdução à Disciplina e Arquitetura da Web

**Materiais:**

| Arquivo | Conteúdo |
|---|---|
| [P1_Introducao_a_Disciplina.pdf](<Aula 01/P1_Introducao_a_Disciplina.pdf>) | Tópico 00 — Introdução à Disciplina |
| [P1_Material_de_Apoio.pdf](<Aula 01/P1_Material_de_Apoio.pdf>) | Material de apoio do Tópico 00 |
| [P2_Introducao_a_Web.pdf](<Aula 01/P2_Introducao_a_Web.pdf>) | Tópico 01 — Introdução à Web |
| [P2_Material_de_Apoio.pdf](<Aula 01/P2_Material_de_Apoio.pdf>) | Material de apoio do Tópico 01 |
| [P3_Arquitetura_da_Web.pdf](<Aula 01/P3_Arquitetura_da_Web.pdf>) | Tópico 02 — Arquitetura da Web |

Esta aula reúne três apresentações, que juntas dão a base necessária antes de começarmos a programar:

1. **Tópico 00 — Introdução à Disciplina**
2. **Tópico 01 — Introdução à Web**
3. **Tópico 02 — Arquitetura da Web**
---

### Tópico 00 — Introdução à Disciplina

#### O que é Backend?

Todo sistema é dividido em duas grandes partes que trabalham juntas — como um restaurante:

- **Frontend (o salão):** o que o cliente vê e toca — telas, botões, formulários.
- **Backend (a cozinha):** o que o cliente não vê — regras de negócio, processamento e banco de dados.
O Backend recebe pedidos, aplica regras de negócio, conversa com o banco de dados e devolve uma resposta. É o que estudaremos durante todo o semestre.

**Exemplos do dia a dia:** iFood (calcula frete, reserva pedido na cozinha), Instagram (registra curtidas, decide o feed), Netflix (salva onde você parou, libera vídeo só para assinantes), banco (confere saldo, valida senha, autoriza transferência).

#### Ementa: as 3 grandes etapas

| Etapa | Conteúdo |
|---|---|
| **1ª — Ambiente de Desenvolvimento** | Fundamentos da Web, protocolo HTTP, linguagem Java (variáveis, orientação a objetos, lógica de programação) |
| **2ª — API REST** | Web Services, arquitetura REST, persistência de dados, DTOs, construção de uma API completa em Java com Spring |
| **3ª — Tópicos Avançados** | Integração de serviços, observabilidade e testes, Model Context Protocol (MCP), autenticação e segurança, infraestrutura, projeto final |

#### Cronograma completo — 19 aulas

| Aula | Conteúdo |
|---|---|
| 1 | Introdução à Disciplina + Arquitetura da Web |
| 2 | Ambiente de Desenvolvimento |
| 3-4 | Orientação a Objetos em Java (parte I e II) |
| 5 | Avaliação N1 |
| 6 | Web Services + Protocolo HTTP/JSON |
| 7 | Padrões de Arquitetura + Arquitetura REST |
| 8 | Persistência de Dados |
| 9 | DTO e Mapeamento |
| 10 | Avaliação N2a |
| 11 | Projeto Intermediário N2b |
| 12 | Integração de Serviços |
| 13 | Observabilidade e Testes |
| 14 | Model Context Protocol (MCP) |
| 15 | Autenticação e Segurança |
| 16 | Infraestrutura |
| 17-18 | Desenvolvimento do Projeto Final |
| 19 | Projeto Final — Avaliação N3 |

*Datas específicas a definir — cronograma sujeito a ajustes.*

#### Avaliação

| Código | Tipo | O que cobre |
|---|---|---|
| **N1** | Prova teórica | Arquitetura da Web, HTTP e Orientação a Objetos em Java |
| **N2a** | Prova teórica | Web Services, REST e Persistência de Dados |
| **N2b** | Projeto prático | Construção de uma API REST funcional |
| **N3** | Projeto Final | Sistema completo, integrando os tópicos avançados |

#### Onde encontrar o material

Todo o material da disciplina (slides, materiais de apoio, exemplos de código) fica disponível e sempre atualizado no repositório do GitHub. Sempre que uma pasta for atualizada, o material mais novo estará lá — não é necessário pedir por e-mail.

#### Ferramentas usadas na disciplina

| Ferramenta | O que é |
|---|---|
| **JDK** | O "motor" que traduz e executa código Java |
| **IDE (ex.: IntelliJ)** | Editor de código com ajuda automática |
| **Git** | Guarda o histórico de versões do código |
| **GitHub** | Hospeda repositórios Git na nuvem |
| **Terminal** | Tela de texto para executar comandos diretamente |
| **API** | Conjunto de operações que um sistema oferece a outros sistemas |
| **Banco de dados** | Guarda informação de forma organizada e permanente |
| **Postman** | Testa APIs manualmente, simulando requisições |

---

### Tópico 01 — Introdução à Web

#### Internet x Web

- **Internet:** a infraestrutura — cabos, servidores e protocolos que conectam computadores no mundo todo.
- **Web (WWW):** um dos serviços que roda sobre a internet, baseado em páginas conectadas por links.
E-mail, WhatsApp e chamadas de vídeo também rodam sobre a internet, mas não são "a Web".

#### O nascimento da Web

Em 1989, **Tim Berners-Lee**, no CERN (Suíça), propôs um sistema de hipertexto para resolver o problema de pesquisadores trocando documentos entre computadores diferentes. Dessa proposta nasceram três pilares: **HTML**, **URL** e **HTTP**.

#### Cliente e Servidor

- **Cliente:** o navegador — faz o pedido, mas não guarda dados nem processa regras.
- **Servidor:** recebe o pedido, processa e devolve o resultado. É onde vive o Backend.
#### Anatomia de uma URL

```
https://www.satc.edu.br/cursos/backend?turma=2026
```

| Parte | Nome | Função |
|---|---|---|
| `https://` | Protocolo | A "língua" usada na conversa |
| `www.satc.edu.br` | Domínio | Identifica o servidor |
| `/cursos/backend` | Caminho | Página/recurso específico dentro do servidor |
| `?turma=2026` | Parâmetros | Informações extras enviadas junto do pedido |

#### Ciclo Requisição-Resposta

1. Você digita ou clica
2. O navegador monta uma requisição
3. A requisição viaja pela internet
4. O servidor processa o pedido
5. O servidor devolve uma resposta
6. O navegador exibe o resultado
#### Evolução da Web

| Era | Período | Características |
|---|---|---|
| **Web 1.0** | ~1991–2004 | Páginas estáticas, só leitura, sem conta de usuário |
| **Web 2.0** | ~2004–2010 | Interação, redes sociais, conteúdo gerado pelo usuário |
| **Web 3.0** | ~2010–atual | Web semântica, mobile-first, APIs, descentralização |
| **Web 4.0/5.0** | 2020s em diante | IA generativa integrada, protocolos como o MCP, web onipresente |

#### Roadmap de habilidades para 2026

1. Lógica de programação
2. Uma linguagem backend (Java)
3. Banco de dados
4. APIs e arquitetura REST
5. Versionamento com Git
6. Nuvem (Cloud) e infraestrutura
---

### Tópico 02 — Arquitetura da Web

#### O que é a arquitetura da Web?

Conjunto de tecnologias e padrões que fazem a Web funcionar: **URI, HTTP, DNS e TLS**. A Web foi projetada para ser **simples**, **universal** e **escalável**.

#### URI (Uniform Resource Identifier)

```
https://exemplo.com/artigos/123?ref=homepage
```

| Parte | Nome | Função |
|---|---|---|
| `https://` | Esquema | Protocolo usado para acessar o recurso |
| `exemplo.com` | Domínio | Identifica o servidor |
| `/artigos/123` | Caminho | Recurso específico solicitado |
| `?ref=homepage` | Query / parâmetros | Informações extras do pedido |

#### DNS (Domain Name System)

A "lista telefônica" da Internet — traduz nomes em endereços IP.

```
exemplo.com → 2.2.2.2
```

#### HTTP (HyperText Transfer Protocol)

Define como cliente e servidor conversam, no modelo requisição → resposta.

```http
GET /index.html HTTP/1.1
Host: exemplo.com
```

**Métodos:**

| Método | Intenção |
|---|---|
| `GET` | Buscar/ler um recurso |
| `POST` | Criar um novo recurso |
| `PUT` | Atualizar um recurso existente |
| `DELETE` | Remover um recurso |

**Códigos de status:**

| Faixa | Categoria | Exemplo |
|---|---|---|
| `2xx` | Sucesso | `200 OK` |
| `3xx` | Redirecionamento | `301 Moved` |
| `4xx` | Erro do cliente | `404 Not Found` |
| `5xx` | Erro do servidor | `500 Internal Server Error` |

#### TLS (Transport Layer Security) e HTTPS

Garante **confidencialidade** (criptografia), **integridade** (detecção de alterações) e **autenticidade** (certificado digital). HTTP + TLS = HTTPS.

**Handshake TLS:**

1. Cliente envia informações iniciais
2. Servidor responde com certificado digital
3. Cliente verifica o certificado
4. Cliente e servidor criam uma chave de sessão e passam a criptografar a comunicação
#### Fluxo completo de uma requisição

1. Cliente digita/clica em um URI
2. Domínio resolvido via DNS → IP do servidor
3. Cliente abre conexão TCP (porta 80 ou 443)
4. Handshake TLS, se HTTPS
5. Cliente envia a requisição HTTP
6. Servidor processa a requisição
7. Servidor envia a resposta HTTP
8. Cliente renderiza a página
9. Conexão fechada ou mantida aberta (keep-alive)
10. Novas requisições seguem o mesmo fluxo
#### Mão na massa

**Terminal:**
```bash
curl -v https://exemplo.com
```

**DevTools do navegador:** `F12` → aba **Network** → recarregar a página → inspecionar qualquer requisição (método, headers, status code, corpo da resposta).

#### Servidor Web x Servidor de Aplicação

| | Servidor Web | Servidor de Aplicação |
|---|---|---|
| **Função** | Entrega arquivos estáticos e respostas de aplicações | Executa a lógica de negócio da aplicação |
| **Exemplos** | Apache, Nginx, IIS | APIs REST, aplicações corporativas |

#### Frameworks

| Stack | Servidor embutido |
|---|---|
| **Java / Spring Boot** ⭐ (usado nesta disciplina) | Tomcat |
| **Python / Django, Flask** | Werkzeug, Gunicorn, Uvicorn |
| **Node.js / Express.js** | O próprio Node.js |

---

### Resumo geral da Aula 1

- Backend é a "cozinha" de um sistema: regras de negócio, processamento e banco de dados.
- Internet é a infraestrutura; Web é um dos serviços que rodam sobre ela.
- Toda página envolve um Cliente (navegador) e um Servidor (Backend), conversando via HTTP.
- URI identifica o recurso; DNS traduz nomes em IP; TLS garante segurança (HTTPS).
- A Web evoluiu de estática (1.0) para inteligente e integrada a IA (3.0/4.0/5.0).
- Servidor Web entrega conteúdo; Servidor de Aplicação roda a lógica de negócio — é onde o código desta disciplina vai viver (Java/Spring Boot).
**Próxima aula:** Ambiente de Desenvolvimento — instalação e configuração de JDK, IDE e Git.

---

## Aula 2 — Ambiente de Desenvolvimento

**Materiais:** projeto Maven [`Aula 02/ola-mundo`](<Aula 02/ola-mundo>) — Hello World em Java.

_Slides desta aula ainda não foram adicionados a este repositório._
