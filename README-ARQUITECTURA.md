# 🔄 División de Módulos: Agregador y Gestor de Solicitudes

## 📁 Estructura del Proyecto

### **🟢 AGREGADOR** (Puerto 8081 - Solo procesos en background)
**Responsabilidad:** Proceso automático de agregación de hechos desde fuentes

**Ubicación:** `/agregador`

#### Componentes principales:
- **AgregacionScheduler**: Ejecuta agregación periódica cada 2 minutos
- **ServicioAgregacion**: Core del proceso de agregación
- **IngestaOrquestadorService**: Orquesta la ingesta desde múltiples fuentes
- **DepuracionService**: Normalización y limpieza de datos
- **NormalizacionService**: Normalización de categorías y datos
- **FingerprintService**: Detección de duplicados
- **SincronizacionContribuyentesService**: Sincronización de usuarios
- **IdGeneratorService**: Generación de IDs únicos

#### Clients (solo lectura):
- **FuenteDinamicaClient**: Lee hechos ACEPTADOS de MongoDB
- **FuenteEstaticaClient**: Lee hechos procesados de CSVs

#### Base de datos:
- MySQL en localhost:3306/metamapa (compartida)

#### Flujo de agregación:
```
Scheduler cada 2 min
  → ServicioAgregacion.agregarTodasLasFuentes()
  → FuenteDinamicaClient.obtenerHechos() [solo ACEPTADOS]
  → FuenteEstaticaClient.obtenerHechos()
  → Normalización y depuración
  → Guardar en MySQL
  → Actualizar colecciones con algoritmos de consenso
```

---

### **🔵 GESTOR-SOLICITUDES** (Puerto 8080 - Todas las APIs HTTP)
**Responsabilidad:** Gestión de solicitudes, colecciones, contribuyentes y todas las APIs

**Ubicación:** `/gestor-solicitudes`

#### APIs expuestas:

**1. API Administrativa** (`/api/admin`) - Solo administradores
- `POST /colecciones` - Crear colección
- `PUT /colecciones/{id}` - Modificar colección
- `DELETE /colecciones/{id}` - Eliminar colección
- `GET /hechos/pendientes` - Listar hechos pendientes de aprobación
- `POST /hechos/{id}/aprobar` - Aprobar hecho
- `POST /hechos/{id}/aprobar-con-sugerencias` - Aprobar con sugerencias
- `POST /hechos/{id}/rechazar` - Rechazar hecho
- `POST /solicitudes/{id}/aprobar` - Aprobar solicitud de eliminación
- `POST /solicitudes/{id}/rechazar` - Rechazar solicitud de eliminación
- `POST /fuente-estatica/cargar` - Cargar CSV

**2. API Pública Interna** (`/api/interna`) - Frontend
- `GET /colecciones` - Listar colecciones
- `GET /colecciones/{id}/hechos` - Obtener hechos de colección
- `GET /hechos` - Listar todos los hechos
- `GET /hechos/{id}` - Obtener hecho específico
- `GET /hechos/search` - Buscar hechos
- `POST /hechos` - Reportar nuevo hecho (con/sin archivos)
- `POST /hechos/{id}/solicitudes` - Crear solicitud de eliminación
- `POST /contribuyentes` - Registrar contribuyente
- `GET /contribuyentes` - Listar contribuyentes
- `GET /contribuyentes/{id}` - Obtener contribuyente

**3. API Pública Externa** (`/api/publica`) - Federación
- `GET /colecciones` - Listar colecciones públicas
- `GET /colecciones/{id}/hechos` - Obtener hechos de colección
- `POST /hechos/{id}/solicitudes` - Crear solicitud externa

#### Services:
- **ColeccionService**: Gestión CRUD de colecciones
- **SolicitudService**: Gestión de solicitudes de eliminación
- **ContribuyenteService**: Gestión de usuarios
- **FuenteService**: Gestión de fuentes
- **DetectorDeSpam**: Validación de contenido

#### Clients:
- **FuenteDinamicaCrudClient**: Operaciones de escritura a fuente-dinámica
- **FuenteEstaticaClient**: Carga de CSV

#### Base de datos:
- MySQL en localhost:3306/metamapa (compartida con agregador)

---

## 🔄 Flujos principales

### 1. Usuario reporta hecho
```
Frontend
  → POST /api/interna/hechos
  → GestorSolicitudes.FuenteDinamicaCrudClient
  → Fuente-Dinámica MongoDB (estado: PENDIENTE)
```

### 2. Admin aprueba hecho
```
Frontend
  → POST /api/admin/hechos/{id}/aprobar
  → GestorSolicitudes.FuenteDinamicaCrudClient
  → Fuente-Dinámica MongoDB (actualiza a: ACEPTADO)
```

### 3. Agregador sincroniza (cada 2 minutos)
```
Agregador.Scheduler
  → Agregador.FuenteDinamicaClient.obtenerHechos()
  → Lee solo hechos ACEPTADOS
  → Normaliza y guarda en MySQL
```

### 4. Admin carga CSV
```
Frontend
  → POST /api/admin/fuente-estatica/cargar
  → GestorSolicitudes.FuenteEstaticaClient
  → Fuente-Estática MongoDB
  → Agregador lo tomará en próximo ciclo
```

### 5. Usuario se registra
```
Frontend
  → POST /api/interna/contribuyentes
  → GestorSolicitudes.ContribuyenteService
  → Guarda en MySQL
  → FuenteDinamicaCrudClient.registrarContribuyente()
  → Sincroniza con MongoDB
```

---

## 📊 Arquitectura de 3 capas

```
┌─────────────────────────────────────────────────┐
│           CAPA DE PRESENTACIÓN                  │
│   Frontend React (Puerto 5173)                  │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│         CAPA DE LÓGICA DE NEGOCIO               │
│                                                 │
│  ┌────────────────────┐  ┌──────────────────┐   │
│  │ Gestor-Solicitudes │  │   Agregador      │   │
│  │   Puerto 8080      │  │   Puerto 8081    │   │
│  │  (APIs HTTP)       │  │ (Background)     │   │
│  └────────────────────┘  └──────────────────┘   │
│           │                      │              │
│           │  Comparten MySQL     │              │
│           └──────────┬───────────┘              │
└──────────────────────┼──────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────┐
│            CAPA DE DATOS                        │
│                                                 │
│  MySQL (3306)          MongoDB Dinámico (27017) │
│  - Hechos agregados    - Hechos pendientes      │
│  - Colecciones         - Contribuyentes         │
│  - Solicitudes                                  │
│  - Contribuyentes      MongoDB Estático (27018) │
│                        - CSV procesados         │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Ejecución

### Iniciar Gestor de Solicitudes (APIs):
```bash
cd gestor-solicitudes
mvn spring-boot:run
# Escucha en http://localhost:8080
```

### Iniciar Agregador (Background):
```bash
cd agregador
mvn spring-boot:run
# Escucha en http://localhost:8081 (solo health checks)
# Ejecuta agregación cada 2 minutos
```

### Iniciar Fuentes:
```bash
# Fuente Dinámica
cd fuente-dinamica
mvn spring-boot:run  # Puerto 8082

# Fuente Estática
cd fuente-estatica
mvn spring-boot:run  # Puerto 8083
```

---

## 📝 Notas importantes

1. **Base de datos compartida**: Ambos módulos usan la misma base MySQL
2. **Sin duplicación de código**: Services y Repositories están solo donde se usan
3. **Separación clara**: Agregador NO tiene APIs HTTP, Gestor NO tiene lógica de agregación
4. **Detector de Spam**: Movido al gestor porque valida hechos reportados por usuarios
5. **Puerto 8080**: Se mantiene en gestor para no cambiar endpoints del frontend
