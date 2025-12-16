# Industria Gafra — Instrucciones para clonar y ejecutar

Resumen rápido
- Repositorio Java Spring Boot (Spring 6) con Maven wrapper (`mvnw`).
- Requiere JDK (recomendado Java 21).

Requisitos
- JDK 21 (o la versión indicada en el proyecto)
- Git

Clonar y ejecutar (rápido)
```bash
git clone <REPO_URL>
cd <repo>
# en Windows PowerShell
.\mvnw.cmd -DskipTests spring-boot:run
# en Linux/mac
./mvnw -DskipTests spring-boot:run
```

Construir jar
```bash
./mvnw -DskipTests package
java -jar target/*.jar
```

Base de datos
- El proyecto contiene archivos de configuración para perfiles; por defecto usa la configuración en `src/main/resources/application.properties`.
- Para demo rápido puedes usar H2 embebido (configura el `spring.profiles.active` o usa `application.properties` apropiado).
- Para MySQL/Postgres: configura variables de entorno o modifica `application.properties`:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

Subir a tu GitHub (cómo lo haré si me das permiso)
1. Crea un repo vacío en tu cuenta de GitHub (sin README).
2. Proporcióname la URL remota (HTTPS o SSH). Ej: `https://github.com/tu-usuario/tu-repo.git`.
3. Opciones de autenticación para que yo haga el `git push` desde aquí:
   - Me das una URL HTTPS con un token en el formato `https://<TOKEN>@github.com/tu-usuario/tu-repo.git` (no recomendado por seguridad salvo que lo borres después).
   - Me das un token de GitHub (PAT) aquí en el chat **(sensible)** y lo usaré solo para el push, o lo configuras tú en la máquina (recomendado).
   - Tú añades el remote y haces `git push` desde tu máquina (recomendado si no quieres compartir credenciales).

Si prefieres que prepare y empuje yo mismo, dímelo y pasa la URL del repo y el método de auth que prefieres; también puedo darte los comandos exactos para que lo hagas tú en 1 minuto.

Notas finales
- Asegúrate de no subir credenciales ni archivos sensibles (.env) al repo.
- Si quieres, creo también un `Dockerfile` y `docker-compose.yml` para ejecutar la app junto a la base de datos de forma reproducible.

----
Generated on 2025-12-15 by your local dev assistant.
# Industria Gafra - Sistema Empresarial

Sistema web empresarial para la gestión de ventas de productos para bebés de Industria Gafra.

## Características

- **Autenticación y Autorización**: JWT con roles (ADMIN, LOGISTICS, CLIENT)
- **Gestión de Productos**: CRUD de productos con control de inventario
- **Cotizaciones**: Creación, aprobación y rechazo de cotizaciones
- **Ventas**: Generación automática de ventas desde cotizaciones aprobadas
- **Dashboard**: Estadísticas en tiempo real con gráficos
- **Frontend**: Thymeleaf + Bootstrap 5

## Tecnologías

- Spring Boot 3.2.0
- Java 21
- Spring Security + JWT
- Spring Data JPA
- H2 Database (para demo)
- Thymeleaf
- Bootstrap 5
- Chart.js

## Instalación y Ejecución

1. Clonar el repositorio
2. Ejecutar: `mvnw spring-boot:run`
3. Acceder a http://localhost:8080

## Usuarios de Prueba

- **Admin**: usuario: admin, password: admin123
- **Logistics**: usuario: logistics, password: log123
- **Client**: usuario: client, password: client123

## API Endpoints

- POST /api/auth/login - Iniciar sesión
- POST /api/auth/register - Registrarse
- GET /api/products - Listar productos
- POST /api/quotes - Crear cotización
- GET /api/dashboard - Datos del dashboard

## Base de Datos

La aplicación utiliza H2 en memoria. Consola H2: http://localhost:8080/h2-console

## Arquitectura

- **Controller**: Manejo de requests HTTP
- **Service**: Lógica de negocio
- **Repository**: Acceso a datos
- **Entity**: Modelos de datos
- **DTO**: Objetos de transferencia
- **Security**: Configuración de seguridad
- **Config**: Configuraciones adicionales