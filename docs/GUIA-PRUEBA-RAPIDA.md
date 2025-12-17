# 🧪 Guía de Prueba Rápida - Sistema Auth0

## ⏱️ Tiempo estimado: 20-30 minutos

## Paso 1: Configurar Auth0 Dashboard (10 minutos)

### 1.1 Acceder al Dashboard

Ve a: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/applications/0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO/settings

### 1.2 Configurar URLs (CRÍTICO)

En la sección **Application URIs**, configura:

**Allowed Callback URLs:**
```
http://localhost:5173/callback, http://localhost:3000/callback
```

**Allowed Logout URLs:**
```
http://localhost:5173, http://localhost:3000
```

**Allowed Web Origins:**
```
http://localhost:5173, http://localhost:3000, http://localhost:8086, http://localhost:8080
```

**Allowed Origins (CORS):**
```
http://localhost:5173, http://localhost:3000
```

**Guardar cambios** (botón al final de la página)

### 1.3 Crear API en Auth0

1. **Dashboard → Applications → APIs → Create API**

```
Name: MetaMapa API
Identifier: https://metamapa-api
Signing Algorithm: RS256
```

2. **Hacer clic en "Create"**

### 1.4 Crear Roles (opcional pero recomendado)

1. **Dashboard → User Management → Roles → Create Role**

**Rol ADMIN:**
```
Name: ADMIN
Description: Administrador del sistema
```

**Rol USER:**
```
Name: USER
Description: Usuario regular
```

### 1.5 Asignar Rol a tu Usuario

1. **Dashboard → User Management → Users**
2. **Selecciona tu usuario**
3. **Tab "Roles" → Assign Roles**
4. **Selecciona "ADMIN"**

### 1.6 Configurar Action para Roles (IMPORTANTE)

1. **Dashboard → Actions → Library → Create Action**
2. **Selecciona "Login / Post Login"**
3. **Name:** `Add Roles to Token`
4. **Copiar este código:**

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://metamapa.com';
  
  if (event.authorization) {
    api.accessToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
    api.idToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
  }
};
```

5. **Deploy** (botón arriba a la derecha)
6. **Dashboard → Actions → Flows → Login**
7. **Arrastra "Add Roles to Token" al flujo** (entre Start y Complete)
8. **Apply** (botón arriba a la derecha)

---

## Paso 2: Ejecutar Backend (auth-service) (5 minutos)

### 2.1 Compilar el proyecto

```bash
# Desde la raíz del proyecto
cd c:\Users\diego_moros\Desktop\ProyectoK-v1\ProyectoK

# Compilar todo
mvn clean install -DskipTests
```

### 2.2 Ejecutar auth-service

```bash
# Ir al módulo auth-service
cd auth-service

# Ejecutar
mvn spring-boot:run
```

**Verificar que funciona:**

Abre en el navegador: http://localhost:8086/actuator/health

Deberías ver:
```json
{
  "status": "UP"
}
```

**Dejar este terminal abierto** (el servicio debe estar ejecutándose)

---

## Paso 3: Instalar Dependencias Frontend (5 minutos)

### 3.1 Abrir nuevo terminal PowerShell

```bash
cd c:\Users\diego_moros\Desktop\ProyectoK-v1\ProyectoK\frontEnd\metamapa-frontend
```

### 3.2 Instalar dependencias de Auth0

```bash
npm install @auth0/auth0-react
```

### 3.3 Verificar que package.json tiene axios

```bash
npm list axios
```

Si no está instalado:
```bash
npm install axios
```

---

## Paso 4: Actualizar Frontend para usar Auth0 (5 minutos)

### 4.1 Actualizar main.jsx

Abre: `frontEnd/metamapa-frontend/src/main.jsx`

**Agregar al inicio:**
```jsx
import { Auth0Provider } from '@auth0/auth0-react';
import auth0Config from './auth/auth0Config';
```

**Envolver el App con Auth0Provider:**
```jsx
<Auth0Provider {...auth0Config}>
  <App />
</Auth0Provider>
```

Debería quedar algo así:
```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { Auth0Provider } from '@auth0/auth0-react'
import auth0Config from './auth/auth0Config'
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Auth0Provider {...auth0Config}>
      <App />
    </Auth0Provider>
  </React.StrictMode>,
)
```

### 4.2 Actualizar App.jsx

Abre: `frontEnd/metamapa-frontend/src/App.jsx`

**Agregar al inicio:**
```jsx
import { useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { configureApiAuth } from './services/api';
import LoginButton from './auth/LoginButton';
import LogoutButton from './auth/LogoutButton';
import UserProfile from './auth/UserProfile';
```

**Dentro del componente App, agregar:**
```jsx
const { getAccessTokenSilently, isLoading, isAuthenticated } = useAuth0();

useEffect(() => {
  configureApiAuth(getAccessTokenSilently);
}, [getAccessTokenSilently]);

if (isLoading) {
  return <div>Cargando...</div>;
}
```

**Agregar botones de login/logout en el JSX:**
```jsx
<div style={{ padding: '20px' }}>
  <LoginButton />
  <LogoutButton />
  {isAuthenticated && <UserProfile />}
  {/* Tu contenido existente */}
</div>
```

### 4.3 Crear archivo de configuración API (si no existe)

Crea: `frontEnd/metamapa-frontend/src/services/api.js`

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8086/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

let getAccessTokenSilently = null;

export const configureApiAuth = (getTokenFunc) => {
  getAccessTokenSilently = getTokenFunc;
};

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
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      console.error('No autenticado');
    } else if (error.response?.status === 403) {
      console.error('Acceso denegado');
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## Paso 5: Ejecutar Frontend (2 minutos)

```bash
npm run dev
```

**Verificar:** Deberías ver algo como:
```
VITE v5.x.x  ready in XXX ms

➜  Local:   http://localhost:5173/
```

---

## Paso 6: Probar el Sistema 🎉

### 6.1 Abrir el navegador

Ve a: http://localhost:5173

### 6.2 Hacer Login

1. **Hacer clic en "Iniciar Sesión"**
2. Se abrirá la página de Auth0
3. **Ingresar credenciales** (tu cuenta de Auth0)
4. Serás redirigido de vuelta a la app

### 6.3 Verificar que funcionó

Deberías ver:
- ✅ Botón "Cerrar Sesión"
- ✅ Tu perfil con foto, nombre y email
- ✅ Badge con tu rol (ADMIN)

### 6.4 Ver el Token en DevTools

1. **Abre DevTools** (F12)
2. **Tab "Console"**
3. **Escribe:**
```javascript
// Ver usuario
localStorage
```

4. **Para ver el token decodificado:**

Ve a: https://jwt.io/

Copia el token desde Network → Headers → Authorization → Bearer [token]

Pégalo en jwt.io y verás el contenido decodificado.

### 6.5 Probar Endpoint Protegido

En DevTools Console:
```javascript
fetch('http://localhost:8086/api/auth/me', {
  headers: {
    'Authorization': 'Bearer ' + await auth0Client.getAccessTokenSilently()
  }
})
.then(r => r.json())
.then(console.log)
```

Deberías ver tu información de usuario.

---

## Paso 7: Probar Roles y Permisos

### 7.1 Crear página de prueba Admin

Crea: `frontEnd/metamapa-frontend/src/pages/TestAdminPage.jsx`

```jsx
import { useAuth } from '../auth/AuthContext';

const TestAdminPage = () => {
  const { isAdmin, user } = useAuth();

  return (
    <div style={{ padding: '20px' }}>
      <h1>Test Admin Page</h1>
      <p>Usuario: {user?.email}</p>
      <p>Es Admin: {isAdmin() ? '✅ SÍ' : '❌ NO'}</p>
      {isAdmin() ? (
        <div style={{ background: 'lightgreen', padding: '20px' }}>
          ¡Tienes acceso de ADMIN!
        </div>
      ) : (
        <div style={{ background: 'lightcoral', padding: '20px' }}>
          No tienes permisos de ADMIN
        </div>
      )}
    </div>
  );
};

export default TestAdminPage;
```

### 7.2 Agregar ruta protegida

En `App.jsx`:
```jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import TestAdminPage from './pages/TestAdminPage';

// Dentro del return
<BrowserRouter>
  <Routes>
    <Route path="/" element={/* tu home */} />
    <Route 
      path="/test-admin" 
      element={
        <ProtectedRoute requiredRole="ADMIN">
          <TestAdminPage />
        </ProtectedRoute>
      } 
    />
  </Routes>
</BrowserRouter>
```

### 7.3 Probar

Ve a: http://localhost:5173/test-admin

- Si eres ADMIN → verás la página
- Si no eres ADMIN → serás redirigido a /acceso-denegado

---

## ✅ Checklist de Verificación

- [ ] Auth0 Dashboard configurado (URLs)
- [ ] API creada en Auth0 (https://metamapa-api)
- [ ] Roles creados (ADMIN, USER)
- [ ] Action configurado y en el flujo
- [ ] auth-service ejecutándose (puerto 8086)
- [ ] Frontend con dependencias instaladas
- [ ] main.jsx actualizado con Auth0Provider
- [ ] App.jsx actualizado con useAuth0
- [ ] Frontend ejecutándose (puerto 5173)
- [ ] Login funciona y redirige correctamente
- [ ] Puedo ver mi perfil con roles
- [ ] Endpoint /api/auth/me responde con mis datos
- [ ] Página admin solo accesible con rol ADMIN

---

## 🐛 Problemas Comunes

### Error: "Callback URL mismatch"
**Solución:** Verificar que en Auth0 Dashboard → Settings → Allowed Callback URLs esté:
```
http://localhost:5173/callback
```

### Error: "Invalid audience"
**Solución:** Verificar que en Auth0 Dashboard → APIs exista la API con identifier:
```
https://metamapa-api
```

### Error: CORS en consola del navegador
**Solución:** Verificar que en Auth0 Dashboard → Settings → Allowed Origins (CORS) esté:
```
http://localhost:5173
```

### No veo los roles en el JWT
**Solución:** 
1. Verificar que el Action está en el flujo (Dashboard → Actions → Flows → Login)
2. Verificar que el usuario tiene roles asignados
3. Hacer logout y volver a hacer login para obtener nuevo token

### Puerto 8086 ya en uso
**Solución:** 
```bash
# Encontrar proceso
netstat -ano | findstr :8086

# Matar proceso (reemplazar PID)
taskkill /PID <número_pid> /F
```

---

## 🎯 Próximos Pasos

Una vez que todo funciona:

1. **Integrar con gestor-solicitudes (puerto 8080)**
   - Copiar SecurityConfig al módulo
   - Proteger endpoints existentes

2. **Crear más páginas protegidas**
   - Panel de administración
   - Gestión de hechos
   - Aprobación de solicitudes

3. **Agregar más roles**
   - MODERATOR
   - VIEWER
   - etc.

4. **Tests**
   - Ejecutar: `mvn test` en auth-service
   - Crear tests e2e para frontend

---

## 📞 Ayuda

Si algo no funciona:

1. **Ver logs de auth-service** en el terminal
2. **Ver Console de DevTools** en el navegador
3. **Ver Network tab** para ver requests fallidos
4. **Consultar:** [docs/Autorizacion.md](Autorizacion.md) - Sección Troubleshooting

---

**¡Listo! Sistema de autenticación funcionando** 🎉
