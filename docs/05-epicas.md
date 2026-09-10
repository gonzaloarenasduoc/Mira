# Épicas

Nueve épicas, dimensionadas mediante Poker Planning en escala de Fibonacci, con un total
de 100 puntos de función. **Si algo no está en este documento, no forma parte del alcance.**

## E001 · Gestión de usuarios y control de acceso — 8 PF

> Como administrador del sistema quiero gestionar los usuarios y asignarles un rol para que
> cada persona acceda únicamente a las funcionalidades que le corresponden.

- Crear, editar y desactivar usuarios.
- Cada usuario tiene un rol: administrador, encargado de bodega o vendedor.
- Autenticación con credenciales; contraseñas almacenadas cifradas.
- Un usuario sin el rol correspondiente no accede a una funcionalidad restringida.

## E002 · Registro de equipos por número de serie — 13 PF

> Como encargado de bodega quiero registrar cada equipo como una unidad única identificada
> por su número de serie para conocer su estado y ubicación de forma individual.

- Registro con categoría, marca, modelo, número de serie y bodega.
- El número de serie es único por empresa; el sistema impide duplicados.
- Cada equipo mantiene un estado: disponible, arrendado, vendido, en tránsito o en mantención.
- Ficha del equipo con su información y su historial de movimientos.

## E003 · Gestión de bodegas y traslados — 8 PF

> Como encargado de bodega quiero registrar el traslado de un equipo entre bodegas para que
> el inventario refleje su ubicación real en todo momento.

- Administración de las bodegas de la empresa.
- Registro de traslado con origen, destino, fecha y responsable.
- Al confirmar, el equipo queda asociado a la bodega de destino.
- El historial de traslados queda registrado y es consultable.

## E004 · Consulta de disponibilidad consolidada — 13 PF

> Como vendedor quiero consultar la disponibilidad de un equipo en todas las bodegas desde
> una sola pantalla para responder de inmediato a la solicitud de un cliente.

- Búsqueda por categoría, marca, modelo o número de serie.
- El resultado muestra cantidad disponible por bodega y estado de cada unidad.
- Los equipos arrendados se muestran con su fecha comprometida de retorno.
- La consulta se resuelve sin contactar a otras bodegas.

## E005 · Gestión de arriendos y control de retorno — 21 PF

> Como encargado de bodega quiero registrar los arriendos y controlar sus fechas de retorno
> para evitar que un equipo permanezca inmovilizado en un cliente sin ser detectado.

- Registro con equipo, cliente, fecha de inicio, fecha de retorno comprometida y tarifa.
- **El sistema impide arrendar un equipo que no esté disponible.**
- Al registrar el arriendo, el equipo cambia de estado y queda asociado al cliente.
- Al registrar el retorno, el equipo vuelve a quedar disponible en la bodega indicada.
- Alerta cuando la fecha de retorno se aproxima o está vencida.

Es la épica más grande y la que concentra la lógica transaccional del sistema.

## E006 · Gestión de ventas — 8 PF

> Como vendedor quiero registrar la venta de un equipo para que el inventario refleje su
> salida definitiva y quede constancia del cliente.

- Registro con equipo, cliente, fecha y valor.
- El equipo vendido cambia de estado y deja de aparecer como disponible.
- La operación queda asociada al cliente y es consultable en su historial.

## E007 · Agendamiento de capacitaciones — 8 PF

> Como vendedor quiero agendar la capacitación asociada a una operación para que quede
> planificada y vinculada al equipo entregado.

- Agendamiento con operación asociada, cliente, fecha, lugar y técnico responsable.
- La capacitación queda vinculada a la venta o arriendo que la origina.
- El sistema advierte si el técnico ya tiene una capacitación en la misma fecha.
- Calendario de capacitaciones programadas.

## E008 · Gestión documental de las operaciones — 13 PF

> Como encargado de administración quiero adjuntar y consultar los documentos de cada
> operación para reconstruir su historial sin recurrir a archivos externos.

- Adjuntar orden de compra, guía de despacho y factura a una operación.
- Cada documento registra tipo, número o folio, fecha y archivo asociado.
- Los documentos quedan vinculados a la operación y al equipo correspondiente.
- Consulta de todos los documentos asociados a un equipo.

**El sistema no emite documentos tributarios. Solo los almacena y relaciona.**

## E009 · Panel de indicadores y alertas — 8 PF

> Como jefatura quiero visualizar el estado general del inventario y sus indicadores para
> tomar decisiones con información real.

- Cantidad de equipos por bodega y por estado.
- Nivel de utilización de los equipos y arriendos vigentes.
- Alertas de retorno vencido presentadas de forma destacada.

## Distribución en el tiempo

| Semana | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 |
|---|---|---|---|---|---|---|---|---|---|---|
| E001 | X | X | | | | | | | | |
| E002 | | X | X | X | | | | | | |
| E003 | | | | X | X | | | | | |
| E004 | | | | | X | X | | | | |
| E005 | | | | | | X | X | X | | |
| E006 | | | | | | | | X | | |
| E007 | | | | | | | | | X | |
| E008 | | | | | | | | | X | X |
| E009 | | | | | | | | | | X |

Semana 15: cierre de E009, pruebas integradas y corrección de observaciones.
