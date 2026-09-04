# Proyecto Juegos Monolito

Backend Spring Boot de una tienda de videojuegos con arquitectura modular (Spring Modulith).

## Requisitos

- **Docker Desktop** (único requisito para la opción Docker)
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

La app arranca en `http://localhost:8080` (puerto configurado en `application.yaml`).
**Necesita Java 25 en la PC.**

## Datos de prueba

El sistema carga automáticamente 24 juegos, 14 categorías y 3 usuarios de prueba.

### Usuarios

| Email | Contraseña | Rol | Wallet |
|-------|-----------|-----|--------|
| `player1@gmail.com` | `pass123` | ADMIN | $200 |
| `player2@gmail.com` | `pass123` | CLIENTE | $50 |
| `broke@gmail.com` | `pass123` | CLIENTE | $0 |

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
- `POST /api/v1/games` — Crear juego (ADMIN)
- `PUT /api/v1/games/{id}` — Actualizar juego (ADMIN)
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
- `POST /api/v1/categories` — Crear categoría (ADMIN)
- `PUT /api/v1/categories/{id}` — Actualizar categoría (ADMIN)
- `DELETE /api/v1/categories/{id}` — Eliminar categoría (ADMIN)

### Users
- `GET /api/v1/users` — Listar usuarios (ADMIN)
- `POST /api/v1/users` — Crear usuario (ADMIN)
- `GET /api/v1/users/me` — Mi perfil
- `PUT /api/v1/users` — Actualizar mi usuario
- `PUT /api/v1/users/password` — Cambiar contraseña
- `DELETE /api/v1/users` — Eliminar mi cuenta

### Wallet
- `GET /api/v1/wallet` — Ver mi saldo
- `PUT /api/v1/wallet` — Establecer saldo (ADMIN)
- `POST /api/v1/wallet/deposit` — Depositar fondos en mi wallet

### Purchases
- `POST /api/v1/purchases` — Comprar juegos (requiere `Idempotency-Key` header)
- `GET /api/v1/purchases` — Ver mi historial de compras

### Library
- `GET /api/v1/library` — Ver mi biblioteca de juegos comprados

### Profiles
- `GET /api/v1/profile` — Ver mi perfil
- `GET /api/v1/profile/{userId}` — Ver perfil público de otro usuario
- `PATCH /api/v1/profile` — Actualizar mi perfil

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
