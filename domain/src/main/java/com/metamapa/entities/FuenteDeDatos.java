package com.metamapa.entities;

import com.metamapa.entities.hechos.Hecho;

import java.util.List;

public interface FuenteDeDatos {
    List<Hecho> obtenerHechos();
    Hecho obtenerHechoPorId(Long id);
    void agregarHecho(Hecho hecho);
    void eliminarHecho(String titulo);
    String getTipo();
    String getIdentificador();
}