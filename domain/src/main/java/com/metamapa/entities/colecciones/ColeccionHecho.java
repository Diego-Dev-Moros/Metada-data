package com.metamapa.entities.colecciones;

import com.metamapa.entities.hechos.Hecho;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "coleccion_hecho")
public class ColeccionHecho {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_coleccion", referencedColumnName = "identificador")
    private Coleccion coleccion;
    
    @ManyToOne
    @JoinColumn(name = "id_hecho", referencedColumnName = "id")
    private Hecho hecho;
    
    @Column(name = "consensuado")
    private Boolean consensuado = false;
    
    public ColeccionHecho(Coleccion coleccion, Hecho hecho, Boolean consensuado) {
        this.coleccion = coleccion;
        this.hecho = hecho;
        this.consensuado = consensuado;
    }
}