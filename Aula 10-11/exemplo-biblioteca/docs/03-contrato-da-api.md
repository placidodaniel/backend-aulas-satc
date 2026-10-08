# 3. Contrato da API

## Convenções

| Item | Valor |
|---|---|
| Endereço base | `http://localhost:8080` |
| Formato | JSON, na requisição e na resposta |
| Datas | `yyyy-MM-dd` |
| Erros | Sempre no formato `ErroDTO` (ver [Erros](#erros)) |
| Documentação viva | `http://localhost:8080/swagger-ui.html` |

## Rotas

Coluna **Funciona**: `sim` para as rotas da API simples, que já respondem nesta etapa.

### Livros

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| `POST` | `/livros` | Cadastra um livro | `LivroRequestDTO` | `LivroResponseDTO` | `201` | `400` | sim |
| `GET` | `/livros` | Lista os livros | Nenhuma | lista de `LivroResponseDTO` | `200` | Nenhum | sim |
| `GET` | `/livros/{id}` | Busca um livro | Nenhuma | `LivroResponseDTO` | `200` | `404` | sim |

### Leitores

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| `POST` | `/leitores` | Cadastra um leitor | `LeitorRequestDTO` | `LeitorResponseDTO` | `201` | `400` | não |
| `GET` | `/leitores` | Lista os leitores | Nenhuma | lista de `LeitorResponseDTO` | `200` | Nenhum | não |
| `GET` | `/leitores/{id}` | Busca um leitor | Nenhuma | `LeitorResponseDTO` | `200` | `404` | não |

### Empréstimos

As duas rotas nascem das regras de negócio de `01-visao-geral.md`.

| Verbo | Caminho | O que faz | Regra | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|---|
| `POST` | `/emprestimos` | Registra um empréstimo e tira o livro da estante | R1 e R2 | `EmprestimoRequestDTO` | `EmprestimoResponseDTO` | `201` | `400`, `404` | não |
| `PUT` | `/emprestimos/{id}/devolver` | Registra a devolução e devolve o livro à estante | R3 | Nenhuma | `EmprestimoResponseDTO` | `200` | `404` | não |

## DTOs

| DTO | Direção | Campos | Validações |
|---|---|---|---|
| `LivroRequestDTO` | entrada | `titulo`, `isbn` | os dois obrigatórios; `isbn` com até 20 caracteres |
| `LivroResponseDTO` | saída | `id`, `titulo`, `isbn`, `disponivel` | Nenhuma |
| `LeitorRequestDTO` | entrada | `nome`, `email` | os dois obrigatórios; `email` em formato válido |
| `LeitorResponseDTO` | saída | `id`, `nome`, `email` | Nenhuma |
| `EmprestimoRequestDTO` | entrada | `leitorId`, `livroId` | os dois obrigatórios |
| `EmprestimoResponseDTO` | saída | `id`, `leitor`, `livro`, `dataRetirada`, `dataLimite`, `dataDevolucao`, `atrasado` | Nenhuma. `dataLimite` e `atrasado` são calculados pela regra R3 |

## Exemplos

### `POST /livros` (funciona)

Requisição:

```json
{
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5"
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "disponivel": true
}
```

### `POST /leitores` (próxima etapa)

Requisição:

```json
{
  "nome": "Ana Souza",
  "email": "ana@exemplo.com"
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "nome": "Ana Souza",
  "email": "ana@exemplo.com"
}
```

### `POST /emprestimos` (próxima etapa)

Entram só os ids; leitor e livro voltam completos.

Requisição:

```json
{
  "leitorId": 1,
  "livroId": 1
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "leitor": { "id": 1, "nome": "Ana Souza", "email": "ana@exemplo.com" },
  "livro": { "id": 1, "titulo": "Dom Casmurro", "isbn": "978-85-359-0277-5", "disponivel": false },
  "dataRetirada": "2026-10-06",
  "dataLimite": "2026-10-20",
  "dataDevolucao": null,
  "atrasado": false
}
```

## Erros

Todo erro sai no mesmo formato. Este é o `404` de `GET /livros/999`:

```json
{
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Livro não encontrado: 999",
  "caminho": "/livros/999",
  "timestamp": "2026-10-06T19:30:00",
  "campos": []
}
```

No `400` de validação, a lista `campos` traz um item por campo inválido: `{"campo": "titulo", "mensagem": "Título é obrigatório"}`.

| Status | Quando acontece neste projeto |
|---|---|
| `400` | JSON ilegível ou campo inválido no DTO de entrada |
| `400` | Na próxima etapa: regra R1 ou R2 violada, com a regra explicada na `mensagem` |
| `404` | Id de livro, leitor ou empréstimo que não existe |
