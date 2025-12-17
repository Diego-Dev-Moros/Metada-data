package com.metamapa.dto;

import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.hechos.Multimedia;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.rol.Contribuyente;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para recibir hechos de fuentes externas que usan IDs no numéricos (ej: MongoDB ObjectId)
 * El ID se ignora ya que el agregador generará su propio ID secuencial
 */
@Data
public class HechoExternoDTO {
    private String id; // String para soportar MongoDB ObjectId - SE IGNORA al agregar
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
}
