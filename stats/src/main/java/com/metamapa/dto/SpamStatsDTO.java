package com.metamapa.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO para estadísticas de spam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpamStatsDTO {
    
    private Long totalSolicitudes;
    private Long solicitudesSpam;
    private Long solicitudesNoSpam;
    private Double porcentajeSpam;
    private Double porcentajeNoSpam;
    private LocalDateTime fechaCalculo;
    
    public SpamStatsDTO(Long totalSolicitudes, Long solicitudesSpam, Double porcentajeSpam, LocalDateTime fechaCalculo) {
        this.totalSolicitudes = totalSolicitudes;
        this.solicitudesSpam = solicitudesSpam;
        this.solicitudesNoSpam = totalSolicitudes - solicitudesSpam;
        this.porcentajeSpam = porcentajeSpam;
        this.porcentajeNoSpam = 100.0 - porcentajeSpam;
        this.fechaCalculo = fechaCalculo;
    }
}
