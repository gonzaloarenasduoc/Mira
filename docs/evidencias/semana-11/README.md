# Evidencias de la semana 11

Capturas de HU-15, HU-29 y HU-16 (Sprint 4), tomadas con un navegador automatizado sobre la aplicación levantada
contra PostgreSQL 16.

## Retorno de equipos (HU-15)

El arriendo N° 4 tiene dos equipos; se registra el retorno de uno solo, en una
bodega distinta de la de salida.

| Captura | Qué muestra |
|---|---|
| [01-arriendo-pendiente.png](01-arriendo-pendiente.png) | Arriendo con sus dos equipos pendientes de retorno |
| [02-formulario-retorno.png](02-formulario-retorno.png) | HU-15 formulario con la bodega de asignación y la fecha de hoy propuestas |
| [03-fecha-futura.png](03-fecha-futura.png) | HU-15 una fecha de retorno futura se rechaza con un mensaje del negocio |
| [04-retorno-parcial.png](04-retorno-parcial.png) | HU-15 retorno de un equipo: el otro sigue arrendado |
| [05-ficha-equipo-retornado.png](05-ficha-equipo-retornado.png) | CF-04 el equipo queda disponible en Bodega Norte, con el movimiento de retorno y su observación |
| [06-disponibilidad-tras-retorno.png](06-disponibilidad-tras-retorno.png) | La disponibilidad cuenta de nuevo el equipo retornado |

## Clientes (HU-29, D17)

| Captura | Qué muestra |
|---|---|
| [07-clientes.png](07-clientes.png) | Listado de clientes, con la opción Clientes en el menú del vendedor |
| [08-rut-invalido.png](08-rut-invalido.png) | HU-29 un RUT con dígito verificador incorrecto se rechaza |
| [09-cliente-registrado.png](09-cliente-registrado.png) | HU-29 cliente registrado; el RUT escrito sin puntos queda con formato 76.555.444-6 |
| [10-rut-repetido.png](10-rut-repetido.png) | HU-29 el RUT no se repite dentro de la empresa |
| [11-arriendo-con-cliente-nuevo.png](11-arriendo-con-cliente-nuevo.png) | El cliente nuevo ya se puede elegir al registrar un arriendo |

## Alertas de retorno (HU-16)

| Captura | Qué muestra |
|---|---|
| [12-inicio-alertas.png](12-inicio-alertas.png) | CF-10 el inicio muestra el arriendo vencido destacado, con cliente, equipo y días de atraso, y el que vence en 2 días |
| [13-retorno-desde-alerta.png](13-retorno-desde-alerta.png) | Desde la alerta se registra directamente el retorno del equipo |

Un arriendo que vence en 12 días no aparece: el aviso empieza 3 días antes de la fecha comprometida.

CF-04 también se verifica con la prueba de integración `retorno_enOtraBodega_reasignaElEquipoYCierraLaLinea`
contra PostgreSQL: el equipo se reasigna a la bodega receptora, la línea registra su fecha de retorno
y solo existen los movimientos de salida y retorno, sin un traslado adicional.

## Tablero del proyecto

| Captura | Qué muestra |
|---|---|
| [14-tablero-github-projects.png](14-tablero-github-projects.png) | Tablero de GitHub Projects al 28-10: HU-14 terminada; HU-15, HU-16 y HU-29 en revisión con su responsable, épica, puntos e iteración; HU-17 a HU-19 en el backlog |
