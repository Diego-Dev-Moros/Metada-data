package com.metamapa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColeccionResponseDTO {
    private String titulo;
    private String descripcion;
    private Long identificador;
    private List<HechoDTO> hechos;
    private List<HechoDTO> hechosConsensuados;
    private String algoritmoDeConsenso; // solo el tipo del algoritmo
    private int totalHechos;
    private int totalHechosConsensuados;
}
