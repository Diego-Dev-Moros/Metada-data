package com.metamapa.entities.colecciones;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.metamapa.entities.criterioDePertenencia.Criterio;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.ubicaciones.Lugar;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "coleccion_criterio")
@JsonIgnoreProperties({"coleccion"}) // Evita referencia circular al serializar
public class ColeccionCriterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore // No serializar la referencia a la colección (evita ciclo)
    @ManyToOne
    @JoinColumn(name = "id_coleccion", referencedColumnName = "identificador")
    private Coleccion coleccion;

    @Column(name = "tipo_criterio", nullable = false)
    private String tipoCriterio; // "CATEGORIA", "TITULO", "UBICACION", "FECHA_CARGA", "FECHA_ACONTECIMIENTO", "DESCRIPCION"

    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor; // El valor del criterio (ej: "tecnologia", "Buenos Aires", etc.)

    @Column(name = "parametros", columnDefinition = "TEXT")
    private String parametros; // Parámetros adicionales en formato JSON si es necesario

    // Campos específicos para criterios de fecha
    @Column(name = "fecha_desde")
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta")
    private LocalDateTime fechaHasta;

    // Campos específicos para criterios de ubicación
    @Column(name = "pais")
    private String pais;

    @Column(name = "provincia")
    private String provincia;

    @Column(name = "municipio")
    private String municipio;

    public ColeccionCriterio(Coleccion coleccion, String tipoCriterio, String valor) {
        this.coleccion = coleccion;
        this.tipoCriterio = tipoCriterio;
        this.valor = valor;
    }

    // Constructor para criterios de fecha
    public ColeccionCriterio(Coleccion coleccion, String tipoCriterio, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.coleccion = coleccion;
        this.tipoCriterio = tipoCriterio;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    // Constructor para criterios de ubicación
    public ColeccionCriterio(Coleccion coleccion, String tipoCriterio, String pais, String provincia, String municipio) {
        this.coleccion = coleccion;
        this.tipoCriterio = tipoCriterio;
        this.pais = pais;
        this.provincia = provincia;
        this.municipio = municipio;
    }

    public Criterio toCriterio() {
        switch (this.tipoCriterio) {
            case "CATEGORIA":
                return hecho -> hecho.getCategoria() != null &&
                        hecho.getCategoria().equalsIgnoreCase(this.valor);

            case "TITULO":
                return hecho -> hecho.getTitulo() != null &&
                        hecho.getTitulo().toLowerCase().contains(this.valor.toLowerCase());

            case "DESCRIPCION":
                return hecho -> hecho.getDescripcion() != null &&
                        hecho.getDescripcion().toLowerCase().contains(this.valor.toLowerCase());

            case "FECHA_CARGA":
                return hecho -> {
                    if (hecho.getFechaCarga() == null || this.fechaDesde == null || this.fechaHasta == null)
                        return false;
                    LocalDateTime fecha = hecho.getFechaCarga();
                    return (fecha.isEqual(this.fechaDesde) || fecha.isAfter(this.fechaDesde))
                            && (fecha.isEqual(this.fechaHasta) || fecha.isBefore(this.fechaHasta));
                };

            case "FECHA_ACONTECIMIENTO":
                return hecho -> {
                    if (hecho.getFechaHecho() == null || this.fechaDesde == null || this.fechaHasta == null)
                        return false;
                    LocalDateTime fecha = hecho.getFechaHecho();
                    return (fecha.isEqual(this.fechaDesde) || fecha.isAfter(this.fechaDesde))
                            && (fecha.isEqual(this.fechaHasta) || fecha.isBefore(this.fechaHasta));
                };

            case "UBICACION":
                return hecho -> {
                    if (hecho.getUbicacion() == null) return false;
                    try {
                        Lugar lugarHecho = hecho.getUbicacion().convertirALugar();

                        if (this.pais != null && !this.pais.equalsIgnoreCase(lugarHecho.getPais())) return false;
                        if (this.provincia != null && !this.provincia.equalsIgnoreCase(lugarHecho.getProvincia())) return false;
                        if (this.municipio != null && !this.municipio.equalsIgnoreCase(lugarHecho.getMunicipio())) return false;

                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                };

            default:
                return hecho -> true; // si no se reconoce el tipo, acepta todos los hechos
        }
    }

}