package com.metamapa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metamapa.domain.ContribuyenteDinamico;
import com.metamapa.domain.HechoDinamico;
import com.metamapa.domain.MultimediaDinamica;
import com.metamapa.domain.TipoUsuario;
import com.metamapa.domain.UbicacionDinamica;
import com.metamapa.dto.*;
import com.metamapa.entities.hechos.EstadoRevision;
import com.metamapa.mapper.HechoDinamicoMapper;
import com.metamapa.service.ContribuyenteDinamicoService;
import com.metamapa.service.HechoDinamicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para fuente-dinamica usando dominio específico
 */
@RestController
@RequestMapping("/api/fuente-dinamica")
@RequiredArgsConstructor
public class FuenteDinamicaController {

    private final HechoDinamicoService hechoService;
    private final ContribuyenteDinamicoService contribuyenteService;

    // Carpeta base para subidas (configurable). Si no está en application.properties, usa "uploads".
    @Value("${app.upload.dir:uploads}")
    private String uploadRoot;

    @GetMapping("/hechos")
    public ResponseEntity<List<HechoDinamicoDTO>> obtenerHechos() {
        // Solo devolver hechos ACEPTADOS (no los que tienen sugerencias)
        List<HechoDinamico> hechos = hechoService.obtenerHechosPorEstado(EstadoRevision.ACEPTADO);
        return ResponseEntity.ok(HechoDinamicoMapper.toDTOList(hechos));
    }

    @GetMapping("/hechos/{id}")
    public ResponseEntity<HechoDinamicoDTO> obtenerHechoPorId(@PathVariable String id) {
        Optional<HechoDinamico> hecho = hechoService.buscarPorId(id);
        return hecho.map(h -> ResponseEntity.ok(HechoDinamicoMapper.toDTO(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hechosTODOS")
    public ResponseEntity<List<HechoDinamicoDTO>> obtenerTodoslosHechos() {
        List<HechoDinamico> hechos = hechoService.obtenerTodosActivos();
        return ResponseEntity.ok(HechoDinamicoMapper.toDTOList(hechos));
    }

    @GetMapping("/hechos/pendientes")
    public ResponseEntity<List<HechoDinamicoDTO>> obtenerHechosPendientesRevision() {
        List<HechoDinamico> hechos = hechoService.obtenerHechosPendientes();
        return ResponseEntity.ok(HechoDinamicoMapper.toDTOList(hechos));
    }

    @GetMapping("/hechos/rechazados")
    public ResponseEntity<List<HechoDinamicoDTO>> obtenerHechosRechazados() {
        List<HechoDinamico> hechos = hechoService.obtenerHechosPorEstado(EstadoRevision.RECHAZADO);
        return ResponseEntity.ok(HechoDinamicoMapper.toDTOList(hechos));
    }

    @GetMapping("/hechos/sugerencias")
    public ResponseEntity<List<HechoDinamicoDTO>> obtenerHechosConSugerencias() {
        List<HechoDinamico> hechos = hechoService.obtenerHechosPorEstado(EstadoRevision.ACEPTADO_CON_SUGERENCIAS);
        return ResponseEntity.ok(HechoDinamicoMapper.toDTOList(hechos));
    }

    // Cargar hecho con opción de adjuntar multimedia (opcional)
    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HechoDinamico> crearHecho(
            @RequestHeader(name = "X-Contribuyente-Id", required = false) String idContribuyente,
            @RequestPart("hecho") String dtoJson,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos
    ) {
        try {
            // Parseo manual del JSON (compatible con Spring Boot 2.x / Java 8)
            ObjectMapper mapper = new ObjectMapper();
            CrearHechoDTO dto = mapper.readValue(dtoJson, CrearHechoDTO.class);

            // Construcción del hecho
            HechoDinamico hecho = new HechoDinamico(dto.getTitulo(), dto.getDescripcion(), dto.getCategoria());
            if (dto.getFechaHecho() != null && !dto.getFechaHecho().isEmpty()) {
                // Convertir String a LocalDateTime
                hecho.setFechaHecho(LocalDateTime.parse(dto.getFechaHecho()));
            }
            if (dto.getLatitud() != 0.0 && dto.getLongitud() != 0.0) {
                hecho.setUbicacion(new UbicacionDinamica(dto.getLatitud(), dto.getLongitud()));
            }
            
            // Setear explícitamente esAnonimo del DTO
            if (dto.getEsAnonimo() != null) {
                hecho.setEsAnonimo(dto.getEsAnonimo());
            }

            // === Manejo de multimedia opcional (sin transferTo) ===
            List<MultimediaDinamica> multimedias = new ArrayList<>();
            if (archivos != null && !archivos.isEmpty()) {
                Path base = Paths.get(uploadRoot, "multimedia").toAbsolutePath().normalize();
                if (!Files.exists(base)) {
                    Files.createDirectories(base);
                }

                for (MultipartFile file : archivos) {
                    String original = file.getOriginalFilename();
                    String limpio = sanitizarNombre(original);
                    String nombreArchivo = System.currentTimeMillis() + "_" + limpio;

                    Path destino = base.resolve(nombreArchivo).normalize();
                    try (InputStream in = file.getInputStream()) {
                        Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
                    }

                    MultimediaDinamica media = new MultimediaDinamica(
                            original,
                            detectarTipo(file.getContentType()),
                            destino.toString(),                 // ruta absoluta en disco
                            file.getContentType(),
                            file.getSize()
                    );
                    multimedias.add(media);
                }
            }
            hecho.setMultimedias(multimedias);

            // Guardado según contribuyente (o anónimo)
            HechoDinamico hechoCreado;
            if (dto.getEsAnonimo() || idContribuyente == null || idContribuyente.isEmpty()) {
                hechoCreado = hechoService.crearHechoAnonimo(hecho);
            } else {
                hechoCreado = hechoService.crearHechoConContribuyente(hecho, idContribuyente);
            }

            return ResponseEntity.ok(hechoCreado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Cargar hecho con JSON puro (sin multimedia) - Para llamadas desde otros microservicios
    @PostMapping(value = "/cargar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> crearHechoJSON(
            @RequestBody CrearHechoDTO dto
    ) {
        try {
            // Construcción del hecho
            HechoDinamico hecho = new HechoDinamico(dto.getTitulo(), dto.getDescripcion(), dto.getCategoria());
            if (dto.getFechaHecho() != null && !dto.getFechaHecho().isEmpty()) {
                // Convertir String a LocalDateTime
                hecho.setFechaHecho(LocalDateTime.parse(dto.getFechaHecho()));
            }
            if (dto.getLatitud() != 0.0 && dto.getLongitud() != 0.0) {
                hecho.setUbicacion(new UbicacionDinamica(dto.getLatitud(), dto.getLongitud()));
            }
            
            // Setear explícitamente esAnonimo del DTO
            if (dto.getEsAnonimo() != null) {
                hecho.setEsAnonimo(dto.getEsAnonimo());
            }

            // Buscar contribuyente por idAgregador si está presente
            HechoDinamico hechoCreado;
            if (dto.getIdContribuyente() != null) {
                // Buscar contribuyente en MongoDB por idAgregador
                ContribuyenteDinamico contribuyente = contribuyenteService.buscarPorIdAgregador(dto.getIdContribuyente());
                if (contribuyente != null) {
                    hechoCreado = hechoService.crearHechoConContribuyente(hecho, contribuyente.getId());
                } else {
                    return ResponseEntity.badRequest()
                            .body("Contribuyente no encontrado con ID del agregador: " + dto.getIdContribuyente());
                }
            } else {
                // Crear como anónimo
                hechoCreado = hechoService.crearHechoAnonimo(hecho);
            }

            return ResponseEntity.ok(hechoCreado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al crear el hecho: " + e.getMessage());
        }
    }

    private String detectarTipo(String mime) {
        if (mime == null) return "desconocido";
        if (mime.startsWith("image/")) return "imagen";
        if (mime.startsWith("video/")) return "video";
        if (mime.startsWith("audio/")) return "audio";
        return "documento";
    }

    // Evita traversal y caracteres inválidos en Windows:  <>:"/\|?*
    private static String sanitizarNombre(String nombre) {
        if (nombre == null) return "archivo";
        nombre = nombre.replaceAll("[\\\\/]+", "_");
        nombre = nombre.replaceAll("[<>:\"/\\\\|?*]+", "_");
        if (nombre.length() > 180) nombre = nombre.substring(nombre.length() - 180);
        return nombre.trim().isEmpty() ? "archivo" : nombre;
    }

    @PutMapping(value = "/hechos/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editarHecho(
            @PathVariable String id,
            @RequestHeader(name = "X-Contribuyente-Id", required = true) String idContribuyente,
            @RequestBody EditarHechoDTO dto) {

        try {
            if (idContribuyente == null || idContribuyente.isEmpty()) {
                return ResponseEntity.status(401)
                        .body("Debe proporcionar el ID del contribuyente en el header X-Contribuyente-Id");
            }

            Optional<HechoDinamico> hechoOpt = hechoService.buscarPorId(id);
            if (!hechoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HechoDinamico hecho = hechoOpt.get();

            if (hecho.isEsAnonimo()) {
                return ResponseEntity.status(403)
                        .body("No se pueden editar hechos anónimos");
            }

            if (hecho.getContribuyente() == null) {
                return ResponseEntity.status(403)
                        .body("El hecho no tiene un contribuyente asociado");
            }

            if (!hecho.getContribuyente().getId().equals(idContribuyente)) {
                return ResponseEntity.status(403)
                        .body("Solo el autor del hecho puede editarlo");
            }

            if (dto.getNuevoTitulo() != null) {
                hecho.setTitulo(dto.getNuevoTitulo());
            }
            if (dto.getNuevaDescripcion() != null) {
                hecho.setDescripcion(dto.getNuevaDescripcion());
            }
            if (dto.getNuevaCategoria() != null) {
                hecho.setCategoria(dto.getNuevaCategoria());
            }

            hecho.setEstadoRevision(EstadoRevision.PENDIENTE);
            HechoDinamico hechoActualizado = hechoService.crearHecho(hecho);
            return ResponseEntity.ok(hechoActualizado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtener contribuyente por ID del agregador (para sincronización)
     */
    @GetMapping("/contribuyentes/por-id-agregador/{idAgregador}")
    public ResponseEntity<ContribuyenteDinamico> obtenerContribuyentePorIdAgregador(@PathVariable Long idAgregador) {
        try {
            ContribuyenteDinamico contribuyente = contribuyenteService.buscarPorIdAgregador(idAgregador);
            if (contribuyente != null) {
                return ResponseEntity.ok(contribuyente);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipo")
    public ResponseEntity<String> obtenerTipo() {
        return ResponseEntity.ok("DINAMICA");
    }

    @GetMapping("/identificador")
    public ResponseEntity<String> obtenerIdentificador() {
        return ResponseEntity.ok("fuente-dinamica-mongodb");
    }

    @PostMapping("/registrarse")
    public ResponseEntity<ContribuyenteDinamico> registrarContribuyente(@RequestBody RegistrarContribuyenteDTO contribDto) {
        try {
            ContribuyenteDinamico contribuyente = new ContribuyenteDinamico();
            contribuyente.setIdAgregador(contribDto.getIdAgregador()); // Guardar referencia al ID del agregador
            contribuyente.setNombre(contribDto.getNombre());
            contribuyente.setApellido(contribDto.getApellido());
            contribuyente.setFechaNacimiento(contribDto.getFechaNacimiento());
            
            // Manejar tipoUsuario desde TipoUsuario o String
            if (contribDto.getTipoUsuario() != null) {
                contribuyente.setTipoUsuario(contribDto.getTipoUsuario());
            } else if (contribDto.getTipoUsuarioString() != null) {
                try {
                    contribuyente.setTipoUsuario(TipoUsuario.valueOf(contribDto.getTipoUsuarioString()));
                } catch (IllegalArgumentException e) {
                    contribuyente.setTipoUsuario(TipoUsuario.CONTRIBUYENTE); // Por defecto
                }
            } else {
                contribuyente.setTipoUsuario(TipoUsuario.CONTRIBUYENTE); // Por defecto
            }
            
            ContribuyenteDinamico nuevo = contribuyenteService.registrarContribuyente(contribuyente);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/contribuyentes")
    public ResponseEntity<List<ContribuyenteDTO>> obtenerContribuyentes() {
        List<ContribuyenteDinamico> contribuyentes = contribuyenteService.obtenerTodosActivos();
        List<ContribuyenteDTO> dtos = contribuyentes.stream()
                .map(this::convertirADTO)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Convierte ContribuyenteDinamico a ContribuyenteDTO para sincronización
     */
    private ContribuyenteDTO convertirADTO(ContribuyenteDinamico contribuyente) {
        if (contribuyente == null) return null;
        
        ContribuyenteDTO dto = new ContribuyenteDTO();
        dto.setIdAgregador(contribuyente.getIdAgregador());
        dto.setNombre(contribuyente.getNombre());
        dto.setApellido(contribuyente.getApellido());
        dto.setFechaNacimiento(contribuyente.getFechaNacimiento());
        
        // Mapear TipoUsuario a Rol
        if (contribuyente.getTipoUsuario() != null) {
            switch (contribuyente.getTipoUsuario()) {
                case ADMINISTRADOR:
                    dto.setRol(com.metamapa.entities.rol.Rol.ADMINISTRADOR);
                    break;
                case CONTRIBUYENTE:
                default:
                    dto.setRol(com.metamapa.entities.rol.Rol.CONTRIBUYENTE);
                    break;
            }
        } else {
            dto.setRol(com.metamapa.entities.rol.Rol.CONTRIBUYENTE);
        }
        
        return dto;
    }

    @GetMapping("/contribuyentes/{id}")
    public ResponseEntity<ContribuyenteDinamico> obtenerContribuyentePorId(@PathVariable String id) {
        Optional<ContribuyenteDinamico> contribuyente = contribuyenteService.buscarPorId(id);
        return contribuyente.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/hechos/{id}/estado", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> actualizarEstadoHecho(
            @PathVariable String id,
            @RequestHeader(name = "X-Contribuyente-Id", required = true) String idAdministrador,
            @RequestBody RevisarHechoDTO dto) {

        try {
            Optional<ContribuyenteDinamico> usuarioOpt = contribuyenteService.buscarPorId(idAdministrador);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.status(401).build();
            }

            ContribuyenteDinamico usuario = usuarioOpt.get();
            if (!usuario.esAdministrador()) {
                return ResponseEntity.status(403).build();
            }

            switch (dto.getEstado()) {
                case ACEPTADO:
                    hechoService.aceptarHecho(id);
                    break;
                case RECHAZADO:
                    if (dto.getSugerencia() == null || dto.getSugerencia().trim().isEmpty()) {
                        return ResponseEntity.badRequest().build();
                    }
                    hechoService.rechazarHecho(id, dto.getSugerencia());
                    break;
                case ACEPTADO_CON_SUGERENCIAS:
                    if (dto.getSugerencia() == null || dto.getSugerencia().trim().isEmpty()) {
                        return ResponseEntity.badRequest().build();
                    }
                    hechoService.aceptarHechoConSugerencias(id, dto.getSugerencia());
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // Hecho no encontrado
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
