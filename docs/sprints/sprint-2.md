# Sprint 2 · Semanas 8 y 9 (12 al 25 de octubre)

**Objetivo:** el mismo del Sprint 1, que no se cumplió: un usuario se autentica con su rol y registra
un equipo por número de serie que aparece en un listado filtrado por empresa.

**Resultado:** cumplido. 8 historias y 24 PF, integradas a `main` con el pull request #2 (18-10).

## Sprint Backlog

| Historia | PF | Servicios y entidades | Vistas | Estado | Evidencia |
|---|---|---|---|---|---|
| HU-01 · Iniciar sesión con credenciales | 2 | Joaquín | Gonzalo | Terminada | `semana-08/01-login.png` |
| HU-02 · Crear usuarios con un rol | 3 | Joaquín | Gonzalo | Terminada | `semana-08/15-usuarios.png` |
| HU-03 · Menú y acceso según el rol | 2 | Joaquín | Gonzalo | Terminada | `SeguridadTest`, CF-11 |
| HU-04 · Registrar un equipo por número de serie | 5 | Joaquín | Gonzalo | Terminada | `EquipoServiceTest`, CF-05 |
| HU-05 · Listar equipos con filtros | 3 | Joaquín | Gonzalo | Terminada | `semana-08/12-equipos-filtro-serie.png` |
| HU-06 · Editar y desactivar usuarios | 1 | Joaquín | Gonzalo | Terminada | `UsuarioServiceTest` |
| HU-07 · Ficha del equipo con su historial | 5 | Joaquín | Gonzalo | Terminada | `semana-08/08-ficha-equipo.png` |
| HU-08 · Administrar bodegas (adelantada del Sprint 3) | 3 | Joaquín | Gonzalo | Terminada | `BodegaServiceTest` |

Las capturas están en `docs/evidencias/semana-08/`. Al cierre del sprint había 29 pruebas automatizadas.

## Retrospectiva

**Qué funcionó**
- El objetivo se cumplió completo, y HU-08 se adelantó porque registrar un equipo exige una bodega.
- La Definition of Done obligó a escribir la prueba de cada historia. Al probar las migraciones se
  descubrió que Flyway no se ejecutaba (D12), y quedó una prueba que lo verifica.
- Las migraciones V1 y V2 se aplicaron y verificaron contra PostgreSQL 16 antes de integrarlas.

**Qué no funcionó**
- El pull request #2 se integró sin una revisión registrada en GitHub.
- Algunos archivos (un diario, el video y un bundle) se subieron directamente a `main`, fuera de la
  rama de la semana.
- La aplicación de Claude no tenía acceso al repositorio, y el trabajo se entregó en bundles que subía
  una sola persona.
- El trabajo se concentró en pocos días.

**Qué cambiamos**
- Todo pull request se integra con la aprobación del otro integrante registrada en GitHub.
- La rama `main` solo recibe pull requests.
- Se habilitó el acceso directo al repositorio y se dejaron de usar bundles.
- Se acordó distribuir el avance durante la semana.
