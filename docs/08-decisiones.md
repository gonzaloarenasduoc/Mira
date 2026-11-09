# Bitácora de decisiones

Registro de las decisiones técnicas tomadas, con su motivo. Existe para que ninguno de los
dos revierta sin querer algo que ya se discutió, y para poder responder en la defensa.

Formato: fecha, decisión, motivo, alternativas descartadas.

---

## D01 · Java 21 con Spring Boot 4.1

**Motivo.** Las operaciones del sistema son transaccionales: registrar un arriendo cambia
el estado del equipo y registra su movimiento, y debe ocurrir todo o nada. El tipado
estático detecta errores en un dominio con muchos estados antes de ejecutar. Spring Data
JPA aporta auditoría nativa, que es el requisito central del proyecto. Ambas son versiones
con soporte de largo plazo.

**Descartado.** Python con Django, que habría dado mayor velocidad inicial por su panel de
administración incorporado. Se asumió el mayor volumen de código a cambio de adecuación al
problema y de valor formativo.

---

## D02 · Sin interfaz de página única (SPA)

**Motivo.** Una SPA implica construir y mantener dos aplicaciones separadas, con estado
duplicado. El sistema son formularios, listados y búsquedas, sin interactividad que lo
justifique. Con renderizado en servidor se mantiene un solo proyecto.

**Descartado.** React o Angular con una API completa.

**Nota.** Si una pantalla concreta necesita actualización parcial, se resuelve con HTMX
sobre las mismas plantillas de Thymeleaf, sin frontend separado.

---

## D03 · Monolito modular en capas

**Motivo.** Ningún módulo tiene un perfil de carga distinto al resto, por lo que el
escalado independiente no aporta. La organización por paquetes de dominio da la separación
necesaria sin la complejidad operacional de servicios distribuidos, y permite extraer un
módulo más adelante si hiciera falta.

**Descartado.** Microservicios.

---

## D04 · Multi-tenencia desde el inicio

**Motivo.** Toda entidad de negocio lleva `empresa_id` y las claves únicas lo son por
empresa. Incorporarlo desde el comienzo cuesta días; hacerlo después obligaría a tocar cada
entidad, cada consulta y a migrar todos los datos. Deja abierta la posibilidad de un
modelo multi-empresa sin comprometer el alcance actual.

---

## D05 · La disponibilidad es calculada, no almacenada

**Motivo.** Un campo almacenado puede quedar desincronizado del estado real, que es
exactamente el problema que el proyecto resuelve. Se determina a partir del estado del
equipo y de los arriendos vigentes.

---

## D06 · Documentos adjuntos en sistema de archivos

**Motivo.** Simplicidad para el alcance comprometido. En la base de datos se guarda la
referencia, no el binario. Migrar a almacenamiento de objetos más adelante es un cambio de
configuración.

---

## D07 · Filtro por empresa mediante parámetro explícito

**Motivo.** El identificador de empresa se pasa como parámetro explícito en cada método de
servicio y de repositorio: `findByEmpresaIdAndNumeroSerie(...)`. El controlador lo obtiene
del usuario autenticado.

Es más verboso que las alternativas, pero **el compilador obliga a pasarlo**: no se puede
olvidar en silencio. Con un contexto implícito, una consulta escrita sin el filtro compila
igual y la fuga de datos entre empresas aparece recién en producción.

**Descartado.** Un `TenantContext` con variable de hilo, y los filtros de Hibernate
activados por sesión. Ambos son implícitos y se rompen con facilidad en las pruebas.

**Verificado por** los casos CF-14 y PNF-06.

---

## D08 · Lombok sí, MapStruct no

**Motivo.** Lombok elimina el código repetitivo de entidades y DTOs, que es real en Java.

MapStruct se descarta: los DTOs son `record` y el mapeo se escribe a mano en la capa de
servicio. Agregar un segundo procesador de anotaciones complica la construcción y la
depuración a cambio de ahorrar unas pocas líneas.

---

## D09 · Cuatro roles de usuario

**Motivo.** La documentación de negocio describía cinco perfiles de uso mientras el
enumerado definía tres. Se unifica en cuatro roles: `ADMINISTRADOR`, `ENCARGADO_BODEGA`,
`VENDEDOR` y `JEFATURA`.

Las tareas administrativas de documentación quedan cubiertas por el encargado de bodega y
el administrador, en lugar de crear un rol propio que en una empresa de este tamaño
recaería en la misma persona.

**Matriz de permisos** en `docs/01-contexto-negocio.md`.

---

## D10 · Bootstrap como webjar, no por CDN

**Fecha.** 10-09-2026.

**Motivo.** La defensa es presencial y proyectada en una sala de Duoc: si el wifi falla, con
un CDN la interfaz se ve sin estilos. El webjar viaja dentro del `.jar` y no depende de la
red. Además, todas las vistas se construyen sobre esta base; cambiar el origen de Bootstrap
más adelante obligaría a tocar cada plantilla.

Se agrega `org.webjars:bootstrap` con `org.webjars:webjars-locator-lite`, que resuelve la
ruta sin la versión: `@{/webjars/bootstrap/css/bootstrap.min.css}`. Así, subir la versión de
Bootstrap es cambiar una propiedad del `pom.xml` y ninguna plantilla.

**Descartado.** Bootstrap por CDN, más simple pero dependiente de la red en la demostración.

**Zona de propiedad.** Las plantillas son de Gonzalo. Queda avisado antes de que construya
las vistas.

---

## D11 · Una rama por semana

**Fecha.** 16-10-2026.

**Motivo.** Sugerencia del docente en el control de avance del 08-10-2026. Cada semana del
calendario académico queda como una rama con un pull request revisado y fechado, lo que hace
visible el avance semanal y el aporte de cada integrante, que es lo que se evalúa en el
informe de avance. Con dos integrantes y zonas de propiedad definidas, una rama compartida
por semana no genera más conflictos que una rama por funcionalidad.

**Condiciones.** `main` se mantiene estable: el pull request semanal se integra solo con las
pruebas pasando, y el trabajo inconcluso pasa a la rama de la semana siguiente. El
procedimiento está en `docs/06-flujo-trabajo.md`.

**Descartado.** Una rama por épica o funcionalidad, la convención original: deja ramas de
vida muy desigual, algunas de varias semanas, y no se corresponde con los cortes de
evaluación de la asignatura.

---

## D12 · Flyway con su starter de Spring Boot 4

**Fecha.** 16-10-2026.

**Motivo.** En Spring Boot 4 la autoconfiguración de Flyway se separó en su propio módulo. Con
solo `flyway-core` en el `pom.xml` la aplicación levantaba, pero **no aplicaba ninguna
migración**: la base quedaba vacía sin ningún error visible. Se detectó al aplicar por primera
vez `V1` y `V2` contra PostgreSQL. Se reemplaza `flyway-core` por
`spring-boot-starter-flyway`, que lo incluye junto con la autoconfiguración. Se mantiene
`flyway-database-postgresql`.

**No es una dependencia nueva:** Flyway ya estaba decidido desde el inicio; cambia solo la forma
de declararlo.

**Verificado por** `MiraApplicationTests`, que comprueba que al arrancar quedan aplicadas las dos
migraciones.

---

## D13 · Starters de prueba de MockMvc y de seguridad

**Fecha.** 16-10-2026.

**Motivo.** Las pruebas de rutas y permisos (CF-11, PNF-05) se escriben con `@WebMvcTest`, que
levanta solo la capa web con los servicios simulados y no necesita base de datos. En Spring
Boot 4 esa anotación y la integración de Spring Security con MockMvc salieron de
`spring-boot-starter-test` a dos starters propios: `spring-boot-starter-webmvc-test` y
`spring-boot-starter-security-test`. Este último incluye `spring-security-test`, que ya estaba en
el proyecto. Ambos son de alcance `test` y no llegan al `.jar` de la aplicación.

**Descartado.** Probar los permisos con `@SpringBootTest` completo: necesita PostgreSQL y, sin
Docker, la prueba se omite justo cuando más se necesita.

---

## D14 · El traslado tiene dos pasos: salida y llegada

**Fecha.** 18-10-2026.

**Decisión.** Un traslado se registra en dos momentos, cada uno con su movimiento. La salida
(`TRASLADO_SALIDA`) deja el equipo `EN_TRANSITO` y guarda el origen y el destino; la llegada
(`TRASLADO_LLEGADA`) lo deja `DISPONIBLE` y reasigna `equipo.bodega_id` al destino de esa salida.
Mientras está en tránsito, el equipo **sigue asignado a la bodega de origen**. La pantalla
Traslados muestra los equipos en tránsito para confirmar su llegada.

**Motivo.** Es lo que pide HU-09 ("la salida deja el equipo en tránsito y la llegada lo deja
disponible en la bodega de destino") y lo que ya preveía el enumerado `tipo_movimiento`. Un equipo
en camino no puede ofrecerse en ninguna de las dos bodegas, y el estado `EN_TRANSITO` lo excluye
de la disponibilidad sin reglas adicionales.

**Descartado.** Un traslado de un solo paso que cambie la bodega de inmediato: el inventario
mostraría el equipo disponible en el destino antes de que llegue.

## D15 · Bloqueo del equipo al cambiar su estado

**Fecha.** 18-10-2026.

**Decisión.** Toda operación que cambia el estado de un equipo lo lee con
`EquipoRepository.buscarParaActualizar`, que toma un bloqueo de escritura
(`PESSIMISTIC_WRITE`, `SELECT … FOR UPDATE`) hasta el fin de la transacción. Se usa en los
traslados y se usará en arriendos, retornos y ventas.

**Motivo.** Dos usuarios que trasladan o arriendan el mismo equipo a la vez leerían ambos
`DISPONIBLE` y lo comprometerían dos veces (CF-02). Con el bloqueo, la segunda operación espera a
la primera y ve el estado ya cambiado. No requiere migración.

**Descartado.** Bloqueo optimista con una columna `version`: exige una migración nueva y obliga a
reintentar la operación ante el conflicto, sin ventaja para el volumen de la empresa (PNF-03).

## D16 · Entidades de operaciones antes de sus servicios

**Fecha.** 18-10-2026.

**Decisión.** `Cliente`, `Operacion` (herencia JOINED con discriminador `tipo`), `Arriendo`,
`Venta` y `OperacionDetalle` se mapean en el Sprint 3, sobre las tablas que ya crea `V1`, sin
migración nueva. Por ahora solo se leen: HU-13 necesita la fecha comprometida de los arriendos
vigentes. Los constructores y las reglas de registro llegan con HU-14 a HU-19. `OperacionDetalle`
guarda el equipo como `equipoId` y `Operacion` guarda el usuario como `usuarioId`, igual que
`Movimiento`, para que `operaciones` no use las entidades de `inventario` ni de `usuarios`
(`docs/02-arquitectura.md`).

**Motivo.** La disponibilidad se calcula con el estado y los arriendos vigentes (D05); sin estas
entidades, HU-12 y HU-13 dependerían solo del estado. `ddl-auto: validate` comprueba el mapeo
contra el esquema real en cada arranque.

## D17 · Los clientes se administran desde la aplicación (HU-29)

**Fecha.** 26-10-2026.

**Decisión.** Se agrega al backlog la historia HU-29, "Administrar clientes", dentro de E005, con
3 PF, en el Sprint 4. Permite registrar, editar, activar y desactivar los clientes de la empresa.
El RUT se valida con su dígito verificador, se guarda con formato `12.345.678-9` y es único por
empresa. Los clientes no se eliminan: se desactivan, porque tienen operaciones asociadas. Los
administran el administrador, el encargado de bodega y el vendedor, que son los roles que registran
operaciones; la jefatura solo los consulta.

**Motivo.** HU-14, HU-18 y HU-19 exigen elegir un cliente "de los clientes registrados de la
empresa", pero ninguna historia permitía registrarlos. Se detectó al implementar HU-14. Mira se
plantea como un SaaS multiempresa: cada empresa agrega sus clientes a diario, y cargarlos en la
base de datos haría depender a cada empresa de un desarrollador. El modelo ya lo preveía: `cliente`
tiene `empresa_id` y el RUT es único por empresa.

**Efecto en el alcance.** El backlog pasa de 100 a 103 PF. El ajuste se informa en el acta de
control de avance de la semana 11.

**Descartado.** Cargar los clientes con SQL o con una migración de datos: sirve para una
demostración, no para un producto que usan varias empresas. Incluirlo dentro de HU-14: esa historia
ya estaba terminada e integrada, y los clientes también los usan las ventas y las capacitaciones.

---

## D18 · Cálculo del total de un arriendo (HU-17)

**Fecha.** 09-11-2026.

**Decisión.** El total de un arriendo es la suma, por cada línea, de la tarifa diaria por los días
de arriendo. Los días son la diferencia entre la fecha de inicio y la fecha de fin, con mínimo uno:
del 20 al 30 son 10 días, y un equipo que vuelve el mismo día se cobra un día. La fecha de fin es la
de retorno real; si el equipo no ha vuelto, la fecha comprometida, o la de hoy si ya venció. El total
es **estimado** mientras algún equipo no retorna y **definitivo** cuando todos retornaron. El estado
del arriendo (vigente, vencido o cerrado) y el total no se almacenan: se calculan al consultar.

**Motivo.** HU-17 pide el total y si es estimado o definitivo, pero no definía cómo contar los días;
el equipo lo decidió al implementarla.
Contar hasta hoy un arriendo vencido hace que el total estimado refleje el atraso, que es lo que el
vendedor necesita saber. Calcularlo evita guardar un dato que cambia cada día (03, 1.6).

**Descartado.** Contar los días de forma inclusiva (11 días del 20 al 30). Estimar siempre hasta la
fecha comprometida: ocultaría el atraso de un arriendo vencido.

---

## D19 · Categorías del catálogo de una empresa del rubro (migración V3)

**Fecha.** 09-11-2026.

**Decisión.** La migración `V3__categorias_catalogo.sql` agrega siete categorías a las cuatro de V2
(estación total, GPS RTK, dron y nivel), tomadas del catálogo de productos en
`docs/referencias/catalogo-productos.xlsx`: Accesorios, Cursos, Escáneres láser, Georradares,
Cámaras, Servicios y Registros facturas. Las de V2 no se renombran ni se desactivan: "GPS RTK" y
"Nivel" equivalen a los receptores y niveles del catálogo. Requieren capacitación los escáneres
láser, los georradares y las cámaras (supuesto declarado, como en V2).

**Motivo.** El catálogo muestra los tipos de producto que vende una empresa real del rubro. El
equipo decidió reflejarlos en las categorías para que las demostraciones usen la misma
clasificación.

**Riesgo declarado.** Mira registra cada equipo como una unidad con número de serie, estado y
bodega (03, 1.1). Cursos, Servicios y Registros facturas no son unidades físicas: con esas
categorías se puede registrar un "equipo" que no se guarda en una bodega ni se arrienda. Se
agregaron por decisión del equipo; los cursos tienen su propio flujo en E007 (capacitaciones), y
las ventas de servicios no están en el alcance (`docs/05-epicas.md`). Conviene validarlo con el
docente.

**Descartado.** Editar V2: la regla 1 prohíbe modificar una migración aplicada.

---

## D20 · Reglas del registro de una venta (HU-18)

**Fecha.** 09-11-2026.

**Decisión.** La venta lleva una fecha que el formulario propone con el día de hoy y que se puede
cambiar a una fecha pasada, nunca futura. El valor de venta de cada equipo se escribe siempre: es
obligatorio y mayor que cero, porque el sistema no guarda precios de venta. Solo se venden equipos
disponibles y sin arriendo vigente; el equipo vendido queda en estado `VENDIDO` con su movimiento
`SALIDA_VENTA`, y ningún otro movimiento lo acepta de vuelta. Los permisos siguen la matriz de
`01-contexto-negocio.md`: registran el administrador y el vendedor, la jefatura consulta la ficha
de una venta y el encargado de bodega no accede. La opción Ventas del menú lleva a registrar una
venta; el listado de ventas de un cliente llega con HU-19.

**Motivo.** CF-09 pide registrar la venta "con fecha y valor", pero no definía de dónde sale cada
dato; el equipo lo decidió al implementarla.

---

## D21 · Historial de operaciones de un cliente (HU-19)

**Fecha.** 09-11-2026.

**Decisión.** La ficha del cliente muestra sus ventas y arriendos del más reciente al más antiguo.
La fecha de un arriendo es su fecha de inicio, no la de registro, porque refleja cuándo empezó; la de
una venta es la fecha de la venta. El total de un arriendo sigue la regla de D18 y el de una venta es
la suma de sus valores. Según la matriz de permisos, el encargado de bodega no tiene acceso a las
ventas (E006): en su historial solo aparecen los arriendos.

**Motivo.** HU-19 pide las operaciones "ordenadas por fecha" sin decir cuál fecha usar en un
arriendo; el equipo lo decidió al implementarla.

---

> Agrega acá toda decisión nueva que afecte al otro integrante. Si Claude Code propone algo
> que contradice una decisión registrada, debe advertirlo en vez de aplicarlo.
