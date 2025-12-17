package com.metamapa.service;

import com.metamapa.dto.CrearColeccionDTO;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.MetodoDeNavegacion;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.colecciones.ColeccionFuente;
import com.metamapa.entities.colecciones.ColeccionHecho;
import com.metamapa.repository.ColeccionRepository;
import com.metamapa.repository.HechoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.metamapa.entities.colecciones.AlgoritmoDeConsenso;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.colecciones.Absoluta;
import com.metamapa.entities.colecciones.MayoriaSimple;
import com.metamapa.entities.colecciones.MultiplesMenciones;
import com.metamapa.entities.colecciones.PorDefecto;
import com.metamapa.entities.hechos.Hecho;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioAgregacion {
    
    private final Map<Long, Coleccion> colecciones = new HashMap<>();
    private final Map<String, FuenteDeDatos> fuentes = new HashMap<>();
    private final ColeccionRepository coleccionRepository;
    private final HechoRepository hechoRepository;
    
    public void registrarFuente(FuenteDeDatos fuente) {
        fuentes.put(fuente.getIdentificador(), fuente);
        log.info("Fuente registrada: {} - {}", fuente.getIdentificador(), fuente.getTipo());
    }
    
    /**
     * Registra una colección en el mapa en memoria del agregador.
     * NOTA: La creación de colecciones es responsabilidad de gestor-solicitudes.
     * El agregador solo lee y actualiza colecciones existentes.
     */
    public void registrarColeccion(Coleccion coleccion) {
        colecciones.put(coleccion.getIdentificador(), coleccion);
        log.info("Colección registrada en memoria: {}", coleccion.getIdentificador());
    }
    
    private AlgoritmoDeConsenso obtenerAlgoritmo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new PorDefecto(); // Por defecto: acepta todos los hechos
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
                return new PorDefecto(); // Si no reconoce el nombre, usar por defecto
        }
    }
    
    public AlgoritmoDeConsenso crearAlgoritmo(String nombre) {
        return obtenerAlgoritmo(nombre);
    }

    /**
     * Actualiza los hechos de una colección ejecutando:
     * 1. Carga de fuentes desde entidades persistentes
     * 2. Filtrado de hechos por criterios
     * 3. Ejecución del algoritmo de consenso
     * 4. Persistencia de resultados
     */
    @Transactional
    public void actualizarHechosEnColeccion(Long identificadorColeccion) {
        // Cargar colección con JOIN FETCH para evitar LazyInitializationException
        Coleccion coleccion = coleccionRepository.findByIdWithFuentes(identificadorColeccion)
                .orElse(colecciones.get(identificadorColeccion));
        
        if (coleccion != null) {
            log.debug("Actualizando hechos para colección {}", identificadorColeccion);
            
            // Obtener hechos desde BD en lugar de llamar a fuentes externas
            List<Hecho> hechosDesdeDB = obtenerHechosDesdeBD(coleccion);
            log.info("Obtenidos {} hechos desde BD para colección {}", 
                    hechosDesdeDB.size(), identificadorColeccion);
            
            // Actualizar hechos de la colección con los de BD
            coleccion.actualizarHechosDesdeListaPersistida(hechosDesdeDB);
            
            log.debug("Ejecutando algoritmo de consenso para colección {}", identificadorColeccion);
            coleccion.ejecutarAlgoritmoDeConsenso();
            
            // Sincronizar hechos del Map transitorio a entidades persistentes
            sincronizarHechosPersistentes(coleccion);
            
            // Persistir cambios en BD
            coleccionRepository.save(coleccion);
            
            // También actualizar el mapa en memoria para compatibilidad
            colecciones.put(identificadorColeccion, coleccion);
            
            log.info("Hechos actualizados en colección {}: {} hechos totales, {} consensuados", 
                    identificadorColeccion, coleccion.verHechos().size(), 
                    coleccion.cantidadHechosConsensuados());
        } else {
            log.warn("No se encontró la colección {} para actualizar", identificadorColeccion);
        }
    }
    
    /**
     * Reemplaza los hechos transitorios (de las fuentes) con las versiones persistidas en BD
     * Usa el fingerprint para encontrar el hecho correspondiente en la BD
     */
    private void reemplazarHechosConVersionesPersistidas(Coleccion coleccion) {
        Map<Hecho, Boolean> hechosActualizados = new HashMap<>();
        
        for (Map.Entry<Hecho, Boolean> entry : coleccion.getHechos().entrySet()) {
            Hecho hechoTransitorio = entry.getKey();
            Boolean consensuado = entry.getValue();
            
            // Calcular el fingerprint si no lo tiene (hechos transitorios de fuentes)
            String fingerprint = hechoTransitorio.getFingerprint();
            if (fingerprint == null) {
                fingerprint = buildFingerprint(hechoTransitorio);
                hechoTransitorio.setFingerprint(fingerprint);
            }
            
            // Buscar el hecho persistido por fingerprint
            Optional<Hecho> hechoPersistido = hechoRepository.findByFingerprint(fingerprint);
            if (hechoPersistido.isPresent()) {
                // Reemplazar con la versión persistida (que tiene ID)
                hechosActualizados.put(hechoPersistido.get(), consensuado);
            } else {
                log.warn("Hecho con fingerprint '{}' no encontrado en BD: '{}'", 
                    fingerprint, hechoTransitorio.getTitulo());
            }
        }
        
        // Reemplazar el Map completo
        coleccion.getHechos().clear();
        coleccion.getHechos().putAll(hechosActualizados);
        
        log.info("Reemplazados {} hechos transitorios con versiones persistidas", hechosActualizados.size());
    }
    
    /**
     * Construye el fingerprint de un hecho (mismo formato que DepuracionService)
     */
    private String buildFingerprint(Hecho h) {
        String titulo = h.getTitulo() != null ? h.getTitulo() : "";
        String categoria = h.getCategoria() != null ? h.getCategoria() : "";
        return titulo + "::" + categoria;
    }
    
    /**
     * Obtiene los hechos desde BD filtrando por las fuentes de la colección
     * IMPORTANTE: Solo incluye hechos que pertenecen a AL MENOS UNA de las fuentes seleccionadas
     */
    private List<Hecho> obtenerHechosDesdeBD(Coleccion coleccion) {
        Set<String> fuentesSeleccionadas = coleccion.getFuentesPersistentes().stream()
                .map(ColeccionFuente::getIdentificadorFuente)
                .collect(java.util.stream.Collectors.toSet());
        
        if (fuentesSeleccionadas.isEmpty()) {
            log.warn("Colección {} no tiene fuentes seleccionadas", coleccion.getIdentificador());
            return new ArrayList<>();
        }
        
        Set<Hecho> hechosUnicos = new HashSet<>();
        
        for (String identificadorFuente : fuentesSeleccionadas) {
            List<Hecho> hechosDeFuente = hechoRepository.findByFuente(identificadorFuente);
            hechosUnicos.addAll(hechosDeFuente);
            log.debug("Encontrados {} hechos de fuente '{}' en BD", 
                    hechosDeFuente.size(), identificadorFuente);
        }
        
        log.info("Total de {} hechos únicos obtenidos de {} fuentes para colección {}", 
                hechosUnicos.size(), fuentesSeleccionadas.size(), coleccion.getIdentificador());
        
        return new ArrayList<>(hechosUnicos);
    }


    /**
     * Reconstruye la lista transitoria de fuentes desde las entidades persistentes
     */
    private void cargarFuentesTransitorias(Coleccion coleccion) {
        coleccion.getFuentes().clear();
        
        for (ColeccionFuente cf : coleccion.getFuentesPersistentes()) {
            String identificadorFuente = cf.getIdentificadorFuente();
            FuenteDeDatos fuente = fuentes.get(identificadorFuente);
            
            if (fuente != null) {
                coleccion.getFuentes().add(fuente);
                log.debug("Fuente '{}' cargada para colección {}", identificadorFuente, coleccion.getIdentificador());
            } else {
                log.warn("Fuente '{}' no encontrada en el registro de fuentes activas", identificadorFuente);
            }
        }
        
        log.debug("Cargadas {} fuentes para colección {}", 
                coleccion.getFuentes().size(), coleccion.getIdentificador());
    }
    
    /**
     * Sincroniza los hechos del Map transitorio con las entidades persistentes ColeccionHecho
     * IMPORTANTE: Recarga los hechos desde BD para evitar TransientPropertyValueException
     */
    private void sincronizarHechosPersistentes(Coleccion coleccion) {
        // Limpiar lista de hechos persistentes
        coleccion.getHechosPersistentes().clear();
        
        // Agregar todos los hechos del Map como entidades ColeccionHecho
        for (Map.Entry<Hecho, Boolean> entry : coleccion.getHechos().entrySet()) {
            Hecho hechoTransitorio = entry.getKey();
            Boolean consensuado = entry.getValue();
            
            // Validar que el hecho tenga ID (está persistido en BD)
            if (hechoTransitorio.getId() == null) {
                log.warn("Hecho sin ID encontrado (no persistido): '{}', se omite de la colección {}", 
                        hechoTransitorio.getTitulo(), coleccion.getIdentificador());
                continue;
            }
            
            // Recargar el hecho desde la BD para que esté en el contexto de persistencia
            Hecho hechoPersistente = hechoRepository.findById(hechoTransitorio.getId())
                    .orElse(null);
            
            if (hechoPersistente != null) {
                ColeccionHecho ch = new ColeccionHecho(coleccion, hechoPersistente, consensuado);
                coleccion.getHechosPersistentes().add(ch);
            } else {
                log.warn("Hecho con ID {} no encontrado en BD, se omite de la colección {}", 
                        hechoTransitorio.getId(), coleccion.getIdentificador());
            }
        }
        
        log.debug("Sincronizados {} hechos a entidades persistentes para colección {}", 
                coleccion.getHechosPersistentes().size(), coleccion.getIdentificador());
    }
    
    public List<Hecho> obtenerHechosDeColeccion(Long identificadorColeccion, MetodoDeNavegacion modo) {
        Coleccion coleccion = colecciones.get(identificadorColeccion);
        if (coleccion != null) {
            return coleccion.obtenerHechosSegunModo(modo);
        }
        return new ArrayList<>();
    }
    
    public List<Hecho> filtrarHechosPorCriterios(Long identificadorColeccion, String categoria, 
                                                 String titulo, boolean soloRecientes) {
        Coleccion coleccion = colecciones.get(identificadorColeccion);
        if (coleccion == null) {
            return new ArrayList<>();
        }
        
        List<Hecho> hechos = coleccion.verHechos();
        
        if (categoria != null && !categoria.isEmpty()) {
            hechos = hechos.stream()
                    .filter(h -> h.getCategoria() != null && h.getCategoria().equalsIgnoreCase(categoria))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        if (titulo != null && !titulo.isEmpty()) {
            hechos = hechos.stream()
                    .filter(h -> h.getTitulo() != null && h.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        if (soloRecientes) {
            hechos = hechos.stream()
                    .filter(Hecho::esReciente)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        return hechos;
    }
    
    public List<Coleccion> obtenerTodasLasColecciones() {
        return new ArrayList<>(colecciones.values());
    }
    
    /**
     * Obtiene todas las colecciones con los hechos e inicializa las etiquetas
     * cargándolas desde la BD con JOIN FETCH para evitar LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<Coleccion> obtenerTodasLasColeccionesConEtiquetas() {
        List<Coleccion> todasColecciones = new ArrayList<>(colecciones.values());
        
        // Para cada colección, cargar los hechos con etiquetas desde la BD
        for (Coleccion coleccion : todasColecciones) {
            List<Hecho> hechos = coleccion.verHechos();
            if (!hechos.isEmpty()) {
                // Obtener los IDs de los hechos
                List<Long> hechoIds = hechos.stream()
                        .map(Hecho::getId)
                        .filter(id -> id != null)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                
                if (!hechoIds.isEmpty()) {
                    // Cargar hechos con etiquetas desde la BD
                    List<Hecho> hechosConEtiquetas = hechoRepository.findByIdsWithEtiquetas(hechoIds);
                    
                    // Crear un mapa para acceso rápido
                    Map<Long, Hecho> hechoMap = new HashMap<>();
                    for (Hecho h : hechosConEtiquetas) {
                        hechoMap.put(h.getId(), h);
                    }
                    
                    // Reemplazar los hechos en la colección con los que tienen etiquetas cargadas
                    for (int i = 0; i < hechos.size(); i++) {
                        Hecho hechoOriginal = hechos.get(i);
                        if (hechoOriginal.getId() != null && hechoMap.containsKey(hechoOriginal.getId())) {
                            hechos.set(i, hechoMap.get(hechoOriginal.getId()));
                        }
                    }
                }
            }
        }
        
        return todasColecciones;
    }
    
    public Coleccion obtenerColeccion(Long identificador) {
        return colecciones.get(identificador);
    }
    
    public void eliminarColeccion(Long identificador) {
        colecciones.remove(identificador);
        log.info("Colección eliminada: {}", identificador);
    }
    
    public List<FuenteDeDatos> obtenerTodasLasFuentes() {
        return new ArrayList<>(fuentes.values());
    }
    
    public FuenteDeDatos obtenerFuente(String identificador) {
        return fuentes.get(identificador);
    }
    
    public boolean agregarFuenteAColeccion(Long identificadorColeccion, String idFuente) {
        Coleccion coleccion = colecciones.get(identificadorColeccion);
        FuenteDeDatos fuente = fuentes.get(idFuente);
        if (coleccion != null && fuente != null) {
            List<FuenteDeDatos> fuentesColeccion = coleccion.getFuentes();
            if (!fuentesColeccion.contains(fuente)) {
                fuentesColeccion.add(fuente);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Actualiza todas las colecciones registradas
     * MEJORA: Detecta colecciones nuevas o modificadas desde la BD
     */
    public void actualizarTodasLasColecciones() {
        // Obtener todas las colecciones visibles (no ocultas) desde BD
        List<Coleccion> coleccionesEnBD = coleccionRepository.findAll().stream()
                .filter(c -> !c.isOculta())
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Iniciando actualización de {} colecciones desde BD", coleccionesEnBD.size());
        
        // Actualizar el mapa en memoria con las colecciones de BD
        colecciones.clear();
        for (Coleccion col : coleccionesEnBD) {
            colecciones.put(col.getIdentificador(), col);
        }
        
        // Actualizar hechos de cada colección
        coleccionesEnBD.forEach(col -> {
            try {
                log.info("Actualizando colección {} (última modificación: {})", 
                        col.getIdentificador(), col.getFechaUltimaModificacion());
                actualizarHechosEnColeccion(col.getIdentificador());
            } catch (Exception e) {
                log.error("Error al actualizar colección {}: {}", col.getIdentificador(), e.getMessage());
            }
        });
        
        log.info("Actualización de todas las colecciones completada");
    }
} 