package com.metamapa.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de debugging para desarrollo
 * 
 * IMPORTANTE: Este controlador debe ser deshabilitado en producción
 * o protegido adecuadamente. Expone información sensible del JWT.
 * 
 * Útil para:
 * - Verificar que los tokens se están enviando correctamente
 * - Ver qué claims contiene el JWT
 * - Debugging de problemas de autenticación
 * 
 * @author MetaMapa Team
 */
@Slf4j
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    /**
     * Muestra todos los claims del JWT
     * 
     * ADVERTENCIA: Este endpoint expone información sensible.
     * Solo usar en desarrollo.
     * 
     * @param jwt El token JWT
     * @return Todos los claims del JWT
     */
    @GetMapping("/jwt")
    public ResponseEntity<Map<String, Object>> debugJwt(
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Debug JWT solicitado para usuario: {}", jwt.getSubject());
        
        Map<String, Object> debugInfo = new HashMap<>();
        
        // Claims estándar
        debugInfo.put("subject", jwt.getSubject());
        debugInfo.put("issuer", jwt.getIssuer());
        debugInfo.put("audience", jwt.getAudience());
        debugInfo.put("issuedAt", jwt.getIssuedAt());
        debugInfo.put("expiresAt", jwt.getExpiresAt());
        debugInfo.put("notBefore", jwt.getNotBefore());
        
        // Verificar si el token está expirado
        Instant now = Instant.now();
        debugInfo.put("isExpired", jwt.getExpiresAt().isBefore(now));
        debugInfo.put("secondsUntilExpiration", jwt.getExpiresAt().getEpochSecond() - now.getEpochSecond());
        
        // Todos los claims (incluyendo personalizados)
        debugInfo.put("allClaims", jwt.getClaims());
        
        // Roles específicamente
        debugInfo.put("roles", jwt.getClaimAsStringList("https://metamapa.com/roles"));
        
        // Headers del JWT
        debugInfo.put("headers", jwt.getHeaders());
        
        return ResponseEntity.ok(debugInfo);
    }

    /**
     * Verifica la configuración de seguridad
     * 
     * @return Estado de la configuración
     */
    @GetMapping("/security-config")
    public ResponseEntity<Map<String, Object>> securityConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("message", "Si ves esto, la configuración de seguridad está funcionando");
        config.put("timestamp", Instant.now());
        return ResponseEntity.ok(config);
    }
}
