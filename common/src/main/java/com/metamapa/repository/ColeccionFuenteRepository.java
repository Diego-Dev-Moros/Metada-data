package com.metamapa.repository;

import com.metamapa.entities.colecciones.ColeccionFuente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColeccionFuenteRepository extends JpaRepository<ColeccionFuente, Long> {
    
    @Query("SELECT cf FROM ColeccionFuente cf WHERE cf.coleccion.identificador = :coleccionId")
    List<ColeccionFuente> findByColeccionId(@Param("coleccionId") Long coleccionId);
    
    @Query("SELECT cf FROM ColeccionFuente cf WHERE cf.identificadorFuente = :identificadorFuente")
    List<ColeccionFuente> findByIdentificadorFuente(@Param("identificadorFuente") String identificadorFuente);
    
    @Modifying
    @Query("DELETE FROM ColeccionFuente cf WHERE cf.coleccion.identificador = :coleccionId")
    void deleteByColeccionId(@Param("coleccionId") Long coleccionId);
}