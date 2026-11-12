-- Un año de operación para medir PNF-02 (HU-26). Solo para la base mira_carga.
DO $$
DECLARE
    e BIGINT; u BIGINT; cats BIGINT[]; bods BIGINT[]; clis BIGINT[]; eqs BIGINT[];
    oid BIGINT; ini DATE; fin DATE; eq BIGINT; eq2 BIGINT; hoy DATE := DATE '2026-11-09'; vigente BOOLEAN;
BEGIN
    PERFORM setseed(0.42);
    SELECT id INTO e FROM empresa WHERE rut = '11.111.111-1';
    SELECT id INTO u FROM usuario WHERE email = 'admin@mira.cl';
    SELECT array_agg(id ORDER BY id) INTO cats FROM categoria WHERE empresa_id = e AND activa
        AND nombre IN ('Estación total', 'GPS RTK', 'Dron', 'Nivel', 'Escáneres láser');
    INSERT INTO bodega (empresa_id, nombre, comuna) SELECT e, 'Bodega ' || n, 'Comuna ' || n FROM generate_series(1, 5) n;
    SELECT array_agg(id ORDER BY id) INTO bods FROM bodega WHERE empresa_id = e;
    INSERT INTO cliente (empresa_id, rut, razon_social)
        SELECT e, '7' || lpad(n::text, 7, '0') || '-0', 'Cliente de carga ' || n FROM generate_series(1, 80) n;
    SELECT array_agg(id ORDER BY id) INTO clis FROM cliente WHERE empresa_id = e;
    INSERT INTO equipo (empresa_id, categoria_id, bodega_id, numero_serie, marca, modelo, estado, tarifa_diaria, activo)
        SELECT e, cats[1 + n % array_length(cats, 1)], bods[1 + n % 5], 'CARGA-' || lpad(n::text, 4, '0'),
               'Leica', 'Modelo ' || (n % 20), 'DISPONIBLE', 50000, TRUE
        FROM generate_series(1, 500) n;
    SELECT array_agg(id ORDER BY id) INTO eqs FROM equipo WHERE empresa_id = e;
    INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_destino_id, usuario_id)
        SELECT e, q.id, 'INGRESO', TIMESTAMP '2025-11-09 09:00', q.bodega_id, u FROM equipo q WHERE q.empresa_id = e;

    -- 1.200 arriendos en el año; los 60 más recientes siguen vigentes (algunos ya vencidos).
    FOR k IN 1..1200 LOOP
        ini := DATE '2025-11-09' + (k * 365 / 1200);
        fin := ini + 3 + (k % 18);
        vigente := k > 1140;
        eq := eqs[1 + (k * 7) % 435];
        INSERT INTO operacion (empresa_id, tipo, cliente_id, usuario_id, fecha) VALUES (e, 'ARRIENDO', clis[1 + k % 80], u, ini) RETURNING id INTO oid;
        INSERT INTO arriendo (id, fecha_inicio, fecha_retorno_comprometida) VALUES (oid, ini, fin);
        INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario, fecha_retorno_real)
            VALUES (oid, eq, 50000, CASE WHEN vigente THEN NULL ELSE LEAST(fin, hoy) END);
        INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_origen_id, operacion_id, usuario_id)
            SELECT e, eq, 'SALIDA_ARRIENDO', ini + TIME '10:00', bodega_id, oid, u FROM equipo WHERE id = eq;
        IF vigente THEN
            UPDATE equipo SET estado = 'ARRENDADO' WHERE id = eq;
        ELSE
            INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_destino_id, operacion_id, usuario_id)
                SELECT e, eq, 'RETORNO_ARRIENDO', LEAST(fin, hoy) + TIME '17:00', bodega_id, oid, u FROM equipo WHERE id = eq;
        END IF;
        IF k % 3 = 0 AND NOT vigente THEN
            eq2 := eqs[1 + (k * 11 + 5) % 435];
            INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario, fecha_retorno_real) VALUES (oid, eq2, 70000, LEAST(fin, hoy));
            INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_origen_id, operacion_id, usuario_id)
                SELECT e, eq2, 'SALIDA_ARRIENDO', ini + TIME '10:00', bodega_id, oid, u FROM equipo WHERE id = eq2;
            INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_destino_id, operacion_id, usuario_id)
                SELECT e, eq2, 'RETORNO_ARRIENDO', LEAST(fin, hoy) + TIME '17:00', bodega_id, oid, u FROM equipo WHERE id = eq2;
        END IF;
    END LOOP;

    -- 50 ventas con los últimos equipos y 15 equipos en mantención.
    FOR k IN 1..50 LOOP
        eq := eqs[450 + k];
        INSERT INTO operacion (empresa_id, tipo, cliente_id, usuario_id, fecha) VALUES (e, 'VENTA', clis[1 + k % 80], u, DATE '2025-11-15' + k * 7) RETURNING id INTO oid;
        INSERT INTO venta (id) VALUES (oid);
        INSERT INTO operacion_detalle (operacion_id, equipo_id, valor_unitario) VALUES (oid, eq, 9000000);
        INSERT INTO movimiento (empresa_id, equipo_id, tipo, fecha, bodega_origen_id, operacion_id, usuario_id)
            SELECT e, eq, 'SALIDA_VENTA', DATE '2025-11-15' + k * 7 + TIME '12:00', bodega_id, oid, u FROM equipo WHERE id = eq;
        UPDATE equipo SET estado = 'VENDIDO' WHERE id = eq;
    END LOOP;
    UPDATE equipo SET estado = 'EN_MANTENCION' WHERE id = ANY (eqs[436:450]) AND estado = 'DISPONIBLE';
END $$;
