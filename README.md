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
| API Docs      | Springdoc OpenAPI (Swagger UI)      |
| Build Tool    | Maven                               |
| Billing       | Stripe Integration                  |

## Prerequisites

- Java 25
- PostgreSQL running on `localhost:5432` with a database named `salon_management`
- Default credentials: `postgres` / `123` (configurable in `application-dev.yaml`)

## Getting Started

```bash
# Run the application (dev profile)
./mvnw spring-boot:run

# Build JAR
./mvnw clean package -DskipTests

# Build and run tests
./mvnw clean install

# Run tests only
./mvnw test
```

The API starts at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.

## Quick Start with Docker

```bash
# Build Docker image
docker build -t salon-mgt-backend .

# Run with docker-compose (app + PostgreSQL)
docker-compose up -d
```

## Project Structure

```
src/main/java/com/panda/salon_mgt_backend/
├── controllers/       # REST endpoints
├── services/          # Business logic (interface + impl)
│   ├── impl/         # Service implementations
│   └── analytics/     # Dashboard analytics, billing, forecasts
├── repositories/     # Spring Data JPA repositories
├── models/           # JPA entities & enums
├── payloads/         # Request/response DTOs (Java records)
├── security/         # Spring Security, JWT, CORS
├── exceptions/       # Global error handling
├── configs/          # ModelMapper, OpenAPI, billing, crons
└── utils/            # Auth utilities, tenant context
```

## Features

### Core Features
- **Multi-tenant Architecture** - Strict tenant isolation via owner resolution
- **JWT Authentication** - Access tokens (10 min) + refresh tokens (24 hr) with rotation
- **Role-Based Access Control** - USER, STAFF, SALON_ADMIN, SUPER_ADMIN
- **Salon Management** - Create/update salon with automatic role upgrade
- **Service Management** - CRUD with activation guards
- **Staff Management** - Staff assignment to services, activation controls
- **Booking System** - Full lifecycle with conflict detection
- **Availability Calculation** - Smart time slot generation

### Billing & Subscriptions
- **Subscription Plans** - FREE, PRO, PREMIUM with feature limits
- **Stripe Integration** - Payment processing, webhooks, customer portal
- **Plan Limits** - Staff count, services, bookings per plan
- **Billing Metrics** - Revenue tracking, conversion metrics

### Analytics & Intelligence
- **Dashboard Analytics** - Booking trends, revenue trends
- **Leaderboards** - Top staff and services by completions
- **Forecasting** - Predictive booking forecasts
- **Revenue Timeline** - Daily revenue aggregation

### Audit & Compliance
- **Audit Logging** - Structured logs for billing lifecycle events
- **Scheduled Jobs** - Subscription expiry, payment reconciliation

### API Endpoints

| Area           | Base Path              | Auth         | Description                          |
| -------------- | ---------------------- | ------------ | ------------------------------------ |
| Auth           | `/api/auth`            | Public/Auth  | Register, login, refresh, logout     |
| Salons         | `/api/salons`          | Auth/Admin   | Salon CRUD (owner-scoped)            |
| Services       | `/api/salons/services` | SALON_ADMIN  | Service management                   |
| Staff          | `/api/salons/staff`    | SALON_ADMIN  | Staff management                     |
| Bookings       | `/api/bookings`        | Auth         | Booking lifecycle & availability     |
| Analytics      | `/api/analytics`       | SALON_ADMIN  | Trends & leaderboards                |
| Public         | `/api/public`          | Public       | Browse salons & services             |
| Billing        | `/api/billing`         | Auth         | Subscriptions, payments, portal      |
| Plans          | `/api/plans`           | SALON_ADMIN  | Plan management                      |
| Admin          | `/api/admin`           | SUPER_ADMIN  | Platform-level control               |

## Roles

| Role               | Capabilities                                            |
| ------------------ | ------------------------------------------------------- |
| `ROLE_USER`        | Browse salons, book appointments, view own bookings     |
| `ROLE_STAFF`       | View assigned bookings, complete bookings, mark no-shows|
| `ROLE_SALON_ADMIN` | Full salon management: services, staff, bookings, analytics, billing |
| `ROLE_SUPER_ADMIN` | Platform-level control (`/api/admin/**`)                |

## Configuration

The app uses Spring profiles. The active profile is `dev` by default.

### Environment Variables (Production)

| Variable                  | Purpose                    | Default                    |
| ------------------------- | -------------------------- | -------------------------- |
| `SPRING_PROFILES_ACTIVE`  | Environment                | `dev`                      |
| `DB_URL`                 | PostgreSQL connection URL  | -                          |
| `DB_USERNAME`            | Database username          | -                          |
| `DB_PASSWORD`            | Database password          | -                          |
| `JWT_SECRET`             | HMAC-SHA signing key       | Hardcoded dev secret       |
| `JWT_ACCESS_TTL_SECONDS` | Access token lifetime      | `600` (10 min)             |
| `JWT_REFRESH_TTL_SECONDS`| Refresh token lifetime     | `86400` (24 hr)            |
| `FRONTEND_URL`           | CORS allowed origin        | `http://localhost:5173`    |
| `STRIPE_SECRET_KEY`      | Stripe secret key          | -                          |
| `STRIPE_WEBHOOK_SECRET`  | Stripe webhook secret      | -                          |

## Subscription Plans

| Feature           | FREE       | PRO        | PREMIUM    |
| ----------------- | ---------- | ---------- | ---------- |
| Max Staff         | 2          | 5          | Unlimited  |
| Max Services      | 5          | 20         | Unlimited  |
| Max Bookings/Month| 50         | 200        | Unlimited  |
| Analytics         | Basic      | Advanced   | Full       |
| Smart Alerts      | No         | Yes        | Yes        |
| Price/Month       | Free       | ₹999       | ₹1999      |

## Architecture Highlights

- **Tenant Isolation** - No client-supplied salon IDs trusted for admin operations
- **Stateless JWT** - Access tokens with refresh token rotation and revocation
- **Soft Deactivation** - Staff/services use active flags with dependency guards
- **Conflict Detection** - Prevents double-booking at database level
- **Fetch Joins** - Eliminates N+1 queries on critical paths

## Challenges & Fixes

| Challenge                          | Fix/Approach                                              |
| ---------------------------------- | --------------------------------------------------------- |
| Lombok annotation processing       | Added annotationProcessorPaths in maven-compiler-plugin   |
| Multi-tenant data isolation        | Owner-based resolution, no client-supplied IDs           |
| Booking conflicts                 | Database-level overlap detection query                    |
| Token replay attacks               | JTI-based refresh token rotation with revocation           |
| Memory on 1GB RAM server           | Multi-stage Docker build, JVM heap tuning (-Xmx256m)     |

## License

Private — all rights reserved.
