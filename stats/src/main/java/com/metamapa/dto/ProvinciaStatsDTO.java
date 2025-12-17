package com.metamapa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO para estadísticas por provincia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinciaStatsDTO {
    
    private String provincia;
    private Long cantidadHechos;
    private Double porcentaje;
    private LocalDateTime fechaCalculo;
    private Long coleccionId;
    private String categoria; // Para filtros por categoría
    
    public ProvinciaStatsDTO(String provincia, Long cantidadHechos, Double porcentaje, LocalDateTime fechaCalculo) {
        this.provincia = provincia;
        this.cantidadHechos = cantidadHechos;
        this.porcentaje = porcentaje;
        this.fechaCalculo = fechaCalculo;
    }
}
