# Configuración de Frontend - Integración con Auth0

## 📋 Instalación de Dependencias

Primero, instalar las dependencias necesarias:

```bash
cd frontEnd/metamapa-frontend
npm install @auth0/auth0-react axios react-router-dom
```

## 🔧 Configuración

### 1. Variables de Entorno

Crear archivo `.env` en la raíz del proyecto frontend:

```bash
# URL del backend API
VITE_API_URL=http://localhost:8080/api

# Auth0 Configuration (opcional, ya está en auth0Config.js)
VITE_AUTH0_DOMAIN=dev-x8zpgn3i6vnkjg4m.us.auth0.com
VITE_AUTH0_CLIENT_ID=0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO
VITE_AUTH0_AUDIENCE=https://metamapa-api
```

### 2. Actualizar main.jsx

Envolver la aplicación con Auth0Provider:

```jsx
// src/main.jsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import auth0Config from './auth/auth0Config';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Auth0Provider {...auth0Config}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Auth0Provider>
  </React.StrictMode>
);
```

### 3. Actualizar App.jsx

Configurar rutas y API:

```jsx
// src/App.jsx
import { useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { AuthProvider } from './auth/AuthContext';
import { configureApiAuth } from './services/api';

// Páginas
import Home from './pages/Home';
import Mapa from './pages/Mapa';
import AdminPanel from './pages/AdminPanel';
import CallbackPage from './pages/CallbackPage';
import AccesoDenegadoPage from './pages/AccesoDenegadoPage';

// Componentes de autenticación
import ProtectedRoute from './auth/ProtectedRoute';

// Estilos
import './App.css';

function App() {
  const { getAccessTokenSilently } = useAuth0();

  // Configurar API con Auth0 token
  useEffect(() => {
    configureApiAuth(getAccessTokenSilently);
  }, [getAccessTokenSilently]);

  return (
    <AuthProvider>
      <Routes>
        {/* Rutas públicas */}
        <Route path="/" element={<Home />} />
        <Route path="/callback" element={<CallbackPage />} />
        <Route path="/acceso-denegado" element={<AccesoDenegadoPage />} />

        {/* Rutas protegidas (requieren autenticación) */}
        <Route 
          path="/mapa" 
          element={
            <ProtectedRoute>
              <Mapa />
            </ProtectedRoute>
          } 
        />

        {/* Rutas de administrador (requieren rol ADMIN) */}
        <Route 
          path="/admin/*" 
          element={
            <ProtectedRoute requiredRole="ADMIN">
              <AdminPanel />
            </ProtectedRoute>
          } 
        />
      </Routes>
    </AuthProvider>
  );
}

export default App;
```

### 4. Ejemplo de Uso en Componentes

#### NavBar con Login/Logout

```jsx
// src/components/NavBar.jsx
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import LoginButton from '../auth/LoginButton';
import LogoutButton from '../auth/LogoutButton';
import UserProfile from '../auth/UserProfile';

const NavBar = () => {
  const { isAuthenticated, isAdmin } = useAuth();

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">MetaMapa</Link>
      </div>
      
      <div className="navbar-menu">
        <Link to="/">Inicio</Link>
        
        {isAuthenticated && (
          <>
            <Link to="/mapa">Mapa</Link>
            {isAdmin() && <Link to="/admin">Admin Panel</Link>}
          </>
        )}
      </div>
      
      <div className="navbar-auth">
        {isAuthenticated ? (
          <>
            <UserProfile />
            <LogoutButton />
          </>
        ) : (
          <LoginButton />
        )}
      </div>
    </nav>
  );
};

export default NavBar;
```

#### Hacer llamadas API

```jsx
// src/pages/Mapa.jsx
import { useState, useEffect } from 'react';
import api from '../services/api';

const Mapa = () => {
  const [hechos, setHechos] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarHechos();
  }, []);

  const cargarHechos = async () => {
    try {
      setLoading(true);
      // El token JWT se agrega automáticamente por el interceptor
      const response = await api.get('/interna/hechos');
      setHechos(response.data);
    } catch (error) {
      console.error('Error cargando hechos:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Cargando...</div>;

  return (
    <div className="mapa-container">
      <h1>Mapa de Hechos</h1>
      {/* Renderizar mapa y hechos */}
    </div>
  );
};

export default Mapa;
```

#### Página de Admin

```jsx
// src/pages/AdminPanel.jsx
import { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import api from '../services/api';

const AdminPanel = () => {
  const { user } = useAuth();
  const [hechosPendientes, setHechosPendientes] = useState([]);

  useEffect(() => {
    cargarHechosPendientes();
  }, []);

  const cargarHechosPendientes = async () => {
    try {
      const response = await api.get('/admin/hechos/pendientes');
      setHechosPendientes(response.data);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const aprobarHecho = async (id) => {
    try {
      await api.post(`/admin/hechos/${id}/aprobar`);
      cargarHechosPendientes(); // Recargar lista
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="admin-panel">
      <h1>Panel de Administración</h1>
      <p>Bienvenido, {user?.name}</p>
      
      <h2>Hechos Pendientes</h2>
      {hechosPendientes.map(hecho => (
        <div key={hecho.id} className="hecho-card">
          <h3>{hecho.titulo}</h3>
          <p>{hecho.descripcion}</p>
          <button onClick={() => aprobarHecho(hecho.id)}>
            Aprobar
          </button>
        </div>
      ))}
    </div>
  );
};

export default AdminPanel;
```

## 🚀 Ejecutar Frontend

```bash
cd frontEnd/metamapa-frontend
npm run dev
```

El frontend estará disponible en: http://localhost:5173

## 📝 Estructura de Archivos Creados

```
frontEnd/metamapa-frontend/src/
├── auth/
│   ├── auth0Config.js          # Configuración de Auth0
│   ├── AuthContext.jsx          # Context con helpers de roles
│   ├── LoginButton.jsx          # Botón de login
│   ├── LogoutButton.jsx         # Botón de logout
│   ├── ProtectedRoute.jsx       # Componente de ruta protegida
│   ├── UserProfile.jsx          # Perfil de usuario
│   └── UserProfile.css          # Estilos del perfil
├── pages/
│   ├── CallbackPage.jsx         # Página de callback OAuth
│   ├── AccesoDenegadoPage.jsx   # Página 403
│   └── AccesoDenegadoPage.css   # Estilos 403
└── services/
    └── api.js                   # Cliente HTTP con interceptores
```

## 🔒 Flujo de Autenticación

1. Usuario hace clic en "Iniciar Sesión"
2. Redirige a Auth0 Universal Login
3. Usuario ingresa credenciales
4. Auth0 redirige a `/callback`
5. `CallbackPage` procesa el callback
6. Usuario redirigido a `/mapa`
7. Todas las peticiones API incluyen JWT automáticamente

## ✅ Verificación

### Verificar que el token se envía

1. Abrir DevTools (F12)
2. Ir a Network
3. Hacer una petición a la API
4. Ver Headers → Request Headers
5. Debe aparecer: `Authorization: Bearer eyJhbGc...`

### Verificar roles en JWT

1. Copiar el token del header Authorization
2. Ir a https://jwt.io/
3. Pegar el token
4. Buscar el claim: `"https://metamapa.com/roles"`
5. Debe contener tus roles: `["ADMIN"]` o `["USER"]`

## 🐛 Troubleshooting

### Error: "redirect_uri_mismatch"

**Solución:** Agregar la URL a "Allowed Callback URLs" en Auth0 Dashboard

### Error: Token no se envía

**Solución:** Verificar que `configureApiAuth(getAccessTokenSilently)` se llama en App.jsx

### Error: CORS

**Solución:** Verificar que el backend tiene configurado el origen correcto en `application.yml`

---

**¡Todo listo para usar Auth0 en tu frontend!** 🎉
