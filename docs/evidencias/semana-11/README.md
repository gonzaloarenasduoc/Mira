# Evidencias de la semana 11

Capturas de HU-15 (Sprint 4), tomadas con un navegador automatizado sobre la aplicación levantada
contra PostgreSQL 16. El arriendo N° 4 tiene dos equipos; se registra el retorno de uno solo, en una
bodega distinta de la de salida.

| Captura | Qué muestra |
|---|---|
| [01-arriendo-pendiente.png](01-arriendo-pendiente.png) | Arriendo con sus dos equipos pendientes de retorno |
| [02-formulario-retorno.png](02-formulario-retorno.png) | HU-15 formulario con la bodega de asignación y la fecha de hoy propuestas |
| [03-fecha-futura.png](03-fecha-futura.png) | HU-15 una fecha de retorno futura se rechaza con un mensaje del negocio |
| [04-retorno-parcial.png](04-retorno-parcial.png) | HU-15 retorno de un equipo: el otro sigue arrendado |
| [05-ficha-equipo-retornado.png](05-ficha-equipo-retornado.png) | CF-04 el equipo queda disponible en Bodega Norte, con el movimiento de retorno y su observación |
| [06-disponibilidad-tras-retorno.png](06-disponibilidad-tras-retorno.png) | La disponibilidad cuenta de nuevo el equipo retornado |

CF-04 también se verifica con la prueba de integración `retorno_enOtraBodega_reasignaElEquipoYCierraLaLinea`
contra PostgreSQL: el equipo se reasigna a la bodega receptora, la línea registra su fecha de retorno
y solo existen los movimientos de salida y retorno, sin un traslado adicional.
