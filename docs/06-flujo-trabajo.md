# Flujo de trabajo

Este documento existe porque somos dos personas trabajando en paralelo, cada una con su
propio asistente. La mayoría de los conflictos se evitan respetando lo que está acá.

## Zonas de propiedad

| Área | Responsable | Paquetes |
|---|---|---|
| Modelo de datos y lógica de negocio | Joaquín | `inventario`, `operaciones`, `common`, entidades y servicios en general |
| Interfaz, indicadores y documentos | Gonzalo | `documentos`, `indicadores`, controladores, plantillas Thymeleaf, recursos estáticos |
| Compartido, requiere acuerdo | Ambos | `config`, `pom.xml`, migraciones Flyway, `usuarios` |

Trabajar dentro de tu zona no requiere avisar. **Tocar la zona del otro o la zona
compartida sí lo requiere**, antes de hacerlo, no después.

Si le pides a Claude Code algo que implique modificar archivos fuera de tu zona, que te lo
advierta y se detenga.

## Ramas

```
main                          estable, siempre debe compilar y pasar las pruebas
feature/E005-arriendos        una rama por épica o por funcionalidad
fix/validacion-serie          correcciones
```

Nunca se commitea directo en `main`.

## Commits

Formato: `tipo: descripción en presente`

```
feat: registrar arriendo con validación de disponibilidad
fix: corregir cálculo de disponibilidad con arriendos vencidos
docs: actualizar modelo de datos con entidad Movimiento
test: agregar pruebas de ArriendoService
refactor: extraer validación de estado a método privado
```

Tipos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`.

Commits pequeños y con sentido propio. Un commit que dice "avances" no sirve para nada,
y el historial del repositorio es una evidencia evaluada.

## Pull requests

Toda integración a `main` pasa por PR con revisión del otro integrante. La revisión cruzada
cumple dos funciones: mejora la calidad y asegura que ambos conozcan todo el sistema, que
es lo que necesitan para la defensa.

Antes de abrir un PR:

1. `./mvnw clean verify` pasa sin errores.
2. `docs/ESTADO.md` está actualizado.
3. Si cambió el esquema, hay una migración nueva (no editada).
4. Si cambió una decisión de arquitectura, está registrada en `docs/08-decisiones.md`.

## Migraciones Flyway

```
V1__esquema_inicial.sql
V2__datos_iniciales.sql
V3__agregar_tabla_capacitaciones.sql
```

**Una migración aplicada nunca se modifica.** Si necesitas corregir algo, creas la
siguiente. Editar una ya aplicada deja la base del compañero en un estado que Flyway
detecta como inconsistente y que solo se arregla borrando la base.

Antes de crear una migración, sincroniza con `main` para no repetir número de versión.

## Ritmo

- Iteraciones quincenales, cada una cierra con algo demostrable.
- Sincronización dos veces por semana entre los integrantes.
- Validación con la empresa en las semanas 6 y 11.
- Monitoreos de la asignatura en las semanas 6, 11 y 17.
