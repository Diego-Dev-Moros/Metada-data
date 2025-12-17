package com.metamapa.entities.colecciones;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.metamapa.entities.hechos.Hecho;
import java.util.*;
import java.util.stream.Collectors;

@JsonTypeName("multiples_menciones")
public class MultiplesMenciones implements AlgoritmoDeConsenso {
    
    @Override
    public List<Hecho> filtrarConsensuados(List<Hecho> hechos, List<List<Hecho>> hechosPorFuente) {
        if (hechosPorFuente.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, Integer> conteoHechos = new HashMap<>();
        Map<String, Hecho> hechosUnicos = new HashMap<>();
        
        // Contar ocurrencias de cada hecho por título
        for (List<Hecho> hechosFuente : hechosPorFuente) {
            for (Hecho hecho : hechosFuente) {
                String titulo = hecho.getTitulo();
                conteoHechos.put(titulo, conteoHechos.getOrDefault(titulo, 0) + 1);
                hechosUnicos.put(titulo, hecho);
            }
        }
        
        // Filtrar hechos que aparecen en al menos 2 fuentes
        return hechosUnicos.values().stream()
                .filter(hecho -> conteoHechos.get(hecho.getTitulo()) >= 2)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MultiplesMenciones;
    }

    @Override
    public int hashCode() {
        return MultiplesMenciones.class.hashCode();
    }
}