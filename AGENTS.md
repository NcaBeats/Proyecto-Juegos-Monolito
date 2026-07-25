# Proyecto-Juegos-Monolito — Agent Guide

## Stack

- **Spring Boot 4.1.0** + **Java 25**, Maven wrapper (`mvnw`)
- **Spring Modulith 2.1.0** — modular monolith; structure code into `order.*`, `catalog.*`, `user.*` etc. sub-packages under `com.app.proyectojuegosmonolito`
- **PostgreSQL** via Docker Compose (`compose.yaml`)
- **Flyway** for schema migrations (`src/main/resources/db/migration/`)
- **Lombok** — annotations enabled; never write getters/setters/constructors manually
- **Testcontainers** for integration tests (requires Docker)

## Key commands

```sh
./mvnw test          # all tests
./mvnw test -Dtest=SpecificTest  # single test class
./mvnw spring-boot:run           # run dev server (Docker Compose starts Postgres automatically)
./mvnw verify                    # includes integration tests
```

## Database

- `.env` must exist locally with `DB_NAME`, `DB_USER`, `DB_PASSWORD` (gitignored; copy from `.env.example` or teammates)
- `src/main/resources/application.yaml`: `spring.jpa.hibernate.ddl-auto: none` — Flyway owns all schema changes
- New migrations: `src/main/resources/db/migration/V{next}__{description}.sql`
- Existing schema (`V1.0.0__init_schema.sql`): tables `user`, `profile`, `wallet`, `game`, `library`, `purchase`, `purchase_item`

## Testing

- All tests use Testcontainers (`@Import(TestcontainersConfiguration.class)`), which spins a disposable Postgres container
- Docker must be running to execute tests
- Spring Modulith verification tests go alongside integration tests

## Architecture notes

- Spring Data REST exposes JPA repositories as REST endpoints automatically (HAL+JSON)
- Security (spring-boot-starter-security) is declared but not yet configured
- Prometheus + Actuator for metrics; cached via `spring-boot-starter-cache`
- HATEOAS is enabled — prefer `EntityModel` / `CollectionModel` or Spring Data REST defaults
- No CI workflow yet (`.github/` absent)
