-- V2: datos mínimos para que el sistema se pueda usar al levantarlo por primera vez.
-- Esta migración no se edita nunca.
--
-- La empresa es ficticia y su RUT no corresponde a una empresa real: no hay contraparte, y los
-- datos de demostración no deben confundirse con datos reales (docs/01-contexto-negocio.md).
--
-- Contraseña inicial del administrador: admin123, la del README (solo para demostración; en un
-- ambiente real se cambia antes de habilitar el acceso).
-- Se guarda únicamente su hash BCrypt, generado con BCryptPasswordEncoder de Spring Security.

INSERT INTO empresa (rut, razon_social, nombre_fantasia, creado_por)
VALUES ('11.111.111-1', 'Empresa de Demostración Geomensura SpA', 'Geomensura Demo', 'sistema');

INSERT INTO usuario (empresa_id, nombre, apellido, email, password_hash, rol, creado_por)
SELECT id, 'Administrador', 'Mira', 'admin@mira.cl',
       '$2a$10$csQQAVNCH07lW9R.yygp4uOQosxVqkjUdqCAUr6s0POMaC.x3nFnm',
       'ADMINISTRADOR', 'sistema'
FROM empresa
WHERE rut = '11.111.111-1';

-- requiere_capacitacion: las estaciones totales, los GPS RTK y los drones se entregan con
-- capacitación; el nivel es un instrumento de uso simple (supuesto declarado del equipo).
INSERT INTO categoria (empresa_id, nombre, descripcion, requiere_capacitacion, creado_por)
SELECT e.id, c.nombre, c.descripcion, c.requiere_capacitacion, 'sistema'
FROM empresa e
CROSS JOIN (VALUES
    ('Estación total', 'Instrumento que mide ángulos y distancias', TRUE),
    ('GPS RTK', 'Receptor de posicionamiento de alta precisión en tiempo real', TRUE),
    ('Dron', 'Aeronave para levantamientos fotogramétricos', TRUE),
    ('Nivel', 'Instrumento para determinar diferencias de altura', FALSE)
) AS c (nombre, descripcion, requiere_capacitacion)
WHERE e.rut = '11.111.111-1';
