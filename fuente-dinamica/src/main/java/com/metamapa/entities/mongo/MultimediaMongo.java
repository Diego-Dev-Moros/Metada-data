package com.metamapa.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Multimedia embebida para MongoDB - estructura simple
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaMongo {
    
    @Field("url")
    private String url;
    
    @Field("tipo")
    private String tipo; // "imagen", "video", "audio", "documento"
    
    @Field("titulo")
    private String titulo;
    
    @Field("descripcion")
    private String descripcion;
    
    @Field("tamaño")
    private Long tamanio;
    
    @Field("formato")
    private String formato; // "jpg", "mp4", "pdf", etc.
    
    public MultimediaMongo(String url, String tipo) {
        this.url = url;
        this.tipo = tipo;
    }
    
    public boolean esValido() {
        return url != null && !url.trim().isEmpty() &&
               tipo != null && !tipo.trim().isEmpty();
    }
    
    public boolean esImagen() {
        return "imagen".equalsIgnoreCase(tipo);
    }
    
    public boolean esVideo() {
        return "video".equalsIgnoreCase(tipo);
    }
    
    public Long getTamanio() {
        return tamanio != null ? tamanio : 0L;
    }
}