-- ============================================
-- TABLA: estados
-- ============================================
CREATE TABLE estados
(
    id_estado   SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT
);

-- ============================================
-- TABLA: tipo_prestamo
-- ============================================
CREATE TABLE tipo_prestamo
(
    id_tipo_prestamo      SERIAL PRIMARY KEY,
    nombre                VARCHAR(100)   NOT NULL,
    monto_minimo          NUMERIC(15, 2) NOT NULL,
    monto_maximo          NUMERIC(15, 2) NOT NULL,
    tasa_interes          NUMERIC(5, 2)  NOT NULL, -- Ejemplo: 12.50 %
    validacion_automatica BOOLEAN        NOT NULL DEFAULT FALSE
);

-- ============================================
-- TABLA: solicitud
-- ============================================
CREATE TABLE solicitud
(
    id_solicitud     SERIAL PRIMARY KEY,
    monto            NUMERIC(15, 2) NOT NULL,
    plazo            INT            NOT NULL, -- meses o días, según definición de negocio
    email            VARCHAR(150)   NOT NULL,
    documento_identidad VARCHAR(50)  NOT NULL,
    id_estado        INT            NOT NULL,
    id_tipo_prestamo INT            NOT NULL,

    CONSTRAINT fk_solicitud_estado
        FOREIGN KEY (id_estado)
            REFERENCES estados (id_estado),

    CONSTRAINT fk_solicitud_tipo_prestamo
        FOREIGN KEY (id_tipo_prestamo)
            REFERENCES tipo_prestamo (id_tipo_prestamo)
);

ALTER TABLE solicitud
ADD COLUMN documeto_identidad VARCHAR(50);

INSERT INTO estados (nombre, descripcion) VALUES
('Pendiente de revisión' , 'La solicitud está pendiente de revisión.'),
('Aprobado', 'La solicitud ha sido aprobada.'),
('Revision manual', 'La solicitud requiere revisión manual.'),
('Rechazado', 'La solicitud ha sido rechazada.');

INSERT INTO tipo_prestamo (nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica) VALUES
('Personal', 1000.00, 50000.00, 15.00, TRUE),
('Hipotecario', 50000.00, 500000.00, 7.50, FALSE),
('Vehícular', 5000.00, 100000.00, 10.00, TRUE);
