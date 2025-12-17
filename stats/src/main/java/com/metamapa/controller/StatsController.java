package com.metamapa.controller;

import com.metamapa.dto.*;
import com.metamapa.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para consultar estadísticas del sistema
 * Expone endpoints públicos para obtener estadísticas
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StatsController {
    
    private final StatsService statsService;
    
    /**
     * Obtener provincia con más hechos por colección
     * GET /api/stats/provincia/{coleccionId}
     */
    @GetMapping("/provincia/{coleccionId}")
    public ResponseEntity<Map<String, Object>> getProvinciaConMasHechos(@PathVariable Long coleccionId) {
        log.info("Consultando provincia con más hechos para colección: {}", coleccionId);
        
        try {
            ProvinciaStatsDTO stats = statsService.getProvinciaConMasHechos(coleccionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Estadísticas obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de provincia para colección {}: {}", coleccionId, e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener categoría con más hechos en el sistema
     * GET /api/stats/categoria
     */
    @GetMapping("/categoria")
    public ResponseEntity<Map<String, Object>> getCategoriaConMasHechos() {
        log.info("Consultando categoría con más hechos");
        
        try {
            CategoriaStatsDTO stats = statsService.getCategoriaConMasHechos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Estadísticas obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de categoría: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener provincia con más hechos de una categoría específica
     * GET /api/stats/provincia/categoria/{categoria}
     */
    @GetMapping("/provincia/categoria/{categoria}")
    public ResponseEntity<Map<String, Object>> getProvinciaPorCategoria(@PathVariable String categoria) {
        log.info("Consultando provincia con más hechos para categoría: {}", categoria);
        
        try {
            ProvinciaStatsDTO stats = statsService.getProvinciaPorCategoria(categoria);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Estadísticas obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de provincia por categoría {}: {}", categoria, e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener hora con más hechos de una categoría específica
     * GET /api/stats/hora/categoria/{categoria}
     */
    @GetMapping("/hora/categoria/{categoria}")
    public ResponseEntity<Map<String, Object>> getHoraPorCategoria(@PathVariable String categoria) {
        log.info("Consultando hora con más hechos para categoría: {}", categoria);
        
        try {
            HoraStatsDTO stats = statsService.getHoraPorCategoria(categoria);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Estadísticas obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de hora por categoría {}: {}", categoria, e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener estadísticas de spam en solicitudes de eliminación
     * GET /api/stats/spam
     */
    @GetMapping("/spam")
    public ResponseEntity<Map<String, Object>> getSpamEliminaciones() {
        log.info("Consultando estadísticas de spam");
        
        try {
            SpamStatsDTO stats = statsService.getSpamEliminaciones();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Estadísticas obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de spam: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener todas las estadísticas principales en un solo endpoint
     * GET /api/stats/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Consultando estadísticas del dashboard");
        
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Estadísticas de categorías
            try {
                CategoriaStatsDTO categoriaStats = statsService.getCategoriaConMasHechos();
                dashboard.put("categoriaMasHechos", categoriaStats);
            } catch (Exception e) {
                log.warn("Error al obtener estadísticas de categoría: {}", e.getMessage());
                dashboard.put("categoriaMasHechos", null);
            }
            
            // Estadísticas de spam
            try {
                SpamStatsDTO spamStats = statsService.getSpamEliminaciones();
                dashboard.put("spamStats", spamStats);
            } catch (Exception e) {
                log.warn("Error al obtener estadísticas de spam: {}", e.getMessage());
                dashboard.put("spamStats", null);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dashboard);
            response.put("message", "Dashboard obtenido exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener dashboard: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener dashboard");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
}
