-- Datos de demostración de Mira, para la presentación y los videos.
--
-- Se cargan sobre una base recién creada por Flyway (V1 a V3), con cargar-demo.sh. No es una
-- migración: no se ejecuta sola y nunca debe cargarse en un ambiente real.
--
-- Todo es ficticio: la empresa es la de V2, y los clientes, RUT, personas y documentos no
-- corresponden a nadie real. Las marcas y modelos de los equipos vienen del catálogo de referencia
-- (docs/referencias), sin precios reales.
--
-- Las fechas son relativas al día en que se carga (CURRENT_DATE), para que los arriendos vencidos,
-- los vigentes y las capacitaciones próximas se vean al día en cualquier fecha de presentación.
-- El azar usa una semilla fija: dos cargas el mismo día producen los mismos datos.

\set ON_ERROR_STOP on

-- Ayudas para registrar operaciones como lo hace la aplicación: cada cambio de estado de un equipo
-- deja su movimiento, y el estado y la bodega del equipo quedan coherentes con su historial.

CREATE FUNCTION pg_temp.mover(e BIGINT, eq BIGINT, t VARCHAR, f TIMESTAMP, origen BIGINT, destino BIGINT,
                              op BIGINT, u BIGINT, obs VARCHAR) RETURNS VOID AS $$
    INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_origen_id, bodega_destino_id,
                            operacion_id, usuario_id, observacion, creado_por, creado_en)
    VALUES (e, eq, t, f, origen, destino, op, u, obs, 'demo', f);
$$ LANGUAGE sql;

-- Arriendo de uno o más equipos. Si retorno es NULL, el arriendo sigue vigente.
CREATE FUNCTION pg_temp.arrendar(e BIGINT, u BIGINT, cli BIGINT, eqs BIGINT[], ini DATE, fin DATE,
                                 retorno DATE, bodega_retorno BIGINT, oc VARCHAR) RETURNS BIGINT AS $$
DECLARE
    oid BIGINT; eq BIGINT; b BIGINT; tarifa NUMERIC;
BEGIN
    INSERT INTO operacion (empresa_id, tipo, cliente_id, usuario_id, fecha, creado_por, creado_en)
    VALUES (e, 'ARRIENDO', cli, u, ini, 'demo', ini + TIME '09:30') RETURNING id INTO oid;
    INSERT INTO arriendo (id, fecha_inicio, fecha_retorno_comprometida, numero_orden_compra, creado_por, creado_en)
    VALUES (oid, ini, fin, oc, 'demo', ini + TIME '09:30');
    FOREACH eq IN ARRAY eqs LOOP
        SELECT bodega_id, tarifa_diaria INTO b, tarifa FROM equipo WHERE id = eq;
        INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario, fecha_retorno_real, creado_por, creado_en)
        VALUES (oid, eq, tarifa, retorno, 'demo', ini + TIME '09:30');
        PERFORM pg_temp.mover(e, eq, 'SALIDA_ARRIENDO', ini + TIME '10:00', b, NULL, oid, u, NULL);
        IF retorno IS NULL THEN
            UPDATE equipo SET estado = 'ARRENDADO' WHERE id = eq;
        ELSE
            PERFORM pg_temp.mover(e, eq, 'RETORNO_ARRIENDO', retorno + TIME '17:00', NULL,
                                  COALESCE(bodega_retorno, b), oid, u, NULL);
            UPDATE equipo SET bodega_id = COALESCE(bodega_retorno, b) WHERE id = eq;
        END IF;
    END LOOP;
    RETURN oid;
END $$ LANGUAGE plpgsql;

CREATE FUNCTION pg_temp.vender(e BIGINT, u BIGINT, cli BIGINT, eq BIGINT, f DATE, valor NUMERIC, oc VARCHAR)
    RETURNS BIGINT AS $$
DECLARE
    oid BIGINT; b BIGINT;
BEGIN
    INSERT INTO operacion (empresa_id, tipo, cliente_id, usuario_id, fecha, creado_por, creado_en)
    VALUES (e, 'VENTA', cli, u, f, 'demo', f + TIME '11:00') RETURNING id INTO oid;
    INSERT INTO venta (id, numero_orden_compra, creado_por, creado_en) VALUES (oid, oc, 'demo', f + TIME '11:00');
    SELECT bodega_id INTO b FROM equipo WHERE id = eq;
    INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario, creado_por, creado_en)
    VALUES (oid, eq, valor, 'demo', f + TIME '11:00');
    PERFORM pg_temp.mover(e, eq, 'SALIDA_VENTA', f + TIME '12:00', b, NULL, oid, u, NULL);
    UPDATE equipo SET estado = 'VENDIDO' WHERE id = eq;
    RETURN oid;
END $$ LANGUAGE plpgsql;

DO $$
DECLARE
    hoy DATE := CURRENT_DATE;
    e BIGINT; adm BIGINT; ven BIGINT; jef BIGINT; bod BIGINT;
    central BIGINT; norte BIGINT; sur BIGINT;
    cli BIGINT[]; eqs BIGINT[]; corte DATE[];
    eq BIGINT; ini DATE; dur INT; ret DATE; b_ret BIGINT; uso INT; i INT;
    op_atacama BIGINT; op_m300 BIGINT; op_blk BIGINT; op_sdi BIGINT; op_venta_ts13 BIGINT; op_venta_sv1 BIGINT;
    op_vencido BIGINT; op_nivel BIGINT; op_dron_norte BIGINT;
    andres BIGINT; camila BIGINT; diego BIGINT;
BEGIN
    PERFORM setseed(0.2026);
    SELECT id INTO e FROM empresa WHERE rut = '11.111.111-1';
    IF EXISTS (SELECT 1 FROM bodega WHERE empresa_id = e) THEN
        RAISE EXCEPTION 'La base ya tiene datos: los datos de demostración se cargan sobre una base vacía.';
    END IF;
    SELECT id INTO adm FROM usuario WHERE email = 'admin@mira.cl';

    -- Usuarios de los otros tres roles (contraseñas solo de demostración, las mismas del README).
    INSERT INTO usuario (empresa_id, nombre, apellido, email, password_hash, rol, creado_por) VALUES
        (e, 'Valentina', 'Soto', 'vendedor@mira.cl', '$2a$10$iOGUyk4c9.feZHkv.WAkuektZlNIyAZIjdo3JRhHH3XPiu8kBltcy', 'VENDEDOR', 'demo'),
        (e, 'Rodrigo', 'Muñoz', 'jefatura@mira.cl', '$2a$10$c2eCRbW.iEG9CVkx4dxxwOckAafmkM1b6pXQnnxzn5Bgta0LWqmhS', 'JEFATURA', 'demo'),
        (e, 'Pedro', 'Ibarra', 'bodega@mira.cl', '$2a$10$7yz8LWME3RKdB72jxzkJJOyLRNLJg.NU7Zxomg3OR3Q/ZdK0Bp.0m', 'ENCARGADO_BODEGA', 'demo');
    SELECT id INTO ven FROM usuario WHERE email = 'vendedor@mira.cl';
    SELECT id INTO jef FROM usuario WHERE email = 'jefatura@mira.cl';
    SELECT id INTO bod FROM usuario WHERE email = 'bodega@mira.cl';

    INSERT INTO bodega (empresa_id, nombre, direccion, comuna, creado_por, creado_en) VALUES
        (e, 'Bodega Central', 'Av. Providencia 1234', 'Providencia', 'demo', hoy - 200),
        (e, 'Bodega Norte', 'Av. Grecia 2850', 'Antofagasta', 'demo', hoy - 200),
        (e, 'Bodega Sur', 'Calle Colo Colo 455', 'Concepción', 'demo', hoy - 200);
    SELECT id INTO central FROM bodega WHERE empresa_id = e AND nombre = 'Bodega Central';
    SELECT id INTO norte FROM bodega WHERE empresa_id = e AND nombre = 'Bodega Norte';
    SELECT id INTO sur FROM bodega WHERE empresa_id = e AND nombre = 'Bodega Sur';

    INSERT INTO cliente (empresa_id, rut, razon_social, giro, direccion, comuna, contacto_nombre, contacto_email,
                         contacto_telefono, creado_por, creado_en) VALUES
        (e, '76.123.456-0', 'Constructora Los Andes SpA', 'Construcción de obras civiles', 'Av. Apoquindo 4500', 'Las Condes', 'Marcela Fuentes', 'mfuentes@ejemplo.cl', '+56 2 2345 6789', 'demo', hoy - 190),
        (e, '96.845.210-K', 'Minera Pampa Norte S.A.', 'Explotación de minas', 'Av. Granaderos 1450', 'Calama', 'Jorge Castillo', 'jcastillo@ejemplo.cl', '+56 55 234 5678', 'demo', hoy - 190),
        (e, '76.555.444-6', 'Topografía Atacama Ltda.', 'Servicios de topografía', 'Los Carrera 820', 'Copiapó', 'Daniela Rivas', 'drivas@ejemplo.cl', '+56 52 221 3344', 'demo', hoy - 185),
        (e, '77.210.338-7', 'Ingeniería Vial del Sur Ltda.', 'Ingeniería y consultoría vial', 'Barros Arana 1020', 'Concepción', 'Felipe Navarro', 'fnavarro@ejemplo.cl', '+56 41 290 1122', 'demo', hoy - 180),
        (e, '76.984.120-2', 'Consultora GeoAndes SpA', 'Consultoría en geomática', 'Av. Libertad 1348', 'Viña del Mar', 'Carolina Pizarro', 'cpizarro@ejemplo.cl', '+56 32 268 9900', 'demo', hoy - 175),
        (e, '77.450.916-K', 'Inmobiliaria Costanera Ltda.', 'Desarrollo inmobiliario', 'Av. Vitacura 2939', 'Vitacura', 'Tomás Herrera', 'therrera@ejemplo.cl', '+56 2 2789 0011', 'demo', hoy - 160),
        (e, '76.301.875-K', 'Agrícola Valle Central SpA', 'Agricultura y riego tecnificado', 'Ruta 5 Sur km 205', 'Talca', 'Patricia Morales', 'pmorales@ejemplo.cl', '+56 71 223 4455', 'demo', hoy - 150),
        (e, '77.812.094-1', 'Sociedad de Ingeniería Hidráulica Ltda.', 'Obras hidráulicas', 'Av. Brasil 1234', 'Valparaíso', 'Ignacio Vega', 'ivega@ejemplo.cl', '+56 32 245 6677', 'demo', hoy - 140);
    SELECT array_agg(id ORDER BY id) INTO cli FROM cliente WHERE empresa_id = e;

    -- 32 equipos con número de serie, ingresados hace seis meses.
    INSERT INTO equipo (empresa_id, categoria_id, bodega_id, numero_serie, marca, modelo, estado,
                        fecha_adquisicion, valor_adquisicion, tarifa_diaria, observaciones, creado_por, creado_en)
    SELECT e, c.id, CASE x.b WHEN 'C' THEN central WHEN 'N' THEN norte ELSE sur END, x.serie, x.marca, x.modelo,
           'DISPONIBLE', hoy - 200, x.valor, x.tarifa, x.obs, 'demo', hoy - 185 + TIME '09:00'
    FROM (VALUES
        (1,  'TS16-334512',   'Estación total',  'Leica',       'TS16',           'C', 18500000, 95000,  NULL),
        (2,  'TS16-334988',   'Estación total',  'Leica',       'TS16',           'N', 18500000, 95000,  NULL),
        (3,  'TS13-221045',   'Estación total',  'Leica',       'TS13',           'C', 12900000, 75000,  NULL),
        (4,  'AX10M-240311',  'Estación total',  'STEC',        'AXIS 10M',       'C',  9800000, 60000,  NULL),
        (5,  'AX10M-240327',  'Estación total',  'STEC',        'AXIS 10M',       'S',  9800000, 60000,  NULL),
        (6,  'AXSP1-230918',  'Estación total',  'STEC',        'AXIS SP 1"',     'N', 11200000, 70000,  NULL),
        (7,  'AX9-221104',    'Estación total',  'STEC',        'AXIS 9',         'S',  7600000, 50000,  NULL),
        (8,  'GS18-778201',   'GPS RTK',         'Leica',       'GS18 T',         'C', 14200000, 70000,  NULL),
        (9,  'GS18-778455',   'GPS RTK',         'Leica',       'GS18 T',         'N', 14200000, 70000,  NULL),
        (10, 'SDIP-310552',   'GPS RTK',         'STEC',        'SDi PRO',        'C',  8900000, 55000,  NULL),
        (11, 'SDIP-310561',   'GPS RTK',         'STEC',        'SDi PRO',        'N',  8900000, 55000,  NULL),
        (12, 'SDIP-310578',   'GPS RTK',         'STEC',        'SDi PRO',        'S',  8900000, 55000,  NULL),
        (13, 'SV1-290117',    'GPS RTK',         'STEC',        'SV1',            'C',  6900000, 45000,  NULL),
        (14, 'SV1-290124',    'GPS RTK',         'STEC',        'SV1',            'N',  6900000, 45000,  NULL),
        (15, 'SEL-270803',    'GPS RTK',         'STEC',        'SE LITE',        'S',  4800000, 35000,  NULL),
        (16, 'M300-1A2B3C',   'Dron',            'DJI',         'Matrice 300 RTK','C', 21500000, 120000, 'Incluye dos juegos de baterías TB60'),
        (17, 'M300-4D5E6F',   'Dron',            'DJI',         'Matrice 300 RTK','N', 21500000, 120000, NULL),
        (18, 'P4R-0F7712',    'Dron',            'DJI',         'Phantom 4 RTK',  'C',  7900000, 80000,  NULL),
        (19, 'ALS32-150422',  'Nivel',           'STEC',        'ALS32',          'C',   690000, 15000,  NULL),
        (20, 'ALS32-150436',  'Nivel',           'STEC',        'ALS32',          'S',   690000, 15000,  NULL),
        (21, 'DLS03-180209',  'Nivel',           'STEC',        'DLS 03',         'C',  2400000, 25000,  'Nivel digital'),
        (22, 'NA2-55021',     'Nivel',           'Leica',       'NA2',            'N',  1500000, 18000,  NULL),
        (23, 'BLK360-2201457','Escáneres láser', 'Leica',       'BLK360 G2',      'C', 27900000, 150000, NULL),
        (24, 'RTC360-2140889','Escáneres láser', 'Leica',       'RTC360',         'C', 64500000, 220000, NULL),
        (25, 'FJDP2-P2A0331', 'Escáneres láser', 'FJDynamics',  'P2',             'N', 19800000, 130000, NULL),
        (26, 'GS8000-GR0112', 'Georradares',     'Proceq',      'GS8000',         'C', 32400000, 110000, NULL),
        (27, 'MTP3-PRO3A1092','Cámaras',         'Matterport',  'Pro3',           'C',  5900000, 65000,  NULL),
        (28, 'TS16-335102',   'Estación total',  'Leica',       'TS16',           'S', 18500000, 95000,  NULL),
        (29, 'SV1-290131',    'GPS RTK',         'STEC',        'SV1',            'S',  6900000, 45000,  NULL),
        (30, 'ALS32-150447',  'Nivel',           'STEC',        'ALS32',          'N',   690000, 15000,  NULL),
        (31, 'SDIP-310590',   'GPS RTK',         'STEC',        'SDi PRO',        'C',  8900000, 55000,  NULL),
        (32, 'AX10M-240352',  'Estación total',  'STEC',        'AXIS 10M',       'C',  9800000, 60000,  NULL)
    ) AS x (n, serie, cat, marca, modelo, b, valor, tarifa, obs)
    JOIN categoria c ON c.empresa_id = e AND c.nombre = x.cat
    ORDER BY x.n;
    SELECT array_agg(id ORDER BY id) INTO eqs FROM equipo WHERE empresa_id = e;
    INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_destino_id, usuario_id, observacion, creado_por, creado_en)
    SELECT e, q.id, 'INGRESO', hoy - 185 + TIME '09:00', q.bodega_id, bod, 'Registro inicial del equipo', 'demo', hoy - 185 + TIME '09:00'
    FROM equipo q WHERE q.empresa_id = e;

    -- Hasta qué día tiene historial cada equipo: después viene su estado final (arriendo vigente,
    -- venta, traslado, mantención) o, si no tiene, llega hasta anteayer.
    corte := array_fill(hoy - 2, ARRAY[32]);
    corte[1] := hoy - 15; corte[8] := hoy - 15;       -- arriendo vigente a Topografía Atacama
    corte[16] := hoy - 8;  corte[23] := hoy - 6;       -- drones y escáner arrendados esta semana
    corte[10] := hoy - 12; corte[19] := hoy - 5; corte[17] := hoy - 13;
    corte[2] := hoy - 23;  corte[13] := hoy - 19; corte[26] := hoy - 28;   -- vencidos
    corte[3] := hoy - 13;  corte[29] := hoy - 45; corte[32] := hoy - 80;   -- vendidos
    corte[30] := hoy - 15; corte[31] := hoy - 6;                          -- mantención y tránsito
    corte[6] := hoy - 25;  corte[12] := hoy - 25;                         -- trasladados

    -- Historial de arriendos ya cerrados, sin traslapes por equipo. El uso varía por categoría:
    -- las estaciones totales y los GPS se arriendan seguido; las cámaras y el georradar, poco.
    FOR i IN 1..32 LOOP
        eq := eqs[i];
        uso := CASE WHEN i IN (24, 26, 27) THEN 28 WHEN i IN (19, 20, 21, 22, 30) THEN 16 ELSE 7 END;
        ini := hoy - 175 + (i * 3) % 17;
        LOOP
            dur := 4 + floor(random() * 12)::INT;
            EXIT WHEN ini + dur + 1 > corte[i];
            ret := ini + dur + CASE WHEN random() < 0.15 THEN 1 + floor(random() * 3)::INT ELSE 0 END;
            EXIT WHEN ret >= corte[i];
            b_ret := CASE WHEN random() < 0.1 THEN (ARRAY[central, norte, sur])[1 + floor(random() * 3)::INT] ELSE NULL END;
            PERFORM pg_temp.arrendar(e, CASE WHEN random() < 0.7 THEN ven ELSE bod END,
                                     cli[1 + floor(random() * 8)::INT], ARRAY[eq], ini, ini + dur, ret, b_ret,
                                     CASE WHEN random() < 0.6 THEN 'OC-2026-' || (200 + floor(random() * 700))::INT END);
            ini := ret + 1 + floor(random() * uso)::INT;
        END LOOP;
    END LOOP;

    -- Arriendos vigentes, uno de ellos con dos equipos.
    op_atacama := pg_temp.arrendar(e, ven, cli[3], ARRAY[eqs[1], eqs[8]], hoy - 12, hoy + 4, NULL, NULL, 'OC-2026-131');
    op_m300 := pg_temp.arrendar(e, ven, cli[2], ARRAY[eqs[16]], hoy - 5, hoy + 9, NULL, NULL, 'OC-2026-152');
    op_blk := pg_temp.arrendar(e, ven, cli[1], ARRAY[eqs[23]], hoy - 3, hoy + 11, NULL, NULL, 'OC-2026-158');
    op_sdi := pg_temp.arrendar(e, bod, cli[4], ARRAY[eqs[10]], hoy - 9, hoy + 5, NULL, NULL, NULL);
    op_nivel := pg_temp.arrendar(e, ven, cli[6], ARRAY[eqs[19]], hoy - 2, hoy + 12, NULL, NULL, NULL);
    op_dron_norte := pg_temp.arrendar(e, ven, cli[5], ARRAY[eqs[17]], hoy - 10, hoy + 2, NULL, NULL, 'OC-2026-147');
    -- Arriendos vencidos: aparecen como alertas en el panel (CF-10).
    op_vencido := pg_temp.arrendar(e, ven, cli[2], ARRAY[eqs[2]], hoy - 20, hoy - 6, NULL, NULL, 'OC-2026-139');
    PERFORM pg_temp.arrendar(e, bod, cli[7], ARRAY[eqs[13]], hoy - 16, hoy - 2, NULL, NULL, NULL);
    PERFORM pg_temp.arrendar(e, ven, cli[4], ARRAY[eqs[26]], hoy - 25, hoy - 11, NULL, NULL, 'OC-2026-127');

    -- Ventas (CF-09): el equipo queda vendido y fuera de la disponibilidad.
    PERFORM pg_temp.vender(e, ven, cli[5], eqs[32], hoy - 75, 9200000, 'OC-2026-088');
    op_venta_sv1 := pg_temp.vender(e, ven, cli[7], eqs[29], hoy - 40, 6400000, NULL);
    op_venta_ts13 := pg_temp.vender(e, ven, cli[1], eqs[3], hoy - 8, 11900000, 'OC-2026-161');

    -- Traslados: dos completos y uno en tránsito (CF-08).
    PERFORM pg_temp.mover(e, eqs[6], 'TRASLADO_SALIDA', (hoy - 20) + TIME '09:00', norte, sur, NULL, bod, 'Refuerzo de la zona sur');
    UPDATE equipo SET estado = 'EN_TRANSITO' WHERE id = eqs[6];
    PERFORM pg_temp.mover(e, eqs[6], 'TRASLADO_LLEGADA', (hoy - 18) + TIME '15:00', norte, sur, NULL, bod, NULL);
    UPDATE equipo SET estado = 'DISPONIBLE', bodega_id = sur WHERE id = eqs[6];
    PERFORM pg_temp.mover(e, eqs[12], 'TRASLADO_SALIDA', (hoy - 22) + TIME '09:00', sur, central, NULL, bod, NULL);
    PERFORM pg_temp.mover(e, eqs[12], 'TRASLADO_LLEGADA', (hoy - 21) + TIME '12:00', sur, central, NULL, bod, NULL);
    UPDATE equipo SET bodega_id = central WHERE id = eqs[12];
    PERFORM pg_temp.mover(e, eqs[31], 'TRASLADO_SALIDA', (hoy - 1) + TIME '10:30', central, norte, NULL, bod, 'Para arriendos de la zona norte');
    UPDATE equipo SET estado = 'EN_TRANSITO' WHERE id = eqs[31];

    -- Un nivel en mantención.
    PERFORM pg_temp.mover(e, eqs[30], 'ENTRADA_MANTENCION', (hoy - 10) + TIME '11:00', norte, NULL, NULL, bod, 'Calibración anual');
    UPDATE equipo SET estado = 'EN_MANTENCION' WHERE id = eqs[30];

    -- Técnicos (HU-30) y capacitaciones (HU-20 a HU-22).
    INSERT INTO tecnico (empresa_id, nombre, apellido, email, telefono, activo, creado_por) VALUES
        (e, 'Andrés', 'Pérez', 'aperez@ejemplo.cl', '+56 9 8765 4321', TRUE, 'demo'),
        (e, 'Camila', 'Rojas', 'crojas@ejemplo.cl', NULL, TRUE, 'demo'),
        (e, 'Diego', 'Fuentes', NULL, '+56 9 1234 5678', FALSE, 'demo');
    SELECT id INTO andres FROM tecnico WHERE empresa_id = e AND apellido = 'Pérez';
    SELECT id INTO camila FROM tecnico WHERE empresa_id = e AND apellido = 'Rojas';
    SELECT id INTO diego FROM tecnico WHERE empresa_id = e AND apellido = 'Fuentes';
    INSERT INTO capacitacion (empresa_id, operacion_id, tecnico_id, fecha_programada, fecha_realizada, lugar, estado,
                              observaciones, creado_por) VALUES
        (e, op_atacama, andres, (hoy - 11) + TIME '09:30', (hoy - 11) + TIME '09:45', 'Faena del cliente, Copiapó', 'REALIZADA', 'Replanteo con TS16 y GS18.', 'demo'),
        (e, op_venta_sv1, camila, (hoy - 38) + TIME '10:00', (hoy - 38) + TIME '10:00', 'Fundo del cliente, Talca', 'REALIZADA', NULL, 'demo'),
        (e, op_m300, camila, (hoy + 1) + TIME '10:00', NULL, 'Faena Minera Pampa Norte, Calama', 'PROGRAMADA', 'Vuelo fotogramétrico y planificación de misiones.', 'demo'),
        (e, op_blk, andres, (hoy + 3) + TIME '09:30', NULL, 'Obra Constructora Los Andes, Las Condes', 'PROGRAMADA', NULL, 'demo'),
        (e, op_venta_ts13, andres, (hoy + 3) + TIME '15:00', NULL, 'Oficina del cliente, Las Condes', 'PROGRAMADA', NULL, 'demo'),
        (e, op_sdi, diego, (hoy - 7) + TIME '11:00', NULL, 'Bodega Sur, sala de capacitación', 'CANCELADA', 'El cliente pidió cambiar de técnico.', 'demo'),
        (e, op_sdi, camila, (hoy + 6) + TIME '11:00', NULL, 'Obra del cliente, Concepción', 'PROGRAMADA', NULL, 'demo');
    -- op_dron_norte queda sin capacitación: su ficha sugiere agendarla (HU-20).
END $$;

-- Documentos adjuntos (HU-23 a HU-25). Los PDF están en docs/demo/archivos y cargar-demo.sh los
-- copia a la carpeta de adjuntos de la aplicación.
INSERT INTO documento (empresa_id, operacion_id, tipo, numero_folio, fecha_emision, nombre_archivo, ruta_archivo,
                       content_type, tamano_bytes, creado_por)
SELECT o.empresa_id, o.id, d.tipo, d.folio, o.fecha + d.desfase, d.nombre, d.ruta, 'application/pdf', d.bytes, 'demo'
FROM (VALUES
    ('OC-2026-131', 'ORDEN_COMPRA',  'OC-2026-131', -1, 'OC-2026-131.pdf',        '1/demo-oc-2026-131.pdf',  :bytes_oc131),
    ('OC-2026-131', 'GUIA_DESPACHO', '1045',         0, 'guia-despacho-1045.pdf', '1/demo-guia-1045.pdf',    :bytes_guia1045),
    ('OC-2026-161', 'ORDEN_COMPRA',  'OC-2026-161', -2, 'OC-2026-161.pdf',        '1/demo-oc-2026-161.pdf',  :bytes_oc161),
    ('OC-2026-161', 'FACTURA',       '88231',        0, 'factura-88231.pdf',      '1/demo-factura-88231.pdf', :bytes_fac88231),
    ('OC-2026-127', 'GUIA_DESPACHO', '1012',         0, 'guia-despacho-1012.pdf', '1/demo-guia-1012.pdf',    :bytes_guia1012)
) AS d (oc, tipo, folio, desfase, nombre, ruta, bytes)
JOIN (SELECT op.id, op.empresa_id, op.fecha, COALESCE(a.numero_orden_compra, v.numero_orden_compra) AS oc
      FROM operacion op LEFT JOIN arriendo a ON a.id = op.id LEFT JOIN venta v ON v.id = op.id) o ON o.oc = d.oc;
