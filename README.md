# Tingeso-1
Repositorio para la entrega 1 de técnicas de ingeniería de software.

## Estrategia de ramas

Se recomienda un flujo basado en GitFlow:

- `main`: versiones estables y despliegue final.
- `develop`: integración continua de trabajo listo para pruebas.
- `feature/*`: cambios de una funcionalidad puntual.
- `release/*`: estabilización previa a una publicación.
- `hotfix/*`: correcciones urgentes sobre producción.

## Versionamiento semántico

Se usa Semantic Versioning: `MAJOR.MINOR.PATCH`.

- `MAJOR`: cambios incompatibles.
- `MINOR`: nuevas funcionalidades compatibles.
- `PATCH`: correcciones compatibles.

En Docker Hub, los tags recomendados son:

- `latest` para la rama `main`.
- `vX.Y.Z` cuando se publique una versión estable. Esos tags disparan el workflow y generan la publicación versionada.

## GitHub Actions

El repositorio incluye un workflow en [/.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml) que:

- ejecuta pruebas unitarias del backend con Maven,
- construye el frontend con Vite,
- construye las imágenes Docker del backend y frontend,
- publica ambas imágenes en Docker Hub.

### Secrets requeridos en GitHub

En `Settings > Secrets and variables > Actions` del repositorio crea:

- `DOCKERHUB_USERNAME` = tu usuario de Docker Hub
- `DOCKERHUB_TOKEN` = token personal de Docker Hub con permisos `Read & Write`

## Backend

En `TravelAgencyBackend`:

1. Ejecutar `mvn clean package` o `mvn -DskipTests clean package` si quieres omitir tests.
2. Crear la imagen con `docker build -t ian04/travelagency-backend:latest .`.
3. Para reconstruir sin cache: `docker build --no-cache -t ian04/travelagency-backend:latest .`.
4. Ejecutar la imagen con `docker run -e DB_HOST=192.168.56.1 -e DB_PORT=5432 -e DB_USERNAME=TU_USUARIO -e DB_PASSWORD=TU_PASSWORD -d -p 8090:8090 ian04/travelagency-backend:latest`.
5. Abrir el backend en `http://localhost:8090/api/packages/` o cualquier endpoint de `api`.
6. Publicar en Docker Hub con `docker login` y `docker push ian04/travelagency-backend:latest`.

Para ver el sistema operativo base dentro del contenedor: `cat /etc/os-release`.

## Frontend

En `travelagencyfrontend`:

1. Ejecutar `npm ci` y luego `npm run build` para generar `dist`.
2. Crear la imagen con `docker build -t ian04/travelagency-frontend:latest .`.
3. La imagen ya incluye `nginx.conf` y el build queda con la API apuntando a `/api`.
4. Ejecutar la imagen con `docker run -d -p 8070:80 ian04/travelagency-frontend:latest`.
5. Abrir el frontend en `http://localhost:8070/`.
6. Publicar en Docker Hub con `docker login` y `docker push ian04/travelagency-frontend:latest`.

En el frontend, las variables Vite se aplican en el build final (`dist`), por eso no es necesario pasarlas con `-e` al ejecutar el contenedor.

## Despliegue manual en nube

El despliegue final debe realizarse manualmente desde las imágenes publicadas en Docker Hub.

1. Descargar las imágenes publicadas.
2. Usar Docker Compose para levantar base de datos, Keycloak, backend, frontend y balanceadores.
3. Verificar que el frontend responda por HTTPS y que el login OIDC funcione con el realm configurado.
4. Validar que el backend reciba JWT válidos con el issuer correcto.
