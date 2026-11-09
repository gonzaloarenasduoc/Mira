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
