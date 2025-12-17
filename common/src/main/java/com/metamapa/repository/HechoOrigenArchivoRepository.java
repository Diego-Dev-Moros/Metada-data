package com.metamapa.repository;

import com.metamapa.entities.HechoOrigenArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HechoOrigenArchivoRepository extends JpaRepository<HechoOrigenArchivo, Long> {
    
    /**
     * Encuentra todas las relaciones de un hecho específico.
     * Permite consultar: "¿De qué archivos proviene este hecho?"
     */
    List<HechoOrigenArchivo> findByHechoId(Long hechoId);
    
    /**
     * Encuentra todas las relaciones de un archivo específico.
     * Permite consultar: "¿Qué hechos se crearon a partir de este archivo?"
     */
    List<HechoOrigenArchivo> findByArchivoId(Long archivoId);
    
    /**
     * Verifica si ya existe una relación entre un hecho y un archivo.
     * Evita duplicados en la tabla intermedia.
     */
    boolean existsByHechoIdAndArchivoId(Long hechoId, Long archivoId);
}
