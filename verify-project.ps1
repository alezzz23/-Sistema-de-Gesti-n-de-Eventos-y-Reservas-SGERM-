# Script de Verificacion del Proyecto SGERM
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VERIFICACION DEL PROYECTO SGERM" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar Java
Write-Host "1. Verificando Java..." -ForegroundColor Yellow
try {
    $javaOutput = java -version 2>&1
    Write-Host "   Java instalado correctamente" -ForegroundColor Green
    Write-Host "   Version: $($javaOutput[0])" -ForegroundColor White
} catch {
    Write-Host "   Java NO encontrado" -ForegroundColor Red
    exit 1
}

# 2. Verificar Maven
Write-Host "`n2. Verificando Maven..." -ForegroundColor Yellow
try {
    $mavenOutput = mvn -version 2>&1
    Write-Host "   Maven instalado correctamente" -ForegroundColor Green
    $mavenInstalled = $true
} catch {
    Write-Host "   Maven NO instalado" -ForegroundColor Red
    Write-Host "   Instalar desde: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    $mavenInstalled = $false
}

# 3. Verificar estructura del proyecto
Write-Host "`n3. Verificando estructura del proyecto..." -ForegroundColor Yellow

$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
$javaCount = $javaFiles.Count
Write-Host "   Archivos Java encontrados: $javaCount" -ForegroundColor Cyan

# Contar por categoria
$models = (Get-ChildItem -Path "src\main\java\com\sgerm\eventmanagement\model" -Filter "*.java" -ErrorAction SilentlyContinue).Count
$repositories = (Get-ChildItem -Path "src\main\java\com\sgerm\eventmanagement\repository" -Filter "*.java" -ErrorAction SilentlyContinue).Count
$services = (Get-ChildItem -Path "src\main\java\com\sgerm\eventmanagement\service" -Filter "*.java" -ErrorAction SilentlyContinue).Count
$controllers = (Get-ChildItem -Path "src\main\java\com\sgerm\eventmanagement\controller" -Filter "*.java" -ErrorAction SilentlyContinue).Count

Write-Host "   Distribucion:" -ForegroundColor Cyan
Write-Host "      - Modelos: $models" -ForegroundColor White
Write-Host "      - Repositorios: $repositories" -ForegroundColor White
Write-Host "      - Servicios: $services" -ForegroundColor White
Write-Host "      - Controladores: $controllers" -ForegroundColor White

# 4. Verificar archivos de configuracion
Write-Host "`n4. Verificando configuracion..." -ForegroundColor Yellow

if (Test-Path "pom.xml") {
    Write-Host "   pom.xml encontrado" -ForegroundColor Green
} else {
    Write-Host "   pom.xml NO encontrado" -ForegroundColor Red
}

if (Test-Path "src\main\resources\application.yml") {
    Write-Host "   application.yml encontrado" -ForegroundColor Green
} else {
    Write-Host "   application.yml NO encontrado" -ForegroundColor Red
}

# 5. Resumen final
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "RESUMEN DE VERIFICACION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nEstado del Proyecto SGERM:" -ForegroundColor White
Write-Host "   • Archivos Java: $javaCount" -ForegroundColor White
Write-Host "   • Modelos de datos: $models" -ForegroundColor White
Write-Host "   • Repositorios JPA: $repositories" -ForegroundColor White
Write-Host "   • Servicios de negocio: $services" -ForegroundColor White
Write-Host "   • Controladores REST: $controllers" -ForegroundColor White

Write-Host "`nPara ejecutar el proyecto:" -ForegroundColor Yellow
if ($mavenInstalled) {
    Write-Host "   1. mvn spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
    Write-Host "   2. Abrir: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "   3. H2 Console: http://localhost:8080/h2-console" -ForegroundColor Cyan
} else {
    Write-Host "   1. Instalar Maven primero" -ForegroundColor Red
    Write-Host "   2. Luego ejecutar: mvn spring-boot:run -Dspring.profiles.active=dev" -ForegroundColor Cyan
}

Write-Host "`nDocumentacion:" -ForegroundColor Yellow
Write-Host "   • README.md - Documentacion completa" -ForegroundColor Cyan
Write-Host "   • application.yml - Configuracion" -ForegroundColor Cyan
Write-Host "   • pom.xml - Dependencias Maven" -ForegroundColor Cyan

Write-Host "`n========================================" -ForegroundColor Cyan

if ($mavenInstalled) {
    Write-Host "Proyecto listo para ejecutar" -ForegroundColor Green
} else {
    Write-Host "Instalar Maven para continuar" -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Cyan