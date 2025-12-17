package com.metamapa.service;

import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.hechos.Hecho;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio del agregador para obtener hechos de todas las fuentes registradas.
 * Solo lectura - NO maneja CRUD de fuentes (eso es responsabilidad de gestor-solicitudes)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FuenteService {

    private final ServicioAgregacion servicioAgregacion;
    
    /**
     * Obtiene todos los hechos de todas las fuentes registradas
     * Lee desde las fuentes usando sus clientes respectivos
     */
    public List<Hecho> obtenerHechosDeTodasLasFuentes() {
        log.info("Obteniendo hechos de todas las fuentes");
        
        List<FuenteDeDatos> fuentes = servicioAgregacion.obtenerTodasLasFuentes();
        List<Hecho> todosLosHechos = new ArrayList<>();
        
        for (FuenteDeDatos fuente : fuentes) {
            try {
                log.debug("Obteniendo hechos de fuente: {}", fuente.getIdentificador());
                List<Hecho> hechosDeFuente = fuente.obtenerHechos();
                todosLosHechos.addAll(hechosDeFuente);
                log.info("Obtenidos {} hechos de fuente '{}'", hechosDeFuente.size(), fuente.getIdentificador());
            } catch (Exception e) {
                log.error("Error al obtener hechos de fuente '{}': {}", fuente.getIdentificador(), e.getMessage());
            }
        }
        
        log.info("Total de hechos obtenidos de todas las fuentes: {}", todosLosHechos.size());
        return todosLosHechos;
    }
}
