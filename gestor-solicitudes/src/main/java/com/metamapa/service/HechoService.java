package com.metamapa.service;

import com.metamapa.dto.ActualizarHechoDTO;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.ubicaciones.Lugar;
import com.metamapa.entities.ubicaciones.Ubicacion;
import com.metamapa.repository.HechoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Servicio para gestionar operaciones sobre hechos
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HechoService {
    
    private final HechoRepository hechoRepository;
    private final ContribuyenteService contribuyenteService;
    
    /**
     * Actualiza un hecho existente
     * Reglas de negocio:
     * - Solo el contribuyente que creó el hecho puede editarlo
     * - Solo se puede editar dentro de los 7 días desde su creación
     * - Los administradores pueden editar cualquier hecho sin restricción de tiempo
     */
    @Transactional
    public Hecho actualizarHecho(Long hechoId, Long contribuyenteId, ActualizarHechoDTO dto, boolean esAdmin) {
        // Buscar el hecho
        Optional<Hecho> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("Hecho no encontrado con ID: " + hechoId);
        }
        
        Hecho hecho = hechoOpt.get();
        
        // Verificar si el hecho está eliminado
        if (hecho.isEliminado()) {
            throw new IllegalArgumentException("No se puede editar un hecho eliminado");
        }
        
        // Verificar permisos
        if (!esAdmin) {
            // Si no es admin, verificar que sea el dueño del hecho
            if (hecho.getContribuyente() == null || !hecho.getContribuyente().getId().equals(contribuyenteId)) {
                throw new SecurityException("No tienes permisos para editar este hecho");
            }
            
            // Verificar que no hayan pasado más de 7 días desde la creación
            LocalDateTime fechaCreacion = hecho.getFechaCarga();
            LocalDateTime fechaLimite = fechaCreacion.plusDays(7);
            LocalDateTime ahora = LocalDateTime.now();
            
            if (ahora.isAfter(fechaLimite)) {
                long diasPasados = ChronoUnit.DAYS.between(fechaCreacion, ahora);
                throw new IllegalArgumentException(
                    "No se puede editar el hecho. Han pasado " + diasPasados + 
                    " días desde su creación. Solo se permite editar dentro de los 7 días."
                );
            }
        }
        
        // Actualizar campos permitidos
        if (dto.getTitulo() != null && !dto.getTitulo().trim().isEmpty()) {
            hecho.setTitulo(dto.getTitulo());
        }
        
        if (dto.getDescripcion() != null && !dto.getDescripcion().trim().isEmpty()) {
            hecho.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getCategoria() != null && !dto.getCategoria().trim().isEmpty()) {
            hecho.setCategoria(dto.getCategoria());
        }
        
        if (dto.getFechaHecho() != null) {
            hecho.setFechaHecho(dto.getFechaHecho());
        }
        
        if (dto.getEtiquetas() != null) {
            hecho.setEtiquetas(dto.getEtiquetas());
        }
        
        // Actualizar ubicación si se proporciona
        if (dto.getLatitud() != null && dto.getLongitud() != null) {
            Ubicacion ubicacion = hecho.getUbicacion();
            if (ubicacion == null) {
                ubicacion = new Ubicacion();
                hecho.setUbicacion(ubicacion);
            }
            
            ubicacion.setLatitud(dto.getLatitud());
            ubicacion.setLongitud(dto.getLongitud());
            
            // Actualizar lugar si se proporciona
            if (dto.getPais() != null || dto.getProvincia() != null || dto.getMunicipio() != null) {
                Lugar lugar = ubicacion.getLugar();
                if (lugar == null) {
                    lugar = new Lugar();
                    ubicacion.setLugar(lugar);
                }
                
                if (dto.getPais() != null) {
                    lugar.setPais(dto.getPais());
                }
                if (dto.getProvincia() != null) {
                    lugar.setProvincia(dto.getProvincia());
                }
                if (dto.getMunicipio() != null) {
                    lugar.setMunicipio(dto.getMunicipio());
                }
            }
        }
        
        // Actualizar timestamp de última modificación
        hecho.actualizarUltimaActualizacion();
        
        // Guardar cambios
        Hecho hechoActualizado = hechoRepository.save(hecho);
        
        log.info("Hecho actualizado: ID={}, Contribuyente={}, EsAdmin={}", 
                hechoId, contribuyenteId, esAdmin);
        
        return hechoActualizado;
    }
    
    /**
     * Verifica si un contribuyente puede editar un hecho específico
     */
    public boolean puedeEditar(Long hechoId, Long contribuyenteId, boolean esAdmin) {
        if (esAdmin) {
            return true; // Los admins pueden editar cualquier hecho
        }
        
        Optional<Hecho> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent() || hechoOpt.get().isEliminado()) {
            return false;
        }
        
        Hecho hecho = hechoOpt.get();
        
        // Verificar que sea el dueño
        if (hecho.getContribuyente() == null || !hecho.getContribuyente().getId().equals(contribuyenteId)) {
            return false;
        }
        
        // Verificar que no hayan pasado más de 7 días
        LocalDateTime fechaCreacion = hecho.getFechaCarga();
        LocalDateTime fechaLimite = fechaCreacion.plusDays(7);
        
        return LocalDateTime.now().isBefore(fechaLimite) || LocalDateTime.now().isEqual(fechaLimite);
    }
}
