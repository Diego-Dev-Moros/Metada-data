package com.metamapa.service;

import com.metamapa.dto.CrearColeccionDTO;
import com.metamapa.dto.ModificarColeccionDTO;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.MetodoDeNavegacion;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.colecciones.ColeccionFuente;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.colecciones.AlgoritmoDeConsenso;
import com.metamapa.entities.colecciones.Absoluta;
import com.metamapa.entities.colecciones.MayoriaSimple;
import com.metamapa.entities.colecciones.MultiplesMenciones;
import com.metamapa.entities.colecciones.PorDefecto;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.repository.ColeccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de CRUD de colecciones (APIs administrativas/públicas)
 * Este servicio migrará al módulo gestor-solicitudes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ColeccionService {
    
    private final ContribuyenteService contribuyenteService;
    private final CriterioService criterioService;
    private final ColeccionRepository coleccionRepository;
    private final FuenteService fuenteService;
    
    /**
     * Crea una colección CON administrador (obligatorio)
     * @param dto Datos de la colección
     * @param idAdministrador ID del administrador (REQUERIDO)
     * @return Colección creada
     * @throws IllegalArgumentException si el administrador no existe o no se proporciona
     */
    @Transactional
    public Coleccion crearColeccion(CrearColeccionDTO dto, Long idAdministrador) {
        // Validar que se proporcione un administrador
        if (idAdministrador == null) {
            throw new IllegalArgumentException("El ID del administrador es requerido");
        }
        
        // El ID se generará automáticamente por JPA con @GeneratedValue(strategy = GenerationType.IDENTITY)
        // No se debe proporcionar manualmente
        
        // Validar y obtener el administrador
        Contribuyente administrador = contribuyenteService.obtenerContribuyentePorId(idAdministrador);
        if (administrador == null) {
            throw new IllegalArgumentException("Administrador no encontrado con ID: " + idAdministrador);
        }
        
        // Validar algoritmo de consenso
        AlgoritmoDeConsenso algoritmo = obtenerAlgoritmo(dto.getAlgoritmoConsenso());
        if (algoritmo == null) {
            throw new IllegalArgumentException("Algoritmo de consenso no válido: " + dto.getAlgoritmoConsenso());
        }
        
        // Obtener las fuentes si se proporcionaron identificadores
        List<FuenteDeDatos> fuentesColeccion = new ArrayList<>();
        if (dto.getFuenteIds() != null && !dto.getFuenteIds().isEmpty()) {
            for (String fuenteId : dto.getFuenteIds()) {
                FuenteDeDatos fuente = fuenteService.obtenerFuentePorId(fuenteId);
                if (fuente != null) {
                    fuentesColeccion.add(fuente);
                    log.debug("Fuente '{}' agregada a la colección", fuenteId);
                } else {
                    log.warn("Fuente con identificador '{}' no encontrada, se omitirá", fuenteId);
                }
            }
        }
        
        // Crear la colección (el ID se genera automáticamente al persistir)
        Coleccion coleccion = new Coleccion(dto.getTitulo(), dto.getDescripcion(), null, 
                            fuentesColeccion, algoritmo);
        
        // Asignar el administrador (OBLIGATORIO)
        coleccion.setAdministrador(administrador);
        
        // Generar identificador público único (handle)
        String identificadorPublico = generarIdentificadorPublico(dto.getTitulo());
        coleccion.setIdentificadorPublico(identificadorPublico);
        
        // Timestamp de creación/modificación para que el agregador detecte cambios
        coleccion.setFechaUltimaModificacion(java.time.LocalDateTime.now());
                            
        // Agregar criterios si existen
        if (dto.getCriterios() != null && !dto.getCriterios().isEmpty()) {
            criterioService.agregarCriteriosDesdeDTO(coleccion, dto.getCriterios());
        }
        
        // Agregar fuentes como entidades de relación
        if (!fuentesColeccion.isEmpty()) {
            for (FuenteDeDatos fuente : fuentesColeccion) {
                ColeccionFuente cf = new ColeccionFuente(
                    coleccion, 
                    fuente.getIdentificador(), 
                    fuente.getTipo()
                );
                coleccion.getFuentesPersistentes().add(cf);
                log.debug("Fuente '{}' agregada como relación persistente", fuente.getIdentificador());
            }
        }
        
        // Persistir en base de datos
        Coleccion coleccionGuardada = coleccionRepository.save(coleccion);
        log.info("Colección '{}' persistida en BD con ID {} y handle '{}'", 
                dto.getTitulo(), coleccionGuardada.getIdentificador(), identificadorPublico);
        
        log.info("Colección '{}' creada con {} fuentes y {} criterios. El agregador la detectará automáticamente.", 
                dto.getTitulo(), fuentesColeccion.size(), 
                dto.getCriterios() != null ? dto.getCriterios().size() : 0);
        
        return coleccionGuardada;
    }
    
    private String generarIdentificadorPublico(String titulo) {
        // Convertir título a handle: minúsculas, sin espacios, sin acentos
        String handle = titulo.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        
        // Agregar timestamp para garantizar unicidad
        return handle + "-" + System.currentTimeMillis();
    }
    
    private AlgoritmoDeConsenso obtenerAlgoritmo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new PorDefecto();
        }
        
        switch (nombre.toLowerCase()) {
            case "por_defecto":
                return new PorDefecto();
            case "mayoria_simple":
                return new MayoriaSimple();
            case "multiples_menciones":
                return new MultiplesMenciones();
            case "absoluta":
                return new Absoluta();
            default:
                return new PorDefecto();
        }
    }
    
    /**
     * Obtiene todas las colecciones visibles (no ocultas)
     */
    public List<Coleccion> obtenerTodasLasColecciones() {
        // Obtener todas y filtrar las que no están ocultas
        return coleccionRepository.findAll().stream()
                .filter(c -> !c.isOculta())
                .collect(java.util.stream.Collectors.toList());
    }
    
    public Coleccion obtenerColeccion(Long identificador) {
        return coleccionRepository.findById(identificador)
                .filter(c -> !c.isOculta()) // Filtrar ocultas
                .orElse(null);
    }
    
    /**
     * Obtiene los hechos de una colección desde BD con filtros de navegación
     * NOTA: Lee directamente desde la BD, no desde el agregador
     */
    public List<Hecho> obtenerHechosDeColeccion(Long identificadorColeccion, MetodoDeNavegacion modo) {
        Coleccion coleccion = obtenerColeccion(identificadorColeccion);
        if (coleccion == null) {
            return new ArrayList<>();
        }
        
        // Incrementar contador de visitas
        incrementarVisitas(identificadorColeccion);
        
        // Obtener hechos desde las relaciones persistentes
        return coleccion.getHechosPersistentes().stream()
                .map(ch -> {
                    Hecho h = ch.getHecho();
                    // Aplicar filtro según modo de navegación
                    switch (modo) {
                        case CURADA:
                            return Boolean.TRUE.equals(ch.getConsensuado()) ? h : null;
                        case IRRESTRICTA:
                        default:
                            return h;
                    }
                })
                .filter(h -> h != null && !h.isEliminado()) // Filtrar nulos y eliminados
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Incrementa el contador de visitas de una colección
     */
    @Transactional
    public void incrementarVisitas(Long identificadorColeccion) {
        Coleccion coleccion = coleccionRepository.findById(identificadorColeccion)
                .orElse(null);
        if (coleccion != null) {
            coleccion.setContadorVisitas(coleccion.getContadorVisitas() + 1);
            coleccionRepository.save(coleccion);
        }
    }
    
    /**
     * Soft delete: marca la colección como oculta en lugar de eliminarla físicamente
     */
    @Transactional
    public void eliminarColeccion(Long identificador) {
        Coleccion coleccion = coleccionRepository.findById(identificador)
                .orElseThrow(() -> new IllegalArgumentException("Colección no encontrada: " + identificador));
        
        coleccion.setOculta(true);
        coleccion.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        coleccionRepository.save(coleccion);
        
        log.info("Colección {} marcada como oculta (soft delete). El agregador dejará de procesarla.", identificador);
    }
    
    /**
     * Modifica una colección existente (fuentes y/o algoritmo de consenso)
     * IMPORTANTE: Los cambios triggean una actualización de hechos por el agregador
     */
    @Transactional
    public Coleccion modificarColeccion(Long identificador, ModificarColeccionDTO dto) {
        Coleccion coleccion = coleccionRepository.findById(identificador)
                .orElseThrow(() -> new IllegalArgumentException("Colección no encontrada: " + identificador));
        
        boolean cambiaronFuentes = false;
        boolean cambioAlgoritmo = false;
        
        // 1️⃣ Modificar algoritmo de consenso si se proporciona
        if (dto.getAlgoritmoConsenso() != null) {
            AlgoritmoDeConsenso nuevoAlgoritmo = obtenerAlgoritmo(dto.getAlgoritmoConsenso());
            if (!coleccion.getAlgoritmoDeConsenso().getClass().equals(nuevoAlgoritmo.getClass())) {
                coleccion.setAlgoritmoDeConsenso(nuevoAlgoritmo);
                cambioAlgoritmo = true;
                log.info("Algoritmo de consenso cambiado a: {}", dto.getAlgoritmoConsenso());
            }
        }
        
        // 2️⃣ Modificar fuentes si se proporciona la lista
        if (dto.getFuenteIds() != null) {
            // Limpiar fuentes persistentes actuales
            coleccion.getFuentesPersistentes().clear();
            coleccion.getFuentes().clear();
            
            // Agregar las nuevas fuentes
            for (String fuenteId : dto.getFuenteIds()) {
                FuenteDeDatos fuente = fuenteService.obtenerFuentePorId(fuenteId);
                if (fuente != null) {
                    // Agregar a lista transitoria
                    coleccion.getFuentes().add(fuente);
                    
                    // Agregar como entidad persistente
                    ColeccionFuente cf = new ColeccionFuente(
                        coleccion, 
                        fuente.getIdentificador(), 
                        fuente.getTipo()
                    );
                    coleccion.getFuentesPersistentes().add(cf);
                    log.debug("Fuente '{}' agregada a colección {}", fuenteId, identificador);
                } else {
                    log.warn("Fuente '{}' no encontrada, se omitirá", fuenteId);
                }
            }
            
            cambiaronFuentes = true;
            log.info("Fuentes actualizadas para colección {}: {} fuentes", 
                    identificador, dto.getFuenteIds().size());
        }
        
        // 3️⃣ Marcar timestamp de modificación para que el agregador detecte cambios
        coleccion.setFechaUltimaModificacion(java.time.LocalDateTime.now());
        
        // 4️⃣ Persistir cambios
        Coleccion coleccionGuardada = coleccionRepository.save(coleccion);
        
        // 5️⃣ Logging de qué se actualizará automáticamente
        if (cambiaronFuentes || cambioAlgoritmo) {
            log.info("Colección {} modificada (cambiaronFuentes={}, cambioAlgoritmo={}). El agregador actualizará hechos automáticamente.", 
                    identificador, cambiaronFuentes, cambioAlgoritmo);
        }
        
        return coleccionGuardada;
    }
}
