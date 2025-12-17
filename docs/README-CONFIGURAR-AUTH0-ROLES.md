# Guía: Configurar Roles en Auth0 para MetaMapa

Esta guía explica paso a paso cómo configurar Auth0 para que los roles de usuario se envíen automáticamente en el token JWT en el claim `permissions`.

## 📋 Tabla de Contenidos

1. [Crear Roles en Auth0](#1-crear-roles-en-auth0)
2. [Crear una Action para Agregar Roles al JWT](#2-crear-una-action-para-agregar-roles-al-jwt)
3. [Asignar Roles a Usuarios](#3-asignar-roles-a-usuarios)
4. [Verificar la Configuración](#4-verificar-la-configuración)

---

## 1. Crear Roles en Auth0

### Paso 1: Acceder al Dashboard de Auth0
1. Ve a: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m
2. En el menú lateral izquierdo, selecciona **User Management** → **Roles**

### Paso 2: Crear los Tres Roles

Necesitas crear los siguientes roles:

#### **Rol: USER**
- **Name**: `USER`
- **Description**: `Usuario visualizador - Puede ver hechos y colecciones pero no crear ni editar contenido`
- Click en **Create**

#### **Rol: CONTRIBUTOR**
- **Name**: `CONTRIBUTOR`
- **Description**: `Usuario contribuyente - Puede crear hechos, editar su perfil y sus propios hechos (dentro de 7 días)`
- Click en **Create**

#### **Rol: ADMIN**
- **Name**: `ADMIN`
- **Description**: `Administrador - Control total del sistema, acceso a API Administrativa`
- Click en **Create**

### Paso 3: Configurar Permisos (Opcional)
Si deseas definir permisos granulares, puedes agregar permisos a cada rol. Para este proyecto, los roles por sí solos son suficientes.

---

## 2. Crear una Action para Agregar Roles al JWT

Auth0 Actions permiten modificar el token JWT antes de enviarlo al cliente. Vamos a crear una Action que agregue los roles del usuario al claim `permissions`.

### Paso 1: Acceder a Actions
1. En el menú lateral de Auth0, selecciona **Actions** → **Library**
2. Click en **+ Build Custom**

### Paso 2: Configurar la Action
- **Name**: `Add Roles to Token`
- **Trigger**: Selecciona `Login / Post Login`
- **Runtime**: Deja el valor por defecto (Node 18 o superior)
- Click en **Create**

### Paso 3: Agregar el Código de la Action

Reemplaza el código por defecto con el siguiente:

```javascript
/**
 * Action: Add Roles to Token
 * Trigger: Login / Post Login
 * 
 * Esta Action agrega los roles del usuario al token JWT
 * en el claim 'permissions' para que el backend pueda validarlos.
 */

exports.onExecutePostLogin = async (event, api) => {
  // Namespace para claims personalizados (debe coincidir con tu backend)
  const namespace = 'https://metamapa.com';
  
  // Obtener roles del usuario
  if (event.authorization) {
    const roles = event.authorization.roles || [];
    
    // Si el usuario no tiene roles, asignar USER por defecto
    const userRoles = roles.length > 0 ? roles : ['USER'];
    
    // Agregar roles al access token (JWT) en el claim 'permissions'
    // Spring Security los buscará aquí con el converter que configuramos
    api.accessToken.setCustomClaim(`${namespace}/roles`, userRoles);
    api.accessToken.setCustomClaim('permissions', userRoles);
    
    // También agregarlos al ID token para uso en el frontend
    api.idToken.setCustomClaim(`${namespace}/roles`, userRoles);
    api.idToken.setCustomClaim('roles', userRoles);
    
    // Log para debugging (remover en producción)
    console.log(`Usuario: ${event.user.email}, Roles: ${userRoles.join(', ')}`);
  }
};
```

### Paso 4: Guardar y Desplegar
1. Click en **Deploy** (botón superior derecho)
2. Espera a que la Action se despliegue correctamente

### Paso 5: Agregar la Action al Flow de Login
1. Ve a **Actions** → **Flows**
2. Selecciona el flow **Login**
3. Arrastra tu Action `Add Roles to Token` desde el panel derecho al flow (entre "Start" y "Complete")
4. Click en **Apply** para guardar los cambios

---

## 3. Asignar Roles a Usuarios

### Opción A: Asignar desde la UI de Auth0

1. Ve a **User Management** → **Users**
2. Selecciona un usuario
3. Click en la pestaña **Roles**
4. Click en **Assign Roles**
5. Selecciona el rol apropiado (USER, CONTRIBUTOR, o ADMIN)
6. Click en **Assign**

### Opción B: Asignar automáticamente en el registro

Si quieres que los nuevos usuarios tengan un rol por defecto, modifica la Action anterior:

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://metamapa.com';
  
  if (event.authorization) {
    let roles = event.authorization.roles || [];
    
    // Si es la primera vez que inicia sesión y no tiene roles
    if (roles.length === 0 && event.stats.logins_count === 1) {
      // Asignar rol USER por defecto a nuevos usuarios
      // NOTA: Esto solo afecta el token actual, no persiste en Auth0
      // Para persistir, necesitas usar Management API
      roles = ['USER'];
    }
    
    const userRoles = roles.length > 0 ? roles : ['USER'];
    
    api.accessToken.setCustomClaim(`${namespace}/roles`, userRoles);
    api.accessToken.setCustomClaim('permissions', userRoles);
    api.idToken.setCustomClaim(`${namespace}/roles`, userRoles);
    api.idToken.setCustomClaim('roles', userRoles);
  }
};
```

### Usuarios de Prueba Recomendados

Crea al menos 3 usuarios de prueba, uno para cada rol:

1. **admin@metamapa.test** → Rol: ADMIN
2. **contributor@metamapa.test** → Rol: CONTRIBUTOR
3. **user@metamapa.test** → Rol: USER

---

## 4. Verificar la Configuración

### Paso 1: Verificar el Token JWT

Después de configurar todo, prueba lo siguiente:

1. **Inicia sesión en tu aplicación** con uno de los usuarios de prueba
2. **Copia el Access Token** (puedes verlo en las DevTools del navegador o en Network tab)
3. **Decodifica el token** en https://jwt.io
4. **Verifica que contenga los roles**:

```json
{
  "permissions": ["ADMIN"],
  "https://metamapa.com/roles": ["ADMIN"],
  "sub": "auth0|123456789",
  "aud": "https://metamapa-api",
  "iss": "https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/",
  ...
}
```

### Paso 2: Probar Endpoints del Backend

Usa Postman o curl para probar los endpoints:

#### Test 1: Usuario sin rol (debe fallar)
```bash
curl -X GET "http://localhost:8080/api/interna/perfil" \
  -H "X-Contribuyente-Id: 1"
# Debe retornar 401 Unauthorized
```

#### Test 2: CONTRIBUTOR accediendo a su perfil (debe funcionar)
```bash
curl -X GET "http://localhost:8080/api/interna/perfil" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-Contribuyente-Id: 1"
# Debe retornar 200 OK con datos del perfil
```

#### Test 3: USER intentando acceder a API Admin (debe fallar)
```bash
curl -X GET "http://localhost:8080/api/admin/solicitudes" \
  -H "Authorization: Bearer USER_JWT_TOKEN"
# Debe retornar 403 Forbidden
```

#### Test 4: ADMIN accediendo a API Admin (debe funcionar)
```bash
curl -X GET "http://localhost:8080/api/admin/solicitudes" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
# Debe retornar 200 OK con lista de solicitudes
```

### Paso 3: Verificar en el Backend

Activa los logs de Spring Security en `application.properties`:

```properties
logging.level.org.springframework.security=DEBUG
```

Busca en los logs líneas como:

```
DEBUG o.s.s.o.s.r.a.JwtAuthenticationConverter : Authorities extracted from JWT: [ROLE_ADMIN]
```

---

## 🔍 Troubleshooting

### Problema: Los roles no aparecen en el token

**Solución:**
1. Verifica que la Action esté desplegada y en el flow de Login
2. Verifica que los usuarios tengan roles asignados en Auth0
3. Limpia el caché de Auth0: cierra sesión completamente y vuelve a iniciar sesión
4. Revisa los logs de la Action en Auth0: **Monitoring** → **Logs**

### Problema: Backend no reconoce los roles

**Solución:**
1. Verifica que el `Auth0JwtGrantedAuthoritiesConverter` esté buscando en el claim correcto:
   ```java
   // En Auth0JwtGrantedAuthoritiesConverter.java
   private static final String PERMISSIONS_CLAIM = "permissions";
   ```

2. Verifica que Auth0 esté configurado correctamente en `application.properties`:
   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/
   ```

3. Verifica que el audience sea correcto:
   ```properties
   spring.security.oauth2.resourceserver.jwt.audiences=https://metamapa-api
   ```

### Problema: CORS errors

**Solución:**
Asegúrate de que Auth0 tenga configuradas las URLs permitidas:

1. Ve a **Applications** → Tu aplicación
2. En **Allowed Callback URLs**, agrega: `http://localhost:5173/callback`
3. En **Allowed Logout URLs**, agrega: `http://localhost:5173`
4. En **Allowed Web Origins**, agrega: `http://localhost:5173`

---

## 📚 Recursos Adicionales

- [Auth0 Actions Documentation](https://auth0.com/docs/customize/actions)
- [Auth0 Authorization Core](https://auth0.com/docs/manage-users/access-control/rbac)
- [Auth0 Custom Claims](https://auth0.com/docs/secure/tokens/json-web-tokens/create-custom-claims)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

---

## ✅ Checklist Final

Antes de pasar a producción, verifica que:

- [ ] Los 3 roles estén creados en Auth0 (USER, CONTRIBUTOR, ADMIN)
- [ ] La Action "Add Roles to Token" esté creada y desplegada
- [ ] La Action esté agregada al flow de Login
- [ ] Los usuarios de prueba tengan roles asignados
- [ ] Los tokens JWT contengan el claim `permissions` con los roles
- [ ] El backend valide correctamente los roles
- [ ] Los endpoints estén protegidos según la tabla de restricciones
- [ ] El frontend maneje correctamente los errores 401 y 403
- [ ] Los logs de seguridad estén configurados para debugging

---

¿Necesitas ayuda con algún paso específico? Consulta la documentación completa en [README-ROLES-AUTORIZACIONES.md](./README-ROLES-AUTORIZACIONES.md).
