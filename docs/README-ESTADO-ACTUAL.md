# 📋 Estado Actual del Proyecto MetaMapa

**Fecha:** Diciembre 2025  
**Versión:** 1.0-SNAPSHOT  
**Framework:** Spring Boot 2.7.18  
**Java:** 11  
**Frontend:** React + Vite

## 🎯 Descripción General

MetaMapa es un sistema de mapeo colaborativo de información que permite a usuarios reportar hechos (desastres naturales, sanitarios, tecnológicos) y visualizarlos en un mapa interactivo. El sistema implementa una arquitectura de microservicios con Spring Boot y un frontend desacoplado en React.

## 🏗️ Arquitectura Actual

### Módulos Backend (Spring Boot)

#### 1. **AGREGADOR** (Puerto 8081)
- **Función:** Proceso automático de agregación de hechos desde fuentes
- **Ejecución:** Background, scheduler cada 2 minutos
- **Base de datos:** MySQL (metamapa)
- **Componentes clave:**
  - `AgregacionScheduler`: Automatiza agregación periódica
  - `ServicioAgregacion`: Core del proceso
  - `IngestaOrquestadorService`: Orquesta ingesta de múltiples fuentes
  - `DepuracionService`, `NormalizacionService`: Limpieza de datos
  - `FingerprintService`: Detección de duplicados

#### 2. **GESTOR-SOLICITUDES** (Puerto 8080)
- **Función:** API principal para frontend y operaciones CRUD
- **APIs expuestas:**
  - `/api/admin/*` - Gestión administrativa
  - `/api/interna/*` - API para frontend
  - `/api/publica/*` - API para federación externa
- **Servicios:**
  - `ColeccionService`, `SolicitudService`, `ContribuyenteService`
  - `DetectorDeSpam`: Validación de contenido

#### 3. **FUENTE-DINAMICA** (Puerto 8082)
- **Función:** Gestión de hechos reportados por usuarios
- **Base de datos:** MongoDB
- **Estados:** PENDIENTE → ACEPTADO → RECHAZADO

#### 4. **FUENTE-ESTATICA** (Puerto 8083)
- **Función:** Carga y procesamiento de archivos CSV
- **Almacenamiento:** Sistema de archivos + MySQL

#### 5. **FUENTE-PROXY** (Puerto 8084)
- **Función:** Integración con fuentes externas/federadas

#### 6. **STATS** (Puerto 8085)
- **Función:** Estadísticas y métricas del sistema

#### 7. **COMMON**
- **Función:** Clases compartidas (DTOs, utilidades)

#### 8. **DOMAIN**
- **Función:** Entidades del dominio compartidas

### Frontend

#### **metamapa-frontend**
- **Framework:** React 18 + Vite
- **Ubicación:** `/frontEnd/metamapa-frontend/`
- **Componentes:**
  - Visualización de mapa interactivo
  - Formularios de reporte de hechos
  - Panel administrativo
  - Gestión de colecciones

## 🔄 Flujos Principales Implementados

### 1. Reporte de Hecho por Usuario
```
Usuario → Frontend → Gestor-Solicitudes → Fuente-Dinámica (MongoDB)
Estado: PENDIENTE → Espera aprobación admin
```

### 2. Aprobación de Hecho
```
Admin → Gestor-Solicitudes → Fuente-Dinámica (estado: ACEPTADO)
Scheduler del Agregador → Lee hechos ACEPTADOS → MySQL
```

### 3. Carga de CSV
```
Admin → Gestor-Solicitudes → Fuente-Estática (procesa CSV)
Scheduler del Agregador → Lee hechos procesados → MySQL
```

### 4. Solicitud de Eliminación
```
Usuario → Frontend → Gestor-Solicitudes (crea solicitud)
Admin revisa → Aprueba/Rechaza → Actualiza estado en BD
```

## 🗄️ Bases de Datos

### MySQL (localhost:3306/metamapa)
- Hechos agregados y normalizados
- Colecciones
- Contribuyentes
- Solicitudes de eliminación
- Algoritmos de consenso

### MongoDB
- Hechos dinámicos reportados por usuarios
- Estados: PENDIENTE, ACEPTADO, RECHAZADO

## 📦 Dependencias Principales

- Spring Boot 2.7.18
- Spring Data JPA
- Spring Data MongoDB
- MySQL Connector
- OpenFeign (comunicación entre servicios)
- Lombok
- Validation API
- Docker Compose (para infraestructura)

## ✅ Funcionalidades Implementadas

- ✅ Reporte de hechos por usuarios
- ✅ Visualización de hechos en mapa
- ✅ Panel administrativo
- ✅ Gestión de colecciones
- ✅ Aprobación/rechazo de hechos
- ✅ Solicitudes de eliminación
- ✅ Carga masiva de CSV
- ✅ Algoritmos de consenso
- ✅ Detección de duplicados (fingerprinting)
- ✅ Normalización de datos
- ✅ API REST documentada (Postman)

## 🚫 Funcionalidades NO Implementadas

- ❌ **Autenticación y Autorización** (SSO)
- ❌ **Social Login** (Google, Facebook, etc.)
- ❌ **Control de acceso basado en roles**
- ❌ **Protección de endpoints con JWT**
- ❌ **Gestión de sesiones seguras**
- ❌ **Integración con Auth0**

## 📁 Estructura de Directorios

```
ProyectoK/
├── agregador/              # Servicio de agregación
├── gestor-solicitudes/     # API principal
├── fuente-dinamica/        # Hechos dinámicos
├── fuente-estatica/        # Carga CSV
├── fuente-proxy/           # Fuentes externas
├── stats/                  # Estadísticas
├── common/                 # Clases compartidas
├── domain/                 # Entidades
├── metaMapaApplication/    # Launcher principal
├── frontEnd/
│   └── metamapa-frontend/  # React app
├── docs/                   # 📝 Documentación (NUEVO)
├── csv/                    # Archivos CSV de ejemplo
├── postman/                # Colecciones API
└── docker-compose.yml      # Infraestructura
```

## 🎓 Contexto Académico

**Universidad:** UTN  
**Trabajo Práctico:** Entrega 6  
**Tema:** Arquitectura Web MVC e Interfaz de Usuario

### Requerimientos de la Entrega 6

1. ✅ Diseño y maquetado de interfaces de usuario
2. ✅ Implementación de Cliente Liviano desacoplado
3. ❌ **Integración con SSO** (PENDIENTE - próxima implementación)
4. ❌ Implementación de pruebas unitarias con JUNIT (pendiente)

## 🔍 Próximos Pasos

El proyecto está listo para incorporar el sistema de **Autenticación y Autorización con Auth0**, que será el enfoque principal de la siguiente fase de desarrollo.

## 📞 Endpoints Principales

### Gestor-Solicitudes (8080)
- `POST /api/interna/hechos` - Reportar hecho
- `GET /api/interna/hechos` - Listar hechos
- `POST /api/admin/hechos/{id}/aprobar` - Aprobar hecho
- `POST /api/admin/colecciones` - Crear colección
- `GET /api/publica/colecciones` - Listar colecciones públicas

### Fuente-Dinámica (8082)
- `POST /api/hechos` - Crear hecho
- `GET /api/hechos` - Listar hechos
- `PUT /api/hechos/{id}/estado` - Cambiar estado

### Fuente-Estática (8083)
- `POST /api/fuente-estatica/csv` - Cargar CSV

## 🐳 Docker

El proyecto incluye `docker-compose.yml` con:
- MySQL
- MongoDB
- (Preparado para agregar servicios adicionales)

---

**Nota:** Este documento refleja el estado del proyecto **ANTES** de implementar el sistema de autenticación con Auth0.
