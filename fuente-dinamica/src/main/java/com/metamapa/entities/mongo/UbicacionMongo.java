package com.metamapa.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Ubicación embebida para MongoDB - estructura simple y plana
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionMongo {
    
    @Field("latitud")
    private Double latitud;
    
    @Field("longitud")
    private Double longitud;
    
    @Field("pais")
    private String pais;
    
    @Field("provincia")
    private String provincia;
    
    @Field("municipio")
    private String municipio;
    
    @Field("direccion")
    private String direccion;
    
    public UbicacionMongo(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }
    
    public boolean esValida() {
        return latitud != null && longitud != null &&
               latitud >= -90 && latitud <= 90 &&
               longitud >= -180 && longitud <= 180;
    }
    
    public boolean tieneDetallesLugar() {
        return pais != null || provincia != null || municipio != null;
    }
}