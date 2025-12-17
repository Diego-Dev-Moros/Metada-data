package com.metamapa.repository;

import com.metamapa.entity.StatsProvincia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para estadísticas por provincia
 */
@Repository
public interface StatsProvinciaRepository extends JpaRepository<StatsProvincia, Long> {

    /**
     * Buscar estadísticas por colección ordenadas por cantidad de hechos
     */
    @Query("SELECT s FROM StatsProvincia s WHERE s.coleccionId = :coleccionId ORDER BY s.cantidadHechos DESC")
    List<StatsProvincia> findByColeccionIdOrderByCantidadHechosDesc(@Param("coleccionId") Long coleccionId);

    /**
     * Obtener la provincia con más hechos de una colección
     */
    @Query(value = "SELECT * FROM stats_provincia s WHERE s.coleccion_id = :coleccionId ORDER BY s.cantidad_hechos DESC LIMIT 1", nativeQuery = true)
    Optional<StatsProvincia> findTopByColeccionIdOrderByCantidadHechosDesc(@Param("coleccionId") Long coleccionId);

    /**
     * Buscar estadísticas por provincia
     */
    List<StatsProvincia> findByProvincia(String provincia);

    /**
     * Buscar estadísticas por fecha de cálculo
     */
    List<StatsProvincia> findByFechaCalculoAfter(LocalDateTime fecha);

    /**
     * Eliminar estadísticas anteriores a una fecha
     */
    void deleteByFechaCalculoBefore(LocalDateTime fecha);
}
