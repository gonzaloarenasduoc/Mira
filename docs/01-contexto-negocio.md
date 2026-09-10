# Contexto de negocio

## La empresa

Comercializa y arrienda instrumental de geomensura: estaciones totales, receptores GPS RTK,
drones, niveles y accesorios. Son equipos de alto valor unitario. Sus clientes son empresas
mineras, constructoras y consultoras de topografía.

Opera con **más de una bodega**. Cuando el equipo lo amerita, la entrega incluye un servicio
de **capacitación** en su uso.

## El problema

El inventario se administra en planillas de cálculo actualizadas a mano y repartidas entre
bodegas. El registro es **genérico**: contabiliza cantidades por categoría, no unidades
individuales. Por eso la empresa no puede determinar dónde está un equipo específico, si
está arrendado, a qué cliente fue entregado ni cuándo debe retornar.

El diagnóstico en una frase: **una planilla describe un estado fijo, pero los equipos cambian
de estado y ubicación todos los días.** La herramienta no representa la naturaleza del negocio.

## Consecuencias

- Búsquedas manuales y llamados entre bodegas para ubicar un equipo.
- Demoras en responder consultas de disponibilidad, con pérdida de oportunidades.
- Arriendos con retorno vencido que nadie detecta, con activos inmovilizados.
- Documentación desvinculada del equipo al que corresponde.
- Falta de indicadores para decidir nuevas adquisiciones.

## La solución

Cada equipo se registra como **unidad única con número de serie**, con estado, ubicación e
historial trazables. Sobre ese registro se gestionan las operaciones de venta y arriendo,
las capacitaciones asociadas y la documentación de respaldo.

## Usuarios y qué hace cada uno

| Rol | Uso principal |
|---|---|
| Administrador | Gestiona usuarios, roles, bodegas y categorías |
| Encargado de bodega | Registra equipos, traslados, despachos y retornos |
| Vendedor | Consulta disponibilidad, registra ventas y arriendos, agenda capacitaciones |
| Administración | Adjunta y consulta la documentación de cada operación |
| Jefatura | Revisa indicadores de inventario y utilización |

## Fuera de alcance

No se construye, y no debe proponerse ni implementarse:

- Integración con el SII y emisión de documentos tributarios electrónicos.
- Cruce automático entre orden de compra, guía de despacho y factura.
- Aplicación móvil nativa.
- Módulo de calibración certificada de instrumentos.
- Integración con software contable.
- Facturación y cobranza.
- Migración masiva del inventario histórico.

**El sistema adjunta y relaciona documentos, no los emite.** Es una distinción importante
y debe respetarse en el código, en los nombres y en la interfaz.

## Datos pendientes

Las cifras reales de la empresa (número de bodegas, cantidad de equipos, operaciones
mensuales) se incorporan tras la reunión de levantamiento. Mientras tanto, no inventes
valores en datos de prueba que puedan confundirse con reales.
