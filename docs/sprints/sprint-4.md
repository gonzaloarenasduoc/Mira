# Sprint 4 · Semanas 10 a 13

**Objetivo:** el sistema registra arriendos y ventas validando la disponibilidad y controla los
retornos con alertas.

**Resultado:** cumplido en desarrollo el 09-11-2026, una semana antes del plazo. 7 historias y 32 PF:
HU-14 a HU-18 y HU-29 integradas a `main` (pull requests #19, #21 y #22); HU-19 terminada en la rama
`semana-13`, en el pull request #33.

## Sprint Backlog

| Historia | Issue | PF | Responsable | Estado |
|---|---|---|---|---|
| HU-14 · Registrar un arriendo validando disponibilidad | #13 | 8 | Joaquín (servicios) y Gonzalo (vistas) | Terminada e integrada (PR #19) |
| HU-15 · Registrar el retorno de un equipo arrendado | #14 | 5 | Joaquín (servicios) y Gonzalo (vistas) | Terminada e integrada (PR #21) |
| HU-16 · Alertas de retorno próximo o vencido | #15 | 5 | Gonzalo (vistas) y Joaquín (servicios) | Terminada e integrada (PR #21) |
| HU-17 · Consultar los arriendos y su total | #16 | 3 | Gonzalo | Terminada e integrada (PR #22, D18) |
| HU-18 · Registrar una venta | #17 | 5 | Joaquín (asignada); la implementó Gonzalo | Terminada e integrada (PR #22, D20) |
| HU-19 · Consultar el historial de operaciones de un cliente | #18 | 3 | Gonzalo | Terminada, pull request #33 (D21) |
| HU-29 · Administrar clientes (agregada, D17) | #20 | 3 | Joaquín (servicios) y Gonzalo (vistas) | Terminada e integrada (PR #21) |

**Ajuste al backlog (semana 11):** el registro de arriendos y ventas exige elegir un cliente
registrado, pero el backlog no tenía una historia para registrarlos. Se agregó HU-29 (3 PF) con la
decisión D17; el backlog pasa de 100 a 103 PF y el Sprint 4, de 29 a 32 PF. Se informó en el acta de
control de avance del 02-11-2026.

Al cierre del desarrollo había 148 pruebas automatizadas, 14 de ellas de integración contra
PostgreSQL 16. Evidencias en `docs/evidencias/semana-10/` a `semana-13/`.

## Retrospectiva (borrador por validar con Joaquín en la semana 13)

**Qué funcionó**
- El sprint se desarrolló completo una semana antes del plazo, incluida la historia agregada (HU-29).
- Las reglas que el backlog no definía se decidieron y registraron antes de integrar (D17 a D21).
- Las reglas transaccionales se verificaron contra PostgreSQL real: reversión completa (CF-03),
  retorno en otra bodega (CF-04) y venta (CF-09).
- Cada pull request se integró con la aprobación del otro integrante registrada en GitHub.

**Qué no funcionó**
- El desarrollo de las semanas 12 y 13 lo hizo un solo integrante, incluida HU-18, asignada al otro.
- El tablero no se actualizó al cerrar cada historia.
- Los comentarios de aprobación de los pull requests no reflejan una revisión propia del código.

**Qué cambiamos**
- En el Sprint 5, cada integrante desarrolla y sube las historias que tiene asignadas.
- Quien cierra una historia mueve su tarjeta en el tablero el mismo día.
- La aprobación de un pull request incluye al menos una observación propia sobre el código.

