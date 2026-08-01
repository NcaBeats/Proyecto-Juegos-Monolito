# Proyecto-Juegos-Monolito — Agent Guide

## Stack
- **Spring Boot 4.1.0** + **Java 25**, Maven wrapper (`mvnw`)
- **Spring Modulith 2.1.0** — modular monolith
- **PostgreSQL 17** via Docker Compose; **Flyway** (`ddl-auto: none`) — `src/main/resources/db/migration/`
- **Lombok** — never write getters/setters/constructors
- **Jackson 3.x** — import `tools.jackson.databind.ObjectMapper` (NOT `com.fasterxml.jackson`)
- **Testcontainers** for integration tests (requires Docker)

## Key commands
```sh
./mvnw compile              # compile
./mvnw test -Dtest=ModulithTests   # Modulith architecture verification (no Docker needed)
./mvnw test                 # all tests — REQUIRES Docker Desktop running (Testcontainers)
./mvnw test -Dtest=SpecificTest
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,local   # dev server: `dev` (pom) + `local` (DB creds + JWT secret) + auto-starts compose.yaml Postgres
./mvnw verify               # includes integration tests
./mvnw verify -Pdependency-check   # OWASP dependency-check (downloads NVD, slow; HTML report in target/)
docker compose up -d        # dev Postgres only (reads .env)
docker compose -f compose.prod.yaml --env-file .env.prod up -d --build   # prod stack (local/ensayo)
docker compose -f compose.prod.yaml --env-file .env.prod down
```
- Prod compose is a SEPARATE project (`name: proyecto-juegos-monolito-prod`) — dev `down` never touches it, and vice versa.

## Profiles — activated EXTERNALLY (no `spring.profiles.active` in `application.yaml`)
- **dev** — via the `spring-boot-maven-plugin` `<profiles>` in `pom.xml`, so it ONLY applies to `./mvnw spring-boot:run`. Running the main class from IntelliJ requires `SPRING_PROFILES_ACTIVE=dev,local` env var in the Run Configuration.
- **local** — NOT in the pom. `./application-local.yaml` (project root, **gitignored**) supplies ALL local dev config for the app: `spring.datasource.*` (real DB creds) + `spring.application.name` + `app.jwt.secret`. Activate with `-Dspring-boot.run.profiles=dev,local` or `SPRING_PROFILES_ACTIVE=dev,local`. If `local` is missing, `${KEY_JWT_SECRET}` in `application-dev.yaml` is unresolved → boot fails (fail-closed, no shared secret). `.env` stays reserved for Docker Compose (Postgres) — it is NOT read by the app; the DB creds it holds are intentionally duplicated in `application-local.yaml` so the host-run app is self-contained.
- **test** — via `@ActiveProfiles("test")` on all 14 `@SpringBootTest` classes; `src/test/resources/application-test.yaml` supplies JWT defaults. `app.jwt.secret` must be ≥256 bits for HS256, else `KeyLengthException`. The 4 `@DataJpaTest` classes have NO profile (they don't load `@Component`, so they don't need it).
- **prod** — via `SPRING_PROFILES_ACTIVE=prod` env var (set by `compose.prod.yaml` / Render / Railway). `application-prod.yaml` has NO defaults for `KEY_JWT_SECRET`/`KEY_JWT_EXPIRATION` → app won't boot without them.
- `DataInitializer` (`config/`) is `@Profile("dev")`: seeds Minecraft/Stardew/Elden Ring + player1 (ADMIN, $200), player2 ($50), broke_player. Prod starts with an EMPTY DB and NO ADMIN (promote one via SQL).

## Auth & Security
- JWT via `spring-boot-starter-oauth2-resource-server` (Nimbus) — **no jjwt**
- HMAC-SHA256: `NimbusJwtEncoder.withSecretKey(secret).algorithm(MacAlgorithm.HS256)`; `@Value("${app.jwt.secret}")`
- `Role` enum (USER/ADMIN), default USER, assigned in `UserService.create()`
- `JwtAuthenticationConverter` maps claim `"role"` → `ROLE_<role>`; tokens without claim → `ROLE_null`
- `server.port: ${PORT:8080}` and `/actuator/health` is `permitAll` (Render/Railway health checks)

Filter chain (`security/config/SecurityConfig.java`):
```
POST /api/v1/auth/logout        → authenticated (declared BEFORE the auth/** permitAll)
/api/v1/auth/**                  → permitAll
/actuator/health                 → permitAll
POST/PUT/DELETE /api/v1/games/** → ADMIN only
GET/POST /api/v1/users          → ADMIN only (exact paths; `/users/me` falls through to authenticated)
PUT /api/v1/wallet               → ADMIN only
anyRequest                        → authenticated (any role)
```

### Token revocation (M2 — token versioned, no DB per request)
- Tokens carry claim `ver` = `user.tokenVersion` (`JwtService.generateAccessToken`). `POST /api/v1/auth/logout` and `PUT /api/v1/users/password` both call `UserService` to bump `token_version` in DB **and** in `TokenVersionCache` (base package, `ConcurrentHashMap` + TTL `app.jwt.token-version-cache-ttl:60000`) → all previously issued tokens for that user become invalid immediately.
- `TokenVersionValidatingJwtAuthenticationConverter` (wraps the role converter) compares `ver` vs cached version on every authenticated request: in-memory lookup only (no DB). On cache-miss it loads the version from DB once and caches; missing `ver` claim / unknown user → treated as version 0 (preserves prior stateless contract). Mismatch → `BadCredentialsException` → 401.
- **Single-instance only**: the cache is in-JVM. `compose.prod.yaml` / Render run 1 replica → OK. If scaled to N replicas, swap `TokenVersionCache` for Redis (version is already in DB; the swap is localized).

### Security hardening (OWASP Top 10 audit — 2026-08)
Implemented:
- **H1** POST `/api/v1/users` → ADMIN only (was permitAll — anyone could create an admin-role user)
- **H3** password min 8 chars (`@Size(min=8)` in `RegisterRequest` + `UserRequestCreate`)
- **M1/L1/L2** `GlobalExceptionHandler` returns generic details ("Resource not found.", "Bad request.") and `sanitize()` strips `\r\n` from all logged values; `UserService.update` logs sanitized username
- **M3** optional OWASP plugin: `./mvnw verify -Pdependency-check` (`org.owasp:dependency-check-maven:12.1.0`, `failBuildOnCVSS > 7`, HTML report in `target/`)
Already solid, no action: BCrypt, HS256 Nimbus (no alg-confusion), no raw SQL/@Query, Bean Validation, SpringDoc/actuator off in prod, no SSRF surface, generic 500s

Known debt (explicitly deferred, not urgent):
- **H2** no rate limiting on `/api/v1/auth/login` (brute-force surface) — add resilience4j / bucket4j before public exposure

### Purchase idempotency (L3 — Idempotency-Key)
- `POST /api/v1/purchases` **requires** the `Idempotency-Key` header (missing → 400 via `MissingRequestHeaderException`). `PurchaseService.create(userId, key, items)` looks up `purchaseRepository.findByUser_IdAndIdempotencyKey` first and replays the existing purchase (no wallet deduction, no library re-add) on a match; otherwise creates with `idempotency_key` set.
- Guard: `purchase` has `UNIQUE (user_id, idempotency_key)` (in `V1.0.0__init_schema.sql`) — a concurrent same-key race makes the loser hit `DataIntegrityViolationException` → mapped to **409** (no double charge possible).

### BOLA conventions
- **No user IDs in paths** — self-scoped endpoints `/api/v1/wallet`, `/api/v1/profile`, `/api/v1/library`, `/api/v1/purchases`, `/api/v1/users/me`; user id comes from JWT
- Ownership enforced in controllers via `SecurityContext.getCurrentUserId()` (injected bean, not `@AuthenticationPrincipal Jwt`). `SecurityContext` lives in the BASE package (`com.app.proyectojuegosmonolito.SecurityContext`) as shared infrastructure — if it were in `security`, the account↔security cycle would break Modulith verification
- Ownership violations return **404** (not 403) to avoid leaking valid user IDs

## Architecture conventions
- **Services** receive/return entities only (`PurchaseService.create` receives `List<PurchaseLine>`, a domain record in `purchase/model`, not the HTTP DTO)
- **Controllers** receive DTOs, call mappers (`toEntityCreate`, `toLines`), call services, map responses (`toResponse`)
- Entity `update()` methods take individual primitive fields, never DTOs
- `UserService.create()` auto-creates `Profile` + `Wallet`; `PurchaseService.create()` validates balance, deducts wallet, sets `COMPLETED`, adds games to library
- `Wallet` uses `@Version` optimistic locking (migration `V1.0.0`) — concurrent stale writes throw `OptimisticLockingFailureException` → mapped to **409 Conflict** in `GlobalExceptionHandler`
- `GlobalExceptionHandler` (exception/) returns `ProblemDetail` (RFC 9457); `spring.mvc.problemdetails.enabled: true`
- Cross-module deps in `package-info.java` use Modulith named-interface syntax, e.g. `@ApplicationModule(allowedDependencies = {"account :: user-service", "game :: service", "security :: service"})`
- `ModulithTests` (root test package) enforces module boundaries via `ApplicationModules.verify()` — run it before the full suite; named interfaces require a `@NamedInterface("...")` in the sub-package's `package-info.java` (e.g. `account :: wallet-model`)

## Database
- Dev: `.env` (gitignored) with `DB_NAME`, `DB_USER`, `DB_PASSWORD` (read by Compose to create the Postgres container); the app reads the SAME creds from `application-local.yaml` (duplicated on purpose — the host-run app does not read `.env`)
- `compose.yaml`: Postgres only, host port 5432, volume `postgres-data`
- `compose.prod.yaml`: `app` + `postgres` (postgres NOT exposed to host); needs `--env-file .env.prod` (5 vars: `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `KEY_JWT_SECRET`, `KEY_JWT_EXPIRATION`); `.env.prod` gitignored
- Tables: `user` (has `role varchar(10)` + `token_version integer`), `profile`, `wallet` (has `version bigint` for optimistic locking), `game`, `library`, `purchase` (has `idempotency_key` + `UNIQUE (user_id, idempotency_key)`), `purchase_item`
- **Migrations**: ALL schema lives in a single file `V1.0.0__init_schema.sql` (wallet `version` and user `token_version` are columns inside the CREATE TABLEs). If an existing dev DB already applied an older split (`V1.1.0`), Flyway's checksum will fail → reset with `docker compose down -v`

## Testing
- All `@SpringBootTest` classes: Testcontainers (`@Import(TestcontainersConfiguration.class)` → `postgres:17-alpine`) + `@ActiveProfiles("test")`
- Docker Desktop down ⇒ every context load fails ("Could not find a valid Docker environment")
- Controller ITs must mock JWT: `SecurityMockMvcRequestPostProcessors.jwt()` with `.subject(userId)`; admin endpoints need `.jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))`
- Controller ITs are `@Transactional`, which hides lazy-loading bugs — the real code runs with `spring.jpa.open-in-view: false`, so **never access lazy associations in a mapper/controller after the service returns** without `@EntityGraph`/fetch join

## Deployment (Render/Railway-ready)
- `Dockerfile`: multi-stage — build `maven:3.9-eclipse-temurin-25`, runtime `eclipse-temurin:25-jre`, `ENTRYPOINT ["java", "-jar", "app.jar"]`
- `.dockerignore` excludes `target/`, `.idea`, `.git`, `.env*`
- Cloud env vars: `SPRING_PROFILES_ACTIVE=prod`, `KEY_JWT_SECRET`, `KEY_JWT_EXPIRATION`, `SPRING_DATASOURCE_URL=jdbc:postgresql://<host>/<db>?sslmode=require` (Render requires SSL), `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` — these override the base `localhost` datasource in `application.yaml`
