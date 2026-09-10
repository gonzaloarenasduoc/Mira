# Convenciones de código

## Idioma

**El dominio se nombra en español**, porque el vocabulario del negocio es español y la
contraparte lo entiende así: `Equipo`, `Arriendo`, `Bodega`, `numeroSerie`, `fechaRetorno`.

Los términos técnicos de Spring y Java se mantienen en inglés: `Repository`, `Service`,
`Controller`, `findBy`, `save`.

No mezcles: `equipoRepository.findByEstado(...)` está bien; `equipmentRepository` no.

## Nombres

| Elemento | Convención | Ejemplo |
|---|---|---|
| Entidad | Sustantivo singular | `Equipo`, `Arriendo` |
| Repositorio | Entidad + `Repository` | `EquipoRepository` |
| Servicio | Entidad + `Service` | `ArriendoService` |
| Controlador | Entidad + `Controller` | `EquipoController` |
| DTO de entrada | Acción + `Request` | `RegistrarArriendoRequest` |
| DTO de salida | Entidad + `Response` | `EquipoResponse` |
| Excepción | Descripción + `Exception` | `EquipoNoDisponibleException` |
| Plantilla | `dominio/accion.html` | `equipos/lista.html` |

## DTOs

**Las entidades JPA no salen de la capa de servicio.** Los controladores reciben y devuelven
DTOs. Exponer entidades directamente acopla la interfaz al esquema de base de datos y abre
problemas de serialización con las relaciones perezosas.

Usa `record` de Java para los DTOs.

## Validación

- Validación de formato en el DTO, con anotaciones de Bean Validation.
- Validación de reglas de negocio en el servicio, lanzando excepciones de dominio.

No repitas la validación de negocio en el controlador.

## Excepciones

Excepciones propias por caso de dominio, heredando de una base común en `common`. Se
traducen a respuestas HTTP en un `@ControllerAdvice` centralizado. No captures excepciones
solo para registrarlas y volver a lanzarlas.

## Consultas

Prefiere los métodos derivados de Spring Data (`findByBodegaIdAndEstado`). Si la consulta
es compleja, usa `@Query` con JPQL y déjala en el repositorio, nunca armada como texto en
el servicio.

Cuidado con el problema N+1: si una consulta recorre una colección perezosa, usa
`JOIN FETCH`.

## Lo que no se hace

- Lógica de negocio en plantillas Thymeleaf.
- Consultas a base de datos dentro de un bucle.
- Campos `String` para datos que son enumerados o fechas.
- Métodos de más de 40 líneas: si crece, extrae.
- Comentarios que explican **qué** hace el código. Comenta solo el **por qué**, cuando no
  sea evidente.
- Código muerto o comentado. Para eso está el control de versiones.
