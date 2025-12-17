package com.metamapa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para estadísticas por categoría
 */
@Entity
@Table(name = "stats_categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsCategoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "categoria")
    private String categoria;
    
    @Column(name = "cantidad_hechos")
    private Long cantidadHechos;
    
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
    
    @Column(name = "porcentaje")
    private Double porcentaje;
    
    public StatsCategoria(String categoria, Long cantidadHechos, LocalDateTime fechaCalculo) {
        this.categoria = categoria;
        this.cantidadHechos = cantidadHechos;
        this.fechaCalculo = fechaCalculo;
    }
}
