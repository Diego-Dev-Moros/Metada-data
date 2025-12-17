package com.metamapa.service;

import com.metamapa.dto.CriterioDTO;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.colecciones.ColeccionCriterio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio del gestor de solicitudes para manejo de criterios de colecciones.
 * Permite agregar criterios a colecciones cuando son creadas o modificadas.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CriterioService {

    /**
     * Agrega criterios a una colección desde DTOs
     */
    public void agregarCriteriosDesdeDTO(Coleccion coleccion, List<CriterioDTO> criteriosDTO) {
        if (criteriosDTO == null || criteriosDTO.isEmpty()) {
            return;
        }
        
        for (CriterioDTO dto : criteriosDTO) {
            ColeccionCriterio criterio = crearColeccionCriterioDesdeDTO(coleccion, dto);
            if (criterio != null) {
                coleccion.agregarCriterio(criterio);
                log.debug("Criterio agregado: tipo={}, valor={}", dto.getTipoCriterio(), dto.getValor());
            }
        }
        
        log.info("Agregados {} criterios a colección {}", criteriosDTO.size(), coleccion.getIdentificador());
    }
    
    /**
     * Crea una entidad ColeccionCriterio basándose en el DTO
     */
    private ColeccionCriterio crearColeccionCriterioDesdeDTO(Coleccion coleccion, CriterioDTO dto) {
        String tipo = dto.getTipoCriterio();
        if (tipo == null) {
            log.warn("Tipo de criterio nulo, ignorando");
            return null;
        }
        
        switch (tipo.toUpperCase()) {
            case "CATEGORIA":
            case "TITULO":
            case "DESCRIPCION":
                return new ColeccionCriterio(coleccion, tipo.toUpperCase(), dto.getValor());
                
            case "FECHA_CARGA":
            case "FECHA_ACONTECIMIENTO":
                return new ColeccionCriterio(coleccion, tipo.toUpperCase(), dto.getFechaDesde(), dto.getFechaHasta());
                
            case "UBICACION":
                return new ColeccionCriterio(coleccion, "UBICACION", dto.getPais(), dto.getProvincia(), dto.getMunicipio());
                
            default:
                log.warn("Tipo de criterio desconocido: {}", tipo);
                return null;
        }
    }
}
