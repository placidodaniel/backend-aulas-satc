import { APIRequestContext, Page } from '@playwright/test';
import { createHmac } from 'node:crypto';

// Aula 15: ajudantes dos testes de tela. Desde a Aula 15 o sistema exige login.

export const SENHA = 'uma senha longa de teste';

export function emailNovo(prefixo = 'tela'): string {
  return `${prefixo}-${Date.now()}-${Math.floor(Math.random() * 1e6)}@teste.com`;
}

// Cria a conta e entra PELA API (rápido), e coloca a sessão no sessionStorage antes de a
// página carregar. Para os testes que não são sobre o login em si.
export async function entrarPelaApi(page: Page, request: APIRequestContext, nome = 'Ana Teste') {
  const email = emailNovo();
  await request.post('/auth/cadastro', { data: { nome, email, senha: SENHA } });
  const login = await (await request.post('/auth/login', { data: { email, senha: SENHA } })).json();
  const sessao = JSON.stringify({ token: login.token, expiraEm: login.expiraEm, usuario: login.usuario });
  // addInitScript: roda antes de qualquer script da página, a cada navegação.
  await page.addInitScript((s) => sessionStorage.setItem('tarefas.sessao', s), sessao);
  return { email, token: login.token as string };
}

// O código de 6 dígitos que o aplicativo autenticador mostraria (RFC 6238), calculado aqui.
// deslocamento = 1 é o código dos PRÓXIMOS 30 segundos (o atual pode já ter sido usado).
export function codigoTotp(segredoBase32: string, deslocamento = 0): string {
  const alfabeto = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';
  let bits = '';
  for (const c of segredoBase32.replace(/\s/g, '')) bits += alfabeto.indexOf(c).toString(2).padStart(5, '0');
  const chave = Buffer.from(bits.match(/.{8}/g)!.map((b) => parseInt(b, 2)));
  const passo = Math.floor(Date.now() / 1000 / 30) + deslocamento;
  const mensagem = Buffer.alloc(8);
  mensagem.writeBigUInt64BE(BigInt(passo));
  const hash = createHmac('sha1', chave).update(mensagem).digest();
  const inicio = hash[hash.length - 1] & 0x0f;
  const numero = (hash.readUInt32BE(inicio) & 0x7fffffff) % 1_000_000;
  return String(numero).padStart(6, '0');
}
