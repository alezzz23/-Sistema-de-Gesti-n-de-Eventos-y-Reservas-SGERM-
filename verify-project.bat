@echo off
echo ========================================
echo VERIFICACION DEL PROYECTO SGERM
echo ========================================
echo.

echo 1. Verificando version de Java...
java -version
echo.

echo 2. Verificando estructura del proyecto...
dir /s /b src\main\java\*.java | find /c ".java"
echo archivos Java encontrados.
echo.

echo 3. Listando archivos principales:
echo.
echo === MODELOS ===
dir /b src\main\java\com\sgerm\eventmanagement\model\*.java
echo.
echo === REPOSITORIOS ===
dir /b src\main\java\com\sgerm\eventmanagement\repository\*.java
echo.
echo === SERVICIOS ===
dir /b src\main\java\com\sgerm\eventmanagement\service\*.java
echo.
echo === CONTROLADORES ===
dir /b src\main\java\com\sgerm\eventmanagement\controller\*.java
echo.

echo 4. Verificando archivo de configuracion...
if exist src\main\resources\application.yml (
    echo ✓ application.yml encontrado
) else (
    echo ✗ application.yml NO encontrado
)
echo.

echo 5. Verificando pom.xml...
if exist pom.xml (
    echo ✓ pom.xml encontrado
    findstr "<artifactId>event-management-system</artifactId>" pom.xml >nul
    if !errorlevel! equ 0 (
        echo ✓ Configuracion correcta del proyecto
    ) else (
        echo ✗ Configuracion del proyecto incorrecta
    )
) else (
    echo ✗ pom.xml NO encontrado
)
echo.

echo ========================================
echo RESUMEN DE VERIFICACION
echo ========================================
echo.
echo El proyecto SGERM contiene:
echo - Modelos de datos (User, Event, Booking, etc.)
echo - Repositorios JPA
echo - Servicios de negocio
echo - Controladores REST
echo - Configuracion Spring Boot
echo.
echo Para ejecutar el proyecto necesitas:
echo 1. Instalar Maven (https://maven.apache.org/download.cgi)
echo 2. Configurar PostgreSQL
echo 3. Ejecutar: mvn spring-boot:run
echo.
echo ========================================
pause