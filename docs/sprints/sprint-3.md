# Sprint 3 · Planificado para las semanas 10 y 11, adelantado a la semana 9

**Objetivo:** el encargado de bodega traslada equipos entre bodegas y el vendedor consulta la
disponibilidad consolidada en una sola pantalla.

**Resultado:** cumplido. 5 historias y 18 PF, desarrolladas en la rama `semana-09` e integradas a
`main` con el pull request #3 (26-10), aprobado por Joaquín Romero.

## Sprint Backlog

| Historia | PF | Servicios y entidades | Vistas | Estado | Evidencia |
|---|---|---|---|---|---|
| HU-09 · Registrar un traslado entre bodegas | 3 | Joaquín | Gonzalo | Terminada | `TrasladoServiceTest`, CF-08 |
| HU-10 · Consultar el historial de traslados | 2 | Joaquín | Gonzalo | Terminada | `semana-09/05-llegada-confirmada.png` |
| HU-11 · Buscar equipos por sus atributos | 5 | Joaquín | Gonzalo | Terminada | `BusquedaTest`, PNF-07 |
| HU-12 · Ver la disponibilidad consolidada por bodega | 5 | Joaquín | Gonzalo | Terminada | `DisponibilidadServiceTest`, CF-07, PNF-01 |
| HU-13 · Ver la fecha de retorno de los equipos arrendados | 3 | Joaquín | Gonzalo | Terminada | `semana-09/14-disponibilidad.png` |

Además, PNF-08 (cookie de sesión `Secure` en el perfil `demo`), pendiente desde la semana 8. Al cierre
del sprint había 57 pruebas automatizadas. Las capturas y mediciones están en
`docs/evidencias/semana-09/`.

## Retrospectiva

**Qué funcionó**
- Terminar el Sprint 2 a tiempo permitió adelantar un sprint completo.
- La disponibilidad se midió con 500 equipos: 0,20 s en el percentil 95, bajo el límite de 2 s (PNF-01).
- Las decisiones de diseño nuevas quedaron registradas antes de integrar (D14 a D16).
- Por primera vez, la integración tuvo la aprobación del otro integrante registrada en GitHub.

**Qué no funcionó**
- La aprobación del pull request #3 y su integración ocurrieron casi al mismo tiempo: la revisión fue
  breve.
- Al planificar el Sprint 4 apareció un vacío del backlog: ninguna historia permite registrar clientes.
- Los campos del tablero (Status, Épica, Puntos, Sprint) quedaron sin completar.

**Qué cambiamos**
- La revisión de cada pull request se hace con tiempo y deja al menos un comentario sobre el código.
- El registro de clientes se consulta con el docente antes de continuar con las operaciones.
- Cada integrante mantiene al día sus tarjetas en el tablero.
