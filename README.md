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

> **Configuración:** todas las variables viven en `.env` (gitignored). Copiá `.env.example`
> y completalo: ahí está documentada cada variable, qué hace y de dónde sale su valor
> en producción.

## Variables de entorno

Los nombres son **los mismos** en local y en producción; lo que cambia es el valor. El prefijo
indica quién es el dueño: `SPRING_*` el framework, `APP_*` la aplicación, y sin prefijo un
proveedor externo.

### Desarrollo local — `.env` (11 variables)

| Variable | Para qué |
|----------|----------|
| `SPRING_DATASOURCE_URL` | Conexión a Postgres. Local: tu máquina. Docker: `compose.yaml` la sobrescribe con el host `postgres` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de Postgres |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de Postgres |
| `APP_JWT_SECRET` | Clave de firma de los JWT (HS256, mínimo 256 bits) |
| `APP_JWT_EXPIRATION` | Vigencia del token en ms (default `3600000`) |
| `CLOUDINARY_URL` | **Legacy, ya no se usa**: todo el media (imágenes + trailers) vive en Cloudflare R2. Se conserva por compatibilidad |
| `R2_ACCOUNT_ID` | Cuenta de Cloudflare R2 (trailers) |
| `R2_ACCESS_KEY_ID` | Access Key de R2 |
| `R2_SECRET_ACCESS_KEY` | Secret Key de R2 |
| `R2_BUCKET` | Bucket de R2 (default `trailers`) |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes del navegador permitidos: separados por comas y **sin barra final** |

Tres variables **no** van en `.env` porque no son configuración sino banderas de arranque:
`SPRING_PROFILES_ACTIVE` (la pone `compose.yaml` o el plugin de Maven), `APP_SEED_ENABLED`
(el perfil `dev` lo fuerza a `true`) y `R2_PUBLIC_BASE_URL` (vive como default en
`application.yaml`, porque no es secreta ni cambia entre entornos).

`APP_CORS_ALLOWED_ORIGINS` acepta una lista explícita, nunca `*`: `allowCredentials` está
activo y el navegador rechaza esa combinación.

### Producción — panel de Render (13 variables)

El panel de Render **es** el almacén de secretos: los valores de producción no van en ningún
archivo del repo. De las 11 de `.env`, solo 2 conservan el mismo valor
(`APP_JWT_EXPIRATION` y `R2_BUCKET`); 8 lo cambian seguro, y una no
(`R2_ACCOUNT_ID`) depende de si la rotación de keys fue dentro de la misma cuenta.

| Variable | De dónde sale el valor |
|----------|----------------------|
| `SPRING_PROFILES_ACTIVE` | Literal `prod` |
| `SPRING_DATASOURCE_URL` | Panel de la DB → *Internal DB URL*, en formato JDBC |
| `SPRING_DATASOURCE_USERNAME` | Panel de la DB |
| `SPRING_DATASOURCE_PASSWORD` | Panel de la DB. **No reutilizar la de local** |
| `APP_JWT_SECRET` | Generado por vos. **No reutilizar la de local** |
| `APP_JWT_EXPIRATION` | `3600000` |
| `APP_CORS_ALLOWED_ORIGINS` | Literal: el dominio de Vercel |
| `APP_SEED_ENABLED` | `true` siembra catálogo y admin de arranque; `false` deja la DB vacía |
| `CLOUDINARY_URL` | Dashboard de Cloudinary. **No reutilizar la de local** |
| `R2_ACCOUNT_ID` | Dashboard de Cloudflare. Igual salvo que la rotación fuera a otra cuenta |
| `R2_ACCESS_KEY_ID` | Dashboard de Cloudflare. Nuevo junto con el secret |
| `R2_SECRET_ACCESS_KEY` | Dashboard de Cloudflare. **No reutilizar la de local** |
| `R2_BUCKET` | `trailers` (tiene default en el código) |

Las cuatro marcadas son secretos: **no pueden reutilizar el valor de local**. Si se filtran
las claves de esta máquina, quien las tenga puede firmar tokens de ADMIN, entrar a la base o
borrar la media de producción.

`application-prod.yaml` declara el datasource, el JWT y el CORS **sin default**, así que una
variable faltante hace fallar el arranque con un mensaje que la nombra. En cambio
`CLOUDINARY_URL` y las de R2 tienen default vacío: si faltan, la app **arranca igual** y
falla recién al subir el primer archivo. Revisá los logs del primer deploy, no solo el health check.

> Con `APP_SEED_ENABLED=true` el seed corre **una sola vez**: `DataInitializer` está guardado
> con `if (gameService.count() == 0)`, así que reiniciar el contenedor no lo vuelve a ejecutar
> ni choca con el `UNIQUE` del email. Es seguro dejarlo activo en Render.
>
> Ojo igual con el admin de arranque: `player1@gmail.com` / `pass123` están hardcodeados en
> `DataInitializer.java` y son públicos en el repo. **Cambiá esa contraseña apenas el primer
> deploy esté arriba, antes de compartir la URL.** El endpoint `PUT /api/v1/users/password`
> también invalida los tokens ya emitidos de ese usuario.

## Media: Cloudflare R2 (todas las imágenes y trailers)

Todo el media del catálogo vive en el bucket R2 **`trailers`**, con claves agrupadas por el **slug
canonical** del juego (derivado del nombre, p. ej. *Assassin's Creed Shadows* → `assassin-s-creed-shadows`):

| Asset | Key |
|-------|-----|
| Card de juego | `{slug}/card/{archivo}` |
| Banner | `{slug}/banner/{archivo}` |
| Galería | `{slug}/gallery/{archivo}` |
| Trailer | `{slug}/trailer.mp4` |
| Portada de blog | `blogs/{slug}/cover.{ext}` |

La URL pública se resuelve contra `R2_PUBLIC_BASE_URL` (default en `application.yaml`, no va en `.env`).
Los uploads **no pasan por el servidor**: el backend **presignea** la URL y el navegador sube directo a R2
(presign de imagen `POST /api/v1/games/media/image/presign` y de video `POST /api/v1/games/media/video/presign`).
El frontend guarda la `publicPath` resultante; `CLOUDINARY_URL` ya no se usa.

### Runner de consolidación (`ImageMigrationRunner`)

Componente en `config/` — temporal pero **reutilizable** (no se elimina). Se activa cuando
`app.migrate.images=true` y deja el catálogo cuadrado con R2 de forma **idempotente**, sin importar
cuántas veces corra:

1. **Imágenes residuales de Cloudinary** → las trae desde Cloudinary al bucket R2 y reescribe la URL persistida
   (juegos: card/banner/galería; blog: cover). Mapeo: `imageUrl→{slug}/card/{archivo}`,
   `bannerUrl→{slug}/banner/{archivo}`, galería→`{slug}/gallery/{archivo}`, blog→`blogs/{slug}/cover.{ext}`.
2. **Trailers en slugs antiguos** → copia server-side (S3 `CopyObject`) a `{slug}/trailer.mp4` y
   reescribe `video_url` (caso real: la DB apuntaba los trailers a `gta-v`, `ac-shadows`… mientras las
   imágenes ya usaban el slug completo; consolidado el 2026-09-30).

El perfil `dev` lo activa por defecto (`app.migrate.images: ${APP_MIGRATE_IMAGES:true}`); en `test` no
existe. En producción se corre **una sola vez** con la guía de abajo.

### Guía: correr la consolidación en producción (una sola vez)

1. En el panel de Render agregá la variable `APP_MIGRATE_IMAGES=true` (bandera de arranque como
   `APP_SEED_ENABLED`; el binding relajado la resuelve sin tocar código).
2. Deploy (Manual Deploy → *Deploy latest image*, o un push). El runner corre al arrancar.
3. Verificá — los logs del runner son `INFO` y prod loguea `WARN`, así que **validá por SQL y por URL**, no por logs:
   ```sql
   -- Debe devolver 0
   SELECT count(*) FROM game
   WHERE image_url LIKE '%res.cloudinary.com%'
      OR banner_url LIKE '%res.cloudinary.com%'
      OR video_url LIKE '%res.cloudinary.com%';
   SELECT count(*) FROM game_image WHERE url LIKE '%res.cloudinary.com%';
   SELECT count(*) FROM blog WHERE cover_image LIKE '%res.cloudinary.com%';
   -- Todos los video_url deben apuntar al slug canonical ({slug}/trailer.mp4):
   SELECT id, video_url FROM game ORDER BY id;
   ```
   Y un HEAD sobre algunos trailers para confirmar 200 + `video/mp4`, p. ej.
   `curl -I https://<R2_PUBLIC_BASE_URL>/assassin-s-creed-shadows/trailer.mp4`.
4. Cuando esté consistente, **quitá `APP_MIGRATE_IMAGES`** del panel (o ponela en `false`) y re-deployá
   para que el runner no vuelva a ejecutarse en cada reinicio.
5. Los objetos huérfanos del bucket (`{slug-viejo}/trailer.mp4`, `.gitkeep`, marcadores de carpeta
   vacíos) se borran a mano desde la consola de R2 o la API, **después** de confirmar que la DB ya no
   los referencia.

> **Si un arranque local devuelve `401 Unauthorized` contra R2**, hay una variable de entorno del SO
> (`R2_ACCESS_KEY_ID`/`R2_SECRET_ACCESS_KEY`) pisando a `.env`: Spring resuelve las env vars con
> prioridad sobre el import de `.env`. Limpiala antes de levantar:
> ```powershell
> Remove-Item Env:R2_ACCESS_KEY_ID, Env:R2_SECRET_ACCESS_KEY
> ./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
> ```

## Tests

Los tests de integración usan **Testcontainers** (levantan un Postgres en Docker sobre la marcha).
Requisito: **Docker Desktop corriendo**. No hace falta definir ninguna variable: el perfil `test`
trae valores ficticios para JWT, Cloudinary y R2, y el datasource lo construye el contenedor.

```bash
# PowerShell
.\mvnw.cmd clean test

# Bash / Linux / macOS
./mvnw clean test
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

El sistema carga automáticamente **44 juegos, 9 categorías, 3 usuarios base y 24 estudios (`VENDEDOR`)**
al primer arranque con la base vacía.

### Usuarios

| Email | Contraseña | Rol | Wallet |
|-------|-----------|-----|--------|
| `player1@gmail.com` | `pass123` | ADMIN | $200 |
| `player2@gmail.com` | `pass123` | CLIENTE | $50 |
| `broke@gmail.com` | `pass123` | CLIENTE | $0 |

Además se crean 24 estudios con rol `VENDEDOR` (p. ej. `ubisoft@gmail.com`, `microsoft@gmail.com`,
`valve@gmail.com`, …) asociados a los juegos como "seller".

### Roles

| Rol | Descripción |
|-----|-------------|
| `ADMIN` | Acceso total al sistema |
| `VENDEDOR` | Visualiza productos y órdenes (solo lectura) |
| `CLIENTE` | Solo accede a la tienda |

### Categorías

Action, Adventure, RPG, Shooter, Fighting, Open World, Horror, Simulation, Racing.

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
- `POST /api/v1/games/media/video/presign` — URL firmada para subir el trailer directo a R2 (ADMIN)
- `POST /api/v1/games/media/image/presign` — URL firmada para subir una imagen (`{slug}/card|banner|gallery/{uuid}.{ext}`) directo a R2 (ADMIN)
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
