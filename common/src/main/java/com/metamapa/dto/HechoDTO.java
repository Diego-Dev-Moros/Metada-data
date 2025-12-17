package com.metamapa.dto;

import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.Multimedia;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.rol.Contribuyente;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Es un objeto de transferencia de datos que representa la versión simplificada y
// serializable de un Hecho del dominio, pensada para:
// - Ser usada en la capa web (REST API): envío y recepción de datos desde el cliente (por JSON).
// - Evita exponer el modelo de dominio directamente

// Para recibir o devolver hechos en una API REST.

@Data
public class HechoDTO {
    private Long id; // Long para sistema central (MySQL autoincremental)
    private String titulo;
    private String descripcion;
    private String categoria;
    private UbicacionDTO ubicacion;
    private List<String> etiquetas;
    private LocalDateTime fechaHecho;
    private LocalDateTime fechaCarga;
    private OrigenHecho origen;
    private Contribuyente contribuyente;
    private List<Multimedia> multimedias;
    private boolean eliminado;
    private EstadoRevision estadoRevision;
    private String sugerenciaDeCambio;
    private boolean esAnonimo;
    
    /**
     * Lista de fuentes que reportaron este hecho (para depuración).
     */
    private List<String> fuentes;
    
    /**
     * ID del archivo de origen (solo para fuente-estática).
     * Usado para crear la relación N-N en la tabla hecho_origen_archivo.
     */
    private Long origenArchivoId;
} 