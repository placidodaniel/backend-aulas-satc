# 1. Visão geral

## Tema

Exemplo da disciplina. Biblioteca não está entre os temas do sorteio: este projeto existe só para mostrar como fica uma entrega completa da Etapa 1.

## Problema

A biblioteca do bairro controla os empréstimos num caderno. Quando um leitor pede um livro, o bibliotecário folheia páginas para descobrir se o exemplar está na estante ou com alguém, e não tem como saber quem está atrasado. O sistema guarda o acervo, os leitores e cada empréstimo, e responde na hora se um livro está disponível e quais empréstimos passaram do prazo.

## Quem usa

| Ator | O que faz no sistema |
|---|---|
| Bibliotecário | Cadastra livros e leitores, registra empréstimos e devoluções |
| Leitor | Consulta os livros do acervo e se estão disponíveis |

## O que o sistema faz

- Cadastra e consulta os livros do acervo.
- Cadastra e consulta os leitores.
- Registra a saída de um livro (empréstimo) e a volta dele (devolução).
- Mostra se um empréstimo está atrasado.

## Regras de negócio

Criadas pelo grupo a partir do problema acima. Moram no **Service**.

| # | Regra | Entidades envolvidas |
|---|---|---|
| R1 | Um leitor não pode ter mais de 3 empréstimos em aberto | Leitor, Emprestimo |
| R2 | Um livro emprestado só pode ser emprestado de novo depois da devolução | Livro, Emprestimo |
| R3 | O prazo de devolução é de 14 dias; depois disso, o empréstimo fica atrasado | Emprestimo |

## Fora do escopo

- Multa por atraso.
- Reserva de livros.
- Tela de frontend: a API é testada pelo Postman e pelo Swagger UI.
