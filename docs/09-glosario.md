# Glosario

Vocabulario del negocio y su correspondencia en el código. Úsalo para nombrar: si el
término está acá, se usa así y no de otra forma.

| Término del negocio | En el código | Qué es |
|---|---|---|
| Equipo | `Equipo` | Unidad física individual, identificada por número de serie |
| Número de serie | `numeroSerie` | Identificador único del equipo, viene del fabricante |
| Categoría | `Categoria` | Tipo de instrumento: estación total, GPS RTK, dron, nivel |
| Bodega | `Bodega` | Lugar físico donde se almacenan equipos |
| Traslado | `Movimiento` (tipo `TRASLADO`) | Cambio de un equipo de una bodega a otra |
| Movimiento | `Movimiento` | Registro histórico de todo cambio de estado o ubicación |
| Estado | `EstadoEquipo` | Situación actual del equipo, enumerado |
| Disponibilidad | (calculada) | Si un equipo puede comprometerse. No es un campo |
| Arriendo | `Arriendo` | Entrega temporal con fecha de retorno comprometida |
| Retorno | `fechaRetornoReal` | Devolución efectiva del equipo arrendado |
| Venta | `Venta` | Salida definitiva del inventario |
| Operación | `Operacion` | Venta o arriendo. Concepto que agrupa a ambas |
| Capacitación | `Capacitacion` | Servicio de enseñanza asociado a una operación |
| Técnico | `Tecnico` | Persona de la empresa que imparte la capacitación |
| Cliente | `Cliente` | Empresa que compra o arrienda |
| Orden de compra | `Documento` (tipo `ORDEN_COMPRA`) | Documento que emite el cliente |
| Guía de despacho | `Documento` (tipo `GUIA_DESPACHO`) | Respalda la salida física del equipo |
| Factura | `Documento` (tipo `FACTURA`) | Documento tributario. **El sistema no la emite** |

## Términos de instrumental

Contexto para entender el dominio, no necesariamente entidades del sistema.

- **Estación total**: instrumento que mide ángulos y distancias. El más caro del catálogo.
- **GPS RTK**: receptor de posicionamiento de alta precisión, en tiempo real.
- **Nivel**: instrumento para determinar diferencias de altura.
- **Mira**: regla graduada que se usa junto al nivel. Da nombre al proyecto.
- **Dron**: aeronave para levantamientos fotogramétricos.

## Palabras que no se usan

| No uses | Usa |
|---|---|
| Producto, artículo, ítem | Equipo |
| Stock | Inventario, o disponibilidad |
| Almacén, pañol | Bodega |
| Reserva, préstamo | Arriendo |
| Emitir factura | Adjuntar factura |
