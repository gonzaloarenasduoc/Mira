# Estado del proyecto

Este archivo se actualiza al terminar cada sesión de trabajo. Es lo primero que lee el
asistente del otro integrante para saber qué está en curso y no pisar nada.

**Última actualización:** 10-09-2026

## Fase actual

Fase 1, definición del proyecto. El desarrollo comienza en la semana 5.

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

## En curso

| Qué | Quién | Rama | Notas |
|---|---|---|---|
| Esqueleto del proyecto | Joaquín | `feature/esqueleto` | Terminado y compilando. Falta levantarlo contra la base: Docker en el equipo de Joaquín todavía no responde |

## Siguiente

1. Verificar el arranque cuando Docker responda: `docker compose up -d db` y
   `.\mvnw.cmd spring-boot:run`. Spring Security pide usuario y contraseña, es lo esperado:
   la clave aparece en la consola. El esqueleto entra a `main` antes de que Gonzalo clone.
2. Migración `V1__esquema_inicial.sql` a partir del modelo de datos, más
   `MiraApplicationTests` con Testcontainers, que es la primera prueba que necesita base de
   datos real.
3. Épica 001, acceso y roles.
4. Reunión con la contraparte cuando sea posible, para confirmar los supuestos de la
   sección 6 del modelo de datos.

## Bloqueado

| Qué | Por qué | Quién lo destraba |
|---|---|---|
| Cifras reales en la documentación | No se ha concretado la reunión con la empresa | Joaquín |
| Confirmar los supuestos 3, 7, 8 y 10 del modelo de datos | Requiere consultar a la contraparte o a la Eve | Joaquín |
| Push directo a GitHub desde el equipo de Joaquín | Sin acceso a la cuenta, segundo factor perdido | Joaquín. Mientras tanto, entrega por bundle según `docs/06-flujo-trabajo.md` |
| Levantar la aplicación en el equipo de Joaquín | Docker instalado pero requiere reiniciar el equipo | Joaquín. La sesión 2 igual se puede hacer: compila sin base de datos |
| Tablero en GitHub Projects | Requiere acceso al repositorio | Gonzalo |

## Advertencias vigentes

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
