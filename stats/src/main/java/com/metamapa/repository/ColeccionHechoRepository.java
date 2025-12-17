package com.metamapa.repository;

import com.metamapa.entities.colecciones.ColeccionHecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColeccionHechoRepository extends JpaRepository<ColeccionHecho, Long> {

    @Query("SELECT DISTINCT ch.coleccion.identificador FROM ColeccionHecho ch")
    List<Long> findDistinctColeccionIds();
}


