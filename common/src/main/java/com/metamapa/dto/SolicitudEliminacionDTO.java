package com.metamapa.dto;

import com.metamapa.entities.solicitudes.EstadoSolicitud;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SolicitudEliminacionDTO {
    private Long id;
    private String motivo;
    private HechoDTO hecho;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
    private boolean esSpam;
    private ContribuyenteDTO contribuyente;
}
