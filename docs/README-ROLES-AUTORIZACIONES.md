# Sistema de Roles y Autorizaciones - MetaMapa

## Fecha de Implementación
14 de Diciembre de 2025

## Resumen Ejecutivo

Se ha implementado un sistema completo de roles y autorizaciones basado en Auth0 para el proyecto MetaMapa. El sistema define tres niveles de acceso (USER, CONTRIBUTOR, ADMIN) y asegura que cada endpoint tenga las restricciones apropiadas según la funcionalidad que ofrece.

---

## 1. Definición de Roles

### 1.1 USER (Visualizador/Usuario Normal)
**Descripción**: Usuario con permisos básicos de visualización.

**Permisos**:
- ✅ Ver hechos y colecciones (API Pública e Interna)
- ✅ Filtrar y buscar información
- ✅ Solicitar eliminaciones de hechos
- ✅ Ver detalles de hechos y colecciones
- ❌ **NO** puede crear o editar contenido
- ❌ **NO** puede acceder a perfiles (GET /perfil)

### 1.2 CONTRIBUTOR (Contribuyente)
**Descripción**: Usuario registrado que puede aportar contenido al sistema.

**Permisos**:
- ✅ Todos los permisos de USER
- ✅ Crear nuevos hechos (POST /hechos)
- ✅ Ver su propio perfil (GET /perfil)
- ✅ Editar su propio perfil (PUT /perfil)
- ✅ Editar sus propios hechos (PUT /hechos/{id})
  - ⚠️ Restricción: Solo dentro de 7 días desde creación
- ❌ **NO** puede acceder a la API Administrativa

### 1.3 ADMIN (Administrador)
**Descripción**: Usuario con control total sobre el sistema.

**Permisos**:
- ✅ Todos los permisos de CONTRIBUTOR
- ✅ Acceso completo a la API Administrativa
- ✅ Gestionar colecciones (crear, editar, eliminar)
- ✅ Aprobar/rechazar hechos pendientes
- ✅ Gestionar solicitudes de eliminación
- ✅ Editar cualquier hecho sin restricción de tiempo
- ✅ Acceder a estadísticas administrativas

---

## 2. Estructura de APIs y Control de Acceso

### 2.1 API Pública (`/api/public/**`)
**Acceso**: Sin autenticación

**Endpoints**:
- `GET /api/public/metamapa/colecciones` - Listar colecciones
- `GET /api/public/metamapa/colecciones/{id}/hechos` - Hechos de una colección
- `POST /api/public/metamapa/hechos/{idHecho}/solicitudes` - Crear solicitud de eliminación

**Configuración**: No requiere token JWT

### 2.2 API Interna (`/api/interna/**`)
**Acceso**: Usuarios autenticados (USER, CONTRIBUTOR, ADMIN)

**Restricciones granulares implementadas**:

#### Endpoints de Lectura (USER, CONTRIBUTOR, ADMIN)
- `GET /api/interna/colecciones` - Listar colecciones
- `GET /api/interna/colecciones/{id}/hechos` - Hechos de colección
- `GET /api/interna/hechos` - Listar todos los hechos
- `GET /api/interna/hechos/{id}` - Detalle de un hecho
- `GET /api/interna/hechos/search` - Búsqueda full-text

#### Endpoints Solo para CONTRIBUTOR y ADMIN
- ✅ `POST /api/interna/hechos` - Crear hecho (JSON)
- ✅ `POST /api/interna/hechos` (multipart) - Crear hecho con archivos
- ✅ `GET /api/interna/perfil` - Ver perfil propio
- ✅ `PUT /api/interna/perfil` - **NUEVO** Editar perfil propio
- ✅ `PUT /api/interna/hechos/{id}` - **NUEVO** Editar hecho
- ✅ `GET /api/interna/hechos/{id}/puede-editar` - **NUEVO** Verificar si puede editar

**Implementación**:
```java
@PreAuthorize(RoleConstants.HAS_ROLE_CONTRIBUTOR_OR_ADMIN)
@PostMapping(value = "/hechos", consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> reportarHecho(...)
```

### 2.3 API Administrativa (`/api/admin/**`)
**Acceso**: Solo ADMIN

**Protección a nivel de controlador**:
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize(RoleConstants.HAS_ROLE_ADMIN) // Aplica a todos los endpoints
public class APIAdministrativaController {
    // ...
}
```

**Endpoints protegidos**:
- `POST /api/admin/colecciones` - Crear colección
- `PUT /api/admin/colecciones/{id}` - Modificar colección
- `DELETE /api/admin/colecciones/{id}` - Eliminar colección
- `GET /api/admin/hechos/pendientes` - Hechos pendientes de aprobación
- `POST /api/admin/hechos/aprobar` - Aprobar hecho
- `POST /api/admin/hechos/rechazar` - Rechazar hecho
- `GET /api/admin/solicitudes` - Ver solicitudes de eliminación
- `POST /api/admin/solicitudes/{id}/aprobar` - Aprobar solicitud
- `POST /api/admin/solicitudes/{id}/rechazar` - Rechazar solicitud

---

## 3. Nuevos Endpoints Implementados

### 3.1 PUT /api/interna/perfil
**Descripción**: Permite a un usuario editar su propio perfil.

**Restricciones**:
- Solo CONTRIBUTOR y ADMIN
- Solo puede editar su propio perfil (verificado por `X-Contribuyente-Id`)

**Request**:
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "fechaNacimiento": "1990-05-15"
}
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "fechaNacimiento": "1990-05-15",
  "fechaRegistro": "2025-01-10T10:30:00",
  "rol": "CONTRIBUYENTE",
  "edad": 35
}
```

**Implementación**: `ContribuyenteService.actualizarPerfil()`

### 3.2 PUT /api/interna/hechos/{id}
**Descripción**: Permite editar un hecho existente.

**Restricciones**:
- Solo CONTRIBUTOR y ADMIN
- **CONTRIBUTOR**: Solo puede editar sus propios hechos y dentro de 7 días de creación
- **ADMIN**: Puede editar cualquier hecho sin restricción de tiempo

**Request**:
```json
{
  "titulo": "Nuevo título actualizado",
  "descripcion": "Descripción modificada",
  "categoria": "Ambiental",
  "fechaHecho": "2025-12-10T15:00:00",
  "etiquetas": ["urgente", "actualizado"],
  "latitud": -34.6037,
  "longitud": -58.3816,
  "pais": "Argentina",
  "provincia": "Buenos Aires",
  "municipio": "CABA"
}
```

**Response (200 OK)**: Objeto Hecho completo actualizado

**Errores posibles**:
- `400 Bad Request` - Validación fallida o tiempo límite superado
- `403 Forbidden` - No tiene permisos para editar este hecho
- `404 Not Found` - Hecho no encontrado

**Lógica de negocio** (en `HechoService.actualizarHecho()`):
```java
// Verificar que no hayan pasado más de 7 días (solo para CONTRIBUTOR)
LocalDateTime fechaCreacion = hecho.getFechaCarga();
LocalDateTime fechaLimite = fechaCreacion.plusDays(7);
LocalDateTime ahora = LocalDateTime.now();

if (ahora.isAfter(fechaLimite)) {
    throw new IllegalArgumentException(
        "No se puede editar el hecho. Han pasado " + diasPasados + 
        " días desde su creación. Solo se permite editar dentro de los 7 días."
    );
}
```

### 3.3 GET /api/interna/hechos/{id}/puede-editar
**Descripción**: Verifica si el usuario actual puede editar un hecho específico.

**Restricciones**: Solo CONTRIBUTOR y ADMIN

**Response (200 OK)**:
```json
{
  "puedeEditar": true,
  "esAdmin": false
}
```

**Uso**: Frontend puede usar este endpoint para mostrar/ocultar botones de edición.

---

## 4. Servicios Implementados

### 4.1 HechoService
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/service/HechoService.java`

**Métodos principales**:
- `actualizarHecho(Long hechoId, Long contribuyenteId, ActualizarHechoDTO dto, boolean esAdmin)` - Actualiza un hecho con validaciones
- `puedeEditar(Long hechoId, Long contribuyenteId, boolean esAdmin)` - Verifica permisos de edición

**Reglas de negocio**:
1. Verificar que el hecho existe y no está eliminado
2. Si no es admin, verificar que sea el dueño del hecho
3. Si no es admin, verificar que no hayan pasado 7 días desde creación
4. Actualizar solo campos proporcionados en el DTO
5. Actualizar timestamp de última modificación

### 4.2 ContribuyenteService (Actualizado)
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/service/ContribuyenteService.java`

**Nuevo método**:
```java
@Transactional
public Contribuyente actualizarPerfil(
    Long contribuyenteId, 
    String nombre, 
    String apellido, 
    LocalDate fechaNacimiento
)
```

---

## 5. DTOs Creados

### 5.1 ActualizarPerfilDTO
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/dto/ActualizarPerfilDTO.java`

```java
@Data
public class ActualizarPerfilDTO {
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
}
```

### 5.2 ActualizarHechoDTO
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/dto/ActualizarHechoDTO.java`

```java
@Data
public class ActualizarHechoDTO {
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDateTime fechaHecho;
    private List<String> etiquetas;
    
    // Ubicación
    private Double latitud;
    private Double longitud;
    private String pais;
    private String provincia;
    private String municipio;
}
```

---

## 6. Configuración de Seguridad

### 6.1 RoleConstants
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/security/RoleConstants.java`

Define constantes para los roles:
```java
public static final String USER = "USER";
public static final String CONTRIBUTOR = "CONTRIBUTOR";
public static final String ADMIN = "ADMIN";

// Expresiones para @PreAuthorize
public static final String HAS_ROLE_USER = "hasRole('USER')";
public static final String HAS_ROLE_CONTRIBUTOR = "hasRole('CONTRIBUTOR')";
public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";

public static final String HAS_ROLE_CONTRIBUTOR_OR_ADMIN = 
    "hasAnyRole('CONTRIBUTOR', 'ADMIN')";
```

### 6.2 SecurityConfig
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/config/SecurityConfig.java`

**Configuración clave**:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .antMatchers("/api/interna/**").authenticated()
            .and()
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }
}
```

### 6.3 Auth0JwtGrantedAuthoritiesConverter
**Archivo**: `gestor-solicitudes/src/main/java/com/metamapa/security/Auth0JwtGrantedAuthoritiesConverter.java`

**Función**: Extrae roles del JWT de Auth0 y los convierte a GrantedAuthorities de Spring Security.

**Busca roles en múltiples ubicaciones**:
1. Claim `permissions`
2. Claim `roles`
3. Namespace personalizado `https://metamapa.com/roles`
4. Namespace personalizado `https://metamapa.com/permissions`

**Prefijo**: Todos los roles se prefijan con `ROLE_` para Spring Security.

---

## 7. Configuración de Auth0

### 7.1 Application Properties
**Archivo**: `gestor-solicitudes/src/main/resources/application.properties`

```properties
# OAuth2 Resource Server - JWT de Auth0
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/
spring.security.oauth2.resourceserver.jwt.audiences=https://metamapa-api

# Configuración personalizada de Auth0
auth0.domain=dev-x8zpgn3i6vnkjg4m.us.auth0.com
auth0.audience=https://metamapa-api
auth0.namespace=https://metamapa.com

# CORS
cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

### 7.2 Dependencias Maven
**Archivo**: `gestor-solicitudes/pom.xml`

```xml
<!-- Spring Security OAuth2 Resource Server para JWT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 7.3 Configuración en Auth0 Dashboard

**URL**: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/apis

**Roles a crear**:
1. **USER**
   - Nombre: Usuario/Visualizador
   - Descripción: Acceso básico de lectura

2. **CONTRIBUTOR**
   - Nombre: Contribuyente
   - Descripción: Puede crear y editar contenido propio

3. **ADMIN**
   - Nombre: Administrador
   - Descripción: Control total del sistema

**Importante**: Los roles deben enviarse en el claim `permissions` del JWT.

---

## 8. Tabla de Endpoints y Restricciones

| Endpoint | Método | Acceso Requerido | Implementado | Notas |
|----------|--------|-----------------|--------------|-------|
| `/api/public/**` | * | Ninguno | ✅ | Sin autenticación |
| `/api/interna/colecciones` | GET | Autenticado | ✅ | USER+ |
| `/api/interna/hechos` | GET | Autenticado | ✅ | USER+ |
| `/api/interna/hechos/{id}` | GET | Autenticado | ✅ | USER+ |
| `/api/interna/hechos` | POST | CONTRIBUTOR/ADMIN | ✅ | Crear hecho |
| `/api/interna/hechos/{id}` | PUT | CONTRIBUTOR/ADMIN | ✅ | **NUEVO** - 7 días |
| `/api/interna/perfil` | GET | CONTRIBUTOR/ADMIN | ✅ | Perfil propio |
| `/api/interna/perfil` | PUT | CONTRIBUTOR/ADMIN | ✅ | **NUEVO** |
| `/api/interna/hechos/{id}/puede-editar` | GET | CONTRIBUTOR/ADMIN | ✅ | **NUEVO** |
| `/api/admin/**` | * | ADMIN | ✅ | Protegido a nivel de clase |

---

## 9. Flujo de Autenticación y Autorización

### 9.1 Flujo completo
```
1. Usuario se autentica en Auth0 (frontend)
   ↓
2. Auth0 emite JWT con roles en claim "permissions"
   ↓
3. Frontend envía JWT en header Authorization: Bearer <token>
   ↓
4. Spring Security intercepta la petición
   ↓
5. Valida el JWT con Auth0 (issuer-uri)
   ↓
6. Auth0JwtGrantedAuthoritiesConverter extrae roles
   ↓
7. Convierte roles a GrantedAuthorities (ROLE_USER, ROLE_CONTRIBUTOR, ROLE_ADMIN)
   ↓
8. @PreAuthorize verifica permisos en el endpoint
   ↓
9. Si autorizado → Ejecuta método del controlador
   Si no autorizado → 403 Forbidden
```

### 9.2 Ejemplo de JWT decodificado
```json
{
  "iss": "https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/",
  "sub": "auth0|123456789",
  "aud": "https://metamapa-api",
  "iat": 1702555200,
  "exp": 1702641600,
  "permissions": [
    "CONTRIBUTOR"
  ],
  "https://metamapa.com/roles": [
    "CONTRIBUTOR"
  ]
}
```

---

## 10. Testing y Validación

### 10.1 Endpoints a probar

#### Como USER:
- ✅ GET /api/interna/colecciones → 200 OK
- ✅ GET /api/interna/hechos → 200 OK
- ❌ POST /api/interna/hechos → 403 Forbidden
- ❌ GET /api/interna/perfil → 403 Forbidden
- ❌ GET /api/admin/colecciones → 403 Forbidden

#### Como CONTRIBUTOR:
- ✅ GET /api/interna/colecciones → 200 OK
- ✅ POST /api/interna/hechos → 201 Created
- ✅ GET /api/interna/perfil → 200 OK
- ✅ PUT /api/interna/perfil → 200 OK
- ✅ PUT /api/interna/hechos/{id} (propio, <7 días) → 200 OK
- ❌ PUT /api/interna/hechos/{id} (propio, >7 días) → 400 Bad Request
- ❌ PUT /api/interna/hechos/{id} (ajeno) → 403 Forbidden
- ❌ GET /api/admin/colecciones → 403 Forbidden

#### Como ADMIN:
- ✅ Todos los endpoints anteriores
- ✅ PUT /api/interna/hechos/{id} (cualquiera, sin límite de tiempo) → 200 OK
- ✅ GET /api/admin/colecciones → 200 OK
- ✅ POST /api/admin/colecciones → 201 Created
- ✅ GET /api/admin/hechos/pendientes → 200 OK

### 10.2 Casos de prueba para PUT /hechos/{id}

| Escenario | Usuario | Días desde creación | Resultado esperado |
|-----------|---------|---------------------|-------------------|
| Editar hecho propio | CONTRIBUTOR | 3 días | ✅ 200 OK |
| Editar hecho propio | CONTRIBUTOR | 8 días | ❌ 400 Bad Request |
| Editar hecho ajeno | CONTRIBUTOR | 2 días | ❌ 403 Forbidden |
| Editar cualquier hecho | ADMIN | 30 días | ✅ 200 OK |
| Editar hecho eliminado | ADMIN | 1 día | ❌ 400 Bad Request |

---

## 11. Próximos Pasos y Recomendaciones

### 11.1 Tareas pendientes
1. ✅ Actualizar frontend para manejar los nuevos endpoints
2. ⏳ Agregar unit tests para HechoService y ContribuyenteService
3. ⏳ Agregar integration tests para endpoints protegidos
4. ⏳ Documentar APIs en Swagger/OpenAPI
5. ⏳ Agregar rate limiting para prevenir abuso
6. ⏳ Implementar auditoría de cambios en hechos

### 11.2 Mejoras sugeridas
1. **Cache de validaciones**: Cachear resultado de `puedeEditar()` para mejorar performance
2. **Notificaciones**: Alertar a usuarios cuando un hecho está próximo a perder editabilidad (6 días)
3. **Historial de cambios**: Guardar versiones anteriores de hechos editados
4. **Soft delete con restauración**: Permitir a ADMIN restaurar hechos eliminados
5. **Permisos granulares**: Agregar permisos específicos por categoría de hecho

### 11.3 Consideraciones de seguridad
- ✅ JWT validado contra Auth0
- ✅ Roles extraídos de claims seguros
- ✅ CORS configurado correctamente
- ✅ CSRF deshabilitado (stateless API)
- ⚠️ Implementar rate limiting en producción
- ⚠️ Habilitar HTTPS en producción
- ⚠️ Rotar secretos de Auth0 periódicamente

---

## 12. Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend (React)                     │
│                     http://localhost:5173                    │
└────────────────────┬────────────────────────────────────────┘
                     │ JWT Token (Authorization: Bearer)
                     ↓
┌─────────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ 1. CORS Filter                                       │   │
│  │ 2. JWT Validation (Auth0)                            │   │
│  │ 3. Auth0JwtGrantedAuthoritiesConverter               │   │
│  │ 4. Authorization Filter (@PreAuthorize)              │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     ↓
         ┌───────────┴───────────┬──────────────────┐
         ↓                       ↓                  ↓
┌─────────────────┐   ┌─────────────────┐   ┌───────────────┐
│  API Pública    │   │  API Interna    │   │  API Admin    │
│  (Sin Auth)     │   │  (Autenticado)  │   │  (Solo ADMIN) │
│                 │   │                 │   │               │
│ GET /public/**  │   │ GET /interna/** │   │ * /admin/**   │
└─────────────────┘   │ POST (C+A)      │   └───────────────┘
                      │ PUT (C+A)       │
                      └─────────────────┘

C = CONTRIBUTOR, A = ADMIN
```

---

## 13. Contacto y Soporte

Para dudas o problemas relacionados con esta implementación:

- **Repositorio**: ProyectoK-v1/ProyectoK
- **Módulo principal**: gestor-solicitudes
- **Documentación Auth0**: [docs/README-AUTH0.md](../docs/README-AUTH0.md)
- **Fecha**: 14 de Diciembre de 2025

---

## Changelog

### Versión 1.0.0 (14/12/2025)
- ✅ Implementado sistema de roles (USER, CONTRIBUTOR, ADMIN)
- ✅ Agregado endpoint PUT /api/interna/perfil
- ✅ Agregado endpoint PUT /api/interna/hechos/{id} con regla de 7 días
- ✅ Agregado endpoint GET /api/interna/hechos/{id}/puede-editar
- ✅ Protegida API Administrativa con rol ADMIN
- ✅ Implementado Auth0JwtGrantedAuthoritiesConverter
- ✅ Actualizado SecurityConfig con OAuth2 Resource Server
- ✅ Creado HechoService con lógica de edición
- ✅ Actualizado ContribuyenteService con método de actualización de perfil
- ✅ Agregadas dependencias Spring Security OAuth2
- ✅ Configurado application.properties con Auth0

---

**Documento generado automáticamente por el sistema MetaMapa**
