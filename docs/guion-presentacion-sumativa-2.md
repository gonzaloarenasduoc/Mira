# Guion de la presentación · Evaluación sumativa 2

Presentación de 7 minutos con `Fase 2/Evidencias Grupales/Presentacion Proyecto.pptx`, seguida del
video de demostración `Demo/Demo_v3.mp4` (2 minutos y 50 segundos). El texto de cada diapositiva es
el de sus notas del orador, ajustado para que el total quede en unos 6 minutos y 50 segundos y sobre
margen para cambiar de expositor.

**Reparto:** Gonzalo expone las diapositivas 1 a 4 y la 9; Joaquín, de la 5 a la 8 y la 11; la 10
la exponen ambos.

## Parte 1 · Presentación (7 minutos)

| # | Diapositiva | Expone | Tiempo | Acumulado |
|---|---|---|---|---|
| 1 | Portada | Gonzalo | 10 s | 0:10 |
| 2 | Temario | Gonzalo | 15 s | 0:25 |
| 3 | El problema | Gonzalo | 45 s | 1:10 |
| 4 | El producto | Gonzalo | 45 s | 1:55 |
| 5 | Metodología de trabajo | Joaquín | 45 s | 2:40 |
| 6 | Plan de trabajo y avance | Joaquín | 55 s | 3:35 |
| 7 | Avance en cifras | Joaquín | 40 s | 4:15 |
| 8 | Dificultades y ajustes | Joaquín | 45 s | 5:00 |
| 9 | Competencias del perfil de egreso | Gonzalo | 45 s | 5:45 |
| 10 | El proyecto y nuestro desarrollo profesional | Ambos | 50 s | 6:35 |
| 11 | Próximos pasos | Joaquín | 15 s | 6:50 |

### 1 · Portada · Gonzalo · 10 s
> Buenos días. Somos Joaquín Romero y Gonzalo Arenas, y presentamos el avance de la Fase 2 de Mira,
> un sistema de gestión y trazabilidad de equipos de geomensura.

### 2 · Temario · Gonzalo · 15 s
> Recorremos el problema que abordamos, el producto, cómo lo estamos construyendo, el avance contra el
> plan, las competencias que el proyecto pone en juego y cómo se vincula con nuestro desarrollo
> profesional. Al final mostramos el sistema funcionando en un video de tres minutos.

### 3 · El problema · Gonzalo · 45 s
> La empresa vende y arrienda estaciones totales, GPS RTK, drones y niveles, y administra su
> inventario en planillas repartidas entre bodegas. Esas planillas cuentan cantidades, no equipos:
> dicen que hay cinco GPS, pero no cuál está en qué bodega ni si alguno está arrendado.
>
> Por eso la pregunta más básica del negocio, *¿tienen un GPS disponible para el lunes?*, puede tomar
> horas en responderse, y los arriendos vencidos pasan inadvertidos. El problema no es la falta de
> registro: es que la herramienta no representa un negocio donde los equipos cambian de estado todos
> los días.

### 4 · El producto · Gonzalo · 45 s
> Mira registra cada equipo como una unidad individual, identificada por su número de serie, con un
> estado único y un historial de movimientos. Sobre ese registro se construyen las ventas, los
> arriendos con control de retorno, las capacitaciones, los documentos y el panel de indicadores.
>
> *(Señalar la captura.)* A la derecha se ve el sistema real: el listado de equipos que construimos
> en el Sprint 2. Dejamos fuera, de forma deliberada, la integración con el SII y la facturación, para
> asegurar la calidad de lo comprometido.

### 5 · Metodología de trabajo · Joaquín · 45 s
> Trabajamos con un marco ágil basado en Scrum. Tras el monitoreo de la semana 6 formalizamos sprints
> de dos semanas, cada uno con un objetivo en una frase y una demostración al cierre. Por sugerencia
> del docente, desde la semana 8 trabajamos con una rama por semana, que se integra mediante un pull
> request revisado por el otro integrante.
>
> Ninguna historia se da por terminada sin su prueba automatizada y su captura como evidencia. Los
> artefactos del marco, el Product Vision, el Backlog, la Definition of Done y el Documento de Diseño,
> están en el repositorio.

### 6 · Plan de trabajo y avance · Joaquín · 55 s
> La Fase 1 se cumplió completa y a tiempo. En la Fase 2 acumulamos cerca de dos semanas de atraso:
> la implementación del modelo de datos estaba comprometida para las semanas 5 y 6 y se concretó en la
> semana 8.
>
> *(Señalar la línea de la semana 8 en la carta Gantt.)* Hoy el esquema completo está en PostgreSQL y
> el núcleo del sistema está en curso: el Sprint 2 dejó funcionando el acceso por rol, los usuarios,
> las bodegas y el registro de equipos. Los módulos de operaciones, el panel y la implantación aún no
> comienzan, pero están dentro de su plazo.

### 7 · Avance en cifras · Joaquín · 40 s
> En cifras: terminamos 8 de las 28 historias del backlog, que equivalen a 24 de los 100 puntos de
> función. El modelo de datos está implementado completo, con 14 tablas creadas mediante migraciones
> versionadas. Tenemos 29 pruebas automatizadas, que verifican las reglas de negocio, los permisos por
> rol y las migraciones. Y el historial del repositorio registra la autoría de cada integrante en cada
> commit.

### 8 · Dificultades y ajustes · Joaquín · 45 s
> Enfrentamos cuatro dificultades. Las reuniones con la empresa se aplazaron y no podíamos seguir
> esperando: consultamos al docente, continuamos con la propuesta original y declaramos los supuestos
> del modelo como decisiones de diseño.
>
> La más importante fue nuestra: el plan tenía rangos de semanas, pero no hitos verificables. Lo
> corregimos con sprints, una definición de terminado y una rama por semana. También resolvimos el
> bloqueo del entorno. Y al probar las migraciones descubrimos que Flyway no se estaba ejecutando: lo
> corregimos y agregamos una prueba que lo verifica.

### 9 · Competencias del perfil de egreso · Gonzalo · 45 s
> El proyecto moviliza cuatro competencias del perfil de egreso, de forma encadenada. Gestionamos el
> proyecto: definimos el alcance, planificamos y, cuando hubo atraso, ofrecimos alternativas.
> Construimos el modelo de datos, que es el centro del proyecto: sin representar cada equipo como
> unidad individual no hay trazabilidad. Desarrollamos la solución en capas, con seguridad por rol y
> transacciones. Y realizamos pruebas de certificación: diseñamos treinta casos de prueba,
> automatizamos las pruebas de cada historia y corregimos el producto a partir de sus resultados.

### 10 · El proyecto y nuestro desarrollo profesional · Ambos · 25 s cada uno
Es la reflexión personal de cada uno: el texto es una base, no un libreto. Cuéntenlo en primera
persona y con un ejemplo propio.

**Joaquín**
> Mi interés está en el backend y en el diseño de bases de datos, la parte del software que sostiene
> las reglas del negocio. Mira me exigió justo lo que identifiqué como mis brechas: las pruebas
> automatizadas y los contenedores. Y el atraso me enseñó que planificar y controlar es tan técnico
> como programar, lo que refuerza mi proyección hacia un rol de líder técnico.

**Gonzalo**
> Mis intereses están en el desarrollo web y en el análisis de datos. En Mira asumí las vistas, el
> panel de indicadores y la documentación, y el panel es mi puerta de entrada al análisis de datos.
> Vengo de la prevención de riesgos, y este proyecto consolida mi transición al desarrollo de
> software.

### 11 · Próximos pasos · Joaquín · 15 s
> Lo que sigue: en el Sprint 3, los traslados y la disponibilidad consolidada; en el Sprint 4, los
> arriendos con control de retorno y las ventas; en el Sprint 5, capacitaciones, documentos y el panel;
> y en la semana 16, la implantación con Docker. A continuación, el sistema funcionando.

**Frase opcional**, solo si el docente pregunta por el avance posterior a la semana 8:
> Después de la semana 8 adelantamos el Sprint 3: los traslados y la disponibilidad consolidada ya
> están implementados en la rama de la semana 9 y en revisión.

## Parte 2 · Video de demostración (2 minutos y 50 segundos)

`Demo/Demo_v3.mp4` no tiene audio y trae la narración como subtítulos, así que puede reproducirse
solo. Si prefieren narrarlo en vivo, usen esta tabla: cada fila indica qué aparece en pantalla y qué
se dice en ese momento.

| Tiempo | En pantalla | Narración | Voz |
|---|---|---|---|
| 0:00 – 0:15 | Aviso "Se abre localhost:8080/equipos sin haber iniciado sesión"; el sistema muestra el inicio de sesión. Clave incorrecta: "Correo o contraseña incorrectos". | "Mira protege todas sus pantallas: sin sesión, cualquier dirección lleva al inicio de sesión. Si la clave es incorrecta, el mensaje no revela si el error está en el correo o en la contraseña." | Gonzalo |
| 0:15 – 0:30 | Ingreso como administrador; el cursor recorre el menú. | "Ingresamos como administrador. El menú se arma según el rol: el administrador ve equipos, bodegas y usuarios." | Gonzalo |
| 0:30 – 0:50 | Bodegas. "Nueva bodega" con "bodega central": aparece "Ya existe una bodega llamada Bodega Central". | "Las bodegas se administran desde aquí. El nombre es único dentro de la empresa, sin importar mayúsculas o minúsculas." | Gonzalo |
| 0:50 – 1:25 | Registro de la estación total TS16-334512; se abre su ficha, "Disponible", con el movimiento de ingreso. | "Registramos una estación total con su número de serie. El equipo queda disponible en la bodega indicada y, en la misma transacción, se registra su movimiento de ingreso: quién, cuándo y hacia qué bodega. Ese historial es la base de la trazabilidad." | Joaquín |
| 1:25 – 1:40 | El mismo número de serie, en minúsculas: "Ya existe un equipo con el número de serie…". | "El sistema impide registrar dos veces el mismo número de serie dentro de la empresa." | Joaquín |
| 1:40 – 2:00 | Listado filtrado por Bodega Norte y búsqueda de "gs18". | "El listado permite filtrar por categoría, estado y bodega, y buscar por número de serie. Cada usuario ve solo los equipos de su empresa." | Joaquín |
| 2:00 – 2:25 | Creación de Valentina Soto con rol Vendedor; el administrador no tiene botón para desactivarse a sí mismo. Cierre de sesión. | "El administrador crea usuarios y les asigna un rol. La contraseña se guarda cifrada y nadie puede desactivar su propio usuario." | Gonzalo |
| 2:25 – 2:50 | El vendedor entra: su menú no tiene Usuarios y en Equipos no aparece "Registrar equipo". Aviso de dirección escrita a mano: "Acceso denegado". | "El vendedor solo consulta el inventario: no ve la administración de usuarios ni puede registrar equipos. Y si intenta entrar escribiendo la dirección, el servidor le niega el acceso: el permiso no depende de ocultar un botón. Cada una de estas reglas está cubierta por pruebas automatizadas." | Joaquín |

## Cierre y preguntas

Al terminar el video, Joaquín cierra:
> Eso es lo que llevamos. Quedamos atentos a sus preguntas.

Preguntas probables y quién responde:

| Pregunta | Responde | Respuesta breve |
|---|---|---|
| ¿Cómo garantizan que un equipo no se arriende dos veces? | Joaquín | Toda operación que cambia el estado de un equipo es transaccional, y el equipo se bloquea mientras cambia (decisión D15, ya aplicada en los traslados). El caso de prueba CF-02 está diseñado y se automatiza con los arriendos, en el Sprint 4. |
| ¿Cómo se ve el aporte de cada uno? | Gonzalo | Cada commit lleva su autor y las zonas de propiedad están en `docs/06-flujo-trabajo.md`: Joaquín modelo y servicios, Gonzalo vistas, panel y documentación. |
| ¿Qué pasa con la empresa? | Gonzalo | Las reuniones se aplazaron; con autorización del docente, los supuestos del modelo se tratan como decisiones de diseño declaradas. |
| ¿Por qué Spring Boot y no otro stack? | Joaquín | Porque las operaciones son transaccionales (todo o nada) y Spring Data JPA trae auditoría nativa, que es el requisito central del proyecto (`docs/08-decisiones.md`, D01). |

## Recomendaciones

- Ensayen una vez con cronómetro. Si se pasan, acorten la diapositiva 8, que es la más extensa.
- Dejen el video abierto y pausado antes de empezar, para no buscarlo durante la presentación.
- Cambien de expositor entre las diapositivas 4 y 5, 8 y 9, y 9 y 10, sin pausas largas.
