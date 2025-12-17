package com.metamapa.repository;

import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository para consultas de solicitudes de eliminación
 * Accede a las entidades del módulo domain
 */
@Repository
public interface SolicitudEliminacionRepository extends JpaRepository<SolicitudEliminacion, Long> {
    
    /**
     * Contar total de solicitudes de eliminación
     */
    @Query("SELECT COUNT(s) FROM SolicitudEliminacion s")
    Long countTotalSolicitudes();
    
    /**
     * Contar solicitudes de eliminación que son spam
     */
    @Query("SELECT COUNT(s) FROM SolicitudEliminacion s WHERE s.esSpam = true")
    Long countSolicitudesSpam();
    
    /**
     * Contar solicitudes de eliminación que NO son spam
     */
    @Query("SELECT COUNT(s) FROM SolicitudEliminacion s WHERE s.esSpam = false")
    Long countSolicitudesNoSpam();
    
    /**
     * Calcular porcentaje de spam
     */
    @Query("SELECT (COUNT(CASE WHEN s.esSpam = true THEN 1 END) * 100.0 / COUNT(s)) FROM SolicitudEliminacion s")
    Double calcularPorcentajeSpam();
}
