package com.metamapa.entities.criterioDePertenencia;

import com.metamapa.entities.hechos.Hecho;

public class CriterioTitulo implements Criterio {
    private String tituloContiene;

    public CriterioTitulo(String tituloContiene) {
        this.tituloContiene = tituloContiene.toLowerCase(); // para comparación case-insensitive
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        if (hecho.getTitulo() == null) return false;
        return hecho.getTitulo().toLowerCase().contains(tituloContiene.toLowerCase());
    }

    public String getTituloContiene() {
        return tituloContiene;
    }
}
