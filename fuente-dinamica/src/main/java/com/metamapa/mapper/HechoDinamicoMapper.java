package com.metamapa.mapper;

import com.metamapa.domain.ContribuyenteDinamico;
import com.metamapa.domain.HechoDinamico;
import com.metamapa.domain.MultimediaDinamica;
import com.metamapa.dto.ContribuyenteDTO;
import com.metamapa.dto.HechoDinamicoDTO;
import com.metamapa.dto.MultimediaDTO;
import com.metamapa.dto.UbicacionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper específico de fuente-dinamica
 * Convierte entre entidades de dominio y DTOs
 */
@Slf4j
@Component
public class HechoDinamicoMapper {
    
    public static HechoDinamicoDTO toDTO(HechoDinamico hecho) {
        if (hecho == null) return null;
        
        HechoDinamicoDTO dto = new HechoDinamicoDTO();
        dto.setId(hecho.getId());
        dto.setTitulo(hecho.getTitulo());
        dto.setDescripcion(hecho.getDescripcion());
        dto.setCategoria(hecho.getCategoria());
        
        // Mapear ubicación
        if (hecho.getUbicacion() != null) {
            UbicacionDTO ubicacionDTO = new UbicacionDTO();
            ubicacionDTO.setLatitud(hecho.getUbicacion().getLatitud());
            ubicacionDTO.setLongitud(hecho.getUbicacion().getLongitud());
            dto.setUbicacion(ubicacionDTO);
        }
        
        dto.setEtiquetas(hecho.getEtiquetas());
        dto.setFechaHecho(hecho.getFechaHecho());
        dto.setFechaCarga(hecho.getFechaCarga());
        dto.setEstadoRevision(hecho.getEstadoRevision()); // Usar el enum directamente
        dto.setSugerenciaDeCambio(hecho.getSugerenciaDeCambio());
        dto.setEsAnonimo(hecho.isEsAnonimo());
        dto.setEliminado(hecho.isEliminado());
        
        // Mapear contribuyente
        if (hecho.getContribuyente() != null) {
            log.info("🔵 Mapper fuente-dinamica: Hecho '{}' tiene contribuyente, mapeando...", hecho.getTitulo());
            dto.setContribuyente(contribuyenteDinamicoToDTO(hecho.getContribuyente()));
        } else {
            log.info("🔵 Mapper fuente-dinamica: Hecho '{}' NO tiene contribuyente (esAnonimo={})", 
                    hecho.getTitulo(), hecho.isEsAnonimo());
        }
        
        // Mapear multimedias
        if (hecho.getMultimedias() != null && !hecho.getMultimedias().isEmpty()) {
            dto.setMultimedias(hecho.getMultimedias().stream()
                    .map(HechoDinamicoMapper::multimediaDinamicaToDTO)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Convierte ContribuyenteDinamico a ContribuyenteDTO
     */
    private static ContribuyenteDTO contribuyenteDinamicoToDTO(ContribuyenteDinamico contribuyente) {
        if (contribuyente == null) {
            log.debug("🔴 Mapper fuente-dinamica: contribuyente es NULL");
            return null;
        }
        
        log.info("🟢 Mapper fuente-dinamica: Convirtiendo contribuyente - ID MongoDB: {}, idAgregador: {}, nombre: {} {}", 
                contribuyente.getId(),
                contribuyente.getIdAgregador(),
                contribuyente.getNombre(),
                contribuyente.getApellido());
        
        ContribuyenteDTO dto = new ContribuyenteDTO();
        dto.setIdAgregador(contribuyente.getIdAgregador()); // ID del agregador (MySQL)
        dto.setNombre(contribuyente.getNombre());
        dto.setApellido(contribuyente.getApellido());
        dto.setFechaNacimiento(contribuyente.getFechaNacimiento());
        
        log.info("🟢 Mapper fuente-dinamica: DTO creado - idAgregador: {}", dto.getIdAgregador());
        
        return dto;
    }
    
    /**
     * Convierte MultimediaDinamica a MultimediaDTO
     */
    private static MultimediaDTO multimediaDinamicaToDTO(MultimediaDinamica multimedia) {
        if (multimedia == null) return null;
        
        MultimediaDTO dto = new MultimediaDTO();
        // Usar rutaArchivo si existe, sino usar url
        dto.setUrl(multimedia.getRutaArchivo() != null ? multimedia.getRutaArchivo() : multimedia.getUrl());
        dto.setTipo(multimedia.getTipo());
        dto.setTitulo(multimedia.getNombre()); // nombre como título
        dto.setDescripcion(multimedia.getDescripcion());
        dto.setTamanio(multimedia.getTamaño());
        dto.setFormato(multimedia.getFormatoMime());
        dto.setFechaCarga(java.time.LocalDateTime.now()); // Añadir fecha de carga
        
        return dto;
    }
    
    public static List<HechoDinamicoDTO> toDTOList(List<HechoDinamico> hechos) {
        if (hechos == null) return new ArrayList<>();
        return hechos.stream()
                .map(HechoDinamicoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
