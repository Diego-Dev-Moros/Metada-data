-- Crear índice FULLTEXT automáticamente al iniciar la aplicación
-- Este script se ejecuta después de que Hibernate cree las tablas

-- Verificar si el índice ya existe antes de crearlo
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics 
               WHERE table_schema = DATABASE() 
               AND table_name = 'Hecho' 
               AND index_name = 'idx_fulltext_hecho_busqueda');

SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE Hecho ADD FULLTEXT INDEX idx_fulltext_hecho_busqueda (titulo, descripcion)', 
    'SELECT ''El índice FULLTEXT ya existe'' AS mensaje');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
