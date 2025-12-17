package com.metamapa.repository;

import com.metamapa.entities.colecciones.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColeccionRepository extends JpaRepository<Coleccion, Long> {
    // JpaRepository ya proporciona:
    // - save(Coleccion) para guardar/actualizar
    // - findAll() para obtener todas
    // - findById(Long) para buscar por ID
    // - delete(Coleccion) para eliminar
    
    /**
     * Busca una colección por ID con sus fuentes precargadas (JOIN FETCH)
     * Esto evita LazyInitializationException al acceder a fuentesPersistentes
     */
    @Query("SELECT c FROM Coleccion c LEFT JOIN FETCH c.fuentesPersistentes WHERE c.identificador = :id")
    Optional<Coleccion> findByIdWithFuentes(@Param("id") Long id);
    
    /**
     * Obtiene todas las colecciones con los hechos precargados
     * Nota: Las etiquetas de los hechos deben cargarse en un paso separado
     */
    @Query("SELECT DISTINCT c FROM Coleccion c LEFT JOIN FETCH c.hechosPersistentes")
    java.util.List<Coleccion> findAllWithHechos();
}
