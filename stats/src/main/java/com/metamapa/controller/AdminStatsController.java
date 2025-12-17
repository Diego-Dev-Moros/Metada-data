package com.metamapa.controller;

import com.metamapa.service.StatsService;
import com.metamapa.service.CSVExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para operaciones administrativas del servicio de estadísticas
 * Endpoints que requieren permisos administrativos
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminStatsController {
    
    private final StatsService statsService;
    private final CSVExportService csvExportService;
    
    /**
     * Recalcular todas las estadísticas del sistema
     * POST /api/admin/stats/recalcular
     */
    @PostMapping("/recalcular")
    public ResponseEntity<Map<String, Object>> recalcularEstadisticas() {
        log.info("Iniciando recálculo manual de estadísticas");
        
        try {
            statsService.recalcularEstadisticas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estadísticas recalculadas exitosamente");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al recalcular estadísticas: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al recalcular estadísticas");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Limpiar caché de estadísticas
     * POST /api/admin/stats/cache/limpiar
     */
    @PostMapping("/cache/limpiar")
    public ResponseEntity<Map<String, Object>> limpiarCache() {
        log.info("Limpiando caché de estadísticas");
        
        try {
            // El StatsService tiene un método para limpiar caché
            // Como no está expuesto públicamente, forzamos un recálculo que limpia el caché
            statsService.recalcularEstadisticas();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Caché limpiado exitosamente");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al limpiar caché: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al limpiar caché");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener estado del servicio de estadísticas
     * GET /api/admin/stats/estado
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstado() {
        log.info("Consultando estado del servicio de estadísticas");
        
        try {
            Map<String, Object> estado = new HashMap<>();
            estado.put("servicio", "metamapa-stats");
            estado.put("estado", "ACTIVO");
            estado.put("timestamp", java.time.LocalDateTime.now());
            estado.put("version", "1.0.0");
            
            // Información adicional del sistema
            Map<String, Object> sistema = new HashMap<>();
            sistema.put("java_version", System.getProperty("java.version"));
            sistema.put("os_name", System.getProperty("os.name"));
            sistema.put("available_processors", Runtime.getRuntime().availableProcessors());
            sistema.put("max_memory", Runtime.getRuntime().maxMemory());
            sistema.put("total_memory", Runtime.getRuntime().totalMemory());
            sistema.put("free_memory", Runtime.getRuntime().freeMemory());
            
            estado.put("sistema", sistema);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estado);
            response.put("message", "Estado obtenido exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener estado: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estado");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Limpiar archivos CSV antiguos
     * POST /api/admin/stats/csv/limpiar
     */
    @PostMapping("/csv/limpiar")
    public ResponseEntity<Map<String, Object>> limpiarArchivosCSVAntiguos() {
        log.info("Limpiando archivos CSV antiguos");
        
        try {
            csvExportService.limpiarArchivosAntiguos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivos CSV antiguos eliminados exitosamente");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al limpiar archivos CSV antiguos: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al limpiar archivos CSV");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
}
