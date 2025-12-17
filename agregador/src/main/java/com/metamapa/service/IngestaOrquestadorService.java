package com.metamapa.service;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.service.normalizacion.NormalizacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestaOrquestadorService {

    private final NormalizacionService normalizacionService;
    private final DepuracionService depuracionService;
    private final ServicioAgregacion servicioAgregacion;
    private final FuenteService fuenteService; // obtiene hechos de todas las fuentes

    private final Long idColeccion = 1L; // Colección por defecto

    /**
     * Orquesta la ingesta de hechos crudos recibidos manualmente
     */
    public void procesarYActualizarColeccion(Long idColeccion, List<Hecho> hechosCrudos) {
        log.info("Iniciando ingesta de {} hechos crudos", hechosCrudos.size());

        if (hechosCrudos == null || hechosCrudos.isEmpty()) {
            log.info("No hay hechos crudos para procesar");
            return;
        }

        // 1️⃣ Normalización
        List<Hecho> hechosNormalizados = normalizacionService.normalizar(hechosCrudos);
        log.info("Normalización completada: {} hechos normalizados", hechosNormalizados.size());

        // 2️⃣ Depuración / deduplicación
        List<Hecho> hechosDepurados = depuracionService.depurar(hechosNormalizados);
        log.info("Depuración completada: {} hechos únicos", hechosDepurados.size());

        // 3️⃣ Persistencia en repositorio en memoria (ya dentro de depuracionService)

        // 4️⃣ Actualización de la colección en ServicioAgregacion
        servicioAgregacion.actualizarHechosEnColeccion(idColeccion);
        log.info("Colección '{}' actualizada con los nuevos hechos", idColeccion);
    }

    /**
     * Orquesta la ingesta automática desde todas las fuentes
     */
    public void procesarHechosDeFuentes() {
        log.info("Iniciando ingesta desde todas las fuentes");

        // 1️⃣ Obtener hechos crudos de todas las fuentes
        List<Hecho> hechosCrudos = fuenteService.obtenerHechosDeTodasLasFuentes();
        log.info("Se obtuvieron {} hechos crudos de todas las fuentes", hechosCrudos.size());

        // 2️⃣Procesar con el flujo estándar
        if (!hechosCrudos.isEmpty()) {
            procesarYActualizarColeccion(idColeccion, hechosCrudos);
        } else {
            log.info("No hay hechos para procesar");
        }
    }
}

