package com.metamapa.entities.solicitudes;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "solicitud_eliminacion")
public class SolicitudEliminacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "id_hecho", referencedColumnName = "id")
    private Hecho hecho;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud", nullable = false, length = 50)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(name = "fecha_solicitud", columnDefinition = "DATETIME")
    private LocalDateTime fechaSolicitud = LocalDateTime.now();
    
    @Column(name = "fecha_resolucion", columnDefinition = "DATETIME")
    private LocalDateTime fechaResolucion;
    
    @Column(name = "es_spam", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean esSpam = false;

    @ManyToOne
    @JoinColumn(name = "id_contribuyente", referencedColumnName = "id")
    private Contribuyente contribuyente;

    public SolicitudEliminacion() {}
    public SolicitudEliminacion(Hecho hecho, String motivo) {
        this.hecho = hecho;
        this.motivo = motivo;
    }

    public void aceptar() { 
        this.estado = EstadoSolicitud.APROBADA; 
        if (hecho != null) {
            hecho.marcarComoEliminado(); 
        }
    }
    public void rechazar() { this.estado = EstadoSolicitud.RECHAZADA; }
    public void marcarComoSpam() { this.esSpam = true; this.estado = EstadoSolicitud.RECHAZADA; }
    public boolean esValida() { return motivo != null && motivo.length() >= 500 && !esSpam; } // Restaurado a 500
}