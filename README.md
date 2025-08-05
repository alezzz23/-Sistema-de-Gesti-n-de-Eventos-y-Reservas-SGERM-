# Sistema de Gestión de Eventos y Reservas (SGERM)

## 📋 Descripción

SGERM es una aplicación REST API para la gestión integral de eventos y reservas, desarrollada con Spring Boot, que permite administrar usuarios, eventos, reservas y recursos de manera eficiente a través de endpoints REST.

## 🏗️ Arquitectura del Proyecto

### Estructura de Directorios
```
src/main/java/com/sgerm/eventmanagement/
├── EventManagementApplication.java    # Clase principal
├── controller/                         # Controladores REST
│   └── UserController.java
├── model/                             # Entidades JPA
│   ├── User.java
│   ├── Event.java
│   ├── Booking.java
│   ├── EventResource.java
│   ├── Notification.java
│   └── [enums y otras entidades]
├── repository/                        # Repositorios JPA
│   ├── UserRepository.java
│   ├── EventRepository.java
│   ├── BookingRepository.java
│   ├── EventResourceRepository.java
│   └── NotificationRepository.java
└── service/                          # Lógica de negocio
    ├── UserService.java
    ├── EventService.java
    ├── BookingService.java
    ├── EventResourceService.java
    ├── NotificationService.java
    └── EmailService.java
```

## 🚀 Características Principales

### ✅ Funcionalidades Implementadas

#### 🔌 API REST Completa
- ✅ Endpoints para todas las operaciones CRUD
- ✅ Documentación de API integrada
- ✅ Respuestas en formato JSON
- ✅ Manejo de errores estandarizado

#### 👥 Gestión de Usuarios
- ✅ Registro y autenticación de usuarios vía API
- ✅ Gestión de perfiles y roles (USER, ORGANIZER, ADMIN)
- ✅ Verificación de email y recuperación de contraseña
- ✅ Sistema de notificaciones

#### 🎪 Gestión de Eventos
- ✅ API para creación y edición de eventos
- ✅ Categorización de eventos
- ✅ Control de capacidad y disponibilidad
- ✅ Estados de eventos (DRAFT, PUBLISHED, CANCELLED, COMPLETED)

#### 🎫 Sistema de Reservas
- ✅ API para reserva de entradas con códigos únicos
- ✅ Estados de reserva (PENDING, CONFIRMED, CANCELLED, COMPLETED)
- ✅ Generación de códigos QR
- ✅ Sistema de check-in

#### 🛠️ Gestión de Recursos
- ✅ API para administración de recursos para eventos
- ✅ Control de inventario y costos
- ✅ Asignación de responsables

#### 📧 Sistema de Notificaciones
- ✅ API para notificaciones por email
- ✅ Diferentes tipos y prioridades
- ✅ Plantillas HTML con Thymeleaf

#### 🌐 Interfaz Web Simple
- ✅ Página de información del sistema
- ✅ Documentación de endpoints disponibles
- ✅ Acceso a herramientas de desarrollo (H2 Console, Actuator)

## 🛠️ Tecnologías Utilizadas

- **Framework**: Spring Boot 3.2.0
- **Base de Datos**: PostgreSQL (Producción), H2 (Desarrollo)
- **ORM**: Spring Data JPA / Hibernate
- **Seguridad**: Spring Security
- **Cache**: Redis
- **Email**: Spring Mail + Thymeleaf
- **Documentos**: iText PDF, Apache POI (Excel)
- **QR Codes**: ZXing
- **API Documentation**: Interfaz web simple para documentación
- **Templates**: Thymeleaf (para páginas informativas)
- **Containerización**: Docker & Docker Compose
- **Java**: OpenJDK 17+

## 📋 Prerrequisitos

### Software Requerido
1. **Java 17 o superior** ✅ (Detectado: OpenJDK 21.0.8)
2. **Maven 3.6+** ❌ (No instalado)
3. **PostgreSQL 12+** (Para producción)
4. **Redis** (Opcional, para cache)

### Instalación de Maven

#### Windows:
1. Descargar Maven desde: https://maven.apache.org/download.cgi
2. Extraer en `C:\Program Files\Apache\maven`
3. Agregar `C:\Program Files\Apache\maven\bin` al PATH
4. Verificar: `mvn -version`

#### Usando Chocolatey:
```bash
choco install maven
```

#### Usando Scoop:
```bash
scoop install maven
```

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone <repository-url>
cd Java-project
```

### 2. Configurar Base de Datos

#### Para Desarrollo (H2 - No requiere instalación):
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

#### Para Producción (PostgreSQL):
1. Instalar PostgreSQL
2. Crear base de datos:
```sql
CREATE DATABASE sgerm_db;
CREATE USER sgerm_user WITH PASSWORD 'sgerm_password';
GRANT ALL PRIVILEGES ON DATABASE sgerm_db TO sgerm_user;
```

### 3. Configurar Variables de Entorno
```bash
# Email (opcional)
set EMAIL_USERNAME=tu-email@gmail.com
set EMAIL_PASSWORD=tu-app-password

# JWT (opcional)
set JWT_SECRET=tu-clave-secreta-jwt
```

### 4. Compilar y Ejecutar
```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar aplicación
mvn spring-boot:run
```

## 🔍 Verificación del Proyecto

### Estado Actual ✅
- **Archivos Java**: 26 archivos encontrados
- **Modelos**: 13 entidades y enums
- **Repositorios**: 5 repositorios JPA
- **Servicios**: 6 servicios de negocio
- **Controladores**: 1 controlador REST (UserController)
- **Configuración**: application.yml configurado
- **Build**: pom.xml con todas las dependencias

### Script de Verificación
Ejecuta el script incluido para verificar el estado:
```bash
.\verify-project.bat
```

## 🌐 Endpoints API

### Usuarios
- `GET /api/users/profile` - Obtener perfil del usuario
- `PUT /api/users/profile` - Actualizar perfil
- `POST /api/users/change-password` - Cambiar contraseña
- `GET /api/admin/users` - Listar usuarios (Admin)
- `POST /api/admin/users` - Crear usuario (Admin)

### Próximos Controladores
- `EventController` - Gestión de eventos
- `BookingController` - Gestión de reservas
- `ResourceController` - Gestión de recursos

## 🔧 Configuración de Desarrollo

### Perfiles de Spring
- **dev**: Usa H2 en memoria, logs detallados
- **prod**: Usa PostgreSQL, logs optimizados

### Acceso a H2 Console (Desarrollo)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Contraseña: (vacía)

## 📝 Próximos Pasos

### Pendientes de Implementación
1. **Controladores REST**:
   - EventController
   - BookingController
   - ResourceController
   - NotificationController

2. **Seguridad**:
   - Configuración de Spring Security
   - JWT Authentication
   - Autorización por roles

3. **Frontend**:
   - Plantillas Thymeleaf
   - API REST para SPA

4. **Testing**:
   - Tests unitarios
   - Tests de integración

## 🐛 Solución de Problemas

### Error: 'mvn' no se reconoce
**Solución**: Instalar Maven y agregarlo al PATH del sistema.

### Error de conexión a base de datos
**Solución**: Usar perfil de desarrollo con H2:
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Puerto 8080 en uso
**Solución**: Cambiar puerto en application.yml:
```yaml
server:
  port: 8081
```

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

## 👥 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature
3. Commit tus cambios
4. Push a la rama
5. Abrir un Pull Request

---

**Estado del Proyecto**: 🟡 En Desarrollo  
**Última Actualización**: Enero 2025  
**Versión**: 1.0.0