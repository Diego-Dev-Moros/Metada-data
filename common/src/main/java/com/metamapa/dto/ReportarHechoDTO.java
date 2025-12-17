package com.metamapa.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReportarHechoDTO {
    private String titulo;
    private String descripcion;
    private String categoria;
    private List<String> etiquetas;
    private double latitud;
    private double longitud;
    private String fechaHecho;
    private Boolean esAnonimo; // Indica si el reporte es anónimo
    // No incluir datos del contribuyente - se envían por header
}
