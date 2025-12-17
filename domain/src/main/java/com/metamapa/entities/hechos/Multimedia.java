package com.metamapa.entities.hechos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data

@Entity
@Table(name = "Multimedia")
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_hecho", referencedColumnName = "id")
    @JsonIgnore
    private Hecho hecho;

    private String nombre;
    private String tipo;
    private String formato;
    private long tamanio;
    private String ruta;
    private LocalDateTime fechaCarga = LocalDateTime.now();

    public boolean esValido() {
        return nombre != null && !nombre.isEmpty() && tipo != null && !tipo.isEmpty() && formato != null && !formato.isEmpty() && tamanio > 0 && ruta != null && !ruta.isEmpty();
    }

    public String getTamanioFormateado() {
        if (tamanio < 1024) return tamanio + " B";
        else if (tamanio < 1024 * 1024) return String.format("%.2f KB", tamanio / 1024.0);
        else if (tamanio < 1024 * 1024 * 1024) return String.format("%.2f MB", tamanio / (1024.0 * 1024.0));
        else return String.format("%.2f GB", tamanio / (1024.0 * 1024.0 * 1024.0));
    }
}