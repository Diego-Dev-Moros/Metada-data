package com.metamapa.domain;

import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.OrigenHecho;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Hecho específico para fuente-dinamica
 * Usa String IDs y no depende del dominio central
 */
@Document(collection = "hechos")
@Data
@NoArgsConstructor
public class HechoDinamico {
    
    @Id
    private String id; // MongoDB ObjectId
    
    @Field("titulo")
    private String titulo;
    
    @Field("descripcion")
    private String descripcion;
    
    @Field("categoria")
    private String categoria;
    
    @Field("ubicacion")
    private UbicacionDinamica ubicacion;
    
    @Field("etiquetas")
    private List<String> etiquetas;
    
    @Field("fechaHecho")
    private LocalDateTime fechaHecho;
    
    @Field("fechaCarga")
    private LocalDateTime fechaCarga;
    
    @Field("origen")
    private OrigenHecho origen;
    
    @Field("contribuyente")
    private ContribuyenteDinamico contribuyente;
    
    @Field("multimedias")
    private List<MultimediaDinamica> multimedias;
    
    @Field("eliminado")
    private boolean eliminado = false;
    
    @Field("estadoRevision")
    private EstadoRevision estadoRevision = EstadoRevision.PENDIENTE;
    
    @Field("sugerenciaDeCambio")
    private String sugerenciaDeCambio;
    
    @Field("motivoRechazo")
    private String motivoRechazo;
    
    @Field("esAnonimo")
    private boolean esAnonimo = false;
    
    // Constructor básico
    public HechoDinamico(String titulo, String descripcion, String categoria) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaCarga = LocalDateTime.now();
        this.estadoRevision = EstadoRevision.PENDIENTE;
    }
    
    // Métodos de utilidad
    public boolean tieneUbicacion() {
        return ubicacion != null && ubicacion.esValida();
    }
    
    public boolean tieneContenidoMultimedia() {
        return multimedias != null && !multimedias.isEmpty();
    }
    
    public boolean esReciente() {
        return fechaHecho != null && fechaHecho.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    public void marcarComoEliminado() {
        this.eliminado = true;
    }
    
    public void aceptarRevision() {
        this.estadoRevision = EstadoRevision.ACEPTADO;
    }
    
    public void rechazarRevision(String motivo) {
        this.estadoRevision = EstadoRevision.RECHAZADO;
        this.motivoRechazo = motivo;
    }
    
    public void aceptarRevisionConSugerencia(String sugerencia) {
        this.estadoRevision = EstadoRevision.ACEPTADO_CON_SUGERENCIAS;
        // Concatenar sugerencias si ya hay alguna
        if (this.sugerenciaDeCambio != null && !this.sugerenciaDeCambio.trim().isEmpty()) {
            this.sugerenciaDeCambio = this.sugerenciaDeCambio + "\n---\n" + sugerencia;
        } else {
            this.sugerenciaDeCambio = sugerencia;
        }
    }
}