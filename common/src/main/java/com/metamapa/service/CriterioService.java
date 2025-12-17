package com.metamapa.service;

import com.metamapa.dto.CriterioDTO;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.colecciones.ColeccionCriterio;
import com.metamapa.entities.criterioDePertenencia.*;
import com.metamapa.entities.ubicaciones.Lugar;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CriterioService {
    
    /**
     * Agrega criterios a una colección desde DTOs
     */
    public void agregarCriteriosDesdeDTO(Coleccion coleccion, List<CriterioDTO> criteriosDTO) {
        if (criteriosDTO == null || criteriosDTO.isEmpty()) {
            return;
        }

        for (CriterioDTO criterioDTO : criteriosDTO) {
            if (criterioDTO.getTipoCriterio() == null) {
                throw new IllegalArgumentException("El tipo de criterio no puede ser null. Criterio: " + criterioDTO);
            }
            
            ColeccionCriterio criterio;
            
            switch (criterioDTO.getTipoCriterio()) {
                case "CATEGORIA":
                    criterio = crearCriterioCategoria(coleccion, criterioDTO.getValor());
                    break;
                case "TITULO":
                    criterio = crearCriterioTitulo(coleccion, criterioDTO.getValor());
                    break;
                case "DESCRIPCION":
                    criterio = crearCriterioDescripcion(coleccion, criterioDTO.getValor());
                    break;
                case "FECHA_CARGA":
                    criterio = crearCriterioFechaCarga(coleccion, criterioDTO.getFechaDesde(), 
                            criterioDTO.getFechaHasta());
                    break;
                case "FECHA_ACONTECIMIENTO":
                    criterio = crearCriterioFechaAcontecimiento(coleccion, criterioDTO.getFechaDesde(), 
                            criterioDTO.getFechaHasta());
                    break;
                case "UBICACION":
                    criterio = crearCriterioUbicacion(coleccion, criterioDTO.getPais(), 
                            criterioDTO.getProvincia(), criterioDTO.getMunicipio());
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de criterio no válido: " + criterioDTO.getTipoCriterio());
            }
            
            coleccion.agregarCriterio(criterio);
        }
    }

    /**
     * Convierte un Criterio (interfaz funcional) a una entidad persistente ColeccionCriterio
     */
    public ColeccionCriterio convertirCriterioAEntidad(Coleccion coleccion, Criterio criterio) {
        // Análisis del tipo de criterio mediante instanceof
        if (criterio instanceof CriterioCategoria) {
            CriterioCategoria cc = (CriterioCategoria) criterio;
            return new ColeccionCriterio(coleccion, "CATEGORIA", cc.getCategoria());
        } 
        else if (criterio instanceof CriterioTitulo) {
            CriterioTitulo ct = (CriterioTitulo) criterio;
            return new ColeccionCriterio(coleccion, "TITULO", ct.getTituloContiene());
        }
        else if (criterio instanceof CriterioDescripcion) {
            CriterioDescripcion cd = (CriterioDescripcion) criterio;
            return new ColeccionCriterio(coleccion, "DESCRIPCION", cd.getDescripcion());
        }
        else if (criterio instanceof CriterioFechaCarga) {
            CriterioFechaCarga cf = (CriterioFechaCarga) criterio;
            return new ColeccionCriterio(coleccion, "FECHA_CARGA", cf.getFechaCargaDesde(), cf.getFechaCargaHasta());
        }
        else if (criterio instanceof CriterioFechaAcontecimiento) {
            CriterioFechaAcontecimiento cf = (CriterioFechaAcontecimiento) criterio;
            return new ColeccionCriterio(coleccion, "FECHA_ACONTECIMIENTO", cf.getFechaInicial(), cf.getFechaFinal());
        }
        else if (criterio instanceof CriterioUbicacion) {
            CriterioUbicacion cu = (CriterioUbicacion) criterio;
            Lugar lugar = cu.getLugar();
            return new ColeccionCriterio(coleccion, "UBICACION", lugar.getPais(), lugar.getProvincia(), lugar.getMunicipio());
        }
        
        // Si no se puede determinar el tipo, retornar null o lanzar excepción
        throw new IllegalArgumentException("Tipo de criterio no soportado: " + criterio.getClass());
    }

    /**
     * Convierte una entidad ColeccionCriterio a un Criterio (interfaz funcional)
     */
    public Criterio convertirEntidadACriterio(ColeccionCriterio cc) {
        switch (cc.getTipoCriterio()) {
            case "CATEGORIA":
                return new CriterioCategoria(cc.getValor());
            case "TITULO":
                return new CriterioTitulo(cc.getValor());
            case "DESCRIPCION":
                return new CriterioDescripcion(cc.getValor());
            case "FECHA_CARGA":
                return new CriterioFechaCarga(cc.getFechaDesde(), cc.getFechaHasta());
            case "FECHA_ACONTECIMIENTO":
                return new CriterioFechaAcontecimiento(cc.getFechaDesde(), cc.getFechaHasta());
            case "UBICACION":
                Lugar lugar = new Lugar(cc.getPais(), cc.getProvincia(), cc.getMunicipio());
                return new CriterioUbicacion(lugar);
            default:
                throw new IllegalArgumentException("Tipo de criterio no soportado: " + cc.getTipoCriterio());
        }
    }

    /**
     * Convierte una lista de entidades ColeccionCriterio a una lista de Criterios
     */
    public List<Criterio> convertirEntidadesACriterios(List<ColeccionCriterio> entidades) {
        return entidades.stream()
                .map(this::convertirEntidadACriterio)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Crea un criterio de categoría y lo persiste
     */
    public ColeccionCriterio crearCriterioCategoria(Coleccion coleccion, String categoria) {
        return new ColeccionCriterio(coleccion, "CATEGORIA", categoria);
    }

    /**
     * Crea un criterio de título y lo persiste
     */
    public ColeccionCriterio crearCriterioTitulo(Coleccion coleccion, String titulo) {
        return new ColeccionCriterio(coleccion, "TITULO", titulo);
    }

    /**
     * Crea un criterio de descripción y lo persiste
     */
    public ColeccionCriterio crearCriterioDescripcion(Coleccion coleccion, String descripcion) {
        return new ColeccionCriterio(coleccion, "DESCRIPCION", descripcion);
    }

    /**
     * Crea un criterio de fecha de carga y lo persiste
     */
    public ColeccionCriterio crearCriterioFechaCarga(Coleccion coleccion, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        return new ColeccionCriterio(coleccion, "FECHA_CARGA", fechaDesde, fechaHasta);
    }

    /**
     * Crea un criterio de fecha de acontecimiento y lo persiste
     */
    public ColeccionCriterio crearCriterioFechaAcontecimiento(Coleccion coleccion, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        return new ColeccionCriterio(coleccion, "FECHA_ACONTECIMIENTO", fechaDesde, fechaHasta);
    }

    /**
     * Crea un criterio de ubicación y lo persiste
     */
    public ColeccionCriterio crearCriterioUbicacion(Coleccion coleccion, String pais, String provincia, String municipio) {
        return new ColeccionCriterio(coleccion, "UBICACION", pais, provincia, municipio);
    }
}