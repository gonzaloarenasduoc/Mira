-- V3: categorías tomadas del catálogo de productos de una empresa del rubro (D19,
-- docs/referencias/catalogo-productos.xlsx). Las de V2 no cambian.
-- requiere_capacitacion: los escáneres láser, georradares y cámaras 3D se entregan con
-- capacitación; el resto no (supuesto declarado del equipo, como en V2).
INSERT INTO categoria (empresa_id, nombre, descripcion, requiere_capacitacion, creado_por)
SELECT e.id, c.nombre, c.descripcion, c.requiere_capacitacion, 'sistema'
FROM empresa e
CROSS JOIN (VALUES
    ('Accesorios', 'Baterías, cargadores, jalones, bases y otros accesorios', FALSE),
    ('Cursos', 'Cursos de uso de equipos y software', FALSE),
    ('Escáneres láser', 'Escáner que captura nubes de puntos en 3D', TRUE),
    ('Georradares', 'Equipo de detección de estructuras bajo la superficie', TRUE),
    ('Cámaras', 'Cámara para la captura 3D de espacios', TRUE),
    ('Servicios', 'Servicios prestados al cliente', FALSE),
    ('Registros facturas', 'Registros asociados a la facturación', FALSE)
) AS c (nombre, descripcion, requiere_capacitacion)
WHERE e.rut = '11.111.111-1';
