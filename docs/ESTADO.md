# Estado del proyecto

Este archivo se actualiza al terminar cada sesión de trabajo. Es lo primero que lee el
asistente del otro integrante para saber qué está en curso y no pisar nada.

**Última actualización:** 26-10-2026

## Fase actual

Fase 2, semana 10, rama `semana-10` (D11), creada desde `semana-09` porque el pull request #3
(Sprint 3) todavía no se integra. Comenzó el **Sprint 4** (HU-14 a HU-19, semanas 10 a 13): HU-14
está implementada y probada.

## Terminado

- Definición del proyecto, alcance y objetivos.
- Nueve épicas dimensionadas.
- Stack tecnológico decidido y justificado.
- Documentación base del repositorio.
- Modelo de datos y diccionario de datos (`docs/03-modelo-datos.md`).
- Casos de prueba funcionales y no funcionales (`docs/10-casos-de-prueba.md`).
- Revisión completa de la documentación (sesión 1 de Claude Code). Se detectaron seis
  contradicciones y dos decisiones sin tomar; todas están resueltas:
  - Excepciones a la regla de `empresa_id`: `venta`, `arriendo` y `operacion_detalle` no la
    llevan, porque heredan la empresa de `operacion` (documentado en 03 §1.2).
  - Cálculo del total de un arriendo: `Σ(valor_unitario × días)`, con los días contados desde
    `arriendo.fecha_inicio` hasta `operacion_detalle.fecha_retorno_real` (03 §1.6).
  - Retorno en una bodega distinta a la de salida: reasigna `equipo.bodega_id`, el movimiento
    es `RETORNO_ARRIENDO` con `bodega_destino_id`, sin traslado adicional.
  - Firma del servicio de arriendo: recibe varias líneas, no un solo equipo (04).
  - CF-04 y CF-13 corregidos en `docs/10-casos-de-prueba.md`.
  - Roles: son cuatro, no tres. 03 §1.7 quedó alineado con el enumerado.
  - Decisiones nuevas D07 (filtro por empresa con parámetro explícito), D08 (Lombok sí,
    MapStruct no) y D09 (cuatro roles y matriz de permisos) en `docs/08-decisiones.md`.
- Esqueleto del proyecto (sesión 2 de Claude Code), en la rama `feature/esqueleto`:
  Maven con Spring Boot 4.1.1 y Java 21, wrapper incluido, dependencias del stack, JaCoCo,
  `docker-compose.yml` con el servicio `db`, `.env.example`, `.gitignore`, estructura de
  paquetes vacía, `application.yml` y una página de inicio con Thymeleaf y Bootstrap.
  `./mvnw clean test` pasa. **Sin migraciones ni entidades todavía.**

- Control de avance del 08-10 con el docente. El atraso de la Fase 2 quedó reconocido y el
  trabajo se reorganizó en sprints de dos semanas.
- Documentación de la Fase 2 (semana 8, rama `semana-08`), con la estructura y los nombres del
  repositorio del docente:
  - `Fase 2/Evidencias Proyecto (version Agil)/`: Documento 01 Product Vision, 02 Product
    Backlog (28 historias, HU-01 a HU-28, suman 100 PF), 04 Definition of Done y 05 Documento
    de Diseño.
  - `Fase 2/Evidencias Grupales/`: guía 2.4 renombrada al nombre exacto del docente e informe
    2.6.
  - `Fase 2/Evidencias Individules/`: diarios 2.1 con el formato
    `Apellido_Nombre_2.1_APT122_DiarioReflexionFase2.docx`. El de Joaquín ya está (el mismo entregado
    el 04-10 en la plataforma); falta el de Gonzalo.
- Diagramas UML en PlantUML en `docs/diagramas/`, con sus imágenes: casos de uso,
  arquitectura, E-R, clases, secuencia del arriendo, componentes y despliegue.
- Decisión D11, una rama por semana. `.mailmap` para unificar las identidades de Git.
- Migraciones `V1__esquema_inicial.sql` (las 14 tablas del diccionario) y
  `V2__datos_iniciales.sql` (empresa de demostración, administrador y cuatro categorías),
  aplicadas y verificadas contra PostgreSQL 16. `MiraApplicationTests` las prueba con
  Testcontainers; sin Docker la clase se omite.
- Corrección D12: Flyway no se ejecutaba porque Spring Boot 4 necesita
  `spring-boot-starter-flyway`. **Si alguno levantó la aplicación antes de esto, su base está
  vacía y no hay que hacer nada: Flyway aplica V1 y V2 en el próximo arranque.**
- Sprint 2 implementado (HU-01 a HU-08; HU-08 se adelantó del Sprint 3 porque registrar un equipo
  exige una bodega):
  - Inicio de sesión real contra la tabla `usuario`, con BCrypt y mensaje genérico de error.
  - Menú y permisos por rol según la matriz (`SeguridadConfig` y `MenuGlobal`).
  - Usuarios: crear, editar, activar y desactivar; nadie puede desactivarse a sí mismo.
  - Bodegas: crear, editar, activar y desactivar, con nombre único por empresa.
  - Equipos: registro con su movimiento `INGRESO` en la misma transacción, listado con filtros y
    ficha con historial. Un equipo de otra empresa se responde como inexistente.
  - 29 pruebas: 25 corren sin base de datos y 4 de integración con Testcontainers, que se omiten
    si no hay Docker.
  - Capturas del recorrido en `docs/evidencias/semana-08/`.
- Pull request #2 (`semana-08` → `main`) integrado. Quedó sin revisión registrada en GitHub.
- Entregas de la semana 9: diario 2.1 de Gonzalo, video `Demo/Demo_v3.mp4` y presentación en el AVA, y
  Acta de Control de Avance del 19-10-2026. La aplicación de Claude ya tiene acceso al repositorio:
  no se necesitan más bundles.
- Sprint 3 implementado en `semana-09` (HU-09 a HU-13, 18 PF):
  - **Traslados (HU-09, D14):** la salida deja el equipo `EN_TRANSITO`, todavía asignado a la
    bodega de origen; la llegada lo deja `DISPONIBLE` en la de destino. Cada paso es un
    movimiento, en la misma transacción que el cambio de estado. Solo se trasladan equipos
    disponibles, a una bodega activa y distinta del origen. Se inicia desde la ficha del equipo
    y la llegada se confirma desde la ficha o desde la página Traslados.
  - **Historial de traslados (HU-10):** página Traslados con los equipos en tránsito y el
    historial paginado (20 por página), filtrable por serie, bodega y fechas.
  - **Búsqueda (HU-11):** el texto busca en serie, marca y modelo, como parámetro y con los
    comodines escapados (`common.util.Busqueda`, PNF-07).
  - **Disponibilidad (HU-12, HU-13):** página Disponibilidad, agrupada por bodega. Disponible =
    estado `DISPONIBLE` y sin arriendo vigente; los vendidos no aparecen; los arrendados muestran
    la fecha comprometida de retorno y los vencidos se destacan con los días de atraso.
  - Entidades de operaciones mapeadas sin migración nueva (D16): `Cliente`, `Operacion`,
    `Arriendo`, `Venta` y `OperacionDetalle`. Solo se leen; su registro es del Sprint 4.
  - El equipo se bloquea con `PESSIMISTIC_WRITE` mientras cambia de estado (D15).
  - 57 pruebas: 50 corren sin base de datos y 7 de integración con Testcontainers. Las 7 se
    verificaron contra un PostgreSQL 16 local.
  - PNF-01 medido con 500 equipos: percentil 95 de 0,20 s. PNF-08 resuelto con el perfil `demo`.
  - Capturas y mediciones en `docs/evidencias/semana-09/`.
- Tablero de GitHub Projects con las 9 épicas (#4 a #12) y las 6 historias del Sprint 4 (#13 a
  #18) como *issues*, cada historia colgada de su épica. E001 y E002 están cerradas.
- Pull request #3 (`semana-09` → `main`) abierto el 26-10, con la revisión pedida a Joaquín.
- HU-14 en `semana-10`: registro de un arriendo con varias líneas, en una sola transacción.
  Rechaza equipos no disponibles o con arriendo vigente, con la fecha de retorno en el mensaje
  (CF-02, PNF-10). Si una línea falla no queda nada guardado (CF-03, prueba de integración contra
  PostgreSQL). El equipo cambia de estado solo mediante `EquipoService.despacharEnArriendo`.
  75 pruebas; capturas en `docs/evidencias/semana-10/`.

## En curso

| Qué | Quién | Rama | Notas |
|---|---|---|---|
| Revisión y aprobación del pull request #3 (Sprint 3) | Joaquín | `semana-09` | Lo abrió la cuenta de Gonzalo, así que lo aprueba Joaquín |
| Sprint 4: HU-15 a HU-19 | Ambos | `semana-10` | HU-14 terminada. Siguen el retorno (HU-15), las alertas (HU-16), el listado (HU-17) y las ventas (HU-18, HU-19) |
| Campos del tablero (Status, Épica, Puntos, Sprint) | Ambos | — | Los *issues* ya existen; falta completar esos campos a mano en el tablero |

## Siguiente

1. **Decidir cómo se registran los clientes.** HU-14 exige elegir un cliente "de los clientes
   registrados de la empresa", pero ninguna historia del backlog permite registrarlos. Hoy solo se
   pueden cargar con SQL. Hay que acordarlo con el docente: una historia nueva (por ejemplo,
   "Administrar clientes"), o incluirlo en E005 o E006 y registrarlo en `docs/08-decisiones.md`.
2. HU-15: retorno por línea, incluida la bodega receptora distinta (CF-04).
3. Integrar el pull request #3 y, al cierre de la semana, abrir el de `semana-10`.

## Bloqueado

Nada bloqueado.

Las reuniones con la empresa se aplazaron: con autorización del docente, los supuestos de la
sección 6 del modelo de datos se tratan como decisiones de diseño declaradas. Docker y el acceso de Joaquín a GitHub ya
funcionan.

## Advertencias vigentes

- En los documentos de la Fase 2 no se menciona que la empresa dejó de participar: se habla del
  aplazamiento de las reuniones, como se acordó con el docente. Los documentos ya entregados
  (Fase 1, guía 2.4, diarios) no se modifican.
- El modelo tiene **catorce** tablas, no trece como se escribió en algunos documentos de la
  Fase 1 y en el acta del 08-10. Los documentos de la Fase 2 ya dicen catorce.
- Usuario inicial: `admin@mira.cl`, contraseña `admin123`, solo para demostración.
  Los demás usuarios se crean desde la pantalla Usuarios con el rol que corresponda.
- La cookie de sesión lleva `Secure` solo con el perfil `demo` (`SPRING_PROFILES_ACTIVE=demo`),
  que es para el ambiente con HTTPS. En local no se activa: con HTTP la sesión dejaría de funcionar.
- Un equipo en tránsito sigue asignado a su bodega de origen hasta que se confirma la llegada (D14).
  No hay forma de anular un traslado iniciado: si el destino fue un error, se confirma la llegada y
  se registra el traslado de vuelta.
- Las pantallas de arriendos no existen todavía. Para ver HU-13 en local hay que cargar un arriendo
  con SQL, como se explica en `docs/evidencias/semana-09/README.md`.
- Los DTOs de formulario son clases con `@Data`, no `record` (ver `docs/04-convenciones-codigo.md`).

- El modelo de datos está definido y se puede implementar. Respeta el diccionario tal como
  está escrito: nombres de tabla, de columna y enumerados.
- Los supuestos de la sección 6 del modelo aún no se validan con la contraparte. Si alguno
  cambia, se resuelve con una migración nueva, nunca editando una existente.
- Las áreas de desempeño del Plan de Estudio están pendientes de confirmar.
- Los pasos 1 y 2 del arranque (esqueleto Maven y migración `V1`) caen enteros en zona
  compartida. Se acuerda quién los hace antes de tocar un archivo, y se hacen una sola vez.
- Un documento se adjunta a la **operación**, no al equipo. Si la operación cubre varios
  equipos, el mismo documento aparece en la ficha de todos. Es correcto (CF-13).
- Ninguno de los dos genera el esqueleto por su cuenta. Ya está asignado a Joaquín.
- Gonzalo no hace commits sobre una rama que llegó por bundle. Las correcciones se piden
  en el pull request y llegan en un bundle nuevo, para que el historial siga reflejando
  quién escribió cada cosa.
- Java 17 no sirve. El proyecto compila contra 21 y el compilador se detiene. En el equipo
  de Joaquín, el `java` del PATH era 17 (shim de Oracle) y `JAVA_HOME` estaba vacío. Se
  configuró `JAVA_HOME` a Temurin 21 a nivel de usuario: **afecta a cualquier otro proyecto
  de ese equipo que use Maven o Gradle con Java 17.** Lo que importa es
  `.\mvnw.cmd -v`, porque Maven usa `JAVA_HOME`, no el PATH.
- `cl.duoc.mira.common.controller.InicioController` es **provisional** y cae en dos zonas de
  propiedad: `common` es de Joaquín, los controladores y las vistas son de Gonzalo. **Pasa a
  Gonzalo** cuando arme el layout real; ahí decide dónde vive.
- Bootstrap se sirve como webjar, no por CDN (D08 §D10). Las plantillas lo referencian sin
  versión: `@{/webjars/bootstrap/css/bootstrap.min.css}`. Gonzalo: construye las vistas
  sobre eso, no agregues etiquetas a un CDN.
- La aplicación **no lee `.env`**: ese archivo es para Docker Compose. `application.yml`
  trae los valores por omisión (`localhost:5432/mira`, usuario y clave `mira`), así que
  clonar y levantar funciona sin configurar nada. Es lo que mide PNF-16.
- `docker-compose.yml` tiene solo el servicio `db`. El servicio `app` con su `Dockerfile`
  está pendiente; el README ya quedó corregido y no promete otra cosa.

---

### Cómo actualizar este archivo

Al terminar de trabajar, deja registrado: qué terminaste, qué quedó a medias y en qué rama,
y cualquier cosa que el otro deba saber antes de tocar código. Sé específico: "quedó a
medias el servicio de arriendos, falta la validación de fechas, rama feature/E005" sirve;
"avancé en arriendos" no sirve.
