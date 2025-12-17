# 🔍 Guía de Verificación de Roles - Frontend

## ✅ Cambios Implementados

### 1. **Navbar.jsx**
- ✅ Agregado detección de rol `CONTRIBUTOR`
- ✅ Actualizado indicador visual de roles:
  - 🔴 Rojo para ADMIN
  - 🟡 Amarillo para CONTRIBUTOR
  - 🟢 Verde para USER
- ✅ Agregado botón "Crear Hecho" visible para CONTRIBUTOR y ADMIN
- ✅ Agregado console.log para debugging de roles

### 2. **ProtectedRoute.jsx**
- ✅ Integrado con Auth0 (antes usaba localStorage)
- ✅ Lee roles desde el JWT de Auth0
- ✅ Normaliza roles a mayúsculas
- ✅ Muestra página de acceso denegado con información de roles
- ✅ Muestra loading mientras verifica autenticación

### 3. **App.jsx**
- ✅ Actualizado roles a mayúsculas: `ADMIN`, `CONTRIBUTOR`, `USER`
- ✅ Rutas protegidas correctamente asignadas

### 4. **CallbackPage.jsx**
- ✅ Corregida redirección de `/mapa` a `/`

---

## 🧪 Cómo Probar

### Paso 1: Verificar el Token en el Navegador

1. **Inicia sesión** en la aplicación
2. **Abre DevTools** (F12)
3. **Ve a la Consola** (Console tab)
4. **Busca el log** que dice `=== DEBUG AUTH0 ROLES ===`
5. **Verifica que veas algo así:**

```
=== DEBUG AUTH0 ROLES ===
Usuario: contributor@test.com
Objeto user completo: {sub: "auth0|...", email: "...", ...}
Roles extraídos: ["CONTRIBUTOR"]
isAdmin: false
isContributor: true
isUser: false
========================
```

### Paso 2: Verificar el Token JWT

1. **En DevTools**, ve a **Application** → **Local Storage**
2. Busca una clave que contenga `@@user@@` o similar (Auth0 guarda el token aquí)
3. **Copia el token completo**
4. **Pégalo en** https://jwt.io
5. **En la sección "Decoded"**, busca:

```json
{
  "permissions": ["CONTRIBUTOR"],
  "https://metamapa.com/roles": ["CONTRIBUTOR"],
  "aud": "https://metamapa-api",
  ...
}
```

### Paso 3: Verificar Elementos Visibles

#### Para Usuario ADMIN:
- ✅ Debe ver: "👑 Logueado → Admin" (rojo)
- ✅ Debe ver: Botón "Crear Hecho"
- ✅ Debe ver: Dropdown "Panel Admin"
- ✅ Debe ver: "Mi Perfil"

#### Para Usuario CONTRIBUTOR:
- ✅ Debe ver: "✏️ Logueado → Contributor" (amarillo)
- ✅ Debe ver: Botón "Crear Hecho"
- ✅ NO debe ver: Dropdown "Panel Admin"
- ✅ Debe ver: "Mi Perfil"

#### Para Usuario USER:
- ✅ Debe ver: "👤 Logueado → User" (verde)
- ✅ NO debe ver: Botón "Crear Hecho"
- ✅ NO debe ver: Dropdown "Panel Admin"
- ✅ NO debe ver: "Mi Perfil"

### Paso 4: Probar Rutas Protegidas

#### Como CONTRIBUTOR, intenta acceder a:
- ✅ `/crear-hecho` → Debe funcionar
- ✅ `/perfil` → Debe funcionar
- ❌ `/reportes-hechos` → Debe mostrar "Acceso Denegado"
- ❌ `/solicitudes` → Debe mostrar "Acceso Denegado"

#### Como USER, intenta acceder a:
- ❌ `/crear-hecho` → Debe mostrar "Acceso Denegado"
- ❌ `/perfil` → Debe mostrar "Acceso Denegado"

---

## 🐛 Problemas Comunes y Soluciones

### Problema: Sigue mostrando "User" para todos los roles

**Causa:** Los roles no están llegando en el token JWT desde Auth0

**Solución:**

1. **Verifica que la Action esté desplegada en Auth0:**
   - Ve a: Actions → Flows → Login
   - Verifica que "Add Roles to Token" esté en el flow
   - Si no está, arrástrala y haz clic en "Apply"

2. **Verifica que los usuarios tengan roles asignados:**
   - Ve a: User Management → Users
   - Click en un usuario → Tab "Roles"
   - Debe tener al menos un rol asignado

3. **Limpia el caché de Auth0:**
   ```javascript
   // En la consola del navegador
   localStorage.clear();
   sessionStorage.clear();
   // Luego recarga la página y vuelve a iniciar sesión
   ```

4. **Verifica el código de la Action en Auth0:**
   ```javascript
   // Debe incluir estas líneas:
   api.accessToken.setCustomClaim('permissions', userRoles);
   api.idToken.setCustomClaim('roles', userRoles);
   ```

### Problema: El token no tiene los roles

**Solución:**

1. **Verifica en jwt.io** que el token contenga:
   - `"permissions": ["ADMIN"]` o `["CONTRIBUTOR"]` o `["USER"]`
   - O `"https://metamapa.com/roles": ["ADMIN"]`

2. **Si no están, revisa los logs de Auth0:**
   - Ve a: Monitoring → Logs
   - Busca logs de la Action "Add Roles to Token"
   - Verifica si hay errores

### Problema: Error "No routes matched location '/mapa'"

**Solución:** Ya está corregido en `CallbackPage.jsx` - ahora redirige a `/`

---

## 📋 Checklist Final

Antes de continuar, verifica que:

- [ ] El token JWT contiene el claim `permissions` con los roles
- [ ] El console.log muestra los roles correctamente
- [ ] El indicador visual muestra el rol correcto (Admin/Contributor/User)
- [ ] Los botones de "Crear Hecho" aparecen para CONTRIBUTOR y ADMIN
- [ ] El "Panel Admin" solo aparece para ADMIN
- [ ] Las rutas protegidas funcionan según el rol
- [ ] La página de "Acceso Denegado" muestra los roles requeridos

---

## 🔄 Si Nada Funciona

1. **Cierra sesión completamente**
2. **Limpia el caché:**
   ```javascript
   localStorage.clear();
   sessionStorage.clear();
   ```
3. **Recarga la página** (Ctrl + Shift + R)
4. **Vuelve a iniciar sesión**
5. **Revisa la consola** para ver el log de roles
6. **Si sigue sin funcionar**, verifica que Auth0 esté correctamente configurado siguiendo: [README-CONFIGURAR-AUTH0-ROLES.md](./README-CONFIGURAR-AUTH0-ROLES.md)

---

## 📞 Siguiente Paso

Si todo funciona en el frontend pero no en el backend, el problema está en:
- La configuración de Spring Security
- El converter de roles en el backend
- La configuración de `application.properties`

Revisa la documentación del backend en: [README-ROLES-AUTORIZACIONES.md](./README-ROLES-AUTORIZACIONES.md)
