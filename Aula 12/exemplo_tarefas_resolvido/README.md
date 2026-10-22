# Exemplo de tarefas: Aula 12 (RESOLVIDO)

> **Gabarito.** Esta pasta é o projeto `exemplo_tarefas` da Aula 12 com os
> **Exercícios 1 a 5** do [`EXERCICIOS.md`](EXERCICIOS.md) resolvidos. As
> respostas dos subexercícios estão em [`RESPOSTAS.md`](RESPOSTAS.md). A página
> `contrato.html` deste projeto fica toda ✅ (18 de 18).
>
> O banco deste projeto usa a porta **5440** e o container
> `aula12-tarefas-resolvido-postgres`, para não misturar com o banco do projeto
> dos alunos (porta 5439). Rode os dois ao mesmo tempo só se trocar a porta da
> API (`--server.port=8081`), pois ambos usam a 8080.

| Exercício | Onde está a solução |
|---|---|
| 1. O tempo de agora | `OpenMeteoAgoraDTO`, `OpenMeteoClient.buscarAgora()`, `AgoraDTO`, `PrevisaoMapper.toAgora()`, `PrevisaoService.agora()`, `GET /previsao/agora` |
| 2. Previsão para o prazo da tarefa | `TarefaComPrevisaoDTO`, `PrevisaoService.temPrevisao()` e `validarCoordenadas()` público, `TarefaService.buscarComPrevisao()` (aviso quando não há previsão), `GET /tarefas/{id}/previsao` |
| 3. Tempo esgotado vira 504 | `PrevisaoTempoEsgotadoException` (filha da `PrevisaoIndisponivelException`), `OpenMeteoClient.indisponivel()` (`HttpTimeoutException`), tratador de 504 no `ApiExceptionHandler`, `@ApiResponse` 504 |
| 4. Cache de 30 minutos | `PrevisaoDTO.origem` e `comOrigem()`, cache com `ConcurrentHashMap` no `PrevisaoService.buscar()` |
| 5. Tarefas em dias de chuva | `TarefaRepository.findByConcluidaFalseAndDataPrazoBetweenOrderByDataPrazoAsc()`, `PrevisaoService.buscarJanela()` (uma chamada para 16 dias), `TarefaService.listarEmDiasDeChuva()`, `GET /tarefas/chuvosas` |

## Objetivo

Até a Aula 9, a nossa API só **respondia**: alguém chamava, ela consultava o PostgreSQL e devolvia JSON. Nesta aula ela também **pergunta**: no meio de uma requisição, chama outra API pela internet e usa a resposta.

A outra API é o [Open-Meteo](https://open-meteo.com), um serviço gratuito de previsão do tempo. No cadastro de tarefas, ao escolher a data do prazo, a página pega a localização do navegador e pede à **nossa API** a previsão daquele dia. Quem consulta o Open-Meteo é a nossa API, não o navegador.

```text
Navegador ──GET /previsao──> NOSSA API ──GET /v1/forecast──> Open-Meteo
 (latitude, longitude, data)    │
                                └──SQL──> PostgreSQL (as tarefas, como antes)
```

Este projeto é o `exemplo_tarefas_resolvido` da Aula 09, com a integração já funcionando. Os enunciados estão em [`EXERCICIOS.md`](EXERCICIOS.md).

## O que é novo nesta aula

| O quê | Onde |
|---|---|
| **O cliente do Open-Meteo.** A única classe que sabe o endereço, o caminho (`/v1/forecast`), os parâmetros e o formato do JSON de lá. Faz a requisição com o `RestClient` do Spring | `client/OpenMeteoClient.java` |
| **O contrato do Open-Meteo**, copiado do jeito que ele é (em inglês, com `_`, e com uma lista por medida) | `client/OpenMeteoRespostaDTO.java` |
| **Falha do Open-Meteo** vira `502 Bad Gateway` (quem falhou não fomos nós, foi o serviço que chamamos) | `client/PrevisaoIndisponivelException.java`, `controller/ApiExceptionHandler.java` |
| **O tradutor** entre o Open-Meteo e a nossa API (`weather_code: 95` vira `"Trovoada"` e `⛈️`) | `mapper/PrevisaoMapper.java` |
| **As regras** (R1 a R3) | `service/PrevisaoService.java`, `service/RegraDeNegocioException.java` |
| **A rota** `GET /previsao` | `controller/PrevisaoController.java`, `dto/PrevisaoDTO.java` |
| **Parâmetro da URL ausente ou inválido** também sai como `ErroDTO` | `controller/ApiExceptionHandler.java` |
| **Endereço e timeouts** do Open-Meteo | `application.properties` (`previsao.url`, `spring.http.client.*`) |
| **A previsão na tela**, embaixo do campo de data | `static/index.html` |

Nenhuma dependência nova no `pom.xml` (o `RestClient` já vem no `spring-boot-starter-web`) e nenhuma migration nova (a previsão não é guardada no banco).

### A rota nova

`GET /previsao?latitude=-28.6775&longitude=-49.3697&data=2026-10-24`

```json
{
  "data": "2026-10-24",
  "descricao": "Trovoada",
  "icone": "⛈️",
  "temperaturaMinima": 17.0,
  "temperaturaMaxima": 28.0,
  "chanceDeChuva": 75,
  "ventoMaximo": 15.9,
  "latitude": -28.64675,
  "longitude": -49.36145
}
```

### As regras (violar qualquer uma = 400, e o Open-Meteo nem é chamado)

| Regra | Por quê |
|---|---|
| **R1.** Latitude entre -90 e 90; longitude entre -180 e 180 | Fora disso não é um lugar do planeta |
| **R2.** A previsão é para hoje em diante | Previsão é sobre o futuro |
| **R3.** Só até 15 dias depois de hoje | O Open-Meteo prevê 16 dias, contando hoje |

## Pré-requisitos

- Docker Desktop instalado e aberto.
- JDK 25 instalado e disponível no `PATH`.
- **Internet**: a API consulta o Open-Meteo de verdade.

Os comandos abaixo foram escritos para o laboratório, em Windows com PowerShell. No Linux ou Mac, troque `.\mvnw.cmd` por `./mvnw`.

## 1. Subir o PostgreSQL

```powershell
cd "C:\caminho\do\repositorio\Aula 12\exemplo_tarefas_resolvido"
docker compose up -d
docker compose ps
```

O serviço deve aparecer como `healthy`. O banco do gabarito usa a porta **5440** (usuário, senha e banco: `tarefas`), separado dos bancos das aulas anteriores.

## 2. Executar a aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

A API sobe mesmo sem internet: a primeira chamada ao Open-Meteo só acontece quando alguém chama `/previsao`.

## 3. Ver a previsão no cadastro

Abra <http://localhost:8080/> e escolha uma data no campo do prazo. Na primeira vez, o navegador pergunta se a página pode saber a sua localização: **permita**. A previsão daquele dia aparece logo abaixo do campo, e o log da direita mostra o caminho todo: Controller, Service, a chamada ao Open-Meteo e o Mapper.

> 📍 **A localização vem do navegador**, e o navegador só libera em `http://localhost` ou em HTTPS. Abrindo a página por `http://192.168.x.x:8080`, ele recusa. Se o acesso tiver sido negado, libere no ícone ao lado do endereço e escolha a data de novo.

| Endereço | Para que serve |
|---|---|
| <http://localhost:8080/> | Cadastro de tarefas, com a previsão embaixo do campo de data |
| <http://localhost:8080/contrato.html> | **Contrato da Aula 12**: testa a sua API contra cada exercício do `EXERCICIOS.md` |
| <http://localhost:8080/swagger-ui.html> | Swagger UI: a rota nova está no grupo **Previsão do tempo** |
| <http://localhost:8080/responsaveis.html> | Responsáveis e vínculo (como na Aula 09) |

## 4. Testar pelo PowerShell

```powershell
# A previsão de amanhã na SATC (troque a data)
Invoke-RestMethod "http://localhost:8080/previsao?latitude=-28.6775&longitude=-49.3697&data=2026-10-24"

# Uma regra barrando o pedido (400): data daqui a 30 dias
Invoke-RestMethod "http://localhost:8080/previsao?latitude=-28.6775&longitude=-49.3697&data=2026-11-30"
```

Para comparar, abra no navegador o que o Open-Meteo responde direto, sem a nossa API:
<https://api.open-meteo.com/v1/forecast?latitude=-28.6775&longitude=-49.3697&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto>

## Parar e recriar

```powershell
# Parar a API: Ctrl+C no terminal dela.
docker compose stop          # para o PostgreSQL, mantendo os dados
docker compose down -v       # apaga o banco (as migrations rodam de novo no próximo start)
```
