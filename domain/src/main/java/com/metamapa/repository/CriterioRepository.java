package com.metamapa.repository;

import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.colecciones.ColeccionCriterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriterioRepository extends JpaRepository<ColeccionCriterio, Long> {
    List<ColeccionCriterio> findByColeccion(Coleccion coleccion);
}