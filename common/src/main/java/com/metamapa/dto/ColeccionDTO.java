package com.metamapa.dto;

import com.metamapa.entities.rol.Contribuyente;
import lombok.Data;
import java.util.List;

@Data
public class ColeccionDTO {
    private String titulo;
    private String descripcion;
    private Long identificador;
    private List<String> fuentes; // ids o nombres de fuentes
    private List<HechoDTO> hechos; // todos los hechos
    private List<HechoDTO> hechosConsensuados; // solo hechos consensuados (para compatibilidad)
    private String algoritmoDeConsenso; // nombre del algoritmo
    private ContribuyenteDTO administrador; // nombre o id del admin
    private List<String> criterios; // descripciones o ids de criterios
}
