package com.metamapa.service;

import com.metamapa.client.FuenteDinamicaCrudClient;
import com.metamapa.dto.ContribuyenteDTO;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.rol.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContribuyenteService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final FuenteDinamicaCrudClient fuenteDinamicaCrudClient;
    
    /**
     * Crea un nuevo contribuyente en el agregador Y lo sincroniza con la fuente dinámica
     */
    @Transactional
    public Contribuyente crearContribuyente(ContribuyenteDTO dto) {
        // 1. Crear en el agregador (MySQL)
        Contribuyente contribuyente = new Contribuyente();
        contribuyente.setNombre(dto.getNombre());
        contribuyente.setApellido(dto.getApellido());
        contribuyente.setFechaNacimiento(dto.getFechaNacimiento());
        contribuyente.setFechaRegistro(LocalDateTime.now());
        
        // Si no se especifica rol, usar CONTRIBUYENTE por defecto
        if (dto.getRol() != null) {
            contribuyente.setRol(dto.getRol());
        } else {
            contribuyente.setRol(Rol.CONTRIBUYENTE);
        }
        
        entityManager.persist(contribuyente);
        entityManager.flush(); // Forzar la generación del ID
        
        log.info("Contribuyente creado en agregador: {} {} (ID MySQL: {})", 
                contribuyente.getNombre(), 
                contribuyente.getApellido(), 
                contribuyente.getId());
        
        // 2. Sincronizar con fuente dinámica (MongoDB)
        try {
            String tipoUsuario = (contribuyente.getRol() == Rol.ADMINISTRADOR) ? "ADMINISTRADOR" : "CONTRIBUYENTE";
            String mongoId = fuenteDinamicaCrudClient.registrarContribuyenteEnFuenteDinamica(
                    contribuyente.getId(),
                    contribuyente.getNombre(),
                    contribuyente.getApellido(),
                    contribuyente.getFechaNacimiento(),
                    tipoUsuario
            );
            
            if (mongoId != null) {
                log.info("Contribuyente sincronizado con fuente dinámica. MongoDB ID: {}", mongoId);
            } else {
                log.warn("No se pudo sincronizar el contribuyente con fuente dinámica (puede estar offline)");
            }
        } catch (Exception e) {
            log.error("Error al sincronizar contribuyente con fuente dinámica: {}", e.getMessage(), e);
            // No fallamos la transacción, solo registramos el error
        }
        
        return contribuyente;
    }
    
    /**
     * Obtiene un contribuyente por ID
     */
    public Contribuyente obtenerContribuyentePorId(Long id) {
        return entityManager.find(Contribuyente.class, id);
    }
    
    /**
     * Obtiene todos los contribuyentes
     */
    public List<Contribuyente> obtenerTodosLosContribuyentes() {
        return entityManager.createQuery("SELECT c FROM Contribuyente c", Contribuyente.class)
                .getResultList();
    }
    
    /**
     * Verifica si un contribuyente existe
     */
    public boolean existeContribuyente(Long id) {
        return obtenerContribuyentePorId(id) != null;
    }
    
    /**
     * Busca un contribuyente por nombre, apellido y fecha de nacimiento.
     * Si no existe, lo crea.
     * Este método es útil cuando se reciben hechos de fuente dinámica con contribuyente.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public Contribuyente buscarOCrearContribuyente(String nombre, String apellido, LocalDate fechaNacimiento, Rol rol) {
        // Buscar contribuyente existente
        List<Contribuyente> contribuyentes = entityManager
                .createQuery("SELECT c FROM Contribuyente c WHERE c.nombre = :nombre AND c.apellido = :apellido AND c.fechaNacimiento = :fecha", 
                             Contribuyente.class)
                .setParameter("nombre", nombre)
                .setParameter("apellido", apellido)
                .setParameter("fecha", fechaNacimiento)
                .getResultList();
        
        if (!contribuyentes.isEmpty()) {
            log.debug("Contribuyente encontrado: {} {} (ID: {})", nombre, apellido, contribuyentes.get(0).getId());
            return contribuyentes.get(0);
        }
        
        // Si no existe, crear uno nuevo
        log.info("Creando nuevo contribuyente: {} {}", nombre, apellido);
        Contribuyente nuevoContribuyente = new Contribuyente();
        nuevoContribuyente.setNombre(nombre);
        nuevoContribuyente.setApellido(apellido);
        nuevoContribuyente.setFechaNacimiento(fechaNacimiento);
        nuevoContribuyente.setFechaRegistro(LocalDateTime.now());
        nuevoContribuyente.setRol(rol != null ? rol : Rol.CONTRIBUYENTE);
        
        entityManager.persist(nuevoContribuyente);
        entityManager.flush();
        
        log.info("Nuevo contribuyente creado con ID: {}", nuevoContribuyente.getId());
        return nuevoContribuyente;
    }
    
    /**
     * Actualiza el perfil de un contribuyente existente
     * Solo el propio usuario puede actualizar su perfil
     */
    @Transactional
    public Contribuyente actualizarPerfil(Long contribuyenteId, String nombre, String apellido, LocalDate fechaNacimiento) {
        Contribuyente contribuyente = obtenerContribuyentePorId(contribuyenteId);
        
        if (contribuyente == null) {
            throw new IllegalArgumentException("Contribuyente no encontrado con ID: " + contribuyenteId);
        }
        
        // Actualizar campos
        if (nombre != null && !nombre.trim().isEmpty()) {
            contribuyente.setNombre(nombre);
        }
        
        if (apellido != null && !apellido.trim().isEmpty()) {
            contribuyente.setApellido(apellido);
        }
        
        if (fechaNacimiento != null) {
            contribuyente.setFechaNacimiento(fechaNacimiento);
        }
        
        entityManager.merge(contribuyente);
        entityManager.flush();
        
        log.info("Perfil actualizado para contribuyente ID: {}", contribuyenteId);
        
        return contribuyente;
    }
}
