package com.metamapa.entities.colecciones;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "coleccion_fuente")
public class ColeccionFuente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_coleccion", referencedColumnName = "identificador")
    private Coleccion coleccion;
    
    @Column(name = "identificador_fuente", nullable = false)
    private String identificadorFuente; // "fuente-dinamica", "fuente-estatica", etc.
    
    @Column(name = "tipo_fuente", nullable = false)
    private String tipoFuente; // "DINAMICA", "ESTATICA", "PROXY"
    
    @Column(name = "activa")
    private Boolean activa = true;
    
    public ColeccionFuente(Coleccion coleccion, String identificadorFuente, String tipoFuente) {
        this.coleccion = coleccion;
        this.identificadorFuente = identificadorFuente;
        this.tipoFuente = tipoFuente;
    }
}