package com.metamapa.api.publica;

import com.metamapa.client.FuenteDinamicaCrudClient;
import com.metamapa.dto.ColeccionResponseDTO;
import com.metamapa.dto.CrearSolicitudDTO;
import com.metamapa.dto.ReportarHechoDTO;
import com.metamapa.entities.MetodoDeNavegacion;
import com.metamapa.entities.colecciones.Coleccion;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.rol.Contribuyente;
import com.metamapa.entities.solicitudes.SolicitudEliminacion;
import com.metamapa.mapper.ColeccionMapper;
import com.metamapa.repository.HechoRepository;
import com.metamapa.service.ColeccionService;
import com.metamapa.service.SolicitudService;
import com.metamapa.service.ContribuyenteService;
import com.metamapa.service.FuenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/metamapa")
@RequiredArgsConstructor
public class APIPublicaMetamapaController {
    
    private final ColeccionService coleccionService;
    private final SolicitudService solicitudService;
    private final FuenteDinamicaCrudClient fuenteDinamicaClient;
    private final ColeccionMapper coleccionMapper;
    private final FuenteService fuenteService;
    private final ContribuyenteService contribuyenteService;
    private final HechoRepository hechoRepository;
    
    // Obtener todas las colecciones disponibles
    @GetMapping("/colecciones")
    public ResponseEntity<List<ColeccionResponseDTO>> obtenerTodasLasColecciones() {
        List<Coleccion> colecciones = coleccionService.obtenerTodasLasColecciones();
        List<ColeccionResponseDTO> coleccionesDTO = colecciones.stream()
                .map(coleccionMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(coleccionesDTO);
    }

    // Obtener hechos de una colección con filtros opcionales
    @GetMapping("/colecciones/{id}/hechos")
    public ResponseEntity<List<Hecho>> obtenerHechosDeColeccion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "IRRESTRICTA") MetodoDeNavegacion modo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String titulo,
            @RequestParam(defaultValue = "false") boolean soloRecientes) {
        
        List<Hecho> hechos = coleccionService.obtenerHechosDeColeccion(id, modo);
        
        // Aplicar filtros adicionales
        if (categoria != null && !categoria.isEmpty()) {
            hechos = hechos.stream()
                    .filter(h -> h.getCategoria() != null && h.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
        }
        
        if (titulo != null && !titulo.isEmpty()) {
            hechos = hechos.stream()
                    .filter(h -> h.getTitulo() != null && h.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (soloRecientes) {
            hechos = hechos.stream()
                    .filter(Hecho::esReciente)
                    .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(hechos);
    }


    // Generar una solicitud de eliminación a un hecho (JSON)
    @PostMapping(value = "/hechos/{idHecho}/solicitudes", consumes = "application/json")
    public ResponseEntity<?> crearSolicitudEliminacion(
            @PathVariable Long idHecho,
            @RequestBody CrearSolicitudDTO solicitudDTO,
            @RequestHeader(value = "X-Contribuyente-Id", required = false) Long contribuyenteId) {
        
        try {
            // Buscar el hecho en la base de datos
            Optional<Hecho> hechoOpt = hechoRepository.findById(idHecho);
            if (!hechoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Hecho hecho = hechoOpt.get();
            
            // Validar justificación
            if (solicitudDTO.getJustificacion() == null || solicitudDTO.getJustificacion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La justificación es requerida");
            }
            
            // Obtener contribuyente si se proporciona el ID
            Contribuyente contribuyente = null;
            if (contribuyenteId != null) {
                contribuyente = contribuyenteService.obtenerContribuyentePorId(contribuyenteId);
                if (contribuyente == null) {
                    return ResponseEntity.badRequest()
                            .body("Contribuyente no encontrado con ID: " + contribuyenteId);
                }
            }
            
            SolicitudEliminacion solicitud = solicitudService.crearSolicitudEliminacion(
                    hecho, 
                    solicitudDTO.getJustificacion(), 
                    contribuyente);
            return ResponseEntity.ok(solicitud);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear la solicitud: " + e.getMessage());
        }
    }
}