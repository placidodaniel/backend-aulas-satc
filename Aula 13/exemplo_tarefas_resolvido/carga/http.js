// Teste de carga HTTP (k6): chama TODAS as rotas da API para alimentar o dashboard inteiro.
// Cada iteração cria uma tarefa e faz o ciclo completo (consultar, alterar, concluir,
// reabrir, vincular, excluir) e ainda provoca erros 400 e 404 de propósito.
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const JSON_H = { headers: { 'Content-Type': 'application/json' } };

export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: `${__ENV.SEGUNDOS || 60}s`,
};

function dia(dias) {
  return new Date(Date.now() + dias * 86400000).toISOString().slice(0, 10);
}
function sorteio(n) { return Math.floor(Math.random() * n); }

export function setup() {
  const r = http.post(`${BASE}/responsaveis`, JSON.stringify({
    nome: 'Responsável da carga', email: `carga-${Date.now()}@satc.edu.br` }), JSON_H);
  check(r, { 'responsável criado': (x) => x.status === 201 });
  return { responsavelId: r.json('id') };
}

export default function (dados) {
  const ok = (r, nome, ...codigos) => check(r, { [nome]: (x) => codigos.includes(x.status) });

  // criar (publica evento "criada")
  const nova = http.post(`${BASE}/tarefas`, JSON.stringify({
    titulo: `Tarefa de carga ${__VU}-${__ITER}`, responsavel: 'Ana',
    dataPrazo: dia(1 + sorteio(20)), prioridade: 1 + sorteio(5) }), JSON_H);
  ok(nova, 'POST /tarefas', 201);
  const id = nova.json('id');

  // consultar
  ok(http.get(`${BASE}/tarefas`), 'GET /tarefas', 200);
  ok(http.get(`${BASE}/tarefas/${id}`), 'GET /tarefas/{id}', 200);
  ok(http.get(`${BASE}/tarefas/buscar?responsavel=Ana`), 'GET /tarefas/buscar', 200);
  ok(http.get(`${BASE}/tarefas/atrasadas`), 'GET /tarefas/atrasadas', 200);

  // alterar
  ok(http.put(`${BASE}/tarefas/${id}`, JSON.stringify({
    titulo: 'Tarefa alterada', responsavel: 'Bruno', dataPrazo: dia(5), prioridade: 3 }), JSON_H),
    'PUT /tarefas/{id}', 200);
  ok(http.patch(`${BASE}/tarefas/${id}`, JSON.stringify({ prioridade: 5 }), JSON_H),
    'PATCH /tarefas/{id}', 200);

  // vincular e desvincular responsável
  ok(http.put(`${BASE}/tarefas/${id}/responsavel`,
    JSON.stringify({ responsavelId: dados.responsavelId }), JSON_H), 'PUT responsavel', 200);
  ok(http.del(`${BASE}/tarefas/${id}/responsavel`), 'DELETE responsavel', 200, 204);

  // concluir e reabrir (publica "concluida" e "reaberta")
  ok(http.put(`${BASE}/tarefas/${id}/concluir`), 'PUT concluir', 200);
  ok(http.put(`${BASE}/tarefas/${id}/concluir`), 'PUT reabrir', 200);

  // responsáveis
  ok(http.get(`${BASE}/responsaveis`), 'GET /responsaveis', 200);
  ok(http.get(`${BASE}/responsaveis/${dados.responsavelId}`), 'GET /responsaveis/{id}', 200);
  ok(http.get(`${BASE}/responsaveis/${dados.responsavelId}/tarefas`), 'GET responsaveis tarefas', 200);

  // cadastrar responsável, contrato OpenAPI e página estática
  if (__ITER % 10 === 0) {
    ok(http.post(`${BASE}/responsaveis`, JSON.stringify({
      nome: `Carga ${__VU}-${__ITER}`, email: `c${__VU}-${__ITER}-${Date.now()}@satc.edu.br` }), JSON_H),
      'POST /responsaveis', 201);
    ok(http.get(`${BASE}/v3/api-docs`), 'GET /v3/api-docs', 200);
    ok(http.get(`${BASE}/`), 'GET / (página)', 200);
  }

  // previsão (Open-Meteo): poucas coordenadas e datas, então há "miss" e muitos "hit" no cache
  if (__ITER % 4 === 0) {
    const lat = (-28.67 - sorteio(8) / 100).toFixed(2);
    const lon = '-49.37';
    const q = `latitude=${lat}&longitude=${lon}`;
    ok(http.get(`${BASE}/previsao?${q}&data=${dia(sorteio(3))}`), 'GET /previsao', 200, 502, 503);
    ok(http.get(`${BASE}/previsao/agora?${q}`), 'GET /previsao/agora', 200, 502, 503);
    ok(http.get(`${BASE}/tarefas/${id}/previsao?${q}`), 'GET /tarefas/{id}/previsao', 200, 409, 422, 502, 503);
    ok(http.get(`${BASE}/tarefas/chuvosas?${q}`), 'GET /tarefas/chuvosas', 200, 502, 503);
  }

  // erros de propósito (alimentam os painéis de erros 4xx)
  ok(http.get(`${BASE}/tarefas/999999999`), '404 tarefa inexistente', 404);
  ok(http.post(`${BASE}/tarefas`, JSON.stringify({ titulo: '' }), JSON_H), '400 corpo inválido', 400);
  ok(http.get(`${BASE}/previsao`), '400 sem parâmetros', 400);

  // excluir (publica "removida")
  ok(http.del(`${BASE}/tarefas/${id}`), 'DELETE /tarefas/{id}', 200, 204);
  ok(http.get(`${BASE}/tarefas/${id}`), '404 depois de excluir', 404);

  sleep(0.2);
}
