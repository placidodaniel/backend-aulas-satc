// =====================================================================
// AULA 15: a sessão no navegador e o menu do sistema.
// =====================================================================
//
// Este arquivo é carregado no <head> de TODAS as páginas, antes do script
// de cada uma. Ele faz quatro coisas:
//
//   1. Guarda o token que o login devolveu (sessionStorage).
//   2. Manda quem não entrou para a tela de login.
//   3. Acrescenta "Authorization: Bearer <token>" em todo fetch() para a
//      nossa API, sem precisar mudar o código das páginas antigas.
//   4. Desenha o menu no topo da página.
//
// Onde guardar o token? Aqui usamos o sessionStorage: some quando a aba é
// fechada e não é enviado sozinho pelo navegador (por isso não há CSRF).
// O preço: qualquer JavaScript rodando na página consegue ler. Por isso o
// cuidado com XSS (textContent em vez de innerHTML) e a CSP que a API manda.
// A alternativa, um cookie HttpOnly, esconde o token do JavaScript, mas
// volta a exigir proteção contra CSRF. Veja SEGURANCA.md.
(function () {
  'use strict';

  const CHAVE = 'tarefas.sessao';
  const PAGINAS_PUBLICAS = ['entrar.html', 'criar-conta.html'];

  // A página atual ("index.html", "conta.html"...). A raiz "/" é o index.html.
  const paginaAtual = (location.pathname.split('/').pop() || 'index.html');
  const publica = PAGINAS_PUBLICAS.includes(paginaAtual);

  // Aberta como arquivo (file://), a página chama a API em localhost:8080.
  const API = location.protocol === 'file:' ? 'http://localhost:8080' : location.origin;

  // ------------------------------------------------------------------ sessão

  function ler() {
    try {
      const sessao = JSON.parse(sessionStorage.getItem(CHAVE));
      // Token vencido é o mesmo que não ter token (a API recusaria com 401).
      if (!sessao || !sessao.token || new Date(sessao.expiraEm) <= new Date()) return null;
      return sessao;
    } catch (e) {
      return null;
    }
  }

  // Grava o que veio de POST /auth/login (ou /auth/login/2fa).
  function salvar(resposta) {
    sessionStorage.setItem(CHAVE, JSON.stringify({
      token: resposta.token,
      expiraEm: resposta.expiraEm,
      usuario: resposta.usuario,
    }));
  }

  // Atualiza só os dados do usuário (ex.: depois de ativar o 2FA).
  function atualizarUsuario(usuario) {
    const sessao = ler();
    if (!sessao) return;
    sessao.usuario = usuario;
    sessionStorage.setItem(CHAVE, JSON.stringify(sessao));
    preencherUsuarioNoMenu(usuario);
  }

  // Sair = esquecer o token. O JWT continua válido até expirar (a API não guarda
  // sessão); por isso a validade é curta. Veja "revogação" no SEGURANCA.md.
  function sair(motivo) {
    sessionStorage.removeItem(CHAVE);
    irParaLogin(motivo);
  }

  function irParaLogin(motivo) {
    const parametros = new URLSearchParams();
    // Token vencido: depois do login, volta para onde estava. Saiu de propósito: começa do início.
    if (!publica && motivo !== 'saiu') parametros.set('voltar', paginaAtual);
    if (motivo) parametros.set('motivo', motivo);
    location.replace('entrar.html' + (parametros.toString() ? '?' + parametros : ''));
  }

  // Para onde voltar depois do login. Só aceitamos o NOME de uma página nossa:
  // "?voltar=https://site-falso.com" não pode virar um redirecionamento para fora
  // (o ataque se chama "open redirect" e é muito usado em phishing).
  function destinoDepoisDoLogin() {
    const voltar = new URLSearchParams(location.search).get('voltar');
    return /^[a-z0-9-]+\.html$/.test(voltar || '') && !PAGINAS_PUBLICAS.includes(voltar) ? voltar : 'index.html';
  }

  // ------------------------------------------------------------------ fetch com token

  // Guardamos o fetch original e colocamos uma "capa" por cima dele.
  const fetchOriginal = window.fetch.bind(window);

  window.fetch = async function (recurso, opcoes = {}) {
    const url = new URL(typeof recurso === 'string' ? recurso : recurso.url, location.href);
    const paraNossaApi = url.origin === API;
    const sessao = ler();

    // O token só vai para a NOSSA API. Nunca para outro endereço: um fetch para
    // um site de terceiros levaria a "chave da casa" junto.
    if (paraNossaApi && sessao) {
      const cabecalhos = new Headers(opcoes.headers || (recurso instanceof Request ? recurso.headers : undefined));
      if (!cabecalhos.has('Authorization')) cabecalhos.set('Authorization', 'Bearer ' + sessao.token);
      opcoes = { ...opcoes, headers: cabecalhos };
    }

    const resposta = await fetchOriginal(recurso, opcoes);

    // 401 numa rota que não é de login = token vencido ou recusado: volta ao login.
    if (resposta.status === 401 && paraNossaApi && !url.pathname.startsWith('/auth/login') && !publica) {
      sair('expirou');
    }
    return resposta;
  };

  // ------------------------------------------------------------------ porteiro

  if (!publica && !ler()) {
    irParaLogin();
  }

  // ------------------------------------------------------------------ menu

  const ITENS = [
    { href: 'index.html', texto: '🗂️ Tarefas' },
    { href: 'responsaveis.html', texto: '👥 Responsáveis' },
    { href: 'assistente.html', texto: '🤖 Assistente' },
    { href: 'mcp.html', texto: '🔌 Cliente MCP' },
    { href: 'contrato.html', texto: '📜 Contrato' },
    { href: 'swagger-ui.html', texto: '📘 Swagger', novaAba: true },
  ];

  const ESTILO_DO_MENU = `
    .menu-sistema { position: sticky; top: 0; z-index: 50; height: 48px; display: flex; align-items: center;
      gap: 4px; padding: 0 16px; background: #0b0f2b; border-bottom: 1px solid #2a3170;
      font-family: -apple-system, "Segoe UI", Roboto, Arial, sans-serif; font-size: 14px; overflow-x: auto; }
    .menu-sistema .marca { color: #fff; font-weight: 700; margin-right: 12px; white-space: nowrap; text-decoration: none; }
    .menu-sistema a.item { color: #CADCFC; text-decoration: none; padding: 6px 10px; border-radius: 8px; white-space: nowrap; }
    .menu-sistema a.item:hover { background: rgba(255,255,255,.08); }
    .menu-sistema a.item.ativo { background: #1E2761; color: #fff; box-shadow: inset 0 -2px 0 #6FE0C7; }
    .menu-sistema .espaco { flex: 1; }
    .menu-sistema .usuario { color: #fff; text-decoration: none; padding: 6px 10px; border-radius: 8px; white-space: nowrap; }
    .menu-sistema .usuario:hover, .menu-sistema .usuario.ativo { background: #1E2761; }
    .menu-sistema .selo { font-size: 11px; padding: 2px 8px; border-radius: 999px; margin-left: 6px; }
    .menu-sistema .selo.sim { background: rgba(111,224,199,.15); color: #6FE0C7; border: 1px solid #2f7d6a; }
    .menu-sistema .selo.nao { background: rgba(245,198,107,.12); color: #F5C66B; border: 1px solid #6b5a2a; }
    .menu-sistema button { margin-left: 8px; background: transparent; color: #ffb4a8; border: 1px solid #7a3a33;
      border-radius: 8px; padding: 6px 12px; cursor: pointer; font: inherit; white-space: nowrap; }
    .menu-sistema button:hover { background: rgba(192,57,43,.2); }
    /* As páginas antigas ocupam 100vh: descontamos a altura do menu. */
    @media (min-width: 901px) { body > .container { height: calc(100vh - 48px); } }`;

  function desenharMenu() {
    const sessao = ler();
    if (publica || !sessao) return;

    const estilo = document.createElement('style');
    estilo.textContent = ESTILO_DO_MENU;
    document.head.appendChild(estilo);

    const nav = document.createElement('nav');
    nav.className = 'menu-sistema';
    nav.setAttribute('aria-label', 'Menu do sistema');

    const marca = document.createElement('a');
    marca.className = 'marca';
    marca.href = 'index.html';
    marca.textContent = '✅ Tarefas SATC';
    nav.appendChild(marca);

    for (const item of ITENS) {
      const a = document.createElement('a');
      a.className = 'item' + (item.href === paginaAtual ? ' ativo' : '');
      a.href = item.href;
      a.textContent = item.texto;
      if (item.novaAba) { a.target = '_blank'; a.rel = 'noopener'; }
      nav.appendChild(a);
    }

    const espaco = document.createElement('span');
    espaco.className = 'espaco';
    nav.appendChild(espaco);

    // O nome do usuário leva para "Minha conta". textContent: o nome foi digitado
    // por uma pessoa no cadastro e NUNCA entra na página como HTML.
    const usuario = document.createElement('a');
    usuario.className = 'usuario' + (paginaAtual === 'conta.html' ? ' ativo' : '');
    usuario.href = 'conta.html';
    usuario.title = 'Minha conta e verificação em duas etapas';
    usuario.id = 'menu-usuario';
    nav.appendChild(usuario);

    const botaoSair = document.createElement('button');
    botaoSair.type = 'button';
    botaoSair.textContent = 'Sair';
    botaoSair.addEventListener('click', () => sair('saiu'));
    nav.appendChild(botaoSair);

    document.body.prepend(nav);
    preencherUsuarioNoMenu(sessao.usuario);
  }

  // Nome e selo do 2FA no menu. Separado para "Minha conta" atualizar sem recarregar.
  function preencherUsuarioNoMenu(dados) {
    const usuario = document.getElementById('menu-usuario');
    if (!usuario) return;
    usuario.textContent = '👤 ' + (dados ? dados.nome : 'Minha conta');
    const selo = document.createElement('span');
    const comDoisFatores = dados && dados.segundoFatorAtivo;
    selo.className = 'selo ' + (comDoisFatores ? 'sim' : 'nao');
    selo.textContent = comDoisFatores ? '2FA ligado' : '2FA desligado';
    usuario.appendChild(selo);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', desenharMenu);
  } else {
    desenharMenu();
  }

  // O que as páginas de login, cadastro e conta usam.
  window.Sessao = { ler, salvar, atualizarUsuario, sair, destinoDepoisDoLogin, API };
})();
