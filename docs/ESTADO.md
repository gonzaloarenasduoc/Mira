# Estado del proyecto

Este archivo se actualiza al terminar cada sesión de trabajo. Es lo primero que lee el
asistente del otro integrante para saber qué está en curso y no pisar nada.

**Última actualización:** 16-10-2026

## Fase actual

Fase 2, semana 8. Sprint 2 (semanas 8 y 9), que retoma el objetivo no cumplido del Sprint 1:
un usuario se autentica con su rol y registra un equipo por número de serie que aparece en un
listado filtrado por empresa. Desde esta semana rige **una rama por semana** (D11): la de esta
semana es `semana-08`.

Entregas de la asignatura: documentos de la Fase 2 el sábado 17-10 y presentación (7 minutos
más un video de 3 minutos) el domingo 18-10.

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
  - `Fase 2/Evidencias Individules/`: diarios 2.1 de cada integrante, con el formato
    `Apellido_Nombre_` que usa el docente en la Fase 2.
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

## En curso

| Qué | Quién | Rama | Notas |
|---|---|---|---|
| Historias HU-01 a HU-07 del Sprint 2 | Ambos | `semana-08` | El esquema ya existe: siguen las entidades, los servicios, el inicio de sesión real y las vistas |
| Revisión de los documentos de la Fase 2 | Ambos | `semana-08` | Los diarios 2.1 son borradores: cada uno revisa y personaliza el suyo |

## Siguiente

1. Historias HU-01 a HU-07 del Sprint 2 (E001 y E002), cada una con su prueba automatizada.
2. Presentación del domingo 18-10 y guion del video de 3 minutos.
3. Pull request de `semana-08` a `main` al cierre de la semana, revisado por el otro integrante.

## Bloqueado

| Qué | Por qué | Quién lo destraba |
|---|---|---|
| Push desde las sesiones de Claude Code en la nube | GitHub responde 403: la aplicación de Claude no tiene permiso sobre el repositorio | Gonzalo, reconectando GitHub en claude.ai e instalando la aplicación de Claude en el repositorio |
| Tablero en GitHub Projects | Compromiso de la semana 7 aún no cumplido | Gonzalo |

Ya no hay contraparte: con autorización del docente, los supuestos de la sección 6 del modelo
de datos son decisiones de diseño declaradas. Docker y el acceso de Joaquín a GitHub ya
funcionan.

## Advertencias vigentes

- El modelo tiene **catorce** tablas, no trece como se escribió en algunos documentos de la
  Fase 1 y en el acta del 08-10. Los documentos de la Fase 2 ya dicen catorce.
- Usuario inicial: `admin@mira.cl`, contraseña `admin123`, solo para demostración.

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
