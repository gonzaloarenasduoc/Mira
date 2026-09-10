# Mira — instrucciones para Claude Code

Este archivo lo lee Claude Code automáticamente al iniciar cada sesión, en el equipo
de **ambos** integrantes. Lo que está acá es obligatorio y no se negocia sin acuerdo
del equipo.

## Qué es este proyecto

Mira es un sistema web de **gestión y trazabilidad de equipos de geomensura** para una
empresa que vende y arrienda instrumental topográfico (estaciones totales, GPS RTK,
drones, niveles). Cada equipo es una **unidad única identificada por número de serie**,
con estado y ubicación conocidos en todo momento.

Proyecto de título, Capstone APT122, Duoc UC. Integrantes: Joaquín Romero y Gonzalo Arenas.

## Stack

Java 21 · Spring Boot 4.1 · Spring Data JPA · Spring Security · Flyway · Thymeleaf ·
Bootstrap 5 · Chart.js · PostgreSQL 16 · Lombok · Maven · JUnit 5 + Mockito + Testcontainers · Docker.

**MapStruct no se usa.** El mapeo entre entidades y DTOs se escribe a mano en la capa de
servicio. Ver `docs/08-decisiones.md`, D08.

No agregues dependencias nuevas sin dejarlo registrado en `docs/08-decisiones.md`.

## Reglas de oro

Estas reglas existen porque somos dos personas trabajando en paralelo con asistentes
distintos. Romper una de estas rompe el trabajo del otro.

1. **Nunca modifiques una migración de Flyway ya existente.** Si el esquema cambia, se
   crea una migración nueva con el número siguiente. Editar una migración ya aplicada
   deja la base de datos del compañero inconsistente y no hay forma limpia de arreglarlo.

2. **Nunca cambies una entidad sin su migración correspondiente**, en el mismo commit.

3. **La lógica de negocio vive en la capa de servicios.** Un controlador nunca llama a
   un repositorio directamente. Si necesitas una regla nueva, va en un `@Service`.

4. **Toda operación que modifique el estado de un equipo es `@Transactional`.** No hay
   excepciones a esto: es la razón por la que elegimos este stack.

5. **Ejecuta `./mvnw test` antes de cada commit.** Si algo falla, no se commitea.

6. **No trabajes sobre `main`.** Rama por funcionalidad, integración por pull request.

7. **Respeta las zonas de propiedad** definidas en `docs/06-flujo-trabajo.md`. Si
   necesitas tocar código del otro integrante, avísale antes; no lo hagas por tu cuenta.

8. **Toda entidad de negocio lleva `empresa_id`.** Ver `docs/02-arquitectura.md`,
   sección multi-tenencia. Se nos olvida una vez y después es imposible de agregar.

9. **Actualiza `docs/ESTADO.md`** al terminar una sesión de trabajo. Es lo que el
   asistente del otro integrante lee para saber qué está en curso.

10. **No inventes requisitos.** Si algo no está en `docs/05-epicas.md`, no se construye.
    Si falta información, pregunta antes de asumir.

## Antes de empezar a trabajar, lee

| Si vas a... | Lee primero |
|---|---|
| Entender el negocio o nombrar algo | `docs/01-contexto-negocio.md` y `docs/09-glosario.md` |
| Crear o modificar clases | `docs/02-arquitectura.md` y `docs/04-convenciones-codigo.md` |
| Tocar entidades o el esquema | `docs/03-modelo-datos.md` |
| Implementar una funcionalidad | `docs/05-epicas.md` |
| Hacer commits o abrir un PR | `docs/06-flujo-trabajo.md` |
| Escribir pruebas | `docs/07-pruebas.md` |
| Cualquier cosa | `docs/ESTADO.md`, para saber qué está en curso |
| Arrancar el proyecto por primera vez | `docs/11-arranque-claude-code.md` |

## Comandos

```bash
./mvnw spring-boot:run          # levantar la aplicación
./mvnw test                     # pruebas unitarias
./mvnw clean verify             # pruebas completas, incluidas las de integración
docker compose up -d db         # solo la base de datos
```

## Cómo quiero que trabajes

- Antes de escribir código, dime qué archivos vas a crear o modificar y espera confirmación.
- Cambios pequeños y verificables. Una funcionalidad por vez.
- Si detectas que algo que te pido rompe una de las reglas de oro, **dímelo en vez de hacerlo**.
- Si una decisión no está documentada y es relevante, propone agregarla a `docs/08-decisiones.md`.
- Responde en español.
