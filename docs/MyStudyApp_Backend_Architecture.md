# MyStudyApp Backend Architecture Documentation

> **Project:** MyStudyApp — Campus Event Platform  
> **Framework:** Spring Boot 3.x (Java)  
> **Pattern:** Layered Architecture + Domain-Driven Design  
> **Base Package:** `de.fhdortmund.mystudyapp`  
> **Total Files:** 180+ | **Classes:** 116 | **Interfaces:** 14 | **Enums:** 8 | **REST Endpoints:** 80+

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Folder Structure](#2-folder-structure)
3. [Domain Modules Deep Dive](#3-domain-modules-deep-dive)
   - [3.1 Common (`common/`)](#31-common-common)
   - [3.2 Identity (`identity/`)](#32-identity-identity)
   - [3.3 Events (`events/`)](#33-events-events)
   - [3.4 Registration (`registration/`)](#34-registration-registration)
   - [3.5 Moderation (`moderation/`)](#35-moderation-moderation)
   - [3.6 Notifications (`notification/`)](#36-notifications-notification)
   - [3.7 MQTT Integration (`mqtt/`)](#37-mqtt-integration-mqtt)
4. [Data Flow & Architecture Patterns](#4-data-flow--architecture-patterns)
5. [Security Architecture](#5-security-architecture)
6. [API Endpoint Reference](#6-api-endpoint-reference)
7. [Entity Relationship Diagram (Conceptual)](#7-entity-relationship-diagram-conceptual)
8. [Key Design Decisions](#8-key-design-decisions)

---

## 1. Project Overview

MyStudyApp is a **campus event platform** for verified university students. The backend is a **Spring Boot monolith** organized by domain modules. It handles:

- 🔐 **Authentication & Authorization** — JWT-based auth with email verification
- 🎫 **Event Management** — CRUD, media uploads, drafts, publishing workflow
- 📝 **RSVP & Waitlist** — Capacity management with atomic operations
- ⭐ **Reviews & Ratings** — Post-event reviews with helpfulness voting
- 🚨 **Reports & Moderation** — User-generated reports with admin resolution
- 🔔 **Notifications** — In-app notification system with deep-linking
- 📡 **MQTT Integration** — Receives official AStA events via MQTT
- 👤 **Trust System** — Host promotion based on completed events + ratings
- 🛡️ **Admin Dashboard** — Event approval, user management, analytics

---

## 2. Folder Structure

```
de.fhdortmund.mystudyapp/
│
├── common/                          # Shared infrastructure & cross-cutting concerns
│   ├── config/                      # Spring configurations (CORS, Security, OpenAPI, etc.)
│   ├── exception/                   # Custom exceptions & global handler
│   ├── response/                    # Standardized API response wrappers
│   ├── scheduler/                   # Background jobs (token cleanup)
│   ├── security/                    # JWT filters, rate limiting
│   └── service/                     # Shared services (file storage, thumbnails)
│
├── identity/                        # User identity, auth, profiles, trust system
│   ├── controller/                  # REST controllers (auth, profile, admin users)
│   ├── dto/                         # Data Transfer Objects
│   ├── mapper/                      # Entity ↔ DTO converters
│   ├── model/                       # JPA entities (User, Role, TrustLevel, etc.)
│   ├── repository/                  # Spring Data JPA repositories
│   └── service/                     # Business logic (auth, trust levels, email)
│
├── events/                          # Event management core
│   ├── controller/                  # Event, category, search, admin, public, SSE controllers
│   ├── dto/                         # Event DTOs, requests, responses
│   ├── factory/                     # Event creation factory (Creational Pattern)
│   ├── mapper/                      # Event → EventDto mapping
│   ├── model/                       # Event, Category, EventMedia, EventCategory entities
│   ├── repository/                  # Event & category repositories
│   └── service/                     # Event CRUD, search, lifecycle, SSE
│
├── registration/                    # RSVP, waitlist, check-in
│   ├── controller/                  # RSVP controller
│   ├── dto/                         # RSVP DTOs
│   ├── mapper/                      # RSVP mapping
│   ├── model/                       # Rsvp entity, RsvpStatus enum
│   ├── observer/                    # Spring events for waitlist promotion
│   ├── repository/                  # RSVP repository
│   └── service/                     # RSVP service, waitlist service
│
├── moderation/                      # Reviews, reports, content moderation
│   ├── controller/                  # Review & report controllers
│   ├── dto/                         # Review/Report DTOs
│   ├── mapper/                      # Review/Report mappers
│   ├── model/                       # Review, Report, ReviewVote entities
│   ├── repository/                  # Review, report, vote repositories
│   └── service/                     # Review service, report service
│
├── notification/                    # In-app notification system
│   ├── controller/                  # Notification controller
│   ├── dto/                         # NotificationDto
│   ├── listener/                    # Event listener for notifications
│   ├── mapper/                      # Notification mapper
│   ├── model/                       # Notification entity, NotificationType enum
│   ├── publisher/                   # Spring ApplicationEvent publisher
│   ├── repository/                  # Notification repository
│   └── service/                     # Notification CRUD service
│
└── mqtt/                            # MQTT integration for AStA events
    ├── adapter/                     # Adapter pattern interface
    ├── config/                      # MQTT client configuration
    ├── dto/                         # OfficialEventMessage DTO, OfficialEventAdapter
    └── listener/                    # MQTT message listener
```

---

## 3. Domain Modules Deep Dive

### 3.1 Common (`common/`)

> **Purpose:** Cross-cutting concerns shared across all domains. Zero business logic.

#### 3.1.1 Config (`common/config/`)

| File | Purpose |
|------|---------|
| `CorsConfig.java` | CORS configuration allowing frontend at `http://localhost:5173` (configurable). Exposes `Authorization` and `X-Refresh-Token` headers. |
| `OpenApiConfig.java` | Swagger/OpenAPI 3.0 setup with Bearer JWT security scheme. Title: "MyStudyApp API". |
| `PageableConfig.java` | Configures Spring Data pagination with max page size of 100. |
| `SecurityConfig.java` | **Core security.** Disables CSRF, stateless sessions. Defines URL authorization rules: `/api/auth/**` and `/api/public/**` permitAll; `/api/admin/**` requires ADMIN role; write operations on events/reports/reviews/rsvps require authentication. |
| `StorageConfig.java` | Serves static files from `/uploads/avatars/**` and `/uploads/events/**` from disk. |
| `StorageProperties.java` | Configuration properties for file storage: max image size (5MB), max video size (20MB), max 5 images / 2 videos per event. |

#### 3.1.2 Exception Handling (`common/exception/`)

| File | Purpose |
|------|---------|
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` that catches all exceptions and returns standardized `ApiResponse<T>` with appropriate HTTP status codes (400, 401, 403, 404, 409, 413, 500). |
| `ResourceNotFoundException.java` | Thrown when an entity is not found → 404. |
| `ForbiddenActionException.java` | Thrown when user lacks permission → 403. |
| `CapacityExceededException.java` | Thrown when event is full → 409 Conflict. |

#### 3.1.3 Response Wrappers (`common/response/`)

| File | Purpose |
|------|---------|
| `ApiResponse<T>` | Standard envelope: `{success, message, data, timestamp}`. Static factory methods `success()` and `error()`. |
| `PageResponse<T>` | Pagination envelope: `{content, page, size, totalElements, totalPages, last}`. |

#### 3.1.4 Security (`common/security/`)

| File | Purpose |
|------|---------|
| `JwtAuthFilter.java` | `OncePerRequestFilter` that parses Bearer tokens, validates JWT, checks user trust level (FLAGS users get 403), and sets Spring Security context. Skips `/api/auth/**` and `/api/public/**`. |
| `JwtUtil.java` | JWT token generation/validation. Access tokens (15 min) include `userId`, `role`, `trustLevel`, `type=access`. Refresh tokens (7 days) include `type=refresh`. Uses HS512. |
| `RateLimitingFilter.java` | Rate limits auth endpoints (5 req/min) and write endpoints (20 req/min) per IP. Returns `429 Too Many Requests` with `ApiResponse` envelope. |

#### 3.1.5 Services (`common/service/`)

| File | Purpose |
|------|---------|
| `FileStorageService.java` | Handles all file I/O: avatar uploads (JPG/PNG/WEBP), event images/videos (MP4/WEBM/MOV). Stores to disk under `uploads/avatars/` and `uploads/events/{eventId}/images|videos/`. |
| `ThumbnailService.java` | Generates 400×300 thumbnails and 800×600 medium images using Thumbnailator. Deletes variants alongside originals. |

#### 3.1.6 Scheduler (`common/scheduler/`)

| File | Purpose |
|------|---------|
| `TokenCleanupService.java` | Runs daily at 3:00 AM. Deletes expired verification and password reset tokens from the database. |

---

### 3.2 Identity (`identity/`)

> **Purpose:** User management, authentication, authorization, profiles, and the trust system.

#### Entities (`identity/model/`)

| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `User` | `id: UUID`, `universityEmail`, `passwordHash`, `displayName`, `bio`, `profileImageUrl`, `role: Role`, `trustLevel: TrustLevel`, `isVerified` | Core user entity. `universityEmail` must match university domain pattern (`.de`/`.edu`, blocks free providers). |
| `UserPreference` | `userId: UUID` (PK), `emailNotifications`, `pushNotifications`, `notifyOnRsvpChange`, `notifyOnReview`, `timezone`, `language` | One-to-one with User. Stores notification preferences. |
| `VerificationToken` | `id: UUID`, `token: String`, `user: User`, `expiryDate: Instant` | Email verification tokens, valid 24 hours. |
| `PasswordResetToken` | `id: UUID`, `token: String`, `user: User`, `expiryDate: Instant` | Password reset tokens, valid 1 hour. |

#### Enums

| Enum | Values |
|------|--------|
| `Role` | `STUDENT`, `ADMIN` |
| `TrustLevel` | `NEW`, `TRUSTED_HOST`, `FLAGGED` |

#### Controllers (`identity/controller/`)

| Controller | Base Path | Key Endpoints |
|-----------|-----------|---------------|
| `UserController` | `/api/auth` | `POST /register`, `POST /login`, `POST /refresh`, `POST /logout`, `GET /verify?token=`, `POST /forgot-password`, `POST /reset-password`, `GET /me`, `PUT /me` (multipart profile), `PUT /me/password`, `DELETE /me`, `GET /me/trust-status` |
| `UserPreferenceController` | `/api/auth/me/preferences` | `GET /`, `PUT /` |
| `PublicUserController` | `/api/public/users` | `GET /{userId}` — public profile with trust metrics |
| `AdminUserController` | `/api/admin/users` | `GET /` (search + filter by trust level), `GET /{userId}`, `PATCH /{userId}/trust-level`, `POST /{userId}/flag`, `POST /{userId}/promote`, `DELETE /{userId}` |

#### Services (`identity/service/`)

| Service | Responsibility |
|---------|---------------|
| `UserService` | Registration, login, token refresh, logout (with blacklist), password reset, profile CRUD, account deletion (cascading cleanup of RSVPs, reviews, reports, events, media), admin user search. |
| `UserDetailsServiceImpl` | Spring Security integration. Loads user by email. Sets `disabled(!isVerified)` and `accountLocked(FLAGGED)`. |
| `TrustLevelService` | Auto-promotion logic: requires ≥3 completed events with reviews AND average rating ≥4.0. Manual promote/flag operations. |
| `EmailService` | Sends verification and password reset emails via `JavaMailSender`. Links point to configurable frontend URL. |
| `UserPreferenceService` | CRUD for user preferences. Creates defaults on registration. |

#### DTOs (`identity/dto/`)

| DTO | Usage |
|-----|-------|
| `AuthResponse` | `{accessToken, refreshToken, tokenType, expiresIn, user}` |
| `UserDto` | `{id, universityEmail, displayName, bio, profileImageUrl, role, trustLevel, createdAt}` |
| `PublicProfileDto` | `{id, displayName, bio, profileImageUrl, trustLevel, createdAt, completedEventsWithReviews, averageHostRating}` |
| `RegisterRequest` | Email (university-only regex), password (8+ chars, complexity), display name |
| `LoginRequest` | Email + password |
| `ChangePasswordRequest` | Current + new + confirm password |
| `ResetPasswordRequest` | Token + new + confirm password |
| `ForgotPasswordRequest` | Email |
| `UpdateProfileRequest` | `displayName`, `bio`, `profileImage` (MultipartFile) |
| `TrustQualificationStatus` | `{completedEventsWithReviews, minimumEventsRequired, averageRating, minimumRatingRequired, meetsEventCount, meetsRatingThreshold, qualifies}` |
| `UserPreferencesDto` | `{emailNotifications, pushNotifications, notifyOnRsvpChange, notifyOnReview, timezone, language}` |

---

### 3.3 Events (`events/`)

> **Purpose:** Event lifecycle, categories, media, search, and admin moderation.

#### Entities (`events/model/`)

| Entity | Key Fields | Relationships |
|--------|-----------|---------------|
| `Event` | `id: UUID`, `host: User`, `title`, `description`, `location`, `startTime`, `endTime`, `maxCapacity`, `currentRsvpCount`, `status: EventStatus`, `slug`, `viewCount`, `deletedAt`, `checkInCode`, `cancellationReason` | `@OneToMany` → `EventCategory`, `@OneToMany` → `EventMedia` |
| `Category` | `id: Integer`, `name` (unique), `icon`, `color`, `sortOrder` | Many-to-many via `EventCategory` |
| `EventCategory` | Composite PK: `EventCategoryId` (`eventId`, `categoryId`) | `@ManyToOne` → Event, `@ManyToOne` → Category |
| `EventMedia` | `id: UUID`, `url`, `mediaType: MediaType`, `filename`, `thumbnailUrl`, `mediumUrl`, `displayOrder` | `@ManyToOne` → Event |

#### Enums

| Enum | Values |
|------|--------|
| `EventStatus` | `DRAFT`, `PUBLISHED`, `UNDER_REVIEW`, `CANCELLED`, `COMPLETED` |
| `MediaType` | `IMAGE`, `VIDEO` |

#### Controllers (`events/controller/`)

| Controller | Base Path | Description |
|-----------|-----------|-------------|
| `EventController` | `/api/events` | Full event CRUD, drafts, publish, cancel, soft delete, restore, permanent delete, media upload/reorder/delete, check-in code generation. |
| `PublicEventController` | `/api/public/events` | **No auth required.** Browse published events, get by ID/slug, featured events. |
| `AdminEventController` | `/api/admin/events` | Admin-only: list all/pending events, approve, reject, flag, bulk approve/reject. |
| `CategoryController` | `/api/public/categories` | Public category listing with icons/colors. |
| `AdminCategoryController` | `/api/admin/categories` | Admin CRUD for categories. |
| `SearchController` | `/api/search` | `GET /suggestions?q=&type=` — autocomplete for events, categories, users, locations. |
| `EventSseController` | `/api/events/stream` | `GET /{eventId}` — Server-Sent Events for real-time RSVP/waitlist updates. |
| `AdminDashboardController` | `/api/admin/dashboard` | `GET /` — aggregated stats for admin dashboard. |

#### Services (`events/service/`)

| Service | Responsibility |
|---------|---------------|
| `EventService` | **Core event logic.** Create (with trust-based auto-publish), draft creation, publish validation, update (with capacity checks), cancel (notifies attendees), soft delete, restore, permanent delete (cleans up media, RSVPs, reviews, reports). Media upload with thumbnail generation. Check-in code generation. |
| `EventFactory` | **Creational Pattern.** Centralizes Event creation from REST API requests and MQTT messages. Trusted hosts/Admins get `PUBLISHED` status; others get `UNDER_REVIEW`. |
| `EventLifecycleService` | `@Scheduled(cron = "0 0 * * * *")` — hourly job that auto-completes past `PUBLISHED` events to `COMPLETED`. |
| `AdminDashboardService` | Aggregates stats: pending events, open reports, total users, new users today, events this week, recent reports, recent pending events. |
| `SearchService` | Autocomplete suggestions across events, categories, users, locations. Max 5 per type. |
| `EventSseService` | Manages `SseEmitter` subscriptions per event. Broadcasts RSVP updates, waitlist updates, and cancellation events. 5-minute timeout. |
| `SlugGenerator` | Generates URL-friendly slugs from titles. Deduplicates with `-1`, `-2` suffixes. |

#### Key DTOs

| DTO | Fields |
|-----|--------|
| `EventDto` | `id, host: HostDto, title, description, location, startTime, endTime, maxCapacity, currentRsvpCount, status, categories, media, createdAt, isHost, myRsvpStatus, slug, viewCount, cancellationReason` |
| `HostDto` | `id, displayName, profileImageUrl, trustLevel, averageHostRating, totalHostReviews, completedEventsWithReviews` |
| `CreateEventRequest` | `title, description, location, startTime, endTime, maxCapacity, categoryIds, slug` |
| `EventMediaDto` | `id, url, mediaType, filename, thumbnailUrl, mediumUrl, displayOrder` |
| `CheckInCodeDto` | `checkInCode, eventId, eventTitle, generatedAt, refreshIntervalSeconds` |
| `AdminDashboardDto` | `pendingEventsCount, openReportsCount, totalUsersCount, newUsersToday, eventsThisWeek, recentReports, recentPendingEvents` |

---

### 3.4 Registration (`registration/`)

> **Purpose:** RSVP management, waitlist handling, and event check-in.

#### Entity (`registration/model/`)

| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Rsvp` | `id: UUID`, `event: Event`, `user: User`, `status: RsvpStatus`, `createdAt`, `cancellationReason` | Unique constraint on `(event_id, user_id)`. |

#### Enum

| Enum | Values |
|------|--------|
| `RsvpStatus` | `GOING`, `WAITLISTED`, `CANCELLED`, `ATTENDED` |

#### Controllers (`registration/controller/`)

| Controller | Base Path | Key Endpoints |
|-----------|-----------|---------------|
| `RsvpController` | `/api` | `POST /events/{eventId}/rsvps`, `GET /events/{eventId}/rsvps/me`, `GET /rsvps/me`, `PATCH /rsvps/{rsvpId}/cancel`, `GET /rsvps/{rsvpId}/position`, `GET /events/{eventId}/rsvps`, `GET /events/{eventId}/rsvps/status/{status}`, `PATCH /events/{eventId}/rsvps/{rsvpId}/attended`, `PATCH /events/{eventId}/rsvps/{rsvpId}/promote`, `POST /events/{eventId}/check-in` |

#### Services (`registration/service/`)

| Service | Responsibility |
|---------|---------------|
| `RsvpService` | Create RSVP (atomic capacity check via `incrementRsvpCount()` query), cancel RSVP (decrements count, publishes `RsvpCancelledEvent`, notifies host), mark attended (host only), self check-in via QR code, get waitlist position. |
| `WaitlistService` | Auto-promotes next waitlisted user when a spot opens (via `RsvpCancelledEvent` listener). Host can manually promote. Uses pessimistic locking on Event. Notifies promoted user. |

#### Observer Pattern (`registration/observer/`)

| Class | Purpose |
|-------|---------|
| `RsvpCancelledEvent` | Spring `ApplicationEvent` carrying `eventId` and `rsvpId`. |
| `RsvpEventPublisher` | Publishes cancellation events. |
| `WaitlistPromotionListener` | `@EventListener` that triggers `WaitlistService.promoteNextWaitlistedUser()` on cancellation. |

---

### 3.5 Moderation (`moderation/`)

> **Purpose:** Reviews, ratings, helpfulness voting, and content reporting.

#### Entities (`moderation/model/`)

| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Review` | `id: UUID`, `event: Event`, `reviewer: User`, `rating: Integer (1-5)`, `comment`, `createdAt`, `helpfulCount` | Unique constraint `(event_id, reviewer_id)`. Only attendees can review. |
| `ReviewVote` | `id: UUID`, `review: Review`, `user: User`, `voteType` | Unique constraint `(review_id, user_id)`. Denormalized `helpfulCount` on Review. |
| `Report` | `id: UUID`, `event: Event`, `reporter: User`, `reason: ReportReason`, `details`, `status: ReportStatus`, `createdAt` | User reports on events. |

#### Enums

| Enum | Values |
|------|--------|
| `ReportReason` | `SPAM`, `INAPPROPRIATE`, `FAKE_EVENT`, `OTHER` |
| `ReportStatus` | `OPEN`, `RESOLVED` |

#### Controllers (`moderation/controller/`)

| Controller | Base Path | Key Endpoints |
|-----------|-----------|---------------|
| `ReviewController` | `/api` | `POST /reviews`, `GET /reviews/event/{eventId}`, `GET /reviews/host/{hostId}`, `DELETE /reviews/{reviewId}`, `POST /reviews/{reviewId}/helpful`, `POST /reviews/{reviewId}/report` |
| `ReportController` | `/api` | `POST /reports`, `GET /admin/reports`, `GET /admin/reports/{reportId}`, `GET /admin/reports/status/{status}`, `GET /admin/reports/reason/{reason}`, `PATCH /admin/reports/{reportId}/resolve`, `DELETE /admin/reports/{reportId}` |

#### Services (`moderation/service/`)

| Service | Responsibility |
|---------|---------------|
| `ReviewService` | Create review (validates event ended, user attended, not already reviewed). Toggle helpful vote (denormalized counter). Delete review (cleans up votes). Auto-triggers trust promotion for host after review creation. |
| `ReportService` | Create report (blocks self-reporting). Admin can resolve with optional event flagging. Publishes critical alerts (INAPPROPRIATE/FAKE_EVENT) via MQTT to AStA backend. |

---

### 3.6 Notifications (`notification/`)

> **Purpose:** In-app notification system with deep-linking and deduplication.

#### Entity (`notification/model/`)

| Entity | Key Fields | Description |
|--------|-----------|-------------|
| `Notification` | `id: UUID`, `user: User`, `type: NotificationType`, `title`, `message`, `relatedEventId`, `relatedUserId`, `actionUrl`, `isRead`, `createdAt` | Indexed on `(user_id, is_read)` and `created_at`. |

#### Enum

| Enum | Values |
|------|--------|
| `NotificationType` | `EVENT_APPROVED`, `EVENT_REJECTED`, `WAITLIST_PROMOTED`, `NEW_REVIEW`, `TRUST_PROMOTED`, `EVENT_CANCELLED`, `RSVP_CANCELLED`, `REPORT_RESOLVED` |

#### Architecture Pattern: Event-Driven

```
Service Layer → NotificationEventPublisher → Spring ApplicationEventPublisher
                                                    ↓
                                       NotificationEventListener (@EventListener)
                                                    ↓
                                       NotificationService.createNotification()
                                                    ↓
                                               Database
```

**Loose coupling:** Services call `publishEventNotification()` without knowing about the notification infrastructure.

#### Controller (`notification/controller/`)

| Controller | Base Path | Endpoints |
|-----------|-----------|-----------|
| `NotificationController` | `/api/notifications` | `GET /` (filter unread), `GET /unread-count`, `PATCH /{id}/read`, `PATCH /read-all`, `DELETE /{id}` |

---

### 3.7 MQTT Integration (`mqtt/`)

> **Purpose:** Receives official AStA events via MQTT and auto-publishes them.

#### Architecture Pattern: Adapter + Factory

```
MQTT Broker ──► OfficialEventListener ──► EventFactory.createOfficialEvent()
                                                │
                                                ▼
                                        OfficialEventAdapter (Adapter Pattern)
                                                │
                                                ▼
                                        EventService.saveOfficialEvent()
```

#### Files

| File | Purpose |
|------|---------|
| `MqttConfig.java` | Configures MQTT client factory, inbound channel (`university/events`), outbound channel (`university/alerts`). Auto-reconnect enabled. |
| `OfficialEventListener.java` | `@ServiceActivator` on `mqttEventInputChannel`. Deserializes JSON to `OfficialEventMessage`, calls `EventFactory.createOfficialEvent()`, saves via `EventService`. |
| `OfficialEventAdapter.java` | Implements `EventMessageTarget` (Adapter Pattern). Converts AStA format to internal `Event` entity. Creates/fetches synthetic AStA host user. |
| `OfficialEventMessage.java` | DTO for incoming MQTT JSON: `{activity_name, time, venue, organiser}`. |
| `EventMessageTarget.java` | Adapter interface contract. |

**Critical Report Alerts:** `ReportService` publishes critical reports (INAPPROPRIATE/FAKE_EVENT) via `AlertMqttGateway` to `university/alerts` topic.

---

## 4. Data Flow & Architecture Patterns

### 4.1 Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Controller Layer  (@RestController)                    │
│  → Input validation, HTTP status codes, auth checks     │
├─────────────────────────────────────────────────────────┤
│  Service Layer     (@Service)                           │
│  → Business logic, transactions, cross-domain calls     │
├─────────────────────────────────────────────────────────┤
│  Repository Layer  (@Repository)                        │
│  → Spring Data JPA, custom @Query methods               │
├─────────────────────────────────────────────────────────┤
│  Entity Layer      (@Entity)                            │
│  → JPA mappings, relationships, constraints             │
└─────────────────────────────────────────────────────────┘
```

### 4.2 Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Factory** | `EventFactory` | Centralizes event creation from multiple sources (REST, MQTT) with consistent defaults |
| **Adapter** | `EventMessageTarget` / `OfficialEventAdapter` | Decouples AStA message format from internal Event model |
| **Observer** | `RsvpCancelledEvent` + `WaitlistPromotionListener` | Loose coupling between RSVP cancellation and waitlist promotion |
| **Publisher-Subscriber** | `NotificationEventPublisher` + `NotificationEventListener` | Decoupled notification system |
| **DTO** | All `*Dto`, `*Request` classes | Separates API contract from internal entities |
| **Mapper** | All `*Mapper` classes | Entity ↔ DTO conversion logic |

### 4.3 Event Publishing Flow (Example: RSVP Cancellation)

```
User cancels RSVP
      │
      ▼
RsvpService.cancelRsvp()
      │
      ├──► eventRepository.decrementRsvpCount()
      │
      ├──► rsvpEventPublisher.publishRsvpCancelled()
      │         │
      │         ▼
      │    WaitlistPromotionListener (@EventListener)
      │         │
      │         ▼
      │    WaitlistService.promoteNextWaitlistedUser()
      │         │
      │         ├──► notificationPublisher.publishEventNotification()
      │         │         │
      │         │         ▼
      │         │    NotificationEventListener (@EventListener)
      │         │         │
      │         │         ▼
      │         │    NotificationService.createNotification()
      │         │
      │         └──► sseService.notifyRsvpUpdate()
      │
      └──► notificationPublisher.publishEventNotification() → Host gets "RSVP Cancelled" notification
```

---

## 5. Security Architecture

### 5.1 Authentication Flow

```
Client ──► POST /api/auth/register
               │
               ▼
        UserService.register()
               │
               ├──► Create User (isVerified=false)
               ├──► Generate VerificationToken (24h)
               └──► EmailService.sendVerificationEmail()
               │
Client ──► GET /api/auth/verify?token=...
               │
               ▼
        UserService.verifyAccount()
               │
               └──► Set isVerified=true

Client ──► POST /api/auth/login
               │
               ▼
        AuthenticationManager.authenticate()
               │
               ├──► UserDetailsServiceImpl.loadUserByUsername()
               │         ├──► disabled(!isVerified) → 403
               │         └──► accountLocked(FLAGGED) → 403
               │
               └──► JwtUtil.generateAccessToken() + generateRefreshToken()
```

### 5.2 Authorization Matrix

| Endpoint Prefix | Required Role | Notes |
|----------------|---------------|-------|
| `/api/auth/**` | PermitAll | Registration, login, verification, password reset |
| `/api/public/**` | PermitAll | Browse events, categories, user profiles |
| `/api/admin/**` | ADMIN | Dashboard, event moderation, user management |
| `/api/events` POST/DELETE | Authenticated | Any logged-in user |
| `/api/reports` POST | Authenticated | Any logged-in user |
| `/api/reviews` POST | Authenticated | Any logged-in user |
| `/api/rsvps` POST | Authenticated | Any logged-in user |
| All other `/api/**` | Authenticated | Default fallback |

### 5.3 JWT Token Structure

**Access Token (15 minutes):**
```json
{
  "sub": "user@stud.fh-dortmund.de",
  "userId": "uuid",
  "role": "STUDENT",
  "trustLevel": "NEW",
  "type": "access",
  "iat": 1234567890,
  "exp": 1234568790
}
```

**Refresh Token (7 days):**
```json
{
  "sub": "user@stud.fh-dortmund.de",
  "userId": "uuid",
  "type": "refresh",
  "iat": 1234567890,
  "exp": 1235172690
}
```

### 5.4 Rate Limiting

| Endpoint Type | Limit | Window |
|--------------|-------|--------|
| `/api/auth/**` | 5 requests | 1 minute |
| Write endpoints (`/api/reviews`, `/api/reports`, `/api/rsvps`, `/api/events`) | 20 requests | 1 minute |
| GET / OPTIONS / HEAD | Unlimited | — |

---

## 6. API Endpoint Reference

### 6.1 Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/register` | Register with university email | No |
| POST | `/login` | Login with email + password | No |
| POST | `/refresh` | Refresh access token via refresh token | No |
| POST | `/logout` | Blacklist current token | Yes |
| GET | `/verify?token=` | Verify email address | No |
| POST | `/resend-verification` | Resend verification email | No |
| POST | `/forgot-password` | Request password reset | No |
| POST | `/reset-password` | Reset password with token | No |
| GET | `/me` | Get current user profile | Yes |
| PUT | `/me` | Update profile (multipart) | Yes |
| PUT | `/me/password` | Change password | Yes |
| DELETE | `/me` | Delete own account | Yes |
| GET | `/me/trust-status` | Get trust qualification status | Yes |

### 6.2 User Preferences (`/api/auth/me/preferences`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/` | Get notification preferences | Yes |
| PUT | `/` | Update preferences | Yes |

### 6.3 Public Events (`/api/public/events`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/` | Browse published events (filter by category, date, location, query) | No |
| GET | `/{eventId}` | Get single published event | No |
| GET | `/slug/{slug}` | Get event by slug | No |
| GET | `/featured` | Get 6 upcoming featured events | No |

### 6.4 Public Categories (`/api/public/categories`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/` | List all categories with icons/colors | No |

### 6.5 Public Users (`/api/public/users`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/{userId}` | Get public profile with trust metrics | No |

### 6.6 Events (`/api/events`) — Authenticated

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create event (trust-based auto-publish or review) |
| POST | `/draft` | Create draft event (partial data) |
| PUT | `/{eventId}/publish` | Publish draft (validates dates) |
| GET | `/{eventId}` | Get event detail |
| GET | `/by-slug/{slug}` | Get event by slug |
| GET | `/` | List published events with filters |
| GET | `/my-events` | List my events (optionally include deleted) |
| PUT | `/{eventId}` | Update event |
| PATCH | `/{eventId}/cancel` | Cancel event (notifies attendees) |
| DELETE | `/{eventId}` | Soft delete event (trash bin) |
| PATCH | `/{eventId}/restore` | Restore soft-deleted event |
| DELETE | `/{eventId}/permanent` | Permanently delete event |
| POST | `/{eventId}/media` | Upload images/videos (multipart) |
| DELETE | `/{eventId}/media/{mediaId}` | Delete media |
| PATCH | `/{eventId}/media/reorder` | Reorder media display |
| GET | `/{eventId}/check-in-code` | Generate QR check-in code (host only) |

### 6.7 Search (`/api/search`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/suggestions?q=&type=` | Autocomplete suggestions | No |

### 6.8 RSVP (`/api`) — Authenticated

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/events/{eventId}/rsvps` | RSVP to event (GOING or WAITLISTED) |
| GET | `/events/{eventId}/rsvps/me` | Get my RSVP for event |
| GET | `/rsvps/me` | List all my RSVPs |
| PATCH | `/rsvps/{rsvpId}/cancel` | Cancel my RSVP |
| GET | `/rsvps/{rsvpId}/position` | Get waitlist position |
| GET | `/events/{eventId}/rsvps` | List event RSVPs (host only) |
| GET | `/events/{eventId}/rsvps/status/{status}` | Filter RSVPs by status (host only) |
| PATCH | `/events/{eventId}/rsvps/{rsvpId}/attended` | Mark attendee as attended (host only) |
| PATCH | `/events/{eventId}/rsvps/{rsvpId}/promote` | Promote waitlisted user (host only) |
| POST | `/events/{eventId}/check-in` | Self check-in with QR code |

### 6.9 Reviews (`/api`) — Authenticated

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reviews` | Create review (must have attended) |
| GET | `/reviews/event/{eventId}` | List event reviews (sorted by helpfulness) |
| GET | `/reviews/host/{hostId}` | List host's event reviews |
| DELETE | `/reviews/{reviewId}` | Delete review (reviewer or admin) |
| POST | `/reviews/{reviewId}/helpful` | Toggle helpful vote |
| POST | `/reviews/{reviewId}/report` | Report review as inappropriate |

### 6.10 Reports (`/api`) — Authenticated

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| POST | `/reports` | Submit event report | Any |
| GET | `/admin/reports` | List all reports | Admin |
| GET | `/admin/reports/{reportId}` | Get report detail | Admin |
| GET | `/admin/reports/status/{status}` | Filter by status | Admin |
| GET | `/admin/reports/reason/{reason}` | Filter by reason | Admin |
| PATCH | `/admin/reports/{reportId}/resolve` | Resolve report (optional flag event) | Admin |
| DELETE | `/admin/reports/{reportId}` | Delete report | Admin |

### 6.11 Notifications (`/api/notifications`) — Authenticated

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List notifications (optionally unread only) |
| GET | `/unread-count` | Get unread notification count |
| PATCH | `/{notificationId}/read` | Mark single notification read |
| PATCH | `/read-all` | Mark all notifications read |
| DELETE | `/{notificationId}` | Delete notification |

### 6.12 Admin Dashboard (`/api/admin/dashboard`) — Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get dashboard statistics |

### 6.13 Admin Events (`/api/admin/events`) — Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List all events (filter by status) |
| GET | `/pending` | List pending (UNDER_REVIEW) events |
| PATCH | `/{eventId}/approve` | Approve event → PUBLISHED |
| PATCH | `/{eventId}/reject` | Reject event → CANCELLED |
| PATCH | `/{eventId}/flag` | Flag event → UNDER_REVIEW |
| POST | `/bulk-approve` | Bulk approve events |
| POST | `/bulk-reject` | Bulk reject events |

### 6.14 Admin Categories (`/api/admin/categories`) — Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List all categories |
| POST | `/` | Create category |
| PUT | `/{id}` | Update category |
| DELETE | `/{id}` | Delete category |

### 6.15 Admin Users (`/api/admin/users`) — Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | List users (search + trust level filter) |
| GET | `/{userId}` | Get user detail |
| PATCH | `/{userId}/trust-level` | Update trust level |
| POST | `/{userId}/flag` | Flag user |
| POST | `/{userId}/promote` | Promote to trusted host |
| DELETE | `/{userId}` | Delete user |

### 6.16 SSE (`/api/events/stream`) — Authenticated

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{eventId}` | Subscribe to real-time event updates (RSVP, waitlist, cancellation) |

---

## 7. Entity Relationship Diagram (Conceptual)

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    User     │◄──────┤   Event     │◄──────┤   Rsvp      │
│  (identity) │  1:N  │   (events)  │  1:N  │(registration│
│             │ host  │             │       │             │
└──────┬──────┘       └──────┬──────┘       └──────┬──────┘
       │                     │                      │
       │              ┌──────┴──────┐               │
       │              │             │               │
       │         ┌────┴────┐   ┌────┴────┐          │
       │         │Category │   │EventMedia│          │
       │         │(events) │   │ (events) │          │
       │         └─────────┘   └─────────┘          │
       │              ▲                              │
       │              │ N:M                          │
       │         ┌────┴────┐                        │
       │         │EventCat.│                        │
       │         └─────────┘                        │
       │                                            │
       │         ┌─────────────┐                   │
       │         │   Review    │◄──────────────────┘
       └────────►│ (moderation)│  (reviewer)
         1:N     └──────┬──────┘
                        │
                   ┌────┴────┐
                   │ReviewVote│
                   └─────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   Report    │◄──────┤ Notification│       │UserPreference│
│(moderation) │       │(notification│       │ (identity)  │
│             │       │             │       │             │
└─────────────┘       └─────────────┘       └─────────────┘

┌─────────────────────────────────────────────────────────┐
│  Supporting: VerificationToken, PasswordResetToken      │
│  (identity)                                             │
└─────────────────────────────────────────────────────────┘
```

---

## 8. Key Design Decisions

### 8.1 Trust-Based Publishing
- **NEW** users: Events go to `UNDER_REVIEW` (admin approval required)
- **TRUSTED_HOST** / **ADMIN**: Events are `PUBLISHED` immediately
- Auto-promotion: ≥3 completed events with reviews + average rating ≥4.0

### 8.2 Soft Delete for Events
- Events have `deletedAt` timestamp instead of hard deletion
- Hosts can restore from trash bin
- Permanent delete requires soft-delete first (safety) or admin privileges
- Deleted events excluded from public feeds

### 8.3 Atomic Capacity Management
- RSVP count updated via `@Modifying @Query` to prevent race conditions
- `incrementRsvpCount()` only succeeds if `currentRsvpCount < maxCapacity`
- Waitlist promotion uses pessimistic locking (`findByIdLocked`)

### 8.4 Denormalized Review Helpfulness
- `Review.helpfulCount` is updated directly instead of counting votes each time
- `ReviewVote` table still exists for per-user tracking
- Trade-off: slight inconsistency risk vs. read performance

### 8.5 File Storage Strategy
- Avatars: `/uploads/avatars/avatar_{userId}.{ext}`
- Event media: `/uploads/events/{eventId}/images|videos/{uuid}.{ext}`
- Thumbnails generated alongside originals: `{name}_thumb.{ext}`, `{name}_medium.{ext}`
- Spring `ResourceHandler` serves files directly from disk

### 8.6 Notification Deduplication
- `NotificationRepository.existsByUserIdAndTypeAndRelatedEventId()` prevents duplicate notifications
- Example: Only one "EVENT_APPROVED" notification per event per user

### 8.7 MQTT Critical Alerts
- Reports with reason `INAPPROPRIATE` or `FAKE_EVENT` trigger MQTT alert to AStA backend
- Payload includes severity level, reporter/host emails, timestamp
- Enables rapid response by university administration

### 8.8 Scheduled Jobs
| Job | Schedule | Purpose |
|-----|----------|---------|
| `autoCompletePastEvents` | Every hour at :00 | Flip past PUBLISHED events to COMPLETED |
| `purgeExpiredTokens` | Daily at 3:00 AM | Clean up expired verification/reset tokens |

---

## Appendix: Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (jjwt) |
| Data Access | Spring Data JPA + Hibernate |
| Database | PostgreSQL (implied by JPA annotations) |
| File Storage | Local filesystem (configurable path) |
| Email | Spring Mail (JavaMailSender) |
| MQTT | Eclipse Paho + Spring Integration |
| Image Processing | Thumbnailator |
| API Docs | OpenAPI 3.0 (Swagger) |
| Build | Maven/Gradle (implied) |

---

*Documentation generated from source code analysis. Total analysis: 180 files, 116 classes, 14 interfaces, 8 enums, 80+ REST endpoints across 16 controllers.*
