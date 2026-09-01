# Proyecto Juegos Monolito

Backend Spring Boot de una tienda de videojuegos con arquitectura modular (Spring Modulith).

## Requisitos

- **Docker Desktop** (para la base de datos PostgreSQL)
- **Java 25** (JDK 25+)

## Cómo levantar el proyecto

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd Proyecto-Juegos-Monolito

# 2. Levantar PostgreSQL
docker compose up -d

# 3. Compilar y ejecutar la aplicación
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

La app arranca en `http://localhost:8080`.

## Datos de prueba

El sistema carga automáticamente 24 juegos, 14 categorías y 3 usuarios de prueba.

### Usuarios

| Usuario | Contraseña | Rol | Wallet |
|---------|-----------|-----|--------|
| `player1` | `pass123` | ADMIN | $200 |
| `player2` | `pass123` | USER | $50 |
| `broke_player` | `pass123` | USER | $0 |

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

## Endpoints principales

### Auth
- `POST /api/v1/auth/login` — Iniciar sesión
- `POST /api/v1/auth/register` — Registrar usuario
- `POST /api/v1/auth/logout` — Cerrar sesión

### Games
- `GET /api/v1/games` — Listar juegos (paginado)
- `GET /api/v1/games/{id}` — Detalle de un juego
- `GET /api/v1/games/discounted` — Juegos en descuento
- `POST /api/v1/games` — Crear juego (ADMIN)
- `PUT /api/v1/games/{id}` — Actualizar juego (ADMIN)
- `DELETE /api/v1/games/{id}` — Eliminar juego (ADMIN)

### Categories
- `GET /api/v1/categories` — Listar categorías
- `POST /api/v1/categories` — Crear categoría (ADMIN)

### Users
- `GET /api/v1/users` — Listar usuarios (ADMIN)
- `GET /api/v1/users/me` — Mi perfil
- `PUT /api/v1/users/password` — Cambiar contraseña

### Wallet
- `GET /api/v1/wallet` — Ver saldo
- `PUT /api/v1/wallet` — Agregar fondos (ADMIN)

### Purchases
- `POST /api/v1/purchases` — Comprar juegos
- `GET /api/v1/purchases` — Ver historial de compras

### Library
- `GET /api/v1/library` — Ver biblioteca de juegos comprados

### Profiles
- `GET /api/v1/profile` — Ver perfil
- `PUT /api/v1/profile` — Actualizar perfil

## Autenticación

Los endpoints protegidos requieren un token JWT en el header:

```
Authorization: Bearer <token>
```

Para obtener un token, hacer login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "player1", "password": "pass123"}'
```

Los endpoints de ADMIN (POST/PUT/DELETE en juegos, usuarios, wallet) requieren el rol `ROLE_ADMIN`.

## Health check

```bash
curl http://localhost:8080/actuator/health
```
