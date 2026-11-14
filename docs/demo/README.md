# Datos de demostración

Carga un escenario completo para presentar Mira: seis meses de operación de la empresa ficticia de V2.

| Qué | Cantidad |
|---|---|
| Usuarios | Los cuatro roles: `admin@mira.cl`, `vendedor@mira.cl`, `jefatura@mira.cl` y `bodega@mira.cl` (contraseñas `admin123`, `vendedor123`, `jefatura123`, `bodega123`) |
| Bodegas | 3: Central (Providencia), Norte (Antofagasta) y Sur (Concepción) |
| Equipos | 32 de 7 categorías, con marcas y modelos del catálogo de referencia |
| Clientes | 8, ficticios, con RUT válido |
| Arriendos | Unos 330 cerrados en seis meses, 7 vigentes (uno con dos equipos) y 3 vencidos |
| Ventas | 3 |
| Traslados | 2 completos y 1 en tránsito |
| Mantención | 1 equipo |
| Documentos | 5 PDF ficticios, marcados "documento de demostración, sin validez tributaria" |
| Capacitaciones | 7: realizadas, programadas, una cancelada y un arriendo de dron sin capacitación, para que la ficha sugiera agendarla |

Las fechas son relativas al día de la carga: los vencidos, los vigentes y las capacitaciones
próximas siempre se ven al día. Los datos son coherentes con las reglas del sistema: cada cambio de
estado tiene su movimiento y ningún equipo tiene arriendos superpuestos.

## Cómo cargarlos

Sobre una base **vacía** (el script se detiene si ya hay bodegas):

```bash
# Con todo en Docker (opción C del README)
docker compose down -v            # solo si ya había datos
docker compose up --build -d
docs/demo/cargar-demo.sh docker

# Con la base en Docker o local y la aplicación con ./mvnw spring-boot:run (opciones A y B)
docs/demo/cargar-demo.sh local
```

Para volver al estado inicial durante una presentación basta con repetir la secuencia: la carga
tarda unos segundos.

Ningún dato corresponde a personas ni empresas reales. Estos datos no se cargan nunca en un
ambiente real.
