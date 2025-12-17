# Documentación Técnica - MetaMapa

## Índice
1. [Arquitectura General](#arquitectura-general)
2. [Backend - APIs REST](#backend---apis-rest)
3. [Sistema de Autenticación y Autorización](#sistema-de-autenticación-y-autorización)
4. [Frontend - Componentes React](#frontend---componentes-react)
5. [Base de Datos](#base-de-datos)
6. [Configuración y Despliegue](#configuración-y-despliegue)

---

## Arquitectura General

MetaMapa es una aplicación web para la gestión y visualización de hechos históricos y eventos de interés público en Argentina. Utiliza una arquitectura de microservicios con las siguientes capas:

### Estructura de Módulos

```
ProyectoK/
├── agregador/              # Coordinador de fuentes de datos
├── auth-service/           # Servicio de autenticación (legado)
├── gestor-solicitudes/     # API principal (Backend)
├── fuente-dinamica/        # Fuente de datos en tiempo real (MongoDB)
├── fuente-estatica/        # Fuente de datos históricos (MySQL)
├── fuente-proxy/           # Proxy para fuentes externas
├── common/                 # Clases compartidas
├── domain/                 # Entidades del dominio
├── stats/                  # Servicio de estadísticas
└── frontEnd/
    └── metamapa-frontend/  # Aplicación React
```

### Stack Tecnológico

**Backend:**
- Java 17
- Spring Boot 3.x
- Spring Security + OAuth2 Resource Server
- JPA/Hibernate
- MySQL 8 (datos persistentes)
- MongoDB (datos temporales/pendientes)
- Maven

**Frontend:**
- React 18
- React Router v6
- Auth0 React SDK
- Leaflet (mapas)
- Axios
- Vite

**Autenticación:**
- Auth0 (Identity Provider)
- OAuth2 + JWT
- Actions personalizadas de Auth0

---

## Backend - APIs REST

El backend principal está en el módulo **gestor-solicitudes** y expone tres grupos de APIs:

### 1. API Pública Externa (`/api/public/metamapa`)

**Sin autenticación requerida** - Acceso para usuarios no registrados.

#### Endpoints Disponibles

**GET `/api/public/metamapa/colecciones`**
Obtiene todas las colecciones disponibles.

```http
GET http://localhost:8080/api/public/metamapa/colecciones
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "identificador": 1,
    "titulo": "Desastres Naturales en Argentina",
    "descripcion": "Recopilación de eventos climáticos extremos",
    "categoria": "Desastres",
    "cantidadHechos": 245,
    "contadorVisitas": 1523,
    "fechaCreacion": "2024-01-15T10:30:00",
    "fechaUltimaModificacion": "2024-12-10T14:22:00"
  }
]
```

---

**GET `/api/public/metamapa/colecciones/{id}/hechos`**
Obtiene los hechos de una colección específica con filtros opcionales.

```http
GET http://localhost:8080/api/public/metamapa/colecciones/1/hechos?categoria=Inundacion&titulo=Santa Fe
```

**Parámetros de consulta:**
- `modo` (default: IRRESTRICTA) - Método de navegación
- `categoria` - Filtrar por categoría
- `titulo` - Buscar en título (contiene)
- `soloRecientes` - true/false

**Respuesta:**
```json
[
  {
    "id": 1,
    "titulo": "Inundación en Santa Fe 2003",
    "descripcion": "Inundación histórica que afectó...",
    "categoria": "Inundacion",
    "ubicacion": {
      "latitud": -31.6333,
      "longitud": -60.7000,
      "lugar": {
        "pais": "Argentina",
        "provincia": "Santa Fe",
        "municipio": "Santa Fe"
      }
    },
    "fechaHecho": "2003-04-29T12:00:00",
    "etiquetas": ["desastre", "clima"],
    "eliminado": false
  }
]
```

---

**POST `/api/public/metamapa/hechos/{idHecho}/solicitudes`**
Crea una solicitud de eliminación de un hecho (sin autenticación).

```http
POST http://localhost:8080/api/public/metamapa/hechos/123/solicitudes
Content-Type: application/json

{
  "justificacion": "Este hecho contiene información incorrecta sobre las fechas"
}
```

**Respuesta:**
```json
{
  "id": 45,
  "hecho": { ... },
  "justificacion": "Este hecho contiene información...",
  "estado": "PENDIENTE",
  "fechaCreacion": "2024-12-15T10:30:00"
}
```

---

### 2. API Interna (`/api/interna`)

**Requiere autenticación** - Para usuarios registrados (USER, CONTRIBUTOR, ADMIN).

#### Endpoints Disponibles

**GET `/api/interna/colecciones`**
Obtiene colecciones con ordenamiento avanzado.

```http
GET http://localhost:8080/api/interna/colecciones?orderBy=visitas
Authorization: Bearer <JWT_TOKEN>
```

**Parámetros:**
- `orderBy`: `fecha` (default), `visitas`, `hechos`

---

**GET `/api/interna/hechos`**
Obtiene todos los hechos con filtros avanzados.

```http
GET http://localhost:8080/api/interna/hechos?categoria=Incendio&desde=2020-01-01&hasta=2024-12-31&ubicacion=Córdoba
Authorization: Bearer <JWT_TOKEN>
```

**Parámetros de consulta:**
- `categoria` - Filtrar por categoría exacta
- `desde` - Fecha inicio (formato: YYYY-MM-DD)
- `hasta` - Fecha fin (formato: YYYY-MM-DD)
- `ubicacion` - Busca en país, provincia, municipio o título

**Respuesta:** Array de hechos (mismo formato que API pública)

---

**GET `/api/interna/hechos/{id}`**
Obtiene un hecho específico por ID.

```http
GET http://localhost:8080/api/interna/hechos/123
Authorization: Bearer <JWT_TOKEN>
```

---

**POST `/api/interna/hechos`** 🔒 CONTRIBUTOR, ADMIN
Crea un nuevo hecho (requiere rol CONTRIBUTOR o ADMIN).

**Opción 1: JSON**
```http
POST http://localhost:8080/api/interna/hechos
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "titulo": "Incendio Forestal en Córdoba",
  "descripcion": "Gran incendio que afectó 5000 hectáreas",
  "categoria": "Incendio",
  "ubicacion": {
    "latitud": -31.4201,
    "longitud": -64.1888,
    "lugar": {
      "pais": "Argentina",
      "provincia": "Córdoba",
      "municipio": "Villa Carlos Paz"
    }
  },
  "fechaHecho": "2024-10-15T14:30:00",
  "etiquetas": ["incendio", "forestal", "desastre"],
  "fuenteUrl": "https://example.com/noticia",
  "esPublico": true
}
```

**Opción 2: Multipart (con archivo)**
```http
POST http://localhost:8080/api/interna/hechos
Content-Type: multipart/form-data
Authorization: Bearer <JWT_TOKEN>

hechoJson: {"titulo": "...", "descripcion": "...", ...}
file: [archivo.csv]
```

---

**PUT `/api/interna/hechos/{id}`** 🔒 CONTRIBUTOR (7 días), ADMIN (sin límite)
Actualiza un hecho existente.

```http
PUT http://localhost:8080/api/interna/hechos/123
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "titulo": "Título Actualizado",
  "descripcion": "Nueva descripción con más detalles",
  "categoria": "Incendio"
}
```

**Reglas de negocio:**
- **CONTRIBUTOR**: Solo puede editar sus propios hechos dentro de 7 días de la creación
- **ADMIN**: Puede editar cualquier hecho en cualquier momento

**Respuesta:**
```json
{
  "id": 123,
  "titulo": "Título Actualizado",
  "descripcion": "Nueva descripción...",
  "fechaUltimaModificacion": "2024-12-15T16:45:00"
}
```

---

**GET `/api/interna/hechos/{id}/puede-editar`** 🔒 Autenticado
Verifica si el usuario actual puede editar un hecho.

```http
GET http://localhost:8080/api/interna/hechos/123/puede-editar
Authorization: Bearer <JWT_TOKEN>
```

**Respuesta:**
```json
{
  "puedeEditar": true,
  "motivo": "Es administrador"
}
```

Posibles motivos:
- `"Es administrador"` - Usuario tiene rol ADMIN
- `"Es el creador y aún está dentro del período de 7 días"` - CONTRIBUTOR dentro del plazo
- `"No es el creador del hecho"` - CONTRIBUTOR intentando editar hecho ajeno
- `"El período de edición de 7 días ha expirado"` - CONTRIBUTOR fuera del plazo

---

**GET `/api/interna/hechos/search`** 🔒 Autenticado
Búsqueda de texto completo en hechos.

```http
GET http://localhost:8080/api/interna/hechos/search?q=inundación&limit=20
Authorization: Bearer <JWT_TOKEN>
```

**Parámetros:**
- `q` - Término de búsqueda
- `limit` - Número máximo de resultados (default: 10)

---

**GET `/api/interna/perfil`** 🔒 Autenticado
Obtiene el perfil del usuario actual.

```http
GET http://localhost:8080/api/interna/perfil
Authorization: Bearer <JWT_TOKEN>
```

**Respuesta:**
```json
{
  "id": 5,
  "email": "usuario@example.com",
  "nombre": "Juan Pérez",
  "nombreUsuario": "juanperez",
  "cantidadHechosCreados": 12,
  "cantidadSolicitudesEnviadas": 3,
  "fechaRegistro": "2024-08-20T10:00:00"
}
```

---

**PUT `/api/interna/perfil`** 🔒 CONTRIBUTOR, ADMIN
Actualiza el perfil del usuario.

```http
PUT http://localhost:8080/api/interna/perfil
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "nombre": "Juan Carlos Pérez",
  "nombreUsuario": "juancp"
}
```

---

### 3. API Administrativa (`/api/admin`)

**Requiere rol ADMIN** - Todos los endpoints protegidos con `@PreAuthorize("hasRole('ROLE_ADMIN')")`

#### Endpoints Disponibles

**POST `/api/admin/colecciones`** 🔒 ADMIN
Crea una nueva colección.

```http
POST http://localhost:8080/api/admin/colecciones
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Admin-Id: 1

{
  "titulo": "Contaminación Ambiental Argentina",
  "descripcion": "Casos de contaminación industrial y urbana",
  "categoria": "Medio Ambiente",
  "algoritmo": "CONSENSO_MAYORIA",
  "metodosNavegacion": ["IRRESTRICTA", "RESTRINGIDA"]
}
```

**Campos del DTO:**
- `titulo` (requerido)
- `descripcion` (opcional)
- `categoria` (requerido)
- `algoritmo` (default: CONSENSO_MAYORIA)
- `metodosNavegacion` (default: [IRRESTRICTA])

---

**DELETE `/api/admin/colecciones/{identificador}`** 🔒 ADMIN
Elimina una colección.

```http
DELETE http://localhost:8080/api/admin/colecciones/5
Authorization: Bearer <JWT_TOKEN>
```

---

**POST `/api/admin/hechos/{id}/aprobar`** 🔒 ADMIN
Aprueba un hecho pendiente de revisión.

```http
POST http://localhost:8080/api/admin/hechos/507f1f77bcf86cd799439011/aprobar
Authorization: Bearer <JWT_TOKEN>
X-Admin-Id: 1
```

**Nota:** El `id` es el ID de MongoDB (fuente dinámica), no el ID de MySQL.

---

**POST `/api/admin/hechos/{id}/aprobar-con-sugerencias`** 🔒 ADMIN
Aprueba un hecho con sugerencias de mejora.

```http
POST http://localhost:8080/api/admin/hechos/507f1f77bcf86cd799439011/aprobar-con-sugerencias
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Admin-Id: 1

{
  "sugerencias": "Sería bueno agregar más detalles sobre las víctimas y el impacto económico"
}
```

---

**POST `/api/admin/hechos/{id}/rechazar`** 🔒 ADMIN
Rechaza un hecho pendiente.

```http
POST http://localhost:8080/api/admin/hechos/507f1f77bcf86cd799439011/rechazar
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Admin-Id: 1

{
  "motivo": "La información proporcionada no es verificable y carece de fuentes confiables"
}
```

---

**POST `/api/admin/hechos/{id}/solicitar-cambios`** 🔒 ADMIN
Solicita cambios en un hecho.

```http
POST http://localhost:8080/api/admin/hechos/507f1f77bcf86cd799439011/solicitar-cambios
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Admin-Id: 1

{
  "sugerencias": "Por favor, agregar coordenadas exactas y fecha precisa del evento"
}
```

---

**POST `/api/admin/datasets/cargar`** 🔒 ADMIN
Carga un dataset CSV masivo.

```http
POST http://localhost:8080/api/admin/datasets/cargar
Content-Type: multipart/form-data
Authorization: Bearer <JWT_TOKEN>

file: [archivo.csv]
tipoFuente: ESTATICA
```

**Formato CSV esperado:**
```csv
titulo,descripcion,categoria,fechaHecho,latitud,longitud,pais,provincia,municipio
"Inundación Santa Fe","Gran inundación...","Inundacion","2003-04-29",-31.6333,-60.7000,"Argentina","Santa Fe","Santa Fe"
```

---

## Sistema de Autenticación y Autorización

MetaMapa utiliza **Auth0** como proveedor de identidad con **OAuth2/JWT** para autenticación y autorización basada en roles.

### Flujo de Autenticación

```
1. Usuario hace clic en "Iniciar Sesión"
2. Frontend redirige a Auth0 (/authorize)
3. Usuario se autentica (email/password o Google)
4. Auth0 ejecuta Action "Add Roles to Token"
5. Auth0 genera JWT con roles en el claim "permissions"
6. Auth0 redirige a /callback con JWT
7. Frontend guarda JWT en sessionStorage
8. Frontend incluye JWT en header Authorization: Bearer <token>
9. Backend valida JWT y extrae roles
10. Backend verifica permisos con @PreAuthorize
```

### Configuración de Auth0

**Tenant:** `dev-x8zpgn3i6vnkjg4m.us.auth0.com`

**Application:**
- Nombre: MetaMapa Frontend
- Tipo: Single Page Application
- Allowed Callback URLs: `http://localhost:5173/callback`
- Allowed Logout URLs: `http://localhost:5173`
- Allowed Web Origins: `http://localhost:5173`

**API:**
- Nombre: MetaMapa API
- Identifier (Audience): `https://metamapa-api`

### Roles del Sistema

| Rol | Nombre en Auth0 | Permisos |
|-----|----------------|----------|
| **USER** (Visualizador) | `USER` | Ver hechos públicos, crear solicitudes de eliminación |
| **CONTRIBUTOR** (Contribuyente) | `CONTRIBUTOR` | Todo lo de USER + Crear hechos, Editar propios hechos (7 días), Actualizar perfil |
| **ADMIN** (Administrador) | `ADMIN` | Acceso completo: Crear/editar/eliminar hechos, Moderar contenido, Gestionar colecciones, Gestionar usuarios |

### Auth0 Action: "Add Roles to Token"

Esta Action se ejecuta en el flujo de Login para inyectar los roles en el JWT.

**Ubicación:** Actions → Flows → Login → "Add Roles to Token"

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://metamapa.com';
  
  if (event.authorization) {
    // Obtener roles asignados al usuario
    const roles = event.authorization.roles || [];
    
    // Agregar roles en el claim personalizado
    api.idToken.setCustomClaim(`${namespace}/roles`, roles);
    api.accessToken.setCustomClaim(`${namespace}/roles`, roles);
    
    // También agregar en el claim 'permissions' para compatibilidad
    api.accessToken.setCustomClaim('permissions', roles);
    
    console.log(`Roles asignados a ${event.user.email}:`, roles);
  }
};
```

**Cómo asignar roles a usuarios:**

1. Ve a **User Management → Users**
2. Selecciona un usuario
3. Ve a la pestaña **Roles**
4. Haz clic en **Assign Roles**
5. Selecciona el rol (USER, CONTRIBUTOR o ADMIN)
6. Guarda

### Configuración Backend (Spring Security)

**SecurityConfig.java:**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/interna/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
            new Auth0JwtGrantedAuthoritiesConverter()
        );
        return converter;
    }
}
```

**Auth0JwtGrantedAuthoritiesConverter.java:**

Extrae roles del JWT en múltiples ubicaciones para máxima compatibilidad:

```java
@Component
public class Auth0JwtGrantedAuthoritiesConverter 
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        
        // 1. Buscar en "permissions" (Auth0 estándar)
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) {
            roles.addAll(permissions);
        }
        
        // 2. Buscar en "https://metamapa.com/roles" (claim personalizado)
        List<String> customRoles = jwt.getClaimAsStringList(
            "https://metamapa.com/roles"
        );
        if (customRoles != null) {
            roles.addAll(customRoles);
        }
        
        // 3. Buscar en "roles" (compatibilidad)
        List<String> rolesArray = jwt.getClaimAsStringList("roles");
        if (rolesArray != null) {
            roles.addAll(rolesArray);
        }
        
        // Convertir a GrantedAuthority con prefijo ROLE_
        return roles.stream()
            .map(role -> {
                String normalizedRole = role.toUpperCase();
                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }
                return new SimpleGrantedAuthority(normalizedRole);
            })
            .collect(Collectors.toSet());
    }
}
```

### Protección de Endpoints con @PreAuthorize

**RoleConstants.java:**

```java
public class RoleConstants {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_CONTRIBUTOR = "CONTRIBUTOR";
    public static final String ROLE_ADMIN = "ADMIN";
    
    // Expresiones para @PreAuthorize
    public static final String HAS_ROLE_USER = "hasRole('ROLE_USER')";
    public static final String HAS_ROLE_CONTRIBUTOR = "hasRole('ROLE_CONTRIBUTOR')";
    public static final String HAS_ROLE_ADMIN = "hasRole('ROLE_ADMIN')";
    public static final String HAS_CONTRIBUTOR_OR_ADMIN = 
        "hasAnyRole('ROLE_CONTRIBUTOR', 'ROLE_ADMIN')";
}
```

**Ejemplos de uso:**

```java
// Solo CONTRIBUTOR y ADMIN pueden crear hechos
@PostMapping("/hechos")
@PreAuthorize(RoleConstants.HAS_CONTRIBUTOR_OR_ADMIN)
public ResponseEntity<?> crearHecho(@RequestBody ReportarHechoDTO dto) {
    // ...
}

// Solo ADMIN puede aprobar hechos
@PostMapping("/hechos/{id}/aprobar")
@PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
public ResponseEntity<?> aprobarHecho(@PathVariable String id) {
    // ...
}
```

### Configuración Frontend (React + Auth0)

**auth0Config.js:**

```javascript
export const auth0Config = {
  domain: 'dev-x8zpgn3i6vnkjg4m.us.auth0.com',
  clientId: 'YOUR_CLIENT_ID',
  authorizationParams: {
    redirect_uri: window.location.origin + '/callback',
    audience: 'https://metamapa-api',
    scope: 'openid profile email'
  }
};
```

**main.jsx:**

```jsx
import { Auth0Provider } from '@auth0/auth0-react';
import { auth0Config } from './config/auth0Config';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Auth0Provider {...auth0Config}>
      <Router>
        <App />
      </Router>
    </Auth0Provider>
  </React.StrictMode>
);
```

### Extracción de Roles en Frontend

**ProtectedRoute.jsx:**

```jsx
import { useAuth0 } from '@auth0/auth0-react';
import { Navigate } from 'react-router-dom';

const getUserRoles = (user) => {
  if (!user) return [];
  
  // Buscar en múltiples ubicaciones
  const roles = 
    user['https://metamapa.com/roles'] ||
    user['permissions'] ||
    user['roles'] ||
    [];
  
  // Normalizar a mayúsculas
  return roles.map(role => role.toUpperCase());
};

export default function ProtectedRoute({ children, allowedRoles = [] }) {
  const { isAuthenticated, isLoading, user } = useAuth0();
  
  if (isLoading) return <div>Cargando...</div>;
  
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }
  
  // Si allowedRoles está vacío, solo requiere autenticación
  if (allowedRoles.length === 0) {
    return children;
  }
  
  const userRoles = getUserRoles(user);
  const hasPermission = allowedRoles.some(role => 
    userRoles.includes(role)
  );
  
  if (!hasPermission) {
    return <Navigate to="/acceso-denegado" />;
  }
  
  return children;
}
```

### Envío de JWT en Peticiones HTTP

**Usando Axios con interceptor:**

```javascript
import axios from 'axios';
import { useAuth0 } from '@auth0/auth0-react';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(async (config) => {
  const { getAccessTokenSilently } = useAuth0();
  
  try {
    const token = await getAccessTokenSilently();
    config.headers.Authorization = `Bearer ${token}`;
  } catch (error) {
    console.error('Error obteniendo token:', error);
  }
  
  return config;
});

export default api;
```

**Petición manual:**

```javascript
const { getAccessTokenSilently } = useAuth0();

const crearHecho = async (hechoData) => {
  const token = await getAccessTokenSilently();
  
  const response = await fetch('http://localhost:8080/api/interna/hechos', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(hechoData)
  });
  
  return response.json();
};
```

---

## Frontend - Componentes React

### Estructura de Componentes

```
src/
├── components/
│   ├── Navbar.jsx              # Barra de navegación con roles
│   ├── Footer.jsx              # Pie de página
│   ├── MapaHechos.jsx          # Mapa interactivo con Leaflet
│   ├── FiltrosRapidos.jsx      # Filtros de búsqueda
│   └── ProtectedRoute.jsx      # HOC para protección de rutas
├── pages/
│   ├── HechosPage.jsx          # Página principal con mapa
│   ├── CrearHechoPage.jsx      # Formulario crear hecho
│   ├── ColeccionesPage.jsx     # Lista de colecciones
│   ├── ColeccionPage.jsx       # Detalle de colección
│   ├── PerfilAuth0Page.jsx     # Perfil de usuario Auth0
│   ├── ReportesHechosPage.jsx  # Panel admin - hechos pendientes
│   └── SolicitudesEliminacionPage.jsx  # Panel admin - solicitudes
├── auth/
│   ├── LoginButton.jsx         # Botón de login Auth0
│   ├── LogoutButton.jsx        # Botón de logout
│   └── UserProfile.jsx         # Widget perfil usuario
└── config/
    └── auth0Config.js          # Configuración Auth0
```

### Componente: Navbar.jsx

Barra de navegación principal con detección de roles y menú contextual.

**Características:**
- Logo y enlaces principales
- Botón "Crear Hecho" para CONTRIBUTOR y ADMIN
- Dropdown "Panel Admin" para ADMIN
- Indicador visual de rol (colores)
- Perfil de usuario con logout

**Extracción de roles:**

```jsx
const getRoles = () => {
  if (!user) return [];
  const roles = user['https://metamapa.com/roles'] || [];
  return roles;
};

const roles = getRoles();
const isAdmin = roles.includes('ADMIN');
const isContributor = roles.includes('CONTRIBUTOR');
const isUser = roles.includes('USER');
```

**Indicador visual de rol:**

```jsx
{isAuthenticated && (
  <div className="user-role-indicator">
    {isAdmin && <span className="role-badge admin">Admin</span>}
    {isContributor && !isAdmin && <span className="role-badge contributor">Contributor</span>}
    {isUser && !isAdmin && !isContributor && <span className="role-badge user">User</span>}
  </div>
)}
```

**CSS para badges:**

```css
.role-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin-left: 8px;
}

.role-badge.admin {
  background-color: #dc3545;
  color: white;
}

.role-badge.contributor {
  background-color: #ffc107;
  color: black;
}

.role-badge.user {
  background-color: #28a745;
  color: white;
}
```

---

### Componente: MapaHechos.jsx

Mapa interactivo con Leaflet que muestra hechos con marcadores coloreados.

**Props:**
- `filtros` - Objeto con filtros de búsqueda (categoria, desde, hasta, ubicacion)

**Características:**
- Marcadores con colores según gravedad (verde, naranja, rojo)
- Agrupamiento de marcadores en misma ubicación
- Actualización automática cada 60 segundos
- Popup con información del hecho
- Navegación a detalle del hecho

**Código clave:**

```jsx
useEffect(() => {
  const fetchHechos = async () => {
    const res = await axios.get('http://localhost:8080/api/interna/hechos', {
      params: {
        categoria: filtros.categoria || undefined,
        desde: filtros.desde || undefined,
        hasta: filtros.hasta || undefined,
        ubicacion: filtros.ubicacion || undefined
      }
    });
    setHechos(res.data || []);
  };
  
  fetchHechos();
  const id = setInterval(fetchHechos, 60_000);
  return () => clearInterval(id);
}, [filtros]);
```

**Renderizado de marcadores:**

```jsx
<MapContainer center={[-34.6, -58.4]} zoom={5}>
  <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
  {hechos.map(hecho => (
    <Marker 
      key={hecho.id}
      position={[hecho.ubicacion.latitud, hecho.ubicacion.longitud]}
      icon={ICONS[hecho.color || 'verde']}
    >
      <Popup>
        <h3>{hecho.titulo}</h3>
        <p>{hecho.descripcion}</p>
        <button onClick={() => navigate(`/hechos/${hecho.id}`)}>
          Ver detalle
        </button>
      </Popup>
    </Marker>
  ))}
</MapContainer>
```

---

### Componente: ProtectedRoute.jsx

Higher-Order Component para proteger rutas según roles.

**Props:**
- `children` - Componente hijo a renderizar
- `allowedRoles` - Array de roles permitidos (ej: `["ADMIN", "CONTRIBUTOR"]`)

**Lógica:**

```jsx
export default function ProtectedRoute({ children, allowedRoles = [] }) {
  const { isAuthenticated, isLoading, user } = useAuth0();
  
  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Verificando permisos...</p>
      </div>
    );
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  // Si allowedRoles está vacío, solo requiere autenticación
  if (allowedRoles.length === 0) {
    return children;
  }
  
  const userRoles = getUserRoles(user);
  const hasPermission = allowedRoles.some(role => 
    userRoles.includes(role.toUpperCase())
  );
  
  if (!hasPermission) {
    return (
      <div className="access-denied">
        <h1>🚫 Acceso Denegado</h1>
        <p>No tienes permisos para acceder a esta sección.</p>
        <p><strong>Roles requeridos:</strong> {allowedRoles.join(', ')}</p>
        <p><strong>Tus roles:</strong> {userRoles.join(', ') || 'Ninguno'}</p>
        <button onClick={() => navigate('/')}>Volver al inicio</button>
      </div>
    );
  }
  
  return children;
}
```

**Uso en rutas:**

```jsx
<Route path="/crear-hecho" element={
  <ProtectedRoute allowedRoles={["CONTRIBUTOR", "ADMIN"]}>
    <CrearHechoPage/>
  </ProtectedRoute>
}/>

<Route path="/reportes-hechos" element={
  <ProtectedRoute allowedRoles={["ADMIN"]}>
    <ReportesHechosPage/>
  </ProtectedRoute>
}/>
```

---

### Componente: PerfilAuth0Page.jsx

Página de perfil que muestra información del usuario desde Auth0.

**Información mostrada:**
- Avatar (picture de Auth0)
- Nombre completo
- Email
- Estado de verificación de email
- Roles asignados (con badges de colores)
- User ID
- Última actualización

**Código:**

```jsx
export default function PerfilAuth0Page() {
  const { user, isAuthenticated, isLoading } = useAuth0();
  
  if (isLoading) return <div>Cargando perfil...</div>;
  if (!isAuthenticated) return <Navigate to="/login" />;
  
  const roles = user['https://metamapa.com/roles'] || [];
  
  return (
    <div className="perfil-auth0">
      <div className="perfil-card">
        <img src={user.picture} alt={user.name} className="avatar" />
        
        <h1>{user.name}</h1>
        
        <div className="info-row">
          <strong>Email:</strong>
          <span>{user.email}</span>
          {user.email_verified && <span className="verified">✓ Verificado</span>}
        </div>
        
        <div className="info-row">
          <strong>Roles:</strong>
          <div className="roles-list">
            {roles.map(role => (
              <span key={role} className={`role-badge ${role.toLowerCase()}`}>
                {role}
              </span>
            ))}
            {roles.length === 0 && <span>Sin roles asignados</span>}
          </div>
        </div>
        
        <div className="info-row">
          <strong>User ID:</strong>
          <code>{user.sub}</code>
        </div>
        
        <div className="info-row">
          <strong>Última actualización:</strong>
          <span>{new Date(user.updated_at).toLocaleString('es-AR')}</span>
        </div>
      </div>
      
      {roles.length === 0 && (
        <div className="debug-section">
          <h3>Debug: Objeto user completo</h3>
          <pre>{JSON.stringify(user, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}
```

---

### Componente: CrearHechoPage.jsx

Formulario para crear nuevos hechos (solo CONTRIBUTOR y ADMIN).

**Campos del formulario:**
- Título (requerido)
- Descripción (requerida)
- Categoría (select)
- Fecha del hecho (date picker)
- Ubicación:
  - Latitud
  - Longitud
  - País
  - Provincia
  - Municipio
- Etiquetas (separadas por comas)
- URL de fuente
- Es público (checkbox)
- Archivo CSV (opcional)

**Envío con autenticación:**

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  
  const { getAccessTokenSilently } = useAuth0();
  const token = await getAccessTokenSilently();
  
  const hechoData = {
    titulo: formData.titulo,
    descripcion: formData.descripcion,
    categoria: formData.categoria,
    ubicacion: {
      latitud: parseFloat(formData.latitud),
      longitud: parseFloat(formData.longitud),
      lugar: {
        pais: formData.pais,
        provincia: formData.provincia,
        municipio: formData.municipio
      }
    },
    fechaHecho: formData.fechaHecho,
    etiquetas: formData.etiquetas.split(',').map(t => t.trim()),
    fuenteUrl: formData.fuenteUrl,
    esPublico: formData.esPublico
  };
  
  const response = await fetch('http://localhost:8080/api/interna/hechos', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(hechoData)
  });
  
  if (response.ok) {
    navigate('/');
  }
};
```

---

## Base de Datos

### MySQL (Datos Persistentes)

**Base de datos:** `utndds`  
**Host:** `localhost:3306`  
**Usuario:** `root`  
**Password:** `root`

**Tablas principales:**

**`hechos`** - Hechos aprobados y persistentes
```sql
CREATE TABLE hechos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(255) NOT NULL,
  descripcion TEXT,
  categoria VARCHAR(100),
  fecha_hecho DATETIME,
  fecha_carga DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_ultima_modificacion DATETIME,
  eliminado BOOLEAN DEFAULT FALSE,
  latitud DECIMAL(10, 8),
  longitud DECIMAL(11, 8),
  pais VARCHAR(100),
  provincia VARCHAR(100),
  municipio VARCHAR(100),
  etiquetas TEXT,
  fuente_url VARCHAR(500),
  es_publico BOOLEAN DEFAULT TRUE,
  contribuyente_id BIGINT,
  FOREIGN KEY (contribuyente_id) REFERENCES contribuyentes(id)
);
```

**`colecciones`** - Colecciones de hechos
```sql
CREATE TABLE colecciones (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  identificador BIGINT UNIQUE,
  titulo VARCHAR(255) NOT NULL,
  descripcion TEXT,
  categoria VARCHAR(100),
  contador_visitas INT DEFAULT 0,
  fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_ultima_modificacion DATETIME,
  algoritmo_consenso VARCHAR(50),
  admin_id BIGINT,
  FOREIGN KEY (admin_id) REFERENCES administradores(id)
);
```

**`contribuyentes`** - Usuarios contribuyentes
```sql
CREATE TABLE contribuyentes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  nombre VARCHAR(255),
  nombre_usuario VARCHAR(100) UNIQUE,
  fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
  auth0_user_id VARCHAR(255) UNIQUE
);
```

**`solicitudes_eliminacion`** - Solicitudes de eliminación
```sql
CREATE TABLE solicitudes_eliminacion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hecho_id BIGINT NOT NULL,
  justificacion TEXT NOT NULL,
  estado VARCHAR(50) DEFAULT 'PENDIENTE',
  fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_resolucion DATETIME,
  contribuyente_id BIGINT,
  admin_id BIGINT,
  FOREIGN KEY (hecho_id) REFERENCES hechos(id),
  FOREIGN KEY (contribuyente_id) REFERENCES contribuyentes(id),
  FOREIGN KEY (admin_id) REFERENCES administradores(id)
);
```

### MongoDB (Datos Temporales)

**Base de datos:** `metamapa`  
**Host:** `localhost:27017`

**Colecciones:**

**`hechos_dinamicos`** - Hechos pendientes de aprobación
```javascript
{
  _id: ObjectId("507f1f77bcf86cd799439011"),
  titulo: "Incendio Forestal en Córdoba",
  descripcion: "Gran incendio...",
  categoria: "Incendio",
  estadoRevision: "PENDIENTE", // PENDIENTE, ACEPTADO, RECHAZADO
  sugerenciaDeCambio: null,
  fechaHecho: ISODate("2024-10-15T14:30:00Z"),
  fechaCarga: ISODate("2024-12-10T10:00:00Z"),
  ubicacion: {
    latitud: -31.4201,
    longitud: -64.1888,
    lugar: {
      pais: "Argentina",
      provincia: "Córdoba",
      municipio: "Villa Carlos Paz"
    }
  },
  etiquetas: ["incendio", "forestal"],
  esAnonimo: false,
  eliminado: false,
  contribuyenteId: "auth0|693f7f23ef51f52b03f354ef"
}
```

---

## Configuración y Despliegue

### Backend (gestor-solicitudes)

**application.properties:**

```properties
# Server
server.port=8080

# Database MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/utndds
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Auth0 OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/
spring.security.oauth2.resourceserver.jwt.audiences=https://metamapa-api

# Auth0 Configuration
auth0.domain=dev-x8zpgn3i6vnkjg4m.us.auth0.com
auth0.audience=https://metamapa-api
auth0.client-secret=${AUTH0_CLIENT_SECRET:}

# Fuentes externas
fuente.dinamica.url=http://localhost:8081
fuente.estatica.url=http://localhost:8083
```

**Ejecutar con Maven:**

```bash
cd gestor-solicitudes
mvn spring-boot:run
```

**Ejecutar con IntelliJ:**
1. Abre el proyecto en IntelliJ
2. Navega a `GestorSolicitudesApplication.java`
3. Clic derecho → Run 'GestorSolicitudesApplication'

---

### Frontend

**Instalar dependencias:**

```bash
cd frontEnd/metamapa-frontend
npm install
```

**Variables de entorno (`.env`):**

```env
VITE_AUTH0_DOMAIN=dev-x8zpgn3i6vnkjg4m.us.auth0.com
VITE_AUTH0_CLIENT_ID=YOUR_CLIENT_ID
VITE_AUTH0_AUDIENCE=https://metamapa-api
VITE_API_URL=http://localhost:8080
```

**Ejecutar en desarrollo:**

```bash
npm run dev
```

Abre `http://localhost:5173`

---

### Docker Compose (Base de datos)

**docker-compose.yml:**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: metamapa-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: utndds
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  mongodb:
    image: mongo:6.0
    container_name: metamapa-mongo
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db

volumes:
  mysql-data:
  mongo-data:
```

**Iniciar:**

```bash
docker-compose up -d
```

---

## Resumen de URLs

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Frontend** | `http://localhost:5173` | Aplicación React |
| **Backend API** | `http://localhost:8080` | Gestor de solicitudes |
| **Auth0** | `https://dev-x8zpgn3i6vnkjg4m.us.auth0.com` | Proveedor de identidad |
| **MySQL** | `localhost:3306` | Base de datos principal |
| **MongoDB** | `localhost:27017` | Base de datos temporal |

---

## Troubleshooting

### Problema: Roles no aparecen en JWT

**Solución:**
1. Verifica que el Action "Add Roles to Token" esté deployed
2. Verifica que esté en el flujo Login (Actions → Flows → Login)
3. Asigna roles al usuario (User Management → Users → Roles)
4. Cierra sesión completamente: `localStorage.clear()`, `sessionStorage.clear()`
5. Vuelve a hacer login

### Problema: Backend ERR_CONNECTION_REFUSED

**Solución:**
1. Verifica que MySQL esté corriendo: `docker ps` o XAMPP
2. Inicia el backend: IntelliJ → Run GestorSolicitudesApplication
3. Verifica puerto 8080: `netstat -ano | findstr :8080`

### Problema: 403 Forbidden en endpoints protegidos

**Solución:**
1. Verifica que el token JWT sea válido: `jwt.io`
2. Verifica que contenga el claim `permissions` o `https://metamapa.com/roles`
3. Verifica que el rol esté en mayúsculas: `ADMIN`, no `admin`
4. Verifica que el usuario tenga el rol asignado en Auth0

---

## Referencias

- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Auth0 React SDK](https://auth0.com/docs/quickstart/spa/react)
- [Auth0 Actions](https://auth0.com/docs/customize/actions)
- [React Leaflet](https://react-leaflet.js.org/)
