package com.metamapa.dto;

import com.metamapa.entities.hechos.EstadoRevision;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para hechos de la fuente dinámica
 * Usa String ID (MongoDB ObjectId)
 */
@Data
public class HechoDinamicoDTO {
    private String id; // MongoDB ObjectId
    private String titulo;
    private String descripcion;
    private String categoria;
    private UbicacionDTO ubicacion;
    private List<String> etiquetas;
    private List<MultimediaDTO> multimedias;
    private ContribuyenteDTO contribuyente;
    private LocalDateTime fechaHecho;
    private LocalDateTime fechaCarga;
    private EstadoRevision estadoRevision; // PENDIENTE, ACEPTADO, RECHAZADO
    private String sugerenciaDeCambio;
    private boolean esAnonimo;
    private boolean eliminado;
}
