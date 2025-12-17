# Fuente Estática - Flujo de Procesamiento de CSV

## Arquitectura

La fuente estática funciona como un **microservicio independiente** que almacena archivos CSV en MongoDB y los procesa cuando el agregador lo solicita.

## Flujo Completo

### 1. Carga de archivo (Admin)

```http
POST http://localhost:8083/api/fuente-estatica/cargar
Content-Type: multipart/form-data

file: archivo.csv
```

**Proceso:**
1. Calcula SHA-256 del archivo
2. Verifica si ya existe (deduplicación)
3. Guarda archivo en MongoDB con estado `PENDIENTE`
4. Retorna confirmación inmediata

**Respuesta:**
```json
{
  "procesadas": 0,
  "insertadas": 0,
  "reemplazadas": 0,
  "salteadas": 0,
  "errores": [],
  "mensaje": "✅ Archivo cargado exitosamente. Será procesado por el agregador en el próximo ciclo."
}
```

### 2. Procesamiento automático (Agregador cada hora)

**Proceso:**
1. Agregador llama cada hora: `GET http://localhost:8083/api/fuente-estatica/hechos`
2. Fuente-estática busca archivos con estado `PENDIENTE`
3. Procesa cada CSV:
   - Lee líneas del archivo
   - Valida datos (coordenadas, fecha, campos requeridos)
   - Crea objetos `Hecho` con fuente `ESTATICA`
   - Marca archivo como `PROCESADO` o `FALLIDO`
4. Devuelve lista de hechos al agregador
5. Agregador aplica depuración (fingerprint: `titulo::categoria`)
6. Agregador guarda en MySQL central

## Estados del archivo

| Estado | Descripción |
|--------|-------------|
| `PENDIENTE` | Archivo subido, esperando procesamiento |
| `PROCESADO` | Archivo procesado exitosamente |
| `FALLIDO` | Error durante el procesamiento |

## Endpoints disponibles

### Para Admin (Gestor de Solicitudes)

- `POST /api/fuente-estatica/cargar` - Subir archivo CSV
- `GET /api/fuente-estatica/archivos` - Listar archivos subidos
- `GET /api/fuente-estatica/archivos/{id}` - Ver detalle de archivo
- `GET /api/fuente-estatica/hechos/cantidad` - Cantidad de archivos pendientes

### Para Agregador

- `GET /api/fuente-estatica/hechos` - Obtener hechos de archivos pendientes (gatilla procesamiento)
- `GET /api/fuente-estatica/tipo` - Obtener tipo de fuente
- `GET /api/fuente-estatica/identificador` - Obtener identificador

## Formato del CSV

```csv
titulo,descripcion,categoria,latitud,longitud,fecha
"Incendio forestal","Gran incendio en zona boscosa","Incendio",-34.6037,-58.3816,"2024-01-15"
```

**Campos requeridos:**
- `titulo` (String, no vacío)
- `descripcion` (String)
- `categoria` (String)
- `latitud` (Double, rango: -90 a 90)
- `longitud` (Double, rango: -180 a 180)
- `fecha` (String, formatos: yyyy-MM-dd, dd/MM/yyyy, etc.)

## Deduplicación

### Nivel 1: Archivo
- Usa SHA-256 del contenido
- Evita procesar el mismo archivo dos veces

### Nivel 2: Hechos (en el Agregador)
- Usa fingerprint: `titulo + "::" + categoria`
- Si existe, incrementa credibilidad y agrega fuente
- Si es nuevo, lo inserta con contador = 1

## Tecnologías

- **Storage:** MongoDB (archivos CSV como byte array)
- **API:** Spring Boot REST
- **Formato:** CSV con OpenCSV
- **Hash:** SHA-256 para deduplicación

## Ejemplo de uso completo

### 1. Admin sube CSV
```bash
curl -X POST http://localhost:8083/api/fuente-estatica/cargar \
  -F "file=@hechos_dataset.csv"
```

### 2. Verificar que está pendiente
```bash
curl http://localhost:8083/api/fuente-estatica/archivos
```

Respuesta:
```json
[
  {
    "id": "64a1b2c3d4e5f6g7h8i9j0k1",
    "nombreArchivo": "hechos_dataset.csv",
    "hash": "a3f2c1...",
    "fechaCarga": "2025-12-13T10:30:00",
    "estado": "PENDIENTE",
    "filasProcesadas": 0,
    "hechoInsertados": 0
  }
]
```

### 3. Agregador procesa (automático cada hora)
El agregador ejecuta internamente:
```java
fuenteEstaticaClient.obtenerHechos()
```

### 4. Verificar que fue procesado
```bash
curl http://localhost:8083/api/fuente-estatica/archivos
```

Respuesta:
```json
[
  {
    "id": "64a1b2c3d4e5f6g7h8i9j0k1",
    "nombreArchivo": "hechos_dataset.csv",
    "hash": "a3f2c1...",
    "fechaCarga": "2025-12-13T10:30:00",
    "estado": "PROCESADO",
    "filasProcesadas": 10000,
    "hechoInsertados": 9813,
    "filasSalteadas": 187,
    "errores": []
  }
]
```

### 5. Consultar hechos en MySQL central
```bash
curl http://localhost:8081/api/hechos
```

Los hechos ya están en la base central, depurados y listos para consulta.

## Configuración

### application.properties (fuente-estática)
```properties
server.port=8083
spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27018/fuenteEstatica
```

### application.properties (agregador)
```properties
fuente.estatica.url=http://localhost:8083
metamapa.agregacion.cron=0 0 * * * *  # Cada hora
```

## Ventajas del diseño

✅ **Separación de responsabilidades**: Fuente-estática solo almacena, agregador procesa  
✅ **Deduplicación en dos niveles**: Archivo (SHA-256) y Hecho (fingerprint)  
✅ **Resiliencia**: Si falla un CSV, no afecta a otros  
✅ **Trazabilidad**: Estado y estadísticas de cada archivo  
✅ **Escalabilidad**: Procesamiento en lotes cada hora  
✅ **Idempotencia**: Reprocesar el mismo archivo no duplica hechos
