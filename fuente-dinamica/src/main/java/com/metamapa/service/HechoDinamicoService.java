package com.metamapa.service;

import com.metamapa.domain.ContribuyenteDinamico;
import com.metamapa.domain.HechoDinamico;
import com.metamapa.domain.MultimediaDinamica;
import com.metamapa.domain.UbicacionDinamica;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.repository.HechoDinamicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de hechos en fuente-dinamica
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HechoDinamicoService {
    
    private final HechoDinamicoRepository hechoRepository;
    private final ContribuyenteDinamicoService contribuyenteService;
    
    /**
     * Crear un nuevo hecho
     */
    public HechoDinamico crearHecho(HechoDinamico hecho) {
        log.info("Creando nuevo hecho: {}", hecho.getTitulo());
        
        // Validaciones básicas
        if (hecho.getTitulo() == null || hecho.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del hecho es requerido");
        }
        
        if (hecho.getDescripcion() == null || hecho.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del hecho es requerida");
        }
        
        // Configurar valores por defecto
        hecho.setFechaCarga(LocalDateTime.now());
        hecho.setEstadoRevision(EstadoRevision.PENDIENTE);
        hecho.setOrigen(OrigenHecho.CONTRIBUYENTE);
        hecho.setEliminado(false);
        
        return hechoRepository.save(hecho);
    }
    
    /**
     * Crear hecho con contribuyente
     */
    public HechoDinamico crearHechoConContribuyente(HechoDinamico hecho, String contribuyenteId) {
        log.info("Creando hecho con contribuyente: {}", contribuyenteId);
        
        // Buscar el contribuyente
        Optional<ContribuyenteDinamico> contribuyenteOpt = contribuyenteService.buscarPorId(contribuyenteId);
        if (!contribuyenteOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el contribuyente especificado");
        }
        
        hecho.setContribuyente(contribuyenteOpt.get());
        // Solo setear esAnonimo a false si no se especificó explícitamente como true
        if (!hecho.isEsAnonimo()) {
            hecho.setEsAnonimo(false);
        }
        
        return crearHecho(hecho);
    }
    
    /**
     * Crear hecho anónimo
     */
    public HechoDinamico crearHechoAnonimo(HechoDinamico hecho) {
        log.info("Creando hecho anónimo");
        
        // Para hechos anónimos, no se asigna contribuyente
        hecho.setContribuyente(null);
        hecho.setEsAnonimo(true);
        
        return crearHecho(hecho);
    }
    
    /**
     * Buscar hecho por ID
     */
    public Optional<HechoDinamico> buscarPorId(String id) {
        return hechoRepository.findById(id);
    }
    
    /**
     * Obtener todos los hechos activos
     */
    public List<HechoDinamico> obtenerTodosActivos() {
        return hechoRepository.findByEliminadoFalse();
    }
    
    /**
     * Obtener hechos pendientes de revisión
     */
    public List<HechoDinamico> obtenerHechosPendientes() {
        return hechoRepository.findByEstadoRevisionAndEliminadoFalse(EstadoRevision.PENDIENTE, false);
    }
    
    /**
     * Moderar hecho - Aceptar
     */
    public HechoDinamico aceptarHecho(String hechoId) {
        log.info("Aceptando hecho: {}", hechoId);
        
        Optional<HechoDinamico> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el hecho especificado");
        }
        
        HechoDinamico hecho = hechoOpt.get();
        hecho.aceptarRevision();
        
        return hechoRepository.save(hecho);
    }
    
    /**
     * Moderar hecho - Rechazar
     */
    public HechoDinamico rechazarHecho(String hechoId, String motivo) {
        log.info("Rechazando hecho: {} con motivo: {}", hechoId, motivo);
        
        Optional<HechoDinamico> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el hecho especificado");
        }
        
        HechoDinamico hecho = hechoOpt.get();
        hecho.rechazarRevision(motivo);
        
        return hechoRepository.save(hecho);
    }
    
    /**
     * Moderar hecho - Aceptar con sugerencias
     */
    public HechoDinamico aceptarHechoConSugerencias(String hechoId, String sugerencias) {
        log.info("Aceptando hecho con sugerencias: {}", hechoId);
        
        Optional<HechoDinamico> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el hecho especificado");
        }
        
        HechoDinamico hecho = hechoOpt.get();
        hecho.aceptarRevisionConSugerencia(sugerencias);
        
        return hechoRepository.save(hecho);
    }
    
    /**
     * Obtener hechos por estado de revisión
     */
    public List<HechoDinamico> obtenerHechosPorEstado(EstadoRevision estado) {
        return hechoRepository.findByEstadoRevision(estado);
    }
    
    /**
     * Obtener hechos por categoría
     */
    public List<HechoDinamico> obtenerHechosPorCategoria(String categoria) {
        return hechoRepository.findByCategoria(categoria);
    }
    
    /**
     * Obtener hechos recientes (últimos 7 días)
     */
    public List<HechoDinamico> obtenerHechosRecientes() {
        LocalDateTime fechaDesde = LocalDateTime.now().minusDays(7);
        return hechoRepository.findHechosRecientes(fechaDesde);
    }
    
    /**
     * Eliminar hecho (soft delete)
     */
    public void eliminarHecho(String id) {
        log.info("Eliminando hecho: {}", id);
        
        Optional<HechoDinamico> hechoOpt = hechoRepository.findById(id);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el hecho a eliminar");
        }
        
        HechoDinamico hecho = hechoOpt.get();
        hecho.marcarComoEliminado();
        hechoRepository.save(hecho);
    }
    
    /**
     * Actualizar ubicación del hecho
     */
    public HechoDinamico actualizarUbicacion(String hechoId, UbicacionDinamica ubicacion) {
        log.info("Actualizando ubicación del hecho: {}", hechoId);
        
        Optional<HechoDinamico> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el hecho");
        }
        
        HechoDinamico hecho = hechoOpt.get();
        hecho.setUbicacion(ubicacion);
        
        return hechoRepository.save(hecho);
    }

    /**
     * Agregar multimedia a un hecho existente
     */
    public HechoDinamico agregarMultimedia(String hechoId, List<MultimediaDinamica> nuevosArchivos) {
        Optional<HechoDinamico> hechoOpt = hechoRepository.findById(hechoId);
        if (!hechoOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el hecho especificado");
        }
        HechoDinamico hecho = hechoOpt.get();
        if (hecho.getMultimedias() == null) {
            hecho.setMultimedias(new ArrayList<>());
        }
        hecho.getMultimedias().addAll(nuevosArchivos);
        return hechoRepository.save(hecho);
    }


    /**
     * Contar hechos por estado
     */
    public long contarHechosPorEstado(EstadoRevision estado) {
        return hechoRepository.countByEstadoRevision(estado);
    }
}