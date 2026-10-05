ALTER TABLE productos
    ADD COLUMN imagen_url VARCHAR(500);

DO $$
DECLARE
cliente_id BIGINT;
    mouse_id BIGINT;
    teclado_id BIGINT;
    audifonos_id BIGINT;
    primera_compra_id BIGINT;
    segunda_compra_id BIGINT;
BEGIN
SELECT id
INTO cliente_id
FROM usuarios
WHERE email = 'cliente@softdv.com';

IF cliente_id IS NULL THEN
        RAISE EXCEPTION
            'Primero registra al cliente cliente@softdv.com';
END IF;

INSERT INTO productos (
    nombre,
    descripcion,
    activo,
    imagen_url
)
VALUES (
           'Mouse Logitech M330',
           'Mouse inalámbrico con clics silenciosos y diseño ergonómico.',
           TRUE,
           '/images/productos/mouse-logitech-m330.jpg'
       )
    RETURNING id INTO mouse_id;

INSERT INTO productos (
    nombre,
    descripcion,
    activo,
    imagen_url
)
VALUES (
           'Teclado Redragon K552',
           'Teclado mecánico compacto con iluminación y conexión USB.',
           TRUE,
           '/images/productos/teclado-redragon-k552.jpg'
       )
    RETURNING id INTO teclado_id;

INSERT INTO productos (
    nombre,
    descripcion,
    activo,
    imagen_url
)
VALUES (
           'Audífonos de prueba',
           'Audífonos con micrófono integrado y conexión por cable.',
           TRUE,
           '/images/productos/audifonos.jpg'
       )
    RETURNING id INTO audifonos_id;

INSERT INTO compras (
    fecha,
    total,
    estado,
    usuario_id
)
VALUES (
           TIMESTAMP '2026-09-20 10:30:00',
           310.00,
           'ENTREGADA',
           cliente_id
       )
    RETURNING id INTO primera_compra_id;

INSERT INTO detalles_compra (
    compra_id,
    producto_id,
    cantidad,
    precio_unitario
)
VALUES
    (primera_compra_id, mouse_id, 2, 80.00),
    (primera_compra_id, teclado_id, 1, 150.00);

INSERT INTO compras (
    fecha,
    total,
    estado,
    usuario_id
)
VALUES (
           TIMESTAMP '2026-09-25 15:00:00',
           120.00,
           'ENTREGADA',
           cliente_id
       )
    RETURNING id INTO segunda_compra_id;

INSERT INTO detalles_compra (
    compra_id,
    producto_id,
    cantidad,
    precio_unitario
)
VALUES (
           segunda_compra_id,
           audifonos_id,
           1,
           120.00
       );
END $$;