package com.metamapa.repository.mongo;

import com.metamapa.entities.mongo.ContribuyenteMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para contribuyentes en fuente dinámica
 */
@Repository
public interface ContribuyenteMongoRepository extends MongoRepository<ContribuyenteMongo, Long> {
    
    // Buscar por nombre
    Optional<ContribuyenteMongo> findByNombre(String nombre);
    
    // Buscar por nombre y apellido
    Optional<ContribuyenteMongo> findByNombreAndApellido(String nombre, String apellido);
    
    // Buscar contribuyentes no anónimos
    List<ContribuyenteMongo> findByEsAnonimoFalse();
    
    // Buscar por nombre parcial (case insensitive)
    @Query("{ 'nombre': { $regex: ?0, $options: 'i' } }")
    List<ContribuyenteMongo> findByNombreContaining(String nombre);
    
    // Verificar si existe por nombre
    boolean existsByNombre(String nombre);
}