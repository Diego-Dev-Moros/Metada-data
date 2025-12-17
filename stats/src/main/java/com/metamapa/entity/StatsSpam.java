package com.metamapa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para estadísticas de spam
 */
@Entity
@Table(name = "stats_spam")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsSpam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "total_solicitudes")
    private Long totalSolicitudes;
    
    @Column(name = "solicitudes_spam")
    private Long solicitudesSpam;
    
    @Column(name = "porcentaje_spam")
    private Double porcentajeSpam;
    
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
    
    public StatsSpam(Long totalSolicitudes, Long solicitudesSpam, Double porcentajeSpam, LocalDateTime fechaCalculo) {
        this.totalSolicitudes = totalSolicitudes;
        this.solicitudesSpam = solicitudesSpam;
        this.porcentajeSpam = porcentajeSpam;
        this.fechaCalculo = fechaCalculo;
    }
}
