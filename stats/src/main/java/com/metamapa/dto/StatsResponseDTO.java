package com.metamapa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta general para estadísticas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponseDTO {
    
    private String tipoEstadistica;
    private Object resultado;
    private LocalDateTime fechaCalculo;
    private String version;
    private Long totalRegistros;
    private List<String> fuentes;
    private String mensaje;
    
    public StatsResponseDTO(String tipoEstadistica, Object resultado, LocalDateTime fechaCalculo) {
        this.tipoEstadistica = tipoEstadistica;
        this.resultado = resultado;
        this.fechaCalculo = fechaCalculo;
        this.version = "1.0";
    }
}
