package com.metamapa.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints públicos (sin autenticación)
 */
@RestController
@RequestMapping("/api/publica")
public class PublicController {

    @GetMapping("/test")
    public Map<String, Object> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Este es un endpoint público - no requiere autenticación");
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/info")
    public Map<String, String> publicInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "MetaMapa Auth Service");
        info.put("version", "1.0.0");
        info.put("description", "Servicio de autenticación y autorización con Auth0");
        return info;
    }
}
