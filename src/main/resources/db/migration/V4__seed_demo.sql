-- Datos de prueba idempotentes

-- Categorías
INSERT INTO categorias (nombre)
SELECT 'Bebidas'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre='Bebidas');

INSERT INTO categorias (nombre)
SELECT 'Comidas'
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre='Comidas');

-- Productos (usa SELECT para tomar el id de la categoría)
INSERT INTO productos (nombre, categoria_id, precio, costo, stock_actual, stock_min)
SELECT 'Café Americano', c.id, 5000, 2000, 50, 10 FROM categorias c WHERE c.nombre='Bebidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre='Café Americano');

INSERT INTO productos (nombre, categoria_id, precio, costo, stock_actual, stock_min)
SELECT 'Capuchino', c.id, 7000, 3000, 40, 8 FROM categorias c WHERE c.nombre='Bebidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre='Capuchino');

INSERT INTO productos (nombre, categoria_id, precio, costo, stock_actual, stock_min)
SELECT 'Sandwich', c.id, 9000, 5000, 30, 6 FROM categorias c WHERE c.nombre='Comidas'
AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre='Sandwich');

-- Usuario cajero demo (password hash de ejemplo)
INSERT INTO usuarios (nombre, email, hash, rol_id, activo)
SELECT 'Cajero Demo', 'cajero@local', '$2a$10$abcdefghijklmnopqrstuv', r.id, true
FROM roles r WHERE r.nombre='CAJERO'
AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.email='cajero@local');


