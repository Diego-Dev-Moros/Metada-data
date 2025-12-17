package com.metamapa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para estadísticas por categoría
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CategoriaStatsDTO {
    
    private String categoria;
    private Long cantidadHechos;
    private Double porcentaje;
    private LocalDateTime fechaCalculo;
}
