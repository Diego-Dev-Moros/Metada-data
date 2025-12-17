# 📦 Instalación de Prerrequisitos - MetaMapa Auth

## ⚠️ Prerrequisitos Necesarios

Para ejecutar el sistema necesitas:

- ☑️ **Java 11 o superior** (para backend Spring Boot)
- ☑️ **Maven** (para compilar proyectos Java)
- ☑️ **Node.js 16+** (para frontend React)

---

## 1️⃣ Instalar Java JDK 11

### Opción A: Descargar desde Oracle (Recomendado)

1. **Ir a:** https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
2. **Descargar:** `Windows x64 Installer` (archivo .exe)
3. **Instalar:**

   - Ejecutar el instalador
   - Siguiente → Siguiente → Instalar
   - Ubicación por defecto: `C:\Program Files\Java\jdk-11.x.x`
4. **Configurar Variables de Entorno:**

   **Abrir PowerShell como Administrador y ejecutar:**

   ```powershell
   # Establecer JAVA_HOME (ajustar versión si es necesaria)
   [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-11", "Machine")

   # Agregar Java al PATH
   $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
   [Environment]::SetEnvironmentVariable("Path", "$currentPath;%JAVA_HOME%\bin", "Machine")
   ```
5. **Cerrar y reabrir PowerShell**
6. **Verificar instalación:**

   ```powershell
   java -version
   ```

   Deberías ver algo como:

   ```
   java version "11.0.x"
   Java(TM) SE Runtime Environment
   ```

### Opción B: Instalar con Chocolatey (Más Rápido)

Si tienes Chocolatey instalado:

```powershell
choco install openjdk11
```

---

## 2️⃣ Instalar Maven

### Opción A: Descargar Manualmente

1. **Ir a:** https://maven.apache.org/download.cgi
2. **Descargar:** `apache-maven-3.9.x-bin.zip`
3. **Extraer a:** `C:\Program Files\Apache\maven`
4. **Configurar Variables de Entorno:**

   **PowerShell como Administrador:**

   ```powershell
   # Establecer MAVEN_HOME
   [Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Apache\maven", "Machine")

   # Agregar Maven al PATH
   $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
   [Environment]::SetEnvironmentVariable("Path", "$currentPath;%MAVEN_HOME%\bin", "Machine")
   ```
5. **Cerrar y reabrir PowerShell**
6. **Verificar:**

   ```powershell
   mvn -version
   ```

### Opción B: Con Chocolatey

```powershell
choco install maven
```

---

## 3️⃣ Instalar Node.js

### Opción A: Instalador Oficial (Recomendado)

1. **Ir a:** https://nodejs.org/
2. **Descargar:** Versión LTS (Long Term Support)

   - Para Windows: `node-vXX.x.x-x64.msi`
3. **Instalar:**

   - Ejecutar el instalador
   - ✅ Marcar "Automatically install the necessary tools"
   - Siguiente → Siguiente → Instalar
4. **Cerrar y reabrir PowerShell**
5. **Verificar:**

   ```powershell
   node --version
   npm --version
   ```

   Deberías ver:

   ```
   v20.x.x  (o similar)
   10.x.x   (o similar)
   ```

### Opción B: Con Chocolatey

```powershell
choco install nodejs-lts
```

---

## 🚀 Instalación Rápida con Chocolatey (TODO EN UNO)

Si no tienes Chocolatey, instalarlo primero:

**1. Instalar Chocolatey:**

PowerShell como Administrador:

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

**2. Cerrar y reabrir PowerShell como Administrador**

**3. Instalar todo:**

```powershell
choco install openjdk11 maven nodejs-lts -y
```

**4. Cerrar y reabrir PowerShell normal**

**5. Verificar todo:**

```powershell
java -version
mvn -version
node --version
npm --version
```

---

## ✅ Verificación Final

**Ejecuta este script para verificar que todo está instalado:**

```powershell
Write-Host "=== Verificando Instalaciones ===" -ForegroundColor Cyan

# Java
Write-Host "`nJava:" -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java instalado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java NO instalado" -ForegroundColor Red
}

# Maven
Write-Host "`nMaven:" -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "✅ Maven instalado: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven NO instalado" -ForegroundColor Red
}

# Node.js
Write-Host "`nNode.js:" -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js instalado: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js NO instalado" -ForegroundColor Red
}

# npm
Write-Host "`nnpm:" -ForegroundColor Yellow
try {
    $npmVersion = npm --version
    Write-Host "✅ npm instalado: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ npm NO instalado" -ForegroundColor Red
}

Write-Host "`n=== Verificación Completa ===" -ForegroundColor Cyan
```

---

## 📋 Después de Instalar

Una vez que tengas todo instalado:

1. **Cerrar TODOS los terminales de PowerShell**
2. **Abrir un nuevo PowerShell**
3. **Continuar con la guía de prueba:**
   - Ver: `docs/GUIA-PRUEBA-RAPIDA.md`

---

## 🐛 Problemas Comunes

### "java no se reconoce como comando"

**Solución:**

1. Verificar que JAVA_HOME esté configurado:

   ```powershell
   $env:JAVA_HOME
   ```

   Debería mostrar: `C:\Program Files\Java\jdk-11.x.x`
2. Si está vacío, configurar manualmente:

   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-11.0.xx"
   $env:Path += ";$env:JAVA_HOME\bin"
   ```

### "mvn no se reconoce como comando"

**Solución:** Similar a Java, verificar MAVEN_HOME y PATH

### PowerShell requiere permisos de administrador

**Solución:**

1. Click derecho en PowerShell
2. "Ejecutar como administrador"

### Error: "running scripts is disabled"

**Solución:**

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## ⏱️ Tiempo Estimado de Instalación

- **Con instaladores manuales:** 20-30 minutos
- **Con Chocolatey:** 10-15 minutos

---

## 📞 Siguiente Paso

Después de instalar todo y verificar que funciona:

➡️ **Continuar con:** [GUIA-PRUEBA-RAPIDA.md](GUIA-PRUEBA-RAPIDA.md)
