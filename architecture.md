# Salon Management Backend — Architecture

## Overview

A **multi-tenant salon management REST API** built with Spring Boot 4. The system allows salon owners to manage their salon, services, staff, and bookings while customers can browse salons and book appointments. It enforces strict tenant isolation — all data access is scoped to the authenticated user, and no client-supplied salon IDs are trusted.

---

## Tech Stack

| Layer             | Technology                                |
| ----------------- | ----------------------------------------- |
| Framework         | Spring Boot 4.0.2 (Java 25)              |
| Web               | Spring WebMVC                             |
| Security          | Spring Security + JWT (jjwt 0.13.0)       |
| Persistence       | Spring Data JPA + Hibernate               |
| Database          | PostgreSQL                                |
| Validation        | Jakarta Bean Validation                   |
| Object Mapping    | ModelMapper 3.2.4                         |
| API Documentation | Springdoc OpenAPI (Swagger UI) 2.8.9      |
| Build Tool        | Maven                                     |
| Boilerplate       | Lombok                                    |

---

## Project Structure

```
src/main/java/com/panda/salon_mgt_backend/
├── SalonMgtBackendApplication.java      # Entry point
│
├── configs/                             # Application-level configuration
│   ├── APIDocConfig.java                #   OpenAPI/Swagger metadata
│   └── ModelMapperConfig.java           #   ModelMapper bean
│
├── controllers/                         # REST API layer
│   ├── AuthController.java              #   Authentication endpoints
│   ├── SalonController.java             #   Salon CRUD (owner-scoped)
│   ├── ServicesController.java          #   Service management
│   ├── StaffController.java             #   Staff management
│   ├── BookingController.java           #   Booking lifecycle
│   ├── AnalyticsController.java         #   Dashboard analytics & trends
│   ├── PublicSalonController.java       #   Unauthenticated salon browsing
│   └── HelloController.java             #   Health check
│
├── exceptions/                          # Global error handling
│   ├── GlobalExceptionHandler.java      #   @RestControllerAdvice
│   ├── ErrorResponse.java               #   Standard error body
│   ├── ResourceNotFoundException.java   #   404
│   ├── AlreadyExistsException.java      #   409
│   ├── CanNotException.java             #   409 (business rule violation)
│   ├── DeactivateException.java         #   409 (deactivation blocked)
│   ├── InactiveException.java           #   409 (inactive resource)
│   └── RefreshTokenException.java       #   401 (token issues)
│
├── models/                              # JPA entities & enums
│   ├── User.java                        #   Users (customers, staff, admins)
│   ├── Salon.java                       #   Salon entity (1:1 with owner)
│   ├── Services.java                    #   Salon services offered
│   ├── Booking.java                     #   Appointment bookings
│   ├── Role.java                        #   Role entity
│   ├── RefreshToken.java                #   Persisted refresh tokens
│   ├── AppRole.java                     #   Role enum
│   ├── BookingStatus.java               #   Booking lifecycle states
│   ├── BookingRange.java                #   Query filter enum
│   ├── TrendRange.java                  #   Analytics time range enum
│   └── Provider.java                    #   Auth provider enum (future)
│
├── payloads/                            # DTOs (request/response records)
│   ├── TokenResponse.java               #   JWT access token response
│   ├── RefreshTokenRequest.java         #   Refresh token request body
│   ├── UserResponse.java                #   User profile DTO
│   ├── SalonCreateRequest.java          #   Salon creation/update
│   ├── SalonResponse.java               #   Salon response DTO
│   ├── PublicSalonResponse.java         #   Public salon listing DTO
│   ├── ServiceCreateRequest.java        #   Service creation (validated)
│   ├── ServiceUpdateRequest.java        #   Service update (validated)
│   ├── ServiceResponse.java             #   Service response DTO
│   ├── StaffCreateRequest.java          #   Staff creation (validated)
│   ├── StaffResponse.java               #   Staff response with services
│   ├── AssignServicesRequest.java       #   Assign services to staff
│   ├── AssignStaffRequest.java          #   Assign staff to service
│   ├── CreateBookingRequest.java        #   Booking creation
│   ├── BookingResponse.java             #   Booking response DTO
│   ├── PageResponse.java                #   Generic paginated response
│   ├── TimeSlot.java                    #   Availability slot DTO
│   ├── AdminDashboardResponse.java      #   Dashboard aggregates
│   ├── LeaderboardItemDTO.java          #   Top staff/services ranking
│   ├── UserDto.java                     #   Legacy user DTO
│   └── RoleDto.java                     #   Legacy role DTO
│
├── repositories/                        # Spring Data JPA interfaces
│   ├── UserRepository.java              #   User queries + fetch joins
│   ├── SalonRepository.java             #   Salon queries
│   ├── ServicesRepository.java          #   Service queries + projections
│   ├── BookingRepository.java           #   Complex booking queries
│   ├── RoleRepository.java              #   Role lookup
│   └── RefreshTokenRepository.java      #   Token storage
│
├── security/                            # Security infrastructure
│   ├── SecurityConfig.java              #   Filter chain + seed data
│   ├── CorsConfig.java                  #   CORS configuration
│   ├── jwt/
│   │   ├── JwtService.java              #   Token generation & validation
│   │   ├── AuthTokenFilter.java         #   JWT authentication filter
│   │   ├── AuthEntryPointJwt.java       #   401 handler
│   │   └── JwtAccessDeniedHandler.java  #   403 handler
│   ├── services/
│   │   ├── UserDetailsImpl.java         #   Spring Security UserDetails
│   │   ├── UserDetailsServiceImpl.java  #   Loads user by email
│   │   └── CookieService.java           #   Refresh token cookie mgmt
│   ├── requests/
│   │   ├── UserLoginRequest.java        #   Login payload
│   │   └── UserRegisterRequest.java     #   Registration payload
│   └── responses/
│       └── MessageResponse.java         #   Simple message response
│
├── services/                            # Business logic layer
│   ├── AuthService.java                 #   Interface
│   ├── UserService.java                 #   Interface
│   ├── SalonService.java                #   Interface
│   ├── ServicesService.java             #   Interface
│   ├── StaffService.java                #   Interface
│   ├── BookingService.java              #   Interface
│   ├── AvailabilityService.java         #   Interface
│   ├── impl/
│   │   ├── AuthServiceImpl.java         #   Auth + token rotation
│   │   ├── UserServiceImpl.java         #   User resolution
│   │   ├── SalonServiceImpl.java        #   Salon CRUD + role upgrade
│   │   ├── ServicesServiceImpl.java     #   Service management
│   │   ├── StaffServiceImpl.java        #   Staff lifecycle
│   │   ├── BookingServiceImpl.java      #   Booking lifecycle + dashboard
│   │   └── AvailabilityServiceImpl.java #   Slot calculation
│   └── analytics/
│       ├── AnalyticsService.java         #   Interface
│       ├── AnalyticsServiceImpl.java     #   Trend & leaderboard queries
│       └── TrendPointDTO.java            #   Date + value pair
│
└── utils/
    └── AuthUtils.java                   # Auth helper (resolve user)
```

---

## Layered Architecture

The application follows a classic **3-tier layered architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                     Client (Frontend)                       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼──────────────────────────────────┐
│                   Security Filter Chain                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐  │
│  │ CORS Filter │→ │AuthTokenFilter│→ │ SecurityFilterChain│  │
│  └─────────────┘  └──────────────┘  └────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Controller Layer                          │
│  REST endpoints, request validation, HTTP status mapping     │
│  No business logic — delegates everything to services        │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                     Service Layer                            │
│  Business logic, ownership checks, state transitions,        │
│  tenant isolation, role upgrades, availability calculation    │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   Repository Layer                           │
│  Spring Data JPA interfaces with JPQL queries,               │
│  fetch joins to avoid N+1, DTO projections                   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      PostgreSQL                              │
└─────────────────────────────────────────────────────────────┘
```

### Controller Layer

Thin REST controllers that handle HTTP concerns only:
- Request/response mapping
- `@PreAuthorize` annotations for method-level role checks
- Delegates all logic to service interfaces
- Returns appropriate HTTP status codes

### Service Layer

All business logic lives here, following the **interface + implementation** pattern:
- Every service has an interface (e.g., `BookingService`) and an `impl/` class (e.g., `BookingServiceImpl`)
- Ownership and tenant authorization is enforced here (not in controllers)
- Transactional boundaries are declared at this level
- Services depend on other service interfaces, not implementations

### Repository Layer

Spring Data JPA repositories with:
- Custom `@Query` methods using JPQL
- Fetch joins to prevent N+1 problems (`findByEmailWithRoles`, `findByIdWithDetails`)
- DTO projections for listing endpoints (`findServiceResponsesBySalon`)
- Complex paginated queries with dynamic filtering

---

## Domain Model

### Entity Relationship Diagram

```
┌──────────┐        ┌──────────┐        ┌──────────────┐
│   Role   │◄──M:N──│   User   │──1:1──►│    Salon     │
│          │        │          │  owner  │              │
└──────────┘        │          │        └──────┬───────┘
                    │          │               │
                    │          │──M:1──►        │ 1:N
                    │          │ staffSalon     │
                    │          │               ▼
                    │          │──M:N──►┌──────────────┐
                    │          │        │   Services   │
                    └──────────┘        │              │
                         │              └──────┬───────┘
                         │                     │
                    ┌────▼─────┐               │
                    │ Refresh  │               │
                    │  Token   │               │
                    └──────────┘               │
                                               │
                    ┌──────────────────────────┐
                    │        Booking            │
                    │  salon ──► Salon          │
                    │  service ──► Services     │
                    │  staff ──► User           │
                    │  customer ──► User        │
                    └──────────────────────────┘
```

### Key Entities

| Entity         | Description                                                         |
| -------------- | ------------------------------------------------------------------- |
| **User**       | Central identity. Can be a customer, staff member, salon admin, or super admin. Has M:N relationship with `Role` and `Services`. Optionally linked to a `Salon` as owner (1:1) or staff member (M:1). |
| **Salon**      | A salon business. Strictly one owner (`User`). All data is tenant-scoped through this entity. |
| **Services**   | A service offered by a salon (e.g., "Haircut"). Has price, duration, active flag. Unique per salon by name. M:N with staff (via `staff_services` join table). |
| **Booking**    | An appointment. Links a salon, service, staff member, and customer with a time range and lifecycle status. Indexed for performance on staff+time, salon+date, and status. |
| **Role**       | Persistent role entity mapped to the `AppRole` enum.                |
| **RefreshToken** | Persisted JTI-tracked refresh token for secure token rotation. Supports revocation and replacement chain tracking. |

### Enums

| Enum              | Values                                                    |
| ----------------- | --------------------------------------------------------- |
| `AppRole`         | `ROLE_USER`, `ROLE_SALON_ADMIN`, `ROLE_SUPER_ADMIN`, `ROLE_STAFF` |
| `BookingStatus`   | `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `NO_SHOW` |
| `BookingRange`    | `TODAY`, `UPCOMING`, `PAST`                               |
| `TrendRange`      | `LAST_7_DAYS`, `LAST_30_DAYS`, `LAST_90_DAYS`, `CUSTOM`  |
| `Provider`        | `LOCAL`, `GOOGLE`, `FACEBOOK`, `GITHUB` (future OAuth2)   |

---

## Security Architecture

### Authentication Flow

```
             ┌──────────┐
             │  Client  │
             └────┬─────┘
                  │
    ┌─────────────▼──────────────┐
    │   POST /api/auth/login     │
    │   { email, password }      │
    └─────────────┬──────────────┘
                  │
    ┌─────────────▼──────────────┐
    │  AuthenticationManager     │
    │  DaoAuthenticationProvider │
    │  BCryptPasswordEncoder     │
    └─────────────┬──────────────┘
                  │ ✅ Authenticated
    ┌─────────────▼──────────────┐
    │  JwtService generates:     │
    │  • Access Token (10 min)   │
    │  • Refresh Token (24 hr)   │
    └─────────────┬──────────────┘
                  │
    ┌─────────────▼──────────────┐
    │  Response:                  │
    │  Body:  { accessToken }    │
    │  Cookie: refreshToken      │
    │         (httpOnly, secure)  │
    └────────────────────────────┘
```

### JWT Token Design

**Access Token** (short-lived, 10 minutes):
- Subject: user email
- Claims: `userId`, `roles[]`, `typ: "access"`
- Sent via `Authorization: Bearer <token>` header

**Refresh Token** (long-lived, 24 hours):
- Subject: user email
- Claims: `typ: "refresh"`, JTI (unique ID)
- Sent via httpOnly cookie or request body
- Persisted in database for revocation and rotation

### Token Rotation

The refresh flow implements **token rotation with revocation detection**:

1. Client sends refresh token (cookie or body)
2. Server validates the token and looks up the JTI in the database
3. Old refresh token is revoked, `replacedByToken` is set
4. New access + refresh tokens are issued
5. New refresh token JTI is persisted

This prevents refresh token replay attacks.

### Request Authentication Pipeline

```
HTTP Request
    │
    ▼
AuthTokenFilter (OncePerRequestFilter)
    │
    ├── Skip: /api/auth/login, /api/auth/register, /api/auth/refresh
    │
    ├── No "Bearer" header → pass through (anonymous)
    │
    ├── Extract token → reject if not access token type
    │
    ├── Extract email from subject → load UserDetails from DB
    │
    ├── Validate token (signature, expiry, subject match)
    │
    └── Set SecurityContext authentication
         │
         ▼
    SecurityFilterChain (URL pattern checks)
         │
         ▼
    @PreAuthorize (method-level role checks)
         │
         ▼
    Service Layer (ownership/tenant checks)
```

---

## Authorization Model

Authorization is enforced in **three distinct layers**:

### Layer 1 — Authentication (Spring Security Filter)

> "Is the user logged in?"

The `AuthTokenFilter` validates the JWT and populates the `SecurityContext`. Binary: authenticated or not.

### Layer 2 — Role-Based Access (SecurityConfig + @PreAuthorize)

> "What kind of user is this?"

**URL-level rules** (SecurityConfig):

```
Permit All:      /api/auth/login, /api/auth/register, /api/auth/refresh
                 /api/public/**, /swagger-ui/**, /v3/api-docs/**
Authenticated:   POST /api/salons, GET /api/salons/me
SALON_ADMIN:     /api/salons/**
SUPER_ADMIN:     /api/admin/**
Any Auth:        everything else
```

**Method-level rules** (`@PreAuthorize` on controller methods):

```
Booking create:     USER or SALON_ADMIN
Salon bookings:     SALON_ADMIN
Complete/No-show:   STAFF or SALON_ADMIN
Analytics:          SALON_ADMIN
Staff listing:      SALON_ADMIN
```

### Layer 3 — Ownership / Tenant Authorization (Service Layer)

> "Is this resource theirs?"

- Salon data is always resolved from the authenticated user: `salonRepository.findByOwner(currentUser)`
- No salon ID is ever accepted from the client for admin operations
- Booking ownership is checked per role (admin → salon scope, staff → assigned, user → customer)
- Staff must belong to the salon to be managed

### Role Transition

```
ROLE_USER  ──(creates salon)──►  ROLE_SALON_ADMIN
```

A user is promoted from `ROLE_USER` → `ROLE_SALON_ADMIN` automatically upon successful salon creation. The new role is reflected after the next token refresh.

### Roles Summary

| Role               | Capabilities                                                     |
| ------------------ | ---------------------------------------------------------------- |
| `ROLE_USER`        | Browse salons, book appointments, view own bookings              |
| `ROLE_STAFF`       | View assigned bookings, complete bookings, mark no-shows         |
| `ROLE_SALON_ADMIN` | Full salon management: services, staff, bookings, analytics      |
| `ROLE_SUPER_ADMIN` | Platform-level control (reserved for `/api/admin/**`)            |

---

## API Endpoints

### Authentication (`/api/auth`)

| Method | Path         | Auth     | Description                    |
| ------ | ------------ | -------- | ------------------------------ |
| POST   | `/register`  | Public   | Register a new user            |
| POST   | `/login`     | Public   | Login, returns JWT + cookie    |
| POST   | `/refresh`   | Public   | Rotate tokens                  |
| POST   | `/logout`    | Auth     | Revoke refresh token           |
| GET    | `/me`        | Auth     | Current user profile           |

### Salon Management (`/api/salons`)

| Method | Path   | Auth        | Description                      |
| ------ | ------ | ----------- | -------------------------------- |
| POST   | `/`    | Auth        | Create salon (+ role upgrade)    |
| GET    | `/me`  | Auth        | Get my salon                     |
| PUT    | `/me`  | SALON_ADMIN | Update my salon                  |

### Services (`/api/salons/services`)

| Method | Path                      | Auth        | Description                  |
| ------ | ------------------------- | ----------- | ---------------------------- |
| GET    | `/`                       | SALON_ADMIN | List my salon's services     |
| POST   | `/`                       | SALON_ADMIN | Create a service             |
| PUT    | `/{serviceId}`            | SALON_ADMIN | Update a service             |
| PATCH  | `/{serviceId}/deactivate` | SALON_ADMIN | Deactivate (if no staff)     |
| PATCH  | `/{serviceId}/reactivate` | SALON_ADMIN | Reactivate                   |
| GET    | `/{serviceId}/staff`      | SALON_ADMIN | Staff assigned to service    |

### Staff (`/api/salons/staff`)

| Method | Path                   | Auth        | Description                  |
| ------ | ---------------------- | ----------- | ---------------------------- |
| GET    | `/`                    | SALON_ADMIN | List salon staff             |
| POST   | `/`                    | SALON_ADMIN | Create staff member          |
| PATCH  | `/{id}/deactivate`     | SALON_ADMIN | Deactivate + unassign        |
| PATCH  | `/{id}/reactivate`     | SALON_ADMIN | Reactivate                   |
| GET    | `/{staffId}/services`  | SALON_ADMIN | Services for a staff member  |
| PUT    | `/{id}/services`       | SALON_ADMIN | Assign services to staff     |

### Bookings (`/api/bookings`)

| Method | Path                      | Auth                   | Description                     |
| ------ | ------------------------- | ---------------------- | ------------------------------- |
| POST   | `/`                       | USER / SALON_ADMIN     | Create booking                  |
| GET    | `/`                       | All roles              | Paginated list (role-scoped)    |
| GET    | `/{bookingId}`            | All roles              | Single booking (ownership)      |
| GET    | `/salon`                  | SALON_ADMIN            | Salon bookings (30 day window)  |
| GET    | `/today`                  | SALON_ADMIN / STAFF    | Today's bookings                |
| GET    | `/staff/{staffId}`        | SALON_ADMIN / STAFF    | Staff schedule for a date       |
| GET    | `/availability`           | USER / SALON_ADMIN     | Available time slots            |
| PATCH  | `/{bookingId}/cancel`     | USER / SALON_ADMIN     | Cancel booking                  |
| PATCH  | `/{bookingId}/complete`   | STAFF / SALON_ADMIN    | Complete booking                |
| PATCH  | `/{bookingId}/no-show`    | STAFF / SALON_ADMIN    | Mark no-show                    |
| GET    | `/dashboard/admin`        | SALON_ADMIN            | Dashboard aggregates            |

### Analytics (`/api/analytics`)

| Method | Path                    | Auth        | Description                    |
| ------ | ----------------------- | ----------- | ------------------------------ |
| GET    | `/bookings/trend`       | SALON_ADMIN | Booking count trend over time  |
| GET    | `/revenue/trend`        | SALON_ADMIN | Revenue trend over time        |
| GET    | `/leaderboard/staff`    | SALON_ADMIN | Top 5 staff by completions     |
| GET    | `/leaderboard/services` | SALON_ADMIN | Top 5 services by completions  |

### Public (`/api/public`)

| Method | Path                           | Auth   | Description                  |
| ------ | ------------------------------ | ------ | ---------------------------- |
| GET    | `/salons`                      | Public | List all salons              |
| GET    | `/salons/{salonId}`            | Public | Salon details                |
| GET    | `/salons/{salonId}/services`   | Public | Active services for a salon  |
| GET    | `/services/{serviceId}/staff`  | Public | Active staff for a service   |

---

## Booking Lifecycle

Bookings follow a strict state machine with guarded transitions:

```
                    ┌──────────┐
        ┌──────────►│CANCELLED │◄──────────┐
        │           └──────────┘           │
        │                                  │
┌───────┴──┐        ┌──────────┐     ┌─────┴────┐
│ CONFIRMED│───────►│COMPLETED │     │ PENDING  │
└───────┬──┘        └──────────┘     └─────┬────┘
        │                                  │
        │           ┌──────────┐           │
        └──────────►│ NO_SHOW  │     CONFIRMED
                    └──────────┘
```

- New bookings are currently created with status `CONFIRMED` (auto-confirmed)
- `PENDING` → `CONFIRMED` | `CANCELLED` (supported by code, for future use if a confirmation step is added)
- `CONFIRMED` → `CANCELLED` | `COMPLETED` | `NO_SHOW`
- Terminal states (`CANCELLED`, `COMPLETED`, `NO_SHOW`) cannot be transitioned further
- Conflict detection prevents double-booking: overlapping confirmed bookings for the same staff are rejected

---

## Availability System

The `AvailabilityService` calculates free time slots for a staff member on a given date:

1. Working hours: 09:00 – 21:00 (Asia/Kolkata timezone)
2. Fetches all confirmed bookings for the staff on that day
3. Computes gaps between booked slots
4. Returns available `TimeSlot` windows as `OffsetDateTime` pairs

---

## Error Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps exceptions to consistent HTTP responses:

| Exception                   | HTTP Status | Scenario                              |
| --------------------------- | ----------- | ------------------------------------- |
| `ResourceNotFoundException` | 404         | Entity not found                      |
| `AlreadyExistsException`    | 409         | Duplicate resource (salon, service)   |
| `CanNotException`           | 409         | Business rule violation               |
| `DeactivateException`       | 409         | Deactivation blocked by dependencies  |
| `InactiveException`         | 409         | Operating on inactive resource        |
| `RefreshTokenException`     | 401         | Invalid/expired/revoked refresh token |
| `BadCredentialsException`   | 401         | Wrong email or password               |
| `DisabledException`         | 403         | Account disabled                      |
| `AccessDeniedException`     | 403         | Insufficient permissions              |

Error response format:

```json
{
  "message": "Service with this name already exists",
  "status": "CONFLICT"
}
```

---

## Data Transfer Objects

The project uses Java **records** for all DTOs, ensuring immutability:

- **Request records** include Jakarta Validation annotations (`@NotBlank`, `@Min`, `@DecimalMin`, etc.)
- **Response records** are flat projections — no nested entities are leaked
- `PageResponse<T>` provides a generic paginated wrapper
- `TokenResponse` uses a static factory method (`TokenResponse.of(...)`) for clean construction

---

## Configuration

### Profiles

- `application.yaml` — base config, activates `dev` profile
- `application-dev.yaml` — PostgreSQL connection, JWT secrets, CORS origins

### Key Configuration Properties

| Property                           | Purpose                                 |
| ---------------------------------- | --------------------------------------- |
| `spring.datasource.*`              | PostgreSQL connection                   |
| `spring.jpa.hibernate.ddl-auto`    | Schema auto-update (`update`)           |
| `security.jwt.secret`              | HMAC-SHA signing key (≥64 chars)        |
| `security.jwt.access-ttl-seconds`  | Access token lifetime (600s = 10 min)   |
| `security.jwt.refresh-ttl-seconds` | Refresh token lifetime (86400s = 24 hr) |
| `security.jwt.cookie-*`            | Refresh cookie settings                 |
| `app.cors.front-end-url`           | Allowed CORS origin                     |

### Database Seeding

`SecurityConfig` contains a `CommandLineRunner` that initializes:
- All four roles (`ROLE_USER`, `ROLE_STAFF`, `ROLE_SALON_ADMIN`, `ROLE_SUPER_ADMIN`)
- Three seed users: `user1` (user), `owner1` (salon admin), `admin` (super admin)

---

## Key Design Decisions

### 1. Tenant Isolation via Owner Resolution

Salon data is never accessed by client-supplied ID for admin operations. Instead:
```java
Salon salon = salonRepository.findByOwner(currentUser);
```
This eliminates an entire class of authorization bypass vulnerabilities.

### 2. One User, One Salon

The `User → Salon` relationship is 1:1 with a unique constraint on `owner_id`. This simplifies tenant resolution — the authenticated user's salon is always unambiguous.

### 3. Dynamic Role Promotion

Users start as `ROLE_USER`. Creating a salon automatically adds `ROLE_SALON_ADMIN`. No manual intervention or separate admin workflow is required.

### 4. Stateless Sessions + Persistent Refresh Tokens

The API is fully stateless (no HTTP sessions). Refresh tokens are persisted in the database with JTI tracking, enabling:
- Token rotation (new JTI per refresh)
- Revocation (logout, compromise detection)
- Replacement chain tracking (`replacedByToken`)

### 5. Service-Staff Many-to-Many

Staff members are assigned to specific services. Bookings validate that the selected staff is assigned to the selected service, ensuring domain consistency.

### 6. Booking Conflict Detection

Before creating a booking, the system queries for overlapping confirmed bookings on the same staff member:
```java
bookingRepository.findOverlappingBookings(staffId, start, end)
```
This prevents double-booking at the database query level.

### 7. Deactivation Guards

- Services cannot be deactivated while staff is assigned (prevents orphaned assignments)
- Deactivating staff automatically clears their service assignments
- Inactive staff/services are rejected during booking creation

### 8. Role-Scoped Booking Queries

The paginated booking list endpoint returns different results based on the caller's role:
- **SALON_ADMIN**: All bookings for their salon
- **STAFF**: Only bookings assigned to them
- **USER**: Only bookings where they are the customer

This is implemented with separate JPQL queries per role, supporting filtering by status, search text, date range, and time range (`today`, `upcoming`, `past`).

---

## Performance Considerations

- **Fetch joins** on critical paths (user+roles, booking+service+staff+customer) to eliminate N+1 queries
- **DTO projections** in `ServicesRepository.findServiceResponsesBySalon()` to avoid loading unused fields
- **Database indexes** on `Booking` table: `idx_booking_staff_time`, `idx_booking_salon_date`, `idx_booking_status`
- **Refresh token indexes**: unique index on `jti`, index on `user_id`
- `spring.jpa.open-in-view: false` — prevents accidental lazy loading in controllers
