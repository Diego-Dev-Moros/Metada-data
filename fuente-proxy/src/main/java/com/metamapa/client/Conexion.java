package com.metamapa.client;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Conexion {
    private int contador = 0;

    /**
     * Devuelve un mapa con los atributos de un hecho, indexados por nombre de atributo.
     * Si el método retorna null, significa que no hay nuevos hechos por ahora.
     * La fecha es opcional.
     */
    public Map<String, Object> siguienteHecho(URL url, LocalDateTime fechaUltimaConsulta) {
        // Simulación: cada llamada devuelve un hecho diferente hasta 3 hechos, luego null
        if (contador >= 3) return null;
        Map<String, Object> hecho = new HashMap<>();
        hecho.put("titulo", "Hecho demo " + (contador + 1));
        hecho.put("descripcion", "Descripción del hecho demo " + (contador + 1));
        hecho.put("categoria", "Demo");
        hecho.put("latitud", -34.60 + contador);
        hecho.put("longitud", -58.38 + contador);
        hecho.put("fechaHecho", fechaUltimaConsulta != null ? fechaUltimaConsulta.plusHours(contador + 1) : LocalDateTime.now());
        contador++;
        return hecho;
    }
} 