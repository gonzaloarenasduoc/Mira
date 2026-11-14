# Evidencias · Semana 14

## Implantación con Docker (D28)

Prueba del 12-11-2026: `docker compose down -v` y luego `docker compose up` con la imagen
construida desde el `Dockerfile`, sobre una base vacía.

- La base pasó su chequeo de salud y la aplicación arrancó en 7 s después de aplicar las tres
  migraciones (V1 a V3).
- El contenedor corre con el usuario `mira` (uid 999), no como root, y con la hora de Santiago (-03).
- Desde el navegador se registraron una bodega, un equipo, un cliente y un arriendo, y se adjuntó
  una orden de compra en PDF.
- Después de `docker compose restart app`, el documento se descargó idéntico byte a byte: los
  adjuntos viven en el volumen `mira-uploads`, no dentro del contenedor.

| Captura | Qué muestra |
|---|---|
| [01-docker-arriendo.png](01-docker-arriendo.png) | Arriendo con su orden de compra adjunta, servido por el contenedor `mira-app` |

En el entorno de desarrollo de Claude Code la construcción necesitó la configuración del proxy de
salida de ese entorno. Esa configuración se agregó solo a una copia local del `Dockerfile` y no está
en el repositorio. En un equipo con acceso directo a internet, `docker compose up --build` funciona
sin cambios.

## Video Demo_v5 (Retrospectiva Semana 13)

[`Demo/Demo_v5.mp4`](../../../Demo/Demo_v5.mp4), 7 min, grabado el 14-11-2026 sobre `main` más los
cambios de `semana-14`:

1. **Repositorio e implantación.** Merge del PR #33 en `main`, módulos del sistema, y
   `docker compose up -d` desde cero (V1 a V3 aplicadas). Luego la carga de los datos de
   demostración con `docs/demo/cargar-demo.sh docker`.
2. **Funcionalidades** con los roles vendedor, jefatura y encargado de bodega:
   - Listado de arriendos (HU-17) y ficha con documentos y capacitación (HU-24 y HU-20).
   - Venta de un equipo (HU-18) y factura adjunta (HU-23).
   - Sugerencia de capacitación y aviso de técnico ocupado (HU-20 y HU-21).
   - Calendario, donde una capacitación se marca como realizada (HU-22).
   - Historial del cliente (HU-19) y documentos del equipo (HU-25).
   - Panel de indicadores (HU-26 a HU-28).
   - Acceso denegado al encargado de bodega (CF-11).
3. **Datos en PostgreSQL:** la venta, su movimiento, la factura (solo con su referencia, porque el
   archivo está en el volumen) y las capacitaciones.
4. **Pruebas:** `./mvnw clean verify` en vivo, con 219 pruebas sin fallas ni omitidas y una
   cobertura de servicios de 84,6%.

Para grabarlo, la imagen `mira-app` se construyó antes con la copia del `Dockerfile` que agrega el
proxy de ese entorno (ver arriba). El video lo dice en pantalla antes del `docker compose up -d`.

## Pruebas no funcionales (14-11-2026)

Medidas sobre el sistema levantado con Docker Compose y los datos de demostración. El resultado de cada
caso está en [`docs/12-matriz-pruebas.md`](../../12-matriz-pruebas.md).

| Archivo | Caso | Resultado |
|---|---|---|
| [pnf/pnf03-pausa-1s.txt](pnf/pnf03-pausa-1s.txt) | PNF-03 con pausas de 1 s | Sin degradación (−10 % en el p95) |
| [pnf/pnf03-sin-pausa.txt](pnf/pnf03-sin-pausa.txt) | PNF-03 en saturación | +371 % en el p95 (de 0,036 a 0,171 s): no cumple el criterio relativo |
| [pnf/pnf13.txt](pnf/pnf13.txt) | PNF-13 | Ningún registro parcial tras la caída de la base; el usuario ve "Ocurrió un problema" |
| [pnf/pnf14.txt](pnf/pnf14.txt) | PNF-14 | Restauración completa: mismos conteos en las 14 tablas y la misma huella de movimientos |

La primera ejecución de PNF-13 mostró la página Whitelabel de Spring en lugar de un mensaje propio, lo que
incumplía PNF-10. La plantilla de error existía con un nombre que Spring no usa (`error/error.html`). Se
renombró a `error/5xx.html` y la prueba se repitió con el mensaje correcto.
