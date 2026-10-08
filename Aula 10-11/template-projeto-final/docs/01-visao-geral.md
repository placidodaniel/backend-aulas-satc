# 1. Visão geral

> Preencha antes de escrever código. As linhas marcadas com *(exemplo)* mostram o nível de detalhe esperado: apague e escreva as do grupo. O domínio do exemplo (biblioteca) não vale como tema.

## Tema

{{O tema sorteado para o grupo.}}

## Problema

O problema é criado pelo grupo, dentro do tema.

{{Em 3 a 5 linhas: qual é o problema, quem sofre com ele hoje e o que o sistema muda.}}

## Quem usa

| Ator | O que faz no sistema |
|---|---|
| Bibliotecário *(exemplo)* | Cadastra livros e leitores, registra empréstimos e devoluções |
| Leitor *(exemplo)* | Consulta os livros disponíveis e os próprios empréstimos |

## O que o sistema faz

- {{Funcionalidade 1, em uma frase que começa com verbo}}
- {{Funcionalidade 2}}
- {{Funcionalidade 3}}

## Regras de negócio

No mínimo 3, **criadas pelo grupo** a partir do problema acima: o que o sistema precisa garantir ou impedir. As do exemplo (biblioteca) só mostram o formato. São elas que vão morar no **Service**. O que só valida formato (`campo obrigatório`, `número positivo`) é validação do DTO, não regra de negócio.

| # | Regra | Entidades envolvidas |
|---|---|---|
| R1 *(exemplo)* | Um leitor não pode ter mais de 3 empréstimos em aberto | Leitor, Emprestimo |
| R2 *(exemplo)* | Um livro emprestado só pode ser emprestado de novo depois da devolução | Livro, Emprestimo |
| R3 | ... | ... |

## Fora do escopo

O que o grupo decidiu **não** fazer, para o projeto caber no semestre.

- {{ex.: pagamento de multa por atraso}}
- {{ex.: tela de frontend}}
