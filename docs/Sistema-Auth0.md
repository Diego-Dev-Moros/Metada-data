# 🔐 Sistema de Autenticación y Autorización con Auth0

## 📌 Introducción

Este documento describe el **nuevo sistema de autenticación y autorización** que se integrará al proyecto MetaMapa utilizando **Auth0** como proveedor de identidad (Identity Provider) y **Spring Boot 3.0 con Spring Security 6** para el backend, junto con **React** en el frontend.

## 🎯 Objetivos

1. **Seguridad:** Proteger endpoints y recursos del sistema
2. **Autenticación:** Verificar la identidad de usuarios
3. **Autorización:** Controlar el acceso basado en roles (RBAC)
4. **SSO:** Single Sign-On para experiencia unificada
5. **Social Login:** Permitir login con Google, Facebook, etc.
6. **Escalabilidad:** Sistema reutilizable para futuros proyectos
7. **Simplicidad:** Implementación clara y mantenible

## 🏗️ Arquitectura del Sistema

### Componentes Principales

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                        │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Auth0 React SDK (@auth0/auth0-react)                  │    │
│  │  - LoginButton                                          │    │
│  │  - LogoutButton                                         │    │
│  │  - useAuth0 Hook                                        │    │
│  │  - ProtectedRoute Component                            │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
                         JWT Access Token
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot 3.0)                    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Spring Security 6                                      │    │
│  │  - SecurityFilterChain                                  │    │
│  │  - JwtAuthenticationFilter                             │    │
│  │  - Auth0 JWT Validator                                 │    │
│  └────────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  Role-Based Access Control (RBAC)                      │    │
│  │  - @PreAuthorize("hasRole('ADMIN')")                   │    │
│  │  - @PreAuthorize("hasRole('USER')")                    │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              ↓ ↑
                         Validación JWT
                              ↓ ↑
┌─────────────────────────────────────────────────────────────────┐
│                         AUTH0 (Cloud)                           │
│  - Gestión de usuarios                                          │
│  - Social Login (Google, Facebook, GitHub)                      │
│  - Emisión de JWT tokens                                        │
│  - Roles y Permisos                                             │
│  - Dashboard de configuración                                   │
└─────────────────────────────────────────────────────────────────┘
```

## 🔑 ¿Qué es Auth0?

**Auth0** es una plataforma de autenticación y autorización como servicio (IDaaS) que proporciona:

- **Universal Login:** Página de login alojada y segura
- **Social Connections:** Login con Google, Facebook, GitHub, etc.
- **Database Users:** Gestión de usuarios propios
- **Multi-Factor Authentication (MFA):** Seguridad adicional
- **JWT Tokens:** Tokens seguros y estandarizados
- **APIs de Management:** Gestión programática de usuarios y configuraciones

## 🔐 Flujo de Autenticación OAuth 2.0 / OIDC

### 1. Authorization Code Flow (Más Seguro)

```
Usuario                     Frontend              Auth0              Backend
  |                           |                     |                   |
  |---(1) Click Login-------->|                     |                   |
  |                           |---(2) Redirect----->|                   |
  |<--------------(3) Login Page-------------------|                   |
  |---(4) Credentials-------->|                     |                   |
  |                           |<--(5) Auth Code-----|                   |
  |                           |---(6) Exchange Code------------------->|
  |                           |                     |<--(7) Validate---|
  |                           |<-------------(8) Access Token----------|
  |                           |                     |                   |
  |<--(9) Authenticated-------|                     |                   |
  |                           |                     |                   |
  |---(10) API Call + Token------------------------->|                   |
  |                           |                     |---(11) Validate-->|
  |<-----------------(12) Protected Resource-------------------------|
```

### Pasos Detallados:

1. **Usuario hace clic en "Login"** en el frontend
2. **Frontend redirige** al Universal Login de Auth0
3. **Auth0 muestra** página de login (email/password o social)
4. **Usuario ingresa credenciales** o selecciona proveedor social
5. **Auth0 valida** y devuelve un **Authorization Code**
6. **Frontend intercambia** el código por tokens
7. **Auth0 valida** el código
8. **Auth0 devuelve** Access Token (JWT) y Refresh Token
9. **Frontend almacena** el token (memory o secure storage)
10. **Frontend hace peticiones** API incluyendo token en header `Authorization: Bearer <token>`
11. **Backend valida** el JWT con la clave pública de Auth0
12. **Backend responde** con el recurso protegido

## 👥 Roles y Permisos

### Roles Definidos

1. **USER (Usuario Regular)**
   - Ver hechos en el mapa
   - Reportar nuevos hechos
   - Crear solicitudes de eliminación
   - Editar su perfil

2. **ADMIN (Administrador)**
   - Todo lo de USER +
   - Aprobar/rechazar hechos pendientes
   - Aprobar/rechazar solicitudes de eliminación
   - Crear/modificar/eliminar colecciones
   - Configurar fuentes y algoritmos de consenso
   - Cargar archivos CSV
   - Ver estadísticas avanzadas

### Implementación en Auth0

Los roles se configuran en el **Dashboard de Auth0**:
- **Auth0 Dashboard → User Management → Roles**
- Se asignan roles a usuarios
- Los roles se incluyen en el JWT como claims

```json
{
  "sub": "auth0|123456",
  "email": "usuario@ejemplo.com",
  "roles": ["ADMIN"],
  "permissions": [
    "read:hechos",
    "write:hechos",
    "delete:hechos",
    "manage:colecciones"
  ]
}
```

## 🛡️ Seguridad en Backend

### Configuración de Spring Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/publica/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/interna/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
            );
        return http.build();
    }
}
```

### Protección de Endpoints

```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/hechos/{id}/aprobar")
    public ResponseEntity<?> aprobarHecho(@PathVariable Long id) {
        // Solo accesible para ADMIN
    }
}
```

## 🎨 Integración en Frontend React

### Configuración de Auth0Provider

```jsx
// main.jsx
import { Auth0Provider } from '@auth0/auth0-react';

<Auth0Provider
  domain="dev-x8zpgn3i6vnkjg4m.us.auth0.com"
  clientId="0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO"
  authorizationParams={{
    redirect_uri: window.location.origin,
    audience: "https://metamapa-api",
    scope: "openid profile email"
  }}
>
  <App />
</Auth0Provider>
```

### Componentes de Autenticación

```jsx
// LoginButton.jsx
import { useAuth0 } from '@auth0/auth0-react';

const LoginButton = () => {
  const { loginWithRedirect } = useAuth0();
  return <button onClick={() => loginWithRedirect()}>Login</button>;
};

// LogoutButton.jsx
const LogoutButton = () => {
  const { logout } = useAuth0();
  return <button onClick={() => logout()}>Logout</button>;
};

// ProtectedRoute.jsx
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth0();
  
  if (isLoading) return <Loading />;
  if (!isAuthenticated) return <Navigate to="/" />;
  
  return children;
};
```

### Llamadas API con Token

```jsx
// api/axiosConfig.js
import axios from 'axios';
import { useAuth0 } from '@auth0/auth0-react';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Interceptor para agregar token
api.interceptors.request.use(async (config) => {
  const { getAccessTokenSilently } = useAuth0();
  const token = await getAccessTokenSilently();
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

## 📋 Configuración de Auth0

### Tu Aplicación en Auth0

**Dashboard URL:** https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/

**Application Settings:**
- **Domain:** `dev-x8zpgn3i6vnkjg4m.us.auth0.com`
- **Client ID:** `0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO`
- **Client Secret:** (en settings, no lo compartas en código)

### Configuraciones Necesarias

1. **Allowed Callback URLs:**
   ```
   http://localhost:5173/callback,
   http://localhost:3000/callback,
   https://tu-dominio.com/callback
   ```

2. **Allowed Logout URLs:**
   ```
   http://localhost:5173,
   http://localhost:3000,
   https://tu-dominio.com
   ```

3. **Allowed Web Origins:**
   ```
   http://localhost:5173,
   http://localhost:8080
   ```

4. **API Audience:**
   - Crear un API en Auth0 Dashboard
   - Identifier: `https://metamapa-api`
   - Este valor se usa como `audience` en configuración

## 🔄 Módulo auth-service (Nuevo)

Se creará un nuevo módulo Spring Boot dedicado a autenticación:

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/metamapa/auth/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── Auth0Config.java
│   │   │   ├── filter/
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── service/
│   │   │   │   ├── Auth0Service.java
│   │   │   │   └── UserService.java
│   │   │   └── AuthServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
└── pom.xml
```

## 📊 Ventajas de esta Arquitectura

✅ **Seguridad:** JWT tokens firmados y validados  
✅ **Escalabilidad:** Auth0 maneja millones de usuarios  
✅ **Mantenimiento:** No gestionar contraseñas ni hashing  
✅ **Social Login:** Configuración en minutos  
✅ **MFA:** Autenticación multi-factor disponible  
✅ **Auditoría:** Logs completos en Auth0 Dashboard  
✅ **Reutilizable:** Mismo sistema para otros proyectos  
✅ **Cumplimiento:** GDPR, SOC 2, HIPAA compliant  

## 🚀 Plan de Implementación

### Fase 1: Configuración de Auth0
1. Configurar aplicación en Auth0 Dashboard
2. Definir roles y permisos
3. Configurar social connections (Google, GitHub)

### Fase 2: Backend (Spring Boot)
1. Crear módulo `auth-service`
2. Configurar dependencias de Spring Security
3. Implementar JWT validation
4. Proteger endpoints existentes
5. Agregar control de roles

### Fase 3: Frontend (React)
1. Instalar `@auth0/auth0-react`
2. Configurar Auth0Provider
3. Implementar componentes de login/logout
4. Crear rutas protegidas
5. Actualizar llamadas API con tokens

### Fase 4: Integración
1. Probar flujo completo de autenticación
2. Validar roles y permisos
3. Pruebas de seguridad
4. Documentación de APIs protegidas

### Fase 5: Testing
1. Pruebas unitarias (JUnit)
2. Pruebas de integración
3. Pruebas de seguridad (penetration testing)

## 📚 Recursos y Referencias

- **Auth0 Docs:** https://auth0.com/docs
- **Spring Security:** https://spring.io/projects/spring-security
- **Auth0 React SDK:** https://github.com/auth0/auth0-react
- **OAuth 2.0:** https://oauth.net/2/
- **JWT:** https://jwt.io/

---

**Próximo paso:** Ver [Autorizacion.md](./Autorizacion.md) para detalles técnicos de implementación.
