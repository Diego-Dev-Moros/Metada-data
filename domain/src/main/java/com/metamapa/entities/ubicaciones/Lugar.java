package com.metamapa.entities.ubicaciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor

@Entity
@Table(name = "Lugar")
@NoArgsConstructor
public class Lugar {

    @Id
    @GeneratedValue/*(strategy = GenerationType.IDENTITY)*/
    private Long idLugar;

    private String pais;
    private String provincia;
    private String municipio;

    public Lugar(String pais, String provincia, String municipio) {
        this.pais = pais;
        this.provincia = provincia;
        this.municipio = municipio;
    }
}
