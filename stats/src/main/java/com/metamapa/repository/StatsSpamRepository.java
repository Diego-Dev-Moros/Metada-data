package com.metamapa.repository;

import com.metamapa.entity.StatsSpam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para estadísticas de spam
 */
@Repository
public interface StatsSpamRepository extends JpaRepository<StatsSpam, Long> {

    /**
     * Obtener la estadística de spam más reciente
     */
    @Query(value = "SELECT * FROM stats_spam s ORDER BY s.fecha_calculo DESC LIMIT 1", nativeQuery = true)
    Optional<StatsSpam> findLatest();

    /**
     * Buscar estadísticas por fecha de cálculo
     */
    List<StatsSpam> findByFechaCalculoAfter(LocalDateTime fecha);

    /**
     * Eliminar estadísticas anteriores a una fecha
     */
    void deleteByFechaCalculoBefore(LocalDateTime fecha);
}
