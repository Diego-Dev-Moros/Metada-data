# Configuración de Auth0 Dashboard para MetaMapa

Este documento describe paso a paso la configuración realizada en Auth0 para el sistema de autenticación y autorización de MetaMapa.

## 📋 Tabla de Contenidos

1. [Acceso a Auth0](#1-acceso-a-auth0)
2. [Configuración de la Aplicación](#2-configuración-de-la-aplicación)
3. [Creación de la API](#3-creación-de-la-api)
4. [Creación de Roles](#4-creación-de-roles)
5. [Creación de Usuario de Prueba](#5-creación-de-usuario-de-prueba)
6. [Configuración del Action (CRÍTICO)](#6-configuración-del-action-crítico)
7. [Verificación de la Configuración](#7-verificación-de-la-configuración)

---

## 1. Acceso a Auth0

### 1.1 Ingreso al Dashboard

1. Navega a: https://manage.auth0.com
2. Inicia sesión con tu cuenta Auth0
3. Verás el tenant: **dev-x8zpgn3i6vnkjg4m.us.auth0.com**

---

## 2. Configuración de la Aplicación

### 2.1 Crear/Configurar Aplicación SPA

**Ruta:** Applications → Applications

1. Si no existe, crear nueva aplicación:
   - Click en **"+ Create Application"**
   - Name: `MetaMapa Frontend`
   - Application Type: **Single Page Web Application**
   - Click **Create**

2. En la pestaña **Settings**, configurar:

```
Application Name: MetaMapa Frontend
Application Type: Single Page Application
```

### 2.2 Configurar URLs Permitidas

En la misma pestaña Settings, configurar las siguientes URLs:

```
Allowed Callback URLs:
http://localhost:5173/callback

Allowed Logout URLs:
http://localhost:5173

Allowed Web Origins:
http://localhost:5173

Allowed Origins (CORS):
http://localhost:5173
```

### 2.3 Credenciales de la Aplicación

**IMPORTANTE:** Guardar estos valores (están en la pestaña Settings):

```
Domain: dev-x8zpgn3i6vnkjg4m.us.auth0.com
Client ID: 0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO
```

Estos valores ya están configurados en:
- Backend: `auth-service/src/main/resources/application.yml`
- Frontend: `frontEnd/metamapa-frontend/src/auth/auth0Config.js`

### 2.4 Guardar Cambios

Click en **"Save Changes"** al final de la página.

---

## 3. Creación de la API

### 3.1 Crear API

**Ruta:** Applications → APIs

1. Click en **"+ Create API"**
2. Completar el formulario:

```
Name: MetaMapa API
Identifier: https://metamapa-api
Signing Algorithm: RS256
```

**⚠️ IMPORTANTE:** El `Identifier` debe ser exactamente `https://metamapa-api` (no agregar barra final).

3. Click **"Create"**

### 3.2 Configuración de la API

La API se crea con configuración por defecto. Los valores importantes son:

```yaml
Identifier (Audience): https://metamapa-api
Signing Algorithm: RS256
Token Expiration: 86400 seconds (24 hours)
Allow Offline Access: No (por defecto)
```

---

## 4. Creación de Roles

### 4.1 Crear Roles

**Ruta:** User Management → Roles

#### Rol ADMIN

1. Click en **"+ Create Role"**
2. Completar:
   ```
   Name: ADMIN
   Description: Administrator role with full access
   ```
3. Click **"Create"**

#### Rol USER

1. Click en **"+ Create Role"** nuevamente
2. Completar:
   ```
   Name: USER
   Description: Standard user role with basic access
   ```
3. Click **"Create"**

### 4.2 Permisos de Roles

Por ahora, los roles no tienen permisos específicos asignados. Los permisos se controlan en el backend a nivel de endpoints mediante Spring Security.

**Estructura de Autorización en el Backend:**

```java
// Endpoint público - sin autenticación
/api/publica/** → permitAll()

// Endpoint protegido - requiere autenticación
/api/interna/** → authenticated()

// Endpoint admin - requiere rol ADMIN
/api/admin/** → hasRole("ADMIN")
```

---

## 5. Creación de Usuario de Prueba

### 5.1 Crear Usuario

**Ruta:** User Management → Users

1. Click en **"+ Create User"**
2. Completar el formulario:

```
Email: [tu-email@ejemplo.com]
Password: [contraseña-segura-mínimo-8-caracteres]
Connection: Username-Password-Authentication
```

3. **OPCIONAL:** Desmarcar "Send verification email" para pruebas inmediatas
4. Click **"Create"**

### 5.2 Asignar Roles al Usuario

1. Una vez creado el usuario, click sobre su email en la lista
2. Ve a la pestaña **"Roles"**
3. Click en **"Assign Roles"**
4. Seleccionar ambos roles:
   - ✅ ADMIN
   - ✅ USER
5. Click **"Assign"**

### 5.3 Verificar Asignación

En la pestaña Roles del usuario, deberías ver:

```
Roles Assigned:
- ADMIN
- USER
```

---

## 6. Configuración del Action (CRÍTICO)

Este es **EL PASO MÁS IMPORTANTE**. Sin este Action, los roles NO se incluirán en el JWT token y la autorización NO funcionará.

### 6.1 Crear Action Personalizado

**Ruta:** Actions → Library

1. Click en **"Create Action"**
2. Seleccionar **"Create Custom Action"**
3. Completar el formulario:

```
Name: Add Roles to Token
Trigger: Login / Post Login
Runtime: Node 22 (Recommended) o Node 18
```

4. Click **"Create"**

### 6.2 Código del Action

En el editor que se abre, **REEMPLAZAR TODO EL CÓDIGO** con:

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://metamapa.com';
  
  if (event.authorization) {
    // Obtener roles del usuario
    const roles = event.authorization.roles || [];
    
    // Agregar roles al access token
    api.accessToken.setCustomClaim(`${namespace}/roles`, roles);
    
    // Agregar roles al ID token (opcional)
    api.idToken.setCustomClaim(`${namespace}/roles`, roles);
  }
};
```

**Explicación del código:**

- `namespace`: Prefijo personalizado para los claims del JWT (debe ser una URL)
- `event.authorization.roles`: Roles asignados al usuario en Auth0
- `api.accessToken.setCustomClaim()`: Agrega los roles al Access Token (usado para API)
- `api.idToken.setCustomClaim()`: Agrega los roles al ID Token (información del usuario)

### 6.3 Desplegar el Action

1. Click en **"Deploy"** (botón azul arriba a la derecha)
2. Esperar a que aparezca el mensaje **"Deployed"** con un check verde ✅
3. El Action está ahora disponible pero **NO está activo aún**

### 6.4 Agregar Action al Flujo de Login

**Ruta:** Actions → Triggers (o Actions → Flows)

1. Click en **"Login / Post Login"**
2. Verás un diagrama de flujo:
   ```
   Start → [espacio vacío] → Complete
   ```
3. En el panel derecho (Custom Actions), buscar **"Add Roles to Token"**
4. **Arrastrar** el Action al centro del flujo (entre Start y Complete):
   ```
   Start → Add Roles to Token → Complete
   ```
5. Click en **"Apply"** (botón azul arriba a la derecha)

### 6.5 Verificar que el Action está Activo

En Actions → Triggers → Login, deberías ver:

```
Flow: Login / Post Login
└── Start
    └── Add Roles to Token ✓
        └── Complete
```

**⚠️ CRÍTICO:** Si el Action NO está en el flujo, los tokens JWT NO contendrán los roles y la autorización fallará.

---

## 7. Verificación de la Configuración

### 7.1 Checklist de Configuración

Verificar que todos estos elementos estén configurados:

- [ ] Aplicación SPA creada con Callback URLs configuradas
- [ ] API creada con identifier `https://metamapa-api`
- [ ] Roles ADMIN y USER creados
- [ ] Usuario de prueba creado
- [ ] Roles asignados al usuario
- [ ] Action "Add Roles to Token" creado y desplegado
- [ ] Action agregado al flujo de Login
- [ ] Backend corriendo en puerto 8086

### 7.2 URLs de Acceso Rápido

**Dashboard principal:**
- https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/

**Aplicaciones:**
- https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/applications

**APIs:**
- https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/apis

**Users:**
- https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/users

**Actions:**
- https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/actions/library

---

## 8. Estructura del JWT Token Resultante

### 8.1 Access Token

Después de configurar todo correctamente, el Access Token tendrá esta estructura:

```json
{
  "iss": "https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/",
  "sub": "auth0|[user-id]",
  "aud": [
    "https://metamapa-api"
  ],
  "iat": 1702598400,
  "exp": 1702684800,
  "azp": "0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO",
  "scope": "openid profile email",
  "https://metamapa.com/roles": [
    "ADMIN",
    "USER"
  ]
}
```

### 8.2 Claims Importantes

| Claim | Descripción | Valor |
|-------|-------------|-------|
| `iss` | Emisor del token | https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/ |
| `aud` | Audiencia (API) | https://metamapa-api |
| `sub` | Subject (identificador del usuario) | auth0\|[user-id] |
| `exp` | Expiración del token | Timestamp Unix |
| `https://metamapa.com/roles` | **Roles del usuario** | ["ADMIN", "USER"] |

### 8.3 Conversión de Roles en el Backend

El backend (clase `Auth0JwtAuthenticationConverter`) convierte los roles:

```
JWT Claim: ["ADMIN", "USER"]
    ↓
Spring Security: [ROLE_ADMIN, ROLE_USER]
```

Esto permite usar en los controladores:
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
```

---

## 9. Problemas Comunes y Soluciones

### 9.1 Token no contiene roles

**Síntoma:** El backend responde con 403 Forbidden en endpoints protegidos

**Causa:** El Action no está en el flujo de Login

**Solución:**
1. Ir a Actions → Triggers → Login
2. Verificar que "Add Roles to Token" esté entre Start y Complete
3. Si no está, arrastrarlo desde el panel derecho
4. Click en Apply

### 9.2 Error "Invalid audience"

**Síntoma:** Backend rechaza el token con error de audiencia

**Causa:** El identifier de la API no coincide con la configuración del backend

**Solución:**
1. Verificar en Auth0: Applications → APIs → MetaMapa API
2. El Identifier debe ser exactamente: `https://metamapa-api`
3. Verificar en backend: `application.yml` → `auth0.audience`

### 9.3 CORS Error en frontend

**Síntoma:** Browser bloquea requests con error CORS

**Causa:** La URL del frontend no está en Allowed Origins

**Solución:**
1. Ir a Applications → Applications → MetaMapa Frontend → Settings
2. En "Allowed Origins (CORS)" agregar: `http://localhost:5173`
3. Save Changes

### 9.4 Usuario no tiene roles

**Síntoma:** Token válido pero sin roles en el claim personalizado

**Causa:** No se asignaron roles al usuario

**Solución:**
1. Ir a User Management → Users
2. Click en el usuario
3. Pestaña Roles → Assign Roles
4. Seleccionar ADMIN y USER → Assign

---

## 10. Testing con Postman

### 10.1 Obtener Access Token Manualmente

**Endpoint:** `https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/oauth/token`

**Método:** POST

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "grant_type": "password",
  "username": "[tu-email@ejemplo.com]",
  "password": "[tu-contraseña]",
  "audience": "https://metamapa-api",
  "client_id": "0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO",
  "scope": "openid profile email"
}
```

**Response:**
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "id_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "scope": "openid profile email",
  "expires_in": 86400,
  "token_type": "Bearer"
}
```

**⚠️ NOTA:** Para que funcione el grant type "password", debes habilitar "Password" en:
- Applications → Applications → MetaMapa Frontend → Settings → Advanced Settings → Grant Types
- Marcar: ✅ Password

### 10.2 Probar Endpoints del Backend

**Endpoint público (sin token):**
```
GET http://localhost:8086/api/publica/test
```

**Endpoint protegido (requiere token):**
```
GET http://localhost:8086/api/auth/me
Authorization: Bearer [access_token]
```

**Endpoint admin (requiere rol ADMIN):**
```
GET http://localhost:8086/api/admin/test
Authorization: Bearer [access_token]
```

---

## 11. Configuraciones de Producción

Cuando lleves el sistema a producción, deberás actualizar:

### 11.1 En Auth0 Dashboard

**Allowed Callback URLs:**
```
https://tu-dominio.com/callback
```

**Allowed Logout URLs:**
```
https://tu-dominio.com
```

**Allowed Web Origins y CORS:**
```
https://tu-dominio.com
```

### 11.2 En el Backend

Archivo: `auth-service/src/main/resources/application.yml`

```yaml
cors:
  allowed-origins:
    - https://tu-dominio.com
```

### 11.3 En el Frontend

Archivo: `frontEnd/metamapa-frontend/src/auth/auth0Config.js`

```javascript
const auth0Config = {
  domain: 'dev-x8zpgn3i6vnkjg4m.us.auth0.com',
  clientId: '0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO',
  redirect_uri: 'https://tu-dominio.com/callback',
  audience: 'https://metamapa-api',
  // ... resto de config
};
```

---

## 12. Recursos Adicionales

### 12.1 Documentación Oficial de Auth0

- **Actions:** https://auth0.com/docs/customize/actions
- **APIs:** https://auth0.com/docs/get-started/apis
- **SPA Authentication:** https://auth0.com/docs/quickstart/spa
- **Custom Claims:** https://auth0.com/docs/secure/tokens/json-web-tokens/create-custom-claims

### 12.2 Documentación del Proyecto

- [Autorizacion.md](./Autorizacion.md) - Guía técnica completa
- [Sistema-Auth0.md](./Sistema-Auth0.md) - Overview del sistema
- [GUIA-PRUEBA-RAPIDA.md](./GUIA-PRUEBA-RAPIDA.md) - Testing rápido
- [README-IMPLEMENTACION-AUTH.md](./README-IMPLEMENTACION-AUTH.md) - Checklist de implementación

---

## 📝 Notas Finales

- **Tenant:** dev-x8zpgn3i6vnkjg4m.us.auth0.com (Desarrollo)
- **Client ID:** 0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO
- **API Audience:** https://metamapa-api
- **Namespace Custom Claims:** https://metamapa.com
- **Puerto Backend:** 8086
- **Puerto Frontend:** 5173

**Configuración realizada el:** 14 de Diciembre de 2025

**Estado:** ✅ Completado y verificado
