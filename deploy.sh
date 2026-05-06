#!/bin/bash

# Script de despliegue para AWS EC2
# Este script descarga el proyecto, lo compila y lo ejecuta

set -e

echo "🚀 Iniciando despliegue de Travel Agency en AWS EC2..."

# Actualizar el sistema
echo "📦 Actualizando paquetes del sistema..."
sudo dnf update -y
sudo dnf install -y git curl

# Instalar Docker
echo "🐳 Instalando Docker..."
sudo dnf install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Instalar Docker Compose
echo "📝 Instalando Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Clonar el repositorio
echo "📥 Clonando repositorio..."
cd /home/ec2-user
if [ -d "Tingeso-1" ]; then
  cd Tingeso-1
  git pull origin main
else
  git clone https://github.com/tu-usuario/Tingeso-1.git
  cd Tingeso-1
fi

# Construir y ejecutar contenedores
echo "🔨 Construyendo y ejecutando contenedores..."
docker-compose up -d

echo "✅ Despliegue completado!"
echo "📍 Aplicación disponible en:"
echo "   - Frontend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)"
echo "   - Backend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8090"
