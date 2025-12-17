# 🚨 SOLUCIÓN: Roles Vacíos en Auth0

## El Problema

Los logs muestran que el usuario está autenticado pero **no tiene roles asignados**:

```javascript
Roles extraídos: Array(0) // ← VACÍO ❌
isAdmin: false
isContributor: false
isUser: false
```

Esto significa que **la Action de Auth0 NO está funcionando correctamente**.

---

## ✅ Solución Paso a Paso (5 minutos)

### **Paso 1: Verificar que la Action esté creada**

1. Ve a: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/actions/library
2. Busca una Action llamada **"Add Roles to Token"** o similar
3. Si **NO existe**, créala ahora:

#### Crear la Action:

1. Click en **+ Build Custom**
2. **Name**: `Add Roles to Token`
3. **Trigger**: `Login / Post Login`
4. **Runtime**: Node 18 (o el más reciente)
5. Click en **Create**

#### Código de la Action (COPIA ESTO):

```javascript
/**
 * Handler that will be called during the execution of a PostLogin flow.
 *
 * @param {Event} event - Details about the user and the context in which they are logging in.
 * @param {PostLoginAPI} api - Interface whose methods can be used to change the behavior of the login.
 */
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://metamapa.com';
  
  console.log('=== ADD ROLES TO TOKEN ACTION ===');
  console.log('User:', event.user.email);
  console.log('User roles:', event.authorization.roles);
  
  if (event.authorization) {
    // Obtener roles del usuario
    const roles = event.authorization.roles || [];
    
    // Si no tiene roles, asignar USER por defecto
    const userRoles = roles.length > 0 ? roles : ['USER'];
    
    console.log('Roles to add to token:', userRoles);
    
    // IMPORTANTE: Agregar roles en múltiples ubicaciones para máxima compatibilidad
    
    // 1. En el claim 'permissions' (Spring Security lo busca aquí)
    api.accessToken.setCustomClaim('permissions', userRoles);
    
    // 2. En el namespace personalizado
    api.accessToken.setCustomClaim(`${namespace}/roles`, userRoles);
    
    // 3. En el ID token para el frontend
    api.idToken.setCustomClaim('roles', userRoles);
    api.idToken.setCustomClaim(`${namespace}/roles`, userRoles);
    
    console.log('Roles successfully added to token');
  }
  
  console.log('=================================');
};
```

6. Click en **Deploy** (botón superior derecho)
7. ⏳ **Espera** a que el estado cambie a "Deployed"

---

### **Paso 2: Agregar la Action al Flow de Login**

🚨 **ESTE ES EL PASO MÁS IMPORTANTE** - Si no haces esto, la Action no se ejecutará.

1. Ve a: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/actions/flows/login

2. Deberías ver algo así:
   ```
   Start → [espacio vacío] → Complete
   ```

3. En el **panel derecho** ("Custom" tab), busca **"Add Roles to Token"**

4. **Arrastra la Action** desde el panel derecho al espacio entre "Start" y "Complete"

5. Ahora debería verse así:
   ```
   Start → [Add Roles to Token] → Complete
   ```

6. Click en **Apply** (botón superior derecho)

7. ✅ Verifica que veas un mensaje de éxito

---

### **Paso 3: Asignar el rol ADMIN a tu usuario**

1. Ve a: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/users

2. Busca tu usuario: `diegomoros.dev.ar@gmail.com`

3. Click en el usuario

4. Ve a la pestaña **"Roles"**

5. Click en **"Assign Roles"**

6. Selecciona **ADMIN** (o el rol que quieras)

7. Click en **"Assign"**

8. ✅ Deberías ver el rol listado en la tabla

---

### **Paso 4: Limpiar el caché y volver a iniciar sesión**

Esto es **CRUCIAL** porque Auth0 cachea los tokens.

#### En el Frontend:

1. **Abre la consola del navegador** (F12)

2. **Ejecuta estos comandos:**
   ```javascript
   localStorage.clear();
   sessionStorage.clear();
   ```

3. **Recarga la página** (Ctrl + Shift + R o Cmd + Shift + R)

4. **Cierra sesión** en la aplicación

5. **Vuelve a iniciar sesión**

6. **Verifica la consola** - Deberías ver:
   ```javascript
   === DEBUG AUTH0 ROLES ===
   Usuario: diegomoros.dev.ar@gmail.com
   Roles extraídos: ["ADMIN"]  // ← ¡AHORA DEBE TENER ROLES! ✅
   isAdmin: true
   isContributor: false
   isUser: false
   ========================
   ```

---

### **Paso 5: Verificar el Token JWT**

Para estar 100% seguro de que funciona:

1. **En DevTools**, ve a **Application** → **Local Storage**

2. Busca una clave que empiece con `@@auth0spajs@@`

3. **Copia el valor completo**

4. Ve a https://jwt.io y **pégalo**

5. En la sección **"Decoded"**, busca:
   ```json
   {
     "permissions": ["ADMIN"],
     "https://metamapa.com/roles": ["ADMIN"],
     "aud": "https://metamapa-api",
     "iss": "https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/",
     ...
   }
   ```

6. ✅ Si ves los roles aquí, **¡funcionó!**

---

## 🔍 Verificar si la Action se Ejecuta

Para ver los logs de la Action:

1. Ve a: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/logs

2. Busca logs después de hacer login

3. Deberías ver entradas que digan:
   ```
   === ADD ROLES TO TOKEN ACTION ===
   User: diegomoros.dev.ar@gmail.com
   User roles: ["ADMIN"]
   Roles to add to token: ["ADMIN"]
   Roles successfully added to token
   =================================
   ```

4. Si **NO ves estos logs**, la Action no se está ejecutando → Revisa el Paso 2

---

## 🎯 Checklist Final

Antes de continuar, verifica que:

- [ ] La Action "Add Roles to Token" existe y está **Deployed**
- [ ] La Action está **en el flow de Login** (entre Start y Complete)
- [ ] El usuario tiene un rol asignado (**ADMIN** recomendado para pruebas)
- [ ] Has limpiado localStorage y sessionStorage
- [ ] Has cerrado sesión y vuelto a iniciar sesión
- [ ] Los logs de la consola muestran roles: `["ADMIN"]`
- [ ] El indicador en el navbar muestra "👑 Logueado → Admin" (en rojo)
- [ ] El token JWT contiene el claim `permissions` con los roles

---

## 🐛 Si Sigue Sin Funcionar

### Problema: La Action no aparece en el panel derecho del Flow

**Solución:**
- Asegúrate de que la Action esté **Deployed** (no "Draft")
- Recarga la página del dashboard
- Ve a la pestaña "Custom" en el panel derecho

### Problema: Los roles no aparecen en el token

**Solución:**
- Verifica los logs en: Monitoring → Logs
- Busca errores en la Action
- Asegúrate de que el código esté exactamente como se muestra arriba

### Problema: "Email no verificado"

**Solución:**
- En tu perfil de Auth0, marca el email como verificado manualmente
- O revisa tu bandeja de entrada para el email de verificación

---

## 📞 Próximos Pasos

Una vez que los roles funcionen:

1. **Prueba con diferentes roles:**
   - Crea un usuario USER
   - Crea un usuario CONTRIBUTOR
   - Verifica que cada uno vea opciones diferentes

2. **Verifica las rutas protegidas:**
   - Como USER, intenta acceder a `/crear-hecho` → Debe negar el acceso
   - Como CONTRIBUTOR, intenta acceder a `/reportes-hechos` → Debe negar el acceso
   - Como ADMIN, todo debe funcionar

3. **Revisa la nueva página de perfil:**
   - Ve a `/perfil`
   - Deberías ver tus datos de Auth0
   - Si no tienes roles, verás instrucciones de debug

---

## ✨ Nueva Página de Perfil

He creado **PerfilAuth0Page** que muestra:

- ✅ Email del usuario
- ✅ Estado de verificación del email
- ✅ Roles asignados (con colores)
- ✅ Nickname
- ✅ ID de usuario
- ✅ Última actualización
- ✅ Información de debug si no hay roles
- ✅ Botones para volver al mapa y cerrar sesión

**Ruta:** `/perfil`

La página vieja de perfil (con hechos del backend) está ahora en `/perfil-completo` y solo accesible para CONTRIBUTOR y ADMIN.

---

¿Necesitas ayuda con algún paso específico? ¡Avísame!
