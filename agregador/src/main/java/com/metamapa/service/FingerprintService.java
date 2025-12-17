package com.metamapa.service;

import com.metamapa.entities.hechos.Hecho;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para calcular fingerprints únicos de hechos
 * Usado por el agregador para evitar duplicados
 */
@Service
public class FingerprintService {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Calcula un fingerprint único para un hecho basado en sus datos clave
     */
    public String calcularFingerprint(Hecho hecho) {
        if (hecho == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Campos clave para identificar unicidad
        if (hecho.getTitulo() != null) {
            sb.append(hecho.getTitulo().toLowerCase().trim());
        }
        sb.append("|");
        
        if (hecho.getDescripcion() != null) {
            sb.append(hecho.getDescripcion().toLowerCase().trim());
        }
        sb.append("|");
        
        if (hecho.getCategoria() != null) {
            sb.append(hecho.getCategoria());
        }
        sb.append("|");
        
        if (hecho.getUbicacion() != null) {
            sb.append(hecho.getUbicacion().getLatitud()).append(",");
            sb.append(hecho.getUbicacion().getLongitud());
        }
        sb.append("|");
        
        if (hecho.getFechaHecho() != null) {
            sb.append(hecho.getFechaHecho().format(FORMATTER));
        }
        
        // Generar hash SHA-256
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            // Fallback: usar hashCode simple
            return String.valueOf(sb.toString().hashCode());
        }
    }
}
