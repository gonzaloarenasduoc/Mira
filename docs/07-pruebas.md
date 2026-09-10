# Pruebas

Las pruebas no son un trámite del final: acreditan una de las cuatro competencias del
perfil de egreso y son la red que evita que uno rompa el trabajo del otro.

## Qué se prueba, en orden de prioridad

1. **Las reglas críticas del negocio.** Son las que no pueden fallar nunca.
2. Los servicios con lógica condicional.
3. Las consultas de repositorio que no son triviales.
4. Los controladores, solo en su comportamiento de rutas y permisos.

No se prueban getters, setters ni configuración del framework.

## Las reglas críticas

Estas deben tener prueba sí o sí:

- Un equipo no disponible **no puede** ser arrendado.
- Un equipo no puede estar comprometido en dos operaciones simultáneas.
- Al registrar un arriendo, el cambio de estado y el registro del movimiento ocurren
  juntos; si algo falla, no queda nada guardado.
- Al registrar el retorno, el equipo vuelve a quedar disponible.
- El número de serie es único por empresa.
- Un usuario sin el rol correspondiente no accede a la funcionalidad restringida.
- Ninguna consulta devuelve datos de otra empresa.

## Tipos

**Unitarias.** Servicios aislados, con Mockito para los repositorios. Rápidas, son la
mayoría.

**De integración.** Con Testcontainers levantando PostgreSQL real. Se usan para las
consultas y para verificar que las transacciones efectivamente revierten. No uses una base
en memoria: se comporta distinto a PostgreSQL y las pruebas mienten.

## Convenciones

Nombre del método: `metodo_condicion_resultadoEsperado`.

```java
@Test
void registrar_equipoArrendado_lanzaExcepcion() { ... }

@Test
void registrar_equipoDisponible_cambiaEstadoYCreaMovimiento() { ... }
```

Estructura interna: preparar, ejecutar, verificar. Una sola cosa por prueba.

## Regla

`./mvnw test` antes de cada commit. Si falla, no se commitea. Si una prueba del otro
integrante falla por un cambio tuyo, **no la borres ni la ignores**: avísale.
