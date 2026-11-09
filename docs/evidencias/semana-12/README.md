# Evidencias de la semana 12

Capturas de HU-17 (Sprint 4), tomadas con un navegador automatizado sobre la aplicación levantada
contra PostgreSQL 16, con los arriendos registrados en la demostración de la semana 11. Fecha de la
consulta: 09-11-2026.

## Listado de arriendos con su total (HU-17, D18)

| Captura | Qué muestra |
|---|---|
| [01-listado-arriendos.png](01-listado-arriendos.png) | Listado con cliente, fechas, equipos, estado y total |
| [02-ficha-desde-listado.png](02-ficha-desde-listado.png) | Desde el listado se abre la ficha del arriendo |
| [03-listado-jefatura.png](03-listado-jefatura.png) | La jefatura consulta el listado sin el botón para registrar arriendos |

Totales verificados a mano con la regla de D18:

| Arriendo | Estado | Cálculo | Total |
|---|---|---|---|
| N° 3 | Vigente | 14 días (02-11 al 16-11, fecha comprometida) × $ 165.000 | $ 2.310.000 estimado |
| N° 2 | Vencido | 12 días (28-10 al 09-11, hoy, porque venció el 04-11) × $ 120.000 | $ 1.440.000 estimado |
| N° 1 | Cerrado | 13 días (20-10 al 02-11, retorno real) × $ 15.000 | $ 195.000 definitivo |

## Registro de una venta (HU-18, CF-09, D20)

Venta del equipo TS13-221045 como vendedor, el 09-11-2026.

| Captura | Qué muestra |
|---|---|
| [04-ficha-con-vender.png](04-ficha-con-vender.png) | La ficha de un equipo disponible ofrece Vender |
| [05-fecha-futura.png](05-fecha-futura.png) | Una fecha de venta futura se rechaza |
| [06-venta-registrada.png](06-venta-registrada.png) | Ficha de la venta con cliente, fecha, orden de compra, equipo y total |
| [07-ficha-equipo-vendido.png](07-ficha-equipo-vendido.png) | CF-09: el equipo queda vendido, con el movimiento de salida por venta y el enlace a la venta, y sin botones para arrendar, vender ni trasladar |
| [08-disponibilidad-sin-el-vendido.png](08-disponibilidad-sin-el-vendido.png) | CF-09: el equipo vendido deja de aparecer en la disponibilidad |
| [09-encargado-sin-acceso.png](09-encargado-sin-acceso.png) | El encargado de bodega no tiene acceso a las ventas (matriz de permisos) |

El equipo vendido tampoco se ofrece al registrar un arriendo. La operación quedó asociada al cliente
Constructora Los Andes SpA, y su historial se consulta con HU-19.
