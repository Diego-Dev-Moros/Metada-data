package com.metamapa.entities.criterioDePertenencia;


import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.ubicaciones.Lugar;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CriterioUbicacion implements Criterio {

    private Lugar lugar;

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        Lugar lugarHecho = hecho.getUbicacion().convertirALugar();

        if (lugar.getPais() != null && !lugar.getPais().equalsIgnoreCase(lugarHecho.getPais())) {
            return false;
        }
        if (lugar.getProvincia() != null && !lugar.getProvincia().equalsIgnoreCase(lugarHecho.getProvincia())) {
            return false;
        }
        if (lugar.getMunicipio() != null && !lugar.getMunicipio().equalsIgnoreCase(lugarHecho.getMunicipio())) {
            return false;
        }
        return true;
    }

    public Lugar getLugar() {
        return lugar;
    }
}
