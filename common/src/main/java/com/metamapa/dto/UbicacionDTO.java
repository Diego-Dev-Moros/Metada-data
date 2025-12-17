package com.metamapa.dto;

import lombok.Data;

// Es un DTO simple que representa una ubicación geográfica por coordenadas (latitud y longitud).
// Se usa para transferir datos entre cliente y servidor en el contexto de un hecho (HechoDTO).
// SOLO se enfoca en transportar los datos, NO expone logica

@Data
public class UbicacionDTO {
    private double latitud;
    private double longitud;
} 