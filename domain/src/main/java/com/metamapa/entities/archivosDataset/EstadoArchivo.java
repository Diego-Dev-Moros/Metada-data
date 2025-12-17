package com.metamapa.entities.archivosDataset;

public enum EstadoArchivo {
    PENDIENTE,   // Archivo subido pero no procesado
    PROCESADO,   // Procesado exitosamente
    FALLIDO      // Error durante el procesamiento
}
