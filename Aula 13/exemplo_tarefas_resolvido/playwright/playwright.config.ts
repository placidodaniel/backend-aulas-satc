import { defineConfig } from '@playwright/test';

// Onde está a aplicação. Dentro do Docker Compose, a API se chama "api"; na sua máquina, localhost.
const baseURL = process.env.BASE_URL ?? 'http://localhost:8080';

export default defineConfig({
  testDir: './tests',
  // Um teste de cada vez: todos usam o mesmo banco, e assim a tela que você mostra é previsível.
  fullyParallel: false,
  workers: 1,
  timeout: 30_000,
  expect: { timeout: 7_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL,
    locale: 'pt-BR',
    // Grava o "filme" de cada teste (passo a passo, com a tela): é o que o modo UI mostra.
    trace: 'on',
    screenshot: 'only-on-failure',
    viewport: { width: 1280, height: 900 },
  },
  projects: [{ name: 'chromium', use: { browserName: 'chromium' } }],
});
