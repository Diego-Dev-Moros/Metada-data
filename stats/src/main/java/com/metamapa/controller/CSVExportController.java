package com.metamapa.controller;

import com.metamapa.service.CSVExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para exportar estadísticas a CSV
 * Permite descargar estadísticas en formato CSV
 */
@RestController
@RequestMapping("/api/stats/export")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CSVExportController {
    
    private final CSVExportService csvExportService;
    
    /**
     * Exportar estadísticas de provincias por colección a CSV
     * GET /api/stats/export/provincias/{coleccionId}
     */
    @GetMapping("/provincias/{coleccionId}")
    public ResponseEntity<?> exportarProvinciasCSV(@PathVariable Long coleccionId) {
        log.info("Exportando estadísticas de provincias para colección: {}", coleccionId);
        
        try {
            Resource resource = csvExportService.exportarProvinciasCSV(coleccionId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"provincias_coleccion_" + coleccionId + ".csv\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error al exportar estadísticas de provincias para colección {}: {}", coleccionId, e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al exportar archivo CSV");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Exportar estadísticas de categorías a CSV
     * GET /api/stats/export/categorias
     */
    @GetMapping("/categorias")
    public ResponseEntity<?> exportarCategoriasCSV() {
        log.info("Exportando estadísticas de categorías");
        
        try {
            Resource resource = csvExportService.exportarCategoriasCSV();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"categorias_estadisticas.csv\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error al exportar estadísticas de categorías: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al exportar archivo CSV");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Exportar estadísticas completas a CSV
     * GET /api/stats/export/completo
     */
    @GetMapping("/completo")
    public ResponseEntity<?> exportarCompletoCSV() {
        log.info("Exportando estadísticas completas");
        
        try {
            Resource resource = csvExportService.exportarCompletoCSV();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"estadisticas_completas.csv\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error al exportar estadísticas completas: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al exportar archivo CSV");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Limpiar archivos CSV antiguos
     * POST /api/stats/export/limpiar
     */
    @PostMapping("/limpiar")
    public ResponseEntity<Map<String, Object>> limpiarArchivosAntiguos() {
        log.info("Limpiando archivos CSV antiguos");
        
        try {
            csvExportService.limpiarArchivosAntiguos();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivos CSV antiguos eliminados exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al limpiar archivos CSV antiguos: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al limpiar archivos");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener información sobre archivos CSV disponibles
     * GET /api/stats/export/archivos
     */
    @GetMapping("/archivos")
    public ResponseEntity<Map<String, Object>> listarArchivosCSV() {
        log.info("Listando archivos CSV disponibles");
        
        try {
            // Esta funcionalidad podría implementarse en el servicio si es necesaria
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Funcionalidad de listado de archivos no implementada aún");
            response.put("data", new HashMap<>());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al listar archivos CSV: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al listar archivos");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
