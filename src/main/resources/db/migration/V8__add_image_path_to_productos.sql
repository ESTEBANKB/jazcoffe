-- Agrega columna image_path para almacenar el nombre/ruta de la imagen del producto
ALTER TABLE productos
ADD COLUMN image_path VARCHAR(255);
