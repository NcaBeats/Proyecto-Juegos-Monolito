# Proyecto-Juegos-Monolito — Agent Guide

## Stack

- **Spring Boot 4.1.0** + **Java 25**, Maven wrapper (`mvnw`)
- **Spring Modulith 2.1.0** — modular monolith
- **PostgreSQL 17** via Docker Compose (`compose.yaml`)
- **Flyway** (`ddl-auto: none`) — `src/main/resources/db/migration/`
- **Lombok** — never write getters/setters/constructors
- **Testcontainers** for integration tests (requires Docker)

## Key commands

```sh
./mvnw compile              # check compilation + Modulith verification
./mvnw test                 # all tests
./mvnw test -Dtest=SpecificTest  # single test class
./mvnw spring-boot:run      # dev server (Compose starts Postgres automatically)
./mvnw verify               # includes integration tests
```

## Modules & package structure

Root: `com.app.proyectojuegosmonolito`

```
config/  exception/  game/  library/  purchase/  security/  user/
```

Each module follows the same internal layout:

```
module/
├── model/          ← entities + enums; exposed via @NamedInterface("model")
├── repository/     ← JpaRepository interfaces
├── service/        ← business logic; exposed via @NamedInterface("service")
├── dto/            ← request/response records
├── mapper/         ← @Component mappers (toEntity / toResponse)
└── controller/     ← @RestController
```

The `security/` module has no model/repository (no DB tables); its `config/` subpackage holds `SecurityConfig`, `JwtService`, etc.

Cross-module access is declared in `module/package-info.java`:

```java
@ApplicationModule(allowedDependencies = {"user :: service", "user :: model", "game :: service", ...})
```

## Auth & Security

- **JWT** via `spring-boot-starter-oauth2-resource-server` (Nimbus) — **no jjwt dependency**
- **HMAC-SHA256** using `NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build()`
- **Secret & expiration** in `application-dev.yaml` / `application-prod.yaml` as `app.jwt.secret` / `app.jwt.expiration`, sourced from env vars `KEY_JWT_SECRET` / `KEY_JWT_EXPIRATION`
- `Role` enum (`USER`, `ADMIN`) on `User` entity (`@Enumerated(EnumType.STRING)`), default `USER`, default assigned in `UserService.create()`
- `JwtAuthenticationConverter` maps JWT claim `"role"` → authority `"ROLE_<role>"` — old tokens without the claim will produce `ROLE_null`
- `DataInitializer` (`@Profile("dev")`) seeds 3 games + 3 users; `player1` gets `Role.ADMIN`, `player2` and `broke_player` get `Role.USER`

### Filter chain rules (`SecurityConfig.securityFilterChain`)

```
/api/v1/auth/**                  → permitAll
POST   /api/v1/games            → ADMIN only
PUT    /api/v1/games/**         → ADMIN only
DELETE /api/v1/games/**         → ADMIN only
GET    /api/v1/users            → ADMIN only
PUT    /api/v1/wallet           → ADMIN only
anyRequest                       → authenticated (any role)
```

- `GET /api/v1/games` / `GET /api/v1/games/{id}` fall through to `authenticated` — any logged-in user can browse games
- All other endpoints (profile, wallet, purchases, library) require a valid JWT but accept `USER` or `ADMIN`

### BOLA (Broken Object Level Authorization) conventions

- **No user IDs in paths or DTOs** — endpoints use `/api/v1/wallet`, `/api/v1/profile`, `/api/v1/library`, `/api/v1/purchases`, `/api/v1/users` (no `{userId}`)
- **Ownership enforced in controllers** via `SecurityContext.getCurrentUserId()` (injected bean, not `@AuthenticationPrincipal Jwt`)
- Ownership violations return `404` (not `403`) to avoid leaking valid user IDs

## Architecture conventions

- **Services** receive/return entities only
- **Controllers** receive DTOs, call mappers (`toEntityCreate`), call services, then map responses (`toResponse`)
- **update() methods** on entities accept individual primitive fields, never DTOs
- **UserService.create()** auto-creates `Profile` + `Wallet` for new users
- **PurchaseService.create()** validates balance, deducts wallet, sets `COMPLETED`, adds games to library
- **`GlobalExceptionHandler`** at `exception/` uses `ProblemDetail` (RFC 9457); enabled via `spring.mvc.problemdetails.enabled: true`

## Database

- `.env` required locally with `DB_NAME`, `DB_USER`, `DB_PASSWORD` (gitignored; no `.env.example`)
- Volume in `compose.yaml` commented out during schema iteration; re-enable when stable
- Tables: `user` (has `role varchar(10)`), `profile`, `wallet`, `game`, `library`, `purchase`, `purchase_item`

## Testing

- All tests use Testcontainers (`@Import(TestcontainersConfiguration.class)`)
- Docker must be running to execute tests
- Modulith verification tests run alongside integration tests
- **Controller ITs must mock JWT** via `SecurityMockMvcRequestPostProcessors.jwt()` with `.subject(userId)` — requests without it get 401
- **`GameControllerIT` is broken** — its `getById`, `getAll`, `create`, `update`, `delete` tests lack `with(jwt())` and will fail because all endpoints require authentication
