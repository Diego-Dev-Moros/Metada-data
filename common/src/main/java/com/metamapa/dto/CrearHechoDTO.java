package com.metamapa.dto;

import com.metamapa.entities.hechos.Multimedia;
import lombok.Data;
import java.util.List;

@Data
public class CrearHechoDTO {
    private String titulo;
    private String descripcion;
    private String categoria;
    private double latitud;
    private double longitud;
    private String fechaHecho; // Cambiado a String para evitar problemas de serialización/deserialización
    private Boolean esAnonimo = false;

    // Datos de contribuyente (opcionales)
    private String nombreContribuyente;
    private String apellidoContribuyente;
    private Integer edadContribuyente;
    private Long idContribuyente; // ID del contribuyente en el agregador (para sincronización)

    // Multimedia opcional
    private List<Multimedia> urlsMultimedia;
}
