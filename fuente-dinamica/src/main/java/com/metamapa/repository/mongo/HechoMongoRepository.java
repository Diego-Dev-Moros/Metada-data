package com.metamapa.repository.mongo;

import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.entities.mongo.HechoMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio MongoDB para hechos en fuente dinámica
 */
@Repository
public interface HechoMongoRepository extends MongoRepository<HechoMongo, Long> {
    
    // Buscar por estado de revisión
    List<HechoMongo> findByEstadoRevision(EstadoRevision estadoRevision);
    
    // Buscar hechos pendientes
    List<HechoMongo> findByEstadoRevisionOrderByFechaCargaDesc(EstadoRevision estadoRevision);
    
    // Buscar hechos aprobados (ACEPTADO o ACEPTADO_CON_SUGERENCIAS)
    @Query("{ 'estado_revision': { $in: ['ACEPTADO', 'ACEPTADO_CON_SUGERENCIAS'] } }")
    List<HechoMongo> findHechosAprobados();
    
    // Buscar por categoría
    List<HechoMongo> findByCategoria(String categoria);
    
    // Buscar por título (contiene texto)
    @Query("{ 'titulo': { $regex: ?0, $options: 'i' } }")
    List<HechoMongo> findByTituloContaining(String titulo);
    
    // Buscar hechos recientes (últimos 7 días)
    @Query("{ 'fecha_hecho': { $gte: ?0 } }")
    List<HechoMongo> findHechosRecientes(LocalDateTime fechaDesde);
    
    // Buscar por fingerprint (evitar duplicados)
    Optional<HechoMongo> findByFingerprint(String fingerprint);
    
    // Buscar hechos no eliminados
    List<HechoMongo> findByEliminadoFalse();
    
    // Buscar por contribuyente
    @Query("{ 'contribuyente.nombre': ?0 }")
    List<HechoMongo> findByContribuyenteNombre(String nombreContribuyente);
    
    // Contar hechos por estado
    long countByEstadoRevision(EstadoRevision estadoRevision);
    
    // Hechos pendientes de los últimos N días
    @Query("{ 'estado_revision': 'PENDIENTE', 'fecha_carga': { $gte: ?0 } }")
    List<HechoMongo> findHechosPendientesRecientes(LocalDateTime fechaDesde);
}