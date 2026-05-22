# Guía de Despliegue — TodoList API REST

Documentación completa del proceso de despliegue del proyecto **TodoList API REST** (Spring Boot 4.0.5 + MySQL 8) siguiendo la guía oficial del módulo intermodular.

## Índice

1. [Comprobación en local](#1-comprobación-en-local)
2. [Dockerización](#2-dockerización)
3. [Publicación de la imagen en Docker Hub](#3-publicación-de-la-imagen-en-docker-hub)
4. [Despliegue en Railway (opción recomendada)](#4-despliegue-en-railway-opción-recomendada)
5. [Despliegue en Render (alternativa)](#5-despliegue-en-render-alternativa)
6. [Verificación post-despliegue](#6-verificación-post-despliegue)
7. [Resolución de problemas comunes](#7-resolución-de-problemas-comunes)

---

## 1. Comprobación en local

Antes de dockerizar hay que confirmar que el proyecto arranca correctamente con la configuración actual (XAMPP/MySQL local).

### Pasos

1. Arrancar **XAMPP** y poner en marcha el servicio **MySQL**.
2. En la raíz del proyecto ejecutar:
   ```bash
   ./mvnw spring-boot:run
   ```
   En Windows: `mvnw.cmd spring-boot:run`.
3. Abrir en el navegador:
   - **Swagger UI:** `http://localhost:8080/swagger-ui.html`
   - **Cliente web:** abrir `cliente-todo/index.html` con Live Server (VSCode) o IntelliJ (puerto 63342).
4. Probar login con `pepe / 12345` y comprobar que carga las tareas.

Si todo funciona, pasamos a Docker.

---

## 2. Dockerización

El proyecto incluye tres archivos en la raíz que orquestan el contenedor:

| Archivo | Propósito |
|---|---|
| `Dockerfile` | Construye la imagen de la API en dos etapas (build + runtime) |
| `docker-compose.yml` | Levanta API + MySQL conectados por una red interna |
| `.dockerignore` | Excluye archivos innecesarios del contexto de build |

### 2.1. Dockerfile

Usa **multi-stage build**: la primera etapa compila con Maven (`eclipse-temurin:17-jdk-alpine`), la segunda solo lleva el JRE para que la imagen final pese menos.

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 2.2. docker-compose.yml

Define dos servicios:

- **`db`**: MySQL 8 con volumen persistente (`mysql-data`). Puerto **3307** en el host para no chocar con el XAMPP local que ya usa el 3306.
- **`app`**: la API. Depende de `db` con `healthcheck` para no arrancar hasta que MySQL responda. Las credenciales de BD se inyectan como variables de entorno (Spring Boot las lee automáticamente con *relaxed binding*: `SPRING_DATASOURCE_URL` → `spring.datasource.url`).

Ventaja: **NO** hace falta tocar `application.properties`. Las variables de entorno tienen prioridad sobre el archivo.

### 2.3. Construcción y ejecución local con Docker

```bash
# Construir la imagen sola
docker build -t todolist-api:latest .

# O lo más cómodo: levantar TODO con compose
docker compose up --build
```

El primer arranque tarda 1-2 minutos (descarga MySQL + descarga dependencias Maven). Cuando veas en el log:

```
Started ApirestApplication in X.XXX seconds
```

la API ya está lista. Comprobar:

```bash
# En otra terminal
curl http://localhost:8080/swagger-ui.html
```

Para parar todo:

```bash
docker compose down
```

Si quieres además **borrar los datos de MySQL** (empezar de cero):

```bash
docker compose down -v
```

---

## 3. Publicación de la imagen en Docker Hub

### 3.1. Crear cuenta

1. Ir a [https://hub.docker.com](https://hub.docker.com).
2. **Sign Up** → confirma el email.
3. Anotar el **username** (será parte de la ruta de la imagen).

### 3.2. Login desde la terminal

```bash
docker login
# Usuario: <tu_usuario>
# Contraseña: <tu_contraseña o Personal Access Token>
```

> Recomendado: usar un **Personal Access Token** (Account Settings → Security → New Access Token) en vez de la contraseña real.

### 3.3. Etiquetar y subir la imagen

Reemplaza `jonatanyuguero` por tu usuario real de Docker Hub:

```bash
# Etiquetar la imagen local con el formato usuario/imagen:tag
docker tag todolist-api:latest jonatanyuguero/todolist-api:1.0.0

# Subirla
docker push jonatanyuguero/todolist-api:1.0.0

# Subir también la etiqueta "latest" para que las plataformas la encuentren
docker tag todolist-api:latest jonatanyuguero/todolist-api:latest
docker push jonatanyuguero/todolist-api:latest
```

### 3.4. Documentar la imagen

Entra en Docker Hub → tu repositorio `todolist-api` → pestaña **Overview** → edita la descripción con:

```markdown
# TodoList API REST

API REST para gestión de tareas con autenticación Basic Auth y roles ADMIN/GESTOR/USER.

## Variables de entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL de MySQL | `jdbc:mysql://db:3306/todolist?...` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la BD | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la BD | `rootpass` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Política de schema | `update` |

## Puerto

Expone el `8080`.

## Endpoints clave

- `/swagger-ui.html` — documentación interactiva
- `/auth/register` — registro público
- `/tasks`, `/categories`, `/tags`, `/users/me` — recursos protegidos (Basic Auth)
```

---

## 4. Despliegue en Railway (opción recomendada)

Railway permite usar **Docker Compose** desde un repositorio de GitHub, lo que casa perfectamente con este proyecto.

### 4.1. Preparativos

1. Subir el proyecto a un repositorio de **GitHub** (público o privado).
2. Asegurarse de que el `docker-compose.yml` está en la raíz.
3. Crear cuenta en [https://railway.com](https://railway.com) (mejor con login de GitHub).

### 4.2. Crear el proyecto

1. Dashboard → **New Project** → **Deploy from GitHub repo**.
2. Seleccionar el repositorio del TodoList.
3. Railway detecta el `docker-compose.yml` y crea ambos servicios (`app` y `db`).
4. En el servicio **app**, ir a **Settings** → **Networking** → **Generate Domain** para obtener una URL pública tipo `https://todolist-api-production.up.railway.app`.

### 4.3. Variables de entorno

Si Railway no las hereda automáticamente del compose, añadirlas a mano en el panel del servicio `app`:

- `SPRING_DATASOURCE_URL` → `jdbc:mysql://db:3306/todolist?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8`
- `SPRING_DATASOURCE_USERNAME` → `root`
- `SPRING_DATASOURCE_PASSWORD` → `rootpass`
- `SPRING_JPA_HIBERNATE_DDL_AUTO` → `update`

### 4.4. Probar el despliegue

```bash
curl https://<tu-dominio>.up.railway.app/swagger-ui.html
```

---

## 5. Despliegue en Render (alternativa)

Render **no soporta docker compose directamente**. Hay que crear los servicios uno por uno y conectarlos por variables de entorno.

### 5.1. Crear cuenta

[https://render.com](https://render.com) → registro con GitHub.

### 5.2. Crear la base de datos

1. Dashboard → **New +** → **MySQL** (en plan gratuito está disponible una versión limitada; alternativa: usar **PostgreSQL** gratuito y cambiar el driver del proyecto — pero entonces hay que tocar `pom.xml` y `application.properties`).
2. Anotar las credenciales que te da Render: `host`, `port`, `database`, `username`, `password`.

### 5.3. Crear el Web Service desde la imagen Docker

1. Dashboard → **New +** → **Web Service**.
2. **Source Code:** seleccionar **Existing Image** → meter `docker.io/jonatanyuguero/todolist-api:latest`.
3. **Region:** la más cercana (Frankfurt para España).
4. **Instance Type:** Free.

### 5.4. Variables de entorno (sección Environment)

| Variable | Valor |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<host_de_render>:<port>/<db>?useSSL=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8` |
| `SPRING_DATASOURCE_USERNAME` | `<username_render>` |
| `SPRING_DATASOURCE_PASSWORD` | `<password_render>` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |

### 5.5. Deploy

Pulsar **Create Web Service**. Render descargará la imagen de Docker Hub, levantará un contenedor y le asignará un subdominio tipo `https://todolist-api.onrender.com`.

> **Aviso:** el plan gratuito de Render **hiberna** el servicio tras 15 min de inactividad. La primera petición tras la hibernación tarda 30-50 s.

---

## 6. Verificación post-despliegue

Una vez desplegado (en Railway o Render), comprobar:

```bash
# Sustituir <URL> por la URL pública
URL="https://<tu-dominio>"

# 1) Swagger debería responder con HTML
curl -I $URL/swagger-ui.html

# 2) Login de pepe (Basic Auth: pepe:12345 → cGVwZToxMjM0NQ==)
curl -u pepe:12345 $URL/users/me

# 3) Listar las tareas de pepe
curl -u pepe:12345 $URL/tasks
```

Resultados esperados:
- `200 OK` en `/swagger-ui.html`
- JSON con los datos de pepe en `/users/me`
- JSON array con sus 5 tareas semilla en `/tasks`

### 6.1. Conectar el cliente web al backend desplegado

Editar `cliente-todo/app.js`:

```javascript
const API_URL = "https://<tu-dominio>"; // antes: http://localhost:8080
```

Y subir el cliente a cualquier hosting estático gratuito (**GitHub Pages**, **Netlify**, **Vercel**).

### 6.2. CORS

Si al conectar el cliente sale un error de CORS, hay que añadir el dominio del frontend a `WebConfig.java`:

```java
config.setAllowedOrigins(List.of(
    "http://127.0.0.1:5500",
    "http://localhost:5500",
    "http://localhost:63342",
    "https://<tu-frontend>.netlify.app"   // ← AÑADIR
));
```

Recompilar, volver a hacer push a Docker Hub y redeployar.

---

## 7. Resolución de problemas comunes

### 7.1. `Connection refused` entre app y db

**Causa:** la app intenta arrancar antes de que MySQL esté listo.

**Solución:** el `healthcheck` y `depends_on: condition: service_healthy` ya lo previenen en `docker-compose.yml`. En Render hay que añadir un retraso o configurar `SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT=60000`.

### 7.2. La imagen pesa mucho

La build multi-stage ya reduce a ~200 MB (JRE Alpine). Si quieres reducir más, usar `eclipse-temurin:17-jre-alpine` + Spring Boot layered JARs (avanzado).

### 7.3. `import.sql` no se ejecuta tras el primer arranque

Es el comportamiento esperado: `INSERT IGNORE` silencia los duplicados a partir del segundo arranque. Si necesitas resembrar datos, usa:

```bash
docker compose down -v
docker compose up --build
```

(el `-v` borra el volumen de MySQL).

### 7.4. Puerto 8080 ocupado

En `docker-compose.yml` cambiar el mapeo:

```yaml
ports:
  - "9090:8080"   # 9090 en el host, 8080 dentro del contenedor
```

### 7.5. Error de codificación (caracteres raros)

El compose ya configura MySQL con `--character-set-server=utf8mb4`. Si aun así sale mal, comprobar que el JDBC URL incluya `useUnicode=true&characterEncoding=UTF-8`.

---

## Cuadro resumen de comandos

```bash
# === LOCAL CON DOCKER ===
docker compose up --build              # arrancar todo
docker compose down                    # parar
docker compose down -v                 # parar + borrar BD
docker compose logs -f app             # ver logs de la app
docker exec -it todolist-db mysql -uroot -prootpass  # entrar a MySQL

# === DOCKER HUB ===
docker login
docker tag todolist-api:latest <usuario>/todolist-api:1.0.0
docker push <usuario>/todolist-api:1.0.0

# === DESPLIEGUE ===
# Railway: conectar repo GitHub con docker-compose.yml → deploy automático
# Render: New Web Service → Existing Image → docker.io/<usuario>/todolist-api:latest
```

---

**Autor:** Jonatan Yuguero
**Proyecto:** TodoList API REST — Desarrollo Web en Entorno Servidor · CIFP La Laboral
