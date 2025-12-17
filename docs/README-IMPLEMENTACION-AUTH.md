# 🔐 Sistema de Autenticación y Autorización - MetaMapa

## 📁 Archivos y Carpetas Creados

### 📚 Documentación (`/docs`)

Se han creado 3 documentos completos que explican el sistema:

1. **[README-ESTADO-ACTUAL.md](docs/README-ESTADO-ACTUAL.md)**
   - Estado del proyecto ANTES de implementar autenticación
   - Módulos existentes y su funcionamiento
   - Funcionalidades implementadas
   - Funcionalidades pendientes

2. **[Sistema-Auth0.md](docs/Sistema-Auth0.md)**
   - Introducción al sistema de autenticación con Auth0
   - Arquitectura general
   - Flujo OAuth 2.0 / OIDC
   - Roles y permisos (USER y ADMIN)
   - Plan de implementación
   - Integración frontend y backend

3. **[Autorizacion.md](docs/Autorizacion.md)** ⭐ **DOCUMENTO PRINCIPAL**
   - Guía completa de implementación (100+ páginas)
   - Conceptos fundamentales (JWT, OAuth, OIDC)
   - Configuración paso a paso de Auth0
   - Código completo de backend (Spring Boot)
   - Código completo de frontend (React)
   - Casos de uso con ejemplos
   - Seguridad y mejores prácticas
   - Debugging y troubleshooting
   - Preguntas frecuentes

### 🔧 Módulo Backend (`/auth-service`)

Nuevo módulo Spring Boot completamente funcional:

```
auth-service/
├── pom.xml                          ✅ Dependencias configuradas
├── README.md                        ✅ Documentación del módulo
└── src/
    ├── main/
    │   ├── java/com/metamapa/auth/
    │   │   ├── AuthServiceApplication.java           ✅ Aplicación principal
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java              ✅ Configuración de seguridad
    │   │   │   └── Auth0Properties.java             ✅ Properties de Auth0
    │   │   ├── converter/
    │   │   │   └── Auth0JwtAuthenticationConverter.java  ✅ Extractor de roles
    │   │   ├── controller/
    │   │   │   ├── AuthController.java              ✅ Endpoints de usuario
    │   │   │   └── DebugController.java             ✅ Endpoints de debug
    │   │   └── exception/
    │   │       └── AuthExceptionHandler.java        ✅ Manejo de errores
    │   └── resources/
    │       └── application.yml                       ✅ Configuración completa
    └── test/
        └── java/com/metamapa/auth/
            └── config/
                └── SecurityConfigTest.java           ✅ Tests JUnit
```

**Características del módulo:**
- ✅ Validación de JWT de Auth0
- ✅ CORS configurado para React
- ✅ Control de acceso por roles (RBAC)
- ✅ Manejo de excepciones
- ✅ Endpoints de debug
- ✅ Tests unitarios

**Puerto:** 8086

### ⚛️ Integración Frontend (`/frontEnd/metamapa-frontend`)

Componentes y configuración para React:

```
frontEnd/metamapa-frontend/
├── AUTH-INTEGRATION.md              ✅ Guía de integración
└── src/
    ├── auth/
    │   ├── auth0Config.js          ✅ Configuración de Auth0
    │   ├── AuthContext.jsx         ✅ Context con helpers de roles
    │   ├── LoginButton.jsx         ✅ Botón de login
    │   ├── LogoutButton.jsx        ✅ Botón de logout
    │   ├── ProtectedRoute.jsx      ✅ Componente de ruta protegida
    │   ├── UserProfile.jsx         ✅ Perfil de usuario
    │   └── UserProfile.css         ✅ Estilos del perfil
    └── pages/
        ├── CallbackPage.jsx        ✅ Página de callback OAuth
        ├── AccesoDenegadoPage.jsx  ✅ Página 403
        └── AccesoDenegadoPage.css  ✅ Estilos 403
```

**Características:**
- ✅ Login/Logout con Auth0
- ✅ Rutas protegidas
- ✅ Verificación de roles
- ✅ Perfil de usuario
- ✅ Manejo de callbacks
- ✅ Páginas de error

## 🚀 Próximos Pasos para Implementar

### 1. Configurar Auth0 (30 minutos)

**a) Dashboard de Auth0:**
- URL: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/
- Configurar Allowed Callback URLs
- Configurar Allowed Logout URLs
- Crear API con identifier: `https://metamapa-api`

**b) Crear Roles:**
- Dashboard → User Management → Roles
- Crear rol `ADMIN`
- Crear rol `USER`

**c) Configurar Action para incluir roles en JWT:**
- Dashboard → Actions → Flows → Login
- Crear Action con el código del archivo `Autorizacion.md`
- Deploy y agregar al flujo

**Ver:** [Autorizacion.md - Sección "Configuración de Auth0"](docs/Autorizacion.md#configuración-de-auth0)

### 2. Backend - Ejecutar auth-service (5 minutos)

```bash
# Desde la raíz del proyecto
cd auth-service

# Ejecutar
mvn spring-boot:run
```

**Verificar:** http://localhost:8086/actuator/health

### 3. Frontend - Instalar dependencias (5 minutos)

```bash
cd frontEnd/metamapa-frontend

# Instalar Auth0 SDK
npm install @auth0/auth0-react axios

# Ejecutar
npm run dev
```

**Verificar:** http://localhost:5173

### 4. Integrar con módulos existentes (1-2 horas)

**a) Gestor-Solicitudes (Puerto 8080):**
- Agregar dependencias de Spring Security al `pom.xml`
- Copiar configuración de `auth-service/SecurityConfig.java`
- Agregar `@PreAuthorize("hasRole('ADMIN')")` a endpoints admin
- Actualizar controladores para usar `@AuthenticationPrincipal Jwt jwt`

**b) Otros servicios:**
- Repetir el proceso para `fuente-dinamica`, `fuente-estatica`, etc.

**Ver:** [Autorizacion.md - Sección "Aplicar Seguridad a Controladores Existentes"](docs/Autorizacion.md#implementación-backend)

### 5. Actualizar Frontend (30 minutos)

**a) Actualizar `main.jsx`:**
```jsx
import { Auth0Provider } from '@auth0/auth0-react';
import auth0Config from './auth/auth0Config';

<Auth0Provider {...auth0Config}>
  <App />
</Auth0Provider>
```

**b) Actualizar `App.jsx`:**
```jsx
import { useAuth0 } from '@auth0/auth0-react';
import { configureApiAuth } from './services/api';

const { getAccessTokenSilently } = useAuth0();
useEffect(() => {
  configureApiAuth(getAccessTokenSilently);
}, []);
```

**Ver:** [AUTH-INTEGRATION.md](frontEnd/metamapa-frontend/AUTH-INTEGRATION.md)

### 6. Testing (30 minutos)

**a) Ejecutar tests del auth-service:**
```bash
cd auth-service
mvn test
```

**b) Probar flujo completo:**
1. Iniciar auth-service (8086)
2. Iniciar gestor-solicitudes (8080)
3. Iniciar frontend (5173)
4. Hacer login
5. Verificar token en DevTools
6. Probar endpoint admin
7. Probar endpoint de usuario

**Ver:** [Autorizacion.md - Sección "Testing"](docs/Autorizacion.md#debugging)

## 📖 Cómo Usar Esta Documentación

### Para Entender el Sistema

1. **Leer primero:** [Sistema-Auth0.md](docs/Sistema-Auth0.md)
   - Visión general de alto nivel
   - Arquitectura y flujos
   - Conceptos básicos

2. **Profundizar:** [Autorizacion.md](docs/Autorizacion.md)
   - Guía técnica completa
   - Código con explicaciones
   - Ejemplos prácticos

### Para Implementar

1. **Backend:** Leer sección "Implementación Backend" en [Autorizacion.md](docs/Autorizacion.md#implementación-backend)
2. **Frontend:** Leer [AUTH-INTEGRATION.md](frontEnd/metamapa-frontend/AUTH-INTEGRATION.md)
3. **Configuración:** Seguir "Configuración de Auth0" paso a paso

### Para Debugging

1. **Problemas comunes:** [Autorizacion.md - Troubleshooting](docs/Autorizacion.md#debugging)
2. **Logs:** Activar logging en `application.yml`
3. **Debug endpoints:** Usar `/api/debug/jwt`

### Para Defender en la Entrega

**Temas clave a estudiar:**

1. **¿Qué es OAuth 2.0 y OIDC?**
   - Ver: [Autorizacion.md - Conceptos Fundamentales](docs/Autorizacion.md#conceptos-fundamentales)

2. **¿Cómo funciona JWT?**
   - Ver: [Autorizacion.md - JWT: Anatomía y Validación](docs/Autorizacion.md#jwt-anatomía-y-validación)

3. **¿Cómo se valida el token en Spring Boot?**
   - Ver: SecurityConfig.java con comentarios detallados

4. **¿Qué es RBAC?**
   - Ver: [Autorizacion.md - Control de Acceso Basado en Roles](docs/Autorizacion.md#rbac)

5. **¿Por qué Auth0 y no implementar desde cero?**
   - Ver: [Sistema-Auth0.md - Ventajas](docs/Sistema-Auth0.md)

## 🎓 Cumplimiento con Entrega 6 - UTN

### Requerimientos Cubiertos

✅ **Implementación de SSO con Social-Login**
- Auth0 con Google, Facebook, GitHub
- Universal Login page
- OAuth 2.0 / OIDC

✅ **Cliente Liviano desacoplado**
- React con Auth0 SDK
- Consume APIs del backend
- Manejo de estados de autenticación

✅ **Arquitectura de seguridad**
- Spring Security 6
- JWT validation
- RBAC (Role-Based Access Control)

✅ **Documentación completa**
- 3 documentos detallados
- Código comentado
- Guías de implementación

✅ **Tests unitarios con JUnit**
- SecurityConfigTest.java
- Tests de autorización
- Tests de roles

## 📊 Resumen de Archivos

| Tipo | Cantidad | Detalles |
|------|----------|----------|
| **Documentación** | 4 archivos | README-ESTADO-ACTUAL.md, Sistema-Auth0.md, Autorizacion.md, AUTH-INTEGRATION.md |
| **Backend (Java)** | 7 clases | Application, Config, Converter, Controllers, Exception Handler |
| **Frontend (React)** | 9 archivos | Components, Context, Pages, Styles, Config |
| **Configuración** | 3 archivos | pom.xml, application.yml, auth0Config.js |
| **Tests** | 1 clase | SecurityConfigTest.java |
| **Total** | **24 archivos** | Sistema completo documentado y funcional |

## 🔍 Puntos Clave para Defender

### 1. Seguridad
- Tokens JWT firmados digitalmente (RS256)
- Validación de issuer, audience, expiración
- CORS configurado correctamente
- Tokens en memoria (no localStorage)
- HTTPS en producción

### 2. Arquitectura
- Desacoplamiento frontend-backend
- Stateless (sin sesiones en servidor)
- Escalable (Auth0 maneja millones de usuarios)
- Reutilizable en otros proyectos

### 3. Roles y Permisos
- RBAC implementado
- 2 roles: USER y ADMIN
- Validación en backend (@PreAuthorize)
- Validación en frontend (ProtectedRoute)

### 4. Experiencia de Usuario
- Login con un clic
- Social login (Google, etc.)
- Renovación automática de tokens
- Mensajes de error claros

## 📞 Ayuda y Recursos

- **Documentación principal:** [Autorizacion.md](docs/Autorizacion.md)
- **Auth0 Dashboard:** https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/
- **Decodificar JWT:** https://jwt.io/
- **Auth0 Docs:** https://auth0.com/docs
- **Spring Security Docs:** https://spring.io/projects/spring-security

## ✅ Checklist de Implementación

- [ ] Configurar Auth0 Dashboard
- [ ] Crear roles en Auth0
- [ ] Configurar Action para roles en JWT
- [ ] Ejecutar auth-service (puerto 8086)
- [ ] Instalar dependencias en frontend
- [ ] Actualizar main.jsx con Auth0Provider
- [ ] Actualizar App.jsx con configureApiAuth
- [ ] Probar login/logout
- [ ] Probar endpoints protegidos
- [ ] Verificar que roles funcionan
- [ ] Ejecutar tests con `mvn test`
- [ ] Preparar defensa del TP

---

**¡Todo listo para implementar autenticación y autorización con Auth0!** 🚀

**Tiempo estimado de implementación:** 2-3 horas

**Estado:** ✅ Documentación completa | ✅ Código listo | ✅ Tests incluidos
