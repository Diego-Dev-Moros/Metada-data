package com.metamapa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para multimedia (usado para comunicación entre servicios)
 * Compatible con MultimediaMongo (MongoDB) y Multimedia (MySQL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaDTO {
    
    private String url;      // URL/ruta del archivo
    private String tipo;     // "imagen", "video", "audio", "documento"
    private String titulo;   // Título opcional
    private String descripcion; // Descripción opcional
    private Long tamanio;    // Tamaño en bytes
    private String formato;  // Extensión: "jpg", "mp4", "pdf", etc.
    private LocalDateTime fechaCarga; // Fecha de carga
    
    // Constructor simplificado para conversión desde MongoDB
    public MultimediaDTO(String url, String tipo) {
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
}
