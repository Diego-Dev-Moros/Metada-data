package com.metamapa.mapper;

import com.metamapa.dto.ColeccionResponseDTO;
import com.metamapa.dto.HechoDTO;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.hechos.Hecho;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ColeccionMapper {
    
    public ColeccionResponseDTO toResponseDTO(Coleccion coleccion) {
        ColeccionResponseDTO dto = new ColeccionResponseDTO();
        dto.setTitulo(coleccion.getTitulo());
        dto.setDescripcion(coleccion.getDescripcion());
        dto.setIdentificador(coleccion.getIdentificador());
        
        // Convertir hechos - usar hechosPersistentes si está disponible, sino usar verHechos()
        List<Hecho> hechos = new ArrayList<>();
        if (coleccion.getHechosPersistentes() != null && !coleccion.getHechosPersistentes().isEmpty()) {
            // Obtener hechos desde las entidades persistentes
            hechos = coleccion.getHechosPersistentes().stream()
                    .map(ch -> ch.getHecho())
                    .collect(Collectors.toList());
        } else {
            // Fallback al método transitorio
            hechos = coleccion.verHechos();
        }
        
        List<HechoDTO> hechosDTO = hechos.stream()
                .map(this::toHechoDTO)
                .collect(Collectors.toList());
        dto.setHechos(hechosDTO);
        
        // Convertir hechos consensuados
        List<Hecho> hechosConsensuados = new ArrayList<>();
        if (coleccion.getHechosPersistentes() != null && !coleccion.getHechosPersistentes().isEmpty()) {
            hechosConsensuados = coleccion.getHechosPersistentes().stream()
                    .filter(ch -> ch.getConsensuado() != null && ch.getConsensuado())
                    .map(ch -> ch.getHecho())
                    .collect(Collectors.toList());
        } else {
            hechosConsensuados = coleccion.verHechosConsensuados();
        }
        
        List<HechoDTO> hechosConsensuadosDTO = hechosConsensuados.stream()
                .map(this::toHechoDTO)
                .collect(Collectors.toList());
        dto.setHechosConsensuados(hechosConsensuadosDTO);
        
        // Algoritmo de consenso
        if (coleccion.getAlgoritmoDeConsenso() != null) {
            dto.setAlgoritmoDeConsenso(getAlgoritmoTipo(coleccion.getAlgoritmoDeConsenso().getClass().getSimpleName()));
        }
        
        // Contadores
        dto.setTotalHechos(hechos.size());
        dto.setTotalHechosConsensuados(hechosConsensuados.size());
        
        return dto;
    }
    
    private HechoDTO toHechoDTO(Hecho hecho) {
        HechoDTO dto = new HechoDTO();
        dto.setId(hecho.getId()); // Long ID del sistema central
        dto.setTitulo(hecho.getTitulo());
        dto.setDescripcion(hecho.getDescripcion());
        dto.setCategoria(hecho.getCategoria());
        // Omitir etiquetas por ahora para evitar LazyInitializationException
        // dto.setEtiquetas(hecho.getEtiquetas());
        dto.setFechaHecho(hecho.getFechaHecho());
        dto.setFechaCarga(hecho.getFechaCarga());
        dto.setEsAnonimo(hecho.isEsAnonimo());
        return dto;
    }
    
    private String getAlgoritmoTipo(String className) {
        switch (className) {
            case "Absoluta": return "absoluta";
            case "MayoriaSimple": return "mayoria_simple";
            case "MultiplesMenciones": return "multiples_menciones";
            default: return "desconocido";
        }
    }
}
