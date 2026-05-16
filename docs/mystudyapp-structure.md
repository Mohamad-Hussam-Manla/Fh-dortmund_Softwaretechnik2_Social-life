# MyStudyApp — Final Project Structure

> Maven multi-module · Spring Boot 3 Modulith · React 18 PWA
> Vertical-Slicing-Architecture · Two backends · MQTT via Mosquitto

---

```
mystudyapp/
├── pom.xml                                        # parent POM — declares all Maven modules
├── docker-compose.yml                             # one command: Mosquitto + PostgreSQL
├── mosquitto.conf                                 # allow_anonymous true · port 1883
├── .gitignore
├── README.md                                      # setup guide · architecture · MQTT topic docs
│
├── .github/
│   └── workflows/
│       ├── ci.yml                                 # on push: mvn test · vite build · lint
│       └── pr-check.yml                           # blocks merge if checks fail
│
├── docs/
│   ├── architecture.md                            # Verteilungsdiagramm description + MQTT flow
│   ├── api.md                                     # REST endpoint reference (links to Swagger)
│   └── design-patterns.md                         # Factory · Adapter · Observer — motivation + UML
│
│
├── backend-main/                                  # System 1 — Spring Boot Modulith (port 8080)
│   ├── pom.xml                                    # Spring Boot · Modulith · Security · MQTT · Flyway · MapStruct · JWT
│   └── src/
│       ├── main/
│       │   ├── java/de/fhdortmund/mystudyapp/
│       │   │   │
│       │   │   ├── MainApplication.java           # @SpringBootApplication · @EnableModulith
│       │   │   │
│       │   │   ├── common/                        # cross-cutting — shared by all slices
│       │   │   │   ├── response/
│       │   │   │   │   ├── ApiResponse.java       # uniform JSON envelope {data, message, status}
│       │   │   │   │   └── PageResponse.java      # wraps Spring Page<T> for paginated endpoints
│       │   │   │   ├── exception/
│       │   │   │   │   ├── GlobalExceptionHandler.java    # @ControllerAdvice — maps exceptions → HTTP
│       │   │   │   │   ├── ResourceNotFoundException.java # 404
│       │   │   │   │   ├── ForbiddenActionException.java  # 403
│       │   │   │   │   └── CapacityExceededException.java # 409
│       │   │   │   ├── config/
│       │   │   │   │   ├── SecurityConfig.java    # JWT filter · BCrypt · role-based route rules
│       │   │   │   │   ├── CorsConfig.java        # allows :5173 (Vite dev) and production origin
│       │   │   │   │   └── OpenApiConfig.java     # Swagger UI at /api-docs · JWT bearer scheme
│       │   │   │   └── security/
│       │   │   │       ├── JwtUtil.java           # generate · validate · parse JWT
│       │   │   │       └── JwtAuthFilter.java     # OncePerRequestFilter — reads Bearer token
│       │   │   │
│       │   │   ├── identity/                      # Module 1 — auth + user profiles
│       │   │   │   ├── controller/
│       │   │   │   │   └── UserController.java    # POST /api/auth/register
│       │   │   │   │                              # POST /api/auth/login
│       │   │   │   │                              # GET  /api/users/{id}
│       │   │   │   │                              # GET  /api/users/me
│       │   │   │   ├── service/
│       │   │   │   │   ├── UserService.java
│       │   │   │   │   └── TrustLevelService.java # upgrades to TRUSTED_HOST after 3 good reviews
│       │   │   │   ├── repository/
│       │   │   │   │   └── UserRepository.java
│       │   │   │   ├── model/
│       │   │   │   │   ├── User.java              # @Entity · id · universityEmail · passwordHash
│       │   │   │   │   │                          #         · displayName · bio · role · trustLevel
│       │   │   │   │   ├── Role.java              # enum: STUDENT · ADMIN
│       │   │   │   │   └── TrustLevel.java        # enum: NEW · TRUSTED_HOST · FLAGGED
│       │   │   │   ├── dto/
│       │   │   │   │   ├── UserDto.java
│       │   │   │   │   ├── RegisterRequest.java
│       │   │   │   │   ├── LoginRequest.java
│       │   │   │   │   └── AuthResponse.java      # returns JWT + UserDto on login
│       │   │   │   └── mapper/
│       │   │   │       └── UserMapper.java        # MapStruct — User ↔ UserDto
│       │   │   │
│       │   │   ├── events/                        # Module 2 — event catalog + discovery
│       │   │   │   ├── controller/
│       │   │   │   │   ├── EventController.java   # GET    /api/events          (paginated, filterable)
│       │   │   │   │   │                          # POST   /api/events
│       │   │   │   │   │                          # GET    /api/events/{id}
│       │   │   │   │   │                          # PATCH  /api/events/{id}
│       │   │   │   │   │                          # DELETE /api/events/{id}
│       │   │   │   │   └── CategoryController.java# GET    /api/categories
│       │   │   │   ├── service/
│       │   │   │   │   └── EventService.java      # enforces trust-level → PUBLISHED vs UNDER_REVIEW
│       │   │   │   ├── repository/
│       │   │   │   │   ├── EventRepository.java   # findByStatus · findByCategory · Page<Event>
│       │   │   │   │   └── CategoryRepository.java
│       │   │   │   ├── model/
│       │   │   │   │   ├── Event.java             # @Entity · title · description · location
│       │   │   │   │   │                          #         · startTime · endTime · maxCapacity
│       │   │   │   │   │                          #         · currentRsvpCount · status · hostId
│       │   │   │   │   ├── Category.java          # @Entity · id · name (Study Group, Nightlife…)
│       │   │   │   │   ├── EventCategory.java     # @Entity join table — composite PK
│       │   │   │   │   └── EventStatus.java       # enum: PUBLISHED · UNDER_REVIEW · CANCELLED
│       │   │   │   ├── dto/
│       │   │   │   │   ├── EventDto.java
│       │   │   │   │   └── CreateEventRequest.java
│       │   │   │   ├── mapper/
│       │   │   │   │   └── EventMapper.java       # MapStruct — Event ↔ EventDto
│       │   │   │   └── factory/
│       │   │   │       └── EventFactory.java      # ★ CREATIONAL: Factory Pattern
│       │   │   │                                  #   builds Event from CreateEventRequest
│       │   │   │                                  #   OR from OfficialEventMessage (MQTT)
│       │   │   │
│       │   │   ├── registration/                  # Module 3 — RSVP + waitlist
│       │   │   │   ├── controller/
│       │   │   │   │   └── RsvpController.java    # POST   /api/rsvps
│       │   │   │   │                              # DELETE /api/rsvps/{id}
│       │   │   │   │                              # GET    /api/events/{id}/attendees
│       │   │   │   ├── service/
│       │   │   │   │   ├── RsvpService.java       # checks capacity → GOING or WAITLISTED
│       │   │   │   │   └── WaitlistService.java   # auto-promotes next WAITLISTED on cancel
│       │   │   │   ├── repository/
│       │   │   │   │   └── RsvpRepository.java
│       │   │   │   ├── model/
│       │   │   │   │   ├── Rsvp.java              # @Entity · eventId · userId · status · createdAt
│       │   │   │   │   └── RsvpStatus.java        # enum: GOING · WAITLISTED · CANCELLED · ATTENDED
│       │   │   │   ├── dto/
│       │   │   │   │   └── RsvpDto.java
│       │   │   │   ├── mapper/
│       │   │   │   │   └── RsvpMapper.java
│       │   │   │   └── observer/
│       │   │   │       ├── RsvpCancelledEvent.java        # ★ BEHAVIORAL: Observer Pattern
│       │   │   │       ├── RsvpEventPublisher.java        #   Spring ApplicationEvent payload
│       │   │   │       └── WaitlistPromotionListener.java #   @EventListener → promotes next user
│       │   │   │
│       │   │   ├── moderation/                    # Module 4 — reviews + reports
│       │   │   │   ├── controller/
│       │   │   │   │   ├── ReviewController.java  # POST /api/reviews
│       │   │   │   │   │                          # GET  /api/events/{id}/reviews
│       │   │   │   │   └── ReportController.java  # POST  /api/reports
│       │   │   │   │                              # GET   /api/admin/reports
│       │   │   │   │                              # PATCH /api/admin/reports/{id}/resolve
│       │   │   │   ├── service/
│       │   │   │   │   ├── ReviewService.java     # validates: attended before reviewing · no dupes
│       │   │   │   │   │                          # triggers TrustLevelService recalculation
│       │   │   │   │   └── ReportService.java     # on N reports → auto-flag event UNDER_REVIEW
│       │   │   │   ├── repository/
│       │   │   │   │   ├── ReviewRepository.java
│       │   │   │   │   └── ReportRepository.java
│       │   │   │   ├── model/
│       │   │   │   │   ├── Review.java            # @Entity · rating 1-5 · comment · reviewerId
│       │   │   │   │   ├── Report.java            # @Entity · reason · details · status
│       │   │   │   │   ├── ReportReason.java      # enum: SPAM · INAPPROPRIATE · FAKE_EVENT · OTHER
│       │   │   │   │   └── ReportStatus.java      # enum: OPEN · RESOLVED
│       │   │   │   ├── dto/
│       │   │   │   │   ├── ReviewDto.java
│       │   │   │   │   ├── CreateReviewRequest.java
│       │   │   │   │   ├── ReportDto.java
│       │   │   │   │   └── CreateReportRequest.java
│       │   │   │   └── mapper/
│       │   │   │       ├── ReviewMapper.java
│       │   │   │       └── ReportMapper.java
│       │   │   │
│       │   │   └── mqtt/                          # MQTT bridge — System 1 subscriber side
│       │   │       ├── config/
│       │   │       │   └── MqttConfig.java        # MqttPahoClientFactory · inbound channel adapter
│       │   │       ├── listener/
│       │   │       │   └── OfficialEventListener.java  # @MqttMessageDriven · topic: university/events
│       │   │       ├── adapter/
│       │   │       │   ├── EventMessageTarget.java     # ★ STRUCTURAL: Adapter Pattern (interface)
│       │   │       │   └── OfficialEventAdapter.java   #   activity_name·time·venue → Event entity
│       │   │       └── dto/
│       │   │           └── OfficialEventMessage.java   # AStA JSON shape: activity_name · time · venue
│       │   │
│       │   └── resources/
│       │       ├── application.yml                # base config: DB · MQTT · JWT secret · Flyway
│       │       ├── application-dev.yml            # H2 in-memory · debug logging — no Docker needed
│       │       ├── application-prod.yml           # PostgreSQL · external Mosquitto · no Swagger
│       │       └── db/
│       │           └── migration/
│       │               ├── V1__create_users.sql
│       │               ├── V2__create_events_and_categories.sql
│       │               ├── V3__create_rsvps.sql
│       │               └── V4__create_moderation.sql
│       │
│       └── test/
│           └── java/de/fhdortmund/mystudyapp/
│               ├── identity/
│               │   └── UserServiceTest.java
│               ├── events/
│               │   ├── EventServiceTest.java
│               │   └── EventFactoryTest.java      # unit test: Factory pattern
│               ├── registration/
│               │   ├── RsvpServiceTest.java
│               │   └── WaitlistPromotionTest.java # integration test: Observer pattern
│               ├── moderation/
│               │   └── ReportServiceTest.java
│               └── mqtt/
│                   └── OfficialEventAdapterTest.java  # unit test: Adapter pattern
│
│
├── backend-asta/                                  # System 2 — AStA Event Publisher (port 8081)
│   ├── pom.xml                                    # Spring Boot · MQTT Paho · Jackson
│   └── src/
│       ├── main/
│       │   ├── java/de/fhdortmund/mystudyapp/asta/
│       │   │   ├── AstaApplication.java           # @SpringBootApplication · port 8081
│       │   │   ├── controller/
│       │   │   │   └── AstaController.java        # POST /api/asta/publish-event
│       │   │   │                                  # (demo entry point for Postman presentation)
│       │   │   ├── service/
│       │   │   │   └── AstaPublisherService.java  # serialises payload · publishes to MQTT topic
│       │   │   ├── mqtt/
│       │   │   │   └── MqttPublisherConfig.java   # MqttPahoMessageHandler bean · QoS 1
│       │   │   └── dto/
│       │   │       └── AstaEventRequest.java      # {activity_name, time, venue, organiser}
│       │   └── resources/
│       │       └── application.yml                # port: 8081 · mqtt.broker · topic: university/events
│       └── test/
│           └── java/de/fhdortmund/mystudyapp/asta/
│               ├── AstaControllerTest.java
│               └── AstaPublisherServiceTest.java
│
│
└── frontend/                                      # React 18 PWA (Vite · TanStack Query · Zustand)
    ├── package.json                               # react · react-dom · react-router-dom
    │                                              # @tanstack/react-query · zustand
    │                                              # react-hook-form · zod · axios
    ├── vite.config.js                             # proxy /api → :8080 · PWA plugin · code-split
    ├── index.html
    ├── .env.example                               # VITE_API_URL · VITE_MQTT_URL
    │
    ├── public/
    │   ├── manifest.json                          # PWA: name · icons · theme_color · start_url
    │   ├── service-worker.js                      # stale-while-revalidate offline strategy
    │   └── icons/
    │       ├── icon-192.png
    │       ├── icon-512.png
    │       └── icon-maskable.png                  # Android adaptive icon
    │
    └── src/
        ├── main.jsx                               # ReactDOM · QueryClientProvider · RouterProvider
        ├── App.jsx                                # route tree · layout outlets · ErrorBoundary
        │
        ├── design-system/                         # single source of truth for every visual decision
        │   ├── tokens.css                         # --color-* · --spacing-* · --radius-* · --font-*
        │   │                                      # --breakpoint-sm/md/lg/xl as CSS custom props
        │   ├── animations.css                     # page-fade · skeleton-pulse · slide-in · toast-pop
        │   ├── theme.js                           # exports token map for JS consumers (charts etc.)
        │   └── breakpoints.js                     # sm·md·lg·xl consts — used in useMediaQuery
        │
        ├── components/
        │   │
        │   ├── atoms/                             # smallest primitives — zero business logic
        │   │   ├── Button/
        │   │   │   ├── Button.jsx                 # variant: primary · ghost · danger · loading state
        │   │   │   └── Button.module.css
        │   │   ├── Input/
        │   │   │   ├── Input.jsx                  # label · error msg · helper text · accessible
        │   │   │   └── Input.module.css
        │   │   ├── Badge/
        │   │   │   └── Badge.jsx                  # GOING · WAITLISTED · PUBLISHED · CANCELLED
        │   │   ├── Avatar/
        │   │   │   └── Avatar.jsx                 # initials fallback · size variants (sm/md/lg)
        │   │   ├── Skeleton/
        │   │   │   └── Skeleton.jsx               # animated placeholder — prevents layout shift
        │   │   ├── Spinner/
        │   │   │   └── Spinner.jsx                # size variants · inherits color from parent
        │   │   ├── IconButton/
        │   │   │   └── IconButton.jsx             # always requires aria-label prop
        │   │   └── index.js                       # barrel: import { Button, Badge } from atoms
        │   │
        │   ├── molecules/                         # atoms composed into meaningful UI units
        │   │   ├── EventCard/
        │   │   │   ├── EventCard.jsx              # title · date · location · CapacityBar · RSVP CTA
        │   │   │   └── EventCard.module.css
        │   │   ├── CapacityBar/
        │   │   │   └── CapacityBar.jsx            # fills amber at 80% · red at 100% capacity
        │   │   ├── SearchBar/
        │   │   │   └── SearchBar.jsx              # debounced input · clears on Escape
        │   │   ├── CategoryChip/
        │   │   │   └── CategoryChip.jsx           # toggle filter chip — active/inactive states
        │   │   ├── RatingStars/
        │   │   │   └── RatingStars.jsx            # interactive (submit) + read-only (display) modes
        │   │   ├── FormField/
        │   │   │   └── FormField.jsx              # React Hook Form wrapper: label + Input + error
        │   │   ├── EmptyState/
        │   │   │   └── EmptyState.jsx             # icon · heading · sub-copy · optional CTA button
        │   │   ├── UserTrustBadge/
        │   │   │   └── UserTrustBadge.jsx         # NEW · TRUSTED_HOST · FLAGGED with tooltip
        │   │   ├── Toast/
        │   │   │   └── Toast.jsx                  # success · error · info · auto-dismiss timer
        │   │   └── index.js
        │   │
        │   ├── organisms/                         # full feature blocks composed of molecules
        │   │   ├── Navbar/
        │   │   │   ├── Navbar.jsx                 # responsive · mobile hamburger · auth-aware links
        │   │   │   └── Navbar.module.css
        │   │   ├── EventFeed/
        │   │   │   └── EventFeed.jsx              # infinite scroll · Skeleton on load · EmptyState
        │   │   ├── EventForm/
        │   │   │   └── EventForm.jsx              # React Hook Form · zod schema · category multi-select
        │   │   ├── ReviewSection/
        │   │   │   └── ReviewSection.jsx          # review list + submit form · blocks re-review
        │   │   ├── WaitlistBanner/
        │   │   │   └── WaitlistBanner.jsx         # queue position · estimated wait · cancel CTA
        │   │   ├── ToastContainer/
        │   │   │   └── ToastContainer.jsx         # renders toast stack from uiStore
        │   │   └── AdminReportTable/
        │   │       └── AdminReportTable.jsx       # sortable · filter by reason · bulk resolve
        │   │
        │   └── templates/                         # layout shells with <Outlet> slots
        │       ├── PageLayout/
        │       │   └── PageLayout.jsx             # Navbar + <main> Outlet + Footer + ToastContainer
        │       ├── AuthLayout/
        │       │   └── AuthLayout.jsx             # centered card · no Navbar · redirects if authed
        │       ├── AdminLayout/
        │       │   └── AdminLayout.jsx            # sidebar nav · guards ADMIN role — redirects if not
        │       └── ErrorBoundary/
        │           └── ErrorBoundary.jsx          # catches render errors · shows friendly fallback UI
        │
        ├── pages/                                 # one folder per route — co-locates route logic
        │   ├── Home/
        │   │   └── HomePage.jsx                   # event feed · category filter bar · search
        │   ├── Events/
        │   │   ├── EventsPage.jsx                 # search · filter · sort · paginated grid
        │   │   ├── EventDetailPage.jsx            # RSVP · WaitlistBanner · ReviewSection · report
        │   │   ├── CreateEventPage.jsx            # guarded: STUDENT or TRUSTED_HOST only
        │   │   └── EditEventPage.jsx              # guarded: host owner only
        │   ├── Auth/
        │   │   ├── LoginPage.jsx
        │   │   └── RegisterPage.jsx               # university email (@fh-dortmund.de) validation
        │   ├── Profile/
        │   │   └── ProfilePage.jsx                # hosted events · attended events · trust level
        │   ├── Admin/
        │   │   └── AdminDashboardPage.jsx         # reports queue · under-review events · user flags
        │   └── Error/
        │       ├── NotFoundPage.jsx               # friendly 404 with back-to-feed CTA
        │       └── ErrorPage.jsx                  # router errorElement — unhandled route errors
        │
        ├── hooks/
        │   ├── useAuth.js                         # reads authStore · exposes user · login · logout
        │   ├── useEvents.js                       # TanStack Query wrapper for GET /api/events
        │   ├── useInfiniteEvents.js               # cursor-based infinite scroll for the event feed
        │   ├── useRsvp.js                         # mutation + optimistic UI update on RSVP toggle
        │   ├── useToast.js                        # dispatch to uiStore toast queue
        │   ├── useMediaQuery.js                   # JS-side breakpoint check for conditional renders
        │   └── useDebounce.js                     # debounce search input before firing query
        │
        ├── stores/                                # Zustand — client-only UI state (not server data)
        │   ├── authStore.js                       # JWT token · decoded user · persist to localStorage
        │   └── uiStore.js                         # theme dark/light · toast queue · mobile nav open
        │
        ├── api/                                   # plain async functions — no React Query here
        │   ├── eventsApi.js                       # getEvents · getEvent · createEvent · updateEvent
        │   ├── usersApi.js                        # register · login · getProfile
        │   ├── rsvpApi.js                         # createRsvp · cancelRsvp
        │   └── moderationApi.js                   # createReview · createReport · resolveReport
        │
        ├── lib/
        │   ├── queryClient.js                     # TanStack Query client · staleTime · retry config
        │   ├── axios.js                           # base URL · JWT interceptor · 401 → auto logout
        │   └── i18n.js                            # scaffold for future localisation (de / en)
        │
        ├── constants/
        │   ├── routes.js                          # ROUTES.HOME · ROUTES.EVENT_DETAIL — no magic strings
        │   ├── queryKeys.js                       # TanStack cache keys — enables precise invalidation
        │   └── enums.js                           # EventStatus · RsvpStatus · TrustLevel · ReportReason
        │                                          # mirrors backend Java enums exactly
        │
        ├── utils/
        │   ├── dateFormatter.js                   # formatDate · formatRelative · formatDuration
        │   ├── validators.js                      # isUniversityEmail · isCapacityValid
        │   ├── cn.js                              # classname merger (clsx) — conditional class logic
        │   └── errorMessages.js                   # maps API error codes → user-friendly strings
        │
        └── types/
            └── index.js                           # JSDoc @typedef: Event · User · Rsvp · Review
```

---

## Design Patterns (Praktikum 4)

| Pattern | Category | Location | Purpose |
|---|---|---|---|
| **Factory** | Creational | `events/factory/EventFactory.java` | Builds `Event` from either a `CreateEventRequest` (REST) or `OfficialEventMessage` (MQTT) without polluting the service layer |
| **Adapter** | Structural | `mqtt/adapter/OfficialEventAdapter.java` | Translates AStA JSON (`activity_name`, `time`) into the internal `Event` format — the interface `EventMessageTarget` keeps it swappable |
| **Observer** | Behavioral | `registration/observer/` | When an RSVP is cancelled, `RsvpEventPublisher` fires a Spring `ApplicationEvent`; `WaitlistPromotionListener` reacts automatically to promote the next queued user |

---

## Key Tech Decisions

| Decision | Reason |
|---|---|
| **Flyway migrations** | Versioned schema history; each SQL file maps to a module slice |
| **MapStruct mappers** | Compile-time DTO↔Entity mapping — no runtime reflection, no manual `get/set` chains |
| **`application-dev.yml` with H2** | Any team member runs the backend without Docker |
| **TanStack Query** | Server-state cache with stale-while-revalidate — eliminates redundant API calls as the user navigates |
| **Zustand** | Minimal client-state (auth token, theme, toasts) without Redux boilerplate |
| **React Hook Form + Zod** | Forms that don't re-render on every keystroke; schema validation shared across frontend |
| **Atomic Design** | Atoms → Molecules → Organisms → Templates; one change to `Button.jsx` propagates everywhere |
| **`constants/enums.js`** | Frontend enums mirror backend Java enums exactly — no drift between layers |
| **`constants/queryKeys.js`** | Predictable TanStack cache keys; cancelling an RSVP invalidates exactly the right queries |
| **`EmptyState` + `Skeleton` atoms** | Every loading and zero-data screen has a first-class component — no blank white boxes |
| **`GlobalExceptionHandler`** | Every endpoint returns the same `ApiResponse` shape; `errorMessages.js` maps codes to readable strings |
| **`docker-compose.yml` at root** | One command spins up Mosquitto + PostgreSQL for the full integration stack |
