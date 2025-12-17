package com.metamapa.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de seguridad
 * 
 * Captura y formatea las excepciones de autenticación y autorización
 * para devolver respuestas consistentes al cliente.
 * 
 * @author MetaMapa Team
 */
@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * Maneja excepciones de autenticación (401 Unauthorized)
     * 
     * Se lanza cuando:
     * - No se proporciona token
     * - Token inválido
     * - Token expirado
     * 
     * @param ex La excepción de autenticación
     * @return Respuesta con código 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex) {
        
        log.error("Error de autenticación: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Token de autenticación inválido o expirado");
        errorResponse.put("details", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Maneja excepciones de autorización (403 Forbidden)
     * 
     * Se lanza cuando:
     * - Usuario autenticado pero sin los permisos necesarios
     * - Usuario no tiene el rol requerido
     * 
     * @param ex La excepción de acceso denegado
     * @return Respuesta con código 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        
        log.error("Acceso denegado: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "No tienes permisos para acceder a este recurso");
        errorResponse.put("details", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Maneja cualquier otra excepción no capturada (500 Internal Server Error)
     * 
     * @param ex La excepción genérica
     * @return Respuesta con código 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {
        
        log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", "Ha ocurrido un error inesperado");
        // No incluir detalles en producción por seguridad
        // errorResponse.put("details", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
