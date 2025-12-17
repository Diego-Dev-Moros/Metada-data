package com.metamapa.entities.rol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metamapa.entities.hechos.Hecho;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Data

@Entity
@Table(name = "Contribuyente")
public class Contribuyente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "contribuyente") // mappedBy es para que la relacion sea bidireccional
    @JsonIgnore
    private List<Hecho> hechosSubidos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name ="rolContribuyente", nullable = false)
    private Rol rol = Rol.CONTRIBUYENTE;

    public Contribuyente(String nombre) { this.nombre = nombre; } //contribuyente registrado
    public Contribuyente() {}

    public void agregarHecho(Hecho hecho) {
        if (hecho != null) {
            hechosSubidos.add(hecho);
            hecho.setContribuyente(this);
        }
    }

    public Integer getEdad() {
        if (fechaNacimiento == null) {
            return null; // O return 0; si prefieres un valor por defecto
        }
        LocalDate fechaHoy = LocalDate.now();
        return Period.between(fechaNacimiento, fechaHoy).getYears();
    }

    // Verificacion si se puede editar (si pertenece y es reciente)
    public boolean puedeEditarHecho(Hecho hecho) {
        return hecho.getContribuyente() != null && hechosSubidos.contains(hecho) && hecho.esReciente();
    }

    public List<Hecho> obtenerHechosSubidos() {
        return new ArrayList<>(hechosSubidos);
    }
}