Despliegue manual usando Docker Compose

Requisitos en la máquina destino (servidor cloud):
- Docker instalado
- Docker Compose (v1 o v2 compatible)
- Acceso a Internet para descargar imágenes desde Docker Hub

Pasos rápidos:
1. Clona o copia el directorio `Deploy/` al servidor.
2. Crea un archivo `.env` si tu `docker-compose.yml` usa variables de entorno (opcional). Puedes basarte en `.env.example`.
3. Ejecuta:

```bash
cd Deploy
chmod +x deploy.sh
./deploy.sh
```

Esto ejecuta `docker-compose pull` y `docker-compose up -d` para descargar las imágenes publicadas en Docker Hub y levantar los servicios.

Notas importantes:
- Asegúrate de que `Deploy/docker-compose.yml` referencia las imágenes de Docker Hub públicas o privadas (si son privadas, autentícate con `docker login` antes de ejecutar `deploy.sh`).
- El servicio Keycloak en `docker-compose.yml` expone un puerto 8180 en el host; ajusta según tu infraestructura.
- Si necesitas múltiples réplicas o balanceo adicional, edita `docker-compose.yml` antes de ejecutar.
