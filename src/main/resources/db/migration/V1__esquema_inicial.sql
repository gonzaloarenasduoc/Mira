-- V1: esquema inicial de Mira, según docs/03-modelo-datos.md.
-- Esta migración no se edita nunca. Cualquier ajuste va en una migración nueva.
--
-- Los enumerados se guardan como VARCHAR con una restricción CHECK: el diccionario los define
-- así y JPA los mapea con @Enumerated(EnumType.STRING).
--
-- Todas las tablas llevan los cuatro campos de auditoría. creado_en tiene valor por omisión
-- porque en las tablas hijas de la herencia JOINED (venta, arriendo) JPA audita la tabla padre.

-- ---------------------------------------------------------------------------------------------
-- Empresa y acceso
-- ---------------------------------------------------------------------------------------------

CREATE TABLE empresa (
    id              BIGSERIAL    PRIMARY KEY,
    rut             VARCHAR(12)  NOT NULL,
    razon_social    VARCHAR(200) NOT NULL,
    nombre_fantasia VARCHAR(200),
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por      VARCHAR(150),
    creado_en       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por  VARCHAR(150),
    modificado_en   TIMESTAMP,
    CONSTRAINT uk_empresa_rut UNIQUE (rut)
);

CREATE TABLE usuario (
    id             BIGSERIAL    PRIMARY KEY,
    empresa_id     BIGINT       NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    apellido       VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            VARCHAR(30)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por     VARCHAR(150),
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(150),
    modificado_en  TIMESTAMP,
    -- El correo es único global para que el inicio de sesión no pida elegir empresa.
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMINISTRADOR', 'ENCARGADO_BODEGA', 'VENDEDOR', 'JEFATURA'))
);

-- ---------------------------------------------------------------------------------------------
-- Inventario
-- ---------------------------------------------------------------------------------------------

CREATE TABLE bodega (
    id             BIGSERIAL    PRIMARY KEY,
    empresa_id     BIGINT       NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    direccion      VARCHAR(200),
    comuna         VARCHAR(100),
    activa         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por     VARCHAR(150),
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(150),
    modificado_en  TIMESTAMP,
    CONSTRAINT uk_bodega_empresa_nombre UNIQUE (empresa_id, nombre),
    CONSTRAINT fk_bodega_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

CREATE TABLE categoria (
    id                    BIGSERIAL    PRIMARY KEY,
    empresa_id            BIGINT       NOT NULL,
    nombre                VARCHAR(100) NOT NULL,
    descripcion           VARCHAR(300),
    requiere_capacitacion BOOLEAN      NOT NULL DEFAULT FALSE,
    activa                BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por            VARCHAR(150),
    creado_en             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por        VARCHAR(150),
    modificado_en         TIMESTAMP,
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

CREATE TABLE equipo (
    id                BIGSERIAL     PRIMARY KEY,
    empresa_id        BIGINT        NOT NULL,
    categoria_id      BIGINT        NOT NULL,
    bodega_id         BIGINT        NOT NULL,
    numero_serie      VARCHAR(80)   NOT NULL,
    marca             VARCHAR(80)   NOT NULL,
    modelo            VARCHAR(80)   NOT NULL,
    estado            VARCHAR(20)   NOT NULL,
    fecha_adquisicion DATE,
    valor_adquisicion NUMERIC(12, 2),
    tarifa_diaria     NUMERIC(12, 2),
    observaciones     TEXT,
    activo            BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_por        VARCHAR(150),
    creado_en         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por    VARCHAR(150),
    modificado_en     TIMESTAMP,
    CONSTRAINT uk_equipo_empresa_serie UNIQUE (empresa_id, numero_serie),
    CONSTRAINT fk_equipo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_equipo_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT fk_equipo_bodega FOREIGN KEY (bodega_id) REFERENCES bodega (id),
    CONSTRAINT ck_equipo_estado CHECK (estado IN ('DISPONIBLE', 'ARRENDADO', 'EN_TRANSITO', 'EN_MANTENCION', 'VENDIDO'))
);

-- Consulta de disponibilidad (E004)
CREATE INDEX ix_equipo_empresa_estado ON equipo (empresa_id, estado);
CREATE INDEX ix_equipo_empresa_categoria ON equipo (empresa_id, categoria_id);

CREATE TABLE cliente (
    id                BIGSERIAL    PRIMARY KEY,
    empresa_id        BIGINT       NOT NULL,
    rut               VARCHAR(12)  NOT NULL,
    razon_social      VARCHAR(200) NOT NULL,
    giro              VARCHAR(150),
    direccion         VARCHAR(200),
    comuna            VARCHAR(100),
    contacto_nombre   VARCHAR(150),
    contacto_email    VARCHAR(150),
    contacto_telefono VARCHAR(30),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por        VARCHAR(150),
    creado_en         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por    VARCHAR(150),
    modificado_en     TIMESTAMP,
    CONSTRAINT uk_cliente_empresa_rut UNIQUE (empresa_id, rut),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

-- ---------------------------------------------------------------------------------------------
-- Operaciones: herencia JOINED. venta y arriendo comparten la clave primaria de operacion y
-- no llevan empresa_id: la heredan de la tabla padre (03-modelo-datos.md, sección 1.2).
-- ---------------------------------------------------------------------------------------------

CREATE TABLE operacion (
    id             BIGSERIAL   PRIMARY KEY,
    empresa_id     BIGINT      NOT NULL,
    tipo           VARCHAR(20) NOT NULL,
    cliente_id     BIGINT      NOT NULL,
    usuario_id     BIGINT      NOT NULL,
    fecha          DATE        NOT NULL,
    observaciones  TEXT,
    creado_por     VARCHAR(150),
    creado_en      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(150),
    modificado_en  TIMESTAMP,
    CONSTRAINT fk_operacion_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_operacion_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_operacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_operacion_tipo CHECK (tipo IN ('VENTA', 'ARRIENDO'))
);

CREATE TABLE venta (
    id                  BIGINT      PRIMARY KEY,
    numero_orden_compra VARCHAR(50),
    creado_por          VARCHAR(150),
    creado_en           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por      VARCHAR(150),
    modificado_en       TIMESTAMP,
    CONSTRAINT fk_venta_operacion FOREIGN KEY (id) REFERENCES operacion (id)
);

CREATE TABLE arriendo (
    id                         BIGINT      PRIMARY KEY,
    fecha_inicio               DATE        NOT NULL,
    fecha_retorno_comprometida DATE        NOT NULL,
    numero_orden_compra        VARCHAR(50),
    creado_por                 VARCHAR(150),
    creado_en                  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por             VARCHAR(150),
    modificado_en              TIMESTAMP,
    CONSTRAINT fk_arriendo_operacion FOREIGN KEY (id) REFERENCES operacion (id)
);

-- Cálculo de alertas de retorno (E005, E009)
CREATE INDEX ix_arriendo_fecha_retorno ON arriendo (fecha_retorno_comprometida);

CREATE TABLE operacion_detalle (
    id                 BIGSERIAL      PRIMARY KEY,
    operacion_id       BIGINT         NOT NULL,
    equipo_id          BIGINT         NOT NULL,
    valor_unitario     NUMERIC(12, 2) NOT NULL,
    fecha_retorno_real DATE,
    observacion        VARCHAR(300),
    creado_por         VARCHAR(150),
    creado_en          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por     VARCHAR(150),
    modificado_en      TIMESTAMP,
    CONSTRAINT uk_detalle_operacion_equipo UNIQUE (operacion_id, equipo_id),
    CONSTRAINT fk_detalle_operacion FOREIGN KEY (operacion_id) REFERENCES operacion (id),
    CONSTRAINT fk_detalle_equipo FOREIGN KEY (equipo_id) REFERENCES equipo (id)
);

-- movimiento se crea después de operacion porque la referencia. Es de solo inserción:
-- un error se corrige con el movimiento inverso, nunca con UPDATE ni DELETE.
CREATE TABLE movimiento (
    id                BIGSERIAL    PRIMARY KEY,
    empresa_id        BIGINT       NOT NULL,
    equipo_id         BIGINT       NOT NULL,
    tipo              VARCHAR(30)  NOT NULL,
    fecha             TIMESTAMP    NOT NULL,
    bodega_origen_id  BIGINT,
    bodega_destino_id BIGINT,
    operacion_id      BIGINT,
    usuario_id        BIGINT       NOT NULL,
    observacion       VARCHAR(300),
    creado_por        VARCHAR(150),
    creado_en         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por    VARCHAR(150),
    modificado_en     TIMESTAMP,
    CONSTRAINT fk_movimiento_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_movimiento_equipo FOREIGN KEY (equipo_id) REFERENCES equipo (id),
    CONSTRAINT fk_movimiento_bodega_origen FOREIGN KEY (bodega_origen_id) REFERENCES bodega (id),
    CONSTRAINT fk_movimiento_bodega_destino FOREIGN KEY (bodega_destino_id) REFERENCES bodega (id),
    CONSTRAINT fk_movimiento_operacion FOREIGN KEY (operacion_id) REFERENCES operacion (id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_movimiento_tipo CHECK (tipo IN ('INGRESO', 'TRASLADO_SALIDA', 'TRASLADO_LLEGADA',
        'SALIDA_ARRIENDO', 'RETORNO_ARRIENDO', 'SALIDA_VENTA', 'ENTRADA_MANTENCION', 'SALIDA_MANTENCION'))
);

-- Reconstrucción del historial de un equipo (E002)
CREATE INDEX ix_movimiento_equipo_fecha ON movimiento (equipo_id, fecha);

-- ---------------------------------------------------------------------------------------------
-- Capacitaciones y documentos
-- ---------------------------------------------------------------------------------------------

CREATE TABLE tecnico (
    id             BIGSERIAL    PRIMARY KEY,
    empresa_id     BIGINT       NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    apellido       VARCHAR(100) NOT NULL,
    email          VARCHAR(150),
    telefono       VARCHAR(30),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_por     VARCHAR(150),
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(150),
    modificado_en  TIMESTAMP,
    CONSTRAINT fk_tecnico_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

CREATE TABLE capacitacion (
    id               BIGSERIAL    PRIMARY KEY,
    empresa_id       BIGINT       NOT NULL,
    operacion_id     BIGINT       NOT NULL,
    tecnico_id       BIGINT       NOT NULL,
    fecha_programada TIMESTAMP    NOT NULL,
    fecha_realizada  TIMESTAMP,
    lugar            VARCHAR(200),
    estado           VARCHAR(20)  NOT NULL,
    observaciones    TEXT,
    creado_por       VARCHAR(150),
    creado_en        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por   VARCHAR(150),
    modificado_en    TIMESTAMP,
    CONSTRAINT fk_capacitacion_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_capacitacion_operacion FOREIGN KEY (operacion_id) REFERENCES operacion (id),
    CONSTRAINT fk_capacitacion_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnico (id),
    CONSTRAINT ck_capacitacion_estado CHECK (estado IN ('PROGRAMADA', 'REALIZADA', 'CANCELADA'))
);

-- Detección de choques de agenda del técnico (E007)
CREATE INDEX ix_capacitacion_tecnico_fecha ON capacitacion (tecnico_id, fecha_programada);

-- El archivo vive en el sistema de archivos (APP_UPLOAD_DIR); aquí solo su referencia (D06).
CREATE TABLE documento (
    id             BIGSERIAL    PRIMARY KEY,
    empresa_id     BIGINT       NOT NULL,
    operacion_id   BIGINT       NOT NULL,
    tipo           VARCHAR(20)  NOT NULL,
    numero_folio   VARCHAR(50),
    fecha_emision  DATE,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo   VARCHAR(500) NOT NULL,
    content_type   VARCHAR(100) NOT NULL,
    tamano_bytes   BIGINT       NOT NULL,
    creado_por     VARCHAR(150),
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(150),
    modificado_en  TIMESTAMP,
    CONSTRAINT fk_documento_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_documento_operacion FOREIGN KEY (operacion_id) REFERENCES operacion (id),
    CONSTRAINT ck_documento_tipo CHECK (tipo IN ('ORDEN_COMPRA', 'GUIA_DESPACHO', 'FACTURA'))
);
