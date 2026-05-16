# Campus Event App — Product Specification

## 1. App Identity & Purpose

A **university-exclusive event platform** where verified students can discover, host, and attend campus events. The app solves three core campus problems:

- **Admin Bottleneck**: Manual approval for every event overwhelms admins. Solved via a **Trust Level** system.
- **Overcrowding & Ghosting**: Events get too full, or people RSVP and don't show. Solved via **capacity limits + waitlists + attendance tracking**.
- **Safety & Moderation**: Students need a way to report suspicious events and review hosts. Solved via a **post-event review + pre-event reporting** system.

---

## 2. Core Feature Modules

### 2.1 Identity & Profile (Authentication & Trust)

| Feature | Description |
|---------|-------------|
| **Campus Verification** | Sign-up requires a university email (`university_email`). This gates the platform to actual students. |
| **Roles** | `STUDENT` (default) and `ADMIN` (moderation privileges). |
| **Trust Levels** | `NEW` → `TRUSTED_HOST` → `FLAGGED`. After hosting **3 well-reviewed events**, a student auto-promotes to `TRUSTED_HOST`. |
| **Profile** | Display name, bio, event history, and trust badge visible to other users. |

**Why this matters**: Trusted hosts bypass manual admin review, allowing the platform to scale without creating an approval backlog.

---

### 2.2 Event Catalog (Discovery & Creation)

| Feature | Description |
|---------|-------------|
| **Event Creation** | Hosts set title, description, location, start/end time, and `max_capacity`. |
| **Auto-Publish vs. Review** | `TRUSTED_HOST` events go straight to `PUBLISHED`. `NEW` host events enter `UNDER_REVIEW` for admin approval. |
| **Categories** | Events tagged via `event_categories` join table (e.g., "Study Group", "Nightlife", "Sports"). |
| **RSVP Count Caching** | `current_rsvp_count` is denormalized on the `events` table so the feed loads fast without counting the RSVPs table on every scroll. |
| **Event Status Lifecycle** | `PUBLISHED` → `CANCELLED` (by host) or `UNDER_REVIEW` (if flagged). |

---

### 2.3 Registration & Waitlist (Attendance Management)

| Feature | Description |
|---------|-------------|
| **RSVP Status** | `GOING`, `WAITLISTED`, `CANCELLED`, `ATTENDED`. |
| **Automatic Waitlist Promotion** | If someone cancels and a spot opens, the next `WAITLISTED` user is automatically moved to `GOING`. |
| **Attendance Tracking** | Hosts (or the system) mark users as `ATTENDED` post-event. This feeds into the trust algorithm. |
| **Capacity Enforcement** | No event exceeds `max_capacity`. |

**Ghosting solution**: Attendance tracking + waitlists ensure only committed attendees take spots.

---

### 2.4 Moderation & Feedback (Safety Loop)

| Feature | Description |
|---------|-------------|
| **Post-Event Reviews** | Attendees leave a 1-5 `rating` + `comment`. These reviews determine host trust promotion. |
| **Event Reporting** | Any user can report an event for `SPAM`, `INAPPROPRIATE`, `FAKE_EVENT`, or `OTHER`. |
| **Admin Report Queue** | Reports have `OPEN` / `RESOLVED` status. Admins filter by reason type to prioritize. |
| **Real-Time Alerts** | MQTT broker (`mosquitto.conf`) pushes real-time notifications to admins when critical reports are filed. |

---

## 3. Architecture & Folder Structure

Your repo follows a **modular polyrepo-in-one** structure with clear separation of concerns:

```
mystudyapp/
├── backend-main/          # Core business logic (Spring Boot)
├── backend-asta/          # Secondary service (likely real-time/notification or ASTA-specific logic)
├── frontend/              # React + Vite SPA (PWA-ready)
├── docs/                  # API specs, architecture decisions, design patterns
├── docker-compose.yml     # Orchestrates backend services + MQTT + DB
└── mosquitto.conf         # MQTT broker config for real-time admin alerts
```

### 3.1 Backend — `backend-main`

The primary Spring Boot service handling:
- REST API for events, users, RSVPs, reviews, reports
- Database migrations (see `resources/db/`)
- Profile-specific configs (`application-dev.yml`, `application-prod.yml`)

### 3.2 Backend — `backend-asta`

Likely a **satellite microservice** (possibly handling asynchronous tasks, real-time messaging, or an external integration). The name "asta" might refer to a specific domain (e.g., "Async Service for Task Automation") or a university-specific module.

### 3.3 Frontend — `frontend/`

A modern React SPA built with Vite:

| Directory | Purpose |
|-----------|---------|
| `src/api/` | API clients per domain (`eventsApi.js`, `rsvpApi.js`, etc.) |
| `src/components/` | **Atomic Design** structure: atoms → molecules → organisms → templates |
| `src/hooks/` | Custom React Query hooks (`useEvents`, `useInfiniteEvents`, `useRsvp`, `useAuth`) |
| `src/stores/` | Global state (`authStore`, `uiStore`) |
| `src/pages/` | Route-level pages: `Home`, `Events`, `Auth`, `Profile`, `Admin`, `Error` |
| `src/design-system/` | Theme tokens, breakpoints, animations, CSS variables |
| `src/lib/` | Axios instance, i18n config, React Query client |
| `public/` | PWA assets: `manifest.json`, service worker, icons |

**PWA Features**: Offline capability, installable on mobile, push notifications via service worker.

---

## 4. User Flows

### 4.1 Student Journey

1. **Sign Up** → Verify university email → Profile created (`trust_level: NEW`)
2. **Browse Feed** → Infinite scroll of `PUBLISHED` events (`useInfiniteEvents`)
3. **Filter** → By category, date, or location
4. **RSVP** → If capacity full, auto-join `WAITLIST`
5. **Attend Event** → Host marks attendance → Student becomes eligible to review
6. **Review Host** → 1-5 rating + comment → Host's trust score updates
7. **Host Event** (after 3 good reviews) → Auto-publishes without admin wait

### 4.2 Admin Journey

1. **Dashboard** → View `UNDER_REVIEW` events and `OPEN` reports
2. **Moderate** → Approve/reject events, resolve reports
3. **Real-Time Alerts** → MQTT pushes new report notifications instantly
4. **User Management** → Flag problematic users (`trust_level: FLAGGED`)

---

## 5. Key Technical Decisions

| Decision | Rationale |
|----------|-----------|
| **UUID Primary Keys** | Prevents enumeration attacks; fits distributed microservices. |
| **Denormalized `current_rsvp_count`** | Read optimization for high-traffic event feeds. |
| **No `PENDING` status** | Simplified state machine: `UNDER_REVIEW` for new hosts, `PUBLISHED` for trusted ones. |
| **Enum-based Reports** | Faster admin filtering than free-text search. |
| **MQTT (Mosquitto)** | Lightweight pub/sub for real-time moderation alerts without heavy WebSocket overhead. |
| **Atomic Design Frontend** | Scalable component architecture as features grow. |

---

## 6. What You Should Build Next

Based on this spec, your development priorities should be:

1. **Auth & Verification** — University email validation + JWT sessions
2. **Event CRUD + Feed** — Create event, category filtering, infinite scroll
3. **RSVP + Waitlist Engine** — Transaction-safe RSVP with automatic waitlist promotion
4. **Trust Algorithm** — Background job that checks if a host has 3+ positive reviews and auto-promotes them
5. **Admin Moderation UI** — Report queue, event approval workflow
6. **PWA Polish** — Service worker caching, offline read-only event browsing
7. **Real-Time Layer** — Wire up MQTT for admin notifications on new reports

This architecture gives you a **scalable, trust-based campus platform** that reduces admin overhead while keeping events safe and well-attended.
