package com.metamapa.dto;
import com.metamapa.entities.hechos.Multimedia;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EditarHechoDTO {
    private String nuevoTitulo;
    private String nuevaDescripcion;
    private String nuevaCategoria;
    private Double nuevaLatitud;
    private Double nuevaLongitud;
    private LocalDateTime nuevaFechaDelHecho;
    private List<Multimedia> nuevasMultimedias;
}