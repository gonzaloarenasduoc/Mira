// PNF-03: 10 usuarios simultáneos frente a 1, sobre las consultas más usadas. Cada usuario tiene su propia sesión.
const base = 'http://localhost:8080';
const RUTAS = ['/disponibilidad', '/arriendos', '/equipos', '/indicadores', '/capacitaciones'];
async function sesion(u, p) {
  const r1 = await fetch(base + '/login'); const c1 = r1.headers.get('set-cookie').split(';')[0];
  const html = await r1.text(); const csrf = html.match(/name="_csrf" value="([^"]+)"/)[1];
  const r2 = await fetch(base + '/login', { method: 'POST', redirect: 'manual', headers: { cookie: c1, 'content-type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ username: u, password: p, _csrf: csrf }) });
  return (r2.headers.get('set-cookie') || c1).split(';')[0];
}
const PAUSA = Number(process.argv[2] || 0);
const porRuta = {};
async function usuario(cookie, vueltas, tiempos, marca) {
  for (let v = 0; v < vueltas; v++) for (const ruta of RUTAS) {
    if (PAUSA) await new Promise(r => setTimeout(r, PAUSA * (0.5 + Math.random())));
    const t = performance.now(); const r = await fetch(base + ruta, { headers: { cookie } }); await r.text();
    if (r.status !== 200) throw new Error(ruta + ' ' + r.status);
    const ms = performance.now() - t; tiempos.push(ms);
    if (marca) ((porRuta[marca + ruta] ||= [])).push(ms);
  }
}
const p95 = (a) => { const s = [...a].sort((x, y) => x - y); return s[Math.ceil(s.length * 0.95) - 1]; };
(async () => {
  const cookies = []; for (let i = 0; i < 10; i++) cookies.push(await sesion('jefatura@mira.cl', 'jefatura123'));
  await usuario(cookies[0], 2, []);                                     // calentamiento
  const uno = []; await usuario(cookies[0], 10, uno, '1 ');
  const diez = []; const t0 = performance.now();
  await Promise.all(cookies.map(c => usuario(c, 10, diez, '10 ')));
  const seg = (performance.now() - t0) / 1000;
  const r = (x) => (x / 1000).toFixed(3) + ' s';
  console.log(`1 usuario:  ${uno.length} solicitudes · p95 ${r(p95(uno))} · promedio ${r(uno.reduce((a, b) => a + b) / uno.length)}`);
  console.log(`10 usuarios: ${diez.length} solicitudes en ${seg.toFixed(1)} s · p95 ${r(p95(diez))} · promedio ${r(diez.reduce((a, b) => a + b) / diez.length)}`);
  console.log('Pausa entre acciones: ' + PAUSA + ' ms (promedio)');
  for (const ruta of RUTAS) console.log(`  ${ruta.padEnd(16)} p95 1 usuario ${r(p95(porRuta['1 ' + ruta]))} · 10 usuarios ${r(p95(porRuta['10 ' + ruta]))}`);
  console.log(`Degradación del p95: ${((p95(diez) / p95(uno) - 1) * 100).toFixed(0)} % (criterio PNF-03: no más de 50 %)`);
})();
