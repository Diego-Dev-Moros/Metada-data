package com.metamapa.repository;

import com.metamapa.entities.colecciones.ColeccionCriterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColeccionCriterioRepository extends JpaRepository<ColeccionCriterio, Long> {
    
    @Query("SELECT cc FROM ColeccionCriterio cc WHERE cc.coleccion.identificador = :coleccionId")
    List<ColeccionCriterio> findByColeccionId(@Param("coleccionId") Long coleccionId);
    
    @Query("SELECT cc FROM ColeccionCriterio cc WHERE cc.coleccion.identificador = :coleccionId AND cc.tipoCriterio = :tipo")
    List<ColeccionCriterio> findByColeccionIdAndTipo(@Param("coleccionId") Long coleccionId, @Param("tipo") String tipo);
    
    @Modifying
    @Query("DELETE FROM ColeccionCriterio cc WHERE cc.coleccion.identificador = :coleccionId")
    void deleteByColeccionId(@Param("coleccionId") Long coleccionId);
}