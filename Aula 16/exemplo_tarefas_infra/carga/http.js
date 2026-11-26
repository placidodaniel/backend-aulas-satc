// Teste de carga HTTP (k6): chama TODAS as rotas da API para alimentar o dashboard inteiro.
// Cada iteração cria uma tarefa e faz o ciclo completo (consultar, alterar, concluir,
// reabrir, vincular, excluir) e ainda provoca erros 400 e 404 de propósito.
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://proxy';
const JSON_H = { headers: { 'Content-Type': 'application/json' } };

export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: `${__ENV.SEGUNDOS || 60}s`,
};

function dia(dias) {
  return new Date(Date.now() + dias * 86400000).toISOString().slice(0, 10);
}
function sorteio(n) { return Math.floor(Math.random() * n); }

// Aula 15: a API exige login. O setup() cria uma conta só para a carga, entra uma vez e
// passa o token para todos os usuários virtuais (o token vale 30 minutos).
// Fazer login a cada iteração mediria o BCrypt (lento de propósito), não a API.
export function setup() {
  const email = `carga-${Date.now()}@teste.com`;
  const senha = 'senha longa da carga de teste';
  http.post(`${BASE}/auth/cadastro`, JSON.stringify({ nome: 'Carga', email, senha }), JSON_H);
  const login = http.post(`${BASE}/auth/login`, JSON.stringify({ email, senha }), JSON_H);
  check(login, { 'login da carga': (x) => x.status === 200 });
  const token = login.json('token');

  const r = http.post(`${BASE}/responsaveis`, JSON.stringify({
    nome: 'Responsável da carga', email: `carga-${Date.now()}@satc.edu.br` }), comToken(token, true));
  check(r, { 'responsável criado': (x) => x.status === 201 });
  return { responsavelId: r.json('id'), token };
}

// Parâmetros de uma requisição com o cabeçalho Authorization (e JSON, se tiver corpo).
function comToken(token, json) {
  const headers = { Authorization: `Bearer ${token}` };
  if (json) headers['Content-Type'] = 'application/json';
  return { headers };
}

export default function (dados) {
  const ok = (r, nome, ...codigos) => check(r, { [nome]: (x) => codigos.includes(x.status) });
  const P = comToken(dados.token, false);   // sem corpo
  const JP = comToken(dados.token, true);   // com corpo JSON

  // criar (publica evento "criada")
  const nova = http.post(`${BASE}/tarefas`, JSON.stringify({
    titulo: `Tarefa de carga ${__VU}-${__ITER}`, responsavel: 'Ana',
    dataPrazo: dia(1 + sorteio(20)), prioridade: 1 + sorteio(5) }), JP);
  ok(nova, 'POST /tarefas', 201);
  const id = nova.json('id');

  // consultar
  ok(http.get(`${BASE}/tarefas`, P), 'GET /tarefas', 200);
  ok(http.get(`${BASE}/tarefas/${id}`, P), 'GET /tarefas/{id}', 200);
  ok(http.get(`${BASE}/tarefas/buscar?responsavel=Ana`, P), 'GET /tarefas/buscar', 200);
  ok(http.get(`${BASE}/tarefas/atrasadas`, P), 'GET /tarefas/atrasadas', 200);

  // alterar
  ok(http.put(`${BASE}/tarefas/${id}`, JSON.stringify({
    titulo: 'Tarefa alterada', responsavel: 'Bruno', dataPrazo: dia(5), prioridade: 3 }), JP),
    'PUT /tarefas/{id}', 200);
  ok(http.patch(`${BASE}/tarefas/${id}`, JSON.stringify({ prioridade: 5 }), JP),
    'PATCH /tarefas/{id}', 200);

  // vincular e desvincular responsável
  ok(http.put(`${BASE}/tarefas/${id}/responsavel`,
    JSON.stringify({ responsavelId: dados.responsavelId }), JP), 'PUT responsavel', 200);
  ok(http.del(`${BASE}/tarefas/${id}/responsavel`, null, P), 'DELETE responsavel', 200, 204);

  // concluir e reabrir (publica "concluida" e "reaberta")
  ok(http.put(`${BASE}/tarefas/${id}/concluir`, null, P), 'PUT concluir', 200);
  ok(http.put(`${BASE}/tarefas/${id}/concluir`, null, P), 'PUT reabrir', 200);

  // responsáveis
  ok(http.get(`${BASE}/responsaveis`, P), 'GET /responsaveis', 200);
  ok(http.get(`${BASE}/responsaveis/${dados.responsavelId}`, P), 'GET /responsaveis/{id}', 200);
  ok(http.get(`${BASE}/responsaveis/${dados.responsavelId}/tarefas`, P), 'GET responsaveis tarefas', 200);

  // cadastrar responsável, contrato OpenAPI e página estática
  if (__ITER % 10 === 0) {
    ok(http.post(`${BASE}/responsaveis`, JSON.stringify({
      nome: `Carga ${__VU}-${__ITER}`, email: `c${__VU}-${__ITER}-${Date.now()}@satc.edu.br` }), JP),
      'POST /responsaveis', 201);
    ok(http.get(`${BASE}/v3/api-docs`, P), 'GET /v3/api-docs', 200);
    ok(http.get(`${BASE}/`, P), 'GET / (página)', 200);
  }

  // previsão (Open-Meteo): poucas coordenadas e datas, então há "miss" e muitos "hit" no cache
  if (__ITER % 4 === 0) {
    const lat = (-28.67 - sorteio(8) / 100).toFixed(2);
    const lon = '-49.37';
    const q = `latitude=${lat}&longitude=${lon}`;
    ok(http.get(`${BASE}/previsao?${q}&data=${dia(sorteio(3))}`, P), 'GET /previsao', 200, 502, 503);
    ok(http.get(`${BASE}/previsao/agora?${q}`, P), 'GET /previsao/agora', 200, 502, 503);
    ok(http.get(`${BASE}/tarefas/${id}/previsao?${q}`, P), 'GET /tarefas/{id}/previsao', 200, 409, 422, 502, 503);
    ok(http.get(`${BASE}/tarefas/chuvosas?${q}`, P), 'GET /tarefas/chuvosas', 200, 502, 503);
  }

  // erros de propósito (alimentam os painéis de erros 4xx)
  ok(http.get(`${BASE}/tarefas/999999999`, P), '404 tarefa inexistente', 404);
  ok(http.post(`${BASE}/tarefas`, JSON.stringify({ titulo: '' }), JP), '400 corpo inválido', 400);
  ok(http.get(`${BASE}/previsao`, P), '400 sem parâmetros', 400);

  // excluir (publica "removida")
  ok(http.del(`${BASE}/tarefas/${id}`, null, P), 'DELETE /tarefas/{id}', 200, 204);
  ok(http.get(`${BASE}/tarefas/${id}`, P), '404 depois de excluir', 404);

  sleep(0.2);
}
