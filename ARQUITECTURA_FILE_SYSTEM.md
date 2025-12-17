# Arquitectura: File System + MySQL (Refactorización)

## 📋 Resumen del Cambio

Se modificó la arquitectura de `fuente-estática` para cumplir con los requerimientos:

**Antes (Incorrecto):**
- CSV procesado inmediatamente al subir
- Hechos guardados en MongoDB (fuente-estática)
- No había estado PENDIENTE
- Agregador no controlaba cuándo procesar

**Ahora (Correcto):**
- CSV guardado en **file system** al subir → estado PENDIENTE
- Metadata en **MySQL** (tabla `archivo_dataset`)
- Agregador decide cuándo procesar (GET /hechos)
- Hechos solo en **MySQL central** (base del agregador)
- **Many-to-Many**: tabla `hecho_origen_archivo` para trazabilidad

---

## 🔄 Flujo Completo

### 1. Admin Sube CSV

**Endpoint:** `POST /api/fuente-estatica/cargar`

```bash
curl -F "file=@datos.csv" http://localhost:8083/api/fuente-estatica/cargar
```

**Qué pasa:**
1. Se calcula hash SHA-256 del archivo
2. Se verifica duplicación por hash
3. Se guarda archivo en `uploads/csv/{hash}.csv`
4. Se crea registro en MySQL:
   ```sql
   INSERT INTO archivo_dataset (
       nombre_archivo, hash, ruta_archivo, estado, fecha_carga
   ) VALUES (
       'datos.csv', 'abc123...', 'abc123abc123.csv', 'PENDIENTE', NOW()
   );
   ```

**Respuesta:**
```json
{
  "exitoso": true,
  "mensaje": "Archivo guardado exitosamente. Estado: PENDIENTE...",
  "procesadas": 0,
  "archivoId": 42
}
```

---

### 2. Agregador Procesa

**Endpoint (interno):** Agregador llama a `GET /api/fuente-estatica/hechos`

El agregador tiene un proceso programado (cada hora, por ejemplo) que:
1. Llama al endpoint GET /hechos de todas las fuentes
2. Recibe los hechos con `origenArchivoId` seteado
3. Los procesa con depuración y fingerprinting

**Qué pasa en fuente-estática:**
```java
public List<Hecho> procesarArchivosPendientes() {
    List<ArchivoDataset> pendientes = archivoRepository.findByEstado(PENDIENTE);
    
    for (ArchivoDataset archivo : pendientes) {
        // 1. Leer CSV desde disco
        byte[] contenido = fileStorageService.leerArchivo(archivo.getRutaArchivo());
        
        // 2. Parsear CSV → List<Hecho>
        List<Hecho> hechos = parsearCSV(contenido);
        
        // 3. CRÍTICO: Setear origenArchivoId en cada hecho
        hechos.forEach(h -> h.setOrigenArchivoId(archivo.getId()));
        
        // 4. Marcar archivo como PROCESADO
        archivo.setEstado(PROCESADO);
        archivoRepository.save(archivo);
    }
    
    return todosLosHechos;
}
```

---

### 3. Agregador Depura y Guarda

**En DepuracionService:**
```java
public List<Hecho> depurar(List<Hecho> normalizados) {
    for (Hecho h : normalizados) {
        String fp = fingerprintService.calcularFingerprint(h);
        h.setFingerprint(fp);
        
        Optional<Hecho> existente = hechoRepository.findByFingerprint(fp);
        
        if (existente.isPresent()) {
            // Hecho duplicado: incrementar contador, agregar fuente
            Hecho e = existente.get();
            e.incrementarContador();
            e.getFuentes().add(h.getFuente());
            hechoRepository.save(e);
            
            // CRÍTICO: Guardar relación N-N
            if (h.getOrigenArchivoId() != null) {
                guardarRelacionOrigenArchivo(e, h.getOrigenArchivoId());
            }
        } else {
            // Hecho nuevo: guardar
            h.setContador(1);
            Hecho guardado = hechoRepository.save(h);
            
            // CRÍTICO: Guardar relación N-N
            if (h.getOrigenArchivoId() != null) {
                guardarRelacionOrigenArchivo(guardado, h.getOrigenArchivoId());
            }
        }
    }
}

private void guardarRelacionOrigenArchivo(Hecho hecho, Long archivoId) {
    if (!hechoOrigenArchivoRepository.existsByHechoIdAndArchivoId(hecho.getId(), archivoId)) {
        HechoOrigenArchivo relacion = new HechoOrigenArchivo(hecho, archivoId);
        hechoOrigenArchivoRepository.save(relacion);
    }
}
```

**Resultado en BD:**
```sql
-- Tabla hechos (agregador)
INSERT INTO hecho (id, titulo, ..., fingerprint) VALUES (123, 'Incendio en...', ..., 'abc...');

-- Tabla intermedia (agregador)
INSERT INTO hecho_origen_archivo (hecho_id, archivo_id, fecha_vinculacion)
VALUES (123, 42, NOW());
```

---

## 🗄️ Esquema de Base de Datos

### fuente-estática (MySQL)

```sql
CREATE TABLE archivo_dataset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_archivo VARCHAR(255) NOT NULL,
    hash VARCHAR(64) UNIQUE NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    estado ENUM('PENDIENTE', 'PROCESADO', 'FALLIDO') NOT NULL,
    fecha_carga DATETIME NOT NULL,
    filas_procesadas INT,
    hecho_insertados INT,
    filas_salteadas INT
);

CREATE TABLE archivo_dataset_errores (
    archivo_id BIGINT,
    error VARCHAR(1000),
    FOREIGN KEY (archivo_id) REFERENCES archivo_dataset(id)
);
```

### agregador (MySQL)

```sql
CREATE TABLE hecho (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255),
    descripcion TEXT,
    categoria VARCHAR(100),
    fingerprint VARCHAR(255) UNIQUE,
    contador INT DEFAULT 1,
    ...
);

CREATE TABLE hecho_origen_archivo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hecho_id BIGINT NOT NULL,
    archivo_id BIGINT NOT NULL,  -- ID del archivo en fuente-estática
    fecha_vinculacion DATETIME NOT NULL,
    FOREIGN KEY (hecho_id) REFERENCES hecho(id),
    UNIQUE KEY unique_hecho_archivo (hecho_id, archivo_id)
);
```

---

## 📦 Many-to-Many: Relación Hecho ↔ Archivo

### ¿Por qué Many-to-Many?

**Escenario:**
1. Admin sube `archivo1.csv` con hecho "Incendio en X"
2. Agregador lo procesa → crea `hecho_123`
3. Admin sube `archivo2.csv` con el MISMO hecho
4. Agregador detecta duplicado (mismo fingerprint)
5. NO crea nuevo hecho, pero SÍ registra que `archivo2.csv` también lo reportó

**Resultado:**
```sql
SELECT * FROM hecho_origen_archivo WHERE hecho_id = 123;
-- hecho_id | archivo_id | fecha_vinculacion
-- 123      | 1          | 2025-01-15 10:00
-- 123      | 2          | 2025-01-16 14:30
```

**Consultas útiles:**
```sql
-- ¿De qué archivos proviene este hecho?
SELECT a.nombre_archivo, hoa.fecha_vinculacion
FROM hecho_origen_archivo hoa
JOIN archivo_dataset a ON a.id = hoa.archivo_id
WHERE hoa.hecho_id = 123;

-- ¿Qué hechos se crearon desde este archivo?
SELECT h.titulo, h.categoria
FROM hecho_origen_archivo hoa
JOIN hecho h ON h.id = hoa.hecho_id
WHERE hoa.archivo_id = 42;
```

---

## 🛠️ Componentes Clave

### 1. FileStorageService (fuente-estática)

**Responsabilidad:** Manejo de archivos en disco.

```java
@Service
public class FileStorageService {
    @Value("${file.storage.location:uploads/csv}")
    private String storageLocation;
    
    public String guardarArchivo(MultipartFile file, String hash) {
        String nombreArchivo = hash.substring(0, 12) + getExtension(file);
        Path rutaDestino = Paths.get(storageLocation, nombreArchivo);
        Files.copy(file.getInputStream(), rutaDestino, REPLACE_EXISTING);
        return nombreArchivo;
    }
    
    public byte[] leerArchivo(String rutaRelativa) {
        return Files.readAllBytes(Paths.get(storageLocation, rutaRelativa));
    }
}
```

### 2. FuenteEstaticaService (fuente-estática)

**Métodos principales:**
- `cargarHechos()`: Guarda archivo en disco + metadata PENDIENTE
- `procesarArchivosPendientes()`: Lee archivos PENDIENTES, parsea CSVs, retorna hechos con origenArchivoId

### 3. DepuracionService (agregador)

**Métodos principales:**
- `depurar()`: Depura duplicados por fingerprint, guarda hechos y relaciones N-N
- `guardarRelacionOrigenArchivo()`: Inserta en tabla intermedia

---

## 🧪 Testing Manual

### Test 1: Subir CSV

```bash
# 1. Subir archivo
curl -F "file=@test.csv" http://localhost:8083/api/fuente-estatica/cargar

# Verificar en BD:
SELECT * FROM archivo_dataset WHERE estado='PENDIENTE';

# Verificar en disco:
ls uploads/csv/
```

### Test 2: Procesar desde Agregador

```bash
# El agregador llamará automáticamente, pero puedes forzarlo:
curl http://localhost:8083/api/fuente-estatica/hechos

# Verificar estado cambió:
SELECT * FROM archivo_dataset WHERE estado='PROCESADO';

# Verificar hechos en agregador:
SELECT * FROM hecho WHERE titulo LIKE '%test%';
```

### Test 3: Duplicado

```bash
# Subir el MISMO archivo otra vez
curl -F "file=@test.csv" http://localhost:8083/api/fuente-estatica/cargar

# Respuesta esperada:
# "mensaje": "Archivo duplicado. Ya fue cargado el..."
```

### Test 4: Many-to-Many

```bash
# 1. Subir archivo1.csv con hecho "X"
# 2. Procesar → verifica que se cree hecho_id=123 y relación (123, 1)
# 3. Subir archivo2.csv con mismo hecho "X"
# 4. Procesar → verifica que NO se cree nuevo hecho, pero SÍ relación (123, 2)

SELECT * FROM hecho_origen_archivo WHERE hecho_id = 123;
-- Debe mostrar DOS filas
```

---

## 🔧 Configuración

### fuente-estática/application.properties

```properties
server.port=8083

# MySQL (no más MongoDB)
spring.datasource.url=jdbc:mysql://localhost:3306/utndds
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update

# File Storage
file.storage.location=uploads/csv
```

---

## 📝 Notas Importantes

### Estados del Archivo

- **PENDIENTE**: Subido pero no procesado
- **PROCESADO**: Procesado exitosamente
- **FALLIDO**: Error durante procesamiento (ver tabla `archivo_dataset_errores`)

### Detección de Duplicados

- **Nivel 1 (Upload)**: Hash SHA-256 del archivo → evita subir mismo archivo dos veces
- **Nivel 2 (Procesamiento)**: Fingerprint del hecho → evita duplicar hechos en BD central

### Campo `origenArchivoId`

**En entidad Hecho:**
```java
@Transient
private Long origenArchivoId;
```

- Es `@Transient` → NO se persiste en BD
- Solo se usa durante el procesamiento para crear la relación N-N
- La fuente-estática lo setea, el agregador lo lee y crea la fila en `hecho_origen_archivo`

---

## 🚀 Próximos Pasos

1. ✅ **Compilar todos los módulos**
   ```bash
   mvn clean install
   ```

2. ✅ **Iniciar servicios**
   ```bash
   # Terminal 1: Agregador
   cd agregador && mvn spring-boot:run
   
   # Terminal 2: Fuente-estática
   cd fuente-estatica && mvn spring-boot:run
   ```

3. ✅ **Probar flujo completo**
   - Subir CSV
   - Verificar archivo PENDIENTE en BD
   - Verificar archivo físico en `uploads/csv/`
   - Llamar endpoint /hechos (agregador lo hará automáticamente)
   - Verificar estado PROCESADO
   - Verificar hechos en BD central
   - Verificar relaciones en `hecho_origen_archivo`

4. **Monitorear logs**
   - Fuente-estática: "Archivo guardado exitosamente. Estado: PENDIENTE"
   - Agregador: "🔗 Relación guardada: Hecho X ← Archivo Y"

---

## 🐛 Troubleshooting

### Error: "Cannot create directory"
```bash
mkdir -p uploads/csv
chmod 755 uploads/csv
```

### Error: "File not found" al procesar
- Verificar que `file.storage.location` esté configurado
- Verificar permisos de lectura en `uploads/csv/`

### Error: "Duplicate entry for key 'fingerprint'"
- Es esperado: significa que el agregador detectó correctamente el duplicado
- Verifica que se haya creado la relación N-N en `hecho_origen_archivo`
