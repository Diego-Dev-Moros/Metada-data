package com.metamapa.repository;

import com.metamapa.entities.colecciones.ColeccionHecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColeccionHechoRepository extends JpaRepository<ColeccionHecho, Long> {
    
    @Query("SELECT ch FROM ColeccionHecho ch WHERE ch.coleccion.identificador = :coleccionId")
    List<ColeccionHecho> findByColeccionId(@Param("coleccionId") Long coleccionId);
    
    @Query("SELECT ch FROM ColeccionHecho ch WHERE ch.coleccion.identificador = :coleccionId AND ch.consensuado = :consensuado")
    List<ColeccionHecho> findByColeccionIdAndConsensuado(@Param("coleccionId") Long coleccionId, @Param("consensuado") Boolean consensuado);
    
    @Query("SELECT ch FROM ColeccionHecho ch WHERE ch.hecho.id = :hechoId")
    List<ColeccionHecho> findByHechoId(@Param("hechoId") Long hechoId);
    
    @Modifying
    @Query("DELETE FROM ColeccionHecho ch WHERE ch.coleccion.identificador = :coleccionId")
    void deleteByColeccionId(@Param("coleccionId") Long coleccionId);
}