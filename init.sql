-- Script de inicialización para PostgreSQL
-- Este archivo se ejecuta automáticamente cuando se crea el contenedor

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Configurar zona horaria
SET timezone = 'America/Mexico_City';

-- Crear esquemas adicionales si es necesario
-- CREATE SCHEMA IF NOT EXISTS audit;

-- Mensaje de confirmación
SELECT 'Base de datos SGERM inicializada correctamente' AS status;