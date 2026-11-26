# Guion de la defensa del proyecto

Defensa final del 9 o 10 de diciembre, con `Fase 3/Evidencias Grupales/Presentacion_Defensa.pptx`. Sigue el temario del
docente: problema, producto, vínculo con el perfil de egreso, vínculo individual con el desarrollo profesional,
arquitectura y tecnologías, y planificación original con sus cambios y lecciones. Son 12 minutos de presentación
y, al final, la demostración en vivo de toda la funcionalidad, en máximo 8 minutos. Ambos presentan.

**Reparto:** Gonzalo presenta el problema, el producto, la planificación, las lecciones y el cierre (láminas 1 a 3 y
8 a 10). Joaquín presenta el perfil de egreso, la arquitectura y el modelo de datos (láminas 4 a 7). La lámina 5 la
comparten: cada uno habla de su propio desarrollo profesional. En la demostración, Joaquín muestra los roles de
administrador y encargado de bodega, y Gonzalo los de vendedor y jefatura.

El texto de cada lámina también está en las notas del orador de la presentación.

## Tiempos

| # | Lámina | Expone | Tiempo | Acumulado |
|---|---|---|---|---|
| 1 | Portada | Gonzalo | 20 s | 0:20 |
| 2 | El problema | Gonzalo | 1 min 15 s | 1:35 |
| 3 | El producto | Gonzalo | 1 min 15 s | 2:50 |
| 4 | Vínculo con el perfil de egreso | Joaquín | 1 min 30 s | 4:20 |
| 5 | El proyecto y nuestro desarrollo profesional | Joaquín y Gonzalo | 1 min 40 s | 6:00 |
| 6 | Arquitectura y tecnologías | Joaquín | 1 min 30 s | 7:30 |
| 7 | Modelo de datos: lo que nos diferencia | Joaquín | 1 min 15 s | 8:45 |
| 8 | Planificación original y real | Gonzalo | 1 min 15 s | 10:00 |
| 9 | Cambios y lecciones | Gonzalo | 1 min | 11:00 |
| 10 | Perseverancia | Gonzalo | 45 s | 11:45 |
| 11 | Demostración en vivo | Joaquín y Gonzalo | 8 min | 19:45 |
| 12 | Gracias y preguntas | Gonzalo | — | — |

## Texto de cada lámina

### 1 · Portada · Gonzalo (20 s)

> Buenos días. Somos Gonzalo Arenas y Joaquín Romero, y les presentamos Mira: un sistema web para la trazabilidad de los equipos de geomensura de una empresa que los vende y los arrienda.

### 2 · El problema · Gonzalo (1 min 15 s)

> Partamos con una pregunta que la empresa recibe todos los días: ¿tienen un GPS RTK disponible para el lunes? Hoy, responderla exige revisar planillas y llamar a cada bodega.
>
> El problema de fondo es que la planilla cuenta cantidades y no equipos: dice que hay cinco GPS, pero no cuál está en qué bodega ni si está arrendado. Con varias bodegas no hay una visión común, los retornos vencidos no se detectan y equipos de alto valor quedan inmovilizados. Y sin historial no hay datos para decidir qué comprar.

### 3 · El producto · Gonzalo (1 min 15 s)

> Mira cambia la unidad de trabajo: cada equipo es una unidad con número de serie, estado y ubicación conocidos en todo momento.
>
> Sobre esa base hay seis módulos: inventario en varias bodegas, con traslados; disponibilidad consolidada en una sola pantalla; arriendos y ventas que validan la disponibilidad y controlan los retornos; capacitaciones con los técnicos; documentos de cada operación, e indicadores para la jefatura. Cada uno de los cuatro roles ve solo lo que le corresponde. Al final lo verán funcionando.

### 4 · Vínculo con el perfil de egreso · Joaquín (1 min 30 s)

> El proyecto pone en práctica cuatro competencias del perfil de egreso, encadenadas.
>
> Gestión de proyectos: planificamos y controlamos cinco sprints y registramos 28 decisiones con sus alternativas. Modelos de datos: diseñamos e implementamos 14 tablas en PostgreSQL, preparadas para varias empresas. Solución de software: construimos e integramos las 30 historias del backlog. Y pruebas de certificación: 219 pruebas automatizadas, 84,6 % de cobertura en los servicios y mediciones de rendimiento.
>
> Están encadenadas porque cada una se apoya en la anterior: sin plan no hay modelo, sin modelo no hay software, y sin pruebas no sabemos si el software cumple.

### 5 · El proyecto y nuestro desarrollo profesional · Joaquín y Gonzalo (1 min 40 s)

> Joaquín: Mi interés está en el backend y en el diseño de bases de datos, la parte que sostiene las reglas del negocio. En Mira lo más exigente fue garantizar que un arriendo, el cambio de estado y su movimiento se guarden juntos o no se guarden. El proyecto también me hizo cerrar dos brechas, las pruebas automatizadas y los contenedores, y me enseñó que planificar y controlar es tan técnico como programar. Por eso me proyecto como líder técnico.
>
> Gonzalo: Mis intereses están en el desarrollo web y en el análisis de datos. En Mira trabajé las vistas, el panel de indicadores y los documentos, y el panel fue mi puerta de entrada a los datos: convertir el historial de cada equipo en indicadores exige entender cómo se registran. Vengo de la prevención de riesgos y este proyecto consolida mi paso al desarrollo de software. Me proyecto como desarrollador frontend que sigue creciendo en datos.

### 6 · Arquitectura y tecnologías · Joaquín (1 min 30 s)

> Mira es un monolito modular: una sola aplicación, organizada en ocho módulos por área de negocio que solo se comunican a través de sus servicios. Lo elegimos porque ningún módulo necesita escalar por separado, y un solo despliegue es más simple de operar para una pyme.
>
> Por capas: el navegador recibe páginas armadas en el servidor con Thymeleaf; los controladores reciben cada solicitud y Spring Security revisa el rol; los servicios tienen las reglas de negocio, y toda operación que cambia el estado de un equipo es transaccional; los repositorios acceden a PostgreSQL 16, cuyo esquema versionamos con Flyway.
>
> Usamos Java 21 con Spring Boot 4.1 por el tipado estático y el manejo de transacciones, y todo se levanta con Docker Compose: la base y la aplicación con un solo comando.

### 7 · Modelo de datos: lo que nos diferencia · Joaquín (1 min 15 s)

> Del modelo de datos destacamos tres decisiones que nos diferencian de una planilla.
>
> Primero, es multiempresa: toda tabla de negocio lleva la empresa, y las claves únicas, como el número de serie o el RUT, son únicas por empresa. Hoy sirve a una empresa; mañana, a varias sin rediseño.
>
> Segundo, el historial es inmutable: cada cambio de estado deja un movimiento que nunca se edita, como un libro contable. Y tercero, la disponibilidad no se guarda, se calcula al consultar, así que nunca queda desactualizada; aun así responde en 0,2 segundos con 500 equipos.

### 8 · Planificación original y real · Gonzalo (1 min 15 s)

> Este gráfico compara la planificación original con lo que realmente pasó, en puntos de función pendientes por semana. La línea verde es el plan de la Fase 1; la roja, lo real.
>
> El primer sprint, en las semanas 6 y 7, no se cumplió: la línea roja queda plana. Desde ahí formalizamos el proceso y recuperamos el atraso adelantando los sprints 3 y 5. En el camino el alcance creció en cinco puntos, con las historias de clientes y técnicos. Y terminamos el desarrollo en la semana 13, antes de lo planificado.

### 9 · Cambios y lecciones · Gonzalo (1 min)

> El Sprint 1 no se cumplió porque el plan tenía rangos de semanas y no hitos. Pasamos a sprints con objetivo, demostración y una definición de terminado: un plan necesita hitos verificables.
>
> Faltaban dos historias, clientes y técnicos: las agregamos, cada una con su decisión registrada e informada en las actas. Y cuando la empresa dejó de participar, declaramos supuestos de diseño: decidir y dejarlo escrito.

### 10 · Perseverancia · Gonzalo (45 s)

> Empezamos este proyecto con una empresa real del rubro, y en el camino la empresa dejó de participar. Podíamos achicar el proyecto; en cambio, con la perseverancia que nos formó Duoc, lo terminamos completo: 30 historias, 105 puntos de función y 219 pruebas.
>
> Y queremos seguir perseverando: ofrecerle Mira a la empresa y aportarle valor con lo que construimos. Ahora se lo mostramos funcionando.

## Demostración en vivo (máximo 8 minutos)

### Preparación, el mismo día

1. En el PC que se proyecta, levantar el sistema desde cero con los datos de demostración. Las fechas de los datos se
   ajustan al día de la carga, así que conviene cargarlos el mismo día:
   ```bash
   docker compose down -v
   docker compose up --build -d
   docs/demo/cargar-demo.sh docker
   ```
2. Abrir `http://localhost:8080` en Chrome a pantalla completa (F11), con el zoom en 100 %, y silenciar las notificaciones.
3. Tener a mano un PDF para adjuntar como factura: sirve cualquiera de `docs/demo/archivos/1/`.
4. Usuarios: `admin@mira.cl`, `bodega@mira.cl`, `vendedor@mira.cl` y `jefatura@mira.cl`; la contraseña es el rol
   seguido de `123` (`admin123`, `bodega123`, `vendedor123`, `jefatura123`).
5. Dejar abierto, detrás del navegador, `Demo/Demo_v5.mp4` en el minuto 1:33, por si algo falla.

El recorrido se ensayó completo el 26-11-2026 sobre los datos de demostración: los 22 pasos funcionaron y cubren las
30 historias.

### Recorrido

| Min. | Presenta | Qué se hace | Qué se dice |
|---|---|---|---|
| 0:00 | Joaquín | Entrar como **administrador** (admin@mira.cl) | Inicio de sesión (HU-01). El administrador ve todo el menú. |
| 0:15 | Joaquín | **Usuarios** · Nuevo usuario con rol Vendedor; luego **Desactivar** | Cada usuario tiene un rol (HU-02); no se borra, se desactiva (HU-06). |
| 0:45 | Joaquín | **Bodegas** · Nueva bodega: Bodega Iquique | La empresa administra sus bodegas (HU-08). |
| 1:00 | Joaquín | **Clientes** · Nuevo: RUT 76.543.210-1, luego corregir a 76.543.210-3 | El RUT se valida con su dígito verificador y es único por empresa (HU-29). |
| 1:25 | Joaquín | **Capacitaciones** · Técnicos · Nuevo técnico | Los técnicos que dictan las capacitaciones (HU-30). |
| 1:45 | Joaquín | Cerrar sesión y entrar como **encargado de bodega** (bodega@mira.cl) | Su menú no tiene ventas ni capacitaciones (HU-03). El inicio muestra las alertas de retorno (HU-16). |
| 2:05 | Joaquín | **Equipos** · Registrar equipo: GS18-990001, GPS RTK, Leica GS18 T, Bodega Central | Cada equipo se registra por su número de serie, único por empresa (HU-04). |
| 2:30 | Joaquín | Filtrar por **GS18** en el listado de equipos | Listado con filtros y búsqueda por serie, marca o modelo (HU-05 y HU-11). |
| 2:45 | Joaquín | Abrir **P4R-0F7712** y pulsar **Trasladar** a Bodega Norte | La ficha muestra el historial del equipo (HU-07); el traslado lo deja en tránsito (HU-09). |
| 3:05 | Joaquín | **Traslados** · Confirmar llegada de SDIP-310590 | Al confirmar, queda disponible en la bodega de destino; el historial de traslados queda abajo (HU-10). |
| 3:20 | Joaquín | **Disponibilidad** | Unidades disponibles por bodega y la fecha de retorno de los arrendados (HU-12 y HU-13). |
| 3:35 | Joaquín | **Inicio** · Registrar retorno del arriendo N° 332 (GS8000-GR0112) | El equipo vuelve y queda disponible en la bodega que lo recibe (HU-15). |
| 3:50 | Joaquín | Escribir a mano **localhost:8080/capacitaciones** | Acceso denegado: los permisos se revisan en el servidor, no solo en el menú (CF-11). Joaquín entrega a Gonzalo. |
| 4:00 | Gonzalo | Entrar como **vendedor** (vendedor@mira.cl) · Disponibilidad · Registrar arriendo | Cliente Topografía Atacama, AX10M-240311 y SEL-270803, retorno en 10 días, OC-2026-201. Se valida la disponibilidad y todo se guarda junto (HU-14). |
| 4:40 | Gonzalo | **Arriendos** | Estado y total de cada arriendo, calculados al consultar; los vencidos destacados (HU-17). |
| 4:55 | Gonzalo | **Ventas** · AX9-221104 a Inmobiliaria Costanera, 7.200.000, OC-2026-202 | La venta deja el equipo como vendido (HU-18). |
| 5:25 | Gonzalo | **Adjuntar documento**: Factura, folio 90015, PDF | Solo PDF, JPG o PNG de hasta 10 MB; queda en la ficha de la operación (HU-23 y HU-24). |
| 5:50 | Gonzalo | **Agendarla ahora** · técnico Andrés Pérez, dentro de tres días, 17:30 | Aviso: ya tiene dos capacitaciones ese día (HU-21). Se agenda de todas formas: el aviso no bloquea (HU-20). |
| 6:20 | Gonzalo | **Capacitaciones** · abrir la de Minera Pampa Norte · Marcar como realizada | Calendario con cliente, técnico y estado; realizada y cancelada son estados finales (HU-22). |
| 6:40 | Gonzalo | **Clientes** · Topografía Atacama | Historial de todas sus operaciones, con el arriendo recién registrado (HU-19). |
| 6:50 | Gonzalo | **Equipos** · AX9-221104 | Documentos de todas las operaciones del equipo (HU-25). |
| 7:00 | Gonzalo | Entrar como **jefatura** (jefatura@mira.cl) · **Indicadores** | Retornos vencidos con sus días de atraso (HU-28), equipos por bodega y estado (HU-26) y utilización por período (HU-27). La jefatura solo consulta. |
| 8:00 | Gonzalo | Volver a la presentación: lámina Gracias | "Muchas gracias; quedamos atentos a sus preguntas." |

Si algo falla (red, Docker o una pantalla que no carga), no se depura en vivo: se pasa al video en el minuto 1:33 y
se narra encima con el mismo recorrido.

## Preguntas probables

| Pregunta | Respuesta corta | Responde |
|---|---|---|
| ¿Por qué un monolito y no microservicios? | Ningún módulo tiene una carga distinta que justifique separarlo, y un solo despliegue es más simple de operar para una pyme (D03). | Joaquín |
| ¿Por qué Thymeleaf y no React? | El sistema son formularios y listados; una SPA obliga a mantener dos aplicaciones con estado duplicado (D02). | Joaquín |
| ¿Cómo evitan que un equipo quede en dos arriendos? | El registro bloquea el equipo, valida que esté disponible y guarda el arriendo, el cambio de estado y el movimiento en una sola transacción (CF-02 y CF-03). | Joaquín |
| ¿La empresa validó el sistema? | No. Dejó de participar, así que lo validamos con pruebas y con datos de demostración que reproducen seis meses de operación. | Gonzalo |
| ¿Por qué el backlog pasó de 100 a 105 PF? | Faltaban clientes (HU-29) y técnicos (HU-30), que el registro de operaciones necesitaba. Se registraron como decisiones y se informaron en las actas. | Gonzalo |
| ¿Qué no se cumplió? | El despliegue en la nube y cuatro pruebas no funcionales: PNF-03 y PNF-11 en parte, y PNF-09 y PNF-16, que requieren personas ajenas al proyecto. | Gonzalo |
| ¿Cómo trabajaron en equipo? | En sesiones de trabajo conjuntas y con una integración semanal por pull request aprobado por el otro integrante. | Gonzalo |
| ¿Cómo usaron la inteligencia artificial? | Con asistentes de programación que leen las reglas del proyecto desde el repositorio; cada cambio se revisa en un pull request y lleva sus pruebas. | Joaquín |
