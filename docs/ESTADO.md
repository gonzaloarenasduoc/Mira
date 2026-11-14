# Estado del proyecto

Este archivo se actualiza al terminar cada sesión de trabajo. Es lo primero que lee el
asistente del otro integrante para saber qué está en curso y no pisar nada.

**Última actualización:** 14-11-2026

## Fase actual

Fase 2, semana 14, rama `semana-14` (D11). **El backlog completo (30 historias, 105 PF) está
implementado e integrado en `main`**: el pull request #33 se integró el 12-11 con la aprobación de
Joaquín y cerró los issues del Sprint 5 y las épicas E006 a E009. `./mvnw clean verify` sobre `main`:
219 pruebas, 0 fallas, 0 omitidas. Las semanas 14 y 15 son de pruebas integradas, implantación y
preparación de la presentación; no se agregan funcionalidades.

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
- Tablero de GitHub Projects con las 9 épicas (#4 a #12) y las historias del Sprint 4 (#13 a
  #18 y #20) como *issues*, cada historia colgada de su épica. El 28-10 se completaron sus campos:
  Status, responsable, Épica, Puntos y Sprint. Las iteraciones del campo Sprint se nombran por
  semanas ("Semanas 10 y 11"), porque el Sprint 4 abarca las semanas 10 a 13. HU-14 terminada;
  HU-15, HU-16 y HU-29 en revisión; HU-17 a HU-19 en el backlog. Captura en
  `docs/evidencias/semana-11/14-tablero-github-projects.png`.
- Pull request #3 (`semana-09` → `main`) abierto el 26-10, con la revisión pedida a Joaquín.
- HU-14 en `semana-10`: registro de un arriendo con varias líneas, en una sola transacción.
  Rechaza equipos no disponibles o con arriendo vigente, con la fecha de retorno en el mensaje
  (CF-02, PNF-10). Si una línea falla no queda nada guardado (CF-03, prueba de integración contra
  PostgreSQL). El equipo cambia de estado solo mediante `EquipoService.despacharEnArriendo`.
  75 pruebas; capturas en `docs/evidencias/semana-10/`.
- HU-15 en `semana-11`: retorno por línea; el equipo queda disponible en la bodega que lo recibe,
  aunque sea distinta, con el movimiento `RETORNO_ARRIENDO` y sin traslado adicional (CF-04, con
  prueba de integración contra PostgreSQL). 85 pruebas; capturas en `docs/evidencias/semana-11/`.
- HU-29 en `semana-11` (D17): clientes con RUT validado por dígito verificador, único por empresa y
  con baja lógica. Menú Clientes; la jefatura solo consulta. 106 pruebas.
- HU-16 en `semana-11`: alertas de retorno en el inicio, con los vencidos destacados y los que
  vencen en 3 días o menos (CF-10). Las pruebas de integración usan el reloj de la aplicación
  (America/Santiago); con la fecha en UTC fallaban de noche. 110 pruebas.
- 28-10: la suite completa (110 pruebas) pasó en el equipo de Gonzalo (Ubuntu, Docker Engine 29,
  Java 21) **sin omitir ninguna**: las 11 pruebas de integración corrieron con Testcontainers sobre
  PostgreSQL 16. Se corrigió `./mvnw`, que no funcionaba en Linux ni en macOS.
- Pull request #19 (semana 10) integrado el 26-10 con la aprobación de Joaquín.
- Pull request #3 integrado el 26-10 con la aprobación de Joaquín: el Sprint 3 está en `main`.
- Sprint Backlog y retrospectiva de los sprints 1 a 3, y el backlog del Sprint 4, en `docs/sprints/`.
  Actas de control de avance de las semanas 9, 10 y 11 en `docs/actas/`. El acta de la semana 11
  (02-11) informa el ajuste del backlog por HU-29 (de 100 a 103 PF).
- Pull request #21 (`semana-11` → `main`) integrado el 02-11 con la aprobación de Joaquín: HU-15,
  HU-16 y HU-29 están en `main` y sus issues (#14, #15 y #20) quedaron cerrados.
- Entregables del 16-11 en `semana-14` (14-11): video `Demo/Demo_v5.mp4`; informe final
  `Fase 2/Evidencias Grupales/Informe_Final_Proyecto_APT.docx`; versiones finales del Product Backlog
  (105 PF), la DoD y el Documento de Diseño (archivos nuevos, los del 16-10 no se tocan); matriz de
  pruebas `docs/12-matriz-pruebas.md` con PNF-03, PNF-13 y PNF-14 medidos. Al medir PNF-13 se corrigió
  la página de error 5xx (antes salía la Whitelabel).
- Datos de demostración en `docs/demo/` (14-11): seis meses de operación ficticia, coherentes con
  las reglas del sistema; se cargan en segundos con `cargar-demo.sh` sobre una base vacía.
- Implantación con Docker en `semana-14` (12-11, D28): `Dockerfile` de dos etapas y servicio `app` en
  `docker-compose.yml`; `docker compose up --build` levanta el sistema completo. Probada sobre una
  base vacía, con un documento adjunto que sobrevive al reinicio del contenedor.
- Pull request #33 (`semana-13` → `main`) integrado el 12-11 con la aprobación de Joaquín: HU-19 a
  HU-28 y HU-30. Se cerraron #18, #23 a #32 y las épicas #9 a #12. Cobertura de la capa de
  servicios: 84,6% (PNF-15 exige 70%).
- HU-22 en `semana-13` (09-11, D27): calendario mensual de capacitaciones con cliente, lugar, técnico
  y estado, y su ficha, donde se marcan realizadas (con fecha y hora, no futuras) o se cancelan;
  ambos estados son finales. El menú "Técnicos" pasa a ser "Capacitaciones". 219 pruebas. **E007 queda
  completa.**
- HU-21 en `semana-13` (09-11, D26): al elegir técnico y fecha, el formulario advierte si el técnico
  ya tiene otra capacitación programada ese día, con su hora y lugar; se puede agendar igual marcando
  "Agendar de todas formas" (el servidor vuelve a revisar al enviar). 211 pruebas.
- HU-20 en `semana-13` (09-11): agendar la capacitación de un arriendo o una venta, con técnico
  activo, fecha (hoy o posterior), hora y lugar; queda programada y vinculada a la operación. La ficha
  sugiere agendarla cuando algún equipo es de una categoría que la requiere y no hay una programada o
  realizada. Agendan administrador y vendedor; la jefatura solo consulta y el encargado no ve la
  sección. 207 pruebas.
- HU-30 en `semana-13` (09-11, D25): administrar técnicos (nueva, 2 PF, como HU-29 con los clientes).
  El backlog pasa de 103 a 105 PF; informarlo en el acta de la semana 13. Menú Técnicos para
  administrador, vendedor y jefatura (que solo consulta); el encargado de bodega no accede. 197 pruebas.
- HU-27 en `semana-13` (adelantada el 09-11, D24): utilización de los equipos en un periodo elegible
  (por omisión, 30 días), global, por categoría y con los diez más y menos usados, y la lista de
  arriendos vigentes. Verificada contra un cálculo independiente en SQL con un año de datos; el panel
  completo responde en 0,095 s en el percentil 95. 191 pruebas. **E009 queda completa.**
- HU-26 en `semana-13` (adelantada el 09-11, D23): gráficos del panel con Chart.js (webjar nuevo en el
  `pom.xml`): equipos por bodega y por estado, más una tabla con los mismos datos; los vendidos no se
  cuentan como inventario. PNF-02 medido con un año de datos: 0,052 s en el percentil 95. La barra de
  navegación pasa a menú desplegable bajo 1200 px. 185 pruebas.
- HU-28 en `semana-13` (adelantada el 09-11, CF-10): nuevo panel de indicadores (`/indicadores`, opción
  Indicadores del menú para todos los roles) que empieza con los retornos vencidos, destacados y con sus
  días de atraso. Reutiliza las alertas de HU-16, sin consultas nuevas. 182 pruebas.
- HU-24 en `semana-13` (adelantada el 09-11): las fichas de arriendo, venta y equipo listan los
  documentos con su tipo, folio y fecha, y el nombre del archivo lo descarga. Solo usuarios de la
  misma empresa; el encargado no descarga documentos de ventas (D21, D22). Verificado byte a byte
  contra el disco; 178 pruebas. **E008 queda completa.**
- HU-25 en `semana-13` (adelantada el 09-11, CF-13): la ficha del equipo muestra los documentos de
  todas las operaciones en que participó; el documento de un arriendo con varios equipos aparece en
  cada ficha. Siguiendo D21, el encargado de bodega no ve los documentos de ventas. 170 pruebas.
- Issues del Sprint 5 creados como sub-issues de sus épicas: #23 a #31 (HU-20 a HU-28).
- HU-23 en `semana-13` (adelantada el 09-11, D22): adjuntar orden de compra, guía de despacho o
  factura a un arriendo o a una venta, con folio, fecha y archivo PDF, JPG o PNG de hasta 10 MB. Se
  valida el contenido del archivo; se guarda en `APP_UPLOAD_DIR` con nombre generado. Página propia
  para un envío sobre el límite (`error/413.html`). Verificado contra PostgreSQL y el disco; 164 pruebas.
- Sprint 4 cerrado en desarrollo, con borrador de retrospectiva por validar con Joaquín, y Sprint 5
  planificado en `docs/sprints/sprint-5.md`.
- HU-19 en `semana-13` (adelantada el 09-11, D21): ficha del cliente con su historial de ventas y
  arriendos, del más reciente al más antiguo, con tipo, equipos, estado y total. El encargado de
  bodega ve solo los arriendos (matriz de permisos). Verificado contra PostgreSQL; 148 pruebas.
  La rama `semana-13` parte de `semana-12`: el PR #22 aún no estaba integrado.
- Cierre de la semana 12 (09-11): pull request #22 (`semana-12` → `main`) con HU-17, HU-18 y la
  migración V3, con la revisión pedida a Joaquín, y acta de control de avance del 09-11 en
  `docs/actas/`. Avance: 19 de 29 historias, 71 de 103 PF.
- HU-17 en `semana-12` (09-11): listado de arriendos con su estado (vigente, vencido o cerrado) y su
  total, estimado o definitivo, calculados al consultar con la regla de D18. La opción Arriendos del
  menú lleva al listado y la ve también la jefatura, que solo consulta. Verificado contra PostgreSQL
  con los arriendos de la demo; 119 pruebas. Capturas en `docs/evidencias/semana-12/`.
- HU-18 en `semana-12` (09-11, D20): registro de una venta con cliente, fecha y valor por equipo, en
  una sola transacción. El equipo queda `VENDIDO` con su movimiento `SALIDA_VENTA` y no vuelve a
  estar disponible: arriendo, traslado y retorno lo rechazan (CF-09, con prueba de integración).
  Botón Vender en la ficha del equipo. Permisos de la matriz: venden administrador y vendedor, la
  jefatura consulta y el encargado de bodega no accede. Verificado contra PostgreSQL; 140 pruebas.
- Migración V3 en `semana-12` (09-11, D19): siete categorías nuevas tomadas del catálogo de
  `docs/referencias/` (Accesorios, Cursos, Escáneres láser, Georradares, Cámaras, Servicios y
  Registros facturas). Verificada contra PostgreSQL 16 sobre una base en V2 con datos y sobre una
  base vacía. **Al traer esta rama, Flyway la aplica sola al levantar la app.**
- Tercer informe de avance (02-11): `Fase 2/Evidencias Grupales/Informe_Avance_3_Semana11.docx`
  (copia de la guía 2.6 actualizada a la semana 11; el informe 2.6 entregado no se modificó), el acta
  del 02-11 y el video `Demo/Demo_v4.mp4` (7 min 21 s, sin audio): repositorio y código, Flyway aplicando
  V1 y V2 sobre una base vacía, recorrido de HU-01 a HU-16 y HU-29, consultas a PostgreSQL con lo
  registrado en la demo, la suite de pruebas y el tablero.

## En curso

| Qué | Quién | Rama | Notas |
|---|---|---|---|
| Entregas del 16-11: Retrospectiva Semana 13 (acta y video) e Informe final Fase 2 (30%) con la defensa | Gonzalo y Joaquín | `semana-14` | Listos: acta, Demo_v5, informe final, versiones finales de Backlog, DoD y Diseño, matriz de pruebas. Faltan la presentación de la defensa y la revisión de ambos (sección 6 del informe y retros) |

## Siguiente

1. Validar con Joaquín los borradores de las retrospectivas de los sprints 4 y 5 (`docs/sprints/`).
2. Entregar el acta de la semana 13 (`docs/actas/Acta_Control_de_Avance_2026_11_16.docx`), que informa
   el ajuste del backlog a 105 PF (D25).
3. Actualizar el tablero: sprints 4 y 5 a Terminado.
4. Consultar con el docente las categorías que no son equipos físicos (D19).
5. Pendientes de la matriz que requieren personas: PNF-09 (3 personas ajenas registran un arriendo), PNF-11
   (probar en Firefox y Edge) y PNF-16 (alguien del curso levanta el sistema con el README).
6. Revisar y ajustar cada uno su párrafo de la sección 6 del informe final (intereses y proyecciones).

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
- Para ver HU-13 en local ya no hace falta cargar un arriendo con SQL: se registra desde la pantalla
  Arriendos (HU-14), con un cliente creado en Clientes (HU-29).
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
- **En Linux:** usar Docker Engine (`get.docker.com`) en vez de Docker Desktop, agregar el usuario al
  grupo `docker` (`sudo usermod -aG docker $USER` y volver a iniciar sesión) e instalar
  `openjdk-21-jdk`, dejándolo como predeterminado con `update-alternatives`. Las pruebas se corren con
  `./mvnw test`.
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
- `docker compose up --build` levanta base y aplicación (D28). Para desarrollar se sigue usando
  `docker compose up -d db` y `./mvnw spring-boot:run`.

---

### Cómo actualizar este archivo

Al terminar de trabajar, deja registrado: qué terminaste, qué quedó a medias y en qué rama,
y cualquier cosa que el otro deba saber antes de tocar código. Sé específico: "quedó a
medias el servicio de arriendos, falta la validación de fechas, rama feature/E005" sirve;
"avancé en arriendos" no sirve.
