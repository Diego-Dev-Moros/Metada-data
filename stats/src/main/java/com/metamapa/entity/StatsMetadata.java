package com.metamapa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para metadatos de estadísticas
 */
@Entity
@Table(name = "stats_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsMetadata {
    
    @Id
    private Long id = 1L; // Solo un registro
    
    @Column(name = "ultima_actualizacion_stats")
    private LocalDateTime ultimaActualizacionStats;
    
    @Column(name = "total_hechos_procesados")
    private Long totalHechosProcesados;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    public StatsMetadata(LocalDateTime ultimaActualizacionStats, Long totalHechosProcesados, String version) {
        this.ultimaActualizacionStats = ultimaActualizacionStats;
        this.totalHechosProcesados = totalHechosProcesados;
        this.version = version;
        this.fechaCreacion = LocalDateTime.now();
    }
}
