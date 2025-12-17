package com.metamapa.dto;

import com.metamapa.entities.hechos.EstadoRevision;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para hechos de la fuente dinámica (usado para comunicación entre servicios)
 * Usa String ID (MongoDB ObjectId)
 */
@Data
public class HechoDinamicoDTO {
    private String id; // MongoDB ObjectId
    private String titulo;
    private String descripcion;
    private String categoria;
    private UbicacionDTO ubicacion; // Cambio de UbicacionDinamicaDTO a UbicacionDTO
    private List<String> etiquetas;
    private LocalDateTime fechaHecho;
    private LocalDateTime fechaCarga;
    private EstadoRevision estadoRevision; // PENDIENTE, ACEPTADO, RECHAZADO
    private String sugerenciaDeCambio;
    private boolean esAnonimo;
    private boolean eliminado;
    private List<MultimediaDTO> multimedias; // Multimedia del hecho
    private ContribuyenteDTO contribuyente; // Contribuyente que creó el hecho
}
