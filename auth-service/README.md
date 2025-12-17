# Auth Service - Servicio de Autenticación y Autorización

## 📋 Descripción

Módulo de Spring Boot que proporciona autenticación y autorización con Auth0 para el proyecto MetaMapa.

## 🎯 Características

- ✅ Validación de JWT tokens de Auth0
- ✅ Control de acceso basado en roles (RBAC)
- ✅ Configuración CORS para frontend React
- ✅ Endpoints de información de usuario
- ✅ Manejo de excepciones de seguridad
- ✅ Tests unitarios con JUnit

## 🚀 Ejecución

### Requisitos Previos

1. **Java 11 o superior**
2. **Maven 3.6+**
3. **Cuenta de Auth0 configurada**

### Variables de Entorno

Crear archivo `.env` o configurar variables de entorno:

```bash
# Obligatorias en producción
AUTH0_CLIENT_SECRET=tu_client_secret_aqui

# Opcionales
AUTH0_MANAGEMENT_TOKEN=tu_management_token_aqui
```

### Ejecutar Localmente

```bash
# Desde la raíz del proyecto
cd auth-service
mvn spring-boot:run
```

El servicio estará disponible en: http://localhost:8086

## 📡 Endpoints

### Públicos (sin autenticación)

- `GET /actuator/health` - Health check
- `GET /api/publica/**` - Endpoints públicos
- `GET /api/debug/**` - Debug (solo desarrollo)

### Autenticados (requieren JWT válido)

- `GET /api/auth/me` - Información del usuario actual
- `GET /api/auth/roles/check` - Verificar roles del usuario
- `GET /api/interna/**` - Endpoints internos

### Administrativos (requieren rol ADMIN)

- `GET /api/admin/**` - Endpoints administrativos

## 🔐 Configuración de Auth0

### 1. Configurar Aplicación en Auth0

```
Dashboard: https://manage.auth0.com/dashboard/us/dev-x8zpgn3i6vnkjg4m/
Application Type: Single Page Application
```

### 2. Settings

```
Domain: dev-x8zpgn3i6vnkjg4m.us.auth0.com
Client ID: 0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO
Client Secret: [Ver en dashboard]
```

### 3. Allowed Callback URLs

```
http://localhost:5173/callback
http://localhost:3000/callback
https://tu-dominio.com/callback
```

### 4. Allowed Logout URLs

```
http://localhost:5173
http://localhost:3000
https://tu-dominio.com
```

### 5. Allowed Web Origins

```
http://localhost:5173
http://localhost:8080
```

### 6. Crear API

```
Name: MetaMapa API
Identifier: https://metamapa-api
Signing Algorithm: RS256
```

### 7. Crear Roles

**Dashboard → User Management → Roles**

- `ADMIN` - Administrador del sistema
- `USER` - Usuario regular

### 8. Configurar Action para Roles

**Dashboard → Actions → Flows → Login → Create Action**

```javascript
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://metamapa.com';
  
  if (event.authorization) {
    api.accessToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
    api.idToken.setCustomClaim(`${namespace}/roles`, event.authorization.roles);
  }
};
```

**Deploy** y **Add to Flow**

## 🧪 Testing

### Ejecutar Tests

```bash
mvn test
```

### Probar con Postman

1. **Obtener Token de Auth0:**

```bash
curl --request POST \
  --url https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"0NvvByZW4f91QPIMeBv2iAvZDbXOU3xO",
    "client_secret":"TU_CLIENT_SECRET",
    "audience":"https://metamapa-api",
    "grant_type":"client_credentials"
  }'
```

2. **Usar Token en Requests:**

```bash
curl --request GET \
  --url http://localhost:8086/api/auth/me \
  --header 'authorization: Bearer TU_TOKEN_AQUI'
```

## 🔧 Configuración

### application.yml

Principal archivo de configuración. Ajustar:

- `server.port` - Puerto del servicio (default: 8086)
- `auth0.domain` - Tu dominio de Auth0
- `auth0.audience` - Audience de tu API
- `cors.allowed-origins` - Orígenes permitidos para CORS

### Logging

Para más detalles en logs:

```yaml
logging:
  level:
    org.springframework.security: TRACE
    com.metamapa.auth: DEBUG
```

## 📦 Estructura del Proyecto

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/metamapa/auth/
│   │   │   ├── AuthServiceApplication.java       # Clase principal
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Configuración de seguridad
│   │   │   │   └── Auth0Properties.java         # Propiedades de Auth0
│   │   │   ├── converter/
│   │   │   │   └── Auth0JwtAuthenticationConverter.java  # Extractor de roles
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java          # Endpoints de usuario
│   │   │   │   └── DebugController.java         # Endpoints de debug
│   │   │   └── exception/
│   │   │       └── AuthExceptionHandler.java    # Manejo de errores
│   │   └── resources/
│   │       └── application.yml                   # Configuración
│   └── test/
│       └── java/com/metamapa/auth/
│           └── config/
│               └── SecurityConfigTest.java       # Tests de seguridad
├── pom.xml                                       # Dependencias Maven
└── README.md                                     # Este archivo
```

## 🐛 Troubleshooting

### Error: "403 Forbidden"

**Causa:** Usuario no tiene el rol requerido.

**Solución:**
1. Verificar que el usuario tiene el rol asignado en Auth0 Dashboard
2. Verificar que el Action está agregando roles al JWT
3. Decodificar JWT en https://jwt.io/ y buscar el claim de roles

### Error: "401 Unauthorized"

**Causa:** Token inválido o expirado.

**Solución:**
1. Verificar que el token se envía en header: `Authorization: Bearer <token>`
2. Verificar que el token no expiró
3. Verificar issuer y audience en el token

### Error: CORS

**Causa:** Frontend y backend en dominios diferentes sin CORS configurado.

**Solución:**
1. Agregar origen del frontend a `cors.allowed-origins` en application.yml
2. Verificar que el navegador no está bloqueando la petición

### Error: "Issuer mismatch"

**Causa:** El issuer del JWT no coincide con la configuración.

**Solución:**
Verificar que `issuer-uri` en application.yml tiene el `/` al final:
```yaml
issuer-uri: https://dev-x8zpgn3i6vnkjg4m.us.auth0.com/
                                                    ^^^
```

## 📚 Recursos

- [Documentación de Auth0](https://auth0.com/docs)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT.io - Decodificar JWTs](https://jwt.io/)

## 🤝 Contribuir

1. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
2. Commit cambios: `git commit -am 'Agregar nueva funcionalidad'`
3. Push a la rama: `git push origin feature/nueva-funcionalidad`
4. Crear Pull Request

## 📄 Licencia

Proyecto académico - UTN

---

**Autor:** MetaMapa Team  
**Versión:** 1.0  
**Fecha:** Diciembre 2025
