
(() => {
  'use strict';

  const PARAMETROS = new URLSearchParams(location.search);

  // Modo portal (gateway): a página declara, antes deste script, onde fica a lista de contratos
  //   <script>window.LUMIERE_DOCS = { fontes: '/docs/specs' };</script>
  // e ganha uma aba por serviço (?servico=auth). Sem isso, é a página de um serviço só.
  const FONTES = window.LUMIERE_DOCS && window.LUMIERE_DOCS.fontes;

  let SPEC_URL = PARAMETROS.get('spec') || '/v3/api-docs';
  let SPEC = null;

  /* ---------- DOM seguro: todo texto entra como texto, nunca como HTML ---------- */

  function el(tag, props, ...kids) {
    const node = document.createElement(tag);
    for (const [chave, valor] of Object.entries(props || {})) {
      if (valor == null || valor === false) continue;
      if (chave === 'class') node.className = valor;
      else if (chave === 'text') node.textContent = valor;
      else if (chave.startsWith('on') && typeof valor === 'function') node.addEventListener(chave.slice(2), valor);
      else node.setAttribute(chave, valor === true ? '' : valor);
    }
    for (const filho of kids.flat()) {
      if (filho == null || filho === false) continue;
      node.append(filho);
    }
    return node;
  }

  /* ---------- markdown mínimo: `código`, **negrito**, parágrafos e listas ---------- */

  function inline(texto) {
    const frag = document.createDocumentFragment();
    const re = /`([^`]+)`|\*\*([^*]+)\*\*/g;
    let ultimo = 0;
    let m;
    while ((m = re.exec(texto))) {
      if (m.index > ultimo) frag.append(texto.slice(ultimo, m.index));
      frag.append(m[1] !== undefined ? el('code', { text: m[1] }) : el('strong', { text: m[2] }));
      ultimo = re.lastIndex;
    }
    if (ultimo < texto.length) frag.append(texto.slice(ultimo));
    return frag;
  }

  const LINHA_DE_STATUS = /^`(\d{3})`\s*[—–-]\s*(.*)$/;

  function blocos(texto) {
    const saida = [];
    for (const bruto of String(texto || '').trim().split(/\n\s*\n/)) {
      const linhas = bruto.split('\n').map((l) => l.trim()).filter(Boolean);
      if (!linhas.length) continue;

      if (linhas.every((l) => l.startsWith('- '))) {
        const itens = linhas.map((l) => l.slice(2));
        const sao_status = itens.every((i) => LINHA_DE_STATUS.test(i));
        saida.push(el('ul', { class: sao_status ? 'erros' : null }, itens.map((item) => {
          const s = item.match(LINHA_DE_STATUS);
          return s
            ? el('li', null, chipStatus(s[1]), el('span', null, inline(s[2])))
            : el('li', null, inline(item));
        })));
      } else {
        saida.push(el('p', null, inline(linhas.join(' '))));
      }
    }
    return saida;
  }

  /* ---------- esquemas: tipos, regras e exemplos ---------- */

  const nomeDaRef = (s) => (s && s.$ref ? s.$ref.split('/').pop() : null);
  const resolver = (s) => {
    const nome = nomeDaRef(s);
    return nome ? (SPEC.components?.schemas?.[nome] ?? {}) : s;
  };

  const TIPOS = { string: 'texto', integer: 'inteiro', number: 'decimal', boolean: 'booleano', object: 'objeto' };

  function tipoNode(s) {
    if (!s) return el('span', { class: 'tipo', text: '—' });
    const nome = nomeDaRef(s);
    if (nome) return el('a', { class: 'tipo tipo-ref', href: '#modelo-' + nome, text: nome });
    if (s.type === 'array') return el('span', { class: 'tipo' }, 'lista de ', tipoNode(s.items));
    if (s.additionalProperties && typeof s.additionalProperties === 'object') {
      return el('span', { class: 'tipo' }, 'mapa de ', tipoNode(s.additionalProperties));
    }
    if (s.format === 'date-time') return el('span', { class: 'tipo', text: 'data e hora' });
    if (s.format === 'date') return el('span', { class: 'tipo', text: 'data' });
    return el('span', { class: 'tipo', text: TIPOS[s.type] || s.type || 'objeto' });
  }

  function regras(s) {
    const lista = [];
    if (!s) return lista;
    if (s.enum) lista.push('valores: ' + s.enum.join(' | '));
    if (s.default !== undefined) lista.push('padrão: ' + s.default);
    if (s.minimum !== undefined) lista.push('mín. ' + s.minimum);
    if (s.maximum !== undefined) lista.push('máx. ' + s.maximum);
    if (s.minLength !== undefined) lista.push('mín. ' + s.minLength + ' caracteres');
    if (s.maxLength !== undefined) lista.push('máx. ' + s.maxLength + ' caracteres');
    if (s.pattern) lista.push('formato: ' + s.pattern);
    return lista;
  }

  function exemplo(schema, nivel = 0) {
    const s = resolver(schema);
    if (!s || nivel > 5) return null;
    if (s.example !== undefined) return s.example;
    if (s.enum) return s.enum[0];
    if (s.type === 'array' || s.items) return [exemplo(s.items, nivel + 1)];
    if (s.properties) {
      return Object.fromEntries(Object.entries(s.properties).map(([k, v]) => [k, exemplo(v, nivel + 1)]));
    }
    if (s.additionalProperties && typeof s.additionalProperties === 'object') {
      return { chave: exemplo(s.additionalProperties, nivel + 1) };
    }
    switch (s.type) {
      case 'string': return s.format === 'date-time' ? '2026-01-01T00:00:00Z' : s.format === 'date' ? '2026-01-01' : 'texto';
      case 'integer':
      case 'number': return 0;
      case 'boolean': return true;
      default: return null;
    }
  }

  /* ---------- peças visuais ---------- */

  function chipStatus(codigo) {
    const classe = { 2: 'status-2', 4: 'status-4', 5: 'status-5' }[String(codigo)[0]] || 'status-x';
    return el('span', { class: 'status ' + classe, text: String(codigo) });
  }

  function caminhoNode(caminho) {
    return el('code', { class: 'caminho' },
      caminho.split(/(\{[^}]+\})/).filter(Boolean).map((parte) =>
        parte.startsWith('{') ? el('span', { class: 'param', text: parte }) : parte));
  }

  function jsonNode(valor) {
    const texto = typeof valor === 'string' ? valor : JSON.stringify(valor, null, 2);
    const pre = el('pre', { class: 'json' });
    const re = /("(?:\\.|[^"\\])*")(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?/g;
    let ultimo = 0;
    let m;
    while ((m = re.exec(texto))) {
      if (m.index > ultimo) pre.append(texto.slice(ultimo, m.index));
      if (m[1] !== undefined) {
        pre.append(el('span', { class: m[2] ? 'j-chave' : 'j-texto', text: m[1] }));
        if (m[2]) pre.append(m[2]);
      } else if (m[3]) {
        pre.append(el('span', { class: 'j-lit', text: m[0] }));
      } else {
        pre.append(el('span', { class: 'j-num', text: m[0] }));
      }
      ultimo = re.lastIndex;
    }
    if (ultimo < texto.length) pre.append(texto.slice(ultimo));
    return pre;
  }

  const schemaDaResposta = (r) => Object.values(r?.content || {})[0]?.schema;

  /* ---------- tabela de parâmetros ---------- */

  function tabelaParametros(parametros) {
    const linhas = parametros.map((p) => {
      const sch = resolver(p.schema) || {};
      const itensRegra = regras(sch);
      return el('tr', null,
        el('td', null, p.name, p.required && el('span', { class: 'obrigatorio', text: 'obrigatório' })),
        el('td', { text: p.in === 'path' ? 'caminho' : p.in === 'query' ? 'query' : p.in }),
        el('td', null, tipoNode(p.schema)),
        el('td', null, el('div', { class: 'regras' }, itensRegra.map((r) => el('span', { class: 'regra', text: r })))),
        el('td', null, inline(p.description || '')));
    });
    return el('div', { class: 'tabela-wrap' }, el('table', null,
      el('thead', null, el('tr', null, ['Nome', 'Em', 'Tipo', 'Regras', 'Descrição'].map((t) => el('th', { text: t })))),
      el('tbody', null, linhas)));
  }

  /* ---------- respostas ---------- */

  function listaRespostas(respostas) {
    return el('div', { class: 'respostas' }, Object.entries(respostas || {}).map(([codigo, r]) => {
      const schema = schemaDaResposta(r);
      const ehSucesso = String(codigo).startsWith('2');
      return el('div', { class: 'resposta' },
        el('div', { class: 'resposta-linha' },
          chipStatus(codigo),
          el('span', { class: 'desc', text: r.description || '' }),
          schema && tipoNode(schema)),
        ehSucesso && schema && el('details', null,
          el('summary', { text: 'Exemplo de resposta' }),
          jsonNode(exemplo(schema))));
    }));
  }

  /* ---------- token: o que o login devolve vale para todos os endpoints protegidos da página ---------- */

  const CHAVE_TOKEN = 'lumiere-docs-token';
  const camposDeToken = new Set();

  function tokenGuardado() {
    try { return sessionStorage.getItem(CHAVE_TOKEN) || ''; } catch { return ''; }
  }

  function guardarToken(token, origem) {
    try {
      if (token) sessionStorage.setItem(CHAVE_TOKEN, token);
      else sessionStorage.removeItem(CHAVE_TOKEN);
    } catch { /* sem sessionStorage: o campo continua funcionando, só não é lembrado */ }
    for (const campo of camposDeToken) {
      if (campo !== origem && campo.value !== token) campo.value = token;
    }
  }

  /* ---------- testador: monta a chamada de verdade e mostra o que voltou ---------- */

  function testador(caminho, metodo, operacao) {
    const parametros = (operacao.parameters || []).filter((p) => p.in === 'path' || p.in === 'query');
    const entradas = new Map();
    const corpoSchema = schemaDaResposta(operacao.requestBody);
    let areaCorpo = null;
    let campoToken = null;

    const campos = parametros.map((p) => {
      const sch = resolver(p.schema) || {};
      let entrada;
      if (sch.enum) {
        entrada = el('select', { name: p.name },
          !p.required && el('option', { value: '', text: '(não enviar)' }),
          sch.enum.map((v) => el('option', { value: v, text: v })));
      } else {
        entrada = el('input', {
          type: 'text',
          name: p.name,
          placeholder: String(p.example ?? sch.default ?? ''),
          autocomplete: 'off',
          spellcheck: 'false',
        });
        if (p.required && p.example !== undefined) entrada.value = String(p.example);
      }
      entradas.set(p.name, entrada);
      const dica = regras(sch).filter((r) => !r.startsWith('valores:'));
      return el('label', { class: 'campo' },
        el('span', { class: 'campo-nome' }, p.name, p.required && el('em', { text: ' *' })),
        entrada,
        dica.length > 0 && el('span', { class: 'campo-dica', text: dica.join(' · ') }));
    });

    if (corpoSchema) {
      areaCorpo = el('textarea', { name: 'corpo', spellcheck: 'false' });
      areaCorpo.value = JSON.stringify(exemplo(corpoSchema), null, 2);
      campos.push(el('label', { class: 'campo largo' }, el('span', { class: 'campo-nome', text: 'corpo (JSON)' }), areaCorpo));
    }

    if (exigeToken(operacao)) {
      campoToken = el('input', {
        type: 'text',
        name: 'token',
        placeholder: 'cole o token do login (ou faça login aqui na página)',
        autocomplete: 'off',
        spellcheck: 'false',
      });
      campoToken.value = tokenGuardado();
      campoToken.addEventListener('input', () => guardarToken(campoToken.value.trim(), campoToken));
      camposDeToken.add(campoToken);
      campos.push(el('label', { class: 'campo largo' },
        el('span', { class: 'campo-nome' }, 'token (Bearer)', el('em', { text: ' *' })),
        campoToken,
        el('span', { class: 'campo-dica', text: 'Preenchido sozinho quando você faz login nesta página. Fica guardado só nesta aba.' })));
    }

    const resultado = el('div', { class: 'resultado', hidden: true });
    const botaoEnviar = el('button', { type: 'submit', class: 'primario', text: 'Enviar requisição' });
    const botaoCurl = el('button', { type: 'button', class: 'secundario', text: 'Copiar cURL' });

    function montarUrl() {
      let url = caminho;
      const consulta = new URLSearchParams();
      for (const p of parametros) {
        const valor = entradas.get(p.name).value.trim();
        if (p.in === 'path') url = url.replace('{' + p.name + '}', encodeURIComponent(valor));
        else if (valor !== '') consulta.set(p.name, valor);
      }
      const q = consulta.toString();
      return url + (q ? '?' + q : '');
    }

    botaoCurl.addEventListener('click', async () => {
      let cmd = `curl -X ${metodo.toUpperCase()} "${location.origin}${montarUrl()}"`;
      if (campoToken && campoToken.value.trim()) cmd += ` -H "Authorization: Bearer ${campoToken.value.trim()}"`;
      if (areaCorpo) cmd += ` -H "Content-Type: application/json" -d '${areaCorpo.value.replace(/\s*\n\s*/g, ' ')}'`;
      try {
        await navigator.clipboard.writeText(cmd);
        botaoCurl.textContent = 'Copiado!';
      } catch {
        botaoCurl.textContent = 'Não foi possível copiar';
      }
      setTimeout(() => { botaoCurl.textContent = 'Copiar cURL'; }, 1600);
    });

    const form = el('form', { class: 'testador', onsubmit: async (evento) => {
      evento.preventDefault();
      const url = montarUrl();
      botaoEnviar.disabled = true;
      botaoEnviar.textContent = 'Enviando…';
      const inicio = performance.now();
      resultado.replaceChildren();
      try {
        const opcoes = { method: metodo.toUpperCase(), headers: {} };
        if (areaCorpo) {
          opcoes.headers['Content-Type'] = 'application/json';
          opcoes.body = areaCorpo.value;
        }
        if (campoToken && campoToken.value.trim()) {
          opcoes.headers.Authorization = 'Bearer ' + campoToken.value.trim();
        }
        const resposta = await fetch(url, opcoes);
        const texto = await resposta.text();
        const ms = Math.round(performance.now() - inicio);

        let json = null;
        try { json = JSON.parse(texto); } catch { /* corpo não é JSON: mostra como texto */ }
        const corpo = json !== null
          ? jsonNode(JSON.stringify(json, null, 2))
          : el('pre', { class: 'texto-puro', text: texto || '(corpo vazio)' });

        // resposta de login: guarda o token para os endpoints protegidos da página
        const guardouToken = resposta.ok && json && typeof json.token === 'string';
        if (guardouToken) guardarToken(json.token);

        resultado.append(
          el('div', { class: 'resultado-cab' }, chipStatus(resposta.status),
            el('span', { text: ms + ' ms' }), el('span', { class: 'resultado-url', text: url }),
            guardouToken && el('span', { class: 'resultado-aviso', text: 'token guardado: os endpoints protegidos já usam' })),
          corpo);
      } catch (erro) {
        resultado.append(el('div', { class: 'falha', text: 'Não consegui falar com o servidor: ' + erro.message }));
      } finally {
        resultado.hidden = false;
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = 'Enviar requisição';
      }
    } },
      el('div', { class: 'campos' }, campos),
      el('div', { class: 'acoes' }, botaoEnviar, botaoCurl),
      resultado);

    return el('details', { class: 'testar' }, el('summary', { text: 'Testar este endpoint' }), form);
  }

  /* ---------- seções ---------- */

  const slug = (t) => t.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');

  // o contrato marca endpoint protegido com "security" (no código: @SecurityRequirement)
  const exigeToken = (op) => Array.isArray(op.security) && op.security.length > 0;

  function cartaoEndpoint(caminho, metodo, op) {
    const id = 'op-' + (op.operationId || slug(metodo + caminho));
    return el('article', { class: 'endpoint', id },
      el('header', { class: 'endpoint-topo' },
        el('span', { class: 'metodo metodo-' + metodo, text: metodo.toUpperCase() }),
        caminhoNode(caminho),
        exigeToken(op) && el('span', { class: 'selo-protegido', text: 'requer token' }),
        el('p', { class: 'endpoint-resumo', text: op.summary || '' })),
      el('div', { class: 'endpoint-corpo' },
        op.description && el('div', { class: 'descricao' }, blocos(op.description)),
        (op.parameters || []).length > 0 && [el('h4', { text: 'Parâmetros' }), tabelaParametros(op.parameters)],
        el('h4', { text: 'Respostas' }),
        listaRespostas(op.responses),
        testador(caminho, metodo, op)));
  }

  function cartaoModelo(nome, schema) {
    const propriedades = Object.entries(schema.properties || {});
    return el('article', { class: 'modelo', id: 'modelo-' + nome },
      el('h3', { text: nome }),
      schema.description && el('div', { class: 'descricao' }, blocos(schema.description)),
      propriedades.length > 0 && el('div', { class: 'tabela-wrap' }, el('table', null,
        el('thead', null, el('tr', null, ['Campo', 'Tipo', 'Descrição', 'Exemplo'].map((t) => el('th', { text: t })))),
        el('tbody', null, propriedades.map(([campo, s]) => {
          const real = resolver(s) || {};
          const ex = s.example ?? real.example;
          return el('tr', null,
            el('td', { text: campo }),
            el('td', null, tipoNode(s), real.enum && el('div', { class: 'regras' }, regras(real).map((r) => el('span', { class: 'regra', text: r })))),
            el('td', null, inline(s.description || real.description || '')),
            el('td', null, ex !== undefined && el('code', { text: typeof ex === 'string' ? ex : JSON.stringify(ex) })));
        })))));
  }

  // separa "intro" das seções "## Título" do texto de descrição
  function separarSecoes(descricao) {
    const partes = String(descricao || '').split(/^## (.+)$/m);
    const secoes = [];
    for (let i = 1; i < partes.length; i += 2) secoes.push({ titulo: partes[i].trim(), corpo: partes[i + 1] });
    return { intro: partes[0], secoes };
  }

  /* ---------- montagem da página ---------- */

  function montar() {
    const info = SPEC.info || {};
    const [produto, servico] = (info.title || 'API').split(/\s+[—–-]\s+/);
    const { intro, secoes } = separarSecoes(info.description);
    const app = document.getElementById('app');
    const nav = document.getElementById('navegacao');
    document.title = `${produto}${servico ? ' · ' + servico : ''} · Documentação`;

    const grupos = [];
    const itensFiltraveis = [];
    const observados = [];

    const hero = el('header', { class: 'hero', id: 'visao-geral' },
      el('p', { class: 'sobretitulo', text: `${produto} · Documentação da API` }),
      el('h1', { text: servico || produto }),
      el('div', { class: 'descricao' }, blocos(intro)),
      el('div', { class: 'selos' },
        info.version && el('span', { class: 'selo' }, 'versão ', el('b', { text: info.version })),
        el('span', { class: 'selo' }, 'base ', el('b', { text: location.origin })),
        el('a', { class: 'selo', href: SPEC_URL, target: '_blank', rel: 'noopener' }, 'contrato ', el('b', { text: 'OpenAPI JSON' }))));
    app.replaceChildren(hero);
    observados.push(hero);

    const introLinks = [{ id: 'visao-geral', rotulo: 'Visão geral' }];

    for (const secao of secoes) {
      const id = slug(secao.titulo);
      const bloco = el('section', { class: 'secao', id },
        el('h2', { text: secao.titulo }),
        el('div', { class: 'descricao' }, blocos(secao.corpo)));
      app.append(bloco);
      observados.push(bloco);
      introLinks.push({ id, rotulo: secao.titulo });
    }
    grupos.push({ titulo: 'Introdução', links: introLinks.map((l) => ({ ...l, href: '#' + l.id })) });

    // endpoints agrupados por tag, na ordem em que o contrato declara as tags
    const operacoes = [];
    for (const [caminho, item] of Object.entries(SPEC.paths || {})) {
      for (const [metodo, op] of Object.entries(item)) {
        if (!['get', 'post', 'put', 'patch', 'delete'].includes(metodo)) continue;
        operacoes.push({ caminho, metodo, op });
      }
    }
    const ordemDeTags = (SPEC.tags || []).map((t) => t.name);
    const porTag = new Map(ordemDeTags.map((t) => [t, []]));
    for (const o of operacoes) {
      const tag = (o.op.tags || [])[0] || 'Endpoints';
      if (!porTag.has(tag)) porTag.set(tag, []);
      porTag.get(tag).push(o);
    }

    for (const [tag, lista] of porTag) {
      if (!lista.length) continue;
      const descTag = (SPEC.tags || []).find((t) => t.name === tag)?.description;
      const secao = el('section', { class: 'secao', id: 'tag-' + slug(tag) },
        el('h2', { text: tag }),
        descTag && el('p', { class: 'lead', text: descTag }));
      const links = [];
      for (const { caminho, metodo, op } of lista) {
        const cartao = cartaoEndpoint(caminho, metodo, op);
        secao.append(cartao);
        observados.push(cartao);
        links.push({ id: cartao.id, href: '#' + cartao.id, metodo, caminho, rotulo: caminho.replace(/^\/[^/]+/, '') || caminho });
        itensFiltraveis.push({ cartao, id: cartao.id, texto: `${metodo} ${caminho} ${op.summary || ''} ${op.description || ''}`.toLowerCase() });
      }
      app.append(secao);
      grupos.push({ titulo: tag, links });
    }

    // modelos
    const modelos = Object.entries(SPEC.components?.schemas || {});
    if (modelos.length) {
      const secao = el('section', { class: 'secao', id: 'modelos' },
        el('h2', { text: 'Modelos' }),
        el('p', { class: 'lead', text: 'Os formatos de dados que entram e saem da API.' }));
      for (const [nome, schema] of modelos) {
        const cartao = cartaoModelo(nome, schema);
        secao.append(cartao);
        observados.push(cartao);
      }
      app.append(secao);
      grupos.push({ titulo: 'Modelos', links: modelos.map(([nome]) => ({ id: 'modelo-' + nome, href: '#modelo-' + nome, rotulo: nome })) });
    }

    app.append(el('footer', { class: 'rodape' },
      `${produto} · documentação gerada a partir do contrato OpenAPI do serviço (`,
      el('a', { href: SPEC_URL, target: '_blank', rel: 'noopener', text: SPEC_URL }), ').'));

    montarNavegacao(nav, grupos, itensFiltraveis);
    acompanharRolagem(observados);
    if (location.hash) document.getElementById(decodeURIComponent(location.hash.slice(1)))?.scrollIntoView();
  }

  function montarNavegacao(nav, grupos, itensFiltraveis) {
    const mapaDeLinks = new Map();
    nav.replaceChildren(...grupos.map((g) => el('div', { class: 'nav-grupo' },
      el('h4', { text: g.titulo }),
      g.links.map((l) => {
        const link = el('a', { class: 'nav-link', href: l.href },
          l.metodo && el('span', { class: 'metodo metodo-' + l.metodo, text: l.metodo.toUpperCase() }),
          el('span', { class: l.metodo ? 'caminho-curto' : null, text: l.rotulo }));
        mapaDeLinks.set(l.id, link);
        return link;
      }))));

    document.getElementById('filtro').addEventListener('input', (e) => {
      const termo = e.target.value.trim().toLowerCase();
      for (const item of itensFiltraveis) {
        const visivel = !termo || item.texto.includes(termo);
        item.cartao.hidden = !visivel;
        mapaDeLinks.get(item.id).hidden = !visivel;
      }
    });

    nav.mapaDeLinks = mapaDeLinks;
  }

  function acompanharRolagem(observados) {
    const mapa = document.getElementById('navegacao').mapaDeLinks;
    const observador = new IntersectionObserver((entradas) => {
      for (const entrada of entradas) {
        if (!entrada.isIntersecting) continue;
        for (const link of mapa.values()) link.classList.remove('ativo');
        const ativo = mapa.get(entrada.target.id);
        if (ativo) {
          ativo.classList.add('ativo');
          ativo.scrollIntoView({ block: 'nearest' });
        }
      }
    }, { rootMargin: '-15% 0px -75% 0px' });
    observados.forEach((o) => observador.observe(o));
  }

  /* ---------- início ---------- */

  // Lê o JSON de um endereço; se falhar, o erro carrega a "mensagem" do corpo quando houver
  // (o gateway explica por que um serviço não respondeu: fora do ar, lento demais...).
  async function lerJson(url) {
    const resposta = await fetch(url);
    if (!resposta.ok) {
      let detalhe = '';
      try { detalhe = (await resposta.json()).mensagem || ''; } catch { /* corpo sem JSON */ }
      throw new Error('HTTP ' + resposta.status + (detalhe ? ' — ' + detalhe : ''));
    }
    return resposta.json();
  }

  function montarAbas(fontes, atual) {
    const abas = el('nav', { class: 'servicos', 'aria-label': 'Serviços' },
      fontes.map((f) => el('a', {
        class: 'servico-aba' + (f.id === atual.id ? ' ativa' : ''),
        href: '?servico=' + encodeURIComponent(f.id),
        'aria-current': f.id === atual.id ? 'page' : null,
        text: f.rotulo,
      })));
    document.querySelector('.filtro').before(abas);
  }

  async function iniciar() {
    if (FONTES) {
      const fontes = await lerJson(FONTES);
      const atual = fontes.find((f) => f.id === PARAMETROS.get('servico')) || fontes[0];
      montarAbas(fontes, atual); // as abas aparecem mesmo que o contrato deste serviço falhe, para dar para trocar
      SPEC_URL = atual.url;
    }
    SPEC = await lerJson(SPEC_URL);
    montar();
  }

  iniciar().catch((erro) => {
    document.getElementById('app').replaceChildren(
      el('p', { class: 'falha' }, `Não consegui carregar o contrato em ${SPEC_URL} (${erro.message}). O serviço está no ar?`));
  });
})();
