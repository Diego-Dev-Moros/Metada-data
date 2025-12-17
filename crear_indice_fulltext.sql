-- ============================================
-- SCRIPT PARA CREAR ÍNDICE FULLTEXT EN TABLA HECHO
-- Este índice es necesario para la búsqueda por texto libre
-- en el endpoint GET /api/interna/hechos/search
-- ============================================

USE utndds;

-- Crear índice FULLTEXT en los campos titulo y descripcion
ALTER TABLE Hecho 
ADD FULLTEXT INDEX idx_fulltext_hecho_busqueda (titulo, descripcion);

-- Verificar que el índice se creó correctamente
SHOW INDEX FROM Hecho WHERE Key_name = 'idx_fulltext_hecho_busqueda';

SELECT 'Índice FULLTEXT creado exitosamente' as Resultado;

-- ============================================
-- NOTAS DE USO:
-- El índice soporta búsqueda en modo BOOLEAN con operadores:
--   - "frase exacta"
--   - +palabra (debe contener)
--   - -palabra (debe excluir)
--   - palabra1 OR palabra2 (cualquiera)
--
-- Ejemplos de búsqueda:
--   - "corte de luz"
--   - sismo OR temblor
--   - +basura -recoleccion
-- ============================================
