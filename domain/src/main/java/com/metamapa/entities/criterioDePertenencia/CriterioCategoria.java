package com.metamapa.entities.criterioDePertenencia;

import com.metamapa.entities.hechos.Hecho;

public class CriterioCategoria implements Criterio {

    private String categoria;

    public CriterioCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        return hecho.getCategoria() != null && hecho.getCategoria().equalsIgnoreCase(categoria);
    }

    public String getCategoria() {
        return categoria;
    }
}

