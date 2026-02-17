# Project Knowledge

## What This Is
Multi-tenant salon management REST API — Spring Boot 4.0.2, Java 25, PostgreSQL. Salon owners manage salons/services/staff/bookings; customers browse and book appointments.

## Key Directories
- `src/main/java/com/panda/salon_mgt_backend/` — all source code
  - `controllers/` — REST endpoints (`@RestController`)
  - `services/` — business logic interfaces; `services/impl/` — implementations; `services/analytics/` — dashboard analytics
  - `repositories/` — Spring Data JPA repositories
  - `models/` — JPA entities (`User`, `Salon`, `Services`, `Booking`, `Role`, `RefreshToken`) and enums
  - `payloads/` — request/response DTOs (Java records)
  - `security/` — Spring Security config, JWT filter, cookie service, `UserDetailsImpl`
  - `exceptions/` — `@RestControllerAdvice` global handler + custom exceptions
  - `configs/` — ModelMapper bean, OpenAPI/Swagger config
  - `utils/` — `AuthUtils` (extracts current user from SecurityContext)
- `src/main/resources/` — `application.yaml` (activates `dev` profile), `application-dev.yaml` (DB, JWT, CORS config)

## Commands
```bash
./mvnw spring-boot:run          # Run the app (dev profile)
./mvnw clean install             # Build + run tests
./mvnw test                      # Run tests only
./mvnw compile                   # Compile only (fast check)
```

## Tech Stack
- **Spring Boot 4.0.2** (WebMVC, Security, Data JPA, Validation)
- **PostgreSQL** — `jdbc:postgresql://localhost:5432/salon_management`
- **JWT** — jjwt 0.13.0 (access token in response body, refresh token in HttpOnly cookie)
- **ModelMapper 3.2.4** — entity ↔ DTO mapping
- **Springdoc OpenAPI 2.8.9** — Swagger UI at `/swagger-ui.html`
- **Lombok** — `@Data`, `@Builder`, `@NoArgsConstructor`, etc.
- **Maven** wrapper (`./mvnw`)

## Conventions & Patterns
- **Layered architecture:** Controller → Service interface → ServiceImpl → Repository
- **Tenant isolation:** All data access scoped to authenticated user via `AuthUtils.getCurrentUser()`. No client-supplied salon IDs trusted — salon always resolved from `salonRepository.findByOwner(currentUser)`.
- **DTOs are Java records** in `payloads/` package. Requests use `@Valid` Jakarta validation annotations.
- **Role promotion:** Users start as `ROLE_USER`, promoted to `ROLE_SALON_ADMIN` on salon creation. Roles: `ROLE_USER`, `ROLE_SALON_ADMIN`, `ROLE_SUPER_ADMIN`.
- **Token flow:** Login returns access token in body + refresh token in HttpOnly cookie. Refresh endpoint rotates both tokens (old refresh token deleted).
- **Error handling:** `GlobalExceptionHandler` maps custom exceptions → structured `ErrorResponse` with HTTP status codes (404, 409, 401).
- **Soft deactivation:** Staff/services use `active` flag rather than deletion. Deactivation blocked if entity has future bookings.
- **Booking conflict detection:** `AvailabilityServiceImpl` checks overlapping time slots before confirming bookings.

## Gotchas
- `open-in-view: false` — no lazy loading outside transactions; use fetch joins or explicit queries.
- JWT secret is hardcoded in dev config — must set `JWT_SECRET` env var in production.
- `ddl-auto: update` — Hibernate manages schema in dev; use migrations for production.
- `Services` entity name collides with Java conventions — it's the salon service offering entity, not a Spring service.
- CORS configured for `http://localhost:5173` (Vite frontend) via `app.cors.front-end-url` property.
