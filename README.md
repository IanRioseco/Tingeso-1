# Tingeso-1
Repositorio para la entrega 1 de técnicas de ingeniería de software

## Backend

En `TravelAgencyBackend`:

1. Ejecutar `mvn clean package` o `mvn -DskipTests clean package` si quieres omitir tests.
2. Crear la imagen con `docker build -t mtisw/payroll-backend:latest .`.
3. Para reconstruir sin cache: `docker build --no-cache -t mtisw/payroll-backend:latest .`.
4. Ejecutar la imagen con `docker run -e DB_HOST=192.168.56.1 -e DB_PORT=5432 -e DB_USERNAME=TU_USUARIO -e DB_PASSWORD=TU_PASSWORD -d -p 8090:8090 mtisw/payroll-backend:latest`.
5. Abrir el backend en `http://localhost:8090/api/packages/` o cualquier endpoint de `api`.
6. Publicar en Docker Hub con `docker login` y `docker push mtisw/payroll-backend:latest`.

Para ver el sistema operativo base dentro del contenedor: `cat /etc/os-release`.

## Frontend

En `travelagencyfrontend`:

1. Ejecutar `npm run build` para generar `dist`.
2. Crear la imagen con `docker build -t mtisw/payroll-frontend:latest .`.
3. La imagen ya incluye `nginx.conf` y el build queda con la API apuntando a `http://localhost:8090/api`.
4. Ejecutar la imagen con `docker run -d -p 8070:80 mtisw/payroll-frontend:latest`.
5. Abrir el frontend en `http://localhost:8070/`.
6. Publicar en Docker Hub con `docker login` y `docker push mtisw/payroll-frontend:latest`.

En el frontend, las variables Vite se aplican en el build final (`dist`), por eso no es necesario pasarlas con `-e` al ejecutar el contenedor.
