package com.metamapa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración específica del servicio de estadísticas
 */
@Configuration
@ConfigurationProperties(prefix = "stats")
@Data
public class StatsConfig {
    
    /**
     * Expresión cron para el scheduler (cada 3 horas por defecto)
     */
    private String cronExpression = "0 0 */3 * * ?";
    
    /**
     * Si el scheduler está habilitado
     */
    private boolean enabled = true;
    
    /**
     * TTL del caché en horas
     */
    private int cacheTtlHours = 3;
    
    /**
     * Archivo para guardar la última actualización
     */
    private String lastUpdateFile = "stats-last-update.txt";
    
    /**
     * Configuración de exportación CSV
     */
    private CsvConfig csv = new CsvConfig();
    
    @Data
    public static class CsvConfig {
        private String exportPath = "/tmp/stats-exports/";
        private String filenamePrefix = "metamapa-stats-";
    }
}
