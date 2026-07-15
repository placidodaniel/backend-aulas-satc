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
- [Aula 2 — Ambiente de Desenvolvimento: Java na prática + Git na prática](#aula-2--ambiente-de-desenvolvimento-java-na-prática--git-na-prática)
  - [Materiais](#materiais)
  - [Objetivo da aula](#objetivo-da-aula)
  - [Parte 1 — Java na prática](#parte-1--java-na-prática)
  - [Parte 2 — Git na prática](#parte-2--git-na-prática)
  - [O que aprendemos hoje](#o-que-aprendemos-hoje)
- [Aula 3 — Orientação a Objetos em Java + Tópicos Especiais](#aula-3--orientação-a-objetos-em-java--tópicos-especiais)
  - [Materiais](#materiais-1)
  - [Objetivo da aula](#objetivo-da-aula-1)
  - [Parte 1 — Orientação a Objetos em Java](#parte-1--orientação-a-objetos-em-java)
  - [Parte 2 — Tópicos Especiais em Java](#parte-2--tópicos-especiais-em-java)
  - [Parte 3 — Prática: evoluindo o projeto exemplo-oop](#parte-3--prática-evoluindo-o-projeto-exemplo-oop)
  - [Glossário rápido](#glossário-rápido)

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

## Aula 2 — Ambiente de Desenvolvimento: Java na prática + Git na prática

### Materiais

| Arquivo | Conteúdo |
|---|---|
| [aula02-java-git-na-pratica.pdf](<Aula 02/aula02-java-git-na-pratica.pdf>) | Slides da aula |
| [ola-mundo.zip](<Aula 02/ola-mundo.zip>) | Projeto Maven usado na aula (Hello World em Java) |

Backend · Prof. Daniel Plácido · SATC 2026.1

### Objetivo da aula

Nesta aula vamos, na prática:

1. Preparar o ambiente de desenvolvimento (JDK + IntelliJ IDEA).
2. Abrir, executar e explorar o projeto `ola-mundo`, nosso primeiro programa Java.
3. Instalar o Git, criar conta no GitHub e configurar acesso via SSH.
4. Versionar o projeto `ola-mundo` e enviar o primeiro commit para o GitHub.
---

### Parte 1 — Java na prática

#### Pré-requisitos

Antes de começar, confirme que você tem:

- **JDK instalado** — o projeto usa Java na versão 25. Confirme com o comando `java -version` no terminal. Se não tiver, peça ajuda ao professor antes de continuar.
- **IntelliJ IDEA instalado** — pode ser a versão Community (gratuita) ou Ultimate. É o programa (IDE) que usaremos para escrever e rodar código Java.
- **Projeto `ola-mundo` recebido** — o arquivo [`ola-mundo.zip`](<Aula 02/ola-mundo.zip>) deve estar salvo em uma pasta de fácil acesso, por exemplo Documentos ou a Área de Trabalho.
> **Dica:** se algo da lista estiver faltando, avise o professor agora — é mais rápido resolver antes de seguir para os próximos passos.

#### Passo 1 — Extraindo o projeto ola-mundo

- O projeto foi entregue como um arquivo compactado: [`ola-mundo.zip`](<Aula 02/ola-mundo.zip>).
- Clique com o botão direito sobre o arquivo `.zip`.
  - **Windows:** escolha "Extrair tudo..." (*Extract All*).
  - **Mac:** dê duplo clique para descompactar automaticamente.
- Escolha uma pasta de fácil acesso, por exemplo `Documentos`.
- Ao final, você terá uma pasta chamada `ola-mundo` com os arquivos do projeto dentro:
```
Documentos/
└── ola-mundo/
    ├── pom.xml
    ├── .gitignore
    └── src/
        └── main/java/org/example/
            └── Main.java
```

> **Dica:** guarde o caminho completo da pasta `ola-mundo` — vamos precisar dele no próximo passo, dentro do IntelliJ.

#### Passo 2 — Abrindo o IntelliJ IDEA

- Abra o IntelliJ IDEA normalmente, como qualquer outro programa.
- Na tela de boas-vindas, você verá algumas opções:
  - **New Project** — cria um projeto do zero.
  - **Open** — abre um projeto que já existe em uma pasta.
  - **Get from VCS** — clona um projeto de um repositório Git.
- Como o `ola-mundo` já existe na sua pasta, vamos usar a opção **Open**.
#### Passo 3 — Selecionando a pasta do projeto

- Na janela que abrir, navegue até a pasta `ola-mundo` que você extraiu no Passo 1.
- Selecione a pasta `ola-mundo` (a pasta inteira, não um arquivo dentro dela) e clique em **OK**.
- O IntelliJ vai reconhecer automaticamente que é um projeto Maven, por causa do arquivo `pom.xml`.
- Pode aparecer uma janela perguntando se você confia neste projeto (*Trust Project*) — clique em **Trust Project**.
> O que é o `pom.xml`? É o arquivo de configuração do Maven, a ferramenta que organiza as dependências e a forma como o projeto Java é construído. Por enquanto, só saiba que é ele quem diz ao IntelliJ "isto aqui é um projeto Java".

#### Passo 4 — Aguardando a preparação do projeto

- Assim que o projeto abrir, o IntelliJ começa a indexar os arquivos e preparar o Maven.
- Você vai ver barras de progresso na parte inferior da tela, com textos como "Indexing", "Resolving dependencies" e "Downloading...".
- Isso pode levar de alguns segundos a poucos minutos, dependendo da internet.
- Espere terminar antes de seguir — mexer no projeto antes disso pode causar erros temporários.
#### Passo 5 — Conhecendo a estrutura do projeto

```
ola-mundo/
├── pom.xml
├── .gitignore
├── src/
│   └── main/
│       └── java/org/example/
│           └── Main.java
└── target/
    └── (gerado automaticamente)
```

- **`pom.xml`** — arquivo de configuração do Maven: nome do projeto, versão do Java, etc.
- **`src/main/java`** — pasta onde fica todo o código-fonte Java do projeto.
- **`org/example/Main.java`** — a classe principal do nosso programa, onde vamos trabalhar hoje.
- **`target/`** — pasta gerada automaticamente com os arquivos compilados (`.class`). Não editamos nada aqui.
#### Passo 6 — Executando o programa pela 1ª vez

- Abra o arquivo `Main.java` (dois cliques nele, na árvore de arquivos à esquerda).
- Procure o método `main` — é o ponto de partida de todo programa Java.
- Ao lado da linha `public static void main`, existe um triângulo verde (▶).
- Clique nesse triângulo verde e escolha **Run 'Main.main()'**.
- Alternativa pelo teclado: `Shift + F10` (Windows/Linux) ou `Control + R` (Mac).
- Uma aba chamada **Run** vai abrir na parte inferior da tela — é o console do programa.
> **Atenção:** se aparecer um erro de "JDK não configurado", veja a seção de solução de problemas mais abaixo — é a falha mais comum nesse primeiro run.

#### Passo 7 — Interagindo com o console

- O programa vai pedir para você digitar seu nome.
- Clique dentro da área do console para garantir o foco, digite o nome e aperte Enter.
- Em seguida, ele pede o sobrenome — digite e aperte Enter novamente.
- O programa então imprime uma saudação e alguns cálculos automáticos. Exemplo de execução:
```
Digite o seu nome agora:
Ada
Digite o seu sobrenome agora:
Lovelace
Olá Ada Lovelace!
O ano atual é: 2026
O tamanho do seu nome é: 11
Seu nome é longo
Média final: 8
Nota Final: 10
Olá mundo!
```

#### Entendendo o código — leitura e variáveis

Antes de entrar no código, alguns fundamentos de sintaxe:

- Todo programa Java vive dentro de uma **classe** (aqui, `Main`).
- A execução começa pelo método `main` — é o ponto de entrada do programa.
- Cada instrução termina com `;` (ponto e vírgula).
- Chaves `{ }` marcam o início e o fim de um bloco de código (classe, método, `if`, `for`, etc.).

```java
Scanner leitor = new Scanner(System.in);
String nome = leitor.nextLine();
String sobrenome = leitor.nextLine();
int ano = 2026;

System.out.println("Olá " + nome + " " + sobrenome + "!");
```

- **`Scanner`** — classe pronta do Java para ler o que o usuário digita no teclado.
- **`String`** — tipo de dado usado para guardar texto (nome, sobrenome).
- **`int`** — tipo de dado usado para guardar números inteiros (o ano).
- **`+`** — operador usado para juntar (concatenar) textos e variáveis.
#### Entendendo o código — decisão, vetores e repetição

```java
if ((nome.length() + sobrenome.length()) > 5) {
    System.out.println("Seu nome é longo");
} else { ... }

Integer[] notas = new Integer[3];
notas[0] = 10; notas[1] = 8; notas[2] = 6;

for (int i = 0; i < 3; i++) {
    soma = soma + notas[i];
}
```

- **`if / else`** — decide qual bloco de código executar, dependendo de uma condição.
- **Array (vetor)** — `notas` guarda 3 valores na mesma variável, acessados por posição `[0]`, `[1]`, `[2]`.
- **`for`** — repete o bloco enquanto `i` for menor que 3, somando cada nota.
- **`while`** — repete enquanto uma condição continuar verdadeira (usado logo depois no código, para calcular a `notaFinal`).
#### Mão na massa: modifique o código

Depois de rodar o projeto, tente estes três desafios:

- **A — Mude a mensagem final:** troque o texto `"Olá mundo!"` dentro do método `exercicio01` por uma mensagem sua.
- **B — Adicione uma 4ª nota:** aumente o array `notas` para 4 posições, adicione um novo valor e ajuste o `for` e a divisão da média.
- **C — Mude a condição do `if`:** troque o número `5` por outro valor e veja como muda a resposta "nome longo/curto".
> Depois de cada alteração, rode o programa de novo (▶) para ver o resultado. Errar é normal — é assim que se aprende a programar!

#### Problemas comuns ao rodar o projeto

| Problema | Solução |
|---|---|
| "No JDK found" / SDK não configurado | `File → Project Structure → Project` → escolha um JDK instalado (versão 25 ou superior). |
| Botão ▶ verde não aparece | Confirme se abriu o arquivo `Main.java` certo e se o Maven já terminou de indexar (Passo 4). |
| Console não aceita digitação | Clique dentro da aba **Run** antes de digitar, para dar foco ao console. |
| Erro de compilação em vermelho | Releia a linha apontada pelo IntelliJ; normalmente é ponto e vírgula ou parênteses faltando. |

---

### Parte 2 — Git na prática

#### Relembrando: o que é controle de versão?

- Gerenciamento do código-fonte ao longo do tempo.
- Permite contribuições de diferentes programadores no mesmo projeto.
- Permite desfazer alterações problemáticas.
- Ajuda na resolução de conflitos de código.
- O **Git** é o sistema de controle de versão que vamos usar, criado por Linus Torvalds.
O Git organiza o projeto em três áreas:

```
working directory  →  staging area  →  repository
     (git add)              (git commit)
```

#### Passo 1 — Instalando o Git

Acesse o site oficial: **git-scm.com/downloads**

- **Windows:** baixe o instalador, clique em *Next* em todas as telas e finalize (as opções padrão funcionam bem).
- **Mac:** abra o Terminal e rode `git --version` — se não estiver instalado, o próprio Mac oferece para instalar as *Command Line Tools*.
- **Linux (Ubuntu/Debian):** abra o terminal e rode `sudo apt install git`.
#### Passo 2 — Verificando a instalação

Abra um terminal (Windows: *Git Bash*; Mac/Linux: o Terminal padrão do sistema) e rode:

```bash
$ git --version
git version 2.45.0
```

Se aparecer um número de versão, o Git foi instalado com sucesso.

#### Passo 3 — Configurando sua identidade

Antes do primeiro uso, o Git precisa saber quem é você — isso identifica seus commits. Isso **não é login nem senha**, é apenas uma identificação de quem fez a alteração.

```bash
$ git config --global user.name "Ada Lovelace"
$ git config --global user.email "ada@email.com"
```

O `--global` salva essa configuração para todos os projetos da sua máquina.

#### Passo 4 — Criando uma conta no GitHub

- Caso ainda não tenha, acesse **github.com** e clique em **Sign up**.
- Informe e-mail, crie uma senha e escolha um nome de usuário.
- Recomendação: escolha um nome de usuário profissional, pois ele aparecerá no seu portfólio.
- Confirme seu e-mail quando o GitHub solicitar.
- O GitHub é onde vamos hospedar o repositório remoto do projeto `ola-mundo`.
#### Passo 5 — Gerando uma chave SSH

SSH é uma forma segura de autenticar com o GitHub sem digitar senha a cada envio.

```bash
$ ssh-keygen -t ed25519 -C "ada@email.com"
Generating public/private ed25519 key pair.
Enter file in which to save the key: [Enter]
Enter passphrase (empty for no passphrase): [Enter]
Enter same passphrase again: [Enter]
```

Aperte Enter três vezes para aceitar o local padrão e não definir senha adicional. Isso gera duas chaves: uma **privada** (fica só na sua máquina) e uma **pública** (vai para o GitHub).

#### Passo 6 — Cadastrando a chave no GitHub

Copie o conteúdo da chave pública:

```bash
# Windows (Git Bash)
$ cat ~/.ssh/id_ed25519.pub | clip

# Mac
$ pbcopy < ~/.ssh/id_ed25519.pub

# Linux
$ cat ~/.ssh/id_ed25519.pub
```

No GitHub: foto de perfil → **Settings** → **SSH and GPG keys** → **New SSH key**. Dê um título (ex.: "Meu notebook - aula backend") e cole a chave no campo *Key*. Clique em **Add SSH key** para confirmar.

#### Passo 7 — Testando a conexão SSH

```bash
$ ssh -T git@github.com
Hi ada-lovelace! You've successfully authenticated,
but GitHub does not provide shell access.
```

Na primeira vez, o Git pergunta se confia no servidor — digite `yes` e aperte Enter. Se aparecer a mensagem "successfully authenticated" com o seu usuário, está tudo certo!

> **Atenção:** não conseguiu autenticar? Confira se colou a chave pública correta (arquivo `.pub`) no GitHub, sem espaços extras.

#### Passo 8 — Criando um repositório no GitHub

- No GitHub, clique no botão **+** no canto superior direito → **New repository**.
- Repository name: `ola-mundo`.
- Deixe como *Public* ou *Private*, como preferir.
- **NÃO** marque a opção de criar README, `.gitignore` ou licença — nosso projeto já existe localmente.
- Clique em **Create repository**.
- Na próxima tela, copie a URL SSH (algo como `git@github.com:seu-usuario/ola-mundo.git`).
> **Atenção:** marcar aquelas opções criaria arquivos no GitHub que ainda não existem localmente, gerando conflito no primeiro envio.

#### Passo 9 — Iniciando o repositório local

```bash
$ cd Documentos/ola-mundo
$ git init
Initialized empty Git repository in .../ola-mundo/.git/
```

Isso cria uma pasta oculta `.git`, onde o Git guarda todo o histórico do projeto.

#### Passo 10 — Conferindo o `.gitignore` do projeto

O projeto `ola-mundo` já vem com um arquivo `.gitignore` pronto, que diz ao Git quais arquivos e pastas **nunca** devem ser versionados:

```
target/
!.mvn/wrapper/maven-wrapper.jar

### IntelliJ IDEA ###
.idea/modules.xml
.idea/jarRepositories.xml
*.iws
*.iml
```

- **`target/`** — pasta com os arquivos compilados (gerados automaticamente pelo Maven).
- **`.idea/` (parcialmente)** — configurações pessoais do IntelliJ, que não precisam ser compartilhadas.
#### Passo 11 — Adicionando e commitando os arquivos

- **`git add .`** — envia todos os arquivos modificados (exceto os ignorados) para a *staging area*.
- **`git commit -m "mensagem"`** — salva uma versão do projeto no repositório local, com uma mensagem descritiva.
```bash
$ git add .
$ git commit -m "Primeiro commit do projeto ola-mundo"
[main (root-commit) 4f2a1b0] Primeiro commit...
 5 files changed, 78 insertions(+)
```

Use mensagens curtas e claras, como "Primeiro commit do projeto ola-mundo".

#### Passo 12 — Vinculando o remoto e enviando

- **`git remote add origin <url>`** — cria um atalho chamado `origin` apontando para o repositório do GitHub.
- **`git push -u origin main`** — envia os commits locais para o GitHub. O `-u` lembra essa ligação para os próximos `push`.
```bash
$ git remote add origin git@github.com:usuario/ola-mundo.git
$ git push -u origin main
Enumerating objects: 6, done.
...
branch 'main' set up to track 'origin/main'.
```

> **Dica:** se o branch padrão do seu projeto se chamar `master` em vez de `main`, troque no comando de acordo (`git push -u origin master`).

#### Conferindo o resultado no GitHub

- Volte ao navegador e atualize a página do seu repositório `ola-mundo`.
- Você deve ver os arquivos do projeto: `pom.xml`, `.gitignore` e a pasta `src`.
- Clique no commit para conferir a mensagem que você escreveu.
- Parabéns — esse é o seu primeiro projeto versionado e publicado no GitHub!
#### Branches na prática

Um branch é uma linha paralela de desenvolvimento, isolada da principal (`main`). Útil para testar uma alteração sem arriscar o código que já funciona.

```bash
$ git checkout -b nova-feature
Switched to a new branch 'nova-feature'

$ git branch
  main
* nova-feature
```

Depois de testar, volte para o `main` com `git checkout main`.

#### Unindo alterações com merge

O comando `merge` incorpora as modificações de um branch em outro.

```bash
$ git checkout main
$ git merge nova-feature
Updating 4f2a1b0..9c3d2e1
Fast-forward
```

Primeiro, volte para o branch que vai receber as alterações (geralmente `main`); depois, rode `git merge` apontando para o branch de origem.

#### Atualizando e enviando alterações

- **`git fetch`** — atualiza as referências locais do repositório remoto, sem alterar seus arquivos.
- **`git pull`** — atualiza as referências e já aplica (*merge*) as mudanças nos seus arquivos locais.
- **`git push`** — envia seus commits locais para o repositório remoto no GitHub.
#### O fluxo completo, do zero ao GitHub

```
Working Directory  →  Staging Area  →  Local Repository  →  Remote Repository (GitHub)
        git add             git commit              git push
```

#### Cheat-sheet: comandos Git desta aula

| Comando | O que faz |
|---|---|
| `git init` | Inicia um repositório Git na pasta atual |
| `git config --global user.name/user.email` | Define sua identidade nos commits |
| `git add .` | Move alterações para a staging area |
| `git commit -m "mensagem"` | Salva uma versão no repositório local |
| `git remote add origin <url>` | Vincula o repositório local a um remoto |
| `git push -u origin main` | Envia commits locais para o GitHub |
| `git fetch` | Atualiza referências do remoto, sem alterar arquivos |
| `git pull` | Atualiza referências e aplica as mudanças localmente |
| `git checkout -b <nome>` | Cria e muda para um novo branch |
| `git merge <branch>` | Incorpora as alterações de outro branch |

---

### O que aprendemos hoje

- Abrir e executar um projeto Java real no IntelliJ IDEA.
- Ler e entender um código com variáveis, decisão, vetores e repetição.
- Instalar o Git e configurar acesso seguro via SSH ao GitHub.
- Fazer o primeiro commit e enviar um projeto para um repositório remoto.
- Trabalhar com branches, merge e sincronização (`fetch`/`pull`/`push`).
**Próxima aula:** Orientação a Objetos I.

---

## Aula 3 — Orientação a Objetos em Java + Tópicos Especiais

### Materiais

| Arquivo | Conteúdo |
|---|---|
| [aula03-oop-topicos-especiais.pdf](<Aula 03/aula03-oop-topicos-especiais.pdf>) | Slides da aula (31 slides) |
| [exemplo-oop.zip](<Aula 03/exemplo-oop.zip>) | Projeto base usado nos exercícios (`Animal`, `Cachorro`, `Gato`, `Robo`, `Rastreavel`, `Main`) |

Backend · Prof. Daniel Plácido · SATC 2026.1

### Objetivo da aula

Nesta aula vamos:

1. Revisar os pilares da Orientação a Objetos (herança, encapsulamento, interfaces, polimorfismo), já conectados ao projeto `exemplo-oop`.
2. Entender tópicos especiais de Java: a classe `Object`, construtores, o Garbage Collector, Collections e tratamento de exceções.
3. Colocar tudo em prática evoluindo o projeto `exemplo-oop` com 5 exercícios progressivos, do mais simples ao desafio final.

---

### Parte 1 — Orientação a Objetos em Java

#### Por que existe a Orientação a Objetos?

Sem OOP, um programa vira uma pilha de variáveis soltas e funções separadas — difícil de saber o que pertence a quê.

```java
// ❌ Sem OOP
String nomeCachorro;
String racaCachorro;
String nomeGato;
String racaGato;
// ... tudo solto e sem relação clara
```

```java
// ✅ Com OOP
class Animal {
  String nome;
  String raca;
  // atributos e métodos juntos, organizados num único lugar
}
```

A OOP agrupa dados e comportamentos relacionados dentro de uma classe. Isso facilita reaproveitar código (herança), proteger dados (encapsulamento) e organizar sistemas grandes em pedaços menores e compreensíveis.

#### O que é Orientação a Objetos?

- **OOP** (Object Oriented Programming) organiza o código em torno de **objetos**, não de funções soltas.
- Cada objeto representa algo do mundo real (ou um conceito abstrato) com **dados** (atributos) e **comportamentos** (métodos).
- A **classe** é o "molde"; o **objeto** é a coisa concreta criada a partir dela — cada objeto criado é uma **instância** da classe.
- Exemplo clássico: a classe `Cachorro` descreve *cor*, *raça* e *peso* (atributos) e *correr*, *comer*, *latir* (métodos).
- Cada instância guarda seus próprios valores: o Cachorro "Paçoca" e o Cachorro "Galactus" compartilham a mesma estrutura, mas têm dados diferentes.

#### Classes, atributos, métodos e `this`

- **Atributos** = variáveis dentro da classe. **Métodos** = funções dentro da classe.
- `new NomeDaClasse()` cria um novo objeto (instância) daquela classe.
- `this` refere-se ao próprio objeto dentro de um método — útil quando o parâmetro tem o mesmo nome do atributo.

```java
public class Animal {
    private String nome;

    public void setNome(String nome) {
        // this.nome = atributo da classe
        // nome      = parâmetro recebido
        this.nome = nome;
    }
}
```

#### Herança — `extends`

- Uma classe pode herdar atributos e métodos de outra usando `extends`.
- No projeto: `Cachorro` e `Gato` herdam tudo de `Animal` (nome, raça, dono, posição, `emitirSom`...).
- `super(...)` chama o construtor da superclasse — é assim que a subclasse aproveita a inicialização que o pai já sabe fazer, sem reescrevê-la.
- Fora de um construtor, `super.metodo()` chama a versão do método implementada no pai, mesmo que você tenha sobrescrito esse método na subclasse.

```java
public class Animal {
    private String nome;
    public Animal(String nome) { this.nome = nome; }
}
public class Cachorro extends Animal {
    public Cachorro(String nome) { super(nome); }
}
```

#### Encapsulamento e modificadores de acesso

- **Encapsulamento** = esconder atributos com `private` e liberar acesso controlado via getters e setters.
- `private`: só a própria classe acessa.
- `protected`: acessa dentro do mesmo *package* (uma "pasta" que agrupa classes relacionadas).
- `public`: acessa de qualquer lugar.
- Por que fazer isso? Porque assim a classe controla suas próprias regras — ninguém de fora consegue colocar um nome vazio ou um valor inválido diretamente.

```java
public class Animal {
    private String nome;

    public String getNome() { return nome; }

    public void setNome(String nome) {
        if (nome.isEmpty()) return;
        this.nome = nome;
    }
}
```

#### Getters e Setters (convenção)

- **Getter** = método que só devolve o valor de um atributo privado. **Setter** = método que só altera esse valor (podendo validar antes).
- Convenção de nomes: `getNomeDoAtributo()` e `setNomeDoAtributo(valor)`.
- Exceção da convenção: atributos `boolean` geralmente usam `isNomeDoAtributo()` no lugar de `getNomeDoAtributo()` (ex.: `isAtivo()` em vez de `getAtivo()`).
- Isso não é regra do compilador — é convenção da comunidade Java, mas frameworks e outros programadores esperam esse padrão.

```java
public class Funcionario {
    private String nome;
    private double salario;
    private boolean ativo;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public void setSalario(double salario) {
        if (salario < 0) return;
        this.salario = salario;
    }

    // boolean usa "is", não "get"
    public boolean isAtivo() { return ativo; }
}
```

#### Interfaces e Polimorfismo

- **Interface** define métodos que quem a implementa é obrigado a escrever (`implements`). É como um contrato: "toda classe que assinar este contrato precisa ter estes métodos".
- `Rastreavel` é uma interface com o método `rastrear()`.
- `Animal` (e suas subclasses) e `Robo` implementam `Rastreavel` — mesmo sendo bem diferentes entre si.
- **Polimorfismo**: uma lista de `Rastreavel` pode guardar `Cachorro`, `Gato` e `Robo` ao mesmo tempo, e cada um responde `rastrear()` do seu jeito.

> 🎮 **Analogia:** um controle remoto de TV e um de ar-condicionado são bem diferentes por dentro, mas os dois têm um botão "ligar". Polimorfismo é isso: comportamentos diferentes por trás do mesmo "botão" (método).

```java
public interface Rastreavel {
    public String rastrear();
}

public class Robo implements Rastreavel {
    private String posicao;
    public String rastrear() {
        return posicao;
    }
}
```

#### Generics — o que significam os `<>`

- Coisas como `ArrayList<Rastreavel>` ou `HashMap<String, Rastreavel>` usam `<>` para dizer qual tipo de dado vai dentro daquela estrutura.
- É como rotular uma caixa: "essa caixa só guarda `Animal`" evita que alguém guarde uma `String` ali dentro por engano.
- Sem generics, o Java deixaria colocar qualquer coisa lá dentro, e o erro só apareceria em tempo de execução — bem mais difícil de achar.

```java
List<String> nomes = new ArrayList<>();
nomes.add("Galactus");
// nomes.add(123); ❌ não compila!

Map<String, Animal> animais = new HashMap<>();
animais.put("Galactus", galactus);
Animal a = animais.get("Galactus"); // já vem como Animal, sem cast
```

#### Recapitulando: Classes x Classes Abstratas x Interfaces

| Estrutura | `new` | Métodos | Atributos | Herança | Palavra-chave |
|---|---|---|---|---|---|
| Classes regulares | Sim | Sim | Sim | Sim | `class` |
| Classes abstratas | Não | Sim | Sim | Sim | `abstract class` |
| Interfaces | Não | Sim | Não | Não | `interface` |

> 💡 Interface = 100% abstrata (nenhum método pronto). Classe abstrata pode misturar métodos prontos e métodos abstratos.

---

### Parte 2 — Tópicos Especiais em Java

#### A classe `Object`

- Toda classe em Java herda — de forma implícita, mesmo sem escrever `extends` — da classe `Object`.
- `toString()`: sem sobrescrever, o padrão é algo pouco legível como `Animal@1a2b3c`. Sobrescrevendo (com `@Override`), você decide o que aparece no console.
- `equals()`: por padrão, compara se é o mesmo objeto na memória. Sobrescrevendo, você decide o que conta como "igual" (ex.: mesmo nome).
- **Regra de ouro:** se você sobrescrever `equals()`, sobrescreva `hashCode()` também — `HashMap` e `HashSet` usam os dois por trás dos panos para funcionar direito.

```java
public class Animal {
    private String nome;
    private String raca;

    @Override
    public String toString() {
        return nome + " (" + raca + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Animal)) return false;
        Animal outro = (Animal) obj;
        return this.nome.equals(outro.nome);
    }
}
// System.out.println(galactus);
// Galactus (Pincher)  ✅ não mais Animal@1a2b3c
```

#### Construtores

- Construtor = método especial chamado no momento do `new`, com o mesmo nome da classe e sem tipo de retorno.
- Se você não escrever nenhum, o Java gera um construtor padrão vazio (**NoArgsConstructor**). Ao escrever qualquer construtor, esse padrão automático deixa de existir.
- Assim como métodos, construtores podem ser **sobrecarregados**: a mesma classe pode ter várias versões de construtor, cada uma com parâmetros diferentes.
- `this(...)` dentro de um construtor chama outro construtor da mesma classe — útil para não repetir lógica de inicialização.

```java
public class Pessoa {
    private String nome;
    private int idade;

    // construtor sem argumentos
    public Pessoa() {
        this("Sem nome", 0);
    }

    // construtor com todos os argumentos
    public Pessoa(String nome, int idade) {
        this.nome = nome;
        this.idade = idade;
    }
}

Pessoa p1 = new Pessoa();
Pessoa p2 = new Pessoa("Ana", 30);
```

#### ⚠️ Java não tem "destrutor" como C++

A sintaxe `~NomeClasse()` é de C++ — **não existe em Java**. Se aparecer em algum material antigo, é um erro de cópia.

- Em Java, a memória é gerenciada pelo **Garbage Collector (GC)** automaticamente.
- Existe o método `finalize()` (herdado de `Object`), mas é *deprecated* — não deve ser usado.
- Na prática: você não escreve código para "destruir" um objeto em Java. Só deixa de referenciá-lo.

#### O que é o Garbage Collector?

- Toda vez que você faz `new Animal()`, o Java reserva um espacinho de memória (RAM) para guardar aquele objeto.
- Uma variável (como `galactus`) não é o objeto — ela é uma **referência**, um "post-it" que aponta para onde o objeto está guardado na memória.
- Quando nenhuma variável aponta mais para um objeto, aquele objeto vira "lixo" — ninguém mais consegue acessá-lo.
- O **Garbage Collector** ("coletor de lixo") é uma parte do Java que roda em segundo plano, encontra esses objetos sem dono e libera a memória automaticamente.
- É por isso que você nunca precisa (e nem consegue) escrever código para "destruir" um objeto — diferente de linguagens como C++.

```java
Animal galactus = new Animal();
// ... uso normal do objeto ...
galactus = null;   // ninguém mais aponta pra cá
// 🗑 o Garbage Collector recolhe a memória, em algum momento depois
```

#### Generics já explicado acima — Collections

Guardando vários objetos: por que não usar só um array? Porque array tem tamanho fixo — depois de criado, não dá para adicionar ou remover posições. *Collections* resolvem isso.

| Estrutura | O que garante | Implementação comum |
|---|---|---|
| `List` | Ordenada, aceita duplicados | `ArrayList`, `LinkedList` |
| `Set` | Sem elementos duplicados | `HashSet` |
| `Queue` | Fila — organiza por ordem de processamento | `PriorityQueue` |
| `Map` | Chave → valor (não é Collection, mas tratado junto) | `HashMap` |

#### Usando uma `List` de verdade

- `ArrayList` é a implementação de `List` mais usada no dia a dia — funciona como uma "gaveta" que cresce sob demanda.
- `add()` insere, `get(indice)` busca pela posição, `contains()` verifica se existe, `remove()` tira um elemento, `size()` diz quantos existem.
- O for-each (`for (Tipo item : lista)`) percorre todos os elementos sem precisar controlar índice manualmente.

```java
List<String> nomes = new ArrayList<>();
nomes.add("Galactus");
nomes.add("Fumaça");
nomes.add("R2D2");

System.out.println(nomes.contains("Fumaça"));
// true

nomes.remove("R2D2");
System.out.println(nomes.size());
// 2

for (String nome : nomes) {
    System.out.println(nome);
}
// Galactus
// Fumaça
```

#### O que é uma exceção?

- Exceção é um "aviso de erro" que o Java lança quando algo impede o código de continuar normalmente (ex.: dividir por zero, acessar posição que não existe).
- Se ninguém tratar a exceção, o programa **para imediatamente** e imprime um *stack trace* (o "rastro" de onde o erro aconteceu).
- **Checked**: verificadas em tempo de compilação — o próprio compilador obriga a tratar ou declarar com `throws` (ex.: `IOException`, ao mexer com arquivos).
- **Unchecked**: só aparecem em tempo de execução, geralmente por bugs de lógica (ex.: `ArrayIndexOutOfBoundsException`).

```java
int[] numeros = {1, 2, 3};
System.out.println(numeros[5]);

// 💥 Programa para e imprime:
// Exception in thread "main"
// java.lang.ArrayIndexOutOfBoundsException:
//   Index 5 out of bounds for length 3
//   at Main.main(Main.java:2)
```

#### Tratando exceções: `throw` e `try-catch-finally`

- `throw` dispara uma exceção manualmente quando uma regra do seu próprio código é quebrada.
- `try/catch` captura o erro e evita que o programa pare de rodar — o que está dentro do `catch` só roda se der erro no `try`.
- `finally` roda **sempre** — deu erro ou não. É o lugar certo para liberar recursos (fechar um arquivo, uma conexão, etc.).
- `getMessage()` devolve o texto que você passou ao criar a exceção.

```java
public int dividir(int a, int b) {
    if (b == 0) {
        throw new ArithmeticException("Impossível dividir por zero!");
    }
    return a / b;
}

try {
    dividir(10, 0);
} catch (ArithmeticException e) {
    System.out.println(e.getMessage());
} finally {
    System.out.println("Fim da tentativa.");
}
```

---

### Parte 3 — Prática: evoluindo o projeto exemplo-oop

#### Conhecendo o projeto

```
Rastreavel «interface»
   ├── Animal
   │      ├── Cachorro
   │      └── Gato
   └── Robo
```

- `Animal` implementa `Rastreavel` e tem `nome`, `raça`, `dono`, `latitude`/`longitude` (com getters/setters).
- `Cachorro` e `Gato` herdam de `Animal` (`extends`) e ainda não adicionam nada de próprio.
- `Robo` implementa `Rastreavel` de forma independente (`nome`, `posição`).
- `Main.java` já monta uma `ArrayList<Rastreavel>` com tipos diferentes — polimorfismo em ação.

#### Exercício 1 — Animal vira classe abstrata

**Conceitos:** Abstração + Polimorfismo obrigatório
**Objetivo:** impedir que `Animal` seja instanciado direto e obrigar cada subclasse a definir seu próprio som.

- Adicione `abstract` em `class Animal` e remova o corpo de `emitirSom()`, deixando-o `abstract`.
- Em `Cachorro`, implemente `emitirSom()` imprimindo `"Au au!"`. Em `Gato`, `"Miau!"`.
- Tente descomentar `new Animal()` no `Main` — o compilador deve recusar.
- Rode o `Main` e confirme: cada subtipo emite seu próprio som ao percorrer a lista.

#### Exercício 2 — Exceção personalizada

**Conceitos:** Classe de exceção própria + `throws`
**Objetivo:** criar sua própria exceção e validar coordenadas reais antes de salvar a posição.

- Crie `PosicaoInvalidaException extends Exception` (construtor recebe a mensagem).
- Em `setPosicao(int lat, int lon)`, valide: latitude entre -90 e 90, longitude entre -180 e 180.
- Se inválido, lance (`throw`) a sua exceção com uma mensagem clara.
- No `Main`, chame `setPosicao` dentro de um `try/catch` e trate o erro sem derrubar o programa.

#### Exercício 3 — De `List` para `Map`

**Conceitos:** Collections: `HashMap` + busca por chave
**Objetivo:** trocar a estrutura de dados para permitir localizar um rastreável instantaneamente pelo nome.

- No `Main`, troque `ArrayList<Rastreavel>` por `HashMap<String, Rastreavel>`, usando o nome como chave.
- Crie um método `buscarPorNome(String nome)` que devolve o `Rastreavel` correspondente (ou `null`).
- Busque um animal específico pelo nome e imprima o resultado de `rastrear()`.
- Discussão: por que buscar num `Map` é mais rápido do que percorrer uma `List` inteira?

#### Exercício 4 — Ordenando com Comparable

**Conceitos:** Interface `Comparable` + `Collections.sort`
**Objetivo:** deixar os animais em ordem alfabética de nome antes de exibi-los.

- Faça `Animal` implementar `Comparable<Animal>`.
- Implemente `compareTo(Animal outro)` usando `this.getNome().compareTo(outro.getNome())`.
- Monte uma `List<Animal>` com vários animais fora de ordem.
- Chame `Collections.sort(lista)` e imprima — deve sair em ordem alfabética.

#### Exercício 5 — Desafio final: `CentralDeRastreamento`

**Conceitos:** Integrando abstração, exceções e Collections
**Objetivo:** construir uma classe de serviço que junta tudo: cadastro sem duplicidade, exceção própria e relatório ordenado.

- Crie `CentralDeRastreamento` com um `HashMap<String, Rastreavel>` interno (privado).
- Método `cadastrar(String nome, Rastreavel r)`: lança `NomeDuplicadoException` se o nome já existir.
- Método `relatorio()`: ordena os nomes (`Collections.sort` numa `List<String>`) e imprime nome + `rastrear()` de cada um.
- No `Main`, cadastre `Cachorro`, `Gato`, `Robo` e `Passaro` tratando a exceção com `try/catch`, depois chame `relatorio()`.

---

### Glossário rápido

| Termo | Significado |
|---|---|
| **Classe** | Molde que descreve os atributos e métodos de um tipo de objeto. |
| **Objeto / Instância** | A "coisa" concreta criada a partir de uma classe (via `new`), com valores próprios. |
| **Atributo** | Uma variável que vive dentro de uma classe — um dado do objeto. |
| **Método** | Uma função que vive dentro de uma classe — um comportamento do objeto. |
| **`this`** | Dentro de um método, refere-se ao próprio objeto que está sendo usado. |
| **Herança (`extends`)** | Uma classe reaproveita atributos e métodos de outra, especializando-a. |
| **Encapsulamento** | Esconder atributos (`private`) e liberar acesso controlado via getters/setters. |
| **Interface (`implements`)** | Um "contrato" de métodos que a classe é obrigada a implementar. |
| **Classe abstrata** | Não pode ser instanciada (sem `new`) — serve de base para outras classes. |
| **Polimorfismo** | O mesmo método se comporta de forma diferente dependendo do objeto que o chama. |
| **`Object`** | Classe da qual toda classe Java herda implicitamente (dá `toString`, `equals`...). |
| **Construtor** | Método especial chamado no momento do `new`, usado para inicializar o objeto. |
| **Garbage Collector (GC)** | Mecanismo do Java que libera automaticamente a memória de objetos sem referência. |
| **Generics (`<>`)** | Indicam qual tipo de dado uma estrutura (`List`, `Map`...) vai guardar. |
| **Collection** | Estrutura para guardar vários objetos: `List`, `Set`, `Queue` (e `Map`, tratado junto). |
| **Exceção** | Aviso de erro lançado quando algo impede a execução normal do programa. |
| **Checked / Unchecked** | Exceções verificadas em tempo de compilação vs. em tempo de execução. |
| **`try` / `catch` / `finally`** | Blocos que capturam, tratam e sempre executam (finally) ao redor de código que pode falhar. |

**Próxima aula:** Avaliação N1, cobrindo o conteúdo desta aula.
