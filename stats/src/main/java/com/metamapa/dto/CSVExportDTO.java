package com.metamapa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para exportación CSV
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CSVExportDTO {
    
    private String tipoExportacion;
    private String nombreArchivo;
    private String rutaArchivo;
    private LocalDateTime fechaExportacion;
    private Long totalRegistros;
    private List<String> columnas;
    private String estado; // "COMPLETADO", "EN_PROCESO", "ERROR"
    private String mensaje;
    
    public CSVExportDTO(String tipoExportacion, String nombreArchivo, String rutaArchivo, LocalDateTime fechaExportacion) {
        this.tipoExportacion = tipoExportacion;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
        this.fechaExportacion = fechaExportacion;
        this.estado = "COMPLETADO";
    }
}
