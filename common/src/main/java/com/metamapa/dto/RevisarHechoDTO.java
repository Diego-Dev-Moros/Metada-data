package com.metamapa.dto;

import lombok.Data;
import com.metamapa.entities.hechos.EstadoRevision;

@Data
public class RevisarHechoDTO {
    private EstadoRevision estado; // enum
    private String sugerencia;              // opcional
}

