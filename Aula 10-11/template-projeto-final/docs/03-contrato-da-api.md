# 3. Contrato da API

> O contrato é o que o cliente da API enxerga: rotas, JSON e status. Desenhe as rotas de **todas** as entidades: nesta etapa só três delas precisam funcionar. As linhas marcadas com *(exemplo)* mostram o nível de detalhe esperado: apague e escreva as do grupo.

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

### Livros *(exemplo)*

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| `GET` | `/livros` | Lista os livros | Nenhuma | lista de `LivroResponseDTO` | `200` | Nenhum | sim |
| `GET` | `/livros/{id}` | Busca um livro | Nenhuma | `LivroResponseDTO` | `200` | `404` | sim |
| `POST` | `/livros` | Cadastra um livro | `LivroRequestDTO` | `LivroResponseDTO` | `201` | `400` | sim |
| `PUT` | `/livros/{id}` | Atualiza um livro | `LivroRequestDTO` | `LivroResponseDTO` | `200` | `400`, `404` | não |
| `DELETE` | `/livros/{id}` | Remove um livro | Nenhuma | Nenhuma (sem corpo) | `204` | `404` | não |

### {{Recurso 2}}

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| ... | ... | ... | ... | ... | ... | ... | não |

### {{Recurso 3}}

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| ... | ... | ... | ... | ... | ... | ... | não |

### Rotas que não são CRUD

As que nascem das regras de negócio de `01-visao-geral.md`.

| Verbo | Caminho | O que faz | Regra | Sucesso | Erros |
|---|---|---|---|---|---|
| `PUT` *(exemplo)* | `/emprestimos/{id}/devolver` | Registra a devolução e libera o livro | R2 | `200` | `404` |
| ... | ... | ... | ... | ... | ... |

## DTOs

O que entra não é igual ao que sai: o cliente não envia `id` nem campos que a API decide.

| DTO | Direção | Campos | Validações |
|---|---|---|---|
| `LivroRequestDTO` *(exemplo)* | entrada | `titulo`, `isbn` | os dois obrigatórios (`@NotBlank`) |
| `LivroResponseDTO` *(exemplo)* | saída | `id`, `titulo`, `isbn`, `disponivel` | Nenhuma |
| ... | ... | ... | ... |

## Exemplos

Um par requisição/resposta por entidade.

### `POST /livros` *(exemplo)*

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
| `404` | Id que não existe |
| {{status}} | {{ex.: regra R2 violada, porque o livro já está emprestado}} |
