import { test, expect } from '@playwright/test';
import { SENHA, emailNovo, codigoTotp } from './sessao';

// Aula 15: testes de TELA da autenticação: o porteiro, o cadastro, o login, o menu,
// o "Sair" e o fluxo completo da verificação em duas etapas (2FA).

test('sem login, qualquer página manda para a tela de entrar', async ({ page }) => {
  await page.goto('/responsaveis.html');
  await expect(page).toHaveURL(/entrar\.html\?voltar=responsaveis\.html/);
  await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible();
});

test('criar conta, entrar, ver o menu e sair', async ({ page }) => {
  const email = emailNovo('cadastro');
  await page.goto('/criar-conta.html');
  await page.locator('#nome').fill('Bia Tela');
  await page.locator('#email').fill(email);
  await page.locator('#senha').fill(SENHA);
  await page.locator('#confirmar').fill(SENHA);
  await page.getByRole('button', { name: 'Criar conta' }).click();

  await expect(page).toHaveURL(/entrar\.html\?motivo=criada/);
  await page.locator('#email').fill(email);
  await page.locator('#senha').fill(SENHA);
  await page.getByRole('button', { name: 'Entrar' }).click();

  // O menu aparece com o nome de quem entrou e o selo do 2FA.
  const menu = page.locator('nav.menu-sistema');
  await expect(menu).toContainText('Bia Tela');
  await expect(menu).toContainText('2FA desligado');
  await expect(menu.locator('a.item.ativo')).toHaveText(/Tarefas/);

  await menu.getByRole('button', { name: 'Sair' }).click();
  await expect(page).toHaveURL(/entrar\.html/);
  await expect(page.locator('#aviso')).toContainText('Você saiu');
});

test('senha errada mostra a mensagem genérica', async ({ page }) => {
  await page.goto('/entrar.html');
  await page.locator('#email').fill(emailNovo('ninguem'));
  await page.locator('#senha').fill('qualquer coisa errada');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.locator('#aviso')).toContainText('E-mail ou senha inválidos');
});

test('2FA: ligar com o QR Code e entrar em duas etapas', async ({ page, request }) => {
  const email = emailNovo('2fa');
  await request.post('/auth/cadastro', { data: { nome: 'Caio 2FA', email, senha: SENHA } });

  // Entra (sem 2FA ainda) e vai para "Minha conta".
  await page.goto('/entrar.html?voltar=conta.html');
  await page.locator('#email').fill(email);
  await page.locator('#senha').fill(SENHA);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page).toHaveURL(/conta\.html/);

  // Liga o 2FA: aparece o QR Code e o segredo. O "celular" do teste é a função codigoTotp.
  await page.getByRole('button', { name: /Ligar a verificação/ }).click();
  await expect(page.locator('#qr')).toHaveAttribute('src', /^data:image\/svg\+xml/);
  const segredo = (await page.locator('#segredo').textContent())!;
  await page.locator('#codigo-ativar').fill(codigoTotp(segredo));
  await page.getByRole('button', { name: 'Ativar' }).click();
  await expect(page.locator('#selo')).toHaveText('ligado');
  await expect(page.locator('nav.menu-sistema')).toContainText('2FA ligado');

  // Sai e entra de novo: agora a senha sozinha não basta.
  await page.getByRole('button', { name: 'Sair' }).click();
  await page.locator('#email').fill(email);
  await page.locator('#senha').fill(SENHA);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('heading', { name: /duas etapas/ })).toBeVisible();

  // O código dos próximos 30 s (o atual já foi usado na ativação e não vale de novo).
  await page.locator('#codigo').fill(codigoTotp(segredo, 1));
  await expect(page).toHaveURL(/index\.html/);
  await expect(page.locator('nav.menu-sistema')).toContainText('Caio 2FA');
});
