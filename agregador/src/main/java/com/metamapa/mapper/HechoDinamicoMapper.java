package com.metamapa.mapper;

import com.metamapa.dto.HechoDinamicoDTO;
import com.metamapa.dto.MultimediaDTO;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.Multimedia;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.ubicaciones.Ubicacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir HechoDinamicoDTO (de fuente dinámica) a entidades Hecho del agregador
 */
@Slf4j
@Component
public class HechoDinamicoMapper {
    
    /**
     * Convierte HechoDinamicoDTO a Hecho (entidad central)
     * Para hechos pendientes, el ID de MongoDB NO se asigna a la entidad Hecho
     */
    public static Hecho toEntity(HechoDinamicoDTO dto) {
        if (dto == null) return null;

        Hecho h = new Hecho();
        // NO asignar ID - para hechos pendientes, este es el ID de MongoDB que NO se guarda aquí
        // El ID de MongoDB está en dto.getId() pero NO lo asignamos al Hecho
        
        h.setTitulo(dto.getTitulo());
        h.setDescripcion(dto.getDescripcion());
        h.setCategoria(dto.getCategoria());

        // Mapear ubicación
        if (dto.getUbicacion() != null) {
            Ubicacion u = new Ubicacion();
            u.setLatitud(dto.getUbicacion().getLatitud());
            u.setLongitud(dto.getUbicacion().getLongitud());
            h.setUbicacion(u);
        }

        h.setEtiquetas(dto.getEtiquetas() != null ? new ArrayList<>(dto.getEtiquetas()) : new ArrayList<>());
        h.setFechaHecho(dto.getFechaHecho());
        h.setFechaCarga(dto.getFechaCarga());
        h.setOrigen(OrigenHecho.CONTRIBUYENTE); // Fuente dinámica siempre es CONTRIBUYENTE
        h.setEliminado(dto.isEliminado());
        h.setEstadoRevision(dto.getEstadoRevision());
        h.setSugerenciaDeCambio(dto.getSugerenciaDeCambio());
        h.setEsAnonimo(dto.isEsAnonimo());

        // Mapear contribuyente (crear instancia transitoria con ID, se buscará en BD después)
        if (dto.getContribuyente() != null && !dto.isEsAnonimo() && dto.getContribuyente().getIdAgregador() != null) {
            log.info("🗺️ Mapper: Contribuyente presente en DTO - idAgregador: {}, nombre: {} {}", 
                    dto.getContribuyente().getIdAgregador(),
                    dto.getContribuyente().getNombre(),
                    dto.getContribuyente().getApellido());
            Contribuyente c = new Contribuyente();
            c.setId(dto.getContribuyente().getIdAgregador()); // Solo el ID, se buscará/cargará después
            h.setContribuyente(c);
            log.info("🗺️ Mapper: Contribuyente asignado al hecho con ID: {}", c.getId());
        } else {
            log.info("🗺️ Mapper: Sin contribuyente - esAnonimo: {}, contribuyenteDTO: {}, idAgregador: {}", 
                    dto.isEsAnonimo(),
                    dto.getContribuyente() != null,
                    dto.getContribuyente() != null ? dto.getContribuyente().getIdAgregador() : "N/A");
        }

        // Mapear multimedias
        if (dto.getMultimedias() != null && !dto.getMultimedias().isEmpty()) {
            List<Multimedia> multimedias = dto.getMultimedias().stream()
                    .map(HechoDinamicoMapper::multimediaDTOToEntity)
                    .collect(Collectors.toList());
            h.setMultimedias(multimedias);
            
            // Establecer la relación bidireccional
            multimedias.forEach(m -> m.setHecho(h));
        }

        return h;
    }
    
    /**
     * Convierte MultimediaDTO a Multimedia entity
     */
    private static Multimedia multimediaDTOToEntity(MultimediaDTO dto) {
        if (dto == null) return null;
        
        Multimedia multimedia = new Multimedia();
        
        // Mapeo con validaciones explícitas
        multimedia.setRuta(dto.getUrl() != null && !dto.getUrl().isEmpty() ? dto.getUrl() : ""); 
        multimedia.setTipo(dto.getTipo() != null && !dto.getTipo().isEmpty() ? dto.getTipo() : "desconocido");
        multimedia.setNombre(dto.getTitulo() != null && !dto.getTitulo().isEmpty() ? dto.getTitulo() : "multimedia");
        multimedia.setFormato(dto.getFormato() != null && !dto.getFormato().isEmpty() ? dto.getFormato() : "");
        multimedia.setTamanio(dto.getTamanio() != null ? dto.getTamanio() : 0L);
        multimedia.setFechaCarga(dto.getFechaCarga() != null ? dto.getFechaCarga() : java.time.LocalDateTime.now());
        
        return multimedia;
    }
    
    public static List<Hecho> toEntityList(List<HechoDinamicoDTO> dtos) {
        List<Hecho> list = new ArrayList<>();
        if (dtos != null) {
            for (HechoDinamicoDTO d : dtos) {
                Hecho hecho = toEntity(d);
                if (hecho != null) {
                    list.add(hecho);
                }
            }
        }
        return list;
    }
}
