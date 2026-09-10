# Casos de prueba

Base para el diseño de pruebas del proyecto y para el informe de pruebas de validación
comprometido como evidencia final.

**Pruebas funcionales:** verifican *qué* hace el sistema. Se derivan de los criterios de
aceptación de las épicas.

**Pruebas no funcionales:** verifican *cómo* lo hace. Rendimiento, seguridad, usabilidad,
compatibilidad, fiabilidad y mantenibilidad. No salen de las épicas: hay que definirlas
aparte, con un criterio medible.

---

# 1. Pruebas funcionales

## CF-01 · Arrendar un equipo disponible

**Épica:** E005 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Equipo GPS-123 en estado `DISPONIBLE` en Bodega Norte. Cliente registrado. |
| Pasos | 1. Ingresar como encargado de bodega. 2. Registrar arriendo del equipo GPS-123 al cliente, del 10 al 25 de marzo. 3. Confirmar. |
| Resultado esperado | El arriendo queda registrado. El equipo pasa a `ARRENDADO` y queda asociado al cliente. Se crea un movimiento de tipo `SALIDA_ARRIENDO`. |

## CF-02 · Rechazar el arriendo de un equipo no disponible

**Épica:** E005 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Equipo GPS-123 en estado `ARRENDADO`. |
| Pasos | Intentar registrar un segundo arriendo sobre el mismo equipo. |
| Resultado esperado | El sistema rechaza la operación con un mensaje que indica que el equipo no está disponible. No se crea el arriendo ni se altera el estado del equipo. |

Es **la** prueba del proyecto: si esta falla, el sistema reproduce el problema que vino a resolver.

## CF-03 · Atomicidad del registro de arriendo

**Épica:** E005 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Equipo disponible. Se simula una falla al registrar el movimiento. |
| Pasos | Registrar un arriendo forzando la excepción en el tercer paso. |
| Resultado esperado | No queda **nada** guardado: sin arriendo, sin cambio de estado, sin movimiento. La transacción revierte completa. |

## CF-04 · Registrar el retorno de un arriendo

**Épica:** E005 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Arriendo vigente sobre el equipo GPS-123. |
| Pasos | Registrar el retorno indicando Bodega Sur y fecha. |
| Resultado esperado | El equipo vuelve a `DISPONIBLE`, queda asociado a Bodega Sur, se registra la fecha de retorno real y se crea un movimiento de tipo `RETORNO`. |

## CF-05 · Impedir número de serie duplicado

**Épica:** E002 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Existe el equipo GPS-123 en la empresa. |
| Pasos | Intentar registrar otro equipo con el mismo número de serie. |
| Resultado esperado | Rechazo con mensaje de número de serie ya registrado. No se crea el equipo. |

## CF-06 · Permitir el mismo número de serie en otra empresa

**Épica:** E002 · **Prioridad:** media

| | |
|---|---|
| Precondición | Existe GPS-123 en la Empresa A. |
| Pasos | Registrar GPS-123 en la Empresa B. |
| Resultado esperado | Se permite. La unicidad del número de serie es **por empresa**, no global. |

Verifica que la restricción de unicidad esté bien definida para el modelo multi-empresa.

## CF-07 · Consulta de disponibilidad consolidada

**Épica:** E004 · **Prioridad:** alta

| | |
|---|---|
| Precondición | 3 equipos de categoría GPS RTK: uno disponible en Norte, uno disponible en Sur, uno arrendado con retorno el 25 de marzo. |
| Pasos | Buscar "GPS RTK". |
| Resultado esperado | Muestra 2 disponibles con su bodega y 1 arrendado con la fecha de retorno. La consulta no requiere contactar otras bodegas. |

## CF-08 · Traslado entre bodegas

**Épica:** E003 · **Prioridad:** media

| | |
|---|---|
| Precondición | Equipo disponible en Bodega Norte. |
| Pasos | Registrar traslado a Bodega Sur con fecha y responsable. |
| Resultado esperado | El equipo queda en Bodega Sur. Se registra el movimiento con origen, destino, fecha y responsable, y aparece en su historial. |

## CF-09 · Venta de un equipo

**Épica:** E006 · **Prioridad:** media

| | |
|---|---|
| Precondición | Equipo disponible. |
| Pasos | Registrar la venta a un cliente con fecha y valor. |
| Resultado esperado | El equipo pasa a `VENDIDO` y deja de aparecer en la consulta de disponibilidad. La operación queda en el historial del cliente. |

## CF-10 · Alerta de retorno vencido

**Épica:** E005 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Arriendo con fecha de retorno comprometida anterior a hoy, sin retorno registrado. |
| Pasos | Ingresar al panel de indicadores. |
| Resultado esperado | El arriendo aparece destacado como vencido, con el equipo, el cliente y los días de atraso. |

## CF-11 · Restricción de acceso por rol

**Épica:** E001 · **Prioridad:** alta

| | |
|---|---|
| Precondición | Usuario con rol vendedor. |
| Pasos | Intentar acceder a la administración de usuarios. |
| Resultado esperado | Acceso denegado. La funcionalidad tampoco aparece en el menú. |

## CF-12 · Advertencia por técnico ocupado

**Épica:** E007 · **Prioridad:** baja

| | |
|---|---|
| Precondición | El técnico ya tiene una capacitación agendada el 12 de marzo. |
| Pasos | Agendar otra capacitación con el mismo técnico ese día. |
| Resultado esperado | El sistema advierte del conflicto antes de confirmar. |

## CF-13 · Documento vinculado a la operación y al equipo

**Épica:** E008 · **Prioridad:** media

| | |
|---|---|
| Precondición | Arriendo registrado sobre el equipo GPS-123. |
| Pasos | Adjuntar una guía de despacho indicando folio y fecha. Luego abrir la ficha del equipo. |
| Resultado esperado | El documento aparece asociado a la operación y también en el historial documental del equipo. |

## CF-14 · Aislamiento entre empresas

**Épica:** transversal · **Prioridad:** alta

| | |
|---|---|
| Precondición | Equipos cargados en la Empresa A y en la Empresa B. |
| Pasos | Ingresar con un usuario de la Empresa A y listar equipos. |
| Resultado esperado | Solo aparecen equipos de la Empresa A. Ninguna consulta devuelve datos de la otra. |

---

# 2. Pruebas no funcionales

## Rendimiento

### PNF-01 · Tiempo de respuesta de la consulta de disponibilidad
**Criterio:** con 500 equipos cargados, la consulta responde en **menos de 2 segundos** en el percentil 95.
**Cómo se mide:** JMeter o k6 con 30 ejecuciones consecutivas.
**Por qué:** es la funcionalidad que resuelve el problema central. Si es lenta, el usuario vuelve al Excel.

### PNF-02 · Carga del panel de indicadores
**Criterio:** menos de **3 segundos** con datos de un año de operación.
**Cómo se mide:** medición directa con el registro de tiempos de la aplicación.

### PNF-03 · Concurrencia
**Criterio:** **10 usuarios simultáneos** operando sin que el tiempo de respuesta se degrade más del 50%.
**Cómo se mide:** prueba de carga con k6.
**Por qué:** dimensionado al tamaño real de la empresa, no a un número inventado.

## Seguridad

### PNF-04 · Almacenamiento de contraseñas
**Criterio:** ninguna contraseña se guarda en texto plano. Se almacena el hash con BCrypt.
**Cómo se mide:** inspección directa de la tabla de usuarios.

### PNF-05 · Acceso sin sesión
**Criterio:** toda URL protegida accedida sin sesión redirige al inicio de sesión.
**Cómo se mide:** prueba automatizada con MockMvc sobre el listado completo de rutas.

### PNF-06 · Referencia directa a objetos (IDOR)
**Criterio:** un usuario de la Empresa A que modifique manualmente el identificador en la URL para acceder a un equipo de la Empresa B recibe un error, **no los datos**.
**Cómo se mide:** prueba automatizada específica.
**Por qué:** es la vulnerabilidad más común en sistemas multi-empresa y la más fácil de dejar abierta.

### PNF-07 · Inyección SQL
**Criterio:** enviar `' OR 1=1 --` en el buscador no altera el resultado ni provoca error.
**Cómo se mide:** prueba manual documentada con captura.

### PNF-08 · Cookies de sesión
**Criterio:** la cookie de sesión tiene los atributos `HttpOnly` y `Secure`.
**Cómo se mide:** inspección con las herramientas de desarrollo del navegador.

## Usabilidad

### PNF-09 · Registro de un arriendo
**Criterio:** un usuario **sin entrenamiento previo** completa el registro de un arriendo en menos de **2 minutos** y en no más de **5 clics** desde el inicio.
**Cómo se mide:** prueba con 3 personas ajenas al proyecto, cronometrada.

### PNF-10 · Mensajes de error
**Criterio:** los mensajes usan el lenguaje del negocio. "El equipo GPS-123 no está disponible porque se encuentra arrendado hasta el 25 de marzo" es válido; una traza de excepción no lo es.
**Cómo se mide:** revisión de la lista completa de mensajes del sistema.

## Compatibilidad

### PNF-11 · Navegadores
**Criterio:** funciona correctamente en las versiones vigentes de Chrome, Firefox y Edge.
**Cómo se mide:** ejecución del conjunto de pruebas funcionales en cada navegador.

### PNF-12 · Resoluciones
**Criterio:** la interfaz es usable en 1366×768 (el computador de bodega) y en una pantalla de 390 px de ancho.
**Cómo se mide:** inspección visual con las herramientas del navegador.

## Fiabilidad

### PNF-13 · Recuperación ante caída de la base de datos
**Criterio:** si la base se cae durante el registro de un arriendo, al restablecerse no existe ningún registro parcial.
**Cómo se mide:** detener el contenedor de PostgreSQL en medio de la operación y verificar el estado.

### PNF-14 · Respaldo y restauración
**Criterio:** la base se respalda y se restaura completa, con los datos íntegros.
**Cómo se mide:** ejecución de `pg_dump` y `pg_restore` sobre un ambiente limpio, verificando conteos por tabla.

## Mantenibilidad

### PNF-15 · Cobertura de pruebas
**Criterio:** **70% o más** de cobertura en la capa de servicios, donde está la lógica de negocio.
**Cómo se mide:** informe de JaCoCo generado en la construcción.

### PNF-16 · Reproducibilidad del entorno
**Criterio:** una persona ajena al proyecto clona el repositorio y levanta el sistema en **menos de 15 minutos** siguiendo únicamente el README.
**Cómo se mide:** prueba real con alguien del curso.
**Por qué:** acredita directamente el indicador de implantación de la solución.

---

# 3. Cómo se automatizan

Las funcionales de lógica de negocio se automatizan con JUnit y Mockito. Las que tocan la
base de datos usan Testcontainers con PostgreSQL real.

```java
@Test
void registrar_equipoYaArrendado_lanzaExcepcionYNoModificaEstado() {
    // preparar
    Equipo equipo = unEquipoConEstado(EstadoEquipo.ARRENDADO);

    // ejecutar y verificar
    assertThrows(EquipoNoDisponibleException.class,
        () -> arriendoService.registrar(equipo.getId(), CLIENTE_ID, DESDE, HASTA));

    assertEquals(EstadoEquipo.ARRENDADO, equipo.getEstado());
    verify(arriendoRepository, never()).save(any());
}
```

Para la atomicidad hay que usar la base real, porque con repositorios simulados la
transacción no existe:

```java
@SpringBootTest
@Testcontainers
class ArriendoServiceIT {

    @Test
    void registrar_fallaAlCrearMovimiento_noPersisteNada() {
        // se fuerza la excepción en el registro del movimiento
        assertThrows(RuntimeException.class, () -> arriendoService.registrar(...));

        assertTrue(arriendoRepository.findAll().isEmpty());
        assertEquals(EstadoEquipo.DISPONIBLE,
                     equipoRepository.findById(equipoId).orElseThrow().getEstado());
    }
}
```

Las no funcionales de rendimiento y usabilidad se ejecutan y documentan de forma manual,
con evidencia (mediciones, capturas) en el informe de pruebas.

# 4. Registro de resultados

Cada ejecución se registra con: identificador del caso, fecha, versión probada, resultado
(conforme o no conforme), evidencia y, si corresponde, el defecto detectado y la corrección
aplicada.

Ese registro es lo que acredita el indicador de calidad **1.3, desarrollar mejoras al
producto en base al resultado de las pruebas**. Sin él, las pruebas quedan en el aire.
