package com.metamapa.entities.criterioDePertenencia;

import com.metamapa.entities.hechos.Hecho;

import java.time.LocalDateTime;

public class CriterioFechaCarga implements Criterio {
    private LocalDateTime fechaCargaDesde;
    private LocalDateTime fechaCargaHasta;

    public CriterioFechaCarga(LocalDateTime fechaCargaDesde, LocalDateTime fechaCargaHasta) {
        this.fechaCargaDesde = fechaCargaDesde;
        this.fechaCargaHasta = fechaCargaHasta;
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        LocalDateTime fecha = hecho.getFechaCarga();
        return (fecha.isEqual(fechaCargaDesde) || fecha.isAfter(fechaCargaDesde)) &&
                (fecha.isEqual(fechaCargaHasta) || fecha.isBefore(fechaCargaHasta));
    }

    public LocalDateTime getFechaCargaDesde() {
        return fechaCargaDesde;
    }

    public LocalDateTime getFechaCargaHasta() {
        return fechaCargaHasta;
    }
}
