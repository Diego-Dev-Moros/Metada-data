package com.metamapa.client;

import com.metamapa.dto.HechoDinamicoDTO;
import com.metamapa.dto.HechoExternoDTO;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.mapper.HechoExternoMapper;
import com.metamapa.mapper.HechoDinamicoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Cliente de solo lectura para obtener hechos de la fuente dinámica.
 * Usado por el proceso de agregación para sincronizar hechos aprobados.
 */
@Component
@Slf4j
public class FuenteDinamicaClient implements FuenteDeDatos {
    
    private final RestTemplate restTemplate;
    private final String fuenteDinamicaUrl;
    
    public FuenteDinamicaClient(@Value("${fuente.dinamica.url:http://localhost:8082}") String fuenteDinamicaUrl) {
        this.restTemplate = new RestTemplate();
        this.fuenteDinamicaUrl = fuenteDinamicaUrl.trim();
        log.info("FuenteDinamicaClient inicializado con URL: '{}'", this.fuenteDinamicaUrl);
    }
    
    // ===== Implementación de FuenteDeDatos para agregación =====
    
    @Override
    public List<Hecho> obtenerHechos() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/hechos") //?ultimaActualizacion={ultimaActualizacion}
                    .toUriString();
            
            log.info("🌐 Obteniendo hechos de fuente dinámica: {}", url);
            
            ResponseEntity<List<HechoDinamicoDTO>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<HechoDinamicoDTO>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                List<HechoDinamicoDTO> hechosDTO = response.getBody();
                if (hechosDTO != null) {
                    log.info("🌐 Recibidos {} HechoDinamicoDTO de fuente dinámica, convirtiendo a entidades...", hechosDTO.size());
                    List<Hecho> hechos = hechosDTO.stream()
                            .map(HechoDinamicoMapper::toEntity)
                            .collect(java.util.stream.Collectors.toList());
                    log.info("🌐 Convertidos {} hechos exitosamente", hechos.size());
                    return hechos;
                }
            }
            log.error("Error al obtener hechos de fuente dinámica. Status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error al comunicarse con fuente dinámica: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Hecho obtenerHechoPorId(Long id) {
        return obtenerHechoPorId(String.valueOf(id));
    }
    
    /**
     * Sobrecarga para obtener hecho por ID de MongoDB (String)
     */
    public Hecho obtenerHechoPorId(String id) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteDinamicaUrl)
                    .path("/api/fuente-dinamica/hechos/{id}")
                    .buildAndExpand(id)
                    .toUriString();
            
            ResponseEntity<HechoExternoDTO> response = restTemplate.getForEntity(url, HechoExternoDTO.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return HechoExternoMapper.toEntity(response.getBody());
            } else {
                log.error("Error al obtener hecho {} de fuente dinámica. Status: {}", id, response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al obtener hecho {} de fuente dinámica: {}", id, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void agregarHecho(Hecho hecho) {
        // No implementado - esta es una fuente de solo lectura para agregación
        log.warn("FuenteDinamicaClient es de solo lectura. Usar FuenteDinamicaCrudClient para escritura");
    }
    
    @Override
    public void eliminarHecho(String id) {
        // No implementado - esta es una fuente de solo lectura para agregación
        log.warn("FuenteDinamicaClient es de solo lectura. Usar FuenteDinamicaCrudClient para escritura");
    }
    
    @Override
    public String getTipo() {
        return "DINAMICA";
    }
    
    @Override
    public String getIdentificador() {
        return "fuente-dinamica-client";
    }
    
    // ===== Métodos para moderación administrativa (solo lectura) =====
    
    public List<Hecho> obtenerHechosPendientes() {
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
                    List<Hecho> hechos = HechoDinamicoMapper.toEntityList(hechosDTO);
                    log.debug("Obtenidos {} hechos pendientes de fuente dinámica", hechos.size());
                    return hechos;
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
}
