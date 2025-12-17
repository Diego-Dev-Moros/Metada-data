package com.metamapa.dto;

import lombok.Data;
import java.util.List;

@Data
public class ModificarColeccionDTO {
    // Algoritmo de consenso (null = sin cambio, "" = sin algoritmo, valor = algoritmo específico)
    private String algoritmoConsenso;
    
    // IDs de fuentes (null = sin cambio, lista vacía = sin fuentes, lista = fuentes específicas)
    private List<String> fuenteIds;
}
