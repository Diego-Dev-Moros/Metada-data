package com.metamapa.repository;

import com.metamapa.entity.StatsCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para estadísticas por categoría
 */
@Repository
public interface StatsCategoriaRepository extends JpaRepository<StatsCategoria, Long> {

    /**
     * Buscar estadísticas ordenadas por cantidad de hechos
     */
    @Query("SELECT s FROM StatsCategoria s ORDER BY s.cantidadHechos DESC")
    List<StatsCategoria> findAllOrderByCantidadHechosDesc();

    /**
     * Obtener la categoría con más hechos
     */
    @Query(value = "SELECT * FROM stats_categoria s ORDER BY s.cantidad_hechos DESC LIMIT 1", nativeQuery = true)
    Optional<StatsCategoria> findTopOrderByCantidadHechosDesc();

    /**
     * Buscar estadísticas por categoría
     */
    List<StatsCategoria> findByCategoria(String categoria);

    /**
     * Buscar estadísticas por fecha de cálculo
     */
    List<StatsCategoria> findByFechaCalculoAfter(LocalDateTime fecha);

    /**
     * Eliminar estadísticas anteriores a una fecha
     */
    void deleteByFechaCalculoBefore(LocalDateTime fecha);
}
