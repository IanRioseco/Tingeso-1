#!/usr/bin/env bash
set -euo pipefail

# Script de despliegue manual por Docker Compose
# Uso: ejecutar en la máquina destino (servidor cloud) dentro del directorio Deploy/
# Asegúrate de haber exportado o creado un archivo .env con las variables necesarias
# (si tu docker-compose lo usa). Este script hace pull de las imágenes (Docker Hub)
# y recrea los servicios.

echo "Pull de imágenes desde Docker Hub..."
docker-compose pull

echo "Recreando servicios (sin detener dependencias innecesarias)..."
docker-compose up -d --remove-orphans

echo "Esperando 5 segundos para comprobar estado..."
sleep 5

echo "Estado de servicios:"
docker-compose ps

echo "Despliegue completado. Revisa logs si fuera necesario: docker-compose logs -f" 
