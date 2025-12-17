package com.metamapa.entities.criterioDePertenencia;

import com.metamapa.entities.hechos.Hecho;

public class CriterioDescripcion implements Criterio {
    private String descripcion;

    public CriterioDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        if (hecho.getDescripcion() == null) return false;
        return hecho.getDescripcion().toLowerCase().contains(descripcion.toLowerCase());
    }

    public String getDescripcion() {
        return descripcion;
    }
}
