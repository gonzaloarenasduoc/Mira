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
