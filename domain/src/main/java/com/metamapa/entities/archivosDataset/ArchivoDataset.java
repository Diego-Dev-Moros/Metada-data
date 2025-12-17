package com.metamapa.entities.archivosDataset;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "archivo_dataset")
@Data
@NoArgsConstructor
public class ArchivoDataset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombreArchivo;
    
    @Column(unique = true, nullable = false, length = 64)
    private String hash;
    
    @Column(nullable = false, length = 500)
    private String rutaArchivo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoArchivo estado;
    
    @Column(nullable = false)
    private LocalDateTime fechaCarga;
    
    // Estadísticas del procesamiento
    private Integer filasProcesadas = 0;
    private Integer hechoInsertados = 0;
    private Integer filasSalteadas = 0;
    
    @Column(length = 5000)
    private String errores;
    
    public ArchivoDataset(String nombreArchivo, String hash, String rutaArchivo, EstadoArchivo estado) {
        this.nombreArchivo = nombreArchivo;
        this.hash = hash;
        this.rutaArchivo = rutaArchivo;
        this.estado = estado;
        this.fechaCarga = LocalDateTime.now();
    }
}
