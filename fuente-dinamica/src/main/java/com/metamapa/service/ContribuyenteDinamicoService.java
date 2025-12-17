package com.metamapa.service;

import com.metamapa.domain.ContribuyenteDinamico;
import com.metamapa.repository.ContribuyenteDinamicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de contribuyentes en fuente-dinamica
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContribuyenteDinamicoService {
    
    private final ContribuyenteDinamicoRepository contribuyenteRepository;
    
    /**
     * Registrar un nuevo contribuyente
     */
    public ContribuyenteDinamico registrarContribuyente(ContribuyenteDinamico contribuyente) {
        log.info("Registrando nuevo contribuyente: {}", contribuyente.getNombreCompleto());
        
        // Validar datos básicos
        if (!contribuyente.esValido()) {
            throw new IllegalArgumentException("Los datos del contribuyente no son válidos");
        }
        
        return contribuyenteRepository.save(contribuyente);
    }
    
    /**
     * Buscar contribuyente por ID
     */
    public Optional<ContribuyenteDinamico> buscarPorId(String id) {
        return contribuyenteRepository.findById(id);
    }
    
    /**
     * Obtener todos los contribuyentes activos
     */
    public List<ContribuyenteDinamico> obtenerTodosActivos() {
        return contribuyenteRepository.findByEliminadoFalse();
    }
    
    /**
     * Actualizar contribuyente
     */
    public ContribuyenteDinamico actualizarContribuyente(ContribuyenteDinamico contribuyente) {
        log.info("Actualizando contribuyente: {}", contribuyente.getId());
        
        if (contribuyente.getId() == null) {
            throw new IllegalArgumentException("El ID del contribuyente es requerido para actualizar");
        }
        
        Optional<ContribuyenteDinamico> existente = contribuyenteRepository.findById(contribuyente.getId());
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("No se encontró el contribuyente a actualizar");
        }
        
        if (!contribuyente.esValido()) {
            throw new IllegalArgumentException("Los datos del contribuyente no son válidos");
        }
        
        return contribuyenteRepository.save(contribuyente);
    }
    
    /**
     * Eliminar contribuyente (soft delete)
     */
    public void eliminarContribuyente(String id) {
        log.info("Eliminando contribuyente: {}", id);
        
        Optional<ContribuyenteDinamico> contribuyenteOpt = contribuyenteRepository.findById(id);
        if (!contribuyenteOpt.isPresent()) {
            throw new IllegalArgumentException("No se encontró el contribuyente a eliminar");
        }
        
        ContribuyenteDinamico contribuyente = contribuyenteOpt.get();
        contribuyente.marcarComoEliminado();
        contribuyenteRepository.save(contribuyente);
    }
    
    /**
     * Buscar contribuyente por ID del agregador (para sincronización entre microservicios)
     */
    public ContribuyenteDinamico buscarPorIdAgregador(Long idAgregador) {
        return contribuyenteRepository.findFirstByIdAgregador(idAgregador);
    }
}