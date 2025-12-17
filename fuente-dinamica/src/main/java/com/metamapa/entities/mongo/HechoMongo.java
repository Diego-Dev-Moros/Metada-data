package com.metamapa.entities.mongo;

import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.OrigenHecho;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad MongoDB para almacenar hechos en la fuente dinámica.
 * Optimizada para NoSQL - estructura plana y flexible.
 */
@Data
@NoArgsConstructor
@Document(collection = "hechos")
public class HechoMongo {
    
    @Id
    private Long id; // Usar Long para compatibilidad con JPA
    
    @Field("titulo")
    private String titulo;
    
    @Field("descripcion")
    private String descripcion;
    
    @Field("categoria")
    private String categoria;
    
    // Ubicación embebida (no relación)
    @Field("ubicacion")
    private UbicacionMongo ubicacion;
    
    // Lista de etiquetas (simple)
    @Field("etiquetas")
    private List<String> etiquetas = new ArrayList<>();
    
    @Field("fecha_hecho")
    private LocalDateTime fechaHecho;
    
    @Field("fecha_carga")
    private LocalDateTime fechaCarga = LocalDateTime.now();
    
    @Field("origen")
    private OrigenHecho origen;
    
    // Contribuyente embebido (no relación)
    @Field("contribuyente")
    private ContribuyenteMongo contribuyente;
    
    // Multimedia embebido (no relación)
    @Field("multimedias")
    private List<MultimediaMongo> multimedias = new ArrayList<>();
    
    @Field("eliminado")
    private boolean eliminado = false;
    
    @Field("estado_revision")
    private EstadoRevision estadoRevision = EstadoRevision.PENDIENTE;
    
    @Field("sugerencia_cambio")
    private String sugerenciaDeCambio;
    
    @Field("motivo_rechazo")
    private String motivoRechazo;
    
    @Field("es_anonimo")
    private boolean esAnonimo = false;
    
    @Field("contador")
    private int contador = 0;
    
    @Field("fingerprint")
    private String fingerprint;
    
    @Field("fuentes")
    private List<String> fuentes = new ArrayList<>();
    
    // Constructor para crear hecho básico
    public HechoMongo(String titulo, String descripcion, String categoria) {
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
    
    public void agregarMultimedia(MultimediaMongo m) {
        if (m != null && m.esValido()) {
            multimedias.add(m);
        }
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