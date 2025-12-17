package com.metamapa.service;

import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.hechos.Hecho;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FuenteMetaMapaService implements FuenteDeDatos {
    private final String identificador;
    private final String urlBase;
    private final RestTemplate restTemplate = new RestTemplate();

    public FuenteMetaMapaService(@Value("${fuente.metamapa.url:http://localhost:8080}") String urlBase) {
        this.urlBase = urlBase;
        this.identificador = "fuente-metamapa-" + urlBase.hashCode();
        log.info("Inicializado FuenteMetaMapaService para: {}", urlBase);
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> todosLosHechos = new ArrayList<>();
        
        try {
            // 1. Intentar obtener hechos directamente de la API pública (más eficiente)
            String urlHechosDirectos = urlBase + "/api/public/hechos";
            try {
                Hecho[] hechosDirectos = restTemplate.getForObject(urlHechosDirectos, Hecho[].class);
                if (hechosDirectos != null && hechosDirectos.length > 0) {
                    log.info("Obtenidos {} hechos directamente de API pública: {}", hechosDirectos.length, urlBase);
                    return Arrays.asList(hechosDirectos);
                }
            } catch (Exception e) {
                log.debug("API pública de hechos no disponible, obteniendo vía colecciones: {}", e.getMessage());
            }
            
            // 2. Fallback: obtener hechos a través de todas las colecciones
            List<Object> colecciones = obtenerColecciones();
            
            if (colecciones.isEmpty()) {
                log.info("No hay colecciones disponibles para obtener hechos de: {}", urlBase);
                return new ArrayList<>();
            }
            
            // Iterar sobre todas las colecciones para obtener sus hechos
            for (Object coleccionObj : colecciones) {
                try {
                    java.util.LinkedHashMap<?, ?> coleccion = (java.util.LinkedHashMap<?, ?>) coleccionObj;
                    Object idColeccion = coleccion.get("identificador");
                    
                    if (idColeccion != null) {
                        String urlHechos = urlBase + "/api/agregador/colecciones/" + idColeccion + "/hechos";
                        Hecho[] hechos = restTemplate.getForObject(urlHechos, Hecho[].class);
                        
                        if (hechos != null) {
                            List<Hecho> hechosColeccion = Arrays.asList(hechos);
                            todosLosHechos.addAll(hechosColeccion);
                            log.debug("Obtenidos {} hechos de colección {}", hechos.length, idColeccion);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error al obtener hechos de una colección en {}: {}", urlBase, e.getMessage());
                }
            }
            
            log.info("Total de hechos obtenidos de {}: {}", urlBase, todosLosHechos.size());
            return todosLosHechos;
            
        } catch (Exception e) {
            log.warn("Error al consultar hechos de instancia MetaMapa {}: {}", urlBase, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public Hecho obtenerHechoPorId(Long id) {
        if (id == null) return null;
        
        try {
            // 1. Intentar obtener directamente de la API pública
            String urlHechoDirecto = urlBase + "/api/public/hechos/" + id;
            try {
                Hecho hecho = restTemplate.getForObject(urlHechoDirecto, Hecho.class);
                if (hecho != null) {
                    log.debug("Hecho {} obtenido directamente de API pública", id);
                    return hecho;
                }
            } catch (Exception e) {
                log.debug("API pública de hecho por ID no disponible, buscando en colecciones: {}", e.getMessage());
            }
            
            // 2. Si falla, buscar en todos los hechos de todas las colecciones
            List<Hecho> todosLosHechos = obtenerHechos();
            return todosLosHechos.stream()
                    .filter(hecho -> id.equals(hecho.getId()))
                    .findFirst()
                    .orElse(null);
                    
        } catch (Exception e) {
            log.warn("Error al obtener hecho {} de instancia MetaMapa {}: {}", id, urlBase, e.getMessage());
            return null;
        }
    }
    @Override
    public void agregarHecho(Hecho hecho) {
        // FuenteMetaMapaService es de solo lectura - no permite agregar hechos
        log.debug("Operación no soportada: FuenteMetaMapaService es de solo lectura");
    }

    @Override
    public void eliminarHecho(String titulo) {
        // FuenteMetaMapaService es de solo lectura - no permite eliminar hechos
        log.debug("Operación no soportada: FuenteMetaMapaService es de solo lectura");
    }

    @Override
    public String getTipo() {
        return "METAMAPA";
    }

    @Override
    public String getIdentificador() {
        return identificador;
    }
    
    /**
     * Obtiene todas las colecciones disponibles de la instancia MetaMapa remota
     * @return Lista de colecciones disponibles
     */
    public List<Object> obtenerColecciones() {
        try {
            String urlColecciones = urlBase + "/api/public/colecciones";
            
            // Intentar primero la API pública
            try {
                Object[] colecciones = restTemplate.getForObject(urlColecciones, Object[].class);
                if (colecciones != null) {
                    log.info("Obtenidas {} colecciones de API pública: {}", colecciones.length, urlBase);
                    return Arrays.asList(colecciones);
                }
            } catch (Exception e) {
                log.debug("API pública de colecciones no disponible, intentando API agregador: {}", e.getMessage());
            }
            
            // Fallback: usar API del agregador
            String urlAgregador = urlBase + "/api/agregador/colecciones";
            Object[] colecciones = restTemplate.getForObject(urlAgregador, Object[].class);
            
            if (colecciones != null) {
                log.info("Obtenidas {} colecciones de API agregador: {}", colecciones.length, urlBase);
                return Arrays.asList(colecciones);
            } else {
                log.info("No hay colecciones disponibles en: {}", urlBase);
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.warn("Error al obtener colecciones de instancia MetaMapa {}: {}", urlBase, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Verifica si la instancia MetaMapa remota está disponible
     * @return true si la instancia responde, false en caso contrario
     */
    public boolean estaDisponible() {
        try {
            String urlPing = urlBase + "/api/public/colecciones";
            restTemplate.getForObject(urlPing, Object.class);
            return true;
        } catch (Exception e) {
            log.debug("Instancia MetaMapa {} no está disponible: {}", urlBase, e.getMessage());
            return false;
        }
    }
} 