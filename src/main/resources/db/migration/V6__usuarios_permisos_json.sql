-- Agregar campo para permisos personalizados en formato JSON
ALTER TABLE usuarios ADD COLUMN permisos_json TEXT;

-- Comentario explicativo
COMMENT ON COLUMN usuarios.permisos_json IS 'Array JSON de permisos personalizados del usuario. Ejemplo: ["POS_ACCESS", "INVENTARIO_VIEW"]';
