# Evidencias de la semana 10

Capturas de HU-14 (Sprint 4), tomadas con un navegador automatizado sobre la aplicación levantada
contra PostgreSQL 16, con la sesión de un usuario vendedor. El cliente de las capturas se cargó con
SQL en la base local de verificación: el backlog todavía no tiene una historia para registrar
clientes (ver `docs/ESTADO.md`).

| Captura | Qué muestra |
|---|---|
| [01-ficha-con-arrendar.png](01-ficha-con-arrendar.png) | Ficha de un equipo disponible, con el botón Arrendar para el vendedor |
| [02-formulario-preseleccionado.png](02-formulario-preseleccionado.png) | HU-14 formulario con el equipo ya marcado y su tarifa diaria propuesta |
| [03-retorno-invalido.png](03-retorno-invalido.png) | HU-14 retorno anterior al inicio: mensaje del negocio y nada guardado |
| [04-formulario-dos-equipos.png](04-formulario-dos-equipos.png) | HU-14 dos equipos en el mismo arriendo, cada uno con su tarifa |
| [05-arriendo-registrado.png](05-arriendo-registrado.png) | CF-01 arriendo registrado con sus dos líneas y la tarifa diaria total |
| [06-ficha-arrendado.png](06-ficha-arrendado.png) | CF-01 el equipo queda arrendado, con su movimiento de salida a arriendo |
| [07-disponibilidad-con-arriendo.png](07-disponibilidad-con-arriendo.png) | HU-13 la disponibilidad muestra la fecha de retorno del arriendo recién registrado |

La reversión completa ante una línea que falla (CF-03) se verifica con la prueba de integración
`arriendo_segundaLineaNoDisponible_noGuardaNada`, contra PostgreSQL: no queda ni el arriendo, ni
equipos arrendados, ni movimientos.
