package com.metamapa.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para endpoints protegidos
 */
@RestController
@RequestMapping("/api")
public class ProtectedController {

    /**
     * Endpoint protegido - requiere autenticación (cualquier usuario autenticado)
     */
    @GetMapping("/interna/test")
    public Map<String, Object> protectedTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint protegido - requiere autenticación");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities()
            .stream()
            .map(a -> a.getAuthority())
            .collect(Collectors.toList()));
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Endpoint admin - requiere rol ADMIN
     */
    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint admin - solo usuarios con rol ADMIN");
        response.put("user", authentication.getName());
        response.put("roles", authentication.getAuthorities()
            .stream()
            .map(a -> a.getAuthority())
            .collect(Collectors.toList()));
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * Endpoint user - requiere rol USER
     */
    @GetMapping("/user/test")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> userTest(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint user - solo usuarios con rol USER");
        response.put("user", authentication.getName());
        response.put("roles", authentication.getAuthorities()
            .stream()
            .map(a -> a.getAuthority())
            .collect(Collectors.toList()));
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
