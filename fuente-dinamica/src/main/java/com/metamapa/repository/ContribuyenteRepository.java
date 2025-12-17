package com.metamapa.repository;

import com.metamapa.entities.rol.Contribuyente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContribuyenteRepository extends JpaRepository<Contribuyente, Long> {
    
    // Buscar contribuyente por nombre y apellido
    Optional<Contribuyente> findByNombreAndApellido(String nombre, String apellido);
    
    // Buscar contribuyentes por rol
    // List<Contribuyente> findByRol(Rol rol);
}