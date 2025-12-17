package com.metamapa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para estadísticas por hora del día
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class HoraStatsDTO {
    
    private String categoria;
    private Integer hora;
    private Long cantidadHechos;
    private Double porcentaje;
    private LocalDateTime fechaCalculo;
}
