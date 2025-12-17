package com.metamapa.entities.hechos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.ubicaciones.Ubicacion;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Hecho")
public class Hecho {
    // ID asignado por IdGeneratorService del agregador
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    
    @Column(length = 2000)
    private String descripcion;
    
    private String categoria;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_ubicacion", referencedColumnName = "id")
    private Ubicacion ubicacion;

    // se podria hacer una clase aparte pero no creo que sea conveniente o si?
    @ElementCollection
    @CollectionTable(name = "hecho_etiquetas", joinColumns = @JoinColumn(name = "hecho_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetas = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHecho;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name ="origenHecho", nullable = false)
    private OrigenHecho origen;

    @ManyToOne
    @JoinColumn(name = "id_Contribuyente", referencedColumnName = "id")
    private Contribuyente contribuyente;

    @OneToMany(mappedBy = "hecho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Multimedia> multimedias = new ArrayList<>();

    private boolean eliminado = false;

    @Enumerated(EnumType.STRING)
    @Column(name ="estadoRevision", nullable = true)
    private EstadoRevision estadoRevision = null;

    private String sugerenciaDeCambio;

    private boolean esAnonimo = false; // por default es false
    private int contador = 0;                         // cuantas fuentes lo reportaron
    private String fingerprint;                       // clave canonical para detectar duplicados
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hecho_fuentes", joinColumns = @JoinColumn(name = "hecho_id"))
    @Column(name = "fuente")
    private List<String> fuentes = new ArrayList<>(); // fuentes que reportaron este hecho

    /**
     * Campo transitorio (no se persiste en BD) usado durante el procesamiento de archivos.
     * La fuente-estática lo setea para indicar de qué archivo proviene este hecho.
     * El agregador lo usa para crear la relación en la tabla intermedia hecho_origen_archivo.
     */
    @Transient
    private Long origenArchivoId;

    public Hecho() {
        // ID será asignado por IdGeneratorService
        this.id = null;
    }

    public Hecho(String titulo, String descripcion, String categoria) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    public Hecho(String titulo, String descripcion, String categoria, Ubicacion ubicacion, LocalDateTime fechaHecho, OrigenHecho origen, List<String> etiquetas, List<Multimedia> multimedias) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.fechaHecho = fechaHecho;
        this.origen = origen;
        if (etiquetas != null) this.etiquetas = etiquetas;
        if (multimedias != null) this.multimedias = multimedias;
    }

    // Constructor para carga desde Fuente Dinamica (no es necesario pasar el id, si se setea aca)
    /*public Hecho(String titulo, String descripcion, String categoria, Ubicacion ubicacion, LocalDateTime fechaHecho) {
        this();
        this.id = autoIncrementId++;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.fechaHecho = fechaHecho;
    }*/

    // Constructor para mapear hecho desde Fuente Demo
    /*public Hecho(String titulo, String descripcion, String categoria, Ubicacion ubicacion, LocalDateTime fechaHecho, OrigenHecho origen, List<String> etiquetas, List<Multimedia> multimedias) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.ubicacion = ubicacion;
        this.fechaHecho = fechaHecho;
        this.origen = origen;
        if (etiquetas != null) this.etiquetas = etiquetas;
        if (multimedias != null) this.multimedias = multimedias;
    }*/

    public boolean tieneUbicacion() { return ubicacion != null && ubicacion.esValida(); }
    public boolean tieneContenidoMultimedia() { return !multimedias.isEmpty(); }
    public boolean esReciente() { return fechaHecho != null && fechaHecho.isAfter(LocalDateTime.now().minusDays(7)); }
    
    public void marcarComoEliminado() { 
        this.eliminado = true; 
        actualizarUltimaActualizacion();
    }
    
    public void agregarMultimedia(Multimedia m) { if (m != null && m.esValido()) multimedias.add(m); }
    public void removerMultimedia(Multimedia m) { multimedias.remove(m); }
    public long getTamanioTotalMultimedia() { return multimedias.stream().mapToLong(Multimedia::getTamanio).sum(); }
    
    public void cambiarEstadoRevision(EstadoRevision estadoRevision) { 
        this.estadoRevision = estadoRevision; 
        actualizarUltimaActualizacion();
    }
    
    public void aceptarRevision() { 
        this.estadoRevision = EstadoRevision.ACEPTADO; 
        actualizarUltimaActualizacion();
    }
    
    public void rechazarRevision() { 
        this.estadoRevision = EstadoRevision.RECHAZADO; 
        actualizarUltimaActualizacion();
    }
    
    public void aceptarRevisionConSugerencia(String s) { 
        this.estadoRevision = EstadoRevision.ACEPTADO_CON_SUGERENCIAS; 
        this.sugerenciaDeCambio = s; 
        actualizarUltimaActualizacion();
    }
    
    public void incrementarContador() {
        this.contador++;
        actualizarUltimaActualizacion();
    }
    
    /**
     * Actualiza el timestamp de última modificación.
     * Debe llamarse cuando el hecho sufre cambios relevantes para estadísticas.
     */
    public void actualizarUltimaActualizacion() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Hook de JPA que se ejecuta antes de actualizar en BD.
     * Actualiza automáticamente ultimaActualizacion.
     */
    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Hook de JPA que se ejecuta antes de persistir en BD.
     * Inicializa ultimaActualizacion si es null.
     */
    @PrePersist
    protected void onCreate() {
        if (this.ultimaActualizacion == null) {
            this.ultimaActualizacion = LocalDateTime.now();
        }
    }

    /*public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }*/
}
