package com.metamapa.api.admin;

import com.metamapa.dto.*;
import com.metamapa.entities.colecciones.AlgoritmoDeConsenso;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.client.FuenteDinamicaCrudClient;
import com.metamapa.repository.HechoRepository;
import com.metamapa.security.RoleConstants;
import com.metamapa.service.ColeccionService;
import com.metamapa.service.SolicitudService;
import com.metamapa.service.ContribuyenteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de la API Administrativa
 * 
 * IMPORTANTE: Todos los endpoints de este controlador requieren rol ADMIN
 * La seguridad se aplica a nivel de clase con @PreAuthorize
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize(RoleConstants.HAS_ROLE_ADMIN) // Proteger toda la API Administrativa
public class APIAdministrativaController {
    
    private final ColeccionService coleccionService;
    private final SolicitudService solicitudService;
    private final FuenteDinamicaCrudClient fuenteDinamicaCrudClient;
    private final HechoRepository hechoRepository;
    private final ContribuyenteService contribuyenteService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${fuente.estatica.url:http://localhost:8083}")
    private String fuenteEstaticaUrl;
    
    // Operaciones CRUD sobre las colecciones
    @PostMapping(value = "/colecciones", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Coleccion> crearColeccion(
            @RequestBody CrearColeccionDTO dto,
            @RequestHeader(value = "X-Admin-Id", required = true) Long adminId,
            UriComponentsBuilder uriBuilder) {
        
        try {
            Coleccion creada = coleccionService.crearColeccion(dto, adminId);
            URI location = uriBuilder.path("/api/admin/colecciones/{identificador}")
                    .buildAndExpand(creada.getIdentificador()).toUri();
            return ResponseEntity.created(location).body(creada);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al crear colección: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado al crear colección", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /*// @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('COLECCION_GESTIONAR')")
    @GetMapping("/colecciones")
    public ResponseEntity<List<Coleccion>> obtenerColecciones() {
        return ResponseEntity.ok(coleccionService.obtenerTodasLasColecciones());
    }*/

    @DeleteMapping("/colecciones/{identificador}")
    public ResponseEntity<Void> eliminarColeccion(@PathVariable Long identificador) {
        coleccionService.eliminarColeccion(identificador);
        return ResponseEntity.ok().build();
    }
    
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('COLECCION_GESTIONAR')")
    public ResponseEntity<?> modificarColeccion(
            @PathVariable Long identificador,
            @RequestBody ModificarColeccionDTO dto) {
        
        try {
            Coleccion coleccion = coleccionService.modificarColeccion(identificador, dto);
            return ResponseEntity.ok(coleccion);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al modificar colección {}: {}", identificador, e.getMessage());
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Error al modificar colección {}: {}", identificador, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // Aprobar o denegar una solicitud de eliminación de un hecho
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_SOLICITUD_ELIMINACION_GESTIONAR')")
    public ResponseEntity<Void> aprobarSolicitud(@PathVariable Long id) {
        try {
            solicitudService.aprobarSolicitud(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_SOLICITUD_ELIMINACION_GESTIONAR')")
    public ResponseEntity<Void> rechazarSolicitud(@PathVariable Long id) {
        try {
            solicitudService.rechazarSolicitud(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_SOLICITUD_ELIMINACION_GESTIONAR')")
    public ResponseEntity<List<SolicitudEliminacion>> obtenerSolicitudes() {
        try {
            return ResponseEntity.ok(solicitudService.obtenerSolicitudesPendientes());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ==================== MODERACIÓN DE HECHOS ====================
    
    // Obtener todos los hechos pendientes de revisión desde fuente dinámica
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_APROBAR_RECHAZAR')")
    public ResponseEntity<List<HechoPendienteDTO>> obtenerHechosPendientes() {
        try {
            List<HechoDinamicoDTO> hechosDinamicos = fuenteDinamicaCrudClient.obtenerHechosPendientesConId();
            
            // Convertir a HechoPendienteDTO
            List<HechoPendienteDTO> pendientes = hechosDinamicos.stream()
                .map(dto -> {
                    HechoPendienteDTO pendiente = new HechoPendienteDTO();
                    pendiente.setMongoId(dto.getId());
                    pendiente.setTitulo(dto.getTitulo());
                    pendiente.setDescripcion(dto.getDescripcion());
                    pendiente.setCategoria(dto.getCategoria());
                    pendiente.setUbicacion(dto.getUbicacion());
                    pendiente.setEtiquetas(dto.getEtiquetas());
                    pendiente.setFechaHecho(dto.getFechaHecho());
                    pendiente.setFechaCarga(dto.getFechaCarga());
                    pendiente.setEstadoRevision(dto.getEstadoRevision());
                    pendiente.setSugerenciaDeCambio(dto.getSugerenciaDeCambio());
                    pendiente.setEsAnonimo(dto.isEsAnonimo());
                    pendiente.setEliminado(dto.isEliminado());
                    return pendiente;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(pendientes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Aprobar un hecho (delega a fuente dinámica, NO mueve a base central)
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_APROBAR_RECHAZAR')")
    @PostMapping("/hechos/{id}/aprobar")
    public ResponseEntity<?> aprobarHecho(
            @PathVariable String id,
            @RequestHeader(value = "X-Admin-Id", required = true) Long adminId) {
        try {
            // Obtener el ID de MongoDB del administrador
            String adminMongoId = fuenteDinamicaCrudClient.obtenerIdMongoDBPorIdAgregador(adminId);
            if (adminMongoId == null) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Administrador no encontrado en MongoDB\"}");
            }
            
            // Delegar la aprobación a fuente dinámica
            fuenteDinamicaCrudClient.actualizarEstadoHecho(id, EstadoRevision.ACEPTADO, null, adminMongoId);
            return ResponseEntity.ok().body("{\"message\": \"Hecho aprobado exitosamente\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // Aprobar un hecho con sugerencias (delega a fuente dinámica)
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_APROBAR_RECHAZAR')")
    @PostMapping("/hechos/{id}/aprobar-con-sugerencias")
    public ResponseEntity<?> aprobarHechoConSugerencias(
            @PathVariable String id,
            @RequestHeader(value = "X-Admin-Id", required = true) Long adminId,
            @RequestBody AprobarHechoDTO dto) {
        try {
            // Validar que haya sugerencia
            if (dto.getSugerencia() == null || dto.getSugerencia().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"La sugerencia es requerida\"}");
            }
            
            // Obtener el ID de MongoDB del administrador
            String adminMongoId = fuenteDinamicaCrudClient.obtenerIdMongoDBPorIdAgregador(adminId);
            if (adminMongoId == null) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Administrador no encontrado en MongoDB\"}");
            }
            
            // Delegar a fuente dinámica
            fuenteDinamicaCrudClient.actualizarEstadoHecho(id, EstadoRevision.ACEPTADO_CON_SUGERENCIAS, dto.getSugerencia(), adminMongoId);
            return ResponseEntity.ok().body("{\"message\": \"Hecho aprobado con sugerencias exitosamente\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // Rechazar un hecho (delega a fuente dinámica)
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('HECHO_APROBAR_RECHAZAR')")
    @PostMapping("/hechos/{id}/rechazar")
    public ResponseEntity<?> rechazarHecho(
            @PathVariable String id,
            @RequestHeader(value = "X-Admin-Id", required = true) Long adminId,
            @RequestBody RechazarHechoDTO dto) {
        try {
            // Validar que haya motivo
            if (dto.getMotivo() == null || dto.getMotivo().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"El motivo es requerido\"}");
            }
            
            // Obtener el ID de MongoDB del administrador
            String adminMongoId = fuenteDinamicaCrudClient.obtenerIdMongoDBPorIdAgregador(adminId);
            if (adminMongoId == null) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Administrador no encontrado en MongoDB\"}");
            }
            
            // Delegar a fuente dinámica
            fuenteDinamicaCrudClient.actualizarEstadoHecho(id, EstadoRevision.RECHAZADO, dto.getMotivo(), adminMongoId);
            return ResponseEntity.ok().body("{\"message\": \"Hecho rechazado exitosamente\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // NOTA: La agregación manual se debe ejecutar directamente en el agregador (puerto 8081)
    // El gestor-solicitudes NO debe tener acceso a IngestaOrquestadorService ni ServicioAgregacion
    // Estos servicios pertenecen al módulo agregador y violan la separación de microservicios
    
    // ==================== IMPORTACIÓN DE DATASETS ====================
    
    /**
     * Endpoint administrativo para importar datasets CSV
     * Hace forward a fuente-estática que procesa el archivo
     */
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('DATASET_IMPORTAR')")
    @PostMapping(value = "/importar-dataset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importarDataset(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-Admin-Id", required = true) Long adminId) {
        try {
            log.info("Admin {} solicita importar dataset: {} ({} bytes)", 
                    adminId, file.getOriginalFilename(), file.getSize());
            
            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"El archivo está vacío\"}");
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("{\"error\": \"Solo se aceptan archivos CSV\"}");
            }
            
            // Construir URL de fuente-estática
            String url = fuenteEstaticaUrl + "/api/fuente-estatica/cargar";
            
            // Preparar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Preparar body con el archivo
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                    new HttpEntity<>(body, headers);
            
            // Forward a fuente-estática
            log.info("Enviando CSV a fuente-estática: {}", url);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            
            log.info("Respuesta de fuente-estática: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            
        } catch (Exception e) {
            log.error("Error al importar dataset: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Error procesando CSV: " + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Listar archivos CSV importados
     * Hace forward a fuente-estática
     */
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('DATASET_LISTAR')")
    @GetMapping("/importaciones")
    public ResponseEntity<?> listarImportaciones() {
        try {
            String url = fuenteEstaticaUrl + "/api/fuente-estatica/archivos";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("Error al listar importaciones: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== GESTIÓN DE CONTRIBUYENTES (ADMIN) ====================
    
    /**
     * Obtener todos los contribuyentes - Solo administradores
     * Para ver lista de usuarios registrados en el sistema
     */
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('SEGURIDAD_DATOS_GESTIONAR')")
    @GetMapping("/contribuyentes")
    public ResponseEntity<List<Contribuyente>> obtenerTodosLosContribuyentes() {
        try {
            List<Contribuyente> contribuyentes = contribuyenteService.obtenerTodosLosContribuyentes();
            return ResponseEntity.ok(contribuyentes);
        } catch (Exception e) {
            log.error("Error al obtener contribuyentes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener un contribuyente específico por ID - Solo administradores
     * Para ver el perfil completo de cualquier usuario (con hechos subidos)
     */
    // @PreAuthorize("hasRole('ADMINISTRADOR') and hasAnyAuthority('SEGURIDAD_DATOS_GESTIONAR')")
    @GetMapping("/contribuyentes/{id}")
    public ResponseEntity<?> obtenerContribuyentePorId(@PathVariable Long id) {
        try {
            Contribuyente contribuyente = contribuyenteService.obtenerContribuyentePorId(id);
            if (contribuyente != null) {
                // Crear respuesta con perfil completo del usuario
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
            log.error("Error al obtener contribuyente {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}