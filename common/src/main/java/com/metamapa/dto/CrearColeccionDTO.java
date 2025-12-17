package com.metamapa.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class CrearColeccionDTO {
    @NotNull @NotEmpty
    private String titulo;
    
    @NotNull
    private String descripcion;
    
    private Long identificador;
    
    @NotNull
    private String algoritmoConsenso;
    
    private List<CriterioDTO> criterios = new ArrayList<>();
    
    // Lista de identificadores de fuentes a agregar a la colección (opcional)
    // Ejemplo: ["fuente-dinamica-client", "fuente-estatica-client"]
    private List<String> fuenteIds = new ArrayList<>();
}

