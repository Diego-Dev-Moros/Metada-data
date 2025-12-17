package com.metamapa.scheduler;

import com.metamapa.service.ServicioAgregacion;
import com.metamapa.service.IngestaOrquestadorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AgregacionScheduler {
    
    private final ServicioAgregacion servicioAgregacion;
    private final IngestaOrquestadorService ingestaOrquestadorService;


    /**
     * Ejecuta la agregación de todas las fuentes cada hora
     */
    @Scheduled(fixedRate = 60000) // 1 hora = 3600000 ms
    public void ejecutarAgregacionProgramada() {
        log.info("Iniciando agregación programada de todas las colecciones");

        try {
            // 1️⃣ Ingesta y procesamiento de hechos desde todas las fuentes
            ingestaOrquestadorService.procesarHechosDeFuentes();

            // 2️⃣ Actualización de todas las colecciones
            servicioAgregacion.actualizarTodasLasColecciones();

            log.info("Ingesta y agregación programada completada exitosamente");
        } catch (Exception e) {
            log.error("Error durante la ingesta y agregación programada: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta la agregación en horarios de baja carga (consenso)
     * Cada día a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutarConsensoEnBajaCarga() {
        log.info("Iniciando cálculo de consenso en horario de baja carga");
        
        try {
            // TODO: Implementar cálculo de consenso específico
            servicioAgregacion.actualizarTodasLasColecciones();
            log.info("Cálculo de consenso completado exitosamente");
        } catch (Exception e) {
            log.error("Error durante el cálculo de consenso: {}", e.getMessage(), e);
        }
    }
}
