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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cliente HTTP para comunicarse con el microservicio fuente-estática.
 * Obtiene hechos de archivos CSV procesados.
 */
@Component
@Slf4j
public class FuenteEstaticaClient implements FuenteDeDatos {
    
    private final RestTemplate restTemplate;
    private final String fuenteEstaticaUrl;
    
    public FuenteEstaticaClient(@Value("${fuente.estatica.url:http://localhost:8083}") String fuenteEstaticaUrl) {
        this.restTemplate = new RestTemplate();
        this.fuenteEstaticaUrl = fuenteEstaticaUrl.trim();
        log.info("FuenteEstaticaClient inicializado con URL: '{}'", this.fuenteEstaticaUrl);
    }
    
    /**
     * Obtiene todos los hechos de archivos CSV pendientes.
     * Este método gatilla el procesamiento en fuente-estática.
     */
    @Override
    public List<Hecho> obtenerHechos() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(fuenteEstaticaUrl)
                    .path("/api/fuente-estatica/hechos")
                    .toUriString();
            
            log.info("📁 Obteniendo hechos de fuente estática: {}", url);
            
            ResponseEntity<List<HechoDTO>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<HechoDTO>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                List<HechoDTO> hechosDTO = response.getBody();
                if (hechosDTO != null) {
                    log.info("📁 Recibidos {} HechoDTO de fuente estática, convirtiendo a entidades...", hechosDTO.size());
                    List<Hecho> hechos = hechosDTO.stream()
                            .map(HechoMapper::toEntity)
                            .collect(Collectors.toList());
                    log.info("📁 Convertidos {} hechos exitosamente desde archivos CSV", hechos.size());
                    return hechos;
                }
            }
            log.warn("No se recibieron hechos de fuente estática. Status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error al comunicarse con fuente estática: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Hecho obtenerHechoPorId(Long id) {
        throw new UnsupportedOperationException("Fuente estática no soporta consulta individual por ID");
    }
    
    @Override
    public void agregarHecho(Hecho hecho) {
        throw new UnsupportedOperationException("Los hechos estáticos se cargan únicamente desde datasets CSV");
    }
    
    @Override
    public void eliminarHecho(String id) {
        throw new UnsupportedOperationException("Fuente estática no permite eliminación desde el agregador");
    }
    
    @Override
    public String getTipo() {
        return "ESTATICA";
    }
    
    @Override
    public String getIdentificador() {
        return "ESTATICA";
    }
}
