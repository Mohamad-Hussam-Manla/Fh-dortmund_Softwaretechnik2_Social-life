# MyStudyApp — Campus Event Platform

> A modern, full-stack campus event platform for verified university students. Built with a **Spring Boot 3.x** monolith (`backend-main`), a **Spring Boot MQTT publisher** (`backend-asta`), and a **React/Vite** frontend. Features real-time SSE updates, trust-based hosting, comprehensive moderation, and AStA MQTT integration.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Repository Structure](#repository-structure)
3. [Tech Stack](#tech-stack)
4. [Architecture](#architecture)
5. [Key Features](#key-features)
6. [Design System](#design-system)
7. [API Overview](#api-overview)
8. [Getting Started](#getting-started)
9. [Environment Variables](#environment-variables)
10. [Development Guidelines](#development-guidelines)
11. [Deployment](#deployment)
12. [Documentation](#documentation)
13. [License](#license)

---

## Project Overview

MyStudyApp is a **campus event platform** designed specifically for verified university students. It enables students to discover, create, and manage campus events with a focus on trust, safety, and community.

### Core Capabilities

- 🔐 **JWT Authentication** — University email verification, password reset, token refresh
- 🎫 **Event Management** — CRUD operations, media uploads, drafts, publishing workflow
- 📝 **RSVP & Waitlist** — Atomic capacity management with real-time updates
- ⭐ **Reviews & Ratings** — Post-event reviews with helpfulness voting
- 🚨 **Reports & Moderation** — User-generated reports with admin resolution
- 🔔 **Notifications** — In-app notification system with deep-linking
- 📡 **Real-Time SSE** — Live RSVP/waitlist updates via Server-Sent Events
- 📡 **MQTT Integration** — Receives official AStA events via MQTT broker
- 👤 **Trust System** — Host promotion based on completed events + ratings
- 🛡️ **Admin Dashboard** — Event approval, user management, analytics

---

## Repository Structure

```
campus_event_app/
│
├── backend-main/              # Main Spring Boot application (monolith)
│   ├── src/main/java/de/fhdortmund/mystudyapp/
│   │   ├── common/            # Config, security, exceptions, file storage
│   │   │   ├── config/        # CorsConfig, SecurityConfig, OpenApiConfig, etc.
│   │   │   ├── exception/     # GlobalExceptionHandler, custom exceptions
│   │   │   ├── response/      # ApiResponse<T>, PageResponse<T>
│   │   │   ├── scheduler/     # TokenCleanupService
│   │   │   ├── security/      # JwtAuthFilter, JwtUtil, RateLimitingFilter
│   │   │   └── service/       # FileStorageService, ThumbnailService
│   │   ├── events/            # Event CRUD, media, categories, search, SSE
│   │   │   ├── controller/    # EventController, AdminEventController, EventSseController, etc.
│   │   │   ├── dto/           # EventDto, CreateEventRequest, CheckInCodeDto, etc.
│   │   │   ├── factory/       # EventFactory
│   │   │   ├── mapper/        # EventMapper
│   │   │   ├── model/         # Event, Category, EventMedia, EventStatus, MediaType
│   │   │   ├── repository/    # EventRepository, CategoryRepository, EventMediaRepository
│   │   │   └── service/       # EventService, EventLifecycleService, EventSseService, SearchService
│   │   ├── identity/          # Auth, users, profiles, trust levels, preferences
│   │   │   ├── controller/    # UserController, AdminUserController, PublicUserController
│   │   │   ├── dto/           # UserDto, AuthResponse, RegisterRequest, etc.
│   │   │   ├── mapper/        # UserMapper, PublicProfileMapper
│   │   │   ├── model/         # User, Role, TrustLevel, VerificationToken, PasswordResetToken
│   │   │   ├── repository/    # UserRepository, VerificationTokenRepository, etc.
│   │   │   └── service/       # UserService, TrustLevelService, EmailService, etc.
│   │   ├── moderation/        # Reviews, reports, helpful votes
│   │   │   ├── controller/    # ReviewController, ReportController
│   │   │   ├── dto/           # ReviewDto, ReportDto, CreateReviewRequest, etc.
│   │   │   ├── mapper/        # ReviewMapper, ReportMapper
│   │   │   ├── model/         # Review, ReviewVote, Report, ReportReason, ReportStatus
│   │   │   ├── repository/    # ReviewRepository, ReportRepository, ReviewVoteRepository
│   │   │   └── service/       # ReviewService, ReportService
│   │   ├── mqtt/              # MQTT integration for official AStA events
│   │   │   ├── adapter/       # EventMessageTarget (adapter interface)
│   │   │   ├── config/        # MqttConfig
│   │   │   ├── dto/           # OfficialEventMessage, OfficialEventAdapter
│   │   │   └── listener/      # OfficialEventListener
│   │   ├── notification/      # In-app notifications (event-driven)
│   │   │   ├── controller/    # NotificationController
│   │   │   ├── dto/           # NotificationDto
│   │   │   ├── listener/      # NotificationEventListener
│   │   │   ├── mapper/        # NotificationMapper
│   │   │   ├── model/         # Notification, NotificationType
│   │   │   ├── publisher/     # NotificationEvent, NotificationEventPublisher
│   │   │   ├── repository/    # NotificationRepository
│   │   │   └── service/       # NotificationService
│   │   └── registration/      # RSVPs, waitlist, check-in
│   │       ├── controller/    # RsvpController
│   │       ├── dto/           # RsvpDto, CancelRsvpRequest, WaitlistPromotionDto
│   │       ├── mapper/        # RsvpMapper
│   │       ├── model/         # Rsvp, RsvpStatus
│   │       ├── observer/      # RsvpCancelledEvent, RsvpEventPublisher, WaitlistPromotionListener
│   │       ├── repository/    # RsvpRepository
│   │       └── service/       # RsvpService, WaitlistService
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/      # Flyway migrations (V1–V4)
│   ├── src/test/java/         # Unit & integration tests
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── HELP.md
│
├── backend-asta/              # Spring Boot MQTT publisher (AStA integration)
│   ├── src/main/java/de/fhdortmund/mystudyapp/asta/
│   │   ├── AstaApplication.java
│   │   ├── controller/        # AstaController
│   │   ├── dto/               # AstaEventRequest
│   │   ├── mqtt/              # MqttPublisherConfig
│   │   └── service/           # AstaPublisherService
│   ├── src/test/java/         # Unit tests
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── HELP.md
│
├── frontend/                  # React 18 + Vite frontend
│   ├── public/
│   │   ├── favicon.svg
│   │   ├── manifest.json      # PWA manifest
│   │   ├── service-worker.js  # PWA service worker
│   │   └── icons/             # PWA icons (192px, 512px, maskable)
│   ├── src/
│   │   ├── api/               # Axios instances + API modules
│   │   │   ├── eventsApi.js
│   │   │   ├── moderationApi.js
│   │   │   ├── rsvpApi.js
│   │   │   └── usersApi.js
│   │   ├── components/
│   │   │   ├── atoms/         # Avatar, Badge, Button, Input, Skeleton, Spinner
│   │   │   ├── molecules/     # CapacityBar, CategoryChip, EventCard, FormField, RatingStars, SearchBar, Toast, UserTrustBadge
│   │   │   ├── organisms/     # AdminReportTable, EventFeed, EventForm, Navbar, ReviewSection, ToastContainer, WaitlistBanner
│   │   │   └── templates/     # AdminLayout, AuthLayout, ErrorBoundary, PageLayout
│   │   ├── constants/         # enums.js, queryKeys.js, routes.js
│   │   ├── design-system/     # animations.css, breakpoints.js, theme.js, tokens.css
│   │   ├── hooks/             # useAuth, useDebounce, useEvents, useInfiniteEvents, useMediaQuery, useRsvp, useToast
│   │   ├── lib/               # axios.js, i18n.js, queryClient.js
│   │   ├── pages/
│   │   │   ├── Admin/         # AdminDashboardPage
│   │   │   ├── Auth/          # LoginPage, RegisterPage
│   │   │   ├── Error/         # ErrorPage, NotFoundPage
│   │   │   ├── Events/        # CreateEventPage, EditEventPage, EventDetailPage, EventsPage
│   │   │   ├── Home/          # HomePage
│   │   │   └── Profile/       # ProfilePage
│   │   ├── stores/            # Zustand stores (authStore, uiStore)
│   │   ├── types/             # TypeScript interfaces
│   │   └── utils/             # cn.js, dateFormatter, errorMessages, validators
│   ├── index.html
│   ├── vite.config.js
│   ├── eslint.config.js
│   ├── package.json
│   └── README.md
│
├── docs/                      # Project documentation
│   ├── MyStudyApp_Backend_Architecture.md
│   ├── MyStudyApp_Frontend_Guide.md
│   └── MyStudyApp_Modern_UIUX_Specification_v3.md
│
├── docker-compose.yml         # Docker orchestration
├── mosquitto.conf             # MQTT broker configuration
├── pom.xml                    # Root Maven config (multi-module)
└── README.md                  # This file
```

---

## Tech Stack

### Backend — Main (`backend-main`)

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x (Java) |
| Security | Spring Security + JWT (jjwt, HS512) |
| Data Access | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Migrations | Flyway |
| File Storage | Local filesystem (`uploads/`) |
| Email | Spring Mail (JavaMailSender) |
| MQTT | Eclipse Paho + Spring Integration |
| Image Processing | Thumbnailator |
| API Docs | OpenAPI 3.0 (Swagger) |
| Build | Maven |

### Backend — AStA Publisher (`backend-asta`)

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x |
| MQTT | Spring Integration MQTT |
| Build | Maven |

### Frontend (`frontend`)

| Layer | Technology |
|-------|-----------|
| Framework | React 18+ |
| Build Tool | Vite |
| Language | JavaScript (JSX) |
| State Management | Zustand + TanStack Query |
| HTTP Client | Axios (with interceptors) |
| Styling | CSS Modules + design tokens |
| Icons | Lucide React |
| Real-Time | Native EventSource (SSE) |
| PWA | Service Worker + Manifest |
| Linting | ESLint |

### Infrastructure

| Service | Technology |
|---------|-----------|
| Container Orchestration | Docker Compose |
| MQTT Broker | Eclipse Mosquitto |
| Reverse Proxy | Nginx (production) |

---

## Architecture

### Backend — Domain-Driven Design

The `backend-main` module is organized by domain with clear separation of concerns:

```
Controller Layer  (@RestController)
    → Input validation, HTTP status codes, auth checks
Service Layer     (@Service)
    → Business logic, transactions, cross-domain calls
Repository Layer  (@Repository)
    → Spring Data JPA, custom @Query methods
Entity Layer      (@Entity)
    → JPA mappings, relationships, constraints
```

### Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Factory** | `EventFactory` | Centralizes event creation from REST + MQTT |
| **Adapter** | `EventMessageTarget` / `OfficialEventAdapter` | Decouples AStA message format |
| **Observer** | `RsvpCancelledEvent` + `WaitlistPromotionListener` | Loose coupling RSVP → waitlist |
| **Publisher-Subscriber** | `NotificationEventPublisher` + `NotificationEventListener` | Decoupled notifications |
| **DTO** | All `*Dto`, `*Request` classes | API contract separation |
| **Mapper** | All `*Mapper` classes | Entity ↔ DTO conversion |

### Frontend — Component-Driven Architecture

Organized by atomic design principles:
- **Atoms**: Basic building blocks (Button, Input, Avatar, Badge)
- **Molecules**: Composite components (EventCard, CapacityBar, Toast)
- **Organisms**: Complex sections (EventFeed, Navbar, ReviewSection)
- **Templates**: Page layouts (AuthLayout, AdminLayout, PageLayout)
- **Pages**: Route-level views

### Data Flow — RSVP Cancellation Example

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
      │         │    NotificationEventListener
      │         │         │
      │         │         ▼
      │         │    NotificationService.createNotification()
      │         │
      │         └──► sseService.notifyRsvpUpdate()
      │
      └──► notificationPublisher.publishEventNotification()
            → Host gets "RSVP Cancelled" notification
```

---

## Key Features

### Authentication & Trust System
- University email verification (blocks free providers: Gmail, GMX, Yahoo, etc.)
- JWT access tokens (15 min) + refresh tokens (7 days)
- Role-based access: `STUDENT` / `ADMIN`
- Trust levels: `NEW` → `TRUSTED_HOST` → `FLAGGED`
- Auto-promotion: ≥3 completed events with reviews + average rating ≥4.0

### Event Lifecycle
```
DRAFT → UNDER_REVIEW → PUBLISHED → COMPLETED
         ↓              ↓
       CANCELLED     (auto-completed after endTime)
```
- **NEW hosts**: Events require admin approval (`UNDER_REVIEW`)
- **TRUSTED_HOSTS / ADMIN**: Events auto-publish (`PUBLISHED`)
- Soft delete with trash bin (restorable)
- Permanent delete cleans up all associated data

### RSVP & Waitlist
- Atomic capacity checks via `@Modifying @Query`
- Automatic waitlist promotion on cancellation
- Real-time updates via SSE
- QR code check-in system (6-char codes, 5-min refresh)

### Reviews & Moderation
- Only attendees can review (after event ends)
- Helpfulness voting with denormalized counter
- Event reports with critical alerts (INAPPROPRIATE/FAKE_EVENT → MQTT to AStA)
- Admin moderation queue with bulk actions

### Notifications
- 8 notification types with deep-linking
- Deduplication per user/type/event
- Unread badge with real-time polling

### MQTT Integration
- `backend-asta` publishes official AStA events to MQTT broker
- `backend-main` subscribes and auto-creates events via `EventFactory`
- Critical reports (INAPPROPRIATE/FAKE_EVENT) published back to `university/alerts`

---

## Design System

### Color Palette

| Token | Value | Usage |
|-------|-------|-------|
| `--primary-500` | `#6366f1` | Main action |
| `--primary-600` | `#4f46e5` | Hover/Active |
| `--success-500` | `#22c55e` | Success states |
| `--warning-500` | `#f59e0b` | Waitlist, warnings |
| `--error-500` | `#ef4444` | Errors, destructive |

### Trust Level Colors

| Level | Background | Text | Border |
|-------|-----------|------|--------|
| `NEW` | `#f3f4f6` | `#6b7280` | `#e5e7eb` |
| `TRUSTED_HOST` | `#d1fae5` | `#059669` | `#a7f3d0` |
| `FLAGGED` | `#fee2e2` | `#dc2626` | `#fecaca` |

### Typography

- **Font**: `Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`
- **Monospace**: `"JetBrains Mono", "SF Mono", ui-monospace, monospace`
- **Scale**: 4px base grid, 11px overline to 56px display

### Component Highlights

- **Event Card**: 16:9 thumbnail, host badge, capacity bar with animated fill, RSVP button with 8 distinct states
- **RSVP Button**: Morphing states (Join → Going → Waitlist #N → Attended) with width animations
- **Media Carousel**: Blurhash placeholder → low-res → full image, pinch-to-zoom, thumbnail strip
- **Command Palette**: `Cmd/Ctrl + K` global search with keyboard navigation
- **Toast System**: 5 types, max 4 stack, swipe-to-dismiss (mobile), auto-dismiss with progress bar

---

## API Overview

### Base URL
```
http://localhost:8080
```

### Authentication
```
Authorization: Bearer <accessToken>
X-Refresh-Token: <refreshToken>
```

### Response Format
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-05-20T15:30:00Z"
}
```

### Pagination
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false
}
```

### Rate Limits

| Endpoint Type | Limit | Window |
|--------------|-------|--------|
| `/api/auth/**` | 5 requests | 1 minute |
| Write endpoints | 20 requests | 1 minute |
| GET/HEAD/OPTIONS | Unlimited | — |

### Key Endpoints

#### Public (No Auth)
- `GET /api/public/events` — Browse published events
- `GET /api/public/events/featured` — Featured events (6)
- `GET /api/public/categories` — Category listing
- `GET /api/public/users/{id}` — Public profiles
- `GET /api/search/suggestions` — Autocomplete

#### Authentication
- `POST /api/auth/register` — Register with university email
- `POST /api/auth/login` — Login
- `POST /api/auth/refresh` — Token refresh
- `GET /api/auth/me` — Current user profile
- `PUT /api/auth/me` — Update profile (multipart)

#### Events (Authenticated)
- `POST /api/events` — Create event
- `POST /api/events/draft` — Save draft
- `PUT /api/events/{id}/publish` — Publish draft
- `GET /api/events/{id}` — Event detail
- `GET /api/events/my-events` — My events
- `PATCH /api/events/{id}/cancel` — Cancel event
- `POST /api/events/{id}/media` — Upload media

#### RSVP (Authenticated)
- `POST /api/events/{id}/rsvps` — RSVP
- `PATCH /api/rsvps/{id}/cancel` — Cancel RSVP
- `GET /api/rsvps/{id}/position` — Waitlist position
- `POST /api/events/{id}/check-in` — Self check-in

#### Reviews (Authenticated)
- `POST /api/reviews` — Create review
- `GET /api/reviews/event/{id}` — Event reviews
- `POST /api/reviews/{id}/helpful` — Toggle helpful

#### Admin (ADMIN only)
- `GET /api/admin/dashboard` — Dashboard stats
- `GET /api/admin/events/pending` — Moderation queue
- `PATCH /api/admin/events/{id}/approve` — Approve event
- `GET /api/admin/reports` — Reports queue
- `GET /api/admin/users` — User management

#### SSE (Authenticated)
- `GET /api/events/stream/{eventId}` — Real-time updates

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven
- Docker & Docker Compose (optional)

### Option 1: Docker Compose (Recommended)

```bash
# 1. Clone the repository
git clone <repo-url>
cd campus_event_app

# 2. Start all services
docker-compose up -d

# Services started:
# - backend-main:    http://localhost:8080
# - backend-asta:    http://localhost:8081
# - frontend:        http://localhost:5173
# - postgres:        localhost:5432
# - mosquitto (MQTT): localhost:1883
```

### Option 2: Manual Setup

#### 1. PostgreSQL

```bash
# Create database
createdb mystudyapp
# or
psql -c "CREATE DATABASE mystudyapp;"
```

#### 2. MQTT Broker (Mosquitto)

```bash
# Using Docker
docker run -d -p 1883:1883 -p 9001:9001 \
  -v $(pwd)/mosquitto.conf:/mosquitto/config/mosquitto.conf \
  eclipse-mosquitto

# Or install locally and run:
mosquitto -c mosquitto.conf
```

#### 3. Backend — Main

```bash
cd backend-main

# Configure database & email in src/main/resources/application-dev.yml
# Edit: spring.datasource.url, username, password
# Edit: app.frontend-url, spring.mail.*

# Run with Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run JAR
./mvnw clean package -DskipTests
java -jar target/backend-main-*.jar --spring.profiles.active=dev
```

Backend starts on `http://localhost:8080`.

#### 4. Backend — AStA Publisher

```bash
cd backend-asta

# Configure MQTT broker in src/main/resources/application.yml

./mvnw spring-boot:run
```

Backend starts on `http://localhost:8081`.

#### 5. Frontend

```bash
cd frontend

# Install dependencies
npm install

# Configure environment
cp .env.example .env
# Edit .env:
# VITE_API_URL=http://localhost:8080

# Start development server
npm run dev
```

Frontend starts on `http://localhost:5173`.

---

## Environment Variables

### Backend — Main (`backend-main/src/main/resources/application.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mystudyapp
    username: postgres
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

app:
  frontend-url: http://localhost:5173
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration: 900000      # 15 minutes
    refresh-token-expiration: 604800000  # 7 days

storage:
  upload-dir: ./uploads
  max-image-size: 5242880
  max-video-size: 20971520
```

### Backend — AStA (`backend-asta/src/main/resources/application.yml`)

```yaml
server:
  port: 8081

spring:
  mqtt:
    broker-url: tcp://localhost:1883
    client-id: asta-publisher
    username: ${MQTT_USERNAME}
    password: ${MQTT_PASSWORD}

topics:
  publish: university/events
```

### Frontend (`frontend/.env`)

```bash
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=MyStudyApp
VITE_DEFAULT_LANGUAGE=de
VITE_DEFAULT_TIMEZONE=Europe/Berlin

# Upload Limits
VITE_MAX_AVATAR_SIZE=5242880        # 5MB
VITE_MAX_IMAGE_SIZE=5242880         # 5MB
VITE_MAX_VIDEO_SIZE=20971520        # 20MB
VITE_MAX_IMAGES_PER_EVENT=5
VITE_MAX_VIDEOS_PER_EVENT=2

# SSE
VITE_SSE_RETRY_MAX=3
VITE_SSE_RETRY_BASE_MS=5000

# Features
VITE_ENABLE_PWA=true
VITE_ENABLE_PUSH_NOTIFICATIONS=true
VITE_SENTRY_DSN=                    # Optional
```

---

## Development Guidelines

### Backend

1. **Follow the layered architecture**: Controller → Service → Repository → Entity
2. **Use DTOs for all API boundaries** — never expose entities directly
3. **Validate at the controller** using `@Valid` and `BindingResult`
4. **Use custom exceptions** with `GlobalExceptionHandler` for consistent error responses
5. **Rate limiting is enforced** — design UI to handle 429 responses gracefully
6. **File uploads**: Validate type/size before processing, generate thumbnails for images
7. **JWT tokens**: Access tokens in memory, refresh tokens in httpOnly cookies
8. **Trust system**: Auto-promotion runs after review creation, manual via admin
9. **Flyway migrations**: All schema changes must be versioned in `db/migration/`

### Frontend

1. **Component organization**: Follow atomic design (atoms → molecules → organisms → templates → pages)
2. **State management**: TanStack Query for server state, Zustand for client state
3. **Optimistic UI** for RSVP actions — rollback on error with toast
4. **Axios interceptors** handle token refresh, 401 redirect, retry logic
5. **SSE Manager** singleton handles connections, auto-reconnect, cleanup
6. **Form validation** both client-side (real-time) and server-side (on submit)
7. **Accessibility**: WCAG 2.1 AA — focus traps, ARIA labels, keyboard shortcuts
8. **Performance**: Route splitting, image lazy loading, skeleton screens

### API Contract Rules

- All endpoints return `ApiResponse<T>` wrapper
- Pagination uses Spring `Pageable` (page, size, sort)
- File uploads use `multipart/form-data`
- Auth header: `Authorization: Bearer <token>`
- Refresh header: `X-Refresh-Token: <refresh>`
- Dates: ISO 8601 Instant (UTC) — frontend converts to local timezone

---

## Deployment

### Production Build

```bash
# Backend — Main
cd backend-main
./mvnw clean package -DskipTests

# Backend — ASta
cd backend-asta
./mvnw clean package -DskipTests

# Frontend
cd frontend
npm run build
# Output: dist/ folder
```

### Docker Compose (Production)

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mystudyapp
      POSTGRES_USER: mystudyapp
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  mosquitto:
    image: eclipse-mosquitto:2
    volumes:
      - ./mosquitto.conf:/mosquitto/config/mosquitto.conf
    ports:
      - "1883:1883"
      - "9001:9001"

  backend-main:
    build: ./backend-main
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/mystudyapp
      SPRING_DATASOURCE_USERNAME: mystudyapp
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      APP_JWT_SECRET: ${JWT_SECRET}
      APP_FRONTEND_URL: https://your-domain.com
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - mosquitto

  backend-asta:
    build: ./backend-asta
    environment:
      SPRING_MQTT_BROKER_URL: tcp://mosquitto:1883
    ports:
      - "8081:8081"
    depends_on:
      - mosquitto

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend-main

volumes:
  postgres_data:
```

### Manual Deployment

```bash
# 1. Build backend JARs
./mvnw clean package -DskipTests

# 2. Build frontend
npm run build

# 3. Serve frontend with Nginx
# Copy dist/ to /var/www/html/
# Configure nginx.conf for SPA routing (fallback to index.html)

# 4. Start backend services
java -jar backend-main/target/*.jar --spring.profiles.active=prod
java -jar backend-asta/target/*.jar

# 5. Start MQTT broker
mosquitto -c mosquitto.conf
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Backend Architecture](docs/MyStudyApp_Backend_Architecture.md) | Full domain module deep dive, entity relationships, data flow patterns |
| [Frontend Guide](docs/MyStudyApp_Frontend_Guide.md) | Complete API-to-UI contract map, TypeScript types, endpoint reference |
| [UI/UX Specification](docs/MyStudyApp_Modern_UIUX_Specification_v3.md) | Design system tokens, interaction blueprints, animation system, accessibility |

---

## License

MIT License — see [LICENSE](LICENSE) for details.

---

> **Built with 💜 for campus communities.** Every interaction is a conversation with the backend. Every pixel communicates state. Every animation reduces cognitive load.
