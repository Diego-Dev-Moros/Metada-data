package com.metamapa.dto;

import com.metamapa.entities.rol.Rol;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class ContribuyenteDTO {
    private Long idAgregador; // ID en la base de datos MySQL del agregador
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private Rol rol;
}
