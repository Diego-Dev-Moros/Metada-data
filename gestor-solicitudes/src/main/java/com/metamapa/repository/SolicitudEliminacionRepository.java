package com.metamapa.repository;

import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudEliminacionRepository extends JpaRepository<SolicitudEliminacion, Long> {
    // JpaRepository ya incluye los métodos:
    // - save(SolicitudEliminacion)
    // - findById(Long)
    // - findAll()
    // - delete(SolicitudEliminacion)
    // etc.
}
