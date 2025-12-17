package com.metamapa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para estadísticas por provincia
 */
@Entity
@Table(name = "stats_provincia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsProvincia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "coleccion_id")
    private Long coleccionId;
    
    @Column(name = "provincia")
    private String provincia;
    
    @Column(name = "cantidad_hechos")
    private Long cantidadHechos;
    
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
    
    @Column(name = "porcentaje")
    private Double porcentaje;
    
    public StatsProvincia(Long coleccionId, String provincia, Long cantidadHechos, LocalDateTime fechaCalculo) {
        this.coleccionId = coleccionId;
        this.provincia = provincia;
        this.cantidadHechos = cantidadHechos;
        this.fechaCalculo = fechaCalculo;
    }
}
