# Guion de la defensa del proyecto

Presentación de 15 minutos con `Fase 2/Evidencias Grupales/Presentacion_Defensa_Final.pptx`, con demostración en vivo
del sistema levantado con Docker y los datos de demostración. El respaldo es el video `Demo/Demo_v5.mp4`.

**Reparto:** Joaquín presenta el problema, la solución, la metodología, el modelo de datos y la arquitectura
(diapositivas 1 a 8). Gonzalo presenta la demostración, las pruebas, los resultados y el cierre (9 a 13).

## Tiempos

| # | Diapositiva | Expone | Tiempo | Acumulado |
|---|---|---|---|---|
| 1 | Portada | Joaquín | 15 s | 0:15 |
| 2 | Temario | Joaquín | 15 s | 0:30 |
| 3 | El problema | Joaquín | 1 min | 1:30 |
| 4 | La solución: Mira | Joaquín | 1 min | 2:30 |
| 5 | Metodología | Joaquín | 1 min | 3:30 |
| 6 | Plan y ejecución | Joaquín | 1 min | 4:30 |
| 7 | Modelo de datos | Joaquín | 1 min | 5:30 |
| 8 | Arquitectura e implantación | Joaquín | 1 min | 6:30 |
| 9 | Demostración en vivo | Gonzalo | 5 min | 11:30 |
| 10 | Pruebas y calidad | Gonzalo | 1 min | 12:30 |
| 11 | Resultados frente a los objetivos | Gonzalo | 1 min | 13:30 |
| 12 | Dificultades y aprendizajes | Gonzalo | 45 s | 14:15 |
| 13 | Trabajo futuro y cierre | Gonzalo | 30 s | 14:45 |

## Texto de cada diapositiva

### 1 · Portada · Joaquín (15 s)
> Buenos días. Somos Joaquín Romero y Gonzalo Arenas y presentamos Mira, el sistema de gestión y trazabilidad de equipos de geomensura que desarrollamos en el Capstone.

### 2 · Temario · Joaquín (15 s)
> Yo presento el problema, la solución, cómo trabajamos y el diseño. Gonzalo muestra el sistema en vivo, las pruebas, los resultados y lo que queda como trabajo futuro.

### 3 · El problema · Joaquín (1 min)
> La empresa vende y arrienda estaciones totales, GPS RTK, drones y niveles a mineras, constructoras y topógrafos, y lleva su inventario en planillas. El problema de fondo es que una planilla cuenta cantidades, no equipos: dice que hay cinco GPS, pero no cuál está en qué bodega ni si está arrendado. Con varias bodegas, encontrar un equipo disponible es llamar por teléfono. Los arriendos vencidos no se detectan y equipos de alto valor quedan inmovilizados. Y sin historial no hay información para decidir compras.

### 4 · La solución: Mira · Joaquín (1 min)
> Mira reemplaza la planilla por una aplicación web donde cada equipo es una unidad con número de serie, estado y ubicación. Cubre el inventario en varias bodegas, la disponibilidad consolidada, los arriendos y ventas con control de retorno, los documentos y las capacitaciones de cada operación, y un panel de indicadores para la jefatura. Cada uno de los cuatro roles ve solo lo que le corresponde. A la derecha, el panel con datos de demostración.

### 5 · Metodología · Joaquín (1 min)
> Trabajamos con Scrum adaptado al calendario: sprints de dos semanas, cada uno con un objetivo y una demostración. Después de un primer sprint sin cumplir escribimos una Definition of Done: ninguna historia se da por terminada sin su prueba automatizada. Cada semana es una rama que se integra por pull request aprobado por el otro. Registramos 28 decisiones con sus alternativas, llevamos un acta por semana, y las reglas del proyecto están escritas en el repositorio para que nuestros asistentes de programación las respeten.

### 6 · Plan y ejecución · Joaquín (1 min)
> El primer sprint no cumplió su objetivo y sus historias pasaron al segundo. Desde ahí formalizamos el proceso y recuperamos el atraso: el Sprint 3 se adelantó a la semana 9 y el Sprint 5 se desarrolló en la semana 13. Durante el camino detectamos dos historias que faltaban, la administración de clientes y la de técnicos, y el backlog pasó de 100 a 105 puntos. La implantación con Docker quedó lista en la semana 14.

### 7 · Modelo de datos · Joaquín (1 min)
> El modelo tiene 14 tablas y se versiona con migraciones de Flyway que nunca se editan. Cuatro decisiones lo definen: el equipo es la unidad, identificada por su número de serie; cada cambio de estado deja un movimiento inmutable, como un libro contable; lo que se puede calcular, como la disponibilidad o los atrasos, no se guarda, para que nunca quede desactualizado; y toda tabla de negocio lleva la empresa, de modo que el sistema puede servir a varias empresas sin rediseño.

### 8 · Arquitectura e implantación · Joaquín (1 min)
> Es un monolito modular: ocho módulos por área de negocio que solo se hablan a través de servicios. La regla central es transaccional: un arriendo, el cambio de estado de sus equipos y sus movimientos se guardan juntos o no se guarda nada; lo probamos incluso botando la base de datos a mitad de un arriendo. Y se implanta con Docker Compose: un solo comando levanta la base y la aplicación. Ahora Gonzalo lo muestra funcionando.

### 9 · Demostración en vivo · Gonzalo (5 min)
> Ver el recorrido de la demostración, más abajo.

### 10 · Pruebas y calidad · Gonzalo (1 min)
> El sistema tiene 219 pruebas automatizadas que pasan completas, 18 de ellas contra PostgreSQL real, y una cobertura de 84,6 % en la capa de servicios. Los 14 casos funcionales se cumplen. De los 16 no funcionales cumplimos 12: la disponibilidad responde en 0,2 segundos y el panel en 0,05 con un año de datos. Botamos la base en medio de un arriendo y no quedó nada a medias; esa prueba además nos mostró un defecto en la página de error, que corregimos. Somos transparentes: la concurrencia se cumple con usuarios que hacen pausas entre acciones, pero no en saturación, faltan Firefox y Edge, y dos pruebas requieren personas ajenas al proyecto.

### 11 · Resultados frente a los objetivos · Gonzalo (1 min)
> Cumplimos los objetivos de construcción: modelo de datos, inventario, operaciones, capacitaciones, documentos y panel. El levantamiento se ajustó, porque las reuniones con la empresa se aplazaron y los vacíos quedaron como supuestos de diseño declarados. Y la validación se cumplió en parte: con pruebas sí, pero la revisión con la empresa no se pudo hacer. Por eso decimos que el aporte es simulado: lo demostramos con datos que reproducen seis meses de operación.

### 12 · Dificultades y aprendizajes · Gonzalo (45 s)
> Tuvimos cuatro dificultades. Planificar con rangos de semanas permitió que el primer sprint no se cumpliera; lo corregimos con sprints formales y terminamos adelantados. Las reuniones con la empresa se aplazaron; trabajamos con supuestos declarados. Faltaban dos historias en el backlog y las agregamos con su decisión. Y el trabajo de los últimos sprints quedó concentrado en un integrante; lo reconocemos en la retrospectiva y lo compensamos con revisión cruzada y las pruebas finales repartidas.

### 13 · Trabajo futuro y cierre · Gonzalo (30 s)
> Como trabajo futuro quedan el despliegue en la nube con HTTPS, la validación con usuarios reales de la empresa, completar las pruebas de navegadores y de reproducibilidad, y definir con la empresa las categorías que no son equipos físicos. Muchas gracias; quedamos atentos a sus preguntas.

## Demostración en vivo (5 minutos) · Gonzalo

### Preparación, 30 minutos antes

1. En el PC que se proyecta, levantar el sistema desde cero con los datos de demostración. Las fechas de los datos se ajustan al día de la carga, así que conviene cargarlos el mismo día:
   ```bash
   docker compose down -v
   docker compose up --build -d
   docs/demo/cargar-demo.sh docker
   ```
2. Abrir `http://localhost:8080` en Chrome a pantalla completa (F11), con el zoom en 100 % o 110 %, y cerrar las notificaciones del sistema.
3. Tener a mano un PDF para adjuntar como factura. Sirve cualquiera de `docs/demo/archivos/1/`.
4. Dejar abierto, detrás del navegador, `Demo/Demo_v5.mp4` en el minuto 1:33, por si falla algo.
5. Usuarios: `vendedor@mira.cl` / `vendedor123`, `jefatura@mira.cl` / `jefatura123` y `bodega@mira.cl` / `bodega123`.

### Recorrido

| Min. | Qué se hace | Qué se dice |
|---|---|---|
| 0:00 | Entrar como **vendedor** y abrir **Arriendos** | El listado calcula el estado y el total de cada arriendo al consultar; los vencidos se destacan |
| 0:30 | Abrir el arriendo vigente de **Topografía Atacama** | Dos equipos, la orden de compra y la guía de despacho adjuntas, y la capacitación ya realizada |
| 1:00 | **Equipos**, buscar `AX9`, abrir **AX9-221104** y pulsar **Vender** | Cliente **Inmobiliaria Costanera**, valor `7200000` y orden de compra `OC-2026-170`. La venta se guarda en una transacción y el equipo queda vendido |
| 1:45 | **Adjuntar documento**: tipo Factura, folio `90015` y el PDF | Solo se aceptan PDF, JPG o PNG de hasta 10 MB; el archivo se guarda fuera de la base |
| 2:15 | En la ficha de la venta, **Agendarla ahora** | El sistema sugiere la capacitación porque una estación total la requiere |
| 2:30 | Técnico **Andrés Pérez**, fecha **dentro de tres días** | Aparece el aviso: ya tiene dos capacitaciones ese día. Se marca **Agendar de todas formas**, a las 17:30 |
| 3:15 | Menú **Capacitaciones** | Calendario del mes con hora, cliente, técnico y estado; la cancelada aparece tachada |
| 3:40 | **Cerrar sesión**, entrar como **jefatura** y abrir **Indicadores** | Retornos vencidos con sus días de atraso, equipos por bodega y estado, y la utilización por categoría y por equipo |
| 4:30 | **Cerrar sesión**, entrar como **encargado de bodega** y escribir `localhost:8080/capacitaciones` | Su menú no tiene Ventas ni Capacitaciones, y la dirección escrita a mano responde acceso denegado |

Si algo falla (red, Docker o una pantalla que no carga), no se depura en vivo: se pasa al video en el minuto 1:33 y se narra encima con el mismo recorrido.

## Preguntas probables

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué un monolito y no microservicios? | Ningún módulo tiene una carga distinta que justifique separarlo, y un solo despliegue es más simple de operar para una pyme (D03). |
| ¿Cómo evitan que un equipo quede en dos arriendos? | El registro bloquea el equipo, valida que esté disponible y guarda el arriendo, el cambio de estado y el movimiento en una sola transacción (CF-02, CF-03). |
| ¿La empresa validó el sistema? | No. Las reuniones se aplazaron y la validación se hizo con pruebas y datos de demostración; por eso el aporte es simulado. |
| ¿Por qué el backlog pasó de 100 a 105 PF? | Faltaban dos historias que el registro de operaciones necesitaba: clientes (HU-29) y técnicos (HU-30). Se registraron como decisiones y se informaron en las actas. |
| ¿Qué no se cumplió? | La validación con la empresa, el despliegue en la nube y tres pruebas no funcionales: PNF-09 y PNF-16 requieren personas ajenas al proyecto, y en PNF-11 faltan Firefox y Edge. La concurrencia se cumple solo con usuarios que hacen pausas. |
| ¿Cómo usaron la inteligencia artificial? | Con asistentes de programación que leen las reglas del proyecto desde el repositorio; cada cambio se revisa en un pull request y lleva sus pruebas. |
