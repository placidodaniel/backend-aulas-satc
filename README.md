

Readme · MD
# Tópico 02 — Arquitetura da Web
 
**Disciplina:** Backend — Engenharia de Software
**Professor:** Daniel Plácido
**SATC 2026.1**
 
Material de apoio para a aula sobre Arquitetura da Web: os pilares técnicos (URI, HTTP, DNS e TLS) que sustentam toda a comunicação na internet.
 
---
 
## Objetivos da aula
 
- Entender os pilares da Web
- Compreender o modelo cliente-servidor
- Ver como funciona uma requisição HTTP
- Entender por que usamos TLS (HTTPS)
- Diferenciar servidor Web e servidor de aplicação
---
 
## O que é a arquitetura da Web?
 
Conjunto de tecnologias e padrões que fazem a Web funcionar, permitindo comunicação entre pessoas, sistemas e serviços no mundo todo. É baseada em protocolos de rede e em documentos interligados.
 
**Analogia:** pense nos Correios. Para uma carta chegar ao destino certo, existe um sistema por trás — endereço padronizado, agências, veículos, regras de embalagem. A arquitetura da Web é isso, só que para informação digital.
 
### Fundamentos
 
A Web foi projetada para ser:
 
| Qualidade | O que significa |
|---|---|
| **Simples** | Fácil de implementar e entender — qualquer computador pode participar sem depender de tecnologia exclusiva |
| **Universal** | Funciona em qualquer dispositivo, sistema operacional ou localização geográfica |
| **Escalável** | Suporta desde um blog pessoal até bilhões de acessos simultâneos |
 
Isso é possível graças a quatro recursos técnicos: **URI, HTTP, DNS e TLS**.
 
---
 
## Modelo Cliente-Servidor
 
Base de toda a arquitetura da Web.
 
- **Cliente:** faz a requisição — normalmente o navegador. Cada recurso pedido é identificado por um URI.
- **Servidor:** recebe a requisição, processa e responde.
- Comunicação usando **HTTP sobre TCP/IP**.
---
 
## URI (Uniform Resource Identifier)
 
Forma padrão de identificar um recurso na Web: diz **onde** ele está e **como** acessá-lo. Uma URL é o tipo de URI mais comum.
 
```
https://exemplo.com/artigos/123?ref=homepage
```
 
| Parte | Nome | Função |
|---|---|---|
| `https://` | Esquema | Define o protocolo usado para acessar o recurso |
| `exemplo.com` | Domínio | Identifica qual servidor deve receber o pedido |
| `/artigos/123` | Caminho | Indica qual recurso específico está sendo solicitado |
| `?ref=homepage` | Query / parâmetros | Informações extras enviadas junto do pedido |
 
---
 
## DNS (Domain Name System)
 
A "lista telefônica" da Internet. Traduz nomes fáceis de lembrar em endereços IP.
 
```
exemplo.com → 2.2.2.2
```
 
Sem DNS, teríamos que memorizar apenas números. É usado sempre que acessamos um site por nome — antes mesmo da requisição HTTP ser enviada.
 
---
 
## HTTP (HyperText Transfer Protocol)
 
Protocolo que define como cliente e servidor conversam, usando o modelo **requisição → resposta**.
 
Exemplo simples de requisição:
 
```http
GET /index.html HTTP/1.1
Host: exemplo.com
```
 
### Métodos HTTP
 
| Método | Intenção | Exemplo |
|---|---|---|
| `GET` | Buscar/ler um recurso | `GET /produtos/42` → devolve os dados do produto 42 |
| `POST` | Criar um novo recurso | `POST /produtos` → cria um novo produto |
| `PUT` | Atualizar um recurso existente | `PUT /produtos/42` → substitui os dados do produto 42 |
| `DELETE` | Remover um recurso | `DELETE /produtos/42` → remove o produto 42 |
 
### Códigos de status
 
| Faixa | Categoria | Exemplo |
|---|---|---|
| `2xx` | Sucesso | `200 OK` — o pedido funcionou como esperado |
| `3xx` | Redirecionamento | `301 Moved` — o recurso foi movido de endereço |
| `4xx` | Erro do cliente | `404 Not Found` — o recurso pedido não existe |
| `5xx` | Erro do servidor | `500 Internal Server Error` — algo quebrou no servidor |
 
### Exemplo real de resposta HTTP
 
```http
HTTP/1.1 200 OK
Content-Type: text/html
 
<html>
  <body><h1>Olá, Mundo!</h1></body>
</html>
```
 
- `200 OK`: o status, indicando sucesso.
- `Content-Type: text/html`: avisa ao navegador como interpretar o conteúdo.
- O corpo (`<html>...</html>`): o conteúdo que o navegador transforma na página visível.
### Conexões persistentes (keep-alive)
 
No HTTP/1.1, a mesma conexão TCP pode atender várias requisições, reduzindo latência e o custo de abrir novas conexões.
 
```http
GET /index.html HTTP/1.1
Host: exemplo.com
Connection: keep-alive
```
 
---
 
## TLS (Transport Layer Security)
 
Protocolo que adiciona segurança à conexão. Garante:
 
- **Confidencialidade** — dados criptografados
- **Integridade** — detecção de alterações no trajeto
- **Autenticidade** — confirma com quem estamos falando (via certificado digital)
Quando combinado com HTTP, temos **HTTPS** (`https://`).
 
### Handshake TLS (visão geral)
 
1. Cliente envia informações iniciais (algoritmos suportados, número aleatório)
2. Servidor responde com seu número aleatório, certificado digital e algoritmo escolhido
3. Cliente verifica o certificado
4. Cliente e servidor geram uma chave de sessão e passam a criptografar a comunicação
Usado em: login, transações financeiras, dados sensíveis em geral.
 
---
 
## Fluxo completo de uma requisição
 
1. O cliente recebe ou digita um URI
2. O domínio é resolvido via DNS → IP do servidor
3. Cliente abre conexão TCP com o servidor (porta 80 ou 443)
4. Opcionalmente, é feito o handshake TLS (para HTTPS)
5. Cliente envia a requisição HTTP
6. Servidor processa a requisição
7. Servidor envia a resposta HTTP com status e conteúdo
8. Cliente recebe a resposta e renderiza a página
9. Conexão pode ser fechada ou mantida aberta
10. Novas requisições podem seguir o mesmo fluxo
---
 
## Mão na massa: vendo o HTTP funcionar de verdade
 
### Opção 1 — Terminal (curl)
 
```bash
curl -v https://exemplo.com
```
 
O parâmetro `-v` ("verbose") mostra na tela todo o processo: a resolução DNS, o handshake TLS, a requisição enviada e a resposta completa recebida.
 
### Opção 2 — DevTools do navegador
 
1. Abra qualquer site e pressione `F12` (ou botão direito → Inspecionar)
2. Clique na aba **Network** (Rede) e recarregue a página
3. Clique em qualquer requisição listada: você verá o método, os headers, o status code e o corpo da resposta
---
 
## Infraestrutura
 
A Web é composta por uma infraestrutura global de servidores, data centers, redes e serviços.
 
| Componente | Função |
|---|---|
| **Data centers** | Prédios especializados, com múltiplos servidores, energia redundante e conexões de alta velocidade |
| **Load balancer** | Distribui as requisições entre vários servidores |
| **Cache / CDN** | Guarda cópias de conteúdo mais próximas do usuário final |
| **Microsserviços / cluster de banco de dados** | Partes menores e independentes do sistema, com o banco replicado em vários servidores |
 
---
 
## Servidor Web x Servidor de Aplicação
 
| | Servidor Web | Servidor de Aplicação |
|---|---|---|
| **O que faz** | Processa requisições HTTP e entrega arquivos estáticos (HTML, CSS, JS, imagens) ou respostas geradas por aplicações | Executa aplicações web dinâmicas, focado na lógica de negócio |
| **Componentes típicos** | Processamento de requisições, gerenciamento de recursos, segurança (autenticação/autorização), logs | Ponte entre requisições HTTP, código da aplicação e banco de dados/outros serviços |
| **Exemplos** | Apache, Nginx, IIS | APIs REST, aplicações corporativas, servidores de jogos |
 
---
 
## Frameworks
 
| Stack | Como funciona |
|---|---|
| **Java / Spring Boot** ⭐ (usado nesta disciplina) | Já traz um servidor de aplicação embutido (Tomcat) |
| **Python / Django, Flask** | Executados em servidores WSGI/ASGI (Werkzeug, Gunicorn, Uvicorn) |
| **Node.js / Express.js** | O próprio Node.js atua como servidor de aplicação |
 
---
 
## Resumo da aula
 
- URI identifica o recurso; DNS traduz o nome em IP; HTTP é a língua da conversa; TLS garante segurança.
- O fluxo completo de uma requisição envolve DNS → TCP → (TLS) → requisição HTTP → resposta HTTP.
- Métodos HTTP (GET/POST/PUT/DELETE) expressam a intenção; status codes (2xx-5xx) dizem o que aconteceu.
- Servidor Web entrega conteúdo; Servidor de Aplicação roda a lógica de negócio — é onde o código da disciplina vai viver.
**Próxima aula:** Ambiente de Desenvolvimento — instalação e configuração de JDK, IDE e Git.
 

