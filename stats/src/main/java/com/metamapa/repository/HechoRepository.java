package com.metamapa.repository;

import com.metamapa.entities.hechos.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para consultas directas a la base de datos compartida
 * Accede a las entidades del módulo domain
 */
@Repository
public interface HechoRepository extends JpaRepository<Hecho, Long> {

    /**
     * Buscar hechos nuevos o modificados desde la última actualización.
     * Reemplaza findByFechaCargaAfter para considerar también modificaciones.
     */
    @Query("SELECT h FROM Hecho h WHERE h.ultimaActualizacion > :fecha ORDER BY h.ultimaActualizacion")
    List<Hecho> findByUltimaActualizacionAfter(@Param("fecha") LocalDateTime fecha);
    
    /**
     * Buscar hechos modificados entre dos fechas.
     */
    @Query("SELECT h FROM Hecho h WHERE h.ultimaActualizacion BETWEEN :inicio AND :fin ORDER BY h.ultimaActualizacion")
    List<Hecho> findByUltimaActualizacionBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    /**
     * Buscar hechos nuevos desde la última carga (sin considerar modificaciones)
     */
    @Query("SELECT h FROM Hecho h WHERE h.fechaCarga > :fecha ORDER BY h.fechaCarga")
    List<Hecho> findByFechaCargaAfter(@Param("fecha") LocalDateTime fecha);

    /**
     * Obtener el timestamp de la última modificación en la BD
     */
    @Query("SELECT MAX(h.ultimaActualizacion) FROM Hecho h")
    LocalDateTime findMaxUltimaActualizacion();
    
    /**
     * Obtener el hecho más reciente por fecha de carga
     */
    @Query("SELECT MAX(h.fechaCarga) FROM Hecho h")
    LocalDateTime findMaxFechaCarga();

    /**
     * Contar hechos por rango de fechas
     */
    @Query("SELECT COUNT(h) FROM Hecho h WHERE h.fechaCarga BETWEEN :inicio AND :fin")
    Long countByFechaCargaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Buscar hechos por colección y fecha
     * Nota: Hecho no tiene relación directa 'coleccion'; se usa ColeccionHecho
     */
    @Query("SELECT h FROM ColeccionHecho ch JOIN ch.hecho h WHERE ch.coleccion.identificador = :coleccionId AND h.fechaCarga > :fecha")
    List<Hecho> findByColeccionAndFechaCargaAfter(@Param("coleccionId") Long coleccionId, @Param("fecha") LocalDateTime fecha);

    /**
     * Estadísticas por provincia para una colección
     */
    @Query("SELECT h.ubicacion.lugar.provincia, COUNT(h) FROM ColeccionHecho ch JOIN ch.hecho h WHERE ch.coleccion.identificador = :coleccionId GROUP BY h.ubicacion.lugar.provincia ORDER BY COUNT(h) DESC")
    List<Object[]> findStatsPorProvincia(@Param("coleccionId") Long coleccionId);

    /**
     * Estadísticas por categoría
     */
    @Query("SELECT h.categoria, COUNT(h) FROM Hecho h GROUP BY h.categoria ORDER BY COUNT(h) DESC")
    List<Object[]> findStatsPorCategoria();

    /**
     * Estadísticas por provincia y categoría
     */
    @Query("SELECT h.ubicacion.lugar.provincia, COUNT(h) FROM Hecho h WHERE h.categoria = :categoria GROUP BY h.ubicacion.lugar.provincia ORDER BY COUNT(h) DESC")
    List<Object[]> findStatsPorProvinciaYCategoria(@Param("categoria") String categoria);

    /**
     * Estadísticas por hora y categoría
     */
    @Query("SELECT HOUR(h.fechaHecho), COUNT(h) FROM Hecho h WHERE h.categoria = :categoria GROUP BY HOUR(h.fechaHecho) ORDER BY COUNT(h) DESC")
    List<Object[]> findStatsPorHoraYCategoria(@Param("categoria") String categoria);

    /**
     * Contar total de hechos
     */
    @Query("SELECT COUNT(h) FROM Hecho h")
    Long countTotalHechos();

    /**
     * Contar hechos por colección
     */
    @Query("SELECT COUNT(h) FROM ColeccionHecho ch JOIN ch.hecho h WHERE ch.coleccion.identificador = :coleccionId")
    Long countByColeccionId(@Param("coleccionId") Long coleccionId);
}
