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
user/  game/  purchase/  library/  exception/
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

Cross-module access is declared in `module/package-info.java`:

```java
@ApplicationModule(allowedDependencies = {"user :: service", "user :: model", "game :: service", ...})
```

## Architecture conventions

- **Services** receive/return entities only
- **Controllers** receive DTOs, call mappers (`toEntity`), call services, then map responses (`toResponse`)
- **update() methods** on entities accept individual primitive fields, never DTOs
- **UserService.create()** auto-creates `Profile` + `Wallet` for new users
- **PurchaseService.create()** validates balance, deducts wallet, sets `COMPLETED`, adds games to library
- **`GlobalExceptionHandler`** at `exception/` uses `ProblemDetail` (RFC 9457); enabled via `spring.mvc.problemdetails.enabled: true`

## Database

- `.env` required locally with `DB_NAME`, `DB_USER`, `DB_PASSWORD` (gitignored; no `.env.example`)
- Volume in `compose.yaml` commented out during schema iteration; re-enable when stable
- Tables: `user`, `profile`, `wallet`, `game`, `library`, `purchase`, `purchase_item`

## Testing note

- All tests use Testcontainers (`@Import(TestcontainersConfiguration.class)`)
- Docker must be running to execute tests
- Modulith verification tests run alongside integration tests
