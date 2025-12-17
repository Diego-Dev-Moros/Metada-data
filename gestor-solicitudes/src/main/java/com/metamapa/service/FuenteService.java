package com.metamapa.service;

import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.FuenteDeDatos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import com.metamapa.repository.HechoRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuenteService {

    private final List<FuenteDeDatos> fuentes; // Spring inyecta todas las @Component que implementan la interfaz
    private final HechoRepository fullTextRepo;

    public List<Hecho> obtenerHechosDeTodasLasFuentes() {
        List<Hecho> result = new ArrayList<>();
        for (FuenteDeDatos f : fuentes) {
            List<Hecho> hs = f.obtenerHechos();
            log.info("Fuente {} trajo {} hechos", f.getIdentificador(), hs.size());
            
            // Agregar identificador de fuente a cada hecho
            String fuenteId = f.getIdentificador();
            for (Hecho h : hs) {
                if (!h.getFuentes().contains(fuenteId)) {
                    h.getFuentes().add(fuenteId);
                    log.debug("Hecho '{}' marcado con fuente '{}'", h.getTitulo(), fuenteId);
                }
            }
            
            result.addAll(hs);
        }
        return result;
    }

    /**
     * Obtiene una fuente específica por su identificador
     * Útil para crear/modificar colecciones
     */
    public FuenteDeDatos obtenerFuentePorId(String identificador) {
        return fuentes.stream()
                .filter(f -> f.getIdentificador().equals(identificador))
                .findFirst()
                .orElse(null);
    }


    public static class HechoSearchDTO {
        public Long id;
        public String titulo;
        public String descripcion;
        public String categoria;
        public Double score;
    }

    public List<HechoSearchDTO> buscarHechosFullText(String q, String categoria, int page, int size) {
        int limit = Math.max(1, Math.min(size, 50));
        int offset = Math.max(0, page) * limit;

        List<Object[]> filas = fullTextRepo.searchFullText(q, categoria, limit, offset);
        List<HechoSearchDTO> out = new ArrayList<>();
        for (Object[] r : filas) {
            HechoSearchDTO dto = new HechoSearchDTO();
            dto.id = ((Number) r[0]).longValue();
            dto.titulo = (String) r[1];
            dto.descripcion = (String) r[2];
            dto.categoria = (String) r[3];
            dto.score = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            out.add(dto);
        }
        return out;
    }
}
