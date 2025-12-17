package com.metamapa.service;

import com.metamapa.dto.CargaCsvResultado;
import com.metamapa.entities.hechos.Hecho;
import com.metamapa.entities.hechos.OrigenHecho;
import com.metamapa.entities.ubicaciones.Ubicacion;
import com.metamapa.entities.archivosDataset.ArchivoDataset;
import com.metamapa.entities.archivosDataset.EstadoArchivo;
import com.metamapa.repository.ArchivoDatasetRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FuenteEstaticaService {

    @Autowired
    private ArchivoDatasetRepository archivoRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Guarda el archivo CSV en el file system y registra metadata en MySQL con estado PENDIENTE.
     * El procesamiento real ocurre cuando el agregador llama a procesarArchivosPendientes().
     */
    public CargaCsvResultado cargarHechos(MultipartFile file) {
        try {
            // Validar que sea CSV
            String nombreOriginal = file.getOriginalFilename();
            if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".csv")) {
                return new CargaCsvResultado(false, "El archivo debe ser CSV", 0, null);
            }

            // Calcular hash para detectar duplicados
            String hash = calcularSha256(file.getBytes());

            // Verificar si ya existe
            ArchivoDataset existe = archivoRepository.findByHash(hash).orElse(null);
            if (existe != null) {
                return new CargaCsvResultado(
                    false, 
                    "Archivo duplicado. Ya fue cargado el " + existe.getFechaCarga() + 
                    " con estado: " + existe.getEstado(), 
                    0, 
                    existe.getId()
                );
            }

            // Guardar archivo en file system
            String rutaRelativa = fileStorageService.guardarArchivo(file, hash);

            // Guardar metadata en MySQL con estado PENDIENTE
            ArchivoDataset archivo = new ArchivoDataset(
                nombreOriginal,
                hash,
                rutaRelativa,
                EstadoArchivo.PENDIENTE
            );
            ArchivoDataset guardado = archivoRepository.save(archivo);

            return new CargaCsvResultado(
                true, 
                "Archivo guardado exitosamente. Estado: PENDIENTE. Será procesado cuando el agregador lo solicite.",
                0,  // No se procesa todavía
                guardado.getId()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new CargaCsvResultado(false, "Error al guardar archivo: " + e.getMessage(), 0, null);
        }
    }

    /**
     * Procesa todos los archivos con estado PENDIENTE.
     * Este método es llamado por el agregador cuando ejecuta su proceso de consolidación.
     * Retorna hechos con origenArchivoId seteado para que el agregador guarde la relación N-N.
     */
    public List<Hecho> procesarArchivosPendientes() {
        List<Hecho> todosLosHechos = new ArrayList<>();

        List<ArchivoDataset> pendientes = archivoRepository.findByEstado(EstadoArchivo.PENDIENTE);

        for (ArchivoDataset archivo : pendientes) {
            try {
                // Leer archivo desde file system
                byte[] contenido = fileStorageService.leerArchivo(archivo.getRutaArchivo());

                // Parsear CSV
                List<Hecho> hechos = parsearCSV(contenido);

                // Si no se parseó ningún hecho, marcar como FALLIDO
                if (hechos.isEmpty()) {
                    log.warn("Archivo {} no generó ningún hecho válido", archivo.getNombreArchivo());
                    archivo.setEstado(EstadoArchivo.FALLIDO);
                    archivo.setErrores("No se pudo parsear ningún hecho válido del CSV");
                    archivoRepository.save(archivo);
                    continue;
                }

                // CRÍTICO: Setear origenArchivoId para trazabilidad
                for (Hecho hecho : hechos) {
                    hecho.setOrigenArchivoId(archivo.getId());
                }

                todosLosHechos.addAll(hechos);

                // Actualizar estado a PROCESADO
                log.info("Archivo {} procesado exitosamente: {} hechos", archivo.getNombreArchivo(), hechos.size());
                archivo.setEstado(EstadoArchivo.PROCESADO);
                archivo.setFilasProcesadas(hechos.size());
                archivoRepository.save(archivo);

            } catch (Exception e) {
                log.error("Error procesando archivo {}: {}", archivo.getNombreArchivo(), e.getMessage(), e);
                // Marcar como FALLIDO
                archivo.setEstado(EstadoArchivo.FALLIDO);
                archivo.setErrores("Error al procesar: " + e.getMessage());
                archivoRepository.save(archivo);
            }
        }

        return todosLosHechos;
    }

    /**
     * Parsea el contenido del CSV y retorna una lista de hechos.
     * Formato CSV esperado: Titulo,Descripción,Categoría,Latitud,Longitud,Fecha del hecho
     */
    private List<Hecho> parsearCSV(byte[] contenido) throws Exception {
        List<Hecho> hechos = new ArrayList<>();
        
        String csvContent = new String(contenido, StandardCharsets.UTF_8);
        CSVReader csvReader = new CSVReader(new StringReader(csvContent));
        
        List<String[]> allRows = csvReader.readAll();
        csvReader.close();
        
        if (allRows.isEmpty()) {
            log.warn("CSV vacío");
            return hechos;
        }
        
        // Primera fila es el header
        String[] header = allRows.get(0);
        log.info("Header CSV: {}", String.join(", ", header));
        
        int lineaNumero = 1;
        for (int i = 1; i < allRows.size(); i++) {
            lineaNumero = i + 1;
            String[] campos = allRows.get(i);
            
            if (campos.length < 6) {
                log.warn("Línea {} malformada (tiene {} campos)", lineaNumero, campos.length);
                continue;
            }

            try {
                Hecho hecho = new Hecho();
                
                // Campos básicos (A: Titulo, B: Descripción, C: Categoría)
                hecho.setTitulo(campos[0].trim());
                hecho.setDescripcion(campos[1].trim());
                hecho.setCategoria(campos[2].trim());
                
                // Ubicación (D: Latitud, E: Longitud)
                Ubicacion ubicacion = new Ubicacion();
                ubicacion.setLatitud(Double.parseDouble(campos[3].trim()));
                ubicacion.setLongitud(Double.parseDouble(campos[4].trim()));
                hecho.setUbicacion(ubicacion);
                
                // Fecha del hecho: parsear campos[5] en formato DD/MM/YYYY
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate fecha = LocalDate.parse(campos[5].trim(), formatter);
                    hecho.setFechaHecho(fecha.atStartOfDay()); // Convertir a LocalDateTime a medianoche
                } catch (DateTimeParseException e) {
                    // Si falla el parseo, usar fecha fija para mantener consistencia en fingerprint
                    log.warn("Error parseando fecha '{}' en línea {}, usando fecha por defecto", campos[5], lineaNumero);
                    hecho.setFechaHecho(LocalDateTime.of(2025, 1, 1, 0, 0));
                }
                hecho.setFechaCarga(LocalDateTime.now());
                
                // Metadata
                hecho.setOrigen(OrigenHecho.DATASET);
                
                // Fuente para depuración
                List<String> fuentes = new ArrayList<>();
                fuentes.add("fuente-estatica");
                hecho.setFuentes(fuentes);
                
                hechos.add(hecho);
            } catch (NumberFormatException e) {
                log.warn("Error parseando coordenadas en línea {}: {}", lineaNumero, e.getMessage());
                continue;
            }
        }

        log.info("Total de hechos parseados del CSV: {}", hechos.size());
        return hechos;
    }

    /**
     * Calcula hash SHA-256 del archivo para detección de duplicados.
     */
    private String calcularSha256(byte[] contenido) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(contenido);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Obtiene todos los archivos registrados (para endpoint GET /archivos).
     */
    public List<ArchivoDataset> obtenerTodosLosArchivos() {
        return archivoRepository.findAll();
    }
}
