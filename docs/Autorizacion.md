# 🔐 Guía Completa de Autorización con Auth0 y Spring Boot 3.0

## 📋 Tabla de Contenidos

1. [Conceptos Fundamentales](#conceptos-fundamentales)
2. [Arquitectura de Seguridad](#arquitectura-de-seguridad)
3. [Configuración de Auth0](#configuración-de-auth0)
4. [Implementación Backend (Spring Boot)](#implementación-backend)
5. [Implementación Frontend (React)](#implementación-frontend)
6. [JWT: Anatomía y Validación](#jwt-anatomía-y-validación)
7. [Control de Acceso Basado en Roles (RBAC)](#rbac)
8. [Casos de Uso y Ejemplos](#casos-de-uso)
9. [Seguridad y Mejores Prácticas](#seguridad)
10. [Debugging y Troubleshooting](#debugging)
11. [Preguntas Frecuentes](#preguntas-frecuentes)

---

## 🎓 Conceptos Fundamentales {#conceptos-fundamentales}

### ¿Qué es Autenticación vs Autorización?

| Concepto | Definición | Pregunta que responde | Ejemplo |
|----------|------------|----------------------|---------|
| **Autenticación** | Verificar la identidad del usuario | "¿Quién eres?" | Login con email/password |
| **Autorización** | Verificar los permisos del usuario | "¿Qué puedes hacer?" | Acceso a panel admin |

### OAuth 2.0 y OpenID Connect (OIDC)

**OAuth 2.0:** Protocolo de autorización que permite a aplicaciones obtener acceso limitado a recursos.

**OpenID Connect (OIDC):** Capa de identidad sobre OAuth 2.0 que agrega autenticación.

```
OAuth 2.0 → Autorización (acceso a recursos)
OIDC → Autenticación (identidad del usuario) + OAuth 2.0
```

### ¿Qué es un JWT (JSON Web Token)?

Un JWT es un token de acceso que contiene información (claims) en formato JSON, firmado digitalmente.

**Estructura de un JWT:**
```
[Header].[Payload].[Signature]
```

**Ejemplo real:**
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhdXRoMHwxMjM0NTYiLCJuYW1lIjoiSnVhbiBQw6lyZXoiLCJlbWFpbCI6Imp1YW5AZXhhbXBsZS5jb20iLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDA4NjQwMCwiYXVkIjoiaHR0cHM6Ly9tZXRhbWFwYS1hcGkifQ.signature_aqui
```

**Decodificado:**

```json
// Header
{
  "alg": "RS256",
  "typ": "JWT"
}

// Payload (Claims)
{
  "sub": "auth0|123456",           // Subject (ID del usuario)
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "roles": ["ADMIN"],              // Roles personalizados
  "iat": 1700000000,               // Issued at (timestamp)
  "exp": 1700086400,               // Expiration (timestamp)
  "aud": "https://metamapa-api",   // Audience (API autorizada)
  "iss": "https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/"  // Issuer
}

// Signature (Firma digital)
RSASHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  private_key
)
```

### ¿Por qué JWT?

✅ **Stateless:** No requiere almacenar sesiones en servidor  
✅ **Portable:** Se envía en cada request (header HTTP)  
✅ **Seguro:** Firmado digitalmente, no puede ser alterado  
✅ **Self-contained:** Contiene toda la información necesaria  
✅ **Escalable:** Funciona bien en arquitecturas distribuidas  

---

## 🏗️ Arquitectura de Seguridad {#arquitectura-de-seguridad}

### Flujo Completo de Autorización

```
┌─────────────┐
│   USUARIO   │
└──────┬──────┘
       │ 1. Accede a la app
       ▼
┌─────────────────────────────────────────┐
│         FRONTEND (React)                │
│  - Detecta que no hay token             │
│  - Redirige a Auth0 Login               │
└──────┬──────────────────────────────────┘
       │ 2. Redirect a Auth0
       ▼
┌─────────────────────────────────────────┐
│         AUTH0 (Universal Login)         │
│  - Usuario ingresa credenciales         │
│  - O selecciona social login            │
│  - Auth0 valida identidad               │
└──────┬──────────────────────────────────┘
       │ 3. Devuelve Authorization Code
       ▼
┌─────────────────────────────────────────┐
│         FRONTEND (React)                │
│  - Recibe code en callback              │
│  - Intercambia code por tokens          │
│  - Almacena Access Token (JWT)          │
└──────┬──────────────────────────────────┘
       │ 4. API Request + JWT en Header
       │    Authorization: Bearer <token>
       ▼
┌─────────────────────────────────────────┐
│    BACKEND (Spring Boot + Security)     │
│  Step 1: JWT Authentication Filter      │
│    - Extrae token del header            │
│    - Valida firma con clave pública     │
│    - Verifica issuer, audience, exp     │
│  Step 2: Security Context                │
│    - Crea Authentication object         │
│    - Extrae roles del token             │
│    - Establece contexto de seguridad    │
│  Step 3: Authorization                   │
│    - Verifica @PreAuthorize             │
│    - Comprueba hasRole("ADMIN")         │
│    - Permite o deniega acceso           │
└──────┬──────────────────────────────────┘
       │ 5. Response (200 OK o 403 Forbidden)
       ▼
┌─────────────┐
│   USUARIO   │
└─────────────┘
```

### Componentes de Seguridad en Spring Boot

```
┌────────────────────────────────────────────────────┐
│              SecurityFilterChain                   │
│  ┌──────────────────────────────────────────────┐ │
│  │  1. CorsFilter                               │ │
│  │     - Configuración CORS                     │ │
│  └──────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────┐ │
│  │  2. JwtAuthenticationFilter                  │ │
│  │     - Extracción del JWT                     │ │
│  │     - Validación del token                   │ │
│  │     - Establecimiento del SecurityContext    │ │
│  └──────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────┐ │
│  │  3. AuthorizationFilter                      │ │
│  │     - Verifica reglas de autorización        │ │
│  │     - Comprueba roles y permisos             │ │
│  └──────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────┐ │
│  │  4. ExceptionTranslationFilter               │ │
│  │     - Maneja AccessDeniedException           │ │
│  │     - Maneja AuthenticationException         │ │
│  └──────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────┘
```

---

## ⚙️ Configuración de Auth0 {#configuración-de-auth0}

### Paso 1: Crear Application en Auth0

1. **Accede al Dashboard:**
   - URL: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/
   - Tu aplicación: `0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO`

2. **Settings → Basic Information:**
   ```
   Domain: dev-x8zpgn3i6vnkjg4m.us.auth0.com
   Client ID: 0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO
   Client Secret: [Ver en dashboard - NO compartir]
   ```

3. **Application Type:**
   - Selecciona: **Single Page Application (SPA)**

### Paso 2: Configurar URLs

```
Allowed Callback URLs:
http://localhost:5173/callback
http://localhost:3000/callback
https://tu-dominio-produccion.com/callback

Allowed Logout URLs:
http://localhost:5173
http://localhost:3000
https://tu-dominio-produccion.com

Allowed Web Origins:
http://localhost:5173
http://localhost:8080
https://tu-dominio-produccion.com

Allowed Origins (CORS):
http://localhost:5173
http://localhost:8080
```

### Paso 3: Crear API en Auth0

1. **Dashboard → Applications → APIs → Create API**
   ```
   Name: MetaMapa API
   Identifier: https://metamapa-api
   Signing Algorithm: RS256
   ```

2. **Este identifier es tu "audience"** - lo usarás en configuraciones

### Paso 4: Configurar Roles

1. **Dashboard → User Management → Roles → Create Role**

   **Role: ADMIN**
   ```
   Name: ADMIN
   Description: Administrador del sistema MetaMapa
   ```

   **Role: USER**
   ```
   Name: USER
   Description: Usuario regular de MetaMapa
   ```

2. **Asignar permisos a roles:**

   **ADMIN Permissions:**
   ```
   read:hechos
   write:hechos
   delete:hechos
   approve:hechos
   manage:colecciones
   manage:users
   view:stats
   ```

   **USER Permissions:**
   ```
   read:hechos
   write:hechos
   create:solicitudes
   ```

### Paso 5: Configurar Action para incluir roles en JWT

Auth0 necesita un **Action** para incluir roles en el token JWT.

1. **Dashboard → Actions → Flows → Login**
2. **Create Action → Custom**
   ```javascript
   /**
   * Handler que se ejecuta durante el flujo de login
   */
   exports.onExecutePostLogin = async (event, api) => {
     const namespace = 'https://metamapa.com';
     
     // Obtener roles del usuario
     if (event.authorization) {
       // Agregar roles al Access Token
       api.accessToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
       
       // Agregar roles al ID Token
       api.idToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
     }
   };
   ```
3. **Deploy** el action
4. **Add to Flow** en el flujo de Login

### Paso 6: Configurar Social Connections

Para habilitar login con Google, Facebook, etc.:

1. **Dashboard → Authentication → Social**
2. **Activar los proveedores deseados:**
   - ✅ Google
   - ✅ GitHub
   - ✅ Facebook
   - ✅ Microsoft

3. **Para cada proveedor:**
   - Crear aplicación en la plataforma (Google Cloud, GitHub, etc.)
   - Copiar Client ID y Client Secret
   - Configurar en Auth0

**Ejemplo Google:**
```
Google Cloud Console → APIs & Services → Credentials
→ Create OAuth 2.0 Client ID
→ Copiar Client ID y Client Secret a Auth0
```

---

## 💻 Implementación Backend (Spring Boot) {#implementación-backend}

### Estructura del Módulo auth-service

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/metamapa/auth/
│   │   │   ├── AuthServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── Auth0Properties.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── filter/
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── converter/
│   │   │   │   └── Auth0JwtAuthenticationConverter.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   └── TokenService.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   └── exception/
│   │   │       └── AuthExceptionHandler.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/com/metamapa/auth/
│           ├── SecurityConfigTest.java
│           └── JwtValidationTest.java
└── pom.xml
```

### pom.xml - Dependencias

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.metamapa</groupId>
        <artifactId>metamapa</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>auth-service</artifactId>
    <name>MetaMapa Auth Service</name>
    <description>Servicio de autenticación y autorización con Auth0</description>

    <dependencies>
        <!-- Spring Boot Starter Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- OAuth2 Resource Server -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <!-- JWT Support -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-oauth2-jose</artifactId>
        </dependency>

        <!-- Auth0 SDK (opcional, para management API) -->
        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>auth0</artifactId>
            <version>2.9.0</version>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### application.yml

```yaml
server:
  port: 8086

spring:
  application:
    name: auth-service
  security:
    oauth2:
      resourceserver:
        jwt:
          # URL pública para obtener las claves de verificación JWT
          issuer-uri: https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/
          # Audience que debe estar presente en el JWT
          audiences:
            - https://metamapa-api

# Configuración personalizada de Auth0
auth0:
  domain: dev-x8zpgn3i6vnkjg4m.us.auth0.com
  audience: https://metamapa-api
  clientId: 0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO
  # Para operaciones de management (crear usuarios, etc.)
  # managementApiToken: ${AUTH0_MANAGEMENT_TOKEN}

# Logging para debugging
logging:
  level:
    org.springframework.security: DEBUG
    com.metamapa.auth: DEBUG
```

### SecurityConfig.java

```java
package com.metamapa.auth.config;

import com.metamapa.auth.converter.Auth0JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para MetaMapa
 * 
 * Esta clase configura:
 * - Validación de JWT tokens de Auth0
 * - Reglas de autorización para endpoints
 * - CORS para permitir requests desde frontend
 * - Conversión de roles de Auth0 a Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final Auth0JwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (no necesario para APIs stateless con JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar gestión de sesiones (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                .requestMatchers("/api/publica/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Endpoints administrativos (solo ADMIN)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Endpoints internos (cualquier usuario autenticado)
                .requestMatchers("/api/interna/**").authenticated()
                
                // Cualquier otro request requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar OAuth2 Resource Server con JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // Usar nuestro converter personalizado para extraer roles
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            );

        return http.build();
    }

    /**
     * Configuración CORS para permitir requests desde el frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (frontend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // Vite dev server
            "http://localhost:3000",  // React dev server alternativo
            "https://tu-dominio-produccion.com"
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept"
        ));
        
        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Auth0JwtAuthenticationConverter.java

```java
package com.metamapa.auth.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter que extrae los roles del JWT de Auth0 y los convierte
 * a GrantedAuthority de Spring Security
 * 
 * Auth0 incluye los roles en un claim personalizado con namespace,
 * por ejemplo: "https://metamapa.com/roles": ["ADMIN", "USER"]
 * 
 * Este converter extrae esos roles y los convierte al formato que
 * Spring Security espera: ROLE_ADMIN, ROLE_USER
 */
@Component
public class Auth0JwtAuthenticationConverter 
        implements Converter<Jwt, AbstractAuthenticationToken> {

    // Namespace usado en el Action de Auth0
    private static final String ROLES_CLAIM = "https://metamapa.com/roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extrae las autoridades (roles) del JWT
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Obtener el claim de roles
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        // Convertir a GrantedAuthority con prefijo ROLE_
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }
}
```

### Auth0Properties.java

```java
package com.metamapa.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración de Auth0
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth0")
public class Auth0Properties {
    
    /**
     * Dominio de Auth0 (ej: dev-x8zpgn3i6vnkjg4m.us.auth0.com)
     */
    private String domain;
    
    /**
     * Audience de la API (ej: https://metamapa-api)
     */
    private String audience;
    
    /**
     * Client ID de la aplicación
     */
    private String clientId;
    
    /**
     * Token de la Management API (opcional)
     * Se usa para operaciones administrativas como crear usuarios
     */
    private String managementApiToken;
}
```

### Aplicar Seguridad a Controladores Existentes

Ahora actualiza tus controladores existentes para usar autorización:

```java
package com.metamapa.gestorsolicitudes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HechoService hechoService;
    private final SolicitudService solicitudService;

    /**
     * Solo accesible para usuarios con rol ADMIN
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/hechos/{id}/aprobar")
    public ResponseEntity<?> aprobarHecho(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Obtener información del usuario autenticado
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        
        hechoService.aprobar(id, userId);
        return ResponseEntity.ok("Hecho aprobado por " + email);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/solicitudes/{id}/aprobar")
    public ResponseEntity<?> aprobarSolicitud(@PathVariable Long id) {
        solicitudService.aprobar(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/colecciones")
    public ResponseEntity<?> crearColeccion(@RequestBody ColeccionDTO dto) {
        Coleccion coleccion = coleccionService.crear(dto);
        return ResponseEntity.ok(coleccion);
    }
}

@RestController
@RequestMapping("/api/interna")
@RequiredArgsConstructor
public class InternaController {

    private final HechoService hechoService;

    /**
     * Accesible para cualquier usuario autenticado
     */
    @GetMapping("/hechos")
    public ResponseEntity<List<HechoDTO>> listarHechos(
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        List<HechoDTO> hechos = hechoService.listarTodos();
        return ResponseEntity.ok(hechos);
    }

    @PostMapping("/hechos")
    public ResponseEntity<?> reportarHecho(
            @RequestBody HechoDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Asociar el hecho al usuario que lo reporta
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        
        dto.setReportadoPor(userId);
        dto.setEmailReportante(email);
        
        Hecho hecho = hechoService.crear(dto);
        return ResponseEntity.ok(hecho);
    }

    /**
     * Usuario puede crear solicitud de eliminación sobre un hecho
     */
    @PostMapping("/hechos/{id}/solicitudes")
    public ResponseEntity<?> crearSolicitudEliminacion(
            @PathVariable Long id,
            @RequestBody SolicitudDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        dto.setHechoId(id);
        dto.setSolicitadoPor(userId);
        
        Solicitud solicitud = solicitudService.crear(dto);
        return ResponseEntity.ok(solicitud);
    }
}

@RestController
@RequestMapping("/api/publica")
public class PublicaController {

    /**
     * Endpoints públicos - NO requieren autenticación
     */
    @GetMapping("/colecciones")
    public ResponseEntity<List<ColeccionDTO>> listarColeccionesPublicas() {
        // Accesible sin autenticación
        List<ColeccionDTO> colecciones = coleccionService.listarPublicas();
        return ResponseEntity.ok(colecciones);
    }
}
```

---

## 🎨 Implementación Frontend (React) {#implementación-frontend}

### Instalación de Dependencias

```bash
cd frontEnd/metamapa-frontend
npm install @auth0/auth0-react axios
```

### Configuración en main.jsx

```jsx
// frontEnd/metamapa-frontend/src/main.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import App from './App';
import './index.css';

// Configuración de Auth0
const auth0Config = {
  domain: "dev-x8zpgn3i6vnkjg4m.us.auth0.com",
  clientId: "0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO",
  authorizationParams: {
    redirect_uri: window.location.origin + '/callback',
    audience: "https://metamapa-api",
    scope: "openid profile email"
  },
  // Cache tokens en memoria (más seguro para SPAs)
  cacheLocation: 'memory',
  // Usar refresh tokens
  useRefreshTokens: true
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Auth0Provider {...auth0Config}>
      <App />
    </Auth0Provider>
  </React.StrictMode>
);
```

### Context de Autenticación

```jsx
// src/context/AuthContext.jsx
import { createContext, useContext } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const auth0 = useAuth0();
  
  // Función helper para verificar si el usuario tiene un rol
  const hasRole = (role) => {
    const { user } = auth0;
    if (!user) return false;
    
    const roles = user['https://metamapa.com/roles'] || [];
    return roles.includes(role);
  };

  // Función helper para verificar si es admin
  const isAdmin = () => hasRole('ADMIN');

  return (
    <AuthContext.Provider value={{
      ...auth0,
      hasRole,
      isAdmin
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### Componentes de Autenticación

```jsx
// src/components/auth/LoginButton.jsx
import { useAuth0 } from '@auth0/auth0-react';

export const LoginButton = () => {
  const { loginWithRedirect, isAuthenticated } = useAuth0();

  if (isAuthenticated) return null;

  return (
    <button 
      onClick={() => loginWithRedirect()}
      className="btn btn-primary"
    >
      Iniciar Sesión
    </button>
  );
};

// src/components/auth/LogoutButton.jsx
import { useAuth0 } from '@auth0/auth0-react';

export const LogoutButton = () => {
  const { logout, isAuthenticated } = useAuth0();

  if (!isAuthenticated) return null;

  return (
    <button 
      onClick={() => logout({ 
        logoutParams: { returnTo: window.location.origin }
      })}
      className="btn btn-secondary"
    >
      Cerrar Sesión
    </button>
  );
};

// src/components/auth/UserProfile.jsx
import { useAuth0 } from '@auth0/auth0-react';

export const UserProfile = () => {
  const { user, isAuthenticated } = useAuth0();

  if (!isAuthenticated || !user) return null;

  const roles = user['https://metamapa.com/roles'] || [];

  return (
    <div className="user-profile">
      <img src={user.picture} alt={user.name} />
      <div>
        <h4>{user.name}</h4>
        <p>{user.email}</p>
        <div className="roles">
          {roles.map(role => (
            <span key={role} className="badge">{role}</span>
          ))}
        </div>
      </div>
    </div>
  );
};
```

### Rutas Protegidas

```jsx
// src/components/auth/ProtectedRoute.jsx
import { useAuth0 } from '@auth0/auth0-react';
import { Navigate } from 'react-router-dom';

export const ProtectedRoute = ({ children, requiredRole }) => {
  const { isAuthenticated, isLoading, user } = useAuth0();

  if (isLoading) {
    return <div className="loading">Cargando...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  // Si se requiere un rol específico
  if (requiredRole) {
    const roles = user['https://metamapa.com/roles'] || [];
    if (!roles.includes(requiredRole)) {
      return <Navigate to="/no-autorizado" replace />;
    }
  }

  return children;
};

// Uso en App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/callback" element={<Callback />} />
        
        {/* Rutas que requieren autenticación */}
        <Route 
          path="/mapa" 
          element={
            <ProtectedRoute>
              <Mapa />
            </ProtectedRoute>
          } 
        />
        
        {/* Rutas solo para ADMIN */}
        <Route 
          path="/admin/*" 
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AdminPanel />
            </ProtectedRoute>
          } 
        />
      </Routes>
    </BrowserRouter>
  );
}
```

### Configuración de Axios con JWT

```jsx
// src/services/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Variable para almacenar getAccessTokenSilently
let getAccessTokenSilently = null;

// Función para configurar el método de obtención de token
export const configureApiAuth = (getTokenFunc) => {
  getAccessTokenSilently = getTokenFunc;
};

// Interceptor para agregar el token a todas las requests
api.interceptors.request.use(
  async (config) => {
    if (getAccessTokenSilently) {
      try {
        const token = await getAccessTokenSilently();
        config.headers.Authorization = `Bearer ${token}`;
      } catch (error) {
        console.error('Error obteniendo token:', error);
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token inválido o expirado
      window.location.href = '/';
    } else if (error.response?.status === 403) {
      // Sin permisos
      console.error('Acceso denegado');
    }
    return Promise.reject(error);
  }
);

export default api;

// src/App.jsx - Configurar API con Auth0
import { useAuth0 } from '@auth0/auth0-react';
import { useEffect } from 'react';
import { configureApiAuth } from './services/api';

function App() {
  const { getAccessTokenSilently } = useAuth0();

  useEffect(() => {
    configureApiAuth(getAccessTokenSilently);
  }, [getAccessTokenSilently]);

  // ... resto del componente
}
```

### Servicios de API

```jsx
// src/services/hechoService.js
import api from './api';

export const hechoService = {
  // Listar hechos (autenticado)
  listarTodos: async () => {
    const response = await api.get('/interna/hechos');
    return response.data;
  },

  // Reportar hecho (autenticado)
  reportar: async (hechoData) => {
    const response = await api.post('/interna/hechos', hechoData);
    return response.data;
  },

  // Aprobar hecho (solo ADMIN)
  aprobar: async (id) => {
    const response = await api.post(`/admin/hechos/${id}/aprobar`);
    return response.data;
  },

  // Listar hechos públicos (sin auth)
  listarPublicos: async () => {
    const response = await api.get('/publica/hechos');
    return response.data;
  }
};
```

---

## 🔍 JWT: Anatomía y Validación {#jwt-anatomía-y-validación}

### ¿Cómo Spring Boot Valida el JWT?

```java
/**
 * Proceso de validación de JWT en Spring Boot
 */
public class JwtValidationProcess {
    
    /**
     * 1. Obtener la clave pública de Auth0
     * 
     * Spring Boot hace una petición a:
     * https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/.well-known/jwks.json
     * 
     * Este endpoint devuelve las claves públicas (JWK Set) usadas para firmar JWTs
     */
    private JwkSet obtenerClavePublica(String issuerUri) {
        // Spring Boot cachea estas claves automáticamente
        return jwkSetLoader.load(issuerUri + ".well-known/jwks.json");
    }
    
    /**
     * 2. Verificar la firma del JWT
     * 
     * - Decodifica el JWT (Base64)
     * - Obtiene el header y payload
     * - Recalcula la firma usando la clave pública
     * - Compara con la firma del token
     */
    private boolean verificarFirma(Jwt jwt, PublicKey publicKey) {
        String headerAndPayload = jwt.getTokenValue().split("\\.")[0] + "." 
                                 + jwt.getTokenValue().split("\\.")[1];
        String signature = jwt.getTokenValue().split("\\.")[2];
        
        // Verificar que la firma coincide
        return rsaSha256Verify(headerAndPayload, signature, publicKey);
    }
    
    /**
     * 3. Validar claims del JWT
     */
    private void validarClaims(Jwt jwt) {
        // Verificar issuer (quién emitió el token)
        String issuer = jwt.getIssuer().toString();
        if (!issuer.equals("https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/")) {
            throw new JwtException("Issuer inválido");
        }
        
        // Verificar audience (para qué API es el token)
        List<String> audiences = jwt.getAudience();
        if (!audiences.contains("https://metamapa-api")) {
            throw new JwtException("Audience inválido");
        }
        
        // Verificar expiración
        Instant expiration = jwt.getExpiresAt();
        if (expiration.isBefore(Instant.now())) {
            throw new JwtException("Token expirado");
        }
        
        // Verificar issued at (no aceptar tokens del futuro)
        Instant issuedAt = jwt.getIssuedAt();
        if (issuedAt.isAfter(Instant.now())) {
            throw new JwtException("Token emitido en el futuro");
        }
    }
}
```

### Ejemplo de JWT Decodificado

Puedes ver el contenido de un JWT en https://jwt.io/

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "abc123"
  },
  "payload": {
    "iss": "https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/",
    "sub": "auth0|65a1b2c3d4e5f6g7h8i9",
    "aud": ["https://metamapa-api"],
    "iat": 1700000000,
    "exp": 1700086400,
    "azp": "0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO",
    "scope": "openid profile email",
    "https://metamapa.com/roles": ["ADMIN"],
    "email": "admin@metamapa.com",
    "email_verified": true,
    "name": "Juan Pérez",
    "picture": "https://s.gravatar.com/avatar/..."
  },
  "signature": "..." // Firma RSA-SHA256
}
```

---

## 🛡️ Control de Acceso Basado en Roles (RBAC) {#rbac}

### Niveles de Autorización

#### 1. Nivel de Configuración (SecurityFilterChain)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/interna/**").authenticated()
)
```

#### 2. Nivel de Controlador (@PreAuthorize)

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/hechos/{id}/aprobar")
public ResponseEntity<?> aprobarHecho(@PathVariable Long id) {
    // ...
}
```

#### 3. Nivel de Servicio (@PreAuthorize)

```java
@Service
public class ColeccionService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public Coleccion crear(ColeccionDTO dto) {
        // ...
    }
}
```

#### 4. Nivel Programático (Manual)

```java
@PostMapping("/hechos/{id}/editar")
public ResponseEntity<?> editarHecho(
        @PathVariable Long id,
        @RequestBody HechoDTO dto,
        @AuthenticationPrincipal Jwt jwt) {
    
    Hecho hecho = hechoService.findById(id);
    String userId = jwt.getSubject();
    
    // Verificar que el usuario sea el creador o admin
    if (!hecho.getCreadorId().equals(userId) && !esAdmin(jwt)) {
        throw new AccessDeniedException("No puedes editar este hecho");
    }
    
    return ResponseEntity.ok(hechoService.actualizar(id, dto));
}

private boolean esAdmin(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("https://metamapa.com/roles");
    return roles != null && roles.contains("ADMIN");
}
```

### Expresiones de Spring Security

```java
// Roles
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")

// Authorities (más granular que roles)
@PreAuthorize("hasAuthority('write:hechos')")
@PreAuthorize("hasAnyAuthority('write:hechos', 'delete:hechos')")

// Autenticación
@PreAuthorize("isAuthenticated()")
@PreAuthorize("isAnonymous()")

// Lógica compleja
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.subject")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('delete:all')")

// SpEL (Spring Expression Language)
@PreAuthorize("#hecho.creadorId == authentication.principal.subject")
```

---

## 📝 Casos de Uso y Ejemplos {#casos-de-uso}

### Caso 1: Usuario Reporta un Hecho

**Frontend:**
```jsx
// src/components/ReportarHecho.jsx
import { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { hechoService } from '../services/hechoService';

export const ReportarHecho = () => {
  const { user, isAuthenticated } = useAuth0();
  const [formData, setFormData] = useState({
    titulo: '',
    descripcion: '',
    latitud: '',
    longitud: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      await hechoService.reportar(formData);
      alert('Hecho reportado exitosamente');
    } catch (error) {
      alert('Error al reportar hecho');
    }
  };

  if (!isAuthenticated) {
    return <p>Debes iniciar sesión para reportar hechos</p>;
  }

  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text" 
        placeholder="Título"
        value={formData.titulo}
        onChange={(e) => setFormData({...formData, titulo: e.target.value})}
      />
      {/* ... más campos */}
      <button type="submit">Reportar Hecho</button>
    </form>
  );
};
```

**Backend:**
```java
@PostMapping("/hechos")
public ResponseEntity<?> reportarHecho(
        @Valid @RequestBody HechoDTO dto,
        @AuthenticationPrincipal Jwt jwt) {
    
    // Extraer información del usuario del JWT
    String userId = jwt.getSubject();
    String email = jwt.getClaimAsString("email");
    String name = jwt.getClaimAsString("name");
    
    // Asociar el hecho al usuario
    dto.setReportadoPor(userId);
    dto.setEmailReportante(email);
    dto.setNombreReportante(name);
    
    Hecho hecho = hechoService.crear(dto);
    return ResponseEntity.ok(hecho);
}
```

### Caso 2: Admin Aprueba un Hecho

**Frontend:**
```jsx
// src/pages/AdminPanel.jsx
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { hechoService } from '../services/hechoService';

export const AdminPanel = () => {
  const { isAdmin } = useAuth();
  const [hechosPendientes, setHechosPendientes] = useState([]);

  useEffect(() => {
    if (isAdmin()) {
      cargarHechosPendientes();
    }
  }, []);

  const cargarHechosPendientes = async () => {
    const hechos = await hechoService.listarPendientes();
    setHechosPendientes(hechos);
  };

  const aprobar = async (id) => {
    await hechoService.aprobar(id);
    cargarHechosPendientes(); // Recargar lista
  };

  if (!isAdmin()) {
    return <p>Acceso denegado</p>;
  }

  return (
    <div className="admin-panel">
      <h2>Hechos Pendientes de Aprobación</h2>
      {hechosPendientes.map(hecho => (
        <div key={hecho.id} className="hecho-card">
          <h3>{hecho.titulo}</h3>
          <p>{hecho.descripcion}</p>
          <button onClick={() => aprobar(hecho.id)}>Aprobar</button>
          <button onClick={() => rechazar(hecho.id)}>Rechazar</button>
        </div>
      ))}
    </div>
  );
};
```

**Backend:**
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/hechos/{id}/aprobar")
public ResponseEntity<?> aprobarHecho(
        @PathVariable Long id,
        @AuthenticationPrincipal Jwt jwt) {
    
    String adminId = jwt.getSubject();
    String adminEmail = jwt.getClaimAsString("email");
    
    // Log de auditoría
    log.info("Admin {} ({}) aprobó hecho {}", adminId, adminEmail, id);
    
    hechoService.aprobar(id, adminId);
    
    return ResponseEntity.ok(Map.of(
        "message", "Hecho aprobado exitosamente",
        "aprobadoPor", adminEmail
    ));
}
```

### Caso 3: Verificar Rol en Frontend

```jsx
// src/components/NavBar.jsx
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

export const NavBar = () => {
  const { isAuthenticated, isAdmin, user } = useAuth();

  return (
    <nav>
      <Link to="/">Inicio</Link>
      
      {isAuthenticated && (
        <>
          <Link to="/mapa">Mapa</Link>
          <Link to="/reportar">Reportar Hecho</Link>
          
          {/* Solo mostrar link de admin si el usuario es ADMIN */}
          {isAdmin() && (
            <Link to="/admin">Panel Admin</Link>
          )}
        </>
      )}
    </nav>
  );
};
```

---

## 🔒 Seguridad y Mejores Prácticas {#seguridad}

### 1. Almacenamiento de Tokens en Frontend

❌ **MAL - LocalStorage (vulnerable a XSS):**
```javascript
localStorage.setItem('token', accessToken); // NO HACER ESTO
```

✅ **BIEN - Memory (Auth0 SDK):**
```jsx
<Auth0Provider
  cacheLocation="memory"  // Tokens en memoria
  useRefreshTokens={true} // Usar refresh tokens
>
```

### 2. Validación en Backend

✅ **Siempre validar en backend, nunca confiar solo en frontend:**

```java
// ❌ MAL - Solo verificar en frontend
// Usuario puede modificar JavaScript y bypasear la verificación

// ✅ BIEN - Verificar en backend
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/hechos/{id}")
public ResponseEntity<?> eliminarHecho(@PathVariable Long id) {
    // Spring Security ya verificó el rol antes de llegar aquí
    hechoService.eliminar(id);
    return ResponseEntity.ok().build();
}
```

### 3. HTTPS en Producción

```yaml
# application-prod.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12
```

### 4. Configuración de CORS Restrictiva

```java
// ❌ MAL - Permitir todos los orígenes
configuration.setAllowedOrigins(Arrays.asList("*")); // NO HACER ESTO

// ✅ BIEN - Listar orígenes específicos
configuration.setAllowedOrigins(Arrays.asList(
    "https://metamapa.com",
    "https://www.metamapa.com"
));
```

### 5. Refresh Tokens

Auth0 SDK maneja automáticamente la renovación de tokens:

```jsx
const { getAccessTokenSilently } = useAuth0();

// Obtener token (se renueva automáticamente si expiró)
const token = await getAccessTokenSilently();
```

### 6. Rate Limiting

Proteger endpoints de ataques de fuerza bruta:

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests por segundo
    }
}
```

### 7. Logging de Auditoría

```java
@Aspect
@Component
public class AuditAspect {
    
    @AfterReturning("@annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public void logSecurityAccess(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Usuario {} ejecutó {}", 
            auth.getName(), 
            joinPoint.getSignature().getName());
    }
}
```

---

## 🐛 Debugging y Troubleshooting {#debugging}

### Problemas Comunes

#### 1. "403 Forbidden" al llamar a endpoint protegido

**Causa:** El token no tiene el rol requerido o no se envía correctamente.

**Solución:**
```bash
# 1. Verificar que el token se envía en el header
# En DevTools → Network → Headers
Authorization: Bearer eyJhbGc...

# 2. Decodificar el token en jwt.io y verificar roles
# Buscar el claim: "https://metamapa.com/roles"

# 3. Verificar que el Action de Auth0 está agregando roles
# Dashboard → Actions → Flows → Login → Ver que el action esté activo
```

#### 2. "401 Unauthorized"

**Causa:** Token inválido, expirado o no presente.

**Solución:**
```javascript
// Verificar que el token se está obteniendo correctamente
const token = await getAccessTokenSilently();
console.log('Token:', token);

// Verificar expiración
const decodedToken = jwt_decode(token);
console.log('Expira en:', new Date(decodedToken.exp * 1000));
```

#### 3. CORS Error

**Causa:** Backend no permite requests desde el origen del frontend.

**Solución:**
```java
// Verificar que el origen está en la lista de permitidos
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173"  // Agregar el puerto correcto de Vite
));
```

#### 4. "Issuer mismatch"

**Causa:** El issuer del JWT no coincide con la configuración.

**Solución:**
```yaml
# application.yml - Verificar que el issuer tenga el / al final
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/
          # Importante: el / al final es necesario ^^^
```

### Herramientas de Debugging

#### 1. Ver Claims del JWT en Backend

```java
@GetMapping("/debug/me")
public ResponseEntity<?> debugUser(@AuthenticationPrincipal Jwt jwt) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("subject", jwt.getSubject());
    claims.put("issuer", jwt.getIssuer());
    claims.put("audience", jwt.getAudience());
    claims.put("expires", jwt.getExpiresAt());
    claims.put("roles", jwt.getClaimAsStringList("https://metamapa.com/roles"));
    claims.put("all_claims", jwt.getClaims());
    
    return ResponseEntity.ok(claims);
}
```

#### 2. Logging Detallado

```yaml
logging:
  level:
    org.springframework.security: TRACE
    org.springframework.security.oauth2: TRACE
    com.metamapa: DEBUG
```

#### 3. Auth0 Dashboard Logs

```
Dashboard → Monitoring → Logs
- Ver intentos de login
- Ver tokens emitidos
- Ver errores de autenticación
```

---

## ❓ Preguntas Frecuentes {#preguntas-frecuentes}

### ¿Necesito almacenar usuarios en mi base de datos?

**Opción 1:** Solo usar Auth0 (recomendado para empezar)
- Auth0 gestiona todos los usuarios
- No necesitas tabla de users
- Usas `jwt.getSubject()` como ID del usuario

**Opción 2:** Sincronizar con BD local
- Crear usuario en BD cuando se registra en Auth0
- Usar Auth0 hooks o management API
- Útil si necesitas relaciones complejas con el usuario

### ¿Cómo pruebo la autenticación sin frontend?

Usa Postman o curl:

```bash
# 1. Obtener token desde Auth0
curl --request POST \
  --url https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO",
    "client_secret":"TU_CLIENT_SECRET",
    "audience":"https://metamapa-api",
    "grant_type":"client_credentials"
  }'

# 2. Usar el token en requests
curl --request GET \
  --url http://localhost:8080/api/interna/hechos \
  --header 'authorization: Bearer eyJhbGc...'
```

### ¿Los tokens expiran? ¿Qué pasa entonces?

Sí, los Access Tokens expiran (típicamente en 24 horas).

**Solución:** Auth0 SDK usa Refresh Tokens para renovar automáticamente.

```jsx
// Configurar refresh tokens
<Auth0Provider
  useRefreshTokens={true}
  cacheLocation="memory"
>
```

### ¿Cómo pruebo con JUnit?

```java
@WebMvcTest(AdminController.class)
class AdminControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAprobarHecho_comoAdmin_debeFuncionar() throws Exception {
        mockMvc.perform(post("/api/admin/hechos/1/aprobar"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testAprobarHecho_comoUser_debeDenegarAcceso() throws Exception {
        mockMvc.perform(post("/api/admin/hechos/1/aprobar"))
            .andExpect(status().isForbidden());
    }
}
```

### ¿Puedo usar este sistema en otros proyectos?

Sí, este sistema es completamente reutilizable:

1. Crear nueva aplicación en Auth0
2. Copiar el módulo `auth-service`
3. Actualizar configuraciones (domain, clientId, audience)
4. Listo

---

## 📚 Recursos Adicionales

- **Auth0 Docs:** https://auth0.com/docs
- **Spring Security:** https://docs.spring.io/spring-security/reference/
- **JWT.io:** https://jwt.io/ (decodificar JWTs)
- **OAuth 2.0 Playground:** https://www.oauth.com/playground/
- **Auth0 Community:** https://community.auth0.com/

---

**Próximos Pasos:** Implementar el código siguiendo esta guía paso a paso.

