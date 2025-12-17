package com.metamapa.repository;

import com.metamapa.entities.hechos.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HechoRepository extends JpaRepository<Hecho, Long> {
    
    Optional<Hecho> findByFingerprint(String fingerprint);
    
    /**
     * Busca hechos que pertenecen a una fuente específica
     */
    @Query("SELECT h FROM Hecho h WHERE :fuenteId MEMBER OF h.fuentes")
    List<Hecho> findByFuente(@Param("fuenteId") String fuenteId);
    
    @Query("SELECT h FROM Hecho h WHERE LOWER(h.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Hecho> findByTituloContaining(@Param("titulo") String titulo);
    
    List<Hecho> findByCategoria(String categoria);
    
    /**
     * Obtiene hechos modificados/creados después de una fecha específica.
     * Útil para el módulo de estadísticas para procesar solo cambios incrementales.
     */
    @Query("SELECT h FROM Hecho h WHERE h.ultimaActualizacion > :desde ORDER BY h.ultimaActualizacion ASC")
    List<Hecho> findByUltimaActualizacionAfter(@Param("desde") LocalDateTime desde);
    
    /**
     * Obtiene hechos modificados/creados entre dos fechas.
     */
    @Query("SELECT h FROM Hecho h WHERE h.ultimaActualizacion BETWEEN :desde AND :hasta ORDER BY h.ultimaActualizacion ASC")
    List<Hecho> findByUltimaActualizacionBetween(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query(value =
            "SELECT " +
                    "  h.id, " +
                    "  h.titulo, " +
                    "  h.descripcion, " +
                    "  h.categoria, " +
                    "  MATCH(h.titulo, h.descripcion) AGAINST(:q IN BOOLEAN MODE) AS score " +
                    "FROM hecho h " +
                    "WHERE MATCH(h.titulo, h.descripcion) AGAINST(:q IN BOOLEAN MODE) " +
                    "  AND (:categoria IS NULL OR h.categoria = :categoria) " +
                    "ORDER BY score DESC " +
                    "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Object[]> searchFullText(
            @Param("q") String query,
            @Param("categoria") String categoria,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    
    /**
     * Carga hechos con sus etiquetas precargadas usando JOIN FETCH
     * Esto evita LazyInitializationException al serializar
     */
    @Query("SELECT DISTINCT h FROM Hecho h LEFT JOIN FETCH h.etiquetas WHERE h.id IN :ids")
    List<Hecho> findByIdsWithEtiquetas(@Param("ids") List<Long> ids);
}
