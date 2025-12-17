package com.metamapa.dto;

import com.metamapa.domain.TipoUsuario;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO específico para registro de contribuyentes en fuente-dinamica
 */
@Data
public class RegistrarContribuyenteDTO {
    private Long idAgregador; // ID del agregador (MySQL) para sincronización
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private TipoUsuario tipoUsuario; // Opcional, por defecto CONTRIBUYENTE
    private String tipoUsuarioString; // Alternativa como String para compatibilidad
}