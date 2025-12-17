package com.metamapa.repository;

import com.metamapa.entity.StatsMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository para metadatos de estadísticas
 */
@Repository
public interface StatsMetadataRepository extends JpaRepository<StatsMetadata, Long> {
    
    /**
     * Obtener los metadatos de estadísticas
     */
    @Query("SELECT s FROM StatsMetadata s WHERE s.id = 1")
    Optional<StatsMetadata> findStatsMetadata();
    
    /**
     * Actualizar la última actualización de estadísticas
     */
    @Modifying
    @Query("UPDATE StatsMetadata s SET s.ultimaActualizacionStats = :fecha WHERE s.id = 1")
    void actualizarUltimaActualizacionStats(@Param("fecha") LocalDateTime fecha);
    
    /**
     * Actualizar el total de hechos procesados
     */
    @Modifying
    @Query("UPDATE StatsMetadata s SET s.totalHechosProcesados = :total WHERE s.id = 1")
    void actualizarTotalHechosProcesados(@Param("total") Long total);
    
    /**
     * Actualizar la versión
     */
    @Modifying
    @Query("UPDATE StatsMetadata s SET s.version = :version WHERE s.id = 1")
    void actualizarVersion(@Param("version") String version);
}
