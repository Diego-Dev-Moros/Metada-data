package com.metamapa.service;

import com.metamapa.entities.solicitudes.EstadoSolicitud;
import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.spam.DetectorDeSpam;
import com.metamapa.repository.SolicitudEliminacionRepository;
import com.metamapa.repository.HechoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitudService {
    
    private final SolicitudEliminacionRepository solicitudRepository;
    private final HechoRepository hechoRepository;
    private final DetectorDeSpam detectorDeSpam;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public SolicitudEliminacion crearSolicitudEliminacion(Hecho hecho, String justificacion, Contribuyente contribuyente) {
        if (hecho == null) {
            throw new IllegalArgumentException("El hecho es requerido");
        }

        if (justificacion == null || justificacion.trim().length() < 50) {
            throw new IllegalArgumentException("La justificación debe tener al menos 50 caracteres para ser considerada válida");
        }
        
        try {
            // VALIDACIÓN 2: Detector de spam (puede ser intercambiable en el futuro)
            boolean esSpam = detectorDeSpam.esSpam(justificacion);
            if (esSpam) {
                // No lanzamos excepción, creamos la solicitud pero la marcamos como spam
                log.warn("Solicitud detectada como spam para hecho: {}", hecho.getTitulo());
            }
            
            EstadoSolicitud estado = esSpam ? EstadoSolicitud.RECHAZADA : EstadoSolicitud.PENDIENTE;
            
            SolicitudEliminacion solicitud = new SolicitudEliminacion();
            // El ID será generado automáticamente por JPA (@GeneratedValue)
            solicitud.setMotivo(justificacion);
            solicitud.setEstado(estado);
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitud.setEsSpam(esSpam);
            
            // Vincular con el hecho real
            solicitud.setHecho(hecho);
            
            // Asignar contribuyente si se proporciona (puede ser null para solicitudes anónimas)
            if (contribuyente != null) {
                solicitud.setContribuyente(contribuyente);
            }
            
            // Usar el método save() de JPA para persistir en MySQL
            SolicitudEliminacion solicitudGuardada = solicitudRepository.save(solicitud);
            
            log.info("Solicitud de eliminación creada: {} - Hecho: {} - Estado: {} - EsSpam: {} - Contribuyente: {}", 
                    solicitudGuardada.getId(), hecho.getTitulo(), estado, esSpam, contribuyente != null ? contribuyente.getNombre() : "Anónimo");
            
            return solicitudGuardada;
            
        } catch (Exception e) {
            log.error("Error al crear solicitud de eliminación: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error interno al crear la solicitud: " + e.getMessage());
        }
    }

    @Transactional
    public void aprobarSolicitud(Long idSolicitud) {
        log.info("=== Intentando aprobar solicitud ID: {} ===", idSolicitud);
        
        SolicitudEliminacion solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        
        if (solicitud == null) {
            log.warn("Solicitud con ID {} NO ENCONTRADA en la base de datos", idSolicitud);
            return;
        }
        
        log.info("Solicitud encontrada - Estado actual: {}, Hecho ID: {}", 
                solicitud.getEstado(), 
                solicitud.getHecho() != null ? solicitud.getHecho().getId() : "null");
        
        // Cambiar estado
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaResolucion(LocalDateTime.now());

        // Marcar hecho como eliminado y guardarlo
        Hecho hecho = solicitud.getHecho();
        if (hecho != null) {
            log.info("Marcando hecho {} como eliminado", hecho.getId());
            hecho.marcarComoEliminado();
            hechoRepository.save(hecho);
            log.info("Hecho {} guardado con eliminado={}", hecho.getId(), hecho.isEliminado());
        }

        // Guardar la solicitud
        SolicitudEliminacion guardada = solicitudRepository.save(solicitud);
        
        // Forzar flush a la base de datos
        entityManager.flush();
        
        log.info("Solicitud guardada - Nuevo estado: {}, Fecha resolución: {}", 
                guardada.getEstado(), guardada.getFechaResolucion());

        // NOTA: El agregador detectará automáticamente que el hecho fue marcado como eliminado
        // en su próxima ejecución del scheduler (cada 2 minutos) y actualizará las colecciones

        log.info("Solicitud aprobada: {}. El agregador actualizará las colecciones automáticamente.", idSolicitud);
    }

    @Transactional
    public void rechazarSolicitud(Long idSolicitud) {
        log.info("=== Intentando rechazar solicitud ID: {} ===", idSolicitud);
        
        SolicitudEliminacion solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        
        if (solicitud == null) {
            log.warn("Solicitud con ID {} NO ENCONTRADA en la base de datos", idSolicitud);
            return;
        }
        
        log.info("Solicitud encontrada - Estado actual: {}", solicitud.getEstado());
        
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaResolucion(LocalDateTime.now());
        
        SolicitudEliminacion guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud rechazada: {} - Nuevo estado: {}", idSolicitud, guardada.getEstado());
    }

    public List<SolicitudEliminacion> obtenerSolicitudesPendientes() {
        return solicitudRepository.findAll().stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<SolicitudEliminacion> obtenerTodasLasSolicitudes() {
        return solicitudRepository.findAll();
    }

    public SolicitudEliminacion obtenerSolicitud(Long idSolicitud) {
        return solicitudRepository.findById(idSolicitud).orElse(null);
    }
}

