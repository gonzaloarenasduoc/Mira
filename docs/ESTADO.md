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

## En curso

| Qué | Quién | Rama | Notas |
|---|---|---|---|
| _(nada aún)_ | | | |

## Siguiente

1. Configuración del proyecto base: Spring Boot, PostgreSQL en Docker, Flyway y
   dependencias de pruebas. **Lo genera Joaquín**, en la rama `feature/esqueleto`, y entra
   a `main` antes de que Gonzalo clone. Decisión tomada porque el esqueleto cae entero en
   zona compartida (`pom.xml`, `config`, migraciones) y hacerlo dos veces obliga a rehacer
   el merge. Requisitos previos en la lista de verificación de
   `docs/11-arranque-claude-code.md`.
2. Migración `V1__esquema_inicial.sql` a partir del modelo de datos.
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
- Java 17 no sirve. El proyecto compila contra 21 y el compilador se detiene.

---

### Cómo actualizar este archivo

Al terminar de trabajar, deja registrado: qué terminaste, qué quedó a medias y en qué rama,
y cualquier cosa que el otro deba saber antes de tocar código. Sé específico: "quedó a
medias el servicio de arriendos, falta la validación de fechas, rama feature/E005" sirve;
"avancé en arriendos" no sirve.
