package com.metamapa.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para obtener información del usuario autenticado
 * 
 * Este controlador proporciona endpoints para que el frontend
 * obtenga información sobre el usuario actual (perfil, roles, etc.)
 * 
 * @author MetaMapa Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Obtiene la información del usuario autenticado
     * 
     * Endpoint accesible para cualquier usuario autenticado.
     * Devuelve información extraída del JWT token.
     * 
     * @param jwt El token JWT del usuario autenticado (inyectado automáticamente)
     * @return Información del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {
        
        log.debug("Usuario solicitando información: {}", jwt.getSubject());
        
        Map<String, Object> userInfo = new HashMap<>();
        
        // Información básica del usuario
        userInfo.put("userId", jwt.getSubject());
        userInfo.put("email", jwt.getClaimAsString("email"));
        userInfo.put("name", jwt.getClaimAsString("name"));
        userInfo.put("picture", jwt.getClaimAsString("picture"));
        userInfo.put("emailVerified", jwt.getClaimAsBoolean("email_verified"));
        
        // Roles del usuario
        List<String> roles = jwt.getClaimAsStringList("https://metamapa.com/roles");
        userInfo.put("roles", roles != null ? roles : Collections.emptyList());
        
        // Información del token
        userInfo.put("issuedAt", jwt.getIssuedAt());
        userInfo.put("expiresAt", jwt.getExpiresAt());
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Verifica si el usuario tiene un rol específico
     * 
     * @param jwt El token JWT del usuario autenticado
     * @return Mapa con el rol y si el usuario lo tiene
     */
    @GetMapping("/roles/check")
    public ResponseEntity<Map<String, Boolean>> checkRoles(
            @AuthenticationPrincipal Jwt jwt) {
        
        List<String> roles = jwt.getClaimAsStringList("https://metamapa.com/roles");
        
        Map<String, Boolean> roleCheck = new HashMap<>();
        roleCheck.put("isAdmin", roles != null && roles.contains("ADMIN"));
        roleCheck.put("isUser", roles != null && roles.contains("USER"));
        
        return ResponseEntity.ok(roleCheck);
    }

    /**
     * Endpoint de health check para verificar que el servicio está funcionando
     * 
     * @return Mensaje de estado
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        return ResponseEntity.ok(response);
    }
}
