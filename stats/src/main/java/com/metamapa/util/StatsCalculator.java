package com.metamapa.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilidades para cálculos de estadísticas
 */
@Component
@Slf4j
public class StatsCalculator {
    
    /**
     * Calcular porcentaje de un valor sobre el total
     */
    public static Double calcularPorcentaje(Long valor, Long total) {
        if (total == null || total == 0) {
            return 0.0;
        }
        return (valor * 100.0) / total;
    }
    
    /**
     * Calcular porcentajes para una lista de valores
     */
    public static Map<String, Double> calcularPorcentajes(Map<String, Long> valores) {
        Long total = valores.values().stream().mapToLong(Long::longValue).sum();
        
        return valores.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> calcularPorcentaje(entry.getValue(), total)
            ));
    }
    
    /**
     * Obtener el valor máximo de una lista
     */
    public static <T> T obtenerMaximo(List<T> lista) {
        if (lista == null || lista.isEmpty()) {
            return null;
        }
        return lista.get(0); // Asumiendo que la lista está ordenada
    }
    
    /**
     * Calcular total de una lista de valores
     */
    public static Long calcularTotal(List<Long> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0L;
        }
        return valores.stream().mapToLong(Long::longValue).sum();
    }
    
    /**
     * Formatear fecha para logs
     */
    public static String formatearFecha(LocalDateTime fecha) {
        return fecha.toString().replace("T", " ").substring(0, 19);
    }
    
    /**
     * Formatear porcentaje para display
     */
    public static String formatearPorcentaje(Double porcentaje) {
        if (porcentaje == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", porcentaje);
    }
    
    /**
     * Validar si un valor es válido para estadísticas
     */
    public static boolean esValidoParaEstadisticas(Object valor) {
        if (valor == null) {
            return false;
        }
        
        if (valor instanceof String) {
            return !((String) valor).trim().isEmpty();
        }
        
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue() >= 0;
        }
        
        return true;
    }
    
    /**
     * Limpiar texto para estadísticas
     */
    public static String limpiarTexto(String texto) {
        if (texto == null) {
            return "Sin datos";
        }
        
        return texto.trim()
            .replaceAll("[^a-zA-Z0-9\\s]", "") // Remover caracteres especiales
            .replaceAll("\\s+", " ") // Normalizar espacios
            .trim();
    }
    
    /**
     * Calcular estadísticas de tendencia
     */
    public static String calcularTendencia(Long valorActual, Long valorAnterior) {
        if (valorAnterior == null || valorAnterior == 0) {
            return "Nueva";
        }
        
        double cambio = ((double) (valorActual - valorAnterior) / valorAnterior) * 100;
        
        if (cambio > 10) {
            return "Alza significativa";
        } else if (cambio > 0) {
            return "Alza";
        } else if (cambio < -10) {
            return "Baja significativa";
        } else if (cambio < 0) {
            return "Baja";
        } else {
            return "Estable";
        }
    }
}
