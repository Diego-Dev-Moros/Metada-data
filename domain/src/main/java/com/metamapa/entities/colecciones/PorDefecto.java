package com.metamapa.entities.colecciones;

import com.metamapa.entities.hechos.Hecho;
import java.util.List;

/**
 * Algoritmo de consenso por defecto
 * Acepta todos los hechos sin aplicar ningún filtro
 * Se usa cuando no se especifica ningún algoritmo de consenso
 */
public class PorDefecto implements AlgoritmoDeConsenso {
    
    @Override
    public List<Hecho> filtrarConsensuados(List<Hecho> hechos, List<List<Hecho>> hechosPorFuente) {
        // Retorna todos los hechos sin filtrar
        return hechos;
    }
    
    @Override
    public String toString() {
        return "Por Defecto (todos los hechos)";
    }
}
