package com.metamapa.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Multimedia específica para fuente-dinamica
 */
@Data
@NoArgsConstructor
public class MultimediaDinamica {
    
    @Field("id")
    private String id; // MongoDB ObjectId
    
    @Field("nombre")
    private String nombre;
    
    @Field("tipo")
    private String tipo; // imagen, video, audio, documento
    
    @Field("url")
    private String url;
    
    @Field("rutaArchivo")
    private String rutaArchivo;
    
    @Field("descripcion")
    private String descripcion;
    
    @Field("tamaño")
    private Long tamaño;
    
    @Field("formatoMime")
    private String formatoMime;
    
    @Field("eliminado")
    private boolean eliminado = false;
    
    // Constructor básico
    public MultimediaDinamica(String nombre, String tipo, String url) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.url = url;
    }
    
    // Constructor con archivo
    public MultimediaDinamica(String nombre, String tipo, String rutaArchivo, String formatoMime, Long tamaño) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.rutaArchivo = rutaArchivo;
        this.formatoMime = formatoMime;
        this.tamaño = tamaño;
    }
    
    // Métodos de utilidad
    public boolean esImagen() {
        return "imagen".equalsIgnoreCase(tipo) || 
               (formatoMime != null && formatoMime.startsWith("image/"));
    }
    
    public boolean esVideo() {
        return "video".equalsIgnoreCase(tipo) ||
               (formatoMime != null && formatoMime.startsWith("video/"));
    }
    
    public boolean esAudio() {
        return "audio".equalsIgnoreCase(tipo) ||
               (formatoMime != null && formatoMime.startsWith("audio/"));
    }
    
    public boolean esDocumento() {
        return "documento".equalsIgnoreCase(tipo) ||
               (formatoMime != null && (formatoMime.contains("pdf") || 
                                      formatoMime.contains("document") ||
                                      formatoMime.contains("text")));
    }
    
    public boolean tieneArchivo() {
        return rutaArchivo != null && !rutaArchivo.trim().isEmpty();
    }
    
    public boolean tieneUrl() {
        return url != null && !url.trim().isEmpty();
    }
    
    public boolean esValido() {
        return (nombre != null && !nombre.trim().isEmpty()) &&
               (tipo != null && !tipo.trim().isEmpty()) &&
               (tieneArchivo() || tieneUrl());
    }
    
    public void marcarComoEliminado() {
        this.eliminado = true;
    }
    
    public String getTamañoFormateado() {
        if (tamaño == null) return "Desconocido";
        
        if (tamaño < 1024) return tamaño + " B";
        if (tamaño < 1024 * 1024) return String.format("%.1f KB", tamaño / 1024.0);
        if (tamaño < 1024 * 1024 * 1024) return String.format("%.1f MB", tamaño / (1024.0 * 1024));
        return String.format("%.1f GB", tamaño / (1024.0 * 1024 * 1024));
    }
}