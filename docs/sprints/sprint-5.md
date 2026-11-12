# Sprint 5 · Semanas 14 y 15

**Objetivo:** las operaciones quedan con su capacitación y sus documentos, y la jefatura consulta el
panel de indicadores.

**Resultado:** cumplido en desarrollo el 09-11-2026, en la semana 13, antes de que comenzara el plazo
del sprint. 10 historias y 31 PF, con HU-30 agregada (D25). Todas en la rama `semana-13`, en el pull
request #33, que espera la aprobación de Joaquín. Con este sprint, el backlog completo (30 historias,
105 PF) queda implementado.

## Sprint Backlog

En el orden de prioridad del Product Backlog. Las zonas de propiedad se mantienen: Joaquín en
servicios y entidades, Gonzalo en controladores y vistas, y Gonzalo lidera el módulo documental y el
panel (`docs/06-flujo-trabajo.md`).

| Prioridad | Historia | Issue | Épica | PF | Responsable | Estado |
|---|---|---|---|---|---|---|
| 20 | HU-23 · Adjuntar documentos a una operación | #26 | E008 | 5 | Gonzalo (lidera) y Joaquín (servicios) | Terminada, adelantada el 09-11, rama `semana-13` (D22) |
| 21 | HU-25 · Consultar los documentos de un equipo | #28 | E008 | 5 | Gonzalo (lidera) y Joaquín (servicios) | Terminada, adelantada el 09-11, rama `semana-13` (CF-13) |
| 22 | HU-24 · Consultar los documentos de una operación | #27 | E008 | 3 | Gonzalo (lidera) y Joaquín (servicios) | Terminada, adelantada el 09-11, rama `semana-13` |
| 23 | HU-28 · Alertas de retorno vencido en el panel | #31 | E009 | 2 | Gonzalo (lidera) y Joaquín (servicios) | Terminada, adelantada el 09-11, rama `semana-13` (CF-10) |
| 24 | HU-26 · Panel de equipos por bodega y estado | #29 | E009 | 3 | Gonzalo (lidera) y Joaquín (servicios) | Terminada, adelantada el 09-11, rama `semana-13` (D23, PNF-02) |
| 25 | HU-27 · Utilización de equipos y arriendos vigentes | #30 | E009 | 3 | Gonzalo (lidera) y Joaquín (servicios) | Terminada, adelantada el 09-11, rama `semana-13` (D24) |
| — | HU-30 · Administrar técnicos (agregada, D25) | #32 | E007 | 2 | Gonzalo | Terminada el 09-11, rama `semana-13` |
| 26 | HU-20 · Agendar la capacitación de una operación | #23 | E007 | 3 | Joaquín (servicios) y Gonzalo (vistas) | Terminada el 09-11, rama `semana-13` |
| 27 | HU-21 · Advertir la doble asignación de un técnico | #24 | E007 | 2 | Joaquín (servicios) y Gonzalo (vistas) | Terminada el 09-11, rama `semana-13` (D26) |
| 28 | HU-22 · Calendario de capacitaciones | #25 | E007 | 3 | Joaquín (servicios) y Gonzalo (vistas) | Terminada el 09-11, rama `semana-13` (D27) |

Total: 10 historias, 31 PF, con HU-30 agregada en la semana 13 (D25; el backlog pasa de 103 a 105 PF). Las tablas `documento`, `tecnico` y `capacitacion` existen desde V1.

**Contingencia (acta del 08-10-2026):** si el tiempo no alcanza, se priorizan las historias en este
orden y lo que quede se documenta como trabajo futuro, previa consulta con el docente. No fue
necesario usarla.

Al cierre del desarrollo había 219 pruebas automatizadas, 18 de ellas de integración contra
PostgreSQL 16. El 12-11 se ejecutó `./mvnw clean verify` con Docker: 219 pruebas, 0 fallas, 0
omitidas. Evidencias en `docs/evidencias/semana-13/`, capturas 01 a 32 y medición de PNF-02.

## Retrospectiva (borrador por validar con Joaquín)

**Qué funcionó**
- El sprint se desarrolló completo en la semana 13, antes de su plazo (semanas 14 y 15), incluida la
  historia agregada (HU-30).
- Las reglas que el backlog no definía se consultaron y registraron antes de implementarlas (D22 a
  D27). La falta de una historia para administrar técnicos se detectó al empezar HU-20 y se resolvió
  con una historia nueva, sin cargar datos a mano.
- Los requisitos se midieron con datos reales: el panel responde en 0,052 s en el percentil 95 con un
  año de datos (PNF-02) y la utilización de HU-27 coincide con un cálculo independiente en SQL.
- Por primera vez en el proyecto, el pull request se abrió con la suite completa ejecutada y sin
  pruebas omitidas.

**Qué no funcionó**
- No se cumplió el acuerdo de la retrospectiva del Sprint 4: todo el sprint lo desarrolló un solo
  integrante, incluidos los servicios de HU-20 a HU-22, asignados a Joaquín. Se le avisó del cambio
  de zona, pero el código no tuvo un segundo autor.
- El tablero no se actualizó al cerrar cada historia.
- Las pruebas de integración de HU-20 a HU-22 se ejecutaron recién el 12-11, después de abrir el
  pull request, porque el entorno de desarrollo no tenía Docker activo.
- La base de demostración se perdió al reemplazarse el entorno de trabajo el 09-11. Se recuperó desde
  un respaldo del 02-11 y hubo que volver a cargar los datos de la semana.
- Por segunda vez faltó en el backlog una historia de mantención de datos (HU-29 con los clientes,
  HU-30 con los técnicos).

**Qué cambiamos**
- En las semanas 14 y 15, las pruebas integradas se reparten entre ambos integrantes y cada uno sube
  su evidencia desde su propia cuenta.
- `./mvnw clean verify` se ejecuta antes de abrir cada pull request, no después.
- Al cierre de cada semana se respalda la base de demostración (`pg_dump`) junto a las evidencias.
- Quien cierra una historia mueve su tarjeta en el tablero el mismo día (acuerdo que se mantiene del
  Sprint 4).
