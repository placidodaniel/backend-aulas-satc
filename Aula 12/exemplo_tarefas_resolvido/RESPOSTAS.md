# Respostas dos subexercícios: Aula 12

Gabarito dos 10 subexercícios do `EXERCICIOS.md`. O código de cada exercício está resolvido nesta mesma pasta (veja a tabela no `README.md`).

---

## Exercício 1: O tempo de agora

**1.1**: Por que a nossa API não repassa as listas do `daily` do jeito que vieram?

Porque o formato do Open-Meteo é o **contrato dele**, e não o nosso. Ele usa uma lista por medida porque serve qualquer cliente, para qualquer período e qualquer combinação de medidas: é compacto e genérico. Para quem usa a nossa API, esse formato é ruim de ler: para saber a previsão de um dia, é preciso pegar o mesmo índice em seis listas diferentes, e entender que `weather_code: 95` quer dizer trovoada.

Traduzindo no `PrevisaoMapper`, a nossa API:

- fala a língua de quem a usa (português, um objeto por dia, `descricao` e `icone` prontos);
- **não fica amarrada** ao Open-Meteo: se amanhã a previsão vier de outro serviço, com outro formato, só o pacote `client` e o mapper mudam. O `PrevisaoDTO`, que é o que os clientes da nossa API conhecem, continua igual;
- decide o que sai: o Open-Meteo manda `generationtime_ms`, `elevation`, `daily_units`... e nada disso interessa a quem cadastra uma tarefa.

É a mesma lição da Aula 09 (a entidade não é o contrato), agora com o dado de outro sistema no lugar da entidade.

**1.2**: Por que a página pede a previsão à nossa API, e não direto ao Open-Meteo?

- **Para combinar com o que é nosso.** O Exercício 2 junta a previsão com a tarefa, e o 5 com a lista de pendentes. O navegador não tem o banco; a API tem.
- **Uma regra, um lugar.** A tradução dos códigos, as regras (R1 a R3) e o limite de 16 dias ficam no backend. Uma página web, um app de celular e outro sistema recebem a mesma resposta, sem cada um reescrever a tabela de códigos WMO.
- **Trocar de fornecedor sem mexer nos clientes.** Se o Open-Meteo sair do ar de vez, ou passar a cobrar, muda-se o `OpenMeteoClient`. A página continua chamando `GET /previsao`.
- **Controle sobre o uso.** O cache do Exercício 4 só é possível no meio do caminho: 30 navegadores chamando direto fariam 30 vezes mais requisições ao Open-Meteo.
- **Segredos.** Muitas APIs pedem uma chave de acesso. No backend, ela fica no `application.properties` (ou numa variável de ambiente); no navegador, qualquer um veria no F12.

O preço é um passo a mais (um pouco mais de tempo) e a nossa API passar a depender do Open-Meteo, o que é exatamente o assunto dos Exercícios 3 e 4.

---

## Exercício 2: Previsão para o prazo da tarefa

**2.1**: Por que um prazo daqui a 30 dias dá `400` no `GET /previsao`, mas `200` com aviso no `GET /tarefas/{id}/previsao`?

Porque as duas rotas respondem perguntas diferentes:

- `GET /previsao?data=...` pergunta **"qual é a previsão deste dia?"**. Para um dia daqui a 30 dias, essa pergunta não tem resposta: quem pediu errou o pedido. É `400`, com a mensagem da regra.
- `GET /tarefas/{id}/previsao` pergunta **"como está esta tarefa, e qual é a previsão do prazo dela?"**. A tarefa existe e o pedido está certo. Só uma **parte** da resposta (a previsão) não está disponível. Falhar o pedido inteiro por causa dela esconderia a tarefa, que é a parte principal.

Esse é o **fallback**: a resposta muda (vem `"previsao": null` e um aviso), mas não falha. Quem usa a API mostra a tarefa e escreve "previsão indisponível", em vez de mostrar uma tela de erro.

**2.2**: O que se perderia se o `TarefaService` chamasse o `OpenMeteoClient` direto?

Tudo o que o `PrevisaoService` faz e que teria de ser copiado no `TarefaService`:

- as **regras** (R1 a R3) e a conferência de que a resposta trouxe o dia pedido;
- a **tradução** pelo `PrevisaoMapper` (e o `TarefaService` passaria a conhecer o formato do Open-Meteo, com listas e `weather_code`);
- o **cache** do Exercício 4: a previsão do prazo de uma tarefa usa o mesmo cache do `GET /previsao`. Chamando o client direto, cada clique em ☁️ iria ao Open-Meteo.

Duas cópias da mesma lógica acabam diferentes: alguém corrige uma e esquece a outra. É o mesmo princípio de um mapper usar o outro (Aula 09): cada classe cuida de uma coisa, e quem precisa reaproveita. O `OpenMeteoClient` só sabe fazer a requisição; quem sabe o que é uma "previsão" na nossa API é o `PrevisaoService`.

---

## Exercício 3: Tempo esgotado vira 504

**3.1**: Por que a `PrevisaoTempoEsgotadoException` estende a `PrevisaoIndisponivelException`?

Porque tempo esgotado **é** um caso de "o serviço de previsão falhou", só que mais específico. Sendo filha:

- todo código que já trata a mãe continua tratando o tempo esgotado, sem mudar nada;
- o `ApiExceptionHandler` escolhe o tratador mais específico: `504` para a filha, `502` para as outras.

Com o exemplo da pergunta (um `catch (PrevisaoIndisponivelException e)` que devolve um aviso no lugar do erro): se a nova exceção estendesse a `RuntimeException`, esse `catch` **não pegaria** o tempo esgotado. Justo quando o Open-Meteo está lento, o fallback deixaria de funcionar, e a exceção subiria até o `ApiExceptionHandler`, virando `504` em vez do aviso. Herdar da mãe é dizer ao compilador: "isto também é aquilo", e quem já cuidava daquilo continua cuidando.

**3.2**: Com `read-timeout` de 60 segundos e o Open-Meteo lento, o que acontece com a nossa API quando 200 pessoas abrem o cadastro?

Cada requisição ao `GET /previsao` ocupa uma **thread** do Tomcat enquanto espera o Open-Meteo. O Tomcat do Spring Boot tem, por padrão, **200 threads**. Com 200 pessoas escolhendo uma data, as 200 ficam presas esperando até 60 segundos cada uma.

Nesse tempo, **qualquer** outra requisição fica na fila, inclusive as que nem usam o Open-Meteo, como o `GET /tarefas` e o `POST /tarefas`. Para o usuário, a API inteira travou, por causa de um serviço que nem é dela. É uma **falha em cascata**: a lentidão de uma dependência derruba quem depende dela.

Com 5 segundos, cada thread é liberada rápido, e a API responde `504` para a previsão, mas continua atendendo o resto. É por isso que todo cliente HTTP precisa de timeout, e que o valor deve ser o menor que ainda funciona no dia a dia. (Em sistemas maiores, existe ainda o **circuit breaker**: depois de algumas falhas seguidas, a API para de chamar o serviço por um tempo e responde o fallback na hora.)

---

## Exercício 4: Cache de 30 minutos

**4.1**: 30 alunos, 10 trocas de data cada, entre os mesmos 3 dias. Quantas chamadas ao Open-Meteo cada API faz, com e sem cache? Qual é o preço?

Cada aluno roda a **sua** API, com o seu cache:

- **sem cache:** cada troca de data é uma chamada. São **10 chamadas por aluno**, 300 no laboratório;
- **com cache:** só a primeira vez de cada dia vai ao Open-Meteo. São **3 chamadas por aluno** (uma por dia diferente), 90 no laboratório. As outras 7 de cada um saem da memória, em poucos milissegundos.

(Se fosse **uma** API para a turma inteira, como em produção, o cache seria compartilhado: 3 chamadas para os 30 alunos juntos.)

O preço:

- **dado desatualizado:** por até 30 minutos, a API responde a previsão que tinha. Se o Open-Meteo atualizar a previsão nesse meio-tempo, a nossa API não fica sabendo. Para previsão do tempo, que muda devagar, 30 minutos é aceitável; para o saldo de uma conta bancária, não seria;
- **memória:** cada chave guardada ocupa espaço. Este cache nunca apaga as previsões vencidas (só deixa de usá-las), então cresce com o tempo. Numa API real, usa-se uma biblioteca de cache (Caffeine, Redis) que limita o tamanho e remove as entradas velhas;
- **cada instância tem o seu:** o cache está na memória do processo. Reiniciou a API, perdeu tudo; com duas cópias da API rodando, cada uma tem o próprio cache.

**4.2**: O que aconteceria com as coordenadas inteiras na chave? E com 0 casas?

- **Coordenadas inteiras** (todas as casas que o navegador manda, como `-28.677512` e `-28.677498`): duas pessoas na mesma sala, ou a mesma pessoa em dois momentos (o GPS varia um pouco), gerariam chaves diferentes. O cache quase nunca acertaria, e a API chamaria o Open-Meteo quase sempre. O cache existiria, mas não serviria para nada.
- **2 casas** (uns 1,1 km): pessoas no mesmo bairro compartilham a chave. A previsão do tempo não muda em menos de 1 km, e o próprio Open-Meteo arredonda para a grade dele (repare que `-28.6775` volta como `-28.64675`).
- **0 casas** (uns 111 km): Criciúma e Florianópolis poderiam cair na mesma chave. A primeira pessoa a pedir decidiria a previsão de todo mundo num raio enorme: o cache acertaria muito, mas devolveria a **previsão errada**.

A precisão da chave é uma troca entre acertar o cache e acertar o lugar. A escolha certa depende do dado: para previsão do tempo, alguns quilômetros não importam; para o endereço de uma entrega, importariam.

---

## Exercício 5: Tarefas em dias de chuva

**5.1**: Com 50 tarefas pendentes, quantas chamadas faria o jeito ingênuo, e quantas faz o deste exercício? O que mais piora no jeito ingênuo?

- **Ingênuo:** 50 chamadas, uma por tarefa. (Com o cache do Exercício 4 ajudando, no máximo 16, uma por dia diferente da janela, mas ainda assim várias.)
- **Este exercício:** **1 chamada**, com `start_date` e `end_date` cobrindo os 16 dias.

Além do número:

- **tempo:** cada chamada leva algumas centenas de milissegundos. Feitas uma depois da outra, 50 chamadas de 300 ms são 15 segundos para responder uma lista, passando do `read-timeout` de quem chama a nossa API;
- **chance de falha:** com 50 chamadas, basta uma falhar (ou passar do timeout) para a resposta inteira dar erro;
- **limite do outro lado:** serviços públicos limitam quantas requisições cada cliente pode fazer (o Open-Meteo gratuito tem limite diário). Gastar 50 onde 1 resolve esgota esse limite.

É o mesmo problema do "N+1" de banco de dados (uma consulta para a lista, mais uma para cada item), só que pela internet, onde cada ida e volta custa muito mais.

**5.2**: O `GET /tarefas/chuvosas` aproveita o cache do Exercício 4?

**Não.** O cache está dentro do `buscar()`, e a chave é `latitude;longitude;data`, de **um dia**. O `buscarJanela()` não passa pelo `buscar()`: ele chama o `OpenMeteoClient` direto, pedindo o **período** de hoje até hoje + 15, e não consulta nem preenche o cache.

Cada `GET /tarefas/chuvosas` faz, portanto, uma chamada nova ao Open-Meteo, e um `GET /previsao` logo depois, para um desses dias, também faz a sua.

Para aproveitar, daria para:

- fazer o `buscarJanela()` **guardar** no cache cada um dos 16 dias que recebeu (com a mesma chave do `buscar()`), e assim os `GET /previsao` seguintes sairiam da memória;
- ou criar um cache para a janela inteira, com a chave `latitude;longitude;janela` e a mesma validade de 30 minutos.

A primeira opção é a mais útil: uma chamada de 16 dias abastece as perguntas de um dia que vierem depois.
