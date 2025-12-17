package com.metamapa.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para actualizar un hecho existente
 * Solo se pueden editar ciertos campos y dentro de los 7 días de creación
 */
@Data
public class ActualizarHechoDTO {
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDateTime fechaHecho;
    private List<String> etiquetas;
    
    // Ubicación
    private Double latitud;
    private Double longitud;
    private String pais;
    private String provincia;
    private String municipio;
}
