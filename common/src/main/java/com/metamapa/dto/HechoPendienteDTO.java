package com.metamapa.dto;

import com.metamapa.entities.hechos.EstadoRevision;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para responder hechos pendientes con su ID de MongoDB
 * Este DTO se usa para que el admin pueda aprobar/rechazar hechos usando el mongoId
 */
@Data
public class HechoPendienteDTO {
    private String mongoId; // ID en MongoDB
    private String titulo;
    private String descripcion;
    private String categoria;
    private UbicacionDTO ubicacion;
    private List<String> etiquetas;
    private LocalDateTime fechaHecho;
    private LocalDateTime fechaCarga;
    private EstadoRevision estadoRevision;
    private String sugerenciaDeCambio;
    private boolean esAnonimo;
    private boolean eliminado;
}
