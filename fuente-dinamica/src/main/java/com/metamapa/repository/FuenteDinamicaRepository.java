package com.metamapa.repository;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.rol.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuenteDinamicaRepository extends JpaRepository<Hecho, Long> {
    
    // Métodos para hechos por estado de revisión
    List<Hecho> findByEstadoRevision(EstadoRevision estado);
    
    @Query("SELECT h FROM Hecho h WHERE h.estadoRevision = 'ACEPTADO' OR h.estadoRevision = 'ACEPTADO_CON_SUGERENCIAS'")
    List<Hecho> findHechosAprobados();
    
    @Query("SELECT h FROM Hecho h WHERE h.estadoRevision = 'PENDIENTE'")
    List<Hecho> findHechosPendientes();
    
    @Query("SELECT h FROM Hecho h WHERE h.estadoRevision = 'RECHAZADO'")
    List<Hecho> findHechosRechazados();
    
    @Query("SELECT h FROM Hecho h WHERE h.estadoRevision = 'ACEPTADO_CON_SUGERENCIAS'")
    List<Hecho> findHechosConSugerencias();
}



