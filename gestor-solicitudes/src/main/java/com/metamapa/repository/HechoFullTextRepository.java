/*package com.metamapa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository

public interface HechoFullTextRepository extends Repository<Object, Long> {

    @Query(value =
            "SELECT " +
                    "  h.id, " +
                    "  h.titulo, " +
                    "  h.descripcion, " +
                    "  h.categoria, " +
                    "  h.coleccion_id, " +
                    "  MATCH(h.titulo, h.descripcion) AGAINST(:q IN BOOLEAN MODE) AS score " +
                    "FROM hecho h " +
                    "WHERE MATCH(h.titulo, h.descripcion) AGAINST(:q IN BOOLEAN MODE) " +
                    "  AND (:categoria IS NULL OR h.categoria = :categoria) " +
                    "  AND (:coleccionId IS NULL OR h.coleccion_id = :coleccionId) " +
                    "ORDER BY score DESC " +
                    "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Object[]> searchFullText(
            @Param("q") String query,
            @Param("categoria") String categoria,
            @Param("coleccionId") Long coleccionId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

}
*/