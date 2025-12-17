package com.metamapa.client;

import com.metamapa.dto.HechoDTO;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.mapper.HechoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class FuenteEstaticaClient implements FuenteDeDatos {
    
    private final RestTemplate restTemplate;
    private final String fuenteEstaticaUrl;
    
    public FuenteEstaticaClient(@Value("${fuente.estatica.url:http://localhost:8083}") String fuenteEstaticaUrl) {
        this.restTemplate = new RestTemplate();
        // Limpiar cualquier espacio en blanco que pueda haber en la configuración
        this.fuenteEstaticaUrl = fuenteEstaticaUrl.trim();
        log.info("FuenteEstaticaClient inicializado con URL: '{}'", this.fuenteEstaticaUrl);
    }
    
    @Override
    public List<Hecho> obtenerHechos() {
        try {
            // Usar UriComponentsBuilder para construir la URL de forma más robusta
            String url = UriComponentsBuilder.fromHttpUrl(fuenteEstaticaUrl)
                    .path("/api/fuente-estatica/hechos")
                    .toUriString();
            
            log.debug("Intentando conectar a URL: {}", url);
            
            ResponseEntity<List<HechoDTO>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<HechoDTO>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                List<HechoDTO> hechosDTO = response.getBody();
                if (hechosDTO != null) {
                    List<Hecho> hechos = HechoMapper.toEntityList(hechosDTO);
                    log.debug("Obtenidos {} hechos de fuente estática", hechos.size());
                    return hechos;
                }
            }
            log.error("Error al obtener hechos de fuente estática. Status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error al comunicarse con fuente estática: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Hecho obtenerHechoPorId(Long id) {
        try {
            String url = fuenteEstaticaUrl + "/api/fuente-estatica/hechos/" + id;
            
            ResponseEntity<Hecho> response = restTemplate.getForEntity(url, Hecho.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Error al obtener hecho {} de fuente estática. Status: {}", id, response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error al obtener hecho {} de fuente estática: {}", id, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void agregarHecho(Hecho hecho) {
        throw new UnsupportedOperationException("Los hechos estáticos se cargan únicamente desde datasets");
    }
    
    @Override
    public void eliminarHecho(String id) {
        // No implementado para fuente estática
        log.warn("Eliminación de hechos no soportada en fuente estática");
    }
    
    @Override
    public String getTipo() {
        return "ESTATICA";
    }
    
    @Override
    public String getIdentificador() {
        return "fuente-estatica-client";
    }
}
