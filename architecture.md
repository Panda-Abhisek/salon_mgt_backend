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
| API Documentation | Springdoc OpenAPI (Swagger UI)            |
| Build Tool        | Maven                                     |
| Boilerplate       | Lombok                                    |
| Billing           | Stripe Integration                        |

---

## Project Structure

```
src/main/java/com/panda/salon_mgt_backend/
├── SalonMgtBackendApplication.java      # Entry point
│
├── configs/                             # Application configuration
│   ├── APIDocConfig.java               # OpenAPI/Swagger metadata
│   ├── ModelMapperConfig.java          # ModelMapper bean
│   ├── PlanBootstrap.java              # Seed plan data
│   ├── billing/
│   │   ├── StripeBillingProvider.java  # Stripe payment processing
│   │   ├── StripePriceConfig.java      # Stripe price IDs
│   │   └── BillingProviderFactory.java # Provider abstraction
│   └── crons/
│       ├── SubscriptionExpiryJob.java   # Expire subscriptions daily
│       └── PendingPaymentReconcilerJob.java # Retry failed payments
│
├── controllers/                         # REST API layer
│   ├── AuthController.java             # Authentication
│   ├── SalonController.java            # Salon CRUD
│   ├── ServicesController.java         # Service management
│   ├── StaffController.java            # Staff management
│   ├── BookingController.java          # Booking lifecycle
│   ├── AnalyticsController.java        # Dashboard analytics
│   ├── PublicSalonController.java       # Public salon browsing
│   ├── SubscriptionController.java     # Subscription management
│   ├── BillingPortalController.java    # Stripe customer portal
│   ├── BillingWebhookController.java   # Stripe webhooks
│   ├── BillingMetricsController.java   # Revenue metrics
│   ├── PlanController.java             # Plan management
│   ├── ForecastController.java         # Booking predictions
│   ├── RevenueTimelineController.java  # Revenue timeline
│   ├── AdminAuditController.java       # Audit log viewing
│   └── HelloController.java            # Health check
│
├── exceptions/                          # Error handling
│   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   ├── ResourceNotFoundException.java  # 404
│   ├── AlreadyExistsException.java     # 409
│   ├── CanNotException.java            # 409 (business rule)
│   ├── DeactivateException.java        # 409 (deactivation blocked)
│   ├── InactiveException.java          # 409 (inactive resource)
│   ├── RefreshTokenException.java     # 401 (token issues)
│   ├── PlanLimitExceededException.java # 403 (plan limits)
│   └── PlanUpgradeRequiredException.java # 402 (upgrade needed)
│
├── models/                              # JPA entities & enums
│   ├── User.java                       # Users (customer, staff, admin)
│   ├── Salon.java                      # Salon entity (1:1 with owner)
│   ├── Services.java                   # Salon services
│   ├── Booking.java                    # Appointments
│   ├── Role.java                       # Role entity
│   ├── RefreshToken.java               # Persisted refresh tokens
│   ├── Subscription.java               # Salon subscriptions
│   ├── Plan.java                       # Subscription plans
│   ├── BillingTransaction.java         # Payment records
│   ├── StripeWebhookEvent.java         # Webhook event storage
│   ├── AuditLog.java                   # Audit trail
│   ├── AppRole.java                   # Role enum
│   ├── BookingStatus.java              # Booking states
│   ├── SubscriptionStatus.java         # Subscription states
│   ├── BillingStatus.java              # Payment states
│   ├── PlanType.java                   # Plan types
│   ├── TrendRange.java                 # Analytics time ranges
│   ├── BookingRange.java               # Query filter enum
│   └── Provider.java                   # Auth provider enum
│
├── payloads/                           # DTOs (records)
│   ├── TokenResponse.java              # JWT response
│   ├── SalonResponse.java              # Salon DTOs
│   ├── ServiceResponse.java            # Service DTOs
│   ├── StaffResponse.java              # Staff DTOs
│   ├── BookingResponse.java            # Booking DTOs
│   ├── PageResponse.java               # Paginated wrapper
│   ├── TimeSlot.java                   # Availability slot
│   ├── AdminDashboardResponse.java     # Dashboard aggregates
│   ├── LeaderboardItemDTO.java         # Top rankings
│   ├── SubscriptionLifecycleResponse.java # Sub details
│   ├── ConversionMetrics.java          # Billing metrics
│   ├── ForecastPointDTO.java           # Prediction data
│   └── AuditLogDto.java                # Audit trail DTO
│
├── repositories/                        # Spring Data JPA
│   ├── UserRepository.java             # User queries
│   ├── SalonRepository.java            # Salon queries
│   ├── ServicesRepository.java         # Service queries
│   ├── BookingRepository.java          # Complex booking queries
│   ├── RoleRepository.java             # Role lookup
│   ├── RefreshTokenRepository.java     # Token storage
│   ├── SubscriptionRepository.java    # Subscription queries
│   ├── PlanRepository.java            # Plan queries
│   ├── BillingTransactionRepository.java # Payment queries
│   ├── AuditLogRepository.java        # Audit log queries
│   └── StripeWebhookEventRepository.java # Webhook storage
│
├── security/                            # Security infrastructure
│   ├── SecurityConfig.java             # Filter chain + seed data
│   ├── CorsConfig.java                # CORS configuration
│   ├── jwt/
│   │   ├── JwtService.java            # Token generation & validation
│   │   ├── AuthTokenFilter.java       # JWT authentication filter
│   │   ├── AuthEntryPointJwt.java     # 401 handler
│   │   └── JwtAccessDeniedHandler.java # 403 handler
│   ├── services/
│   │   ├── UserDetailsImpl.java       # Spring Security UserDetails
│   │   ├── UserDetailsServiceImpl.java # Loads user by email
│   │   └── CookieService.java         # Cookie management
│   └── requests/
│       ├── UserLoginRequest.java       # Login payload
│       └── UserRegisterRequest.java    # Registration payload
│
├── services/                            # Business logic layer
│   ├── AuthService.java                # Interface
│   ├── UserService.java               # Interface
│   ├── SalonService.java              # Interface
│   ├── ServicesService.java           # Interface
│   ├── StaffService.java              # Interface
│   ├── BookingService.java            # Interface
│   ├── AvailabilityService.java       # Interface
│   ├── SubscriptionService.java       # Interface
│   ├── BillingService.java            # Interface
│   ├── BillingProvider.java           # Interface
│   ├── BillingInsightsService.java    # Interface
│   ├── impl/
│   │   ├── AuthServiceImpl.java      # Auth + token rotation
│   │   ├── UserServiceImpl.java      # User resolution
│   │   ├── SalonServiceImpl.java     # Salon CRUD + role upgrade
│   │   ├── ServicesServiceImpl.java   # Service management
│   │   ├── StaffServiceImpl.java     # Staff lifecycle
│   │   ├── BookingServiceImpl.java   # Booking lifecycle + dashboard
│   │   └── AvailabilityServiceImpl.java # Slot calculation
│   └── analytics/
│       ├── AnalyticsService.java     # Interface
│       ├── AnalyticsServiceImpl.java # Trends & leaderboards
│       ├── AuditLogService.java      # Audit trail
│       ├── AuditLogServiceImpl.java  # Audit implementation
│       ├── BillingMetricsService.java # Revenue metrics
│       ├── BillingMetricsServiceImpl.java # Metrics implementation
│       ├── ForecastService.java      # Interface
│       ├── ForecastServiceImpl.java  # Prediction logic
│       ├── PlanService.java          # Plan management
│       └── TrendPointDTO.java        # Trend data point
│
└── utils/
    ├── AuthUtils.java                # Auth helper
    └── TenantContext.java           # Tenant resolution
```

---

## Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client (Frontend)                       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼──────────────────────────────────┐
│                   Security Filter Chain                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐│
│  │ CORS Filter │→ │AuthTokenFilter│→ │ SecurityFilterChain││
│  └─────────────┘  └──────────────┘  └────────────────────┘│
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
│  fetch joins to avoid N+1, DTO projections                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      PostgreSQL                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Domain Model

### Entity Relationship Diagram

```
┌──────────┐        ┌──────────┐        ┌──────────────┐
│   Role   │◄──M:N──│   User   │──1:1──►│    Salon     │
│          │        │          │  owner  │              │
└──────────┘        │          │        └──────┬───────┘
                    │          │               │
                    │          │──M:1──►       │ 1:N
                    │          │ staffSalon    │
                    │          │               ▼
                    │          │──M:N──►┌──────────────┐
                    │          │        │   Services   │
                    └──────────┘        │              │
                         │              └──────┬───────┘
                         │                     │
                    ┌────▼─────┐               │
                    │ Refresh  │               │
                    │  Token   │               │
                    └────────────┘               │
                         │                       │
┌───────────────────────┼─────────────────────────┤
│                       │                         │
│  ┌────────────────────▼────────────────────┐  │
│  │              Subscription                │◄─┘
│  │  salon ──► Salon                        │
│  │  plan ──► Plan                          │
│  └────────────────────┬────────────────────┘
│                       │
│  ┌────────────────────▼────────────────────┐
│  │         BillingTransaction              │
│  │  subscription ──► Subscription          │
│  │  status ──► BillingStatus               │
│  └─────────────────────────────────────────┘
│
│  ┌──────────────────────┐
│  │        Booking        │
│  │  salon ──► Salon     │
│  │  service ──► Services│
│  │  staff ──► User      │
│  │  customer ──► User   │
│  └──────────────────────┘
```

---

## Security Architecture

### Authentication Flow

```
Client → POST /api/auth/login {email, password}
    → AuthenticationManager (BCrypt)
    → JwtService generates:
        • Access Token (10 min)
        • Refresh Token (24 hr)
    → Response:
        • Body: { accessToken }
        • Cookie: refreshToken (httpOnly)
```

### JWT Token Design

**Access Token** (10 minutes):
- Subject: user email
- Claims: `userId`, `roles[]`, `typ: "access"`
- Header: `Authorization: Bearer <token>`

**Refresh Token** (24 hours):
- Subject: user email
- Claims: `typ: "refresh"`, JTI (unique ID)
- Persisted in database for revocation and rotation

### Token Rotation

1. Client sends refresh token
2. Server validates token + looks up JTI
3. Old token revoked, `replacedByToken` set
4. New access + refresh tokens issued
5. New JTI persisted

This prevents refresh token replay attacks.

---

## Authorization Model

### Three-Layer Authorization

| Layer | Question | Implementation |
|-------|----------|----------------|
| Authentication | "Logged in?" | AuthTokenFilter + SecurityContext |
| Role-Based | "What role?" | SecurityConfig + @PreAuthorize |
| Ownership | "Is it theirs?" | Service layer tenant guards |

### Role Hierarchy

```
ROLE_USER ──(creates salon)──► ROLE_SALON_ADMIN
```

| Role | Capabilities |
|------|-------------|
| `ROLE_USER` | Browse salons, book appointments |
| `ROLE_STAFF` | View assigned bookings, complete, no-show |
| `ROLE_SALON_ADMIN` | Full salon + billing + analytics |
| `ROLE_SUPER_ADMIN` | Platform-level control |

---

## Billing & Subscriptions

### Subscription Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    Subscription Lifecycle                     │
│                                                              │
│  FREE ──[upgrade]──► PRO/PREMIUM ──[expire]──► EXPIRED     │
│     │                    │                    │              │
│     │                    │                    │              │
│     ▼                    ▼                    ▼              │
│  ACTIVE               ACTIVE ◄──[renew]── EXPIRED          │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Plan Limits

| Feature | FREE | PRO | PREMIUM |
|---------|------|-----|---------|
| Max Staff | 2 | 5 | Unlimited |
| Max Services | 5 | 20 | Unlimited |
| Max Bookings/Month | 50 | 200 | Unlimited |
| Analytics | Basic | Advanced | Full |
| Smart Alerts | No | Yes | Yes |
| Price/Month | Free | ₹999 | ₹1999 |

### Stripe Integration

- **Checkout Sessions** - Customer subscription purchase
- **Customer Portal** - Self-service billing management
- **Webhooks** - Async payment events (checkout.session.completed, invoice.payment_succeeded, customer.subscription.deleted)
- **Price IDs** - Configurable per environment

---

## Analytics & Intelligence

### Dashboard Metrics

| Endpoint | Description |
|----------|-------------|
| `/api/analytics/bookings/trend` | Booking count over time |
| `/api/analytics/revenue/trend` | Revenue over time |
| `/api/analytics/leaderboard/staff` | Top 5 staff by completions |
| `/api/analytics/leaderboard/services` | Top 5 services by completions |

### Forecasting

Uses simple moving average algorithm:
- 7-day historical booking patterns
- Predicts next 14 days
- Considers day-of-week seasonality

### Revenue Timeline

- Daily aggregation of completed bookings
- Salon-scoped (admins see only their salon)
- Date range filtering

---

## Audit Logging

### Events Tracked

- `SUBSCRIPTION_CREATED` - New subscription
- `SUBSCRIPTION_UPGRADED` - Plan upgrade
- `SUBSCRIPTION_RENEWED` - Auto-renewal
- `SUBSCRIPTION_EXPIRED` - Expiration
- `SUBSCRIPTION_CANCELLED` - Manual cancellation
- `PAYMENT_SUCCEEDED` - Successful payment
- `PAYMENT_FAILED` - Failed payment

### Implementation

- Async event publication via `AuditLogService`
- Structured payload (actor, action, details, salon_id)
- REST API for admin viewing: `/api/admin/audit/*`

---

## Scheduled Jobs

### SubscriptionExpiryJob

- Runs daily at midnight
- Finds subscriptions where `endDate < now`
- Sets status to `EXPIRED`
- Logs audit event

### PendingPaymentReconcilerJob

- Runs every 15 minutes
- Finds `PENDING` transactions older than 5 minutes
- Retries up to 3 times
- Marks as `FAILED_PERMANENT` after max retries

---

## Booking Lifecycle

```
         ┌──────────┐
         │CANCELLED │◄──────────┐
         └──────────┘           │
           ▲                    │
           │                    ▼
┌─────────┴───┐     ┌──────────┐     ┌─────┴────┐
│  CONFIRMED  │────►│COMPLETED │     │ PENDING  │
└─────────────┘     └──────────┘     └────┬────┘
     ▲                                   │
     │              ┌──────────┐         │
     └─────────────►│ NO_SHOW  │─────────┘
                    └──────────┘       CONFIRMED
```

### Conflict Detection

Before booking creation:
```java
bookingRepository.findOverlappingBookings(staffId, start, end)
```
Prevents double-booking at database level.

---

## Availability System

1. Working hours: 09:00 – 21:00 (Asia/Kolkata)
2. Fetch confirmed bookings for staff on date
3. Compute gaps between booked slots
4. Return available `TimeSlot` windows

---

## Error Handling

| Exception | HTTP | Scenario |
|-----------|------|----------|
| `ResourceNotFoundException` | 404 | Entity not found |
| `AlreadyExistsException` | 409 | Duplicate resource |
| `CanNotException` | 409 | Business rule violation |
| `DeactivateException` | 409 | Deactivation blocked |
| `InactiveException` | 409 | Inactive resource |
| `PlanLimitExceededException` | 403 | Plan limit reached |
| `PlanUpgradeRequiredException` | 402 | Upgrade needed |
| `RefreshTokenException` | 401 | Invalid token |

---

## Key Design Decisions

### 1. Tenant Isolation via Owner Resolution

```java
Salon salon = salonRepository.findByOwner(currentUser);
```
No client-supplied salon IDs trusted for admin operations.

### 2. One User, One Salon

1:1 relationship with unique constraint on `owner_id`.

### 3. Stateless Sessions + Persistent Refresh Tokens

Fully stateless API. Refresh tokens persisted with JTI tracking:
- Token rotation
- Revocation
- Replacement chain

### 4. Booking Conflict Detection

Database-level overlap detection prevents double-booking.

### 5. Deactivation Guards

- Services cannot be deactivated with assigned staff
- Staff deactivation clears service assignments
- Inactive resources rejected during booking

### 6. Soft Deletes

Staff and services use `active` flag, not deletion.

---

## Performance Considerations

- **Fetch joins** on critical paths (user+roles, booking+service+staff+customer)
- **DTO projections** to avoid loading unused fields
- **Database indexes** on Booking table
- **Refresh token indexes** on JTI and user_id
- `spring.jpa.open-in-view: false`

---

## Docker Deployment

### Dockerfile

Multi-stage build:
1. **Builder stage** - JDK Alpine, Maven build
2. **Runtime stage** - JRE Alpine, non-root user

### docker-compose.yml

- App service (port 8080)
- PostgreSQL service (port 5432)
- Health checks

### Resource Tuning

```dockerfile
ENV JAVA_OPTS="-Xms128m -Xmx256m"
```

Optimized for 1GB RAM server.

---

## Challenges & Solutions

| Challenge | Solution |
|-----------|----------|
| Lombok not generating code | Added annotationProcessorPaths to maven-compiler-plugin |
| 1GB RAM server constraints | Multi-stage Docker, reduced JVM heap |
| Tenant isolation bypass risk | Owner-based resolution, no client IDs |
| Token replay attacks | JTI-based rotation with revocation |
| Double-booking | Database-level conflict detection |
| Subscription expiry | Daily cron job + audit logging |

---

## API Reference

### Authentication (`/api/auth`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | Public | Register user |
| POST | `/login` | Public | Login |
| POST | `/refresh` | Public | Rotate tokens |
| POST | `/logout` | Auth | Revoke token |
| GET | `/me` | Auth | Current user |

### Bookings (`/api/bookings`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/` | USER | Create booking |
| GET | `/` | All | List (role-scoped) |
| GET | `/salon` | ADMIN | Salon bookings |
| GET | `/availability` | USER | Time slots |
| PATCH | `/{id}/cancel` | USER | Cancel |
| PATCH | `/{id}/complete` | STAFF | Complete |
| PATCH | `/{id}/no-show` | STAFF | No-show |

### Billing (`/api/billing`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/current` | ADMIN | Current subscription |
| POST | `/checkout` | ADMIN | Create checkout session |
| GET | `/portal` | ADMIN | Customer portal URL |
| POST | `/webhook` | Stripe | Webhook endpoint |

### Analytics (`/api/analytics`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/bookings/trend` | ADMIN | Booking trends |
| GET | `/revenue/trend` | ADMIN | Revenue trends |
| GET | `/leaderboard/staff` | ADMIN | Top staff |
| GET | `/leaderboard/services` | ADMIN | Top services |

### Admin (`/api/admin`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/audit/recent` | SUPER_ADMIN | Recent audits |
| GET | `/audit/salon/{id}` | SUPER_ADMIN | Salon audits |
