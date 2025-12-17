package com.metamapa.entities.mongo;

import com.metamapa.entities.rol.Rol;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Contribuyente para MongoDB - puede ser documento independiente o embebido
 */
@Data
@NoArgsConstructor
@Document(collection = "contribuyentes")
public class ContribuyenteMongo {
    
    @Id
    private Long id;
    
    @Field("nombre")
    private String nombre;
    
    @Field("apellido")
    private String apellido;
    
    @Field("fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    @Field("fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    
    @Field("rol")
    private Rol rol = Rol.CONTRIBUYENTE;
    
    @Field("es_anonimo")
    private boolean esAnonimo = false;
    
    public ContribuyenteMongo(String nombre) {
        this.nombre = nombre;
        this.fechaRegistro = LocalDateTime.now();
    }
    
    public ContribuyenteMongo(String nombre, String apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        if (edad != null && edad > 0) {
            this.fechaNacimiento = LocalDate.now().minusYears(edad);
        }
        this.fechaRegistro = LocalDateTime.now();
    }
    
    public Integer getEdad() {
        if (fechaNacimiento == null) {
            return null;
        }
        LocalDate fechaHoy = LocalDate.now();
        return Period.between(fechaNacimiento, fechaHoy).getYears();
    }
    
    public static ContribuyenteMongo anonimo() {
        ContribuyenteMongo contrib = new ContribuyenteMongo();
        contrib.setNombre("Anónimo");
        contrib.setEsAnonimo(true);
        return contrib;
    }
}