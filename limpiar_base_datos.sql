-- ============================================
-- SCRIPT PARA LIMPIAR LA BASE DE DATOS utndds
-- Elimina todas las tablas para empezar de cero
-- ============================================

USE utndds;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS multimedia;
DROP TABLE IF EXISTS hecho_fuentes;
DROP TABLE IF EXISTS hecho_etiquetas;
DROP TABLE IF EXISTS Hecho;
DROP TABLE IF EXISTS Ubicacion;
DROP TABLE IF EXISTS Lugar;
DROP TABLE IF EXISTS archivo_dataset;
DROP TABLE IF EXISTS Contribuyente;
DROP TABLE IF EXISTS coleccion_fuente;
DROP TABLE IF EXISTS Coleccion;
DROP TABLE IF EXISTS coleccion_criterio;
DROP TABLE IF EXISTS coleccion_hecho;
DROP TABLE IF EXISTS hecho_por_coleccion;
DROP TABLE IF EXISTS hibernate_sequence;
DROP TABLE IF EXISTS solicitud_eliminacion;
DROP TABLE IF EXISTS archivo_dataset_errores;
DROP TABLE IF EXISTS hecho_origen_archivo;
DROP TABLE IF EXISTS stats_categoria;
DROP TABLE IF EXISTS stats_hora;
DROP TABLE IF EXISTS stats_metadata;
DROP TABLE IF EXISTS stats_provincia;
DROP TABLE IF EXISTS stats_spam;


SET FOREIGN_KEY_CHECKS = 1;

SHOW TABLES;

SELECT 'Base de datos limpiada exitosamente' as Resultado;
