package com.metamapa.repository;

import com.metamapa.domain.HechoDinamico;
import com.metamapa.entities.hechos.EstadoRevision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio MongoDB para HechoDinamico
 */
@Repository
public interface HechoDinamicoRepository extends MongoRepository<HechoDinamico, String> {
    
    // Buscar por estado de revisión
    List<HechoDinamico> findByEstadoRevision(EstadoRevision estadoRevision);
    
    // Buscar hechos no eliminados
    List<HechoDinamico> findByEliminadoFalse();
    
    // Buscar por categoría
    List<HechoDinamico> findByCategoria(String categoria);
    
    // Buscar por contribuyente
    @Query("{'contribuyente.id': ?0}")
    List<HechoDinamico> findByContribuyenteId(String contribuyenteId);
    
    // Buscar por rango de fechas
    List<HechoDinamico> findByFechaHechoBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Buscar hechos pendientes de revisión
    List<HechoDinamico> findByEstadoRevisionAndEliminadoFalse(EstadoRevision estadoRevision, boolean eliminado);
    
    // Buscar por etiquetas
    @Query("{'etiquetas': {$in: ?0}}")
    List<HechoDinamico> findByEtiquetasIn(List<String> etiquetas);
    
    // Buscar hechos recientes (últimos 7 días)
    @Query("{'fechaHecho': {$gte: ?0}, 'eliminado': false}")
    List<HechoDinamico> findHechosRecientes(LocalDateTime fechaDesde);
    
    // Contar hechos por estado
    long countByEstadoRevision(EstadoRevision estadoRevision);
    
    // Buscar hechos con ubicación (tienen coordenadas)
    @Query("{'ubicacion': {$ne: null}, 'eliminado': false}")
    List<HechoDinamico> findHechosConUbicacion();
}