package com.metamapa.entities.criterioDePertenencia;

import com.metamapa.entities.hechos.Hecho;

import java.time.LocalDateTime;

public class CriterioFechaAcontecimiento implements Criterio {
    private LocalDateTime fechaInicial;
    private LocalDateTime fechaFinal;

    public CriterioFechaAcontecimiento(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        this.fechaInicial = fechaInicial;
        this.fechaFinal = fechaFinal;
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        LocalDateTime fecha = hecho.getFechaHecho();
        return (fecha.isEqual(fechaInicial) || fecha.isAfter(fechaInicial)) &&
                (fecha.isEqual(fechaFinal) || fecha.isBefore(fechaFinal));
    }

    public LocalDateTime getFechaInicial() {
        return fechaInicial;
    }

    public LocalDateTime getFechaFinal() {
        return fechaFinal;
    }
}
