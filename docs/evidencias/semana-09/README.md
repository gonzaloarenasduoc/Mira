# Evidencias de la semana 9

Capturas del Sprint 3 (HU-09 a HU-13), planificado para las semanas 10 y 11 y adelantado a la
semana 9 porque el Sprint 2 quedó cerrado en la semana 8. Se tomaron con un navegador automatizado sobre la aplicación
levantada contra PostgreSQL 16 con las migraciones V1 y V2. Los arriendos que se ven en las capturas
de disponibilidad se cargaron con SQL en la base local de verificación, porque el registro de
arriendos llega en el Sprint 4 (HU-14); no forman parte de ninguna migración.

## Traslados (HU-09 y HU-10)

| Captura | Qué muestra |
|---|---|
| [01-ficha-disponible.png](01-ficha-disponible.png) | Equipo disponible en Bodega Norte, con el botón Trasladar |
| [02-traslado-validacion.png](02-traslado-validacion.png) | HU-09 formulario con origen y responsable; destino obligatorio |
| [03-ficha-en-transito.png](03-ficha-en-transito.png) | HU-09 la salida deja el equipo en tránsito, aún asignado al origen |
| [04-traslados-en-transito.png](04-traslados-en-transito.png) | Equipos en tránsito con Confirmar llegada |
| [05-llegada-confirmada.png](05-llegada-confirmada.png) | HU-10 historial con la salida y la llegada |
| [06-ficha-en-destino.png](06-ficha-en-destino.png) | CF-08 el equipo queda disponible en Bodega Sur, con ambos movimientos |
| [07-historial-filtrado.png](07-historial-filtrado.png) | HU-10 historial filtrado por bodega |
| [08-historial-fechas-invalidas.png](08-historial-fechas-invalidas.png) | HU-10 rango de fechas inválido, mensaje del negocio |
| [09-ficha-vendedor.png](09-ficha-vendedor.png) | E003 el vendedor ve la ficha sin el botón Trasladar |
| [10-traslados-vendedor.png](10-traslados-vendedor.png) | E003 el vendedor consulta los traslados, sin confirmar llegadas |
| [17-vendedor-sin-acceso-traslado.png](17-vendedor-sin-acceso-traslado.png) | El servidor niega el formulario de traslado al vendedor (403) |

## Búsqueda y disponibilidad (HU-11 a HU-13)

| Captura | Qué muestra |
|---|---|
| [11-busqueda-marca.png](11-busqueda-marca.png) | HU-11 búsqueda parcial por marca ("leica") |
| [12-busqueda-inyeccion.png](12-busqueda-inyeccion.png) | PNF-07 `' OR 1=1 --` no altera el resultado ni provoca error |
| [13-inicio-vendedor.png](13-inicio-vendedor.png) | Menú del vendedor con Disponibilidad y Traslados |
| [14-disponibilidad.png](14-disponibilidad.png) | HU-12 disponibles por bodega; HU-13 retorno comprometido y arriendo vencido en rojo |
| [15-disponibilidad-leica.png](15-disponibilidad-leica.png) | HU-11 y HU-12 la búsqueda aplicada a la consulta consolidada |
| [16-disponibilidad-390px.png](16-disponibilidad-390px.png) | PNF-12 la consulta en una pantalla de 390 px |

## Mediciones

**PNF-01, tiempo de respuesta de la consulta de disponibilidad.** Base aparte con 500 equipos en
cinco bodegas, 100 de ellos con arriendo vigente y 17 en mantención. 30 solicitudes consecutivas a
`/disponibilidad` con sesión iniciada, después de una de calentamiento, medidas con `curl`:

| Consulta | Mínimo | Mediana | Percentil 95 | Máximo | Criterio |
|---|---|---|---|---|---|
| Sin filtros (500 equipos) | 0,137 s | 0,156 s | 0,202 s | 0,231 s | < 2 s |
| "leica" y una categoría | — | — | 0,021 s | 0,026 s | < 2 s |

La consulta informó 383 unidades disponibles, que es el valor esperado: 500 menos 100 arrendadas y
17 en mantención. El caso de prueba pide JMeter o k6; la medición con `curl` es equivalente para
un solo usuario y la prueba de concurrencia (PNF-03) sigue pendiente.

**PNF-08, cookie de sesión.** Cabecera `Set-Cookie` de `/login` en cada perfil:

```
perfil demo:  JSESSIONID=…; Path=/; Secure; HttpOnly; SameSite=Lax
perfil local: JSESSIONID=…; Path=/; HttpOnly
```

En local no se usa `Secure` porque la aplicación corre con HTTP y el navegador no enviaría la cookie.
