package com.metamapa.entities.ubicaciones;

import lombok.Data;

import javax.persistence.*;

@Data

@Entity
@Table(name = "Ubicacion")
public class Ubicacion {

    @Id
    @GeneratedValue/*(strategy = GenerationType.IDENTITY)*/
    private Long id;

    private double latitud;
    private double longitud;

    @ManyToOne
    @JoinColumn(name = "id_lugar")
    private Lugar lugar;

    public Ubicacion() { this.latitud = 0.0; this.longitud = 0.0; }
    public Ubicacion(double latitud, double longitud) { this.latitud = latitud; this.longitud = longitud; }

    public boolean esValida() { return latitud >= -90 && latitud <= 90 && longitud >= -180 && longitud <= 180; }

    public Lugar convertirALugar() {
        if (!esValida()) throw new IllegalArgumentException("Ubicación inválida");
        if (latitud >= -35 && latitud <= -34 && longitud >= -59 && longitud <= -58) return new Lugar("Argentina", "Buenos Aires", "CABA");
        if (latitud >= -40 && latitud <= -38 && longitud >= -64 && longitud <= -62) return new Lugar("Argentina", "Río Negro", "General Roca");
        return new Lugar("Argentina", "Provincia Desconocida", "Municipio Desconocido");
    }
}