package com.metamapa.api.interna;

import com.metamapa.client.FuenteDinamicaCrudClient;
import com.metamapa.dto.ActualizarHechoDTO;
import com.metamapa.dto.ActualizarPerfilDTO;
import com.metamapa.dto.ColeccionResponseDTO;
import com.metamapa.dto.ContribuyenteDTO;
import com.metamapa.dto.CrearSolicitudDTO;
import com.metamapa.dto.ReportarHechoDTO;
import com.metamapa.entities.MetodoDeNavegacion;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import com.metamapa.mapper.ColeccionMapper;
import com.metamapa.repository.HechoRepository;
import com.metamapa.security.RoleConstants;
import com.metamapa.service.ColeccionService;
import com.metamapa.service.SolicitudService;
import com.metamapa.service.FuenteService;
import com.metamapa.service.ContribuyenteService;
import com.metamapa.service.HechoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interna")
@RequiredArgsConstructor
@Slf4j
public class APIPublicaInternaController {
    
    private final ColeccionService coleccionService;
    private final SolicitudService solicitudService;
    private final FuenteDinamicaCrudClient fuenteDinamicaCrudClient;
    private final ColeccionMapper coleccionMapper;
    private final FuenteService fuenteService;
    private final ContribuyenteService contribuyenteService;
    private final HechoRepository hechoRepository;
    private final HechoService hechoService;

    // Obtener todas las colecciones disponibles
    @GetMapping("/colecciones")
    public ResponseEntity<List<ColeccionResponseDTO>> obtenerTodasLasColecciones(
            @RequestParam(required = false, defaultValue = "fecha") String orderBy) {
        List<Coleccion> colecciones = coleccionService.obtenerTodasLasColecciones();
        
        // Ordenar según el parámetro
        if ("visitas".equalsIgnoreCase(orderBy)) {
            // Ordenar por cantidad de visitas (descendente)
            colecciones.sort((c1, c2) -> Integer.compare(
                c2.getContadorVisitas(), 
                c1.getContadorVisitas()));
        } else if ("hechos".equalsIgnoreCase(orderBy)) {
            // Ordenar por cantidad de hechos (descendente)
            colecciones.sort((c1, c2) -> Integer.compare(
                c2.getHechosPersistentes().size(), 
                c1.getHechosPersistentes().size()));
        } else { // "fecha" por defecto
            // Ordenar por fecha de última modificación (más recientes primero)
            colecciones.sort((c1, c2) -> {
                if (c1.getFechaUltimaModificacion() == null && c2.getFechaUltimaModificacion() == null) return 0;
                if (c1.getFechaUltimaModificacion() == null) return 1;
                if (c2.getFechaUltimaModificacion() == null) return -1;
                return c2.getFechaUltimaModificacion().compareTo(c1.getFechaUltimaModificacion());
            });
        }
        
        List<ColeccionResponseDTO> coleccionesDTO = colecciones.stream()
                .map(coleccionMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(coleccionesDTO);
    }
    
    // Obtener hechos de una colección con filtros opcionales
    @GetMapping("/colecciones/{id}/hechos")
    public ResponseEntity<List<Hecho>> obtenerHechosDeColeccion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "IRRESTRICTA") MetodoDeNavegacion modo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String ubicacion) {
        
        List<Hecho> hechos = coleccionService.obtenerHechosDeColeccion(id, modo);
        
        // Aplicar filtros adicionales
        if (categoria != null && !categoria.isEmpty()) {
            hechos = hechos.stream()
                    .filter(h -> h.getCategoria() != null && h.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
        }
        
        // Filtrar por rango de fechas (fecha de hecho)
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            try {
                java.time.LocalDateTime desde = java.time.LocalDateTime.parse(fechaDesde + "T00:00:00");
                hechos = hechos.stream()
                        .filter(h -> h.getFechaHecho() != null && !h.getFechaHecho().isBefore(desde))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Formato de fecha inválido para fechaDesde: {}", fechaDesde);
            }
        }
        
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            try {
                java.time.LocalDateTime hasta = java.time.LocalDateTime.parse(fechaHasta + "T23:59:59");
                hechos = hechos.stream()
                        .filter(h -> h.getFechaHecho() != null && !h.getFechaHecho().isAfter(hasta))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Formato de fecha inválido para fechaHasta: {}", fechaHasta);
            }
        }
        
        // Filtrar por ubicación (busca en país, provincia Y municipio)
        if (ubicacion != null && !ubicacion.isEmpty()) {
            String ubicacionLower = ubicacion.toLowerCase();
            hechos = hechos.stream()
                    .filter(h -> h.getUbicacion() != null && 
                            h.getUbicacion().getLugar() != null && (
                                (h.getUbicacion().getLugar().getPais() != null && 
                                 h.getUbicacion().getLugar().getPais().toLowerCase().contains(ubicacionLower)) ||
                                (h.getUbicacion().getLugar().getProvincia() != null && 
                                 h.getUbicacion().getLugar().getProvincia().toLowerCase().contains(ubicacionLower)) ||
                                (h.getUbicacion().getLugar().getMunicipio() != null && 
                                 h.getUbicacion().getLugar().getMunicipio().toLowerCase().contains(ubicacionLower))
                            ))
                    .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(hechos);
    }

    // Generar una solicitud de eliminación a un hecho (JSON)
    // @PreAuthorize("hasRole('ADMINISTRADOR') || hasRole('CONTRIBUYENTE') and hasAnyAuthority('HECHO_SOLICITUD_ELIMINACION_GENERAR')")
    @PostMapping(value = "/hechos/{idHecho}/solicitudes", consumes = "application/json")
    public ResponseEntity<?> crearSolicitudEliminacion(
            @PathVariable Long idHecho,
            @RequestBody CrearSolicitudDTO solicitudDTO,
            @RequestHeader(value = "X-Contribuyente-Id", required = false) Long contribuyenteId) {
        
        try {
            // Buscar el hecho en la base de datos
            Optional<Hecho> hechoOpt = hechoRepository.findById(idHecho);
            if (!hechoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Hecho hecho = hechoOpt.get();
            
            // Validar justificación
            if (solicitudDTO.getJustificacion() == null || solicitudDTO.getJustificacion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La justificación es requerida");
            }
            
            // Obtener contribuyente si se proporciona el ID
            Contribuyente contribuyente = null;
            if (contribuyenteId != null) {
                contribuyente = contribuyenteService.obtenerContribuyentePorId(contribuyenteId);
                if (contribuyente == null) {
                    return ResponseEntity.badRequest()
                            .body("Contribuyente no encontrado con ID: " + contribuyenteId);
                }
            }
            
            SolicitudEliminacion solicitud = solicitudService.crearSolicitudEliminacion(
                    hecho, 
                    solicitudDTO.getJustificacion(), 
                    contribuyente);
            return ResponseEntity.ok(solicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear la solicitud: " + e.getMessage());
        }
    }
    
    // Obtener todos los hechos disponibles (no eliminados) para mostrar en mapa
    @GetMapping("/hechos")
    public ResponseEntity<List<Hecho>> obtenerTodosLosHechos(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String ubicacion) {
        
        try {
            log.info("Consultando hechos con filtros -> Categoria: {}, Desde: {}, Hasta: {}, Ubicacion: {}",
                    categoria, desde, hasta, ubicacion);
            // Obtener hechos AGREGADOS desde la base de datos MySQL (no desde las fuentes)
            List<Hecho> todosLosHechos = hechoRepository.findAll();
            
            // Aplicar filtros
            List<Hecho> hechosFiltrados = todosLosHechos.stream()
                .filter(hecho -> !hecho.isEliminado()) // Excluir hechos eliminados
                .collect(Collectors.toList());
            
            // Filtrar por categoría
            if (categoria != null && !categoria.isEmpty()) {
                hechosFiltrados = hechosFiltrados.stream()
                    .filter(h -> h.getCategoria() != null && h.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
            }
            
            // Filtrar por rango de fechas (fecha de hecho)
            if (desde != null && !desde.isEmpty()) {
                try {
                    // El front manda YYYY-MM-DD, lo convertimos al inicio del día
                    java.time.LocalDateTime fechaInicio = java.time.LocalDate.parse(desde).atStartOfDay();
                    hechosFiltrados = hechosFiltrados.stream()
                            .filter(h -> h.getFechaHecho() != null && !h.getFechaHecho().isBefore(fechaInicio))
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.warn("Formato de fecha inválido para 'desde': {}", desde);
                }
            }

            if (hasta != null && !hasta.isEmpty()) {
                try {
                    // El front manda YYYY-MM-DD, lo convertimos al final del día (23:59:59)
                    java.time.LocalDateTime fechaFin = java.time.LocalDate.parse(hasta).atTime(23, 59, 59);
                    hechosFiltrados = hechosFiltrados.stream()
                            .filter(h -> h.getFechaHecho() != null && !h.getFechaHecho().isAfter(fechaFin))
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.warn("Formato de fecha inválido para 'hasta': {}", hasta);
                }
            }
            
            // Filtrar por ubicación (busca en país, provincia Y municipio)
            if (ubicacion != null && !ubicacion.isEmpty()) {
                String busqueda = ubicacion.toLowerCase();
                hechosFiltrados = hechosFiltrados.stream()
                        .filter(h -> {
                            // Buscar en Título
                            boolean coincideTitulo = h.getTitulo() != null && h.getTitulo().toLowerCase().contains(busqueda);

                            // Buscar en Ubicación (País, Provincia, Municipio)
                            boolean coincideLugar = h.getUbicacion() != null && h.getUbicacion().getLugar() != null && (
                                    (h.getUbicacion().getLugar().getPais() != null && h.getUbicacion().getLugar().getPais().toLowerCase().contains(busqueda)) ||
                                            (h.getUbicacion().getLugar().getProvincia() != null && h.getUbicacion().getLugar().getProvincia().toLowerCase().contains(busqueda)) ||
                                            (h.getUbicacion().getLugar().getMunicipio() != null && h.getUbicacion().getLugar().getMunicipio().toLowerCase().contains(busqueda))
                            );

                            return coincideTitulo || coincideLugar;
                        })
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(hechosFiltrados);

        } catch (Exception e) {
            log.error("Error al obtener hechos", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Obtener un hecho específico por ID
    @GetMapping("/hechos/{id}")
    public ResponseEntity<Hecho> obtenerHechoPorId(@PathVariable Long id) {
        try {
            // Buscar el hecho directamente en la base de datos central
            Optional<Hecho> hechoOpt = hechoRepository.findById(id);
            
            if (hechoOpt.isPresent() && !hechoOpt.get().isEliminado()) {
                return ResponseEntity.ok(hechoOpt.get());
            }
            
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Reportar un hecho (crear hecho en fuente dinámica) - JSON (sin archivos)
    // Solo administradores y contribuyentes pueden crear hechos
    @PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
    @PostMapping(value = "/hechos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reportarHecho(
            @RequestBody ReportarHechoDTO hechoDTO,
            @RequestHeader(value = "X-Contribuyente-Id", required = true) Long contribuyenteId) {
        
        try {
            // Validar datos básicos del formulario
            if (hechoDTO.getTitulo() == null || hechoDTO.getTitulo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El título es requerido");
            }
            if (hechoDTO.getDescripcion() == null || hechoDTO.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción es requerida");
            }
            if (hechoDTO.getCategoria() == null || hechoDTO.getCategoria().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La categoría es requerida");
            }
            if (hechoDTO.getFechaHecho() == null) {
                return ResponseEntity.badRequest().body("La fecha del hecho es requerida");
            }
            // Validar coordenadas (rango válido: latitud -90 a 90, longitud -180 a 180)
            if (hechoDTO.getLatitud() < -90 || hechoDTO.getLatitud() > 90 ||
                hechoDTO.getLongitud() < -180 || hechoDTO.getLongitud() > 180) {
                return ResponseEntity.badRequest().body("Las coordenadas de ubicación son inválidas");
            }
            
            // Validar que el contribuyente existe (solo admin y contribuyentes)
            Contribuyente contribuyente = contribuyenteService.obtenerContribuyentePorId(contribuyenteId);
            if (contribuyente == null) {
                return ResponseEntity.badRequest()
                        .body("Contribuyente no encontrado con ID: " + contribuyenteId);
            }
            
            // Enviar el hecho SOLO a la fuente dinámica (NO a la base central)
            // El hecho quedará pendiente de aprobación por administradores
            Hecho hechoCreado = fuenteDinamicaCrudClient.crearHecho(hechoDTO, contribuyente, null);

            if (hechoCreado != null) {
                return ResponseEntity.ok(hechoCreado);
            } else {
                return ResponseEntity.internalServerError()
                        .body("Error al crear el hecho en la fuente dinámica");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    // Reportar un hecho con archivos multimedia (form-data)
    // Solo administradores y contribuyentes pueden crear hechos
    // Los archivos son opcionales (pueden ser múltiples o ninguno)
    @PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
    @PostMapping(value = "/hechos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> reportarHechoConArchivos(
            @RequestHeader(value = "X-Contribuyente-Id", required = true) Long contribuyenteId,
            @RequestPart("hecho") String hechoJson,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos) {

        try {
            // Parsear el JSON del hecho
            ObjectMapper mapper = new ObjectMapper();
            ReportarHechoDTO hechoDTO = mapper.readValue(hechoJson, ReportarHechoDTO.class);

            // Validar datos básicos del formulario
            if (hechoDTO.getTitulo() == null || hechoDTO.getTitulo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El título es requerido");
            }
            if (hechoDTO.getDescripcion() == null || hechoDTO.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción es requerida");
            }
            if (hechoDTO.getCategoria() == null || hechoDTO.getCategoria().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La categoría es requerida");
            }
            if (hechoDTO.getFechaHecho() == null) {
                return ResponseEntity.badRequest().body("La fecha del hecho es requerida");
            }
            // Validar coordenadas (rango válido: latitud -90 a 90, longitud -180 a 180)
            if (hechoDTO.getLatitud() < -90 || hechoDTO.getLatitud() > 90 ||
                hechoDTO.getLongitud() < -180 || hechoDTO.getLongitud() > 180) {
                return ResponseEntity.badRequest().body("Las coordenadas de ubicación son inválidas");
            }

            // Validar que el contribuyente existe (solo admin y contribuyentes)
            Contribuyente contribuyente = contribuyenteService.obtenerContribuyentePorId(contribuyenteId);
            if (contribuyente == null) {
                return ResponseEntity.badRequest()
                        .body("Contribuyente no encontrado con ID: " + contribuyenteId);
            }

            // Enviar el hecho con archivos a la fuente dinámica
            Hecho hechoCreado = fuenteDinamicaCrudClient.crearHecho(hechoDTO, contribuyente, archivos);
            
            if (hechoCreado != null) {
                return ResponseEntity.ok(hechoCreado);
            } else {
                return ResponseEntity.internalServerError()
                        .body("Error al crear el hecho en la fuente dinámica");
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    // NUEVO: búsqueda por texto libre con FULLTEXT (MySQL)
    @GetMapping("/hechos/search")
    public ResponseEntity<?> buscarHechosPorTextoLibre(
            @RequestParam(name = "q") String query,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Parámetro 'q' requerido.");
            }
            // Ejemplos soportados: "corte de luz", sismo OR temblor, +basura -recoleccion
            Object resultados = fuenteService.buscarHechosFullText(query.trim(), categoria, page, size);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            e.printStackTrace(); // 👈 así verás el error completo en consola
            return ResponseEntity.internalServerError()
                    .body("Error en búsqueda: " + e.getMessage());        }
    }
    
    // ==================== GESTIÓN DE CONTRIBUYENTES ====================
    
    /**
     * Crear un nuevo contribuyente
     */
    @PostMapping(value = "/contribuyentes", consumes = "application/json")
    public ResponseEntity<Contribuyente> crearContribuyente(@RequestBody ContribuyenteDTO dto) {
        try {
            // Validar datos básicos
            if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Contribuyente contribuyente = contribuyenteService.crearContribuyente(dto);
            return ResponseEntity.ok(contribuyente);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener el perfil del usuario autenticado (contribuyente o administrador)
     * Solo usuarios registrados (CONTRIBUTOR y ADMIN) pueden acceder
     */
    @PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfilUsuario(
            @RequestHeader(value = "X-Contribuyente-Id", required = true) Long contribuyenteId) {
        try {
            Contribuyente contribuyente = contribuyenteService.obtenerContribuyentePorId(contribuyenteId);
            if (contribuyente != null) {
                // Crear respuesta con información del perfil
                java.util.HashMap<String, Object> response = new java.util.HashMap<String, Object>();
                response.put("id", contribuyente.getId());
                response.put("nombre", contribuyente.getNombre());
                response.put("apellido", contribuyente.getApellido());
                response.put("fechaNacimiento", contribuyente.getFechaNacimiento());
                response.put("fechaRegistro", contribuyente.getFechaRegistro());
                response.put("rol", contribuyente.getRol());
                response.put("edad", contribuyente.getEdad());
                response.put("hechosSubidos", contribuyente.obtenerHechosSubidos());
                
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Actualizar el perfil del usuario autenticado
     * Solo usuarios registrados (CONTRIBUTOR y ADMIN) pueden editar su perfil
     */
    @PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfilUsuario(
            @RequestHeader(value = "X-Contribuyente-Id", required = true) Long contribuyenteId,
            @RequestBody ActualizarPerfilDTO dto) {
        try {
            Contribuyente contribuyente = contribuyenteService.actualizarPerfil(
                    contribuyenteId, 
                    dto.getNombre(), 
                    dto.getApellido(), 
                    dto.getFechaNacimiento()
            );
            
            // Crear respuesta con información del perfil actualizado
            java.util.HashMap<String, Object> response = new java.util.HashMap<String, Object>();
            response.put("id", contribuyente.getId());
            response.put("nombre", contribuyente.getNombre());
            response.put("apellido", contribuyente.getApellido());
            response.put("fechaNacimiento", contribuyente.getFechaNacimiento());
            response.put("fechaRegistro", contribuyente.getFechaRegistro());
            response.put("rol", contribuyente.getRol());
            response.put("edad", contribuyente.getEdad());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar perfil: {}", e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Error inesperado al actualizar perfil", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Error interno del servidor\"}");
        }
    }
    
    /**
     * Actualizar un hecho existente
     * Solo CONTRIBUTOR y ADMIN pueden editar hechos
     * Reglas:
     * - CONTRIBUTOR solo puede editar sus propios hechos y dentro de los 7 días de creación
     * - ADMIN puede editar cualquier hecho sin restricción de tiempo
     */
    @PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
    @PutMapping("/hechos/{id}")
    public ResponseEntity<?> actualizarHecho(
            @PathVariable Long id,
            @RequestHeader(value = "X-Contribuyente-Id", required = true) Long contribuyenteId,
            @RequestBody ActualizarHechoDTO dto,
            Authentication authentication) {
        try {
            // Verificar si el usuario es administrador
            boolean esAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_" + RoleConstants.ADMIN));
            
            log.info("Solicitud de actualización de hecho. ID={}, Contribuyente={}, EsAdmin={}", 
                    id, contribuyenteId, esAdmin);
            
            Hecho hechoActualizado = hechoService.actualizarHecho(id, contribuyenteId, dto, esAdmin);
            
            return ResponseEntity.ok(hechoActualizado);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al actualizar hecho {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (SecurityException e) {
            log.error("Error de permisos al actualizar hecho {}: {}", id, e.getMessage());
            return ResponseEntity.status(403).body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Error inesperado al actualizar hecho {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Error interno del servidor\"}");
        }
    }
    
    /**
     * Verificar si un hecho puede ser editado por el usuario actual
     * Endpoint útil para el frontend para mostrar/ocultar botones de edición
     */
    @PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
    @GetMapping("/hechos/{id}/puede-editar")
    public ResponseEntity<?> puedeEditarHecho(
            @PathVariable Long id,
            @RequestHeader(value = "X-Contribuyente-Id", required = true) Long contribuyenteId,
            Authentication authentication) {
        try {
            // Verificar si el usuario es administrador
            boolean esAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_" + RoleConstants.ADMIN));
            
            boolean puedeEditar = hechoService.puedeEditar(id, contribuyenteId, esAdmin);
            
            java.util.HashMap<String, Object> response = new java.util.HashMap<String, Object>();
            response.put("puedeEditar", puedeEditar);
            response.put("esAdmin", esAdmin);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al verificar permisos de edición: {}", e.getMessage());ty.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
