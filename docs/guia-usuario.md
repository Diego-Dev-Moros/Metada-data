# Guía de Usuario - MetaMapa

## Índice
1. [Introducción](#introducción)
2. [Registro e Inicio de Sesión](#registro-e-inicio-de-sesión)
3. [Guía para Usuarios (Visualizador)](#guía-para-usuarios-visualizador)
4. [Guía para Contribuyentes](#guía-para-contribuyentes)
5. [Guía para Administradores](#guía-para-administradores)
6. [Preguntas Frecuentes](#preguntas-frecuentes)

---

## Introducción

**MetaMapa** es una plataforma colaborativa para visualizar, reportar y gestionar eventos históricos y de interés público en Argentina. La plataforma cuenta con tres tipos de usuarios, cada uno con diferentes niveles de acceso y permisos:

### Tipos de Usuarios

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| **👁️ Usuario (Visualizador)** | Usuario básico registrado | Ver mapa, buscar eventos, solicitar eliminaciones |
| **✍️ Contribuyente** | Usuario con permisos de creación | Todo lo de Usuario + Crear y editar eventos propios |
| **🛡️ Administrador** | Gestor de la plataforma | Acceso completo: moderar, aprobar, gestionar todo |

---

## Registro e Inicio de Sesión

### 1. Acceder a la Plataforma

Visita: `http://localhost:5173` (desarrollo) o la URL de producción.

### 2. Opciones de Inicio de Sesión

MetaMapa utiliza **Auth0** para autenticación segura. Tienes dos opciones:

#### Opción A: Crear Cuenta con Email y Contraseña

1. Haz clic en **"Iniciar Sesión"** en la barra superior
2. Haz clic en **"Sign up"** (Registrarse)
3. Completa el formulario:
   - **Email:** tu_email@example.com
   - **Contraseña:** Mínimo 8 caracteres, con mayúsculas, minúsculas y números
4. Haz clic en **"Continue"**
5. Revisa tu email y verifica tu cuenta (haz clic en el link de confirmación)

#### Opción B: Continuar con Google

1. Haz clic en **"Iniciar Sesión"**
2. Haz clic en **"Continue with Google"**
3. Selecciona tu cuenta de Google
4. Autoriza el acceso a MetaMapa

**⚠️ IMPORTANTE:** Si ya creaste una cuenta con email/contraseña y luego usas "Continue with Google" con el mismo email, Auth0 creará **dos cuentas separadas**. Para unificarlas, contacta al administrador.

### 3. Primer Inicio de Sesión

Al iniciar sesión por primera vez:

1. Serás redirigido a la página principal
2. Tu rol predeterminado será **Usuario (Visualizador)**
3. Para obtener permisos de **Contribuyente** o **Administrador**, contacta al administrador del sistema

### 4. Verificar Tu Rol Actual

Una vez autenticado, puedes ver tu rol en la esquina superior derecha de la barra de navegación:

- 🟢 **Badge verde:** Usuario
- 🟡 **Badge amarillo:** Contribuyente
- 🔴 **Badge rojo:** Administrador

---

## Guía para Usuarios (Visualizador)

Como **Usuario**, puedes explorar la plataforma y solicitar cambios en los contenidos.

### 📍 Ver el Mapa de Eventos

#### Navegación Básica

1. En la página principal, verás un mapa interactivo de Argentina
2. Cada **marcador** representa un evento:
   - 🟢 **Verde:** Evento de bajo impacto
   - 🟠 **Naranja:** Evento de impacto medio
   - 🔴 **Rojo:** Evento de alto impacto
3. Haz clic en un marcador para ver información básica en un popup
4. Haz clic en **"Ver detalle"** para ver información completa

#### Usar Filtros de Búsqueda

En la parte superior del mapa encontrarás los **Filtros Rápidos**:

**Filtrar por Categoría:**
```
📂 Categoría: [Selecciona una opción ▼]
```
Opciones: Inundación, Incendio, Terremoto, Contaminación, etc.

**Filtrar por Rango de Fechas:**
```
📅 Desde: [____/__/____]
📅 Hasta: [____/__/____]
```
Ejemplo: Ver eventos entre 2020 y 2024

**Filtrar por Ubicación:**
```
📍 Ubicación: [Escribe provincia, ciudad o municipio]
```
Ejemplo: "Córdoba", "Santa Fe", "Buenos Aires"

**Aplicar Filtros:**
1. Selecciona los criterios deseados
2. Haz clic en **"Aplicar Filtros"**
3. El mapa se actualizará mostrando solo los eventos que cumplen los criterios
4. Haz clic en **"Limpiar Filtros"** para volver a ver todos los eventos

### 📚 Explorar Colecciones

Las colecciones son agrupaciones temáticas de eventos.

1. Haz clic en **"Colecciones"** en la barra de navegación
2. Verás una lista de colecciones disponibles:
   - **Desastres Naturales en Argentina**
   - **Contaminación Ambiental**
   - **Accidentes Tecnológicos**
   - etc.
3. Haz clic en una colección para ver:
   - Descripción detallada
   - Cantidad de eventos
   - Mapa específico de esa colección

### 🔍 Ver Detalle de un Evento

1. Haz clic en un marcador del mapa
2. En el popup, haz clic en **"Ver detalle"**
3. Verás información completa:
   - **Título** del evento
   - **Descripción** detallada
   - **Categoría** (tipo de evento)
   - **Fecha** del evento
   - **Ubicación exacta** (país, provincia, municipio)
   - **Coordenadas** geográficas
   - **Etiquetas** relacionadas
   - **Fuente** de información (URL externa si disponible)
   - **Fecha de carga** en la plataforma
   - **Última modificación**

### 🚫 Solicitar Eliminación de un Evento

Si encuentras un evento con información incorrecta o inapropiada, puedes solicitar su eliminación:

1. Ve al detalle del evento (ver sección anterior)
2. Haz clic en el botón **"Solicitar Eliminación"**
3. Completa el formulario:
   ```
   📝 Justificación: [Explica por qué debería eliminarse]
   ```
   Ejemplo: "El evento tiene fecha incorrecta. La inundación ocurrió en 2004, no en 2003."
4. Haz clic en **"Enviar Solicitud"**
5. Un administrador revisará tu solicitud

**Estado de tu solicitud:**
- ⏳ **Pendiente:** Esperando revisión
- ✅ **Aprobada:** El evento será eliminado
- ❌ **Rechazada:** El evento permanecerá

### 📊 Ver Estadísticas

1. Haz clic en **"Estadísticas"** en la barra de navegación
2. Verás gráficos y datos sobre:
   - Cantidad de eventos por categoría
   - Eventos por provincia
   - Eventos por año
   - Colecciones más visitadas

### 👤 Ver Tu Perfil

1. Haz clic en tu nombre o avatar en la esquina superior derecha
2. Selecciona **"Mi Perfil"**
3. Verás:
   - Tu nombre y email
   - Estado de verificación de email
   - Tu rol actual
   - Tu User ID de Auth0
   - Fecha de última actualización

---

## Guía para Contribuyentes

Como **Contribuyente**, tienes todos los permisos de Usuario más la capacidad de **crear y editar eventos**.

### ➕ Crear un Nuevo Evento

#### Acceder al Formulario

1. Inicia sesión con tu cuenta de Contribuyente
2. Haz clic en **"Crear Hecho"** en la barra de navegación
3. Serás llevado al formulario de creación

#### Completar el Formulario

**📝 Información Básica:**

```
Título: [Escribe un título descriptivo]
Ejemplo: "Inundación en Resistencia, Chaco - Mayo 2023"

Descripción: [Describe el evento con detalle]
Ejemplo: "Gran inundación causada por el desborde del río Paraná. 
Afectó a más de 15,000 personas y causó evacuaciones masivas..."

Categoría: [Selecciona una opción ▼]
Opciones: Inundación, Incendio, Terremoto, Contaminación, etc.
```

**📅 Fecha del Evento:**

```
Fecha del Hecho: [DD/MM/AAAA HH:MM]
Ejemplo: 15/05/2023 14:30
```

**📍 Ubicación del Evento:**

```
Latitud: [-34.603722]
Longitud: [-58.381592]

País: [Argentina]
Provincia: [Chaco]
Municipio: [Resistencia]
```

💡 **Tip:** Para obtener coordenadas exactas:
- Usa Google Maps → Clic derecho en el lugar → "¿Qué hay aquí?"
- O usa herramientas como: https://www.latlong.net/

**🏷️ Etiquetas:**

```
Etiquetas: [inundación, desastre, clima, paraná]
(Separadas por comas)
```

**🔗 Fuente de Información:**

```
URL de la Fuente: [https://ejemplo.com/noticia]
Ejemplo: Link a artículo periodístico, informe oficial, etc.
```

**👁️ Visibilidad:**

```
☑️ Es público (visible para todos los usuarios)
```

#### Enviar el Evento

1. Revisa toda la información
2. Haz clic en **"Crear Evento"**
3. El evento será enviado para **revisión** por un administrador
4. Recibirás una notificación cuando sea aprobado

**Estados del evento:**
- ⏳ **Pendiente:** Esperando aprobación del administrador
- ✅ **Aprobado:** Visible en el mapa público
- ❌ **Rechazado:** No será publicado (recibirás el motivo)
- 📝 **Cambios solicitados:** El admin sugiere mejoras antes de aprobar

### ✏️ Editar Tus Eventos

Como Contribuyente, puedes editar tus propios eventos **durante los primeros 7 días** después de su creación.

#### Proceso de Edición

1. Ve al detalle del evento que creaste
2. Si puedes editarlo, verás un botón **"Editar Evento"**
3. Modifica los campos necesarios:
   - Título
   - Descripción
   - Categoría
   - Ubicación
   - etc.
4. Haz clic en **"Guardar Cambios"**

#### Restricción de 7 Días

**¿Por qué existe esta restricción?**
Para mantener la integridad histórica de los datos. Después de 7 días, solo los administradores pueden modificar eventos.

**¿Qué hacer si necesitas editar después de 7 días?**
1. Solicita la eliminación del evento (con justificación)
2. Crea un nuevo evento con la información correcta
3. O contacta a un administrador para que haga los cambios

### 🔍 Verificar Si Puedes Editar un Evento

1. Ve al detalle del evento
2. El sistema mostrará uno de estos mensajes:
   - ✅ **"Puedes editar este evento"** (botón verde)
   - ⏰ **"Período de edición expirado (7 días)"** (badge gris)
   - 🚫 **"No puedes editar eventos de otros usuarios"** (badge rojo)

### 📊 Ver Tus Contribuciones

1. Ve a **"Mi Perfil"**
2. En la versión completa del perfil (`/perfil-completo`), verás:
   - Cantidad total de eventos creados
   - Lista de tus eventos
   - Estado de cada evento (pendiente, aprobado, rechazado)
   - Cantidad de solicitudes enviadas

---

## Guía para Administradores

Como **Administrador**, tienes **control completo** de la plataforma. Puedes moderar contenido, gestionar usuarios y administrar el sistema.

### 🎛️ Acceder al Panel de Administración

1. Inicia sesión con tu cuenta de Administrador
2. En la barra de navegación verás un dropdown **"Panel Admin"** (🔴 rojo)
3. Haz clic para ver las opciones disponibles

### 📋 Menú del Panel Admin

```
Panel Admin ▼
├── 📊 Dashboard
├── 📝 Hechos Pendientes
├── 🗑️ Solicitudes de Eliminación
├── 📚 Gestionar Colecciones
├── 👥 Gestionar Usuarios
└── 📊 Reportes y Estadísticas
```

---

### 📝 Moderar Hechos Pendientes

Los eventos creados por Contribuyentes necesitan tu aprobación.

#### Ver Lista de Hechos Pendientes

1. Panel Admin → **"Hechos Pendientes"**
2. Verás una lista con:
   - Título del evento
   - Autor (email del Contribuyente)
   - Fecha de creación
   - Categoría
   - Estado actual

#### Revisar un Hecho

1. Haz clic en un hecho pendiente
2. Revisa toda la información:
   - ¿El título es descriptivo?
   - ¿La descripción es clara y precisa?
   - ¿Las coordenadas son correctas?
   - ¿La categoría es apropiada?
   - ¿La fecha es precisa?
   - ¿La fuente es confiable?

#### Aprobar un Hecho

Si el evento es correcto:

1. Haz clic en **"✅ Aprobar"**
2. El evento se publicará automáticamente en el mapa
3. El autor recibirá una notificación

#### Aprobar con Sugerencias

Si el evento es bueno pero podría mejorarse:

1. Haz clic en **"✅ Aprobar con Sugerencias"**
2. Escribe tus comentarios:
   ```
   Sugerencias: [El evento es válido, pero sería útil agregar 
   más detalles sobre el impacto económico y las víctimas]
   ```
3. El evento se publicará
4. El autor verá tus sugerencias para eventos futuros

#### Solicitar Cambios

Si el evento necesita correcciones antes de publicarse:

1. Haz clic en **"📝 Solicitar Cambios"**
2. Especifica qué debe corregirse:
   ```
   Cambios requeridos: 
   - Las coordenadas no corresponden a la ubicación mencionada
   - Falta especificar la fecha exacta del evento
   - La fuente proporcionada no es confiable
   ```
3. El evento volverá al autor con estado "Cambios solicitados"
4. El autor podrá editar y reenviar

#### Rechazar un Hecho

Si el evento no es apropiado para la plataforma:

1. Haz clic en **"❌ Rechazar"**
2. Proporciona un motivo claro:
   ```
   Motivo del rechazo: 
   - La información no es verificable
   - No corresponde a un evento real
   - Contenido inapropiado o fuera de tema
   ```
3. El evento no se publicará
4. El autor recibirá el motivo del rechazo

---

### 🗑️ Gestionar Solicitudes de Eliminación

Los usuarios pueden solicitar eliminar eventos si encuentran errores o contenido inapropiado.

#### Ver Solicitudes Pendientes

1. Panel Admin → **"Solicitudes de Eliminación"**
2. Verás una lista con:
   - Evento afectado (título)
   - Usuario solicitante
   - Justificación
   - Fecha de solicitud

#### Revisar una Solicitud

1. Haz clic en una solicitud
2. Lee la justificación del usuario
3. Ve al detalle del evento para evaluarlo
4. Verifica:
   - ¿Es válida la justificación?
   - ¿El evento realmente tiene errores?
   - ¿Es información falsa o inapropiada?

#### Aprobar Solicitud

Si la solicitud es válida:

1. Haz clic en **"✅ Aprobar Eliminación"**
2. El evento será **marcado como eliminado** (no se borra físicamente)
3. El evento desaparecerá del mapa público
4. El solicitante recibirá una notificación

#### Rechazar Solicitud

Si la solicitud no es válida:

1. Haz clic en **"❌ Rechazar Solicitud"**
2. Opcionalmente, agrega un motivo:
   ```
   Motivo del rechazo: 
   La información del evento es correcta y está respaldada por 
   fuentes oficiales. No se justifica su eliminación.
   ```
3. El evento permanecerá en la plataforma
4. El solicitante recibirá el motivo

---

### 📚 Gestionar Colecciones

Las colecciones son agrupaciones temáticas de eventos.

#### Crear una Nueva Colección

1. Panel Admin → **"Gestionar Colecciones"** → **"Nueva Colección"**
2. Completa el formulario:

```
Título: [Desastres Tecnológicos en Argentina]

Descripción: [Recopilación de accidentes industriales, 
derrames químicos y fallas tecnológicas]

Categoría: [Tecnología]

Algoritmo de Consenso: [Consenso por Mayoría ▼]
Opciones:
- Consenso Simple
- Consenso por Mayoría
- Consenso Total

Métodos de Navegación: 
☑️ Irrestricta (todos los eventos)
☑️ Restringida (solo eventos verificados)
```

3. Haz clic en **"Crear Colección"**

#### Agregar Eventos a una Colección

**Método 1: Al crear un evento**
1. Al aprobar un evento, selecciona las colecciones donde incluirlo
2. Marca las colecciones relevantes

**Método 2: Editar colección**
1. Ve a la colección
2. Haz clic en **"Agregar Eventos"**
3. Busca y selecciona eventos
4. Haz clic en **"Guardar"**

#### Eliminar una Colección

1. Ve a **"Gestionar Colecciones"**
2. Selecciona la colección a eliminar
3. Haz clic en **"🗑️ Eliminar Colección"**
4. Confirma la acción

⚠️ **Precaución:** Eliminar una colección no elimina los eventos, solo la agrupación.

---

### 👥 Gestionar Usuarios

#### Ver Lista de Usuarios

1. Panel Admin → **"Gestionar Usuarios"**
2. Verás todos los usuarios registrados con:
   - Nombre
   - Email
   - Rol actual
   - Fecha de registro
   - Última actividad

#### Cambiar Rol de un Usuario

**Desde la plataforma (si está implementado):**
1. Selecciona el usuario
2. Haz clic en **"Cambiar Rol"**
3. Selecciona el nuevo rol
4. Guarda cambios

**Desde Auth0 (método recomendado):**
1. Ve a Auth0 Dashboard: https://manage.auth0.com
2. User Management → Users
3. Busca el usuario por email
4. Haz clic en el usuario
5. Ve a la pestaña **"Roles"**
6. Haz clic en **"Assign Roles"**
7. Selecciona el rol deseado:
   - **USER** (Visualizador)
   - **CONTRIBUTOR** (Contribuyente)
   - **ADMIN** (Administrador)
8. Guarda
9. El usuario debe cerrar sesión y volver a iniciar para ver los cambios

#### Eliminar/Bloquear un Usuario

**En Auth0:**
1. Ve al usuario en Auth0 Dashboard
2. Opciones:
   - **Block User:** El usuario no podrá iniciar sesión
   - **Delete User:** Elimina permanentemente la cuenta

---

### ✏️ Editar Cualquier Evento (Sin Restricciones)

Como Administrador, puedes editar **cualquier evento en cualquier momento**, sin la restricción de 7 días.

#### Editar un Evento

1. Ve al detalle del evento
2. Haz clic en **"✏️ Editar Evento"** (disponible para ti siempre)
3. Modifica los campos necesarios
4. Haz clic en **"Guardar Cambios"**

#### Eliminar un Evento

**Eliminación lógica (recomendado):**
1. Ve al detalle del evento
2. Haz clic en **"🗑️ Marcar como Eliminado"**
3. El evento desaparecerá del mapa pero permanecerá en la base de datos

**Eliminación física (NO recomendado):**
- Solo a través de la base de datos directamente
- ⚠️ **Precaución:** Esta acción es irreversible

---

### 📊 Importar Datasets Masivos

Si tienes un archivo CSV con múltiples eventos, puedes importarlos masivamente.

#### Preparar el Archivo CSV

El archivo debe tener este formato:

```csv
titulo,descripcion,categoria,fechaHecho,latitud,longitud,pais,provincia,municipio,etiquetas,fuenteUrl
"Inundación en Rosario","Gran inundación...","Inundacion","2020-05-15",-32.9442,-60.6505,"Argentina","Santa Fe","Rosario","inundacion,desastre","https://ejemplo.com"
"Incendio en Córdoba","Incendio forestal...","Incendio","2021-09-10",-31.4201,-64.1888,"Argentina","Córdoba","Villa Carlos Paz","incendio,forestal","https://ejemplo.com"
```

**Columnas requeridas:**
- titulo
- descripcion
- categoria
- fechaHecho (formato: YYYY-MM-DD)
- latitud (decimal)
- longitud (decimal)
- pais
- provincia
- municipio

**Columnas opcionales:**
- etiquetas (separadas por comas)
- fuenteUrl

#### Importar el Archivo

1. Panel Admin → **"Importar Dataset"**
2. Haz clic en **"Seleccionar Archivo"**
3. Elige tu archivo CSV
4. Selecciona el tipo de fuente:
   - **ESTATICA:** Datos históricos confiables
   - **DINAMICA:** Datos temporales que requieren revisión
5. Haz clic en **"Importar"**
6. Espera a que se procese (puede tardar según el tamaño)
7. Verás un resumen:
   ```
   ✅ 234 eventos importados exitosamente
   ⚠️ 12 eventos con advertencias
   ❌ 3 eventos con errores
   ```
8. Revisa los eventos con errores y corrígelos manualmente

---

### 📈 Ver Reportes y Estadísticas

#### Dashboard Administrativo

1. Panel Admin → **"Dashboard"**
2. Verás métricas clave:
   - Total de eventos en el sistema
   - Eventos pendientes de aprobación
   - Solicitudes de eliminación pendientes
   - Usuarios registrados
   - Usuarios activos (última semana)
   - Eventos por categoría (gráfico)
   - Eventos por provincia (mapa de calor)

#### Reportes Detallados

1. Panel Admin → **"Reportes y Estadísticas"**
2. Selecciona el tipo de reporte:
   - **Por Período:** Eventos creados en un rango de fechas
   - **Por Contribuyente:** Eventos de un usuario específico
   - **Por Categoría:** Distribución por tipo de evento
   - **Por Ubicación:** Eventos por región
3. Configura los filtros
4. Haz clic en **"Generar Reporte"**
5. Opciones de exportación:
   - 📊 Ver en pantalla
   - 📄 Descargar PDF
   - 📁 Descargar CSV
   - 📧 Enviar por email

---

## Preguntas Frecuentes

### 🤔 Para Todos los Usuarios

**P: ¿Es necesario registrarse para ver el mapa?**
R: No, puedes ver el mapa sin registrarte usando la API pública (`/api/public/metamapa`). Sin embargo, el frontend requiere login para acceder a filtros avanzados y funcionalidades completas.

**P: ¿Cómo puedo cambiar mi contraseña?**
R: 
1. Cierra sesión
2. En la pantalla de login, haz clic en "¿Olvidaste tu contraseña?"
3. Ingresa tu email
4. Revisa tu correo y sigue las instrucciones

**P: ¿Puedo usar Google y también email/contraseña?**
R: Sí, pero Auth0 creará dos cuentas separadas aunque uses el mismo email. Es mejor usar siempre el mismo método de inicio de sesión.

**P: ¿Cómo unifico mis dos cuentas (Google y email)?**
R: Contacta a un administrador. Ellos pueden vincular tus cuentas usando la función "Account Linking" de Auth0.

**P: ¿Por qué mi email no está verificado?**
R: Auth0 envió un email de verificación cuando te registraste. Revisa tu bandeja de entrada y spam. Si no lo encuentras, contacta a soporte.

---

### ✍️ Para Contribuyentes

**P: ¿Cuántos eventos puedo crear?**
R: No hay límite. Puedes crear tantos eventos como desees, pero cada uno será revisado por un administrador.

**P: ¿Cuánto tarda en aprobarse mi evento?**
R: Depende de la disponibilidad de los administradores. Generalmente entre 24-48 horas.

**P: ¿Por qué mi evento fue rechazado?**
R: Revisa el motivo proporcionado por el administrador. Razones comunes:
- Información no verificable
- Coordenadas incorrectas
- Falta de fuentes confiables
- Contenido inapropiado o fuera de tema

**P: ¿Puedo editar un evento después de 7 días?**
R: No directamente. Opciones:
1. Solicita su eliminación y crea uno nuevo con la información correcta
2. Contacta a un administrador para que haga las correcciones

**P: ¿Qué pasa si cometo un error al crear un evento?**
R: Si aún está en estado "Pendiente", contacta rápidamente a un administrador. Si ya fue aprobado y aún estás dentro de los 7 días, puedes editarlo tú mismo.

**P: ¿Puedo eliminar mis propios eventos?**
R: No directamente. Debes crear una "Solicitud de Eliminación" con justificación, que será revisada por un administrador.

---

### 🛡️ Para Administradores

**P: ¿Cómo asigno roles de Contribuyente a usuarios?**
R: Ve a Auth0 Dashboard → User Management → Users → Selecciona usuario → Roles → Assign Roles → CONTRIBUTOR

**P: ¿Qué hago si un usuario reporta contenido inapropiado?**
R:
1. Revisa el evento reportado
2. Si es inapropiado:
   - Aprueba la solicitud de eliminación
   - Considera bloquear al creador si es reincidente
3. Si no es inapropiado:
   - Rechaza la solicitud con explicación

**P: ¿Cómo restauro un evento eliminado?**
R: Los eventos no se borran físicamente. Puedes restaurarlos:
1. Conectándote a la base de datos MySQL
2. Ejecutando: `UPDATE hechos SET eliminado = false WHERE id = <ID_DEL_HECHO>`

**P: ¿Puedo exportar todos los eventos?**
R: Sí, desde el panel de reportes:
1. Panel Admin → Reportes
2. Selecciona "Todos los eventos"
3. Haz clic en "Descargar CSV"

**P: ¿Cómo elimino un usuario permanentemente?**
R: En Auth0 Dashboard → User Management → Users → Selecciona usuario → Delete User. **Precaución:** Esta acción es irreversible.

**P: ¿Qué significa el "Algoritmo de Consenso" en las colecciones?**
R: Define cómo se validan eventos en la colección:
- **Consenso Simple:** Basta con la aprobación de un admin
- **Consenso por Mayoría:** Requiere aprobación de la mayoría de admins activos
- **Consenso Total:** Requiere aprobación unánime de todos los admins

**P: ¿Cómo hago backup de la base de datos?**
R: 
```bash
# MySQL backup
mysqldump -u root -p utndds > backup_$(date +%Y%m%d).sql

# MongoDB backup
mongodump --db metamapa --out backup_$(date +%Y%m%d)
```

---

## Contacto y Soporte

### 📧 Soporte Técnico
- **Email:** soporte@metamapa.com
- **Horario:** Lunes a Viernes, 9:00 - 18:00 (GMT-3)

### 🐛 Reportar Bugs
- **GitHub Issues:** https://github.com/usuario/metamapa/issues
- **Email:** bugs@metamapa.com

### 💡 Sugerencias y Mejoras
- **Email:** feedback@metamapa.com
- **Formulario:** https://metamapa.com/sugerencias

---

## Glosario de Términos

| Término | Definición |
|---------|------------|
| **Hecho** | Evento histórico o de interés público registrado en la plataforma |
| **Colección** | Agrupación temática de hechos relacionados |
| **Auth0** | Servicio de autenticación utilizado por la plataforma |
| **JWT** | Token de autenticación que contiene información del usuario y sus roles |
| **Solicitud de Eliminación** | Petición de un usuario para eliminar un hecho incorrecto o inapropiado |
| **Contribuyente** | Usuario con permisos para crear y editar hechos |
| **Administrador** | Usuario con control total de la plataforma |
| **Moderación** | Proceso de revisión y aprobación de contenido por administradores |
| **Fuente Estática** | Base de datos MySQL con hechos históricos aprobados y persistentes |
| **Fuente Dinámica** | Base de datos MongoDB con hechos temporales pendientes de aprobación |
| **API Pública** | Endpoints accesibles sin autenticación |
| **API Interna** | Endpoints que requieren autenticación (usuarios registrados) |
| **API Admin** | Endpoints exclusivos para administradores |

---

## Changelog (Historial de Cambios)

### Versión 1.0 (Diciembre 2024)
- ✅ Sistema de autenticación con Auth0
- ✅ Roles: USER, CONTRIBUTOR, ADMIN
- ✅ Creación y edición de hechos con restricción de 7 días
- ✅ Sistema de moderación de contenido
- ✅ Solicitudes de eliminación
- ✅ Gestión de colecciones
- ✅ Importación masiva de datasets CSV
- ✅ Mapa interactivo con Leaflet
- ✅ Filtros avanzados de búsqueda
- ✅ Panel administrativo completo

### Próximas Funcionalidades (Roadmap)
- 🔜 Notificaciones por email
- 🔜 Sistema de comentarios en hechos
- 🔜 Votación y valoración de hechos
- 🔜 API REST pública documentada con Swagger
- 🔜 Exportación de mapas en formato imagen
- 🔜 Integración con redes sociales
- 🔜 Modo oscuro
- 🔜 Aplicación móvil (iOS y Android)

---

¡Gracias por usar MetaMapa! 🗺️✨
