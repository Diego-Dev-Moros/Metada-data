package com.metamapa.mapper;

import com.metamapa.dto.HechoExternoDTO;
import com.metamapa.dto.UbicacionDTO;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.ubicaciones.Ubicacion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper específico del AGREGADOR para convertir DTOs externos a entidades centrales
 */
@Component
public class HechoExternoMapper {
    
    /**
     * Convierte HechoExternoDTO (de fuentes externas) a Hecho (entidad central)
     * IGNORA el ID externo - el agregador generará su propio ID
     */
    public static Hecho toEntity(HechoExternoDTO dto) {
        if (dto == null) return null;

        Hecho h = new Hecho();
        // NO asignar ID - MySQL generará ID autoincremental
        // El ID externo (String de MongoDB) se IGNORA completamente
        
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
        
        // Si origen es null, asignar CONTRIBUYENTE por defecto (fuentes dinámicas sin origen explícito)
        h.setOrigen(dto.getOrigen() != null ? dto.getOrigen() : OrigenHecho.CONTRIBUYENTE);
        
        h.setContribuyente(dto.getContribuyente());
        h.setMultimedias(dto.getMultimedias() != null ? new ArrayList<>(dto.getMultimedias()) : new ArrayList<>());
        h.setEliminado(dto.isEliminado());
        h.setEstadoRevision(dto.getEstadoRevision());
        h.setSugerenciaDeCambio(dto.getSugerenciaDeCambio());
        h.setEsAnonimo(dto.isEsAnonimo());

        return h;
    }
    
    public static List<Hecho> toEntityList(List<HechoExternoDTO> dtos) {
        List<Hecho> list = new ArrayList<>();
        if (dtos != null) {
            for (HechoExternoDTO d : dtos) {
                Hecho hecho = toEntity(d);
                if (hecho != null) {
                    list.add(hecho);
                }
            }
        }
        return list;
    }
}
