# Salon Management Backend

A multi-tenant salon management REST API built with **Spring Boot 4.0.2** and **Java 25**. Salon owners manage their salon, services, staff, and bookings while customers browse salons and book appointments.

## Tech Stack

| Layer         | Technology                          |
| ------------- | ----------------------------------- |
| Framework     | Spring Boot 4.0.2 (Java 25)        |
| Web           | Spring WebMVC                       |
| Security      | Spring Security + JWT (jjwt 0.13.0) |
| Persistence   | Spring Data JPA + Hibernate         |
| Database      | PostgreSQL                          |
| Validation    | Jakarta Bean Validation             |
| Object Mapping| ModelMapper 3.2.4                   |
| API Docs      | Springdoc OpenAPI (Swagger UI) 2.8.9|
| Build Tool    | Maven                               |

## Prerequisites

- Java 25
- PostgreSQL running on `localhost:5432` with a database named `salon_management`
- Default credentials: `postgres` / `123` (configurable in `application-dev.yaml`)

## Getting Started

```bash
# Run the application (dev profile)
./mvnw spring-boot:run

# Build and run tests
./mvnw clean install

# Run tests only
./mvnw test

# Compile only (fast check)
./mvnw compile
```

The API starts at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## Project Structure

```
src/main/java/com/panda/salon_mgt_backend/
├── controllers/       # REST endpoints
├── services/          # Business logic (interface + impl)
│   ├── impl/          # Service implementations
│   └── analytics/     # Dashboard analytics
├── repositories/      # Spring Data JPA repositories
├── models/            # JPA entities & enums
├── payloads/          # Request/response DTOs (Java records)
├── security/          # Spring Security, JWT, CORS
├── exceptions/        # Global error handling
├── configs/           # ModelMapper, OpenAPI config
└── utils/             # Auth utilities
```

## Architecture

The application follows a **layered architecture**: Controller → Service → Repository → PostgreSQL, with a dedicated security filter chain for JWT authentication.

Key design principles:

- **Tenant isolation** — All data access is scoped to the authenticated user. No client-supplied salon IDs are trusted for admin operations.
- **Role-based access** — Users start as `ROLE_USER` and are promoted to `ROLE_SALON_ADMIN` upon salon creation.
- **JWT authentication** — Access tokens (10 min) in response body, refresh tokens (24 hr) in HttpOnly cookies with rotation and revocation support.
- **Soft deactivation** — Staff and services use an `active` flag rather than deletion, with guards to prevent deactivating resources that have future bookings.

See [architecture.md](architecture.md) for the full architecture documentation.

## API Overview

| Area           | Base Path              | Auth         | Description                          |
| -------------- | ---------------------- | ------------ | ------------------------------------ |
| Auth           | `/api/auth`            | Public/Auth  | Register, login, refresh, logout     |
| Salons         | `/api/salons`          | Auth/Admin   | Salon CRUD (owner-scoped)            |
| Services       | `/api/salons/services` | SALON_ADMIN  | Service management                   |
| Staff          | `/api/salons/staff`    | SALON_ADMIN  | Staff management                     |
| Bookings       | `/api/bookings`        | Auth         | Booking lifecycle & availability     |
| Analytics      | `/api/analytics`       | SALON_ADMIN  | Trends & leaderboards                |
| Public         | `/api/public`          | Public       | Browse salons & services             |

## Roles

| Role               | Capabilities                                            |
| ------------------ | ------------------------------------------------------- |
| `ROLE_USER`        | Browse salons, book appointments, view own bookings     |
| `ROLE_STAFF`       | View assigned bookings, complete bookings, mark no-shows|
| `ROLE_SALON_ADMIN` | Full salon management: services, staff, bookings, analytics |
| `ROLE_SUPER_ADMIN` | Platform-level control (`/api/admin/**`)                |

## Configuration

The app uses Spring profiles. The active profile is `dev` by default.

Key environment variables for production:

| Variable                  | Purpose                    | Default                    |
| ------------------------- | -------------------------- | -------------------------- |
| `JWT_SECRET`              | HMAC-SHA signing key       | Hardcoded dev secret       |
| `JWT_ACCESS_TTL_SECONDS`  | Access token lifetime      | `600` (10 min)             |
| `JWT_REFRESH_TTL_SECONDS` | Refresh token lifetime     | `86400` (24 hr)            |
| `FRONTEND_URL`            | CORS allowed origin        | `http://localhost:5173`    |

## License

Private — all rights reserved.
