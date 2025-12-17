package com.metamapa.scheduler;

import com.metamapa.service.StatsService;
import com.metamapa.repository.HechoRepository;
import com.metamapa.repository.StatsMetadataRepository;
import com.metamapa.entity.StatsMetadata;
import com.metamapa.entities.hechos.Hecho;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Scheduler para ejecutar tareas programadas de estadísticas
 * Ejecuta cada 3 horas para sincronizar con el agregador
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StatsScheduler {
    
    private final StatsService statsService;
    private final HechoRepository hechoRepository;
    private final StatsMetadataRepository statsMetadataRepository;

    
    @Scheduled(cron = "0 */5  * * * ?")
    public void calcularEstadisticasDiarias() {
        log.info("Iniciando cálculo de estadísticas cada 3 horas - {}", LocalDateTime.now());
        
        try {
            // 1. Verificar si hay hechos nuevos desde la última ejecución
            LocalDateTime ultimaActualizacion = obtenerUltimaActualizacion();
            List<Hecho> hechosNuevos = hechoRepository.findByFechaCargaAfter(ultimaActualizacion);
            
            if (hechosNuevos.isEmpty()) {
                log.info("No hay hechos nuevos desde {}, manteniendo estadísticas actuales", ultimaActualizacion);
                return;
            }
            
            log.info("Encontrados {} hechos nuevos, recalculando estadísticas", hechosNuevos.size());
            
            // 2. Recalcular estadísticas
            statsService.recalcularEstadisticas();
            
            // 3. Actualizar timestamp de última ejecución
            actualizarUltimaActualizacion(LocalDateTime.now());
            
            log.info("Estadísticas actualizadas exitosamente");
            
        } catch (Exception e) {
            log.error("Error al calcular estadísticas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtener la última actualización de estadísticas
     */
    private LocalDateTime obtenerUltimaActualizacion() {
        Optional<StatsMetadata> metadata = statsMetadataRepository.findStatsMetadata();
        
        if (metadata.isPresent()) {
            return metadata.get().getUltimaActualizacionStats();
        }
        
        // Si no existe metadata, crear uno inicial
        StatsMetadata metadataInicial = new StatsMetadata(LocalDateTime.now().minusDays(1), 0L, "1.0");
        statsMetadataRepository.save(metadataInicial);
        
        return metadataInicial.getUltimaActualizacionStats();
    }
    
    /** Actualizar la última actualización de estadísticas
     */
    private void actualizarUltimaActualizacion(LocalDateTime fecha) {
        Optional<StatsMetadata> metadata = statsMetadataRepository.findStatsMetadata();
        
        if (metadata.isPresent()) {
            metadata.get().setUltimaActualizacionStats(fecha);
            statsMetadataRepository.save(metadata.get());
        } else {
            // Crear metadata si no existe
            StatsMetadata nuevoMetadata = new StatsMetadata(fecha, 0L, "1.0");
            statsMetadataRepository.save(nuevoMetadata);
        }
    }
    
    /**
     * Ejecutar cálculo manual de estadísticas (para testing)
     */
    public void ejecutarCalculoManual() {
        log.info("Ejecutando cálculo manual de estadísticas");
        statsService.recalcularEstadisticas();
    }
}
