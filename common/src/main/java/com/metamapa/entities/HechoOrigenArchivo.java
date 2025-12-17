package com.metamapa.entities;

import com.metamapa.entities.hechos.Hecho;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Tabla intermedia para la relación Many-to-Many entre Hechos y Archivos de origen.
 * Un hecho puede provenir de múltiples archivos CSV (si se sube varias veces).
 * Un archivo CSV contiene múltiples hechos.
 */
@Entity
@Table(name = "hecho_origen_archivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HechoOrigenArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referencia al hecho en la base de datos central (agregador).
     */
    @ManyToOne
    @JoinColumn(name = "hecho_id", nullable = false)
    private Hecho hecho;

    /**
     * ID del archivo de origen en la fuente estática.
     * No usamos @ManyToOne porque ArchivoDataset está en otra base de datos (fuente-estática).
     * Solo guardamos el ID para trazabilidad.
     */
    @Column(name = "archivo_id", nullable = false)
    private Long archivoId;

    /**
     * Fecha en la que se vinculó el hecho con este archivo.
     */
    @Column(name = "fecha_vinculacion")
    private LocalDateTime fechaVinculacion;

    public HechoOrigenArchivo(Hecho hecho, Long archivoId) {
        this.hecho = hecho;
        this.archivoId = archivoId;
        this.fechaVinculacion = LocalDateTime.now();
    }
}
