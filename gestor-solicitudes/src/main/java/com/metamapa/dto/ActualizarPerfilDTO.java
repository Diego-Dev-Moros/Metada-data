package com.metamapa.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para actualizar el perfil de un usuario (Contribuyente o Administrador)
 */
@Data
public class ActualizarPerfilDTO {
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
}
