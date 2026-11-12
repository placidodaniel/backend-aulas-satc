import { test, expect, Page } from '@playwright/test';

// Testes de TELA do cadastro de tarefas (index.html): o Playwright abre um navegador de verdade,
// digita, clica e confere o que aparece, como uma pessoa faria.
// A previsão do tempo é simulada (veja "simularOpenMeteo"): o teste não depende de internet.

function amanha(): string {
  const d = new Date(Date.now() + 24 * 60 * 60 * 1000);
  return d.toISOString().slice(0, 10);
}

// Troca as respostas de /previsao e /previsao/agora por dados fixos (o navegador nem chega à nossa API).
async function simularOpenMeteo(page: Page) {
  await page.route('**/previsao**', async (route) => {
    const caminho = new URL(route.request().url()).pathname;
    if (caminho === '/previsao/agora') {
      return route.fulfill({
        json: { horario: '2026-10-29T14:15:00', descricao: 'Nublado', icone: '☁️', temperatura: 21.4, vento: 11.9 },
      });
    }
    if (caminho === '/previsao') {
      return route.fulfill({
        json: {
          data: amanha(), descricao: 'Trovoada', icone: '⛈️', temperaturaMinima: 17.0, temperaturaMaxima: 28.0,
          chanceDeChuva: 75, ventoMaximo: 15.9, latitude: -28.64, longitude: -49.36, origem: 'open-meteo',
        },
      });
    }
    return route.continue();
  });
}

// Preenche o formulário e clica em "Adicionar". Devolve o título (único), para achar a tarefa na lista.
async function cadastrar(page: Page, prefixo: string): Promise<string> {
  const titulo = `${prefixo} ${Date.now()}`;
  await page.locator('#input-titulo').fill(titulo);
  await page.locator('#input-responsavel').fill('Ana');
  await page.locator('#input-prazo').fill(amanha());
  await page.locator('#input-prioridade').fill('4');
  await page.locator('#btn-add').click();
  return titulo;
}

test.beforeEach(async ({ page }) => {
  await simularOpenMeteo(page);
  // Espera a lista inicial (GET /tarefas) chegar ANTES de mexer na tela. O formulário aparece antes
  // dela; cadastrar nesse intervalo faz a página inserir a tarefa duas vezes (veja o README).
  const listaCarregada = page.waitForResponse(
    (r) => new URL(r.url()).pathname === '/tarefas' && r.request().method() === 'GET');
  await page.goto('/');
  await listaCarregada;
  await expect(page.locator('#form-add')).toBeVisible();
  await expect(page.locator('#lista-abertas li').first()).toBeVisible();
});

test('a página abre com o formulário de cadastro', async ({ page }) => {
  await expect(page).toHaveTitle(/tarefas/i);
  await expect(page.locator('#input-titulo')).toBeVisible();
  await expect(page.locator('#btn-add')).toBeEnabled();
});

test('cadastra uma tarefa e ela aparece em "abertas"', async ({ page }) => {
  const titulo = await cadastrar(page, 'Estudar Playwright');

  const item = page.locator('#lista-abertas li', { hasText: titulo });
  await expect(item).toBeVisible();
  await expect(item).toContainText('Ana');
  await expect(item).toContainText('Prioridade 4');
});

test('título vazio: a API recusa e a tela mostra o erro', async ({ page }) => {
  await page.locator('#input-responsavel').fill('Ana');
  await page.locator('#input-prazo').fill(amanha());
  await page.locator('#btn-add').click();

  await expect(page.locator('#erro-titulo')).toContainText(/título/i);
});

test('escolher a data do prazo mostra a previsão do tempo (Criciúma)', async ({ page }) => {
  // A localização é fixa (Criciúma/SC): a página não pede nada ao navegador.
  const pedido = page.waitForRequest((r) => r.url().includes('/previsao?'));
  await page.locator('#input-prazo').fill(amanha());

  const url = (await pedido).url();
  expect(url).toContain('latitude=-28.6775');
  expect(url).toContain('longitude=-49.3697');

  const caixa = page.locator('#previsao');
  await expect(caixa).toBeVisible();
  await expect(caixa).toContainText('Trovoada');
  await expect(caixa).toContainText('Criciúma/SC');
  await expect(caixa).toContainText('Agora: ☁️ Nublado');
});

test('concluir move a tarefa para "concluídas" e reabrir devolve', async ({ page }) => {
  const titulo = await cadastrar(page, 'Concluir');
  await page.locator('#lista-abertas li', { hasText: titulo }).locator('[data-acao="concluir"]').click();

  const concluida = page.locator('#lista-concluidas li', { hasText: titulo });
  await expect(concluida).toBeVisible();

  await concluida.locator('[data-acao="concluir"]').click();
  await expect(page.locator('#lista-abertas li', { hasText: titulo })).toBeVisible();
});

test('excluir remove a tarefa da lista', async ({ page }) => {
  const titulo = await cadastrar(page, 'Excluir');
  const item = page.locator('#lista-abertas li', { hasText: titulo });
  await expect(item).toBeVisible();

  await item.locator('[data-acao="excluir"]').click();
  await expect(page.locator('li', { hasText: titulo })).toHaveCount(0);
});
