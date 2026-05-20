# MyStudyApp - Complete Frontend Development Guide
## Based 100% on Backend API Specification

> **Version**: 1.0 | **Date**: 2026-05-20
> **Backend**: Spring Boot (Java) | **Auth**: JWT Bearer Token
> **Base URL**: `http://localhost:8080` (adjust for deployment)

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Authentication & Security](#2-authentication--security)
3. [API Response Format](#3-api-response-format)
4. [Error Handling](#4-error-handling)
5. [Rate Limiting](#5-rate-limiting)
6. [File Upload Rules](#6-file-upload-rules)
7. [Real-Time Features (SSE)](#7-real-time-features-sse)
8. [Module 1: Authentication](#module-1-authentication)
9. [Module 2: Events](#module-2-events)
10. [Module 3: RSVPs & Waitlist](#module-3-rsvps--waitlist)
11. [Module 4: Reviews](#module-4-reviews)
12. [Module 5: Reports (Moderation)](#module-5-reports-moderation)
13. [Module 6: Notifications](#module-6-notifications)
14. [Module 7: Search](#module-7-search)
15. [Module 8: User Profiles](#module-8-user-profiles)
16. [Module 9: Admin Dashboard](#module-9-admin-dashboard)
17. [Module 10: Public Pages](#module-10-public-pages)
18. [UI/UX Guidelines](#18-uiux-guidelines)
19. [TypeScript Types](#19-typescript-types)
20. [Frontend State Management](#20-frontend-state-management)

---

## 1. Architecture Overview

### Backend Structure
```
de.fhdortmund.mystudyapp/
├── common/          # Config, Security, Exceptions, Response wrappers
├── events/          # Event CRUD, Media, Categories, Search, SSE
├── identity/        # Auth, Users, Profiles, Trust Levels, Preferences
├── moderation/      # Reviews, Reports, Helpful Votes
├── mqtt/            # MQTT integration for official AStA events
├── notification/    # In-app notifications
└── registration/    # RSVPs, Waitlist, Check-in
```

### Communication Pattern
- **Standard REST**: JSON over HTTP/HTTPS
- **Authentication**: `Authorization: Bearer <accessToken>` header
- **Real-time**: Server-Sent Events (SSE) for live RSVP/Waitlist updates
- **File Upload**: `multipart/form-data` for avatars and event media
- **Pagination**: Spring Data `Pageable` (page, size, sort parameters)

### CORS Configuration
```javascript
// Frontend must handle credentials
axios.defaults.withCredentials = true;
// Allowed origins: configurable via app.frontend-url (default: http://localhost:5173)
// Exposed headers: Authorization, X-Refresh-Token
```

---

## 2. Authentication & Security

### JWT Token System
| Token Type | Duration | Storage | Usage |
|------------|----------|---------|-------|
| **Access Token** | 15 minutes (900s) | Memory (Redux/Zustand) | API calls via `Authorization: Bearer` |
| **Refresh Token** | 7 days (604800s) | httpOnly Cookie / Secure Storage | Token refresh at `/api/auth/refresh` |

### Token Refresh Flow
```typescript
// When 401 received, try refresh
const refreshToken = async () => {
  const response = await fetch('/api/auth/refresh', {
    headers: { 'X-Refresh-Token': refreshTokenValue }
  });
  // New access + refresh tokens returned
  // Old refresh token is blacklisted
};
```

### Auth Endpoints Summary
| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/auth/register` | POST | No | Create account (email verification required) |
| `/api/auth/login` | POST | No | Returns tokens + user data |
| `/api/auth/refresh` | POST | No (uses X-Refresh-Token header) | Get new token pair |
| `/api/auth/logout` | POST | Yes | Blacklists token, clears context |
| `/api/auth/verify?token=` | GET | No | Email verification link |
| `/api/auth/resend-verification` | POST | No | Resend verification email |
| `/api/auth/forgot-password` | POST | No | Request password reset |
| `/api/auth/reset-password` | POST | No | Reset with token |

### Protected Endpoints
- `@PreAuthorize("isAuthenticated()")` → Requires valid access token
- `@PreAuthorize("hasRole('ADMIN')")` → Requires ADMIN role
- `permitAll` → No auth required (public events, categories, search)

### Account States
| TrustLevel | Effect |
|------------|--------|
| `NEW` | Events go to UNDER_REVIEW before publish |
| `TRUSTED_HOST` | Events auto-publish (skip review) |
| `FLAGGED` | **Account locked** - cannot login, tokens rejected |

---

## 3. API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-05-20T15:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "data": { "title": "Title is required" },  // Only for validation errors
  "timestamp": "2026-05-20T15:30:00Z"
}
```

### Paginated Response
```json
{
  "success": true,
  "message": "Events retrieved",
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  }
}
```

### Pagination Parameters
```typescript
// Spring Pageable defaults
interface PaginationParams {
  page?: number;      // Default: 0
  size?: number;      // Default: 20, Max: 100 (enforced by PageableConfig)
  sort?: string;      // e.g., "startTime,desc" or "createdAt"
}
```

---

## 4. Error Handling

### HTTP Status Codes Used
| Status | Trigger |
|--------|---------|
| **400** | Validation errors, malformed JSON, illegal arguments |
| **401** | Bad credentials, expired/invalid token |
| **403** | Access denied, forbidden action, account disabled (not verified), account flagged |
| **404** | Resource not found, no resource found |
| **409** | Capacity exceeded, data integrity violation (duplicate) |
| **413** | File too large (handled as 400 with message) |
| **429** | Rate limit exceeded |
| **500** | Unexpected server errors |

### Specific Error Messages (from GlobalExceptionHandler)
- `MethodArgumentNotValidException` → 400 with field-level errors map
- `BadCredentialsException` → 401
- `AccessDeniedException` → 403 "Access denied"
- `LockedException` → 403 (account locked/flagged)
- `DisabledException` → 403 "Account not verified. Please check your email."
- `ForbiddenActionException` → 403 with specific action + reason
- `ResourceNotFoundException` → 404
- `CapacityExceededException` → 409
- `DataIntegrityViolationException` → 409 "Resource already exists"
- `MaxUploadSizeExceededException` → 400 "File size exceeds..."

---

## 5. Rate Limiting

### Limits (per client IP + endpoint)
| Endpoint Type | Max Requests | Window |
|---------------|--------------|--------|
| `/api/auth/**` | 5 | 1 minute |
| Write endpoints (`/api/reviews`, `/api/reports`, `/api/rsvps`, `/api/events`) | 20 | 1 minute |
| GET/HEAD/OPTIONS | **Unlimited** | - |

### Rate Limit Response (429)
```json
{
  "success": false,
  "message": "Too many requests. Please try again later.",
  "timestamp": "2026-05-20T15:30:00Z"
}
```

---

## 6. File Upload Rules

### Avatar Upload
- **Endpoint**: `PUT /api/auth/me` (multipart/form-data)
- **Field name**: `profileImage`
- **Allowed types**: JPG, PNG, WEBP
- **Max size**: 5 MB
- **Storage**: `/uploads/avatars/avatar_{userId}.{ext}`

### Event Media Upload
- **Endpoint**: `POST /api/events/{eventId}/media` (multipart/form-data)
- **Field names**: `images[]`, `videos[]`
- **Image limits**: Max 5 per event, 5 MB each
- **Video limits**: Max 2 per event, 20 MB each
- **Image types**: JPG, JPEG, PNG, WEBP
- **Video types**: MP4, WEBM, MOV, QUICKTIME
- **Auto-generated**: Thumbnails (400x300) and Medium (800x600) for images

### File URLs
```typescript
// Avatar URL pattern
const avatarUrl = `${API_BASE}/uploads/avatars/avatar_${userId}.png`;

// Event media URL pattern
const imageUrl = `${API_BASE}/uploads/events/{eventId}/images/{uuid}.jpg`;
const thumbUrl = `${API_BASE}/uploads/events/{eventId}/images/{uuid}_thumb.jpg`;
const mediumUrl = `${API_BASE}/uploads/events/{eventId}/images/{uuid}_medium.jpg`;
const videoUrl = `${API_BASE}/uploads/events/{eventId}/videos/{uuid}.mp4`;
```

---

## 7. Real-Time Features (SSE)

### Event Stream Endpoint
```typescript
// Connect to SSE
const eventSource = new EventSource(
  `${API_BASE}/api/events/stream/{eventId}`,
  { withCredentials: true }  // Important for auth
);
```

### Event Types
| Event Name | Payload | When Fired |
|------------|---------|------------|
| `connected` | `{ eventId, status: "subscribed" }` | Initial connection |
| `rsvp-update` | `{ eventId, currentCount, maxCapacity, spotsRemaining, isFull }` | RSVP created/cancelled |
| `waitlist-update` | `{ eventId, waitlistCount, type }` | Waitlist promotion |
| `event-cancelled` | `{ eventId }` | Event cancelled by host |

### Connection Details
- **Timeout**: 5 minutes (300,000ms)
- **Reconnection**: Client must handle reconnection
- **Cleanup**: Server auto-removes dead connections

---

## Module 1: Authentication

### 1.1 Registration Flow

**Endpoint**: `POST /api/auth/register`

**Request Body** (`RegisterRequest`):
```typescript
interface RegisterRequest {
  universityEmail: string;    // Must match regex: blocks free providers, requires .de or .edu
  password: string;           // Min 8 chars, uppercase, lowercase, number, special char
  displayName: string;        // 2-50 chars
}
```

**Email Validation Regex** (from backend):
```
^(?!.*@(gmx|web|gmail|yahoo|hotmail|outlook|icloud|posteo|mailbox)\.(de|com|net|org)$)[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.(de|edu)$
```

**UI Requirements**:
- Show email format validation in real-time
- Password strength indicator (must meet all 4 criteria)
- Display name length counter (2-50)
- After registration: Show "Check your email" screen (account is unverified)

**Response**: `AuthResponse` (tokens empty since unverified)

---

### 1.2 Login Flow

**Endpoint**: `POST /api/auth/login`

**Request Body** (`LoginRequest`):
```typescript
interface LoginRequest {
  universityEmail: string;
  password: string;
}
```

**Response**: `AuthResponse`
```typescript
interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresIn: 900;  // seconds
  user: UserDto;
}
```

**Error States**:
- 401 "Invalid email or password" → Bad credentials
- 403 "Account not verified. Please check your email." → DisabledException
- 403 "Account has been flagged. Contact support." → LockedException (FLAGGED trust level)

---

### 1.3 Email Verification

**Endpoint**: `GET /api/auth/verify?token={token}`

**UI Flow**:
1. User clicks link from email → Frontend route `/verify-email?token=xxx`
2. Frontend calls backend with token
3. On success: Redirect to login with "Account verified" toast
4. On error (expired): Show "Link expired, register again" message

---

### 1.4 Password Reset Flow

**Step 1 - Request Reset**:
- **Endpoint**: `POST /api/auth/forgot-password`
- **Body**: `ForgotPasswordRequest` `{ universityEmail: string }`
- **UI**: Always show "If that email is registered, a reset link has been sent." (security through obscurity)

**Step 2 - Reset Password**:
- **Endpoint**: `POST /api/auth/reset-password`
- **Body**: `ResetPasswordRequest`
```typescript
interface ResetPasswordRequest {
  token: string;        // From URL query param
  newPassword: string;  // Same rules as registration
  confirmPassword: string;
}
```

---

### 1.5 Token Refresh

**Endpoint**: `POST /api/auth/refresh`

**Headers**:
```
X-Refresh-Token: {refreshToken}
```

**Response**: New `AuthResponse` with fresh tokens

**Important**: Old refresh token is blacklisted. Store new refresh token immediately.

---

### 1.6 Logout

**Endpoint**: `POST /api/auth/logout`

**Headers**:
```
Authorization: Bearer {accessToken}
```

**UI**: Clear all token storage, redirect to login

---

### 1.7 Current User Profile

**Endpoint**: `GET /api/auth/me`

**Response**: `UserDto`
```typescript
interface UserDto {
  id: string;           // UUID
  universityEmail: string;
  displayName: string;
  bio: string | null;
  profileImageUrl: string | null;
  role: "STUDENT" | "ADMIN";
  trustLevel: "NEW" | "TRUSTED_HOST" | "FLAGGED";
  createdAt: string;    // ISO 8601
}
```

---

### 1.8 Update Profile

**Endpoint**: `PUT /api/auth/me`

**Content-Type**: `multipart/form-data`

**Fields**:
```typescript
interface UpdateProfileRequest {
  displayName?: string;      // 2-50 chars
  bio?: string;              // Max 500 chars
  profileImage?: File;       // Optional avatar upload
}
```

**UI Notes**:
- Show current avatar preview
- Bio character counter (0/500)
- Display name validation (2-50)

---

### 1.9 Change Password

**Endpoint**: `PUT /api/auth/me/password`

**Body**: `ChangePasswordRequest`
```typescript
interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;      // Same complexity rules
  confirmPassword: string;
}
```

---

### 1.10 Delete Account

**Endpoint**: `DELETE /api/auth/me`

**Headers**: `Authorization: Bearer {token}`

**UI Requirements**:
- Confirmation modal with "This cannot be undone"
- Warn about deletion of all hosted events, RSVPs, reviews
- Requires re-authentication (token in header)

---

### 1.11 Trust Qualification Status

**Endpoint**: `GET /api/auth/me/trust-status`

**Response**: `TrustQualificationStatus`
```typescript
interface TrustQualificationStatus {
  completedEventsWithReviews: number;
  minimumEventsRequired: number;      // 3
  averageRating: number;
  minimumRatingRequired: number;     // 4.0
  meetsEventCount: boolean;
  meetsRatingThreshold: boolean;
  qualifies: boolean;
}
```

**UI**: Show progress bars for:
- Events hosted: X/3
- Average rating: X/5.0 (must be ≥ 4.0)
- Overall qualification status

---

### 1.12 User Preferences

**Endpoints**:
- `GET /api/auth/me/preferences` → `UserPreferencesDto`
- `PUT /api/auth/me/preferences` → `UserPreferencesDto`

```typescript
interface UserPreferencesDto {
  emailNotifications: boolean;    // Default: true
  pushNotifications: boolean;   // Default: true
  notifyOnRsvpChange: boolean;    // Default: true
  notifyOnReview: boolean;        // Default: true
  timezone: string;               // Default: "Europe/Berlin"
  language: string;               // Default: "de"
}
```

**UI**: Settings page with toggles for each notification type, timezone selector, language selector

---

## Module 2: Events

### 2.1 Event Status Lifecycle
```
DRAFT → UNDER_REVIEW → PUBLISHED → COMPLETED
         ↓              ↓
       CANCELLED     (auto-completed after endTime)
```

| Status | Visibility | Actions |
|--------|------------|---------|
| `DRAFT` | Host only | Edit, Publish, Delete |
| `UNDER_REVIEW` | Host + Admin | Admin can Approve/Reject/Flag |
| `PUBLISHED` | Public | RSVP, View, Report |
| `CANCELLED` | Public (with reason) | None (frozen) |
| `COMPLETED` | Public | Reviews allowed |

---

### 2.2 Create Event

**Endpoint**: `POST /api/events`

**Body**: `CreateEventRequest`
```typescript
interface CreateEventRequest {
  title: string;              // Required, 3-100 chars
  description?: string;       // Max 2000 chars
  location: string;           // Required, max 200 chars
  startTime: string;          // Required, ISO 8601 Instant
  endTime: string;            // Required, must be after startTime
  maxCapacity: number;        // Required, min 1
  categoryIds?: number[];     // Set of category IDs
  slug?: string;              // Optional, max 150 chars (auto-generated from title if empty)
}
```

**Validation Rules**:
- `endTime` must be after `startTime`
- `startTime` must be in the future
- If host is `TRUSTED_HOST` or `ADMIN` → status = `PUBLISHED`
- If host is `NEW` → status = `UNDER_REVIEW`

**UI Requirements**:
- Date/time picker with timezone awareness (Europe/Berlin default)
- Category multi-select (fetch from `/api/public/categories`)
- Capacity input with min 1
- Slug auto-generation preview (editable)
- Real-time validation for date logic

---

### 2.3 Create Draft

**Endpoint**: `POST /api/events/draft`

**Same body as CreateEvent** but:
- No date validation (past dates allowed)
- Partial data accepted (location defaults to "TBD", capacity defaults to 10)
- Status = `DRAFT`
- No trust level checks

**UI**: "Save as Draft" button on event creation form

---

### 2.4 Publish Draft

**Endpoint**: `PUT /api/events/{eventId}/publish`

**Validation** (applied at publish time):
- `endTime` must be after `startTime`
- `startTime` must be in the future
- Trust level check for auto-publish vs review

**UI**: "Publish" button on draft event detail page

---

### 2.5 Get Event Details

**Endpoints**:
- `GET /api/events/{eventId}` → Authenticated
- `GET /api/public/events/{eventId}` → Public (no auth)
- `GET /api/events/by-slug/{slug}` → Authenticated
- `GET /api/public/events/slug/{slug}` → Public

**Response**: `EventDto`
```typescript
interface EventDto {
  id: string;                    // UUID
  host: HostDto;
  title: string;
  description: string | null;
  location: string;
  startTime: string;             // ISO 8601
  endTime: string;
  maxCapacity: number;
  currentRsvpCount: number;
  status: EventStatus;
  categories: CategoryDto[];
  media: EventMediaDto[];
  createdAt: string;
  isHost: boolean;               // true if current user is host
  myRsvpStatus: RsvpStatus | null;  // null if not registered
  slug: string;
  viewCount: number;
  cancellationReason: string | null;
}
```

**HostDto**:
```typescript
interface HostDto {
  id: string;
  displayName: string;
  profileImageUrl: string | null;
  trustLevel: TrustLevel;
  averageHostRating: number;      // 0.0 if none
  totalHostReviews: number;
  completedEventsWithReviews: number;
}
```

**EventMediaDto**:
```typescript
interface EventMediaDto {
  id: string;
  url: string;
  mediaType: "IMAGE" | "VIDEO";
  filename: string;
  thumbnailUrl: string | null;   // 400x300
  mediumUrl: string | null;      // 800x600
  displayOrder: number;           // 0 = first/cover
}
```

**UI Notes**:
- Show host trust level badge (NEW, TRUSTED_HOST)
- Display host rating stars (averageHostRating)
- Media carousel with thumbnail/medium/large variants
- View count display (social proof)
- Slug-based sharing URL

---

### 2.6 List Published Events (Feed)

**Endpoint**: `GET /api/events`

**Query Parameters**:
```typescript
interface EventFeedParams {
  categoryId?: number;
  dateFrom?: string;     // ISO 8601 Instant
  dateTo?: string;
  location?: string;     // Partial match
  q?: string;            // Search in title + description
  page?: number;
  size?: number;
  sort?: string;         // Default: "startTime"
}
```

**Response**: `PageResponse<EventDto>`

**UI**: Filter bar with:
- Category pills (from `/api/public/categories`)
- Date range picker
- Location search input
- Free-text search
- Sort dropdown (startTime, viewCount)

---

### 2.7 My Events

**Endpoint**: `GET /api/events/my-events`

**Query Parameters**:
```typescript
interface MyEventsParams {
  includeDeleted?: boolean;  // Default: false
  page?: number;
  size?: number;
}
```

**Response**: `PageResponse<EventDto>`

**UI**: Tabs for:
- Active events (not deleted)
- Trash bin (deleted events, can restore or permanently delete)

---

### 2.8 Update Event

**Endpoint**: `PUT /api/events/{eventId}`

**Body**: `CreateEventRequest` (same as create)

**Restrictions**:
- Only host can update
- Cannot update `CANCELLED` events
- Cannot update deleted events (restore first)
- Cannot reduce capacity below current RSVP count

---

### 2.9 Cancel Event

**Endpoint**: `PATCH /api/events/{eventId}/cancel`

**Body**: `CancelEventRequest` (optional)
```typescript
interface CancelEventRequest {
  reason?: string;  // Max 500 chars
}
```

**Effects**:
- Status → `CANCELLED`
- All GOING/WAITLISTED attendees get notification
- SSE `event-cancelled` broadcast

**UI**: Confirmation modal with optional reason textarea

---

### 2.10 Soft Delete Event

**Endpoint**: `DELETE /api/events/{eventId}`

**Effects**:
- Sets `deletedAt` timestamp
- Event moves to trash bin
- No data loss (can restore)

**UI**: "Move to Trash" with confirmation

---

### 2.11 Restore Event

**Endpoint**: `PATCH /api/events/{eventId}/restore`

**Effects**:
- Clears `deletedAt`
- If past endTime → status becomes `COMPLETED`

**UI**: "Restore" button in trash bin

---

### 2.12 Permanent Delete

**Endpoint**: `DELETE /api/events/{eventId}/permanent`

**Restrictions**:
- Only host or admin
- Must be soft-deleted first (unless admin)
- Deletes all media files from disk
- Deletes all RSVPs, reviews, reports

**UI**: "Delete Forever" with strong confirmation (type event name)

---

### 2.13 Media Management

**Upload Media**:
- **Endpoint**: `POST /api/events/{eventId}/media`
- **Content-Type**: `multipart/form-data`
- **Fields**: `images[]` (File[]), `videos[]` (File[])

**Delete Media**:
- **Endpoint**: `DELETE /api/events/{eventId}/media/{mediaId}`

**Reorder Media**:
- **Endpoint**: `PATCH /api/events/{eventId}/media/reorder`
- **Body**: `string[]` (ordered array of media IDs)

**UI Requirements**:
- Drag-and-drop media reordering
- Image preview with thumbnail/medium/full variants
- Video player with native controls
- Upload progress indicator
- File type and size validation before upload

---

### 2.14 Check-In Code (Host)

**Endpoint**: `GET /api/events/{eventId}/check-in-code`

**Response**: `CheckInCodeDto`
```typescript
interface CheckInCodeDto {
  checkInCode: string;        // 6-char alphanumeric
  eventId: string;
  eventTitle: string;
  generatedAt: string;
  refreshIntervalSeconds: number;  // 300 (5 minutes)
}
```

**UI**: 
- Display QR code with the check-in code
- Auto-refresh every 5 minutes
- Show "Regenerate" button

---

### 2.15 Public Event Feed

**Endpoint**: `GET /api/public/events`

**Same params as authenticated feed**

**Differences**:
- No `myRsvpStatus` (always null)
- `isHost` always false
- Only `PUBLISHED` events

**Endpoint**: `GET /api/public/events/featured`

**Returns**: Next 6 upcoming `PUBLISHED` events

**UI**: Landing page hero section with featured events

---

## Module 3: RSVPs & Waitlist

### 3.1 RSVP Status Values
```typescript
type RsvpStatus = "GOING" | "WAITLISTED" | "CANCELLED" | "ATTENDED";
```

### 3.2 Create RSVP

**Endpoint**: `POST /api/events/{eventId}/rsvps`

**Logic**:
- If capacity available → `GOING`
- If full → `WAITLISTED`
- If previously cancelled → Reactivate (GOING or WAITLISTED)

**Restrictions**:
- Event must be `PUBLISHED`
- Event must not have ended
- Event must not be deleted
- One RSVP per user per event

**UI**: "Register" button with capacity indicator
- Show "X spots remaining" or "Waitlist"
- Disable if event ended or cancelled

---

### 3.3 Cancel RSVP

**Endpoint**: `PATCH /api/rsvps/{rsvpId}/cancel`

**Body**: `CancelRsvpRequest` (optional)
```typescript
interface CancelRsvpRequest {
  reason?: string;  // Max 500 chars
}
```

**Effects**:
- Status → `CANCELLED`
- If was `GOING` → decrement event count, trigger waitlist promotion
- Host gets notification

**UI**: "Cancel Registration" with optional reason

---

### 3.4 Get My RSVPs

**Endpoint**: `GET /api/rsvps/me`

**Response**: `PageResponse<RsvpDto>`

```typescript
interface RsvpDto {
  id: string;
  eventId: string;
  eventTitle: string;
  user: UserDto;
  status: RsvpStatus;
  createdAt: string;
}
```

**UI**: "My Registrations" page with tabs:
- Upcoming (GOING + future events)
- Waitlisted
- Past/Attended
- Cancelled

---

### 3.5 Get Waitlist Position

**Endpoint**: `GET /api/rsvps/{rsvpId}/position`

**Response**: `number` (position, 0 if not waitlisted)

**UI**: Show "You are #3 on the waitlist" badge

---

### 3.6 Host: View Event RSVPs

**Endpoints**:
- `GET /api/events/{eventId}/rsvps` → All RSVPs
- `GET /api/events/{eventId}/rsvps/status/{status}` → Filtered by status

**Response**: `PageResponse<RsvpDto>`

**UI**: Host dashboard with:
- Attendee list with status filters
- Export option (future)
- Check-in management

---

### 3.7 Host: Mark Attended

**Endpoint**: `PATCH /api/events/{eventId}/rsvps/{rsvpId}/attended`

**Restrictions**: Only host can mark attendance

**UI**: Check-in list with "Mark Attended" buttons

---

### 3.8 Self Check-In (QR Code)

**Endpoint**: `POST /api/events/{eventId}/check-in`

**Body**: `CheckInRequest`
```typescript
interface CheckInRequest {
  code: string;  // 6-10 chars, from QR scan
}
```

**Requirements**:
- Must have RSVP with status `GOING`
- Code must match event's current check-in code

**UI**: QR scanner page for attendees

---

### 3.9 Host: Promote Waitlisted User

**Endpoint**: `PATCH /api/events/{eventId}/rsvps/{rsvpId}/promote`

**Effects**:
- RSVP status → `GOING`
- User gets `WAITLIST_PROMOTED` notification
- SSE broadcast

**UI**: Waitlist management panel with "Promote" buttons

---

## Module 4: Reviews

### 4.1 Create Review

**Endpoint**: `POST /api/reviews`

**Body**: `CreateReviewRequest`
```typescript
interface CreateReviewRequest {
  eventId: string;     // UUID
  rating: number;      // 1-5, required
  comment?: string;    // Max 1000 chars
}
```

**Restrictions**:
- Event must have ended (`endTime` < now)
- Must have RSVP with status `ATTENDED`
- One review per user per event

**Effects**:
- Host gets `NEW_REVIEW` notification
- Auto-trust promotion check (if qualifies)

**UI**: Review form shown only after event ends and user attended
- Star rating input (1-5)
- Comment textarea with 1000 char limit

---

### 4.2 View Event Reviews

**Endpoint**: `GET /api/reviews/event/{eventId}`

**Query**: `Pageable` (default sort: `helpfulCount,desc` then `createdAt,desc`)

**Response**: `PageResponse<ReviewDto>`

```typescript
interface ReviewDto {
  id: string;
  eventId: string;
  reviewer: UserDto;
  rating: number;
  comment: string;
  createdAt: string;
  helpfulCount: number;
  isHelpfulByCurrentUser: boolean | null;
}
```

**UI**: Review list with:
- Sort by "Most Helpful" or "Newest"
- Helpful vote button (toggle)
- Report button for inappropriate reviews

---

### 4.3 View Host Reviews

**Endpoint**: `GET /api/reviews/host/{hostId}`

**Response**: `PageResponse<ReviewDto>`

**UI**: Host profile page with review summary

---

### 4.4 Toggle Helpful Vote

**Endpoint**: `POST /api/reviews/{reviewId}/helpful`

**Response**: Updated `ReviewDto`

**UI**: "Helpful" button with count, toggles on/off

---

### 4.5 Report Review

**Endpoint**: `POST /api/reviews/{reviewId}/report`

**Body**: `ReviewReportRequest`
```typescript
interface ReviewReportRequest {
  reason: string;  // Required, max 500 chars
}
```

**UI**: "Report" button → Modal with reason input

---

### 4.6 Delete Review

**Endpoint**: `DELETE /api/reviews/{reviewId}`

**Restrictions**: Reviewer or admin only

---

## Module 5: Reports (Moderation)

### 5.1 Report Event

**Endpoint**: `POST /api/reports`

**Body**: `CreateReportRequest`
```typescript
interface CreateReportRequest {
  eventId: string;        // UUID
  reason: ReportReason;   // "SPAM" | "INAPPROPRIATE" | "FAKE_EVENT" | "OTHER"
  details?: string;       // Max 2000 chars
}
```

**Restrictions**:
- Cannot report own event
- Critical reports (INAPPROPRIATE, FAKE_EVENT) trigger MQTT alert to AStA

**UI**: "Report Event" modal with reason selection and details textarea

---

### 5.2 Admin: View Reports

**Endpoints**:
- `GET /api/admin/reports` → All reports (query: `status`, `reason`)
- `GET /api/admin/reports/status/{status}` → Filter by status
- `GET /api/admin/reports/reason/{reason}` → Filter by reason
- `GET /api/admin/reports/{reportId}` → Single report

**Response**: `PageResponse<ReportDto>`

```typescript
interface ReportDto {
  id: string;
  eventId: string;
  eventTitle: string;
  reporter: UserDto;
  reason: ReportReason;
  details: string;
  status: ReportStatus;  // "OPEN" | "RESOLVED"
  createdAt: string;
}
```

**UI**: Admin moderation queue with:
- Filter by status/reason
- Event preview card
- Reporter info
- Resolve action

---

### 5.3 Admin: Resolve Report

**Endpoint**: `PATCH /api/admin/reports/{reportId}/resolve`

**Query**: `?flagEvent=false` (default)

**Effects**:
- Status → `RESOLVED`
- If `flagEvent=true` → Event status → `UNDER_REVIEW`

**UI**: "Resolve" button with optional "Flag Event" checkbox

---

### 5.4 Admin: Delete Report

**Endpoint**: `DELETE /api/admin/reports/{reportId}`

---

## Module 6: Notifications

### 6.1 Notification Types
```typescript
type NotificationType = 
  | "EVENT_APPROVED"      // Event approved by admin
  | "EVENT_REJECTED"      // Event rejected by admin
  | "WAITLIST_PROMOTED"   // Promoted from waitlist
  | "NEW_REVIEW"          // New review on host's event
  | "TRUST_PROMOTED"      // Promoted to TRUSTED_HOST
  | "EVENT_CANCELLED"     // Event user RSVP'd to was cancelled
  | "RSVP_CANCELLED"      // Someone cancelled RSVP (host notification)
  | "REPORT_RESOLVED";    // Report resolution update
```

### 6.2 Notification Structure
```typescript
interface NotificationDto {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  relatedEventId: string | null;
  relatedUserId: string | null;
  actionUrl: string | null;    // e.g., "/events/123"
  isRead: boolean;
  createdAt: string;
}
```

### 6.3 Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `GET /api/notifications` | GET | Paginated list (query: `unreadOnly=true`) |
| `GET /api/notifications/unread-count` | GET | Badge count |
| `PATCH /api/notifications/{id}/read` | PATCH | Mark single as read |
| `PATCH /api/notifications/read-all` | PATCH | Mark all as read |
| `DELETE /api/notifications/{id}` | DELETE | Dismiss notification |

**UI Requirements**:
- Notification bell icon with unread badge
- Dropdown/panel with recent notifications
- Click notification → navigate to `actionUrl`
- Group by type with icons
- "Mark all read" button
- Real-time updates via polling (SSE not used for notifications)

---

## Module 7: Search

### 7.1 Search Suggestions

**Endpoint**: `GET /api/search/suggestions`

**Query Parameters**:
```typescript
interface SearchSuggestionsParams {
  q: string;              // Required, search query
  type?: "ALL" | "EVENT" | "CATEGORY" | "USER" | "LOCATION";  // Default: "ALL"
}
```

**Response**: `SearchSuggestionDto[]`

```typescript
interface SearchSuggestionDto {
  type: "EVENT" | "CATEGORY" | "USER" | "LOCATION";
  value: string;        // Display text
  id: string;           // UUID or ID for navigation
  subtitle: string;     // Context (e.g., "15.06.2024 18:00 • Campusplatz")
}
```

**UI**: Search bar with autocomplete dropdown
- Group suggestions by type
- Click suggestion → navigate to detail page
- Max 5 suggestions per type

---

## Module 8: User Profiles

### 8.1 Public Profile

**Endpoint**: `GET /api/public/users/{userId}`

**Response**: `PublicProfileDto`
```typescript
interface PublicProfileDto {
  id: string;
  displayName: string;
  bio: string | null;
  profileImageUrl: string | null;
  trustLevel: TrustLevel;
  createdAt: string;
  completedEventsWithReviews: number;
  averageHostRating: number;  // 0.0 if none
}
```

**UI**: Public profile page with:
- Avatar, name, bio
- Trust level badge
- Host metrics (events hosted, avg rating)
- Hosted events list
- Reviews received

---

## Module 9: Admin Dashboard

### 9.1 Dashboard Stats

**Endpoint**: `GET /api/admin/dashboard`

**Response**: `AdminDashboardDto`
```typescript
interface AdminDashboardDto {
  pendingEventsCount: number;
  openReportsCount: number;
  totalUsersCount: number;
  newUsersToday: number;
  eventsThisWeek: number;
  recentReports: ReportDto[];       // Last 5 open reports
  recentPendingEvents: EventDto[];  // Last 5 pending events
}
```

**UI**: Admin dashboard with:
- Stat cards (pending events, open reports, users, etc.)
- Recent reports table
- Recent pending events table
- Quick action buttons

---

### 9.2 Event Moderation

**Endpoints**:
- `GET /api/admin/events` → All events (query: `status`)
- `GET /api/admin/events/pending` → `UNDER_REVIEW` events
- `PATCH /api/admin/events/{id}/approve` → Approve (→ `PUBLISHED`)
- `PATCH /api/admin/events/{id}/reject` → Reject (→ `CANCELLED`)
- `PATCH /api/admin/events/{id}/flag` → Flag (→ `UNDER_REVIEW`)

**Bulk Operations**:
- `POST /api/admin/events/bulk-approve` → Body: `BulkEventActionRequest`
- `POST /api/admin/events/bulk-reject` → Body: `BulkEventActionRequest`

```typescript
interface BulkEventActionRequest {
  eventIds: string[];  // 1-50 items
  reason?: string;     // For rejections, max 500 chars
}

interface BulkEventActionResult {
  processedCount: number;
  successCount: number;
  failedCount: number;
  succeededIds: string[];
  failedIds: string[];
  message: string;
}
```

**UI**: Event moderation queue with:
- Event cards with preview
- Approve/Reject/Flag actions
- Bulk selection with checkboxes
- Reject reason modal

---

### 9.3 User Management

**Endpoints**:
- `GET /api/admin/users` → List users (query: `search`, `trustLevel`)
- `GET /api/admin/users/{id}` → User detail
- `PATCH /api/admin/users/{id}/trust-level` → Update trust level
- `POST /api/admin/users/{id}/flag` → Flag user
- `POST /api/admin/users/{id}/promote` → Promote to TRUSTED_HOST (query: `force`)
- `DELETE /api/admin/users/{id}` → Delete user

**UI**: User management table with:
- Search/filter by trust level
- Trust level badges
- Quick actions (flag, promote, delete)
- User detail modal

---

### 9.4 Category Management

**Endpoints**:
- `GET /api/admin/categories` → All categories
- `POST /api/admin/categories` → Create (Body: `CategoryRequest`)
- `PUT /api/admin/categories/{id}` → Update
- `DELETE /api/admin/categories/{id}` → Delete

```typescript
interface CategoryRequest {
  name: string;        // Required, 1-50 chars
  icon?: string;       // Max 50 chars (e.g., "music", "tech")
  color?: string;      // Hex code, max 7 chars (e.g., "#FF5733")
  sortOrder?: number;  // Display order
}
```

**UI**: Category management with:
- Color picker for hex codes
- Icon selector
- Drag-and-drop sort order
- Name validation (unique)

---

## Module 10: Public Pages

### 10.1 Landing Page

**Data Sources**:
- `GET /api/public/events/featured` → Hero section events
- `GET /api/public/categories` → Category pills/filters
- `GET /api/public/events` → Event feed

**UI Requirements**:
- No auth required
- Event cards with thumbnail, title, date, location, host info
- Category filter chips
- Search bar with suggestions
- "Load more" pagination

### 10.2 Public Event Detail

**Endpoint**: `GET /api/public/events/{eventId}` or `GET /api/public/events/slug/{slug}`

**UI**: Read-only event view with:
- Event media carousel
- Host info with trust badge
- Date/time, location
- Description
- "Login to register" CTA (if not authenticated)

---

## 18. UI/UX Guidelines

### 18.1 Design Principles
1. **Mobile-First**: Campus students primarily use mobile
2. **Accessibility**: WCAG 2.1 AA compliance
3. **Performance**: Lazy loading images, virtualized lists
4. **Real-Time**: SSE connections for live updates
5. **Offline Awareness**: Graceful degradation when disconnected

### 18.2 Key UI Patterns

**Event Card**:
```
┌─────────────────────────────────┐
│ [Thumbnail 400x300]             │
│ Title (3-100 chars)             │
│ 📍 Location | 📅 Date           │
│ 👤 Host Name ⭐ 4.5 (12 reviews)│
│ [Category] [Category]           │
│ 👥 12/50 spots remaining        │
│ [REGISTER] / [WAITLIST]         │
└─────────────────────────────────┘
```

**Host Badge System**:
- `NEW` → Gray badge, events need review
- `TRUSTED_HOST` → Green badge with checkmark, instant publish
- `FLAGGED` → Red badge, account suspended

**RSVP Button States**:
- "Register" (available spots > 0)
- "Join Waitlist" (full)
- "Registered ✓" (GOING)
- "Waitlisted #3" (WAITLISTED)
- "Attended ✓" (ATTENDED)
- "Cancelled" (CANCELLED)
- "Event Cancelled" (event status)

**Notification Icons by Type**:
- `EVENT_APPROVED` → Checkmark
- `EVENT_REJECTED` → X mark
- `WAITLIST_PROMOTED` → Up arrow
- `NEW_REVIEW` → Star
- `TRUST_PROMOTED` → Trophy
- `EVENT_CANCELLED` → Alert triangle
- `RSVP_CANCELLED` → User minus

### 18.3 Form Validation (Frontend + Backend)
All forms must validate both client-side AND server-side:

| Field | Rules |
|-------|-------|
| Email | University format, regex validation |
| Password | 8+ chars, upper, lower, number, special |
| Display Name | 2-50 chars |
| Event Title | 3-100 chars, required |
| Event Description | Max 2000 chars |
| Location | Max 200 chars, required |
| Capacity | Min 1, required |
| Review Rating | 1-5, required |
| Review Comment | Max 1000 chars |
| Report Details | Max 2000 chars |
| Cancellation Reason | Max 500 chars |

### 18.4 Date/Time Handling
- **Backend**: ISO 8601 Instant (UTC)
- **Frontend**: Display in user's timezone (default Europe/Berlin)
- **Input**: DateTime picker with timezone indicator
- **Format**: `dd.MM.yyyy HH:mm` for German locale

### 18.5 Image Handling
- **Avatars**: 1:1 ratio, crop to circle
- **Event Thumbnails**: 400x300, crop center
- **Event Medium**: 800x600, preserve aspect
- **Event Full**: Original resolution
- **Lazy Loading**: Intersection Observer API
- **Placeholder**: Skeleton loader while loading

### 18.6 Error UI Patterns
- **Validation Errors**: Inline field errors, red border
- **API Errors**: Toast notification with backend message
- **Network Errors**: "Connection lost" banner with retry
- **Auth Errors**: Auto-redirect to login with return URL
- **Rate Limit**: "Too many attempts" with countdown timer

---

## 19. TypeScript Types

```typescript
// Enums
enum EventStatus {
  DRAFT = "DRAFT",
  PUBLISHED = "PUBLISHED",
  UNDER_REVIEW = "UNDER_REVIEW",
  CANCELLED = "CANCELLED",
  COMPLETED = "COMPLETED"
}

enum RsvpStatus {
  GOING = "GOING",
  WAITLISTED = "WAITLISTED",
  CANCELLED = "CANCELLED",
  ATTENDED = "ATTENDED"
}

enum TrustLevel {
  NEW = "NEW",
  TRUSTED_HOST = "TRUSTED_HOST",
  FLAGGED = "FLAGGED"
}

enum Role {
  STUDENT = "STUDENT",
  ADMIN = "ADMIN"
}

enum ReportReason {
  SPAM = "SPAM",
  INAPPROPRIATE = "INAPPROPRIATE",
  FAKE_EVENT = "FAKE_EVENT",
  OTHER = "OTHER"
}

enum ReportStatus {
  OPEN = "OPEN",
  RESOLVED = "RESOLVED"
}

enum MediaType {
  IMAGE = "IMAGE",
  VIDEO = "VIDEO"
}

enum NotificationType {
  EVENT_APPROVED = "EVENT_APPROVED",
  EVENT_REJECTED = "EVENT_REJECTED",
  WAITLIST_PROMOTED = "WAITLIST_PROMOTED",
  NEW_REVIEW = "NEW_REVIEW",
  TRUST_PROMOTED = "TRUST_PROMOTED",
  EVENT_CANCELLED = "EVENT_CANCELLED",
  RSVP_CANCELLED = "RSVP_CANCELLED",
  REPORT_RESOLVED = "REPORT_RESOLVED"
}

// API Response Wrapper
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string;
}

// Pagination
interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// All DTOs (as defined in previous sections)
// ... (consolidated from above)
```

---

## 20. Frontend State Management

### 20.1 Recommended Architecture
```
src/
├── api/              # Axios instances, interceptors
├── types/            # TypeScript interfaces
├── stores/           # Zustand/Redux stores
│   ├── authStore.ts
│   ├── eventStore.ts
│   ├── notificationStore.ts
│   └── sseStore.ts
├── hooks/            # Custom React hooks
│   ├── useAuth.ts
│   ├── useEvents.ts
│   ├── useSse.ts
│   └── useNotifications.ts
├── components/       # React components
│   ├── common/       # Buttons, inputs, cards
│   ├── events/       # Event-related
│   ├── auth/         # Auth forms
│   └── admin/        # Admin dashboard
└── pages/            # Route pages
```

### 20.2 Axios Interceptor Setup
```typescript
// api/client.ts
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
});

// Request interceptor: Add auth header
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: Handle 401 + refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        const response = await axios.post('/api/auth/refresh', null, {
          headers: { 'X-Refresh-Token': refreshToken }
        });

        const { accessToken, refreshToken: newRefresh } = response.data.data;
        useAuthStore.getState().setTokens(accessToken, newRefresh);

        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

### 20.3 SSE Hook
```typescript
// hooks/useEventSse.ts
import { useEffect, useRef } from 'react';

export const useEventSse = (eventId: string | null) => {
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (!eventId) return;

    const es = new EventSource(
      `${API_BASE}/api/events/stream/${eventId}`,
      { withCredentials: true }
    );

    es.addEventListener('rsvp-update', (e) => {
      const data = JSON.parse(e.data);
      // Update RSVP count in store
    });

    es.addEventListener('waitlist-update', (e) => {
      const data = JSON.parse(e.data);
      // Update waitlist info
    });

    es.addEventListener('event-cancelled', (e) => {
      // Show cancellation alert, redirect
    });

    es.onerror = () => {
      // Auto-reconnect after timeout
      es.close();
      setTimeout(() => useEventSse(eventId), 5000);
    };

    eventSourceRef.current = es;

    return () => {
      es.close();
    };
  }, [eventId]);
};
```

### 20.4 Auth Store (Zustand)
```typescript
// stores/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  user: UserDto | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;

  setAuth: (auth: AuthResponse) => void;
  setTokens: (access: string, refresh: string) => void;
  logout: () => void;
  updateUser: (user: UserDto) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isAdmin: false,

      setAuth: (auth) => set({
        user: auth.user,
        accessToken: auth.accessToken,
        refreshToken: auth.refreshToken,
        isAuthenticated: true,
        isAdmin: auth.user.role === 'ADMIN'
      }),

      setTokens: (access, refresh) => set({
        accessToken: access,
        refreshToken: refresh
      }),

      logout: () => set({
        user: null,
        accessToken: null,
        refreshToken: null,
        isAuthenticated: false,
        isAdmin: false
      }),

      updateUser: (user) => set({ user })
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        refreshToken: state.refreshToken,
        user: state.user
      })
    }
  )
);
```

---

## Appendix A: Complete Endpoint Reference

### Public Endpoints (No Auth)
| Method | Endpoint | Response |
|--------|----------|----------|
| GET | `/api/public/events` | `PageResponse<EventDto>` |
| GET | `/api/public/events/featured` | `PageResponse<EventDto>` |
| GET | `/api/public/events/{eventId}` | `ApiResponse<EventDto>` |
| GET | `/api/public/events/slug/{slug}` | `ApiResponse<EventDto>` |
| GET | `/api/public/categories` | `ApiResponse<CategoryDto[]>` |
| GET | `/api/public/users/{userId}` | `ApiResponse<PublicProfileDto>` |
| GET | `/api/search/suggestions` | `ApiResponse<SearchSuggestionDto[]>` |

### Authentication Endpoints
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/auth/register` | `RegisterRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/login` | `LoginRequest` | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/refresh` | - (X-Refresh-Token header) | `ApiResponse<AuthResponse>` |
| POST | `/api/auth/logout` | - | `ApiResponse<Void>` |
| GET | `/api/auth/verify?token=` | - | `ApiResponse<Void>` |
| POST | `/api/auth/resend-verification` | `ForgotPasswordRequest` | `ApiResponse<Void>` |
| POST | `/api/auth/forgot-password` | `ForgotPasswordRequest` | `ApiResponse<Void>` |
| POST | `/api/auth/reset-password` | `ResetPasswordRequest` | `ApiResponse<Void>` |
| GET | `/api/auth/me` | - | `ApiResponse<UserDto>` |
| PUT | `/api/auth/me` | `UpdateProfileRequest` (multipart) | `ApiResponse<UserDto>` |
| PUT | `/api/auth/me/password` | `ChangePasswordRequest` | `ApiResponse<Void>` |
| DELETE | `/api/auth/me` | - | `ApiResponse<Void>` |
| GET | `/api/auth/me/trust-status` | - | `ApiResponse<TrustQualificationStatus>` |
| GET | `/api/auth/me/preferences` | - | `ApiResponse<UserPreferencesDto>` |
| PUT | `/api/auth/me/preferences` | `UserPreferencesDto` | `ApiResponse<UserPreferencesDto>` |

### Event Endpoints (Authenticated)
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/events` | `CreateEventRequest` | `ApiResponse<EventDto>` |
| POST | `/api/events/draft` | `CreateEventRequest` | `ApiResponse<EventDto>` |
| GET | `/api/events` | - | `ApiResponse<PageResponse<EventDto>>` |
| GET | `/api/events/my-events` | - | `ApiResponse<PageResponse<EventDto>>` |
| GET | `/api/events/{eventId}` | - | `ApiResponse<EventDto>` |
| GET | `/api/events/by-slug/{slug}` | - | `ApiResponse<EventDto>` |
| PUT | `/api/events/{eventId}` | `CreateEventRequest` | `ApiResponse<EventDto>` |
| DELETE | `/api/events/{eventId}` | - | `ApiResponse<Void>` |
| DELETE | `/api/events/{eventId}/permanent` | - | `ApiResponse<Void>` |
| PATCH | `/api/events/{eventId}/cancel` | `CancelEventRequest` | `ApiResponse<EventDto>` |
| PATCH | `/api/events/{eventId}/restore` | - | `ApiResponse<EventDto>` |
| PUT | `/api/events/{eventId}/publish` | - | `ApiResponse<EventDto>` |
| POST | `/api/events/{eventId}/media` | multipart (images[], videos[]) | `ApiResponse<EventDto>` |
| DELETE | `/api/events/{eventId}/media/{mediaId}` | - | `ApiResponse<EventDto>` |
| PATCH | `/api/events/{eventId}/media/reorder` | `string[]` | `ApiResponse<EventDto>` |
| GET | `/api/events/{eventId}/check-in-code` | - | `ApiResponse<CheckInCodeDto>` |

### RSVP Endpoints (Authenticated)
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/events/{eventId}/rsvps` | - | `ApiResponse<RsvpDto>` |
| GET | `/api/events/{eventId}/rsvps/me` | - | `ApiResponse<RsvpDto>` |
| GET | `/api/events/{eventId}/rsvps` | - | `ApiResponse<PageResponse<RsvpDto>>` |
| GET | `/api/events/{eventId}/rsvps/status/{status}` | - | `ApiResponse<PageResponse<RsvpDto>>` |
| PATCH | `/api/events/{eventId}/rsvps/{rsvpId}/attended` | - | `ApiResponse<RsvpDto>` |
| PATCH | `/api/events/{eventId}/rsvps/{rsvpId}/promote` | - | `ApiResponse<RsvpDto>` |
| POST | `/api/events/{eventId}/check-in` | `CheckInRequest` | `ApiResponse<RsvpDto>` |
| GET | `/api/rsvps/me` | - | `ApiResponse<PageResponse<RsvpDto>>` |
| PATCH | `/api/rsvps/{rsvpId}/cancel` | `CancelRsvpRequest` | `ApiResponse<RsvpDto>` |
| GET | `/api/rsvps/{rsvpId}/position` | - | `ApiResponse<number>` |

### Review Endpoints (Authenticated)
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/reviews` | `CreateReviewRequest` | `ApiResponse<ReviewDto>` |
| GET | `/api/reviews/event/{eventId}` | - | `ApiResponse<PageResponse<ReviewDto>>` |
| GET | `/api/reviews/host/{hostId}` | - | `ApiResponse<PageResponse<ReviewDto>>` |
| POST | `/api/reviews/{reviewId}/helpful` | - | `ApiResponse<ReviewDto>` |
| POST | `/api/reviews/{reviewId}/report` | `ReviewReportRequest` | `ApiResponse<Void>` |
| DELETE | `/api/reviews/{reviewId}` | - | `ApiResponse<Void>` |

### Report Endpoints (Authenticated)
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/reports` | `CreateReportRequest` | `ApiResponse<ReportDto>` |

### Notification Endpoints (Authenticated)
| Method | Endpoint | Response |
|--------|----------|----------|
| GET | `/api/notifications` | `ApiResponse<PageResponse<NotificationDto>>` |
| GET | `/api/notifications/unread-count` | `ApiResponse<number>` |
| PATCH | `/api/notifications/{id}/read` | `ApiResponse<Void>` |
| PATCH | `/api/notifications/read-all` | `ApiResponse<number>` |
| DELETE | `/api/notifications/{id}` | `ApiResponse<Void>` |

### Admin Endpoints (ADMIN only)
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| GET | `/api/admin/dashboard` | - | `ApiResponse<AdminDashboardDto>` |
| GET | `/api/admin/events` | - | `ApiResponse<PageResponse<EventDto>>` |
| GET | `/api/admin/events/pending` | - | `ApiResponse<PageResponse<EventDto>>` |
| PATCH | `/api/admin/events/{id}/approve` | - | `ApiResponse<EventDto>` |
| PATCH | `/api/admin/events/{id}/reject` | - | `ApiResponse<Void>` |
| PATCH | `/api/admin/events/{id}/flag` | - | `ApiResponse<EventDto>` |
| POST | `/api/admin/events/bulk-approve` | `BulkEventActionRequest` | `ApiResponse<BulkEventActionResult>` |
| POST | `/api/admin/events/bulk-reject` | `BulkEventActionRequest` | `ApiResponse<BulkEventActionResult>` |
| GET | `/api/admin/reports` | - | `ApiResponse<PageResponse<ReportDto>>` |
| GET | `/api/admin/reports/{id}` | - | `ApiResponse<ReportDto>` |
| PATCH | `/api/admin/reports/{id}/resolve` | - | `ApiResponse<ReportDto>` |
| DELETE | `/api/admin/reports/{id}` | - | `ApiResponse<Void>` |
| GET | `/api/admin/users` | - | `ApiResponse<PageResponse<UserDto>>` |
| GET | `/api/admin/users/{id}` | - | `ApiResponse<UserDto>` |
| PATCH | `/api/admin/users/{id}/trust-level` | - | `ApiResponse<Void>` |
| POST | `/api/admin/users/{id}/flag` | - | `ApiResponse<Void>` |
| POST | `/api/admin/users/{id}/promote` | - | `ApiResponse<Void>` |
| DELETE | `/api/admin/users/{id}` | - | `ApiResponse<Void>` |
| GET | `/api/admin/categories` | - | `ApiResponse<CategoryDto[]>` |
| POST | `/api/admin/categories` | `CategoryRequest` | `ApiResponse<CategoryDto>` |
| PUT | `/api/admin/categories/{id}` | `CategoryRequest` | `ApiResponse<CategoryDto>` |
| DELETE | `/api/admin/categories/{id}` | - | `ApiResponse<Void>` |

### SSE Endpoint
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events/stream/{eventId}` | Server-Sent Events for live updates |

---

## Appendix B: Environment Variables

```bash
# Frontend .env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=MyStudyApp
VITE_DEFAULT_LANGUAGE=de
VITE_DEFAULT_TIMEZONE=Europe/Berlin
```

---

## Appendix C: Development Checklist

### Phase 1: Core (MVP)
- [ ] Authentication (register, login, logout, refresh)
- [ ] Email verification flow
- [ ] Password reset flow
- [ ] Event creation (with draft)
- [ ] Event feed (public + authenticated)
- [ ] Event detail page
- [ ] RSVP system (GOING/WAITLISTED)
- [ ] Basic profile page
- [ ] Notification system (basic)

### Phase 2: Enhanced
- [ ] Event media upload
- [ ] Media reordering
- [ ] Event slug/sharing
- [ ] Soft delete / trash bin
- [ ] Check-in system (QR codes)
- [ ] Waitlist management
- [ ] Review system
- [ ] Search with suggestions
- [ ] User preferences

### Phase 3: Polish
- [ ] Helpful votes on reviews
- [ ] Review reporting
- [ ] Report system (user → admin)
- [ ] Admin dashboard
- [ ] Admin event moderation
- [ ] Admin user management
- [ ] Admin category management
- [ ] Real-time SSE updates
- [ ] Advanced notifications

---

*This guide is generated 100% from the backend codebase. All endpoints, DTOs, validation rules, and business logic are extracted directly from the Java source files.*
