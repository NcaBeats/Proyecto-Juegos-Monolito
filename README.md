# Proyecto Juegos Monolito

Backend Spring Boot de una tienda de videojuegos con arquitectura modular (Spring Modulith).

## Requisitos

- **Docker Desktop** (opcionales Docker y los tests con Testcontainers)
- **Java 25** (solo si NO usás Docker)

## Cómo levantar el proyecto

### Opción 1: Solo Docker (recomendada para evaluación)

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd Proyecto-Juegos-Monolito

# 2. Levantar todo (Postgres + App)
docker compose up -d

# 3. Ver logs
docker compose logs -f app
```

La app arranca en `http://localhost:9090`. **No necesita Java instalado.**

## ⚠️ Si modificás el código o la migración, ejecutá este flujo

```bash
docker compose down -v          # detiene los contenedores Y borra el volumen postgres-data
docker compose build --no-cache # rebuildea la imagen Docker desde cero (nuevo JAR)
docker compose up -d            # arranca con DB limpia + nuevo código
docker compose logs -f app      # verificá: "Started ProyectoJuegosMonolitoApplication" + sin errores Flyway
```

**Los 3 flags son obligatorios:**

| Flag | Propósito |
|------|-----------|
| `-v` | Borra el volumen `postgres-data` (historial Flyway + schema viejo) |
| `--no-cache` | Reconstruye la imagen Docker desde cero (nuevo JAR con tu código) |
| `-d` | Corre en background (para poder hacer `logs -f app` aparte) |

**Si te falta cualquiera de los 3, el bug vuelve:** vas a ver 401 en endpoints públicos (`permitAll()`) o `Migration checksum mismatch` de Flyway. Eso pasa porque Docker reusa la imagen vieja y el JAR desactualizado choca con el schema nuevo (o con el schema viejo si solo cambiaste código).

**Si solo cambiaste código Java (sin tocar la migración):** podés sacar `-v` pero mantené `--no-cache` (necesitás el JAR nuevo).

**Para iterar rápido sin Docker:** usá `mvnw spring-boot:run` con solo Postgres en Docker — hot reload instantáneo:

```bash
docker compose up -d postgres
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### Opción 2: Docker + Java en PC (desarrollo)

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd Proyecto-Juegos-Monolito

# 2. Levantar solo PostgreSQL
docker compose up -d postgres

# 3. Compilar y ejecutar la aplicación
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

La app arranca en `http://localhost:9090` (configurable con la env var `PORT`).
**Necesita Java 25 en la PC.**

> **Env vars obligatorias** para el perfil `dev`: `R2_ACCOUNT_ID`, `R2_ACCESS_KEY_ID`
> y `R2_SECRET_ACCESS_KEY`. `CLOUDINARY_URL` trae valor por defecto en `application-dev.yaml`
> (lo mismo aplica a Docker, ya resuelto en `compose.yaml`).

## Variables de entorno

| Variable | Obligatoria | Descripción |
|----------|-------------|-------------|
| `PORT` | No | Puerto HTTP (default `9090`) |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | No | Conexión a Postgres (defaults locales) |
| `KEY_JWT_SECRET` | Sí (prod) | Secreto del JWT |
| `KEY_JWT_EXPIRATION` | No | Expiración del JWT en ms (default `3600000`) |
| `CLOUDINARY_URL` | Sí | URL de Cloudinary para imágenes |
| `R2_ACCOUNT_ID` | Sí | Cuenta Cloudflare R2 (trailers/videos) |
| `R2_ACCESS_KEY_ID` | Sí | Access Key de R2 |
| `R2_SECRET_ACCESS_KEY` | Sí | Secret Key de R2 |
| `R2_BUCKET` | No | Bucket R2 (default `trailers`) |
| `R2_PUBLIC_BASE_URL` | No | Base URL pública de R2 |

> Para **correr los tests** (Testcontainers) alcanza con valores dummy en `CLOUDINARY_URL`
> y `R2_*`: el arranque solo valida que no estén vacíos; durante los tests no se suben archivos.

## Tests

Los tests de integración usan **Testcontainers** (levantan un Postgres en Docker sobre la marcha).
Requisito: **Docker Desktop corriendo** + las env vars de arriba.

```bash
# PowerShell
$env:CLOUDINARY_URL = "cloudinary://key:secret@cloud"
$env:R2_ACCOUNT_ID = "dummy-account"
$env:R2_ACCESS_KEY_ID = "dummy"
$env:R2_SECRET_ACCESS_KEY = "dummy"
.\mvnw.cmd test

# Bash / Linux / macOS
CLOUDINARY_URL="cloudinary://key:secret@cloud" \
R2_ACCOUNT_ID="dummy" R2_ACCESS_KEY_ID="dummy" R2_SECRET_ACCESS_KEY="dummy" \
./mvnw test
```

**Nota:** el contexto Spring se comparte entre clases de test (mismo Postgres de Testcontainers).
Los tests no transaccionales persisten filas (p. ej. usuarios con `user@test.com`); por eso usan
emails únicos. Si un caso falla por "email ya existente", es un resto de una corrida anterior:
reiniciá el daemon de Docker o borrá el volumen del contenedor de test.

## Soft delete de usuarios

Los usuarios **no se borran físicamente**: `DELETE /api/v1/users/{id}` y `DELETE /api/v1/users`
hacen un *soft delete* (marcan `deleted_at` y revocan los tokens del usuario).

- El **email queda bloqueado**: como el constraint `UNIQUE` sigue vigente, no se puede
  registrar ni reasignar el email de un usuario eliminado.
- Los usuarios borrados **desaparecen** de listados, búsquedas y login (filtro `deleted_at IS NULL`).
- **No se puede eliminar una cuenta `ADMIN`** (devuelve error).
- Los juegos sí se eliminan físicamente (`DELETE /api/v1/games/{id}`), sujeto a integridad
  referencial (no se puede borrar un juego con compras/biblioteca asociadas).

## Datos de prueba

El sistema carga automáticamente **44 juegos, 14 categorías, 3 usuarios base y 25 estudios (`VENDEDOR`)**
al primer arranque con la base vacía.

### Usuarios

| Email | Contraseña | Rol | Wallet |
|-------|-----------|-----|--------|
| `player1@gmail.com` | `pass123` | ADMIN | $200 |
| `player2@gmail.com` | `pass123` | CLIENTE | $50 |
| `broke@gmail.com` | `pass123` | CLIENTE | $0 |

Además se crean 25 estudios con rol `VENDEDOR` (p. ej. `ubisoft@gmail.com`, `microsoft@gmail.com`,
`valve@gmail.com`, …) asociados a los juegos como "seller".

### Roles

| Rol | Descripción |
|-----|-------------|
| `ADMIN` | Acceso total al sistema |
| `VENDEDOR` | Visualiza productos y órdenes (solo lectura) |
| `CLIENTE` | Solo accede a la tienda |

### Categorías

Action, Adventure, RPG, Shooter, Platformer, Fighting, Open World, Sports, Indie, Stealth, Horror, Simulation, Racing, Strategy.

### Juegos de ejemplo (con precio y descuento)

| Juego | Precio original | Descuento | Precio final |
|-------|----------------|-----------|--------------|
| Minecraft | $29.99 | 0% | $29.99 |
| Stardew Valley | $14.99 | 0% | $14.99 |
| Elden Ring | $59.99 | 0% | $59.99 |
| Rocket League | $0.00 | 0% | $0.00 |
| Spider-Man Remastered | $59.99 | 40% | $35.99 |
| Grand Theft Auto V | $29.99 | 50% | $15.00 |
| Battlefield 1 | $59.99 | 75% | $15.00 |
| Assassin's Creed Shadows | $69.99 | 20% | $55.99 |
| Cyberpunk 2077 | $59.99 | 60% | $24.00 |
| Dragon Ball FighterZ | $59.99 | 70% | $18.00 |
| Mortal Kombat 11 | $49.99 | 80% | $10.00 |
| Red Dead Redemption 2 | $59.99 | 67% | $19.80 |
| The Witcher 3 | $39.99 | 85% | $6.00 |
| Halo Infinite | $59.99 | 50% | $30.00 |
| Forza Horizon 4 | $59.99 | 60% | $24.00 |
| Civilization VI | $59.99 | 75% | $15.00 |

Cada juego incluye una **descripción detallada** y **especificaciones de PC** (mínimas y recomendadas) en formato JSON con campos como `os`, `processor`, `memory`, `graphics` y `storage`.

## Endpoints principales

### Auth
- `POST /api/v1/auth/login` — Iniciar sesión
- `POST /api/v1/auth/register` — Registrar usuario
- `POST /api/v1/auth/logout` — Cerrar sesión

### Games
- `GET /api/v1/games` — Listar juegos (paginado). Acepta `?category=<nombre>` para filtrar
- `GET /api/v1/games/{id}` — Detalle de un juego
- `GET /api/v1/games/discounted` — Juegos en descuento
- `GET /api/v1/games/banners` — Juegos con banner
- `GET /api/v1/games/stats` — Estadísticas (total, activos, valor del catálogo)
- `POST /api/v1/games` — Crear juego (ADMIN)
- `PUT /api/v1/games/{id}` — Actualizar juego (ADMIN)
- `PUT /api/v1/games/{id}/video` — Subir/actualizar trailer (ADMIN, se guarda en R2)
- `DELETE /api/v1/games/{id}` — Eliminar juego (ADMIN)
- `POST /api/v1/games/{id}/image` — Subir imagen de portada (ADMIN)
- `POST /api/v1/games/{id}/banner` — Subir imagen de banner (ADMIN)

**Ejemplo de filtro por categoría:**
```bash
curl "http://localhost:9090/api/v1/games?category=Action" \
  -H "Authorization: Bearer <token>"
```

### Categories
- `GET /api/v1/categories` — Listar categorías
- `GET /api/v1/categories/{id}` — Detalle de una categoría
- `POST /api/v1/categories` — Crear categoría (ADMIN)
- `PUT /api/v1/categories/{id}` — Actualizar categoría (ADMIN)
- `DELETE /api/v1/categories/{id}` — Eliminar categoría (ADMIN)

### Manage (ADMIN / VENDEDOR)
- `GET /api/v1/manage/games` — Listar juegos gestionables
- `GET /api/v1/manage/games/{id}` — Detalle gestionable de un juego
- `GET /api/v1/manage/orders` — Listar órdenes
- `GET /api/v1/manage/orders/{id}` — Detalle de una orden

### Blogs
- `GET /api/v1/blogs` — Listar publicaciones del blog
- `GET /api/v1/blogs/{id}` — Detalle de una publicación
- `POST /api/v1/blogs` — Crear publicación (ADMIN)
- `DELETE /api/v1/blogs/{id}` — Eliminar publicación (ADMIN)

### Users
- `GET /api/v1/users` — Listar usuarios (ADMIN)
- `GET /api/v1/users/{id}` — Detalle de un usuario (ADMIN)
- `PUT /api/v1/users/{id}` — Actualizar email/rol/contraseña (ADMIN)
- `DELETE /api/v1/users/{id}` — Eliminar usuario (ADMIN, **soft delete**)
- `POST /api/v1/users` — Crear usuario (ADMIN)
- `GET /api/v1/users/me` — Mi perfil
- `PUT /api/v1/users` — Actualizar mi usuario
- `PUT /api/v1/users/password` — Cambiar contraseña
- `DELETE /api/v1/users` — Eliminar mi cuenta (**soft delete**)

### Wallet
- `GET /api/v1/wallet` — Ver mi saldo
- `PUT /api/v1/wallet` — Establecer saldo (ADMIN)
- `POST /api/v1/wallet/deposit` — Depositar fondos en mi wallet

### Purchases
- `POST /api/v1/purchases` — Comprar juegos (requiere `Idempotency-Key` header)
- `GET /api/v1/purchases` — Ver mi historial de compras
- `GET /api/v1/purchases/{id}` — Detalle de una compra

### Library
- `GET /api/v1/library` — Ver mi biblioteca de juegos comprados
- `POST /api/v1/library` — Agregar un juego a la biblioteca
- `DELETE /api/v1/library/game/{gameId}` — Quitar un juego de la biblioteca
- `DELETE /api/v1/library` — Vaciar la biblioteca

### Profiles
- `GET /api/v1/profile` — Ver mi perfil
- `GET /api/v1/profile/{userId}` — Ver perfil público de otro usuario
- `PATCH /api/v1/profile` — Actualizar mi perfil
- `PATCH /api/v1/profile/{userId}` — Actualizar perfil de otro usuario (ADMIN)

### Contacts (formulario de contacto público)
- `POST /api/v1/contacts` — Enviar mensaje de contacto (público, sin auth)
- `GET /api/v1/contacts` — Listar mensajes de contacto (ADMIN)

**Ejemplo de contacto:**
```bash
curl -X POST http://localhost:9090/api/v1/contacts \
  -H "Content-Type: application/json" \
  -d '{"name": "Juan", "email": "juan@duoc.cl", "comment": "Hola, me interesa..."}'
```

## Autenticación

Los endpoints protegidos requieren un token JWT en el header:

```
Authorization: Bearer <token>
```

Para obtener un token, hacer login:

```bash
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "player1@gmail.com", "password": "pass123"}'
```

### Permisos por rol

| Rol | Acceso |
|-----|--------|
| `ADMIN` | CRUD completo en juegos, usuarios, wallet, ver contactos |
| `VENDEDOR` | Solo lectura de juegos, categorías y compras |
| `CLIENTE` | Tienda, compras, perfil propio, enviar contacto |
| (Sin auth) | Login, registro, enviar contacto, ver juegos, ver categorías |

## Health check

```bash
curl http://localhost:9090/actuator/health
```
