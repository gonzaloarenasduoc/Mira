# Evidencias de la semana 13

Capturas de HU-19 (Sprint 4), tomadas con un navegador automatizado sobre la aplicación levantada
contra PostgreSQL 16, con los datos de las demostraciones anteriores. HU-19 se adelantó: se
desarrolló el 09-11-2026.

## Historial de operaciones de un cliente (HU-19, D21)

| Captura | Qué muestra |
|---|---|
| [01-clientes-con-enlace.png](01-clientes-con-enlace.png) | En el listado de clientes, la razón social abre la ficha |
| [02-historial-cliente.png](02-historial-cliente.png) | Ficha del cliente: la venta N° 4 y el arriendo N° 1, del más reciente al más antiguo, con equipos, estado y total |
| [03-historial-encargado.png](03-historial-encargado.png) | El encargado de bodega ve solo los arriendos del cliente (matriz de permisos) |

CF-09 se cumple completo: la venta registrada en la semana 12 queda en el historial del cliente.

## Adjuntar documentos a una operación (HU-23, D22)

Adelantada al 09-11-2026. Pruebas con el encargado de bodega sobre el arriendo N° 3 y con el vendedor
sobre la venta N° 4.

| Captura | Qué muestra |
|---|---|
| [04-ficha-arriendo-con-adjuntar.png](04-ficha-arriendo-con-adjuntar.png) | La ficha del arriendo ofrece Adjuntar documento |
| [05-formulario-documento.png](05-formulario-documento.png) | Formulario con tipo, número o folio, fecha y archivo |
| [06-archivo-renombrado-rechazado.png](06-archivo-renombrado-rechazado.png) | Un archivo que no es PDF, aunque se llame `.pdf`, se rechaza |
| [07-guia-adjuntada.png](07-guia-adjuntada.png) | Guía de despacho N° 1045 adjuntada al arriendo |
| [08-archivo-sobre-10mb.png](08-archivo-sobre-10mb.png) | Un archivo de 11 MB se rechaza con una página propia; no queda nada guardado |
| [09-factura-adjuntada-a-venta.png](09-factura-adjuntada-a-venta.png) | Factura N° 2210 adjuntada a la venta por el vendedor |

En la base quedaron los dos documentos con su tipo, folio, fecha, nombre original, ruta generada,
tipo de contenido, tamaño y el usuario que los subió; en el disco, los dos archivos bajo
`uploads/1/`. El encargado de bodega recibe 403 al intentar adjuntar a una venta. Los documentos se
consultan con HU-24 y HU-25.

## Documentos de un equipo (HU-25, CF-13)

| Captura | Qué muestra |
|---|---|
| [10-documentos-en-ficha-GS18.png](10-documentos-en-ficha-GS18.png) | CF-13: la guía de despacho del arriendo N° 3 aparece en la ficha del GS18-778201; también aparece en la del TS16-334512, el otro equipo del arriendo |
| [11-documentos-ficha-equipo-vendido.png](11-documentos-ficha-equipo-vendido.png) | La factura de la venta N° 4 aparece en la ficha del equipo vendido |
| [12-encargado-sin-documentos-de-venta.png](12-encargado-sin-documentos-de-venta.png) | El encargado de bodega no ve los documentos de ventas (D21); sí los de arriendos |

Un equipo sin operaciones con documentos, como el R12I-90311, muestra "Ninguna operación de este
equipo tiene documentos adjuntos".

## Documentos de una operación y su descarga (HU-24)

| Captura | Qué muestra |
|---|---|
| [13-documentos-en-ficha-arriendo.png](13-documentos-en-ficha-arriendo.png) | La ficha del arriendo N° 3 lista su guía de despacho; el nombre del archivo la descarga |
| [14-documentos-en-ficha-venta.png](14-documentos-en-ficha-venta.png) | La ficha de la venta N° 4 lista su factura |

Los archivos descargados son idénticos byte a byte a los subidos. Se entregan como adjunto
(`Content-Disposition: attachment`), con su nombre original, el tipo de contenido validado al subirlos
y `X-Content-Type-Options: nosniff`. El encargado de bodega descarga la guía del arriendo, pero recibe
404 con la factura de la venta (D21); un documento inexistente o de otra empresa también responde 404.

## Panel de indicadores: retornos vencidos (HU-28, CF-10)

| Captura | Qué muestra |
|---|---|
| [15-panel-retornos-vencidos.png](15-panel-retornos-vencidos.png) | CF-10: la jefatura entra al panel y el arriendo N° 2 aparece primero, destacado, con el cliente, el equipo y 5 días de atraso |

La opción Indicadores aparece en el menú de todos los roles (E009 es de lectura para todos). Los
arriendos que vencen hoy o más adelante no se muestran en esta sección: siguen en las alertas del
inicio (HU-16).

## Panel: equipos por bodega y estado (HU-26, D23) y PNF-02

| Captura | Qué muestra |
|---|---|
| [16-panel-con-un-anio-de-datos.png](16-panel-con-un-anio-de-datos.png) | Panel con un año de datos: los 22 retornos vencidos primero, y debajo los gráficos por bodega (barras apiladas) y por estado (dona) con su tabla; los 50 vendidos se informan aparte |
| [17-barra-1440.png](17-barra-1440.png), [18-barra-1366.png](18-barra-1366.png) | Con la opción Indicadores, la barra del administrador cabe en una línea; entre 1200 y 1399 px se oculta el nombre |

**PNF-02, carga del panel con un año de datos.** Base aparte (`mira_carga`) cargada con
[carga-un-anio.sql](carga-un-anio.sql): 5 bodegas, 500 equipos, 80 clientes, 1.200 arriendos en
365 días (1.630 líneas), 50 ventas y 3.650 movimientos; 60 equipos arrendados, 22 con retorno
vencido, 15 en mantención y 50 vendidos. 30 solicitudes consecutivas a `/indicadores` con sesión
iniciada, después de una de calentamiento, medidas con `curl` ([pnf02-tiempos.txt](pnf02-tiempos.txt)),
y 10 cargas completas en el navegador hasta que ambos gráficos quedaron dibujados:

| Medición | Mínimo | Mediana | Percentil 95 | Máximo | Criterio |
|---|---|---|---|---|---|
| Respuesta del servidor | 0,026 s | 0,032 s | 0,052 s | 0,059 s | < 3 s |
| Carga en el navegador con gráficos | — | 0,114 s | — | 0,141 s | < 3 s |

La tabla del panel cuadra con la base: 375 disponibles, 60 arrendados y 15 en mantención, 450 en
total. El caso de prueba pide medirlo con el registro de tiempos de la aplicación; la medición con
`curl` y con el navegador es equivalente para un solo usuario.
