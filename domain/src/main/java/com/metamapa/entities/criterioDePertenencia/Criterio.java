package com.metamapa.entities.criterioDePertenencia;

import com.metamapa.entities.hechos.Hecho;

@FunctionalInterface
public interface Criterio {
    boolean cumpleCriterio(Hecho hecho);
}
