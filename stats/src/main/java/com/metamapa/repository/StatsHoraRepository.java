package com.metamapa.repository;

import com.metamapa.entity.StatsHora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para estadísticas por hora del día
 */
@Repository
public interface StatsHoraRepository extends JpaRepository<StatsHora, Long> {

    /**
     * Buscar estadísticas por categoría ordenadas por cantidad de hechos
     */
    @Query("SELECT s FROM StatsHora s WHERE s.categoria = :categoria ORDER BY s.cantidadHechos DESC")
    List<StatsHora> findByCategoriaOrderByCantidadHechosDesc(@Param("categoria") String categoria);

    /**
     * Obtener la hora con más hechos de una categoría
     */
    @Query(value = "SELECT * FROM stats_hora s WHERE s.categoria = :categoria ORDER BY s.cantidad_hechos DESC LIMIT 1", nativeQuery = true)
    Optional<StatsHora> findTopByCategoriaOrderByCantidadHechosDesc(@Param("categoria") String categoria);

    /**
     * Buscar estadísticas por hora
     */
    List<StatsHora> findByHora(Integer hora);

    /**
     * Buscar estadísticas por fecha de cálculo
     */
    List<StatsHora> findByFechaCalculoAfter(LocalDateTime fecha);

    /**
     * Eliminar estadísticas anteriores a una fecha
     */
    void deleteByFechaCalculoBefore(LocalDateTime fecha);
}
