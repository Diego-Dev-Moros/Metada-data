package com.metamapa.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Ubicación específica para fuente-dinamica
 * Solo coordenadas - la transformación a pais/provincia/municipio se hace después
 */
@Data
@NoArgsConstructor
public class UbicacionDinamica {
    
    @Field("latitud")
    private Double latitud;
    
    @Field("longitud")
    private Double longitud;
    
    // Constructor con coordenadas
    public UbicacionDinamica(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
    
    // Métodos de utilidad
    public boolean esValida() {
        return latitud != null && longitud != null;
    }
    
    public boolean tieneCoordenadas() {
        return latitud != null && longitud != null;
    }
    
    public String getCoordenadas() {
        if (!tieneCoordenadas()) {
            return "Sin coordenadas";
        }
        return String.format("%.6f, %.6f", latitud, longitud);
    }
}