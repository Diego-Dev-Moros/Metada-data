package com.metamapa.repository;

import com.metamapa.domain.ContribuyenteDinamico;
import com.metamapa.domain.TipoUsuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio MongoDB para ContribuyenteDinamico
 */
@Repository
public interface ContribuyenteDinamicoRepository extends MongoRepository<ContribuyenteDinamico, String> {
    
    // Buscar por nombre y apellido
    List<ContribuyenteDinamico> findByNombreAndApellido(String nombre, String apellido);
    
    // Buscar contribuyentes no eliminados
    List<ContribuyenteDinamico> findByEliminadoFalse();
    
    // Buscar por tipo de usuario
    List<ContribuyenteDinamico> findByTipoUsuario(TipoUsuario tipoUsuario);
    
    // Buscar administradores activos
    List<ContribuyenteDinamico> findByTipoUsuarioAndEliminadoFalse(TipoUsuario tipoUsuario, boolean eliminado);
    
    // Buscar por ID del agregador (para sincronización entre microservicios)
    // Usa findFirst para manejar posibles duplicados (devuelve el primero)
    ContribuyenteDinamico findFirstByIdAgregador(Long idAgregador);
}