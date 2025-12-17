package com.metamapa.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para estadísticas por hora del día
 */
@Entity
@Table(name = "stats_hora")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsHora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "categoria")
    private String categoria;
    
    @Column(name = "hora")
    private Integer hora;
    
    @Column(name = "cantidad_hechos")
    private Long cantidadHechos;
    
    @Column(name = "fecha_calculo")
    private LocalDateTime fechaCalculo;
    
    @Column(name = "porcentaje")
    private Double porcentaje;
    
    public StatsHora(String categoria, Integer hora, Long cantidadHechos, LocalDateTime fechaCalculo) {
        this.categoria = categoria;
        this.hora = hora;
        this.cantidadHechos = cantidadHechos;
        this.fechaCalculo = fechaCalculo;
    }
}
