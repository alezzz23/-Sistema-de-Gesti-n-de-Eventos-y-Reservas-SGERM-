# 🚀 Guía de Instalación de Maven - Métodos Más Fáciles

## 🏆 Método 1: Chocolatey (MÁS FÁCIL) ⭐⭐⭐⭐⭐

### ¿Por qué es el más fácil?
- ✅ **Instalación automática** - Solo 2 comandos
- ✅ **Configura PATH automáticamente**
- ✅ **Gestiona actualizaciones**
- ✅ **No requiere configuración manual**

### Pasos:

#### 1. Instalar Chocolatey (si no lo tienes)
```powershell
# Ejecutar PowerShell como Administrador
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

#### 2. Instalar Maven
```powershell
# En PowerShell como Administrador
choco install maven
```

#### 3. Verificar instalación
```powershell
mvn -version
```

### ✅ Ventajas:
- Instalación en menos de 5 minutos
- Actualización automática con `choco upgrade maven`
- Desinstalación fácil con `choco uninstall maven`

---

## 🥈 Método 2: Scoop (SEGUNDO MÁS FÁCIL) ⭐⭐⭐⭐

### ¿Por qué es fácil?
- ✅ **Instalación automática**
- ✅ **No requiere permisos de administrador**
- ✅ **Configura PATH automáticamente**
- ✅ **Instalación en directorio de usuario**

### Pasos:

#### 1. Instalar Scoop (si no lo tienes)
```powershell
# En PowerShell normal (no como administrador)
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex
```

#### 2. Instalar Maven
```powershell
scoop install maven
```

#### 3. Verificar instalación
```powershell
mvn -version
```

### ✅ Ventajas:
- No necesita permisos de administrador
- Instalación limpia en tu directorio personal
- Fácil de desinstalar

---

## 🥉 Método 3: Winget (NATIVO DE WINDOWS) ⭐⭐⭐

### ¿Por qué es bueno?
- ✅ **Viene preinstalado en Windows 10/11**
- ✅ **Oficial de Microsoft**
- ✅ **Instalación automática**

### Pasos:

#### 1. Buscar Maven
```cmd
winget search maven
```

#### 2. Instalar Maven
```cmd
winget install Apache.Maven
```

#### 3. Reiniciar terminal y verificar
```cmd
mvn -version
```

### ⚠️ Nota:
- Puede requerir reiniciar la terminal
- A veces necesita configuración manual del PATH

---

## 🔧 Método 4: Instalación Manual (MÁS CONTROL) ⭐⭐

### Solo si los métodos anteriores no funcionan

#### 1. Descargar Maven
- Ir a: https://maven.apache.org/download.cgi
- Descargar: `apache-maven-3.9.6-bin.zip`

#### 2. Extraer
- Extraer en: `C:\Program Files\Apache\maven`

#### 3. Configurar PATH
```cmd
# Agregar al PATH del sistema:
C:\Program Files\Apache\maven\bin
```

#### 4. Configurar JAVA_HOME (si no está configurado)
```cmd
# Variable de entorno JAVA_HOME:
C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
```

---

## 🎯 Recomendación Final

### Para la mayoría de usuarios: **CHOCOLATEY**

```powershell
# 1. Instalar Chocolatey (PowerShell como Admin)
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 2. Instalar Maven
choco install maven

# 3. Verificar
mvn -version
```

### ¿Por qué Chocolatey es el mejor?
1. **Más rápido**: 2 comandos y listo
2. **Más confiable**: Configura todo automáticamente
3. **Más mantenible**: Actualizaciones automáticas
4. **Más limpio**: Desinstalación fácil

---

## 🚨 Solución de Problemas Comunes

### Error: "mvn no se reconoce"
```powershell
# Reiniciar terminal o ejecutar:
refreshenv

# O cerrar y abrir nueva terminal
```

### Error: "JAVA_HOME no está configurado"
```powershell
# Verificar Java
java -version

# Si Java está instalado, configurar JAVA_HOME automáticamente
[Environment]::SetEnvironmentVariable("JAVA_HOME", (Get-Command java).Source.Replace("\bin\java.exe", ""), "Machine")
```

### Verificar que todo funciona
```powershell
# Verificar Java
java -version

# Verificar Maven
mvn -version

# Verificar variables de entorno
echo $env:JAVA_HOME
echo $env:PATH
```

---

## ⚡ Script de Instalación Automática

### Guarda este script como `instalar-maven.ps1`:

```powershell
# Script de instalación automática de Maven
Write-Host "Instalando Maven automáticamente..." -ForegroundColor Green

# Verificar si Chocolatey está instalado
try {
    choco --version | Out-Null
    Write-Host "Chocolatey ya está instalado" -ForegroundColor Green
} catch {
    Write-Host "Instalando Chocolatey..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
}

# Instalar Maven
Write-Host "Instalando Maven..." -ForegroundColor Yellow
choco install maven -y

# Verificar instalación
Write-Host "Verificando instalación..." -ForegroundColor Yellow
refreshenv
mvn -version

Write-Host "¡Maven instalado correctamente!" -ForegroundColor Green
Write-Host "Ahora puedes ejecutar: mvn spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
```

### Ejecutar el script:
```powershell
# PowerShell como Administrador
.\instalar-maven.ps1
```

---

## 🎉 Después de la Instalación

### Ejecutar tu proyecto SGERM:
```bash
# Navegar al directorio del proyecto
cd C:\Users\MOISES\Desktop\portarfolio\Java-project

# Ejecutar en modo desarrollo
mvn spring-boot:run -Dspring.profiles.active=dev

# Abrir en el navegador
# http://localhost:8080
# http://localhost:8080/h2-console (base de datos)
```

### Comandos útiles de Maven:
```bash
# Compilar
mvn clean compile

# Ejecutar tests
mvn test

# Crear JAR
mvn clean package

# Limpiar proyecto
mvn clean
```

---

**💡 Consejo**: Si tienes dudas, usa **Chocolatey**. Es la opción más fácil y confiable para Windows.