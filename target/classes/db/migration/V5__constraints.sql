-- Reglas de integridad y performance (compatibles con H2 y Postgres)

-- Productos: precios y costos no negativos; stock no negativo
ALTER TABLE productos ADD CONSTRAINT chk_producto_precio CHECK (precio >= 0);
ALTER TABLE productos ADD CONSTRAINT chk_producto_costo CHECK (costo >= 0);
ALTER TABLE productos ADD CONSTRAINT chk_producto_stock CHECK (stock_actual >= 0 AND stock_min >= 0);

-- Movimiento inventario: cantidad positiva y tipo válido
ALTER TABLE movimientos_inventario ADD CONSTRAINT chk_mov_cantidad CHECK (cantidad > 0);
ALTER TABLE movimientos_inventario ADD CONSTRAINT chk_mov_tipo CHECK (LOWER(tipo) IN ('entrada','salida'));

-- Ventas: total no negativo
ALTER TABLE ventas ADD CONSTRAINT chk_venta_total CHECK (total >= 0);

-- Detalle de venta: cantidad y precio positivos (dos posibles nombres de tabla)
ALTER TABLE IF EXISTS ventas_detalle ADD CONSTRAINT chk_det_cantidad CHECK (cantidad > 0);
ALTER TABLE IF EXISTS ventas_detalle ADD CONSTRAINT chk_det_precio CHECK (precio_unitario >= 0);
ALTER TABLE IF EXISTS venta_detalles ADD CONSTRAINT chk_det_cantidad2 CHECK (cantidad > 0);
ALTER TABLE IF EXISTS venta_detalles ADD CONSTRAINT chk_det_precio2 CHECK (precio_unitario >= 0);

-- Índices útiles
CREATE INDEX IF NOT EXISTS idx_prod_categoria ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_det_venta ON ventas_detalle(venta_id);
CREATE INDEX IF NOT EXISTS idx_mov_producto ON movimientos_inventario(producto_id);
