package com.metamapa.mapper;

import com.metamapa.dto.HechoDTO;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.ubicaciones.Ubicacion;
import com.metamapa.dto.UbicacionDTO;
import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.hechos.EstadoRevision;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component
public class HechoMapper {

    // ====== Entity → DTO ======
    public static HechoDTO toDTO(Hecho h) {
        if (h == null) return null;

        HechoDTO dto = new HechoDTO();
        dto.setId(h.getId()); // Long ID del sistema central
        dto.setTitulo(h.getTitulo());
        dto.setDescripcion(h.getDescripcion());
        dto.setCategoria(h.getCategoria());

        // Ubicación
        if (h.getUbicacion() != null) {
            UbicacionDTO uDto = new UbicacionDTO();
            uDto.setLatitud(h.getUbicacion().getLatitud());
            uDto.setLongitud(h.getUbicacion().getLongitud());
            dto.setUbicacion(uDto);
        }

        dto.setEtiquetas(h.getEtiquetas() != null ? new ArrayList<>(h.getEtiquetas()) : new ArrayList<>());
        dto.setFechaHecho(h.getFechaHecho());
        dto.setFechaCarga(h.getFechaCarga());
        dto.setOrigen(h.getOrigen());

        // Contribuyente
        dto.setContribuyente(h.getContribuyente());

        // Multimedia
        dto.setMultimedias(h.getMultimedias() != null ? new ArrayList<>(h.getMultimedias()) : new ArrayList<>());

        dto.setEliminado(h.isEliminado());
        dto.setEstadoRevision(h.getEstadoRevision());
        dto.setSugerenciaDeCambio(h.getSugerenciaDeCambio());
        dto.setEsAnonimo(h.isEsAnonimo());

        if (!h.isEsAnonimo() && h.getContribuyente() != null ) {
            dto.setContribuyente(h.getContribuyente());
        }
        else {
            dto.setContribuyente(null); // se muestra como si no hubiera contribuyente (SE MUESTRA!!, NO ES LA REALIDAD)
        }
        
        // Copiar fuentes
        dto.setFuentes(h.getFuentes() != null ? new ArrayList<>(h.getFuentes()) : new ArrayList<>());
        
        // Copiar origenArchivoId (solo para fuente-estática, será null en otros casos)
        dto.setOrigenArchivoId(h.getOrigenArchivoId());

        return dto;
    }

    // ====== DTO → Entity ======
    public static Hecho toEntity(HechoDTO dto) {
        if (dto == null) return null;

        Hecho h = new Hecho();
        // NO asignar ID - el agregador generará su propio ID secuencial
        // h.setId(dto.getId()); // COMENTADO - ID de fuente externa se ignora
        h.setTitulo(dto.getTitulo());
        h.setDescripcion(dto.getDescripcion());
        h.setCategoria(dto.getCategoria());

        if (dto.getUbicacion() != null) {
            Ubicacion u = new Ubicacion();
            u.setLatitud(dto.getUbicacion().getLatitud());
            u.setLongitud(dto.getUbicacion().getLongitud());
            h.setUbicacion(u);
        }

        h.setEtiquetas(dto.getEtiquetas() != null ? new ArrayList<>(dto.getEtiquetas()) : new ArrayList<>());
        h.setFechaHecho(dto.getFechaHecho());
        h.setFechaCarga(dto.getFechaCarga());
        h.setOrigen(dto.getOrigen());

        h.setContribuyente(dto.getContribuyente());
        h.setMultimedias(dto.getMultimedias() != null ? new ArrayList<>(dto.getMultimedias()) : new ArrayList<>());
        h.setEliminado(dto.isEliminado());
        h.setEstadoRevision(dto.getEstadoRevision());
        h.setSugerenciaDeCambio(dto.getSugerenciaDeCambio());
        h.setEsAnonimo(dto.isEsAnonimo());
        
        // Copiar fuentes
        h.setFuentes(dto.getFuentes() != null ? new ArrayList<>(dto.getFuentes()) : new ArrayList<>());
        
        // Copiar origenArchivoId (solo para fuente-estática, será null en otros casos)
        h.setOrigenArchivoId(dto.getOrigenArchivoId());

        return h;
    }

    // ====== Listas ======
    public static List<HechoDTO> toDTOList(List<Hecho> hechos) {
        List<HechoDTO> list = new ArrayList<>();
        if (hechos != null) {
            for (Hecho h : hechos) list.add(toDTO(h));
        }
        return list;
    }

    public static List<Hecho> toEntityList(List<HechoDTO> dtos) {
        List<Hecho> list = new ArrayList<>();
        if (dtos != null) {
            for (HechoDTO d : dtos) list.add(toEntity(d));
        }
        return list;
    }
}
