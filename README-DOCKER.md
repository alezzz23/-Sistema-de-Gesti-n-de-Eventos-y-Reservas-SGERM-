# 🐳 SGERM - Configuración con Docker

## ¿Por qué usar Docker?

✅ **Sin instalación de PostgreSQL**: No necesitas instalar PostgreSQL en tu máquina  
✅ **Configuración automática**: Base de datos configurada automáticamente  
✅ **Aislamiento**: Todo funciona en contenedores separados  
✅ **Portabilidad**: Funciona igual en cualquier máquina con Docker  
✅ **Fácil limpieza**: Puedes eliminar todo sin afectar tu sistema  

## 📋 Prerrequisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado
- [Docker Compose](https://docs.docker.com/compose/install/) (incluido con Docker Desktop)

## 🚀 Instrucciones de Uso

### 1. Levantar los servicios

```bash
# Construir y levantar todos los contenedores
docker-compose up --build

# O en modo detached (en segundo plano)
docker-compose up --build -d
```

### 2. Acceder a la aplicación

- **Aplicación Web**: http://localhost:8080
- **Base de datos**: localhost:5432
  - Usuario: `sgerm_user`
  - Contraseña: `sgerm_password`
  - Base de datos: `sgerm_db`

### 3. Ver logs

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs solo de la aplicación
docker-compose logs -f app

# Ver logs solo de PostgreSQL
docker-compose logs -f postgres
```

### 4. Detener los servicios

```bash
# Detener contenedores
docker-compose down

# Detener y eliminar volúmenes (CUIDADO: elimina datos)
docker-compose down -v
```

## 🔧 Comandos Útiles

### Reconstruir solo la aplicación
```bash
docker-compose build app
docker-compose up app
```

### Acceder al contenedor de la aplicación
```bash
docker-compose exec app bash
```

### Acceder a PostgreSQL
```bash
docker-compose exec postgres psql -U sgerm_user -d sgerm_db
```

### Ver estado de los contenedores
```bash
docker-compose ps
```

## 📁 Estructura de Archivos Docker

```
├── docker-compose.yml    # Configuración de servicios
├── Dockerfile            # Imagen de la aplicación
├── .dockerignore         # Archivos a ignorar
├── init.sql             # Script de inicialización de BD
└── README-DOCKER.md     # Esta documentación
```

## 🐛 Solución de Problemas

### La aplicación no inicia
```bash
# Ver logs detallados
docker-compose logs app

# Reconstruir desde cero
docker-compose down
docker-compose build --no-cache
docker-compose up
```

### Problemas de conexión a la base de datos
```bash
# Verificar que PostgreSQL esté funcionando
docker-compose exec postgres pg_isready -U sgerm_user

# Reiniciar solo PostgreSQL
docker-compose restart postgres
```

### Limpiar todo y empezar de nuevo
```bash
# CUIDADO: Esto elimina todos los datos
docker-compose down -v
docker system prune -f
docker-compose up --build
```

## 🎯 Ventajas de esta Configuración

1. **Cero configuración manual**: Todo se configura automáticamente
2. **Datos persistentes**: Los datos de PostgreSQL se guardan en un volumen
3. **Health checks**: La aplicación espera a que PostgreSQL esté listo
4. **Red aislada**: Los servicios se comunican en su propia red
5. **Fácil escalabilidad**: Puedes agregar más servicios fácilmente

## 📝 Notas Importantes

- Los datos de PostgreSQL se persisten en un volumen Docker
- La aplicación usa el perfil `docker` automáticamente
- Redis está incluido para futuras funcionalidades de caché
- El primer build puede tardar varios minutos

¡Ahora puedes desarrollar sin preocuparte por configurar PostgreSQL! 🎉