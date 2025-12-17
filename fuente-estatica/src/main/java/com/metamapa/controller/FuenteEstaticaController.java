package com.metamapa.controller;

import com.metamapa.dto.CargaCsvResultado;
import com.metamapa.dto.HechoDTO;
import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.mapper.HechoMapper;
import com.metamapa.service.FuenteEstaticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/fuente-estatica")
@RequiredArgsConstructor
public class FuenteEstaticaController {
    
    private final FuenteEstaticaService fuenteEstaticaService;

   /* @GetMapping("/hechos")
    public ResponseEntity<List<Hecho>> obtenerHechos() {
        List<Hecho> hechos = fuenteEstaticaService.obtenerHechos();
        return ResponseEntity.ok(hechos);
    }*/

    //Devuelve DTOs
   /* @GetMapping("/hechos")
    public ResponseEntity<List<HechoDTO>> obtenerHechos() {
        List<HechoDTO> hechos = fuenteEstaticaService.obtenerHechos();
        return ResponseEntity.ok(hechos);
    }*/

    /**
     * Este endpoint es llamado por el agregador cuando ejecuta su proceso de consolidación.
     * Procesa los archivos PENDIENTES y retorna los hechos con origenArchivoId seteado.
     */
    @GetMapping("/hechos")
    public ResponseEntity<List<HechoDTO>> obtenerHechos() {
        List<Hecho> hechos = fuenteEstaticaService.procesarArchivosPendientes();
        List<HechoDTO> dtos = HechoMapper.toDTOList(hechos);
        return ResponseEntity.ok(dtos);
    }

    // ===========  Endpoints de carga de CSV  =========== //
    @PostMapping(value = "/cargar", consumes = "multipart/form-data")
    public ResponseEntity cargarHechosDesdeCSV(@RequestParam("file") MultipartFile file) {
        //fuenteEstaticaService.cargarHechos(file);
        //return ResponseEntity.ok().build();
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }
        if (file.getOriginalFilename() != null && !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Debe subir un archivo .csv");
        }
        try {
            CargaCsvResultado resultado = fuenteEstaticaService.cargarHechos(file);
            return ResponseEntity.ok(resultado); // devuelve resumen JSON
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error procesando CSV: " + e.getMessage());
        }
    }

    @GetMapping("/archivos")
    public ResponseEntity<?> listarArchivos() {
        List<ArchivoDataset> archivos = fuenteEstaticaService.obtenerTodosLosArchivos();
        return ResponseEntity.ok(archivos);
    }

    @GetMapping("/archivos/{id}")
    public ResponseEntity<ArchivoDataset> obtenerArchivo(@PathVariable Long id) {
        ArchivoDataset archivo = fuenteEstaticaService.obtenerTodosLosArchivos()
            .stream()
            .filter(a -> a.getId().equals(id))
            .findFirst()
            .orElse(null);
        if (archivo != null) {
            return ResponseEntity.ok(archivo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 