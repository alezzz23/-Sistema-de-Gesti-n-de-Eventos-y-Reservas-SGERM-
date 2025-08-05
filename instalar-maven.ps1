# Script simple para instalar Maven
Write-Host "Instalando Maven para SGERM..." -ForegroundColor Green

# Verificar Java
Write-Host "Verificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java encontrado: $($javaVersion[0])" -ForegroundColor Green
} catch {
    Write-Host "Java no encontrado. Instala Java primero." -ForegroundColor Red
    exit 1
}

# Verificar Maven
Write-Host "Verificando Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1
    Write-Host "Maven ya instalado: $($mavenVersion[0])" -ForegroundColor Green
    Write-Host "Puedes ejecutar: mvn spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
    exit 0
} catch {
    Write-Host "Maven no encontrado. Instalando..." -ForegroundColor Yellow
}

# Verificar Chocolatey
Write-Host "Verificando Chocolatey..." -ForegroundColor Yellow
try {
    choco --version | Out-Null
    Write-Host "Chocolatey encontrado" -ForegroundColor Green
} catch {
    Write-Host "Instalando Chocolatey..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
    
    # Actualizar PATH
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}

# Instalar Maven
Write-Host "Instalando Maven con Chocolatey..." -ForegroundColor Yellow
choco install maven -y

# Actualizar PATH
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Verificar instalacion
Write-Host "Verificando instalacion..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

try {
    $mavenVersion = mvn -version 2>&1
    Write-Host "Maven instalado correctamente!" -ForegroundColor Green
    Write-Host "Version: $($mavenVersion[0])" -ForegroundColor White
    Write-Host "" 
    Write-Host "Ejecuta tu proyecto con:" -ForegroundColor Cyan
    Write-Host "mvn spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor White
    Write-Host ""
    Write-Host "URLs:" -ForegroundColor Yellow
    Write-Host "- Aplicacion: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "- Base de datos: http://localhost:8080/h2-console" -ForegroundColor Cyan
} catch {
    Write-Host "Maven instalado pero requiere reiniciar terminal" -ForegroundColor Yellow
    Write-Host "Cierra y abre nueva terminal, luego ejecuta: mvn -version" -ForegroundColor Cyan
}

Write-Host "Instalacion completada!" -ForegroundColor Green