package com.metamapa.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contribuyente específico para fuente-dinamica
 */
@Document(collection = "contribuyentes")
@Data
@NoArgsConstructor
public class ContribuyenteDinamico {
    
    @Id
    private String id; // MongoDB ObjectId
    
    @Field("idAgregador")
    private Long idAgregador; // ID del agregador (MySQL) para referencia cruzada
    
    @Field("nombre")
    private String nombre;
    
    @Field("apellido")
    private String apellido;
    
    @Field("fechaNacimiento")
    private LocalDate fechaNacimiento;
    
    @Field("fechaRegistro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @Field("tipoUsuario")
    private TipoUsuario tipoUsuario = TipoUsuario.CONTRIBUYENTE;
    
    @Field("eliminado")
    private boolean eliminado = false;
    
    // Constructor básico
    public ContribuyenteDinamico(String nombre, String apellido, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaRegistro = LocalDateTime.now();
        this.tipoUsuario = TipoUsuario.CONTRIBUYENTE;
    }
    
    // Constructor con tipo de usuario
    public ContribuyenteDinamico(String nombre, String apellido, LocalDate fechaNacimiento, TipoUsuario tipoUsuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaRegistro = LocalDateTime.now();
        this.tipoUsuario = tipoUsuario;
    }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return String.format("%s %s", nombre != null ? nombre : "", apellido != null ? apellido : "").trim();
    }
    
    public boolean esValido() {
        return (nombre != null && !nombre.trim().isEmpty()) && 
               (apellido != null && !apellido.trim().isEmpty());
    }
    
    public boolean esAdministrador() {
        return tipoUsuario == TipoUsuario.ADMINISTRADOR;
    }
    
    public boolean esContribuyente() {
        return tipoUsuario == TipoUsuario.CONTRIBUYENTE;
    }
    
    public void marcarComoEliminado() {
        this.eliminado = true;
    }
}