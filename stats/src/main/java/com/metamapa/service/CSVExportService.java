package com.metamapa.service;

import com.metamapa.dto.*;
import com.metamapa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para exportación de estadísticas a CSV
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CSVExportService {
    
    private final StatsService statsService;
    private final StatsProvinciaRepository statsProvinciaRepository;
    private final StatsCategoriaRepository statsCategoriaRepository;
    private final StatsHoraRepository statsHoraRepository;
    private final StatsSpamRepository statsSpamRepository;
    
    private static final String CSV_DIR = "/tmp/stats-exports/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Exportar estadísticas de provincias por colección a CSV
     */
    public Resource exportarProvinciasCSV(Long coleccionId) throws IOException {
        log.info("Exportando estadísticas de provincias para colección: {}", coleccionId);
        
        // 1. Obtener estadísticas
        ProvinciaStatsDTO stats = statsService.getProvinciaConMasHechos(coleccionId);
        
        // 2. Crear archivo CSV
        String filename = "provincias_coleccion_" + coleccionId + "_" + 
                         LocalDateTime.now().format(DATE_FORMATTER) + ".csv";
        Path filePath = Paths.get(CSV_DIR + filename);
        
        // 3. Crear directorio si no existe
        Files.createDirectories(filePath.getParent());
        
        // 4. Escribir CSV
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Header
            writer.append("Provincia,Cantidad_Hechos,Porcentaje,Fecha_Calculo,Coleccion_ID\n");
            
            // Data
            writer.append(String.format("%s,%d,%.2f,%s,%d\n",
                stats.getProvincia(),
                stats.getCantidadHechos(),
                stats.getPorcentaje(),
                stats.getFechaCalculo().toString(),
                stats.getColeccionId()
            ));
        }
        
        log.info("Archivo CSV creado: {}", filePath);
        return new UrlResource(filePath.toUri());
    }
    
    /**
     * Exportar estadísticas de categorías a CSV
     */
    public Resource exportarCategoriasCSV() throws IOException {
        log.info("Exportando estadísticas de categorías");
        
        // 1. Obtener estadísticas
        CategoriaStatsDTO stats = statsService.getCategoriaConMasHechos();
        
        // 2. Crear archivo CSV
        String filename = "categorias_" + LocalDateTime.now().format(DATE_FORMATTER) + ".csv";
        Path filePath = Paths.get(CSV_DIR + filename);
        
        // 3. Crear directorio si no existe
        Files.createDirectories(filePath.getParent());
        
        // 4. Escribir CSV
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Header
            writer.append("Categoria,Cantidad_Hechos,Porcentaje,Fecha_Calculo\n");
            
            // Data
            writer.append(String.format("%s,%d,%.2f,%s\n",
                stats.getCategoria(),
                stats.getCantidadHechos(),
                stats.getPorcentaje(),
                stats.getFechaCalculo().toString()
            ));
        }
        
        log.info("Archivo CSV creado: {}", filePath);
        return new UrlResource(filePath.toUri());
    }
    
    /**
     * Exportar estadísticas completas a CSV
     */
    public Resource exportarCompletoCSV() throws IOException {
        log.info("Exportando estadísticas completas");
        
        // 1. Crear archivo CSV
        String filename = "estadisticas_completas_" + LocalDateTime.now().format(DATE_FORMATTER) + ".csv";
        Path filePath = Paths.get(CSV_DIR + filename);
        
        // 2. Crear directorio si no existe
        Files.createDirectories(filePath.getParent());
        
        // 3. Escribir CSV
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Header
            writer.append("Tipo_Estadistica,Valor_Principal,Cantidad,Porcentaje,Fecha_Calculo,Detalles\n");
            
            // Estadísticas de provincias (ejemplo para colección 1)
            try {
                ProvinciaStatsDTO provinciaStats = statsService.getProvinciaConMasHechos(1L);
                writer.append(String.format("Provincia_Mas_Hechos,%s,%d,%.2f,%s,Coleccion_1\n",
                    provinciaStats.getProvincia(),
                    provinciaStats.getCantidadHechos(),
                    provinciaStats.getPorcentaje(),
                    provinciaStats.getFechaCalculo().toString()
                ));
            } catch (Exception e) {
                writer.append("Provincia_Mas_Hechos,Sin_datos,0,0.00," + LocalDateTime.now() + ",Error\n");
            }
            
            // Estadísticas de categorías
            try {
                CategoriaStatsDTO categoriaStats = statsService.getCategoriaConMasHechos();
                writer.append(String.format("Categoria_Mas_Hechos,%s,%d,%.2f,%s,Sistema\n",
                    categoriaStats.getCategoria(),
                    categoriaStats.getCantidadHechos(),
                    categoriaStats.getPorcentaje(),
                    categoriaStats.getFechaCalculo().toString()
                ));
            } catch (Exception e) {
                writer.append("Categoria_Mas_Hechos,Sin_datos,0,0.00," + LocalDateTime.now() + ",Error\n");
            }
            
            // Estadísticas de spam
            try {
                SpamStatsDTO spamStats = statsService.getSpamEliminaciones();
                writer.append(String.format("Spam_Eliminaciones,Total_%d,%d,%.2f,%s,Spam_%d_NoSpam_%d\n",
                    spamStats.getTotalSolicitudes(),
                    spamStats.getSolicitudesSpam(),
                    spamStats.getPorcentajeSpam(),
                    spamStats.getFechaCalculo().toString(),
                    spamStats.getSolicitudesSpam(),
                    spamStats.getSolicitudesNoSpam()
                ));
            } catch (Exception e) {
                writer.append("Spam_Eliminaciones,Sin_datos,0,0.00," + LocalDateTime.now() + ",Error\n");
            }
        }
        
        log.info("Archivo CSV completo creado: {}", filePath);
        return new UrlResource(filePath.toUri());
    }
    
    /**
     * Limpiar archivos CSV antiguos (más de 7 días)
     */
    public void limpiarArchivosAntiguos() {
        try {
            Path csvDir = Paths.get(CSV_DIR);
            if (Files.exists(csvDir)) {
                Files.walk(csvDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant()
                                .isBefore(LocalDateTime.now().minusDays(7).atZone(java.time.ZoneId.systemDefault()).toInstant());
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Archivo CSV antiguo eliminado: {}", path);
                        } catch (IOException e) {
                            log.warn("Error al eliminar archivo CSV: {}", path);
                        }
                    });
            }
        } catch (IOException e) {
            log.error("Error al limpiar archivos CSV antiguos: {}", e.getMessage());
        }
    }
}
