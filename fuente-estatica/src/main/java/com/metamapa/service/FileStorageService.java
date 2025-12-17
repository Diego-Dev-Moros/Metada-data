package com.metamapa.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Servicio para gestión de archivos CSV en el file system.
 */
@Service
@Slf4j
public class FileStorageService {
    
    private final Path directorioBase;
    
    public FileStorageService(@Value("${file.storage.location:uploads/csv}") String ubicacion) {
        this.directorioBase = Paths.get(ubicacion).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directorioBase);
            log.info("Directorio de almacenamiento creado/verificado: {}", this.directorioBase);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", e);
        }
    }
    
    /**
     * Guarda un archivo en el file system con un nombre único basado en el hash.
     * 
     * @param file Archivo a guardar
     * @param hash Hash SHA-256 del archivo
     * @return Ruta relativa del archivo guardado
     */
    public String guardarArchivo(MultipartFile file, String hash) throws IOException {
        // Generar nombre único: hash + extensión original
        String extension = obtenerExtension(file.getOriginalFilename());
        String nombreArchivo = hash.substring(0, 12) + extension; // Primeros 12 chars del hash
        
        Path rutaDestino = this.directorioBase.resolve(nombreArchivo);
        
        // Copiar archivo
        Files.copy(file.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Archivo guardado: {}", rutaDestino);
        
        // Retornar ruta relativa
        return nombreArchivo;
    }
    
    /**
     * Lee el contenido de un archivo desde el file system.
     * 
     * @param rutaRelativa Ruta relativa del archivo
     * @return Contenido del archivo como byte array
     */
    public byte[] leerArchivo(String rutaRelativa) throws IOException {
        Path rutaCompleta = this.directorioBase.resolve(rutaRelativa);
        
        if (!Files.exists(rutaCompleta)) {
            throw new IOException("Archivo no encontrado: " + rutaRelativa);
        }
        
        return Files.readAllBytes(rutaCompleta);
    }
    
    /**
     * Elimina un archivo del file system.
     * 
     * @param rutaRelativa Ruta relativa del archivo
     */
    public void eliminarArchivo(String rutaRelativa) throws IOException {
        Path rutaCompleta = this.directorioBase.resolve(rutaRelativa);
        Files.deleteIfExists(rutaCompleta);
        log.info("Archivo eliminado: {}", rutaCompleta);
    }
    
    /**
     * Obtiene la ruta completa de un archivo.
     * 
     * @param rutaRelativa Ruta relativa del archivo
     * @return Path absoluto
     */
    public Path obtenerRutaCompleta(String rutaRelativa) {
        return this.directorioBase.resolve(rutaRelativa);
    }
    
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".csv";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}
