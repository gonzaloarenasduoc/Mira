// PNF-13: la base se cae durante el registro de un arriendo de dos equipos.
const { chromium } = require('playwright');
const { execSync } = require('child_process');
const sh = (c) => execSync(c, { cwd: '/home/user/Mira', stdio: ['ignore', 'pipe', 'pipe'] }).toString().trim();
const psql = (q) => sh(`docker compose exec -T db psql -U mira mira -At -c "${q}"`);
const estado = () => psql("select (select count(*) from operacion) || ' operaciones, ' || (select count(*) from operacion_detalle) || ' líneas, ' || (select count(*) from movimiento) || ' movimientos; ' || string_agg(numero_serie || '=' || estado, ', ' order by numero_serie) from equipo where numero_serie in ('AX10M-240311','DLS03-180209')");
(async () => {
  const b = await chromium.launch(); const page = await b.newPage();
  const base = 'http://localhost:8080';
  await page.goto(base + '/login'); await page.fill('#username', 'vendedor@mira.cl'); await page.fill('#password', 'vendedor123'); await page.click('main button[type=submit]');
  await page.goto(base + '/arriendos/nuevo');
  await page.selectOption('#clienteId', { index: 2 });
  await page.fill('#fechaRetornoComprometida', new Date(Date.now() + 10 * 864e5).toISOString().slice(0, 10));
  for (const s of ['AX10M-240311', 'DLS03-180209']) await page.locator('#tablaEquipos .fila-equipo', { hasText: s }).locator('input[type=checkbox]').check();
  console.log('Antes:   ' + estado());
  sh('docker compose pause db');
  console.log('Base congelada (docker compose pause db); se envía el arriendo de AX10M-240311 y DLS03-180209');
  const envio = page.click('main button[type=submit]', { timeout: 120000 }).then(() => 'respondió').catch(e => 'sin respuesta: ' + e.message.split('\n')[0]);
  await new Promise(r => setTimeout(r, 3000));
  sh('docker compose kill db');
  console.log('Base detenida a la fuerza en medio de la operación (docker compose kill db)');
  console.log('Navegador: ' + await envio + ' · página: ' + (await page.locator('h1').first().innerText().catch(() => '?')));
  sh('docker compose start db');
  for (let i = 0; i < 30; i++) { try { sh('docker compose exec -T db pg_isready -U mira -d mira'); break; } catch { await new Promise(r => setTimeout(r, 1000)); } }
  console.log('Base restablecida (docker compose start db)');
  console.log('Después: ' + estado());
  const huerfanas = psql("select count(*) from operacion o where not exists (select 1 from operacion_detalle d where d.operacion_id = o.id)");
  console.log('Operaciones sin líneas: ' + huerfanas);
  await page.goto(base + '/equipos?texto=AX10M-240311');
  console.log('La aplicación sigue atendiendo: ' + (await page.locator('.estado').first().innerText()));
  await b.close();
})();
