package com.metamapa.service;

import com.metamapa.client.Conexion;
import com.metamapa.entities.FuenteDeDatos;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.ubicaciones.Ubicacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FuenteDemoService implements FuenteDeDatos {
    private final String identificador = "fuente-demo";
    private final Conexion conexion = new Conexion();
    private final List<Hecho> hechos = new ArrayList<>();
    private LocalDateTime fechaUltimaConsulta = null;
    private final URL urlDemo;

    public FuenteDemoService() {
        try {
            this.urlDemo = new URL("http://demo.fuente-externa.org");
        } catch (Exception e) {
            throw new RuntimeException("URL demo inválida", e);
        }
    }

    @Override
    public List<Hecho> obtenerHechos() {
        return new ArrayList<>(hechos);
    }

    @Override
    public Hecho obtenerHechoPorId(Long id) {
        return hechos.stream()
                .filter(h -> id != null && id.equals(h.getId()))
                .findFirst().get();
    }

    @Override
    public void agregarHecho(Hecho hecho) {
        if (hecho != null && hecho.getTitulo() != null) {
            hechos.removeIf(h -> h.getTitulo().equals(hecho.getTitulo()));
            hechos.add(hecho);
        }
    }

    @Override
    public void eliminarHecho(String titulo) {
        hechos.removeIf(h -> h.getTitulo().equals(titulo));
    }

    @Override
    public String getTipo() {
        return "DEMO";
    }

    @Override
    public String getIdentificador() {
        return identificador;
    }

    public void consultarNuevosHechos() {
        boolean hayNuevos = false;
        while (true) {
            Map<String, Object> datos = conexion.siguienteHecho(urlDemo, fechaUltimaConsulta);
            if (datos == null) break;
            Hecho hecho = mapearHecho(datos);
            if (hecho != null) {
                agregarHecho(hecho);
                hayNuevos = true;
            }
        }
        if (hayNuevos) {
            fechaUltimaConsulta = LocalDateTime.now();
            log.info("Nuevos hechos consultados de la fuente demo");
        }
    }

    private Hecho mapearHecho(Map<String, Object> datos) {
        try {
            String titulo = (String) datos.get("titulo");
            String descripcion = (String) datos.get("descripcion");
            String categoria = (String) datos.get("categoria");
            double latitud = (double) datos.get("latitud");
            double longitud = (double) datos.get("longitud");
            LocalDateTime fechaHecho = (LocalDateTime) datos.get("fechaHecho");
            Ubicacion ubicacion = new Ubicacion(latitud, longitud);
            return new Hecho(titulo, descripcion, categoria, ubicacion, fechaHecho, OrigenHecho.CARGA_MANUAL, null, null);
        } catch (Exception e) {
            log.warn("Error al mapear hecho demo: {}", e.getMessage());
            return null;
        }
    }
} 