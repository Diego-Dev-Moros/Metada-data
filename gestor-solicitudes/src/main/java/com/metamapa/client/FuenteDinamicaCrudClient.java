package com.metamapa.client;

import com.metamapa.dto.HechoDinamicoDTO;
import com.metamapa.dto.ReportarHechoDTO;
import com.metamapa.dto.RevisarHechoDTO;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.rol.Contribuyente;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Cliente para operaciones de escritura (CRUD) en la fuente dinámica.
 * Este cliente será migrado al módulo gestor-solicitudes.
 * 
 * Responsabilidades:
 * - Crear hechos reportados por contribuyentes
 * - Actualizar estado de hechos (aprobar/rechazar)
 * - Registrar contribuyentes en MongoDB
 * - Sincronizar IDs entre agregador y fuente-dinámica
 */
@Component
@Slf4j
public class FuenteDinamicaCrudClient {
    
    private final RestTemplate restTemplate;
    private final String fuenteDinamicaUrl;
    
    public FuenteDinamicaCrudClient(@Value("${fuente.dinamica.url:http://localhost:8082}") String fuenteDinamicaUrl) {
        this.restTemplate = new RestTemplate();
        this.fuenteDinamicaUrl = fuenteDinamicaUrl.trim();
        log.info("FuenteDinamicaCrudClient inicializado con URL: '{}'", this.fuenteDinamicaUrl);
    }
    
    // ===== Operaciones de creación de hechos =====
    
    /**
     * Crea un hecho en la fuente dinámica con soporte para archivos multimedia
     * @param hechoDTO Datos del hecho
     * @param contribuyente Contribuyente que reporta el hecho
     * @param archivos Lista de archivos multimedia (opcional)
     * @return Hecho creado
     */
    public Hecho crearHecho(ReportarHechoDTO hechoDTO, Contribuyente contribuyente, List<MultipartFile> archivos) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/cargar")
                    .toUriString();
            
            // Si hay archivos, usar multipart/form-data
            if (archivos != null && !archivos.isEmpty()) {
                return crearHechoConArchivos(url, hechoDTO, contribuyente, archivos);
            } else {
                return crearHechoSinArchivos(url, hechoDTO, contribuyente);
            }
            
        } catch (Exception e) {
            log.error("Error al comunicarse con fuente dinámica: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Crea hecho con archivos usando multipart/form-data
     */
    private Hecho crearHechoConArchivos(String url, ReportarHechoDTO hechoDTO, 
                                        Contribuyente contribuyente, List<MultipartFile> archivos) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // Crear el DTO interno
            CrearHechoFuenteDinamicaDTO dto = construirDTO(hechoDTO, contribuyente);
            
            // Convertir DTO a JSON string
            ObjectMapper mapper = new ObjectMapper();
            String hechoJson = mapper.writeValueAsString(dto);
            
            // Construir el multipart request
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("hecho", hechoJson);
            
            // Agregar archivos
            for (MultipartFile file : archivos) {
                ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                };
                body.add("archivos", resource);
            }
            
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<HechoResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, request, HechoResponseDTO.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Hecho con {} archivo(s) creado exitosamente en fuente dinámica: {} por {}", 
                        archivos.size(), dto.getTitulo(), contribuyente.getNombre());
                return mapearHechoResponse(response.getBody());
            } else {
                log.error("Error al crear hecho en fuente dinámica. Status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error al crear hecho con archivos: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Crea hecho sin archivos usando application/json
     */
    private Hecho crearHechoSinArchivos(String url, ReportarHechoDTO hechoDTO, Contribuyente contribuyente) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            CrearHechoFuenteDinamicaDTO dto = construirDTO(hechoDTO, contribuyente);
            
            HttpEntity<CrearHechoFuenteDinamicaDTO> request = new HttpEntity<>(dto, headers);
            
            ResponseEntity<HechoResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, request, HechoResponseDTO.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Hecho creado exitosamente en fuente dinámica: {} por {}", 
                        dto.getTitulo(), contribuyente.getNombre());
                return mapearHechoResponse(response.getBody());
            } else {
                log.error("Error al crear hecho en fuente dinámica. Status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error al crear hecho: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Construye el DTO interno para enviar a la fuente dinámica
     */
    private CrearHechoFuenteDinamicaDTO construirDTO(ReportarHechoDTO hechoDTO, Contribuyente contribuyente) {
        CrearHechoFuenteDinamicaDTO dto = new CrearHechoFuenteDinamicaDTO();
        dto.setTitulo(hechoDTO.getTitulo());
        dto.setDescripcion(hechoDTO.getDescripcion());
        dto.setCategoria(hechoDTO.getCategoria());
        dto.setLatitud(hechoDTO.getLatitud());
        dto.setLongitud(hechoDTO.getLongitud());
        
        // Convertir fechaHecho a String (ISO-8601 format)
        if (hechoDTO.getFechaHecho() != null) {
            dto.setFechaHecho(hechoDTO.getFechaHecho());
        } else {
            dto.setFechaHecho(LocalDateTime.now().toString());
        }
        
        // Transferir esAnonimo desde el DTO de entrada
        dto.setEsAnonimo(hechoDTO.getEsAnonimo());
        
        // Datos del contribuyente desde la entidad
        dto.setNombreContribuyente(contribuyente.getNombre());
        dto.setApellidoContribuyente(contribuyente.getApellido());
        dto.setEdadContribuyente(contribuyente.getEdad());
        dto.setIdContribuyente(contribuyente.getId());
        
        return dto;
    }
    
    private Hecho mapearHechoResponse(HechoResponseDTO dto) {
        Hecho hecho = new Hecho();
        // No seteamos el ID porque es un String de MongoDB, no un Long de MySQL
        // El hecho está pendiente en MongoDB y aún no tiene ID en la base central
        hecho.setTitulo(dto.getTitulo());
        hecho.setDescripcion(dto.getDescripcion());
        hecho.setCategoria(dto.getCategoria());
        return hecho;
    }
    
    // ===== Operaciones de moderación administrativa =====
    
    /**
     * Obtiene los hechos pendientes CON SU ID de MongoDB (para que el admin pueda aprobar/rechazar)
     */
    public List<HechoDinamicoDTO> obtenerHechosPendientesConId() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/hechos/pendientes")
                    .toUriString();
            
            ResponseEntity<List<HechoDinamicoDTO>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<HechoDinamicoDTO>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                List<HechoDinamicoDTO> hechosDTO = response.getBody();
                if (hechosDTO != null) {
                    log.debug("Obtenidos {} hechos pendientes de fuente dinámica (con ID)", hechosDTO.size());
                    return hechosDTO;
                }
            }
            log.error("Error al obtener hechos pendientes de fuente dinámica. Status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error al obtener hechos pendientes de fuente dinámica: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Actualiza el estado de un hecho (aprobar/rechazar)
     */
    public void actualizarEstadoHecho(String idHecho, EstadoRevision estado, String sugerencia, String idAdminMongoDB) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/hechos/{id}/estado")
                    .buildAndExpand(idHecho)
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Agregar header con ID del administrador de MongoDB
            if (idAdminMongoDB != null && !idAdminMongoDB.isEmpty()) {
                headers.set("X-Contribuyente-Id", idAdminMongoDB);
            }
            
            RevisarHechoDTO dto = new RevisarHechoDTO();
            dto.setEstado(estado);
            dto.setSugerencia(sugerencia);
            
            HttpEntity<RevisarHechoDTO> request = new HttpEntity<>(dto, headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.PUT, request, Void.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Estado del hecho {} actualizado a {} en fuente dinámica", idHecho, estado);
            } else {
                log.error("Error al actualizar estado del hecho {} en fuente dinámica. Status: {}", idHecho, response.getStatusCode());
                throw new RuntimeException("Error al actualizar estado del hecho. Status: " + response.getStatusCode());
            }
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Error HTTP al actualizar estado del hecho {} en fuente dinámica: {} - {}", idHecho, e.getStatusCode(), e.getMessage());
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("Hecho no encontrado");
            } else if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new RuntimeException("No autorizado para actualizar este hecho");
            } else {
                throw new RuntimeException("Error al actualizar estado del hecho: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error al actualizar estado del hecho {} en fuente dinámica: {}", idHecho, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar estado del hecho: " + e.getMessage());
        }
    }
    
    // ===== Operaciones de sincronización de contribuyentes =====
    
    /**
     * Registra un contribuyente en la fuente dinámica
     * Retorna el ID de MongoDB asignado
     */
    public String registrarContribuyenteEnFuenteDinamica(Long idAgregador, String nombre, String apellido, 
                                                         java.time.LocalDate fechaNacimiento, String tipoUsuario) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/registrarse")
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            RegistrarContribuyenteDTO dto = new RegistrarContribuyenteDTO();
            dto.setIdAgregador(idAgregador); // Guardar referencia al ID del agregador
            dto.setNombre(nombre);
            dto.setApellido(apellido);
            dto.setFechaNacimiento(fechaNacimiento);
            dto.setTipoUsuario(tipoUsuario);
            
            HttpEntity<RegistrarContribuyenteDTO> request = new HttpEntity<>(dto, headers);
            
            ResponseEntity<ContribuyenteResponseDTO> response = restTemplate.exchange(
                url, HttpMethod.POST, request, ContribuyenteResponseDTO.class);
            
            ContribuyenteResponseDTO body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                log.info("Contribuyente {} {} sincronizado con fuente dinámica. MongoDB ID: {}", 
                        nombre, apellido, body.getId());
                return body.getId();
            } else {
                log.error("Error al registrar contribuyente en fuente dinámica. Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al registrar contribuyente en fuente dinámica: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Obtener el ID de MongoDB de un contribuyente usando su ID del agregador
     */
    public String obtenerIdMongoDBPorIdAgregador(Long idAgregador) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/contribuyentes/por-id-agregador/{idAgregador}")
                    .buildAndExpand(idAgregador)
                    .toUriString();
            
            ResponseEntity<ContribuyenteResponseDTO> response = restTemplate.getForEntity(url, ContribuyenteResponseDTO.class);
            
            ContribuyenteResponseDTO body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null) {
                return body.getId();
            } else {
                log.error("Error al obtener contribuyente por idAgregador {} de fuente dinámica. Status: {}", 
                        idAgregador, response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al obtener contribuyente por idAgregador {} de fuente dinámica: {}", 
                    idAgregador, e.getMessage(), e);
            return null;
        }
    }
    
    // ===== DTOs internos =====
    
    public static class CrearHechoFuenteDinamicaDTO {
        private String titulo;
        private String descripcion;
        private String categoria;
        private double latitud;
        private double longitud;
        private String fechaHecho; // String para evitar problemas de serialización
        private String nombreContribuyente;
        private String apellidoContribuyente;
        private Integer edadContribuyente;
        private Long idContribuyente; // ID del contribuyente del agregador
        private Boolean esAnonimo; // Indica si el reporte es anónimo
        
        // Getters y setters
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        
        public double getLatitud() { return latitud; }
        public void setLatitud(double latitud) { this.latitud = latitud; }
        
        public double getLongitud() { return longitud; }
        public void setLongitud(double longitud) { this.longitud = longitud; }
        
        public String getFechaHecho() { return fechaHecho; }
        public void setFechaHecho(String fechaHecho) { this.fechaHecho = fechaHecho; }
        
        public String getNombreContribuyente() { return nombreContribuyente; }
        public void setNombreContribuyente(String nombreContribuyente) { this.nombreContribuyente = nombreContribuyente; }
        
        public String getApellidoContribuyente() { return apellidoContribuyente; }
        public void setApellidoContribuyente(String apellidoContribuyente) { this.apellidoContribuyente = apellidoContribuyente; }
        
        public Integer getEdadContribuyente() { return edadContribuyente; }
        public void setEdadContribuyente(Integer edadContribuyente) { this.edadContribuyente = edadContribuyente; }
        
        public Long getIdContribuyente() { return idContribuyente; }
        public void setIdContribuyente(Long idContribuyente) { this.idContribuyente = idContribuyente; }
        
        public Boolean getEsAnonimo() { return esAnonimo; }
        public void setEsAnonimo(Boolean esAnonimo) { this.esAnonimo = esAnonimo; }
    }
    
    public static class HechoResponseDTO {
        private String id; // MongoDB ObjectId es String
        private String titulo;
        private String descripcion;
        private String categoria;
        
        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
    }
    
    public static class RegistrarContribuyenteDTO {
        private Long idAgregador; // ID del agregador para referencia cruzada
        private String nombre;
        private String apellido;
        private java.time.LocalDate fechaNacimiento;
        private String tipoUsuario;
        
        public Long getIdAgregador() { return idAgregador; }
        public void setIdAgregador(Long idAgregador) { this.idAgregador = idAgregador; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        
        public java.time.LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        
        public String getTipoUsuario() { return tipoUsuario; }
        public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    }
    
    public static class ContribuyenteResponseDTO {
        private String id; // MongoDB ObjectId
        private Long idAgregador;
        private String nombre;
        private String apellido;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public Long getIdAgregador() { return idAgregador; }
        public void setIdAgregador(Long idAgregador) { this.idAgregador = idAgregador; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
    }
}
