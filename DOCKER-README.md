# 🐳 Docker Setup - MetaMapa

## Inicio Rápido

### 1. Levantar las bases de datos
```bash
docker-compose up -d
```

### 2. Ver logs
```bash
docker-compose logs -f
```

### 3. Detener todo
```bash
docker-compose down
```

### 4. Detener y BORRAR TODO (incluye datos)
```bash
docker-compose down -v
```

---

## 📊 Acceso a las Bases de Datos

### MySQL (Puerto 3306)
- **Host**: localhost
- **Puerto**: 3306
- **Usuario**: root
- **Password**: root
- **Base de datos**: utndds

#### Acceso por línea de comandos:
```bash
docker exec -it metamapa-mysql mysql -uroot -proot utndds
```

#### Interfaz web (Adminer):
- URL: http://localhost:8090
- Sistema: MySQL
- Servidor: mysql
- Usuario: root
- Contraseña: root
- Base de datos: utndds

#### Limpiar base de datos MySQL:
```bash
docker exec -i metamapa-mysql mysql -uroot -proot utndds < limpiar_base_datos.sql
```

---

### MongoDB Fuente Dinámica (Puerto 27017)
- **Host**: localhost
- **Puerto**: 27017
- **Usuario**: admin
- **Password**: admin123
- **Base de datos**: fuenteDinamica

#### Acceso por línea de comandos:
```bash
docker exec -it metamapa-mongodb mongosh -u admin -p admin123 --authenticationDatabase admin
```

#### Interfaz web (Mongo Express):
- URL: http://localhost:8091
- Usuario: admin
- Contraseña: admin

#### Limpiar colecciones de MongoDB:
```bash
docker exec -it metamapa-mongodb mongosh -u admin -p admin123 --authenticationDatabase admin fuenteDinamica --eval "db.dropDatabase()"
```

---

### MongoDB Fuente Estática (Puerto 27018)
- **Host**: localhost
- **Puerto**: 27018
- **Usuario**: admin
- **Password**: admin123
- **Base de datos**: fuenteEstatica

#### Acceso por línea de comandos:
```bash
docker exec -it metamapa-mongodb-estatica mongosh -u admin -p admin123 --authenticationDatabase admin
```

---

## 🔄 Comandos Útiles

### Ver estado de los contenedores:
```bash
docker-compose ps
```

### Reiniciar un servicio específico:
```bash
docker-compose restart mysql
docker-compose restart mongodb
```

### Ver logs de un servicio específico:
```bash
docker-compose logs -f mysql
docker-compose logs -f mongodb
```

### Borrar solo los datos (mantener contenedores):
```bash
docker-compose down -v
docker-compose up -d
```

### Entrar a un contenedor:
```bash
# MySQL
docker exec -it metamapa-mysql bash

# MongoDB
docker exec -it metamapa-mongodb bash
```

---

## 🚀 Ejecutar las Aplicaciones Spring Boot

Las aplicaciones Spring Boot se ejecutan **fuera de Docker** (en tu IDE o terminal):

### 1. Fuente Dinámica (Puerto 8082)
```bash
cd fuente-dinamica
mvn spring-boot:run
```
Conecta a: `mongodb://localhost:27017`

### 2. Fuente Estática (Puerto 8083)
```bash
cd fuente-estatica
mvn spring-boot:run
```
Conecta a: `mongodb://localhost:27018`

### 3. Agregador (Puerto 8081)
```bash
cd agregador
mvn spring-boot:run
```
Conecta a: `mysql://localhost:3306/utndds`

### 4. Gestor Solicitudes (Puerto 8080)
```bash
cd gestor-solicitudes
mvn spring-boot:run
```
Conecta a: `mysql://localhost:3306/utndds`

---

## 🧹 Limpiar Todo para Pruebas Frescas

```bash
# 1. Detener y borrar todo
docker-compose down -v

# 2. Levantar de nuevo
docker-compose up -d

# 3. Esperar 10 segundos a que inicien
# Las bases de datos estarán limpias y listas

# 4. Iniciar las aplicaciones Spring Boot
```

---

## 🛠️ Herramientas Recomendadas

### MySQL:
- **MySQL Workbench**: https://dev.mysql.com/downloads/workbench/
- **DBeaver**: https://dbeaver.io/
- **Adminer** (incluido): http://localhost:8090

### MongoDB:
- **MongoDB Compass**: https://www.mongodb.com/products/compass
- **Robo 3T**: https://robomongo.org/
- **Mongo Express** (incluido): http://localhost:8091

---

## ⚠️ Notas Importantes

1. **Primer inicio**: La primera vez tardará más porque descarga las imágenes Docker
2. **Persistencia**: Los datos se guardan en volúmenes Docker (sobreviven a reinicios)
3. **Limpiar todo**: Usa `docker-compose down -v` para borrar datos y empezar de cero
4. **Conectar desde aplicaciones**: Usa `localhost` como host desde tu máquina
5. **Puertos**:
   - 3306: MySQL
   - 27017: MongoDB Dinámica
   - 27018: MongoDB Estática
   - 8090: Adminer (MySQL web)
   - 8091: Mongo Express (MongoDB web)

---

## 🐛 Troubleshooting

### Error: puerto ya en uso
```bash
# Ver qué está usando el puerto
netstat -ano | findstr :3306
netstat -ano | findstr :27017

# Detener todo y reiniciar
docker-compose down
docker-compose up -d
```

### Las aplicaciones no se conectan
```bash
# Verificar que los contenedores estén corriendo
docker-compose ps

# Verificar logs de MySQL
docker-compose logs mysql

# Verificar logs de MongoDB
docker-compose logs mongodb
```

### Quiero empezar de cero
```bash
docker-compose down -v
docker volume prune -f
docker-compose up -d
```
