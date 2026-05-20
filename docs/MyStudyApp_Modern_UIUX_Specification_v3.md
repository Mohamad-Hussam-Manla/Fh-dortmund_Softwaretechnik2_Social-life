# MyStudyApp – Modern UI/UX Specification v3.0
## Contract-Driven Design System & Interaction Blueprint

> **Design Philosophy**: "Every interaction is a conversation with the backend. Every pixel communicates state. Every animation reduces cognitive load."
> 
> **Version**: 3.0 | **Date**: 2026-05-20 | **Backend**: Spring Boot JWT API

---

## Table of Contents

1. [Design System](#1-design-system)
2. [Global Interactions & Behaviors](#2-global-interactions--behaviors)
3. [Component Library](#3-component-library)
4. [Page Specifications](#4-page-specifications)
5. [Search & Discovery System](#5-search--discovery-system)
6. [Real-Time & SSE Integration](#6-real-time--sse-integration)
7. [Motion & Animation System](#7-motion--animation-system)
8. [Accessibility & Inclusive Design](#8-accessibility--inclusive-design)
9. [Performance & Optimization](#9-performance--optimization)
10. [Error Handling & Resilience](#10-error-handling--resilience)
11. [Complete API-to-UI Contract Map](#11-complete-api-to-ui-contract-map)
12. [Appendices](#12-appendices)

---

## 1. Design System

### 1.1 Design Tokens

```css
:root {
  /* ── Primary Palette ── */
  --primary-50:  #eef2ff;
  --primary-100: #e0e7ff;
  --primary-200: #c7d2fe;
  --primary-300: #a5b4fc;
  --primary-400: #818cf8;
  --primary-500: #6366f1;   /* Main action */
  --primary-600: #4f46e5;   /* Hover/Active */
  --primary-700: #4338ca;
  --primary-800: #3730a3;
  --primary-900: #312e81;

  /* ── Semantic Colors ── */
  --success-50:  #f0fdf4;
  --success-100: #dcfce7;
  --success-500: #22c55e;
  --success-600: #16a34a;
  --warning-50:  #fffbeb;
  --warning-100: #fef3c7;
  --warning-500: #f59e0b;
  --warning-600: #d97706;
  --error-50:    #fef2f2;
  --error-100:   #fee2e2;
  --error-500:   #ef4444;
  --error-600:   #dc2626;
  --info-50:     #eff6ff;
  --info-500:    #3b82f6;

  /* ── Trust Level System ── */
  --trust-new-text:       #6b7280;
  --trust-new-bg:         #f3f4f6;
  --trust-new-border:     #e5e7eb;
  --trust-trusted-text:   #059669;
  --trust-trusted-bg:     #d1fae5;
  --trust-trusted-border: #a7f3d0;
  --trust-flagged-text:   #dc2626;
  --trust-flagged-bg:     #fee2e2;
  --trust-flagged-border: #fecaca;

  /* ── Event Status System ── */
  --status-draft-text:       #6b7280;
  --status-draft-bg:         #f3f4f6;
  --status-published-text:   #16a34a;
  --status-published-bg:     #dcfce7;
  --status-review-text:      #d97706;
  --status-review-bg:        #fef3c7;
  --status-cancelled-text:   #dc2626;
  --status-cancelled-bg:     #fee2e2;
  --status-completed-text:   #4b5563;
  --status-completed-bg:     #f3f4f6;

  /* ── Neutral Scale ── */
  --white:       #ffffff;
  --gray-50:     #f9fafb;
  --gray-100:    #f3f4f6;
  --gray-150:    #ebecef;   /* Custom mid-step */
  --gray-200:    #e5e7eb;
  --gray-300:    #d1d5db;
  --gray-400:    #9ca3af;
  --gray-500:    #6b7280;
  --gray-600:    #4b5563;
  --gray-700:    #374151;
  --gray-800:    #1f2937;
  --gray-900:    #111827;
  --black:       #000000;

  /* ── Surface & Background ── */
  --surface:            var(--white);
  --surface-elevated:     var(--white);
  --surface-sunken:       var(--gray-50);
  --background:           var(--gray-50);
  --overlay:            rgba(0, 0, 0, 0.45);
  --overlay-light:      rgba(0, 0, 0, 0.25);
}

[data-theme="dark"] {
  --surface:            #1f2937;
  --surface-elevated:   #111827;
  --surface-sunken:     #0f172a;
  --background:         #0f172a;
  --gray-100:           #374151;
  --gray-200:           #4b5563;
  --gray-800:           #e5e7eb;
  --gray-900:           #f9fafb;
  --white:              #1f2937;
}
```

### 1.2 Typography System

**Font Family**: `Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`
**Monospace**: `"JetBrains Mono", "SF Mono", ui-monospace, monospace`

| Token | Mobile | Desktop | Weight | Line Height | Letter Spacing | Usage |
|-------|--------|---------|--------|-------------|----------------|-------|
| Display | 36px | 56px | 800 | 1.05 | -0.03em | Hero headlines |
| H1 | 30px | 40px | 700 | 1.1 | -0.02em | Page titles |
| H2 | 24px | 32px | 700 | 1.15 | -0.01em | Section headers |
| H3 | 20px | 24px | 600 | 1.25 | 0 | Card titles, modal headers |
| H4 | 18px | 20px | 600 | 1.3 | 0 | Sub-sections |
| Body | 16px | 16px | 400 | 1.6 | 0 | Paragraphs |
| Body Small | 14px | 14px | 400 | 1.5 | 0 | Descriptions, metadata |
| Caption | 13px | 13px | 500 | 1.4 | 0.01em | Labels, timestamps |
| Overline | 11px | 11px | 600 | 1.2 | 0.06em | Badges, category tags |
| Button | 15px | 15px | 600 | 1 | 0.01em | Button text |
| Mono | 14px | 14px | 500 | 1.4 | 0 | Check-in codes, IDs |

### 1.3 Spacing Scale (4px Base Grid)

```
0.5  = 2px   (hairline)
1    = 4px   (micro)
2    = 8px   (xs)
3    = 12px  (sm)
4    = 16px  (base/unit)
5    = 20px  (md)
6    = 24px  (lg)
8    = 32px  (xl)
10   = 40px  (2xl)
12   = 48px  (3xl)
16   = 64px  (4xl)
20   = 80px  (5xl)
24   = 96px  (6xl)
```

### 1.4 Elevation & Shadow System

```css
--shadow-1: 0 1px 2px 0 rgba(0,0,0,0.04);
--shadow-2: 0 1px 3px 0 rgba(0,0,0,0.08), 0 1px 2px -1px rgba(0,0,0,0.04);
--shadow-3: 0 4px 6px -1px rgba(0,0,0,0.08), 0 2px 4px -2px rgba(0,0,0,0.04);
--shadow-4: 0 10px 15px -3px rgba(0,0,0,0.08), 0 4px 6px -4px rgba(0,0,0,0.04);
--shadow-5: 0 20px 25px -5px rgba(0,0,0,0.08), 0 8px 10px -6px rgba(0,0,0,0.04);

/* Focus Rings */
--ring-primary: 0 0 0 3px rgba(99,102,241,0.25);
--ring-error:   0 0 0 3px rgba(239,68,68,0.25);
--ring-success: 0 0 0 3px rgba(34,197,94,0.25);
--ring-warning: 0 0 0 3px rgba(245,158,11,0.25);
```

### 1.5 Border Radius Scale

```
--radius-none: 0;
--radius-sm:   6px;   /* Inputs, small buttons */
--radius-md:   10px;  /* Cards, modals */
--radius-lg:   14px;  /* Large cards, sheets */
--radius-xl:   20px;  /* Bottom sheets, dialogs */
--radius-2xl:  24px;  /* Hero sections */
--radius-full: 9999px;/* Avatars, pills */
```

### 1.6 Z-Index Architecture

```
z-base:        0
z-dropdown:    100
z-sticky:      200
z-drawer:      300
z-modal:       400
z-popover:     500
z-toast:       600
z-tooltip:     700
z-fab:         800
z-devtools:   9999
```

---

## 2. Global Interactions & Behaviors

### 2.1 Navigation Architecture

**Mobile (< 640px)**:
- **Bottom Tab Bar**: 64px height + `env(safe-area-inset-bottom)`
- Tabs: 🏠 Feed | 🔍 Search | ➕ Create | 📅 My RSVPs | 👤 Profile
- Active state: Icon scale 1.1, color primary, label primary, translateY(-2px)
- Inactive: Gray-400, opacity 0.7
- "Create" center button: Elevated 8px, primary bg, white icon, shadow-4
- Badge: 18px circle, error-500 bg, white 11px text, positioned -6px -6px from icon top-right

**Desktop (≥ 1024px)**:
- **Top Navigation Bar**: 64px height, sticky, blur backdrop (glassmorphism)
- Left: Logo + App name
- Center: Global search bar (collapsible, expands on focus)
- Right: Notification bell + Avatar dropdown
- **Create Button**: Primary pill "+ New Event" in top-right

**Tablet (640–1024px)**:
- **Side Rail**: 72px wide icon-only nav, left side
- Icons with tooltips on hover
- Active indicator: 3px primary left border + primary tint bg

### 2.2 Command Palette / Global Search (Cmd+K)

**Trigger**: `Cmd/Ctrl + K` or click search bar
**Pattern**: Spotlight-style overlay, not a separate page

```
┌─────────────────────────────────────────────┐
│                                             │
│   ┌─────────────────────────────────────┐    │
│   │ 🔍 Search events, people, places...│    │
│   │                                     │    │
│   │ Recent: Yoga im Park                │    │
│   │ Recent: Tech Meetup                 │    │
│   └─────────────────────────────────────┘    │
│                                             │
│   Suggestions                               │
│   ┌─────────────────────────────────────┐  │
│   │ EVENTS                                │  │
│   │ 🎯 Yoga im Park        Sat 18:00    │  │
│   │ 🎯 Tech Talk           Mon 19:00    │  │
│   │                                       │  │
│   │ CATEGORIES                            │  │
│   │ 🏷️ Sports                             │  │
│   │ 🏷️ Technology                         │  │
│   │                                       │  │
│   │ PEOPLE                                │  │
│   │ 👤 Sarah Müller        Host           │  │
│   └─────────────────────────────────────┘  │
│                                             │
│   ↑↓ Navigate  ↵ Select  ␛ Close          │
└─────────────────────────────────────────────┘
```

**Behavior**:
- Opens as centered modal overlay (desktop) or full-screen (mobile)
- Debounced 200ms API call to `GET /api/search/suggestions?q={query}&type=ALL`
- Keyboard navigation: ↑↓ arrows, ↵ select, ␛ close
- Recent searches persisted in localStorage (max 5)
- Grouped by type with section headers
- Empty state: "Start typing to search events, categories, or people"
- Click outside or Esc closes

### 2.3 Toast Notification System

**Positioning**:
- Mobile: Bottom-center, 16px from bottom + safe area
- Desktop: Top-right, 24px from edges, stack downward

**Types & Specifications**:

| Type | Icon | Border | Background | Duration | Behavior |
|------|------|--------|------------|----------|----------|
| Success | Check circle | Success-200 | Success-50 | 4000ms | Auto-dismiss |
| Error | X octagon | Error-200 | Error-50 | 6000ms | Persistent if action needed |
| Warning | Alert triangle | Warning-200 | Warning-50 | 5000ms | Auto-dismiss |
| Info | Info circle | Info-200 | Info-50 | 4000ms | Auto-dismiss |
| Rate Limit | Clock | Warning-200 | Warning-50 | Dynamic | Countdown timer, manual dismiss |
| Loading | Spinner | Gray-200 | Gray-50 | Indefinite | Replaced by result toast |

**Stack Behavior**:
- Max 4 visible simultaneously
- New toasts push older up by 8px
- Excess toasts queue and appear as others dismiss
- Swipe right (mobile) to dismiss
- Hover (desktop) pauses auto-dismiss timer

**Animation**:
- Enter: Slide from bottom (mobile) / right (desktop) + fade, 300ms ease-out
- Exit: Fade out + slide right, 200ms ease-in
- Progress bar: 2px height bottom border that shrinks width over duration

### 2.4 Modal & Overlay System

**Layer Stack**:
1. **Backdrop**: `bg-overlay`, `backdrop-filter: blur(6px)`, fade 200ms
2. **Container**: `bg-surface`, `radius-xl`, `shadow-5`, scale 0.96→1.0 250ms ease-out
3. **Header**: Sticky, padding 20px 24px, border-bottom gray-100, title H3 + close X (24px)
4. **Body**: Max-height 75vh, overflow-y auto, padding 24px, scrollable
5. **Footer**: Sticky, padding 16px 24px, border-top gray-100, actions right-aligned, gap 12px

**Modal Types**:
- **Alert**: Title + message + single primary button
- **Confirm**: Title + message + secondary cancel + primary confirm (destructive = error color)
- **Form**: Title + form fields + cancel + submit
- **Bottom Sheet** (mobile only): Slides up from bottom, 85vh max, drag handle at top, swipe down to dismiss
- **Drawer** (desktop): Slides from right, 400px width, for filters, details

**Focus Management**:
- Trap focus within modal
- Auto-focus first input or primary button
- Return focus to trigger on close
- `aria-modal="true"`, `role="dialog"`

### 2.5 Pull-to-Refresh (Mobile)

**Trigger**: Pull down > 80px on scroll-top feeds
**Visual**:
- 0-40px: Arrow icon rotates 0→180°
- 40-80px: "Pull to refresh" text
- >80px: "Release to refresh" + arrow bounces
- Release: Spinner + "Updating..."
- Success: Checkmark + "Updated" flash, fade after 1s
- Error: X + "Couldn't refresh" + retry button

### 2.6 Infinite Scroll

**Trigger**: IntersectionObserver at 200px before list bottom
**Loading State**: 2 skeleton cards appended with stagger 100ms
**Complete**: "You've reached the end" text with party emoji, no more triggers
**Error**: Inline retry button "Failed to load more. [Retry]"
**Data Strategy**: TanStack Query `useInfiniteQuery`, keep previous data

### 2.7 Haptic Feedback (Mobile)

| Interaction | Haptic |
|-------------|--------|
| Button press | Light impact |
| Success action | Success notification |
| Error / Invalid | Error notification |
| Swipe action | Light impact |
| Refresh complete | Success notification |
| Waitlist promotion | Heavy impact + success |

*Implemented via Navigator.vibrate() fallback if Vibration API unavailable*

### 2.8 Gesture System (Mobile)

**Event Cards**:
- Long press: Haptic + context menu (Share, Report, Save)
- Horizontal swipe: Reveal actions (RSVP from feed, quick actions)

**Lists (RSVPs, Notifications)**:
- Swipe left: Primary action (Cancel RSVP, Mark read)
- Swipe right: Secondary action (View detail)
- Threshold: 120px reveal, snap open or close with spring physics

**Image Carousel**:
- Horizontal swipe: Next/prev image
- Pinch: Zoom (fullscreen mode)
- Double tap: Like / Zoom toggle
- Pan: Pan when zoomed

### 2.9 Form Validation UX

**Real-Time Validation**:
- Validate on blur (not on every keystroke to reduce noise)
- Re-validate on change after first error
- Debounce 300ms for async validation (email uniqueness)

**Visual Feedback**:
- Default: Border gray-300, label gray-500
- Focus: Border primary-500, ring-primary, label primary-600
- Valid: Border success-500, checkmark icon right, label success-600
- Invalid: Border error-500, ring-error, error icon left of message, label error-600
- Loading: Spinner inside right padding, border primary-300

**Floating Label Pattern**:
```
Empty:    Label inside input, gray-400, 16px
Focused:  Label shrinks to 12.5px, moves above border line, primary-600
Filled:   Same as focused but gray-600
Error:    Label error-600, error text 13px below with alert-circle icon
```

**Password Strength Indicator**:
```
[████░░░░░░] Weak      (1-2 criteria)
[████████░░] Fair      (3 criteria)
[██████████] Strong    (4-5 criteria)
```
- Color: Error → Warning → Success
- Checklist below: Each criterion gets ✓ or ○ in real-time

### 2.10 Offline & Network Resilience

**Detection**: `navigator.onLine` + heartbeat ping every 30s
**Banner**: "You're offline. Changes will sync when connected." Sticky top, warning-50 bg, warning-600 text, slide down 300ms
**Mutations**: Queue in IndexedDB, show "Will send when online" micro-toast, replay in order on reconnect
**Read Data**: Serve from TanStack Query cache (stale-while-revalidate strategy)
**Retry Strategy**: Exponential backoff 1s, 2s, 4s, max 3 retries

---

## 3. Component Library

### 3.1 Event Card (Universal)

**Used In**: Feed, Search Results, My Events, Admin Queue, Profile Hosted Events

**Layout**:
```
┌─────────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────────┐ │
│ │ [Thumbnail 16:9, radius-md top]             │ │
│ │ ┌────────┐  ┌────────────────────────┐     │ │
│ │ │Category│  │ 🔴 Live (if SSE active)  │     │ │
│ │ └────────┘  └────────────────────────┘     │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ **Event Title That Might Be Long**              │
│ 👤 Host Name  ⭐ 4.5 (12)  🛡️ Trusted          │
│                                                 │
│ 📅 Do, 22. Mai • 18:00 – 21:00                │
│ 📍 Library, Room 3.14                           │
│                                                 │
│ ┌─────────────────────────────────────────────┐│
│ │ ████████████░░░░░░░░░░░░░░  45/60 spots     ││
│ │ ⚡ 15 remaining • Waitlist: 3               ││
│ └─────────────────────────────────────────────┘│
│                                                 │
│ [Primary Action]  [Secondary]  [Ghost Icon]    │
└─────────────────────────────────────────────────┘
```

**Specs**:
- Width: 100% (mobile), calc(50% - 8px) (tablet), calc(33.333% - 16px) (desktop)
- Gap: 16px (mobile), 16px (tablet), 24px (desktop)
- Padding: 16px internal, 0px external
- Background: surface
- Border: 1px solid gray-150 (subtle)
- Radius: radius-md (10px)
- Shadow: shadow-1 default, shadow-3 on hover

**States**:
- **Default**: As above
- **Hover (desktop)**: translateY(-3px), shadow-3, 200ms ease-out
- **Active/Press**: Scale 0.98, 100ms
- **Loading**: Skeleton shimmer, no interactive elements
- **Error**: 3px error-500 left border, error message inline
- **Deleted**: Opacity 0.55, grayscale 100%, "Deleted" badge top-right
- **Under Review**: Orange dot indicator on thumbnail, "Pending" badge

**Thumbnail**:
- Aspect ratio: 16:9
- Object-fit: cover
- Loading: Blurhash placeholder → low-res 20px → full image fade 300ms
- Overlay gradient: Bottom 40% black 0→50% opacity for text readability
- Category badge: Bottom-left, overline style, primary bg, white text

**Host Row**:
- Avatar: 24px circle
- Name: Body Small, gray-700
- Rating: Star icon (warning-500) + "4.5" + "(12)" caption
- Trust badge: Inline pill, overline style

**Capacity Bar**:
- Height: 6px, radius-full
- Background: gray-200
- Fill: Gradient from primary-400 to primary-600
- Segments: >50% green, >80% warning-500, 100% error-500
- Animation: Width transition 600ms cubic-bezier(0.4, 0, 0.2, 1)
- Counter: Caption, gray-500
- "X remaining": Primary-600, font-weight 600, pulses once on decrease

**Action Row**:
- Primary: Full button (Join, Going, Waitlist)
- Secondary: Outline button (Share, Edit)
- Ghost: Icon only (More ⋮, Bookmark)
- Gap: 8px

### 3.2 RSVP Button (Stateful)

**The most critical interactive component. 8 states with distinct visual language.**

| State | Background | Text | Icon | Border | Shadow | Animation |
|-------|------------|------|------|--------|--------|-----------|
| **Join** | Primary-600 | White | Plus | None | shadow-2 | — |
| **Join Waitlist** | Warning-50 | Warning-700 | Clock | Warning-200 1px | — | — |
| **Going ✓** | Success-50 | Success-700 | Check | Success-200 1px | — | Check draw 300ms |
| **Waitlist #N** | Gray-100 | Gray-700 | List | Gray-200 1px | — | Number count-up |
| **Re-join** | Primary-600 | White | Refresh | None | shadow-2 | Rotate icon 360° |
| **Attended** | Gray-100 | Gray-400 | Check | Gray-200 1px | — | — |
| **Event Cancelled** | Error-50 | Error-600 | X | Error-200 1px | — | — |
| **Event Ended** | Gray-100 | Gray-400 | Clock | Gray-200 1px | — | — |
| **Full (disabled)** | Gray-100 | Gray-400 | Lock | Gray-200 1px | — | — |

**Interaction**:
- Click "Join" → Immediate optimistic: Button morphs to "Going ✓" with check draw animation, capacity bar updates, toast "You're in! 🎉"
- If API returns waitlist → Morphs to "Waitlist #X" with number count-up animation
- Click "Going" → Opens cancel confirmation modal (not immediate to prevent accidents)
- Click "Waitlist #N" → Shows tooltip: "Position #N. Estimated wait: ~2h"
- Hover (desktop): Tooltip with full status description

**Width Animation**: On state change, width smoothly transitions 300ms to accommodate new text length.

### 3.3 Host Badge System

```
┌─────────────────────────────────────────┐
│ 🛡️ TRUSTED_HOST  •  8 events hosted    │
└─────────────────────────────────────────┘
```

| Trust Level | Badge | Icon | Tooltip | Color Scheme |
|-------------|-------|------|---------|--------------|
| NEW | "New Host" | Shield outline | "Events require admin review before publishing" | Gray pill |
| TRUSTED_HOST | "Trusted" | Shield check | "Events publish instantly. Host rating ≥ 4.0" | Green pill |
| FLAGGED | "Suspended" | Shield alert | "Account under review. Contact support." | Red pill |

**Hover**: Tooltip appears 400ms delay, max-width 280px, radius-md, shadow-4, padding 12px 16px

### 3.4 Avatar Component

**Sizes**:
- XS: 24px (event card host)
- SM: 32px (comment author)
- MD: 40px (notification)
- LG: 64px (profile header)
- XL: 96px (edit profile preview)
- 2XL: 128px (public profile hero)

**States**:
- Default: Circle, object-fit cover, border 2px white + shadow-1
- Online: 10px green dot bottom-right, border 2px white
- Loading: Skeleton circle with shimmer
- Fallback: Initials (e.g., "SM") on primary-100 bg, primary-700 text, font-weight 600
- Uploadable: Overlay on hover "Change photo" with camera icon, opacity 0→0.7

### 3.5 Media Carousel

**Specs**:
- Aspect ratio: 16:9 (event detail), 4:3 (cards)
- Container: radius-md, overflow hidden, bg gray-900 (for letterboxing)

**Image Loading**:
1. Blurhash placeholder (20px decoded) immediate
2. Low-res 40px wide loaded, blurred 8px
3. Full image loaded, blur 8px→0px 400ms
4. Optional: Medium res (800px) as intermediate step

**Navigation**:
- Desktop: Arrow buttons (40px circle, white bg, shadow-2), opacity 0→1 on hover
- Mobile: Swipe horizontal, snap with spring physics
- Indicators: 8px dots, active: primary-500 width 20px pill, inactive: white 60% opacity
- Counter: "3 / 8" top-right on images

**Video**:
- Poster frame: First frame or custom thumbnail
- Play button: 56px circle, white bg, primary-500 triangle, shadow-3, scale 1.05 on hover
- Native controls on click (fullscreen capable)
- Mute by default in carousel, unmute on explicit click

**Fullscreen**:
- Pinch to zoom (mobile)
- Double-tap to zoom 2x (mobile)
- Click to zoom (desktop)
- Pan when zoomed
- Swipe to dismiss from fullscreen

**Thumbnail Strip**:
- Below main: 72px height, horizontal scroll
- Active: 2px primary-500 border, brightness 100%
- Inactive: opacity 0.6, hover opacity 1
- Gap: 8px

### 3.6 Skeleton Loader System

**Event Card Skeleton**:
```
┌─────────────────────────────────────────┐
│ ┌─────────────────────────────────────┐ │
│ │ [Shimmer rect 16:9]                 │ │
│ └─────────────────────────────────────┘ │
│ ┌────────────────────────┐              │
│ │ [Shimmer line 70%]     │              │
│ └────────────────────────┘              │
│ ┌────────────┐ ┌────────────────────┐ │
│ │ [Circle]   │ │ [Shimmer line 50%] │ │
│ └────────────┘ └────────────────────┘ │
│ ┌────────────────────────┐              │
│ │ [Shimmer line 40%]     │              │
│ └────────────────────────┘              │
│ ┌─────────────────────────────────────┐│
│ │ [Shimmer bar 6px]                   ││
│ └─────────────────────────────────────┘│
│ ┌─────────────────────────────────────┐│
│ │ [Shimmer button]                    ││
│ └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
```

**Animation**: Linear gradient sweep from -200% to 200%, 1.5s infinite, bg-gray-200 base
**Stagger**: Multiple skeletons delay 100ms each (0ms, 100ms, 200ms...)
**Pulse Variant**: Opacity 0.6→1.0 2s infinite (simpler, for reduced motion)

### 3.7 Empty State Component

**Structure**: Centered, max-width 320px, padding 48px 24px
**Elements**:
1. Illustration: 120px Lottie animation or SVG (themed light/dark)
2. Headline: H3, gray-700, margin-top 24px
3. Description: Body Small, gray-500, margin-top 8px, max-width 280px
4. Action: Primary or secondary button, margin-top 24px

**Variants**:

| Context | Illustration | Headline | Description | Action |
|---------|--------------|----------|-------------|--------|
| Feed empty | Empty calendar | "No events yet" | "Be the first to create an event on campus." | [Create Event] |
| My Events empty | Clipboard | "No events created" | "Start hosting events to build your reputation." | [Create First Event] |
| My RSVPs empty | Ticket blank | "No registrations" | "Browse events and join the community." | [Explore Events] |
| Search no results | Magnifying glass | "No matches found" | "Try different keywords or clear your filters." | [Clear Filters] |
| Notifications empty | Bell sleeping | "All caught up!" | "Check back later for updates." | — |
| Reviews empty | Star outline | "No reviews yet" | "Attend events and share your experience." | — |
| Trash empty | Clean bin | "Trash is empty" | "Deleted events appear here for 30 days." | — |
| Waitlist only | Hourglass | "You're on the waitlist" | "We'll notify you when a spot opens up." | [View Event] |
| Error | Warning cloud | "Something went wrong" | "We couldn't load this. Try refreshing." | [Retry] |
| Offline | Cloud offline | "You're offline" | "Connect to the internet to see events." | — |

### 3.8 Bottom Sheet (Mobile)

**Trigger**: Swipe up from bottom edge or button click
**Specs**:
- Height: Auto (content) or 85vh max
- Radius: radius-xl top (20px) only
- Drag handle: 36px wide, 4px tall, gray-300, centered top 8px
- Backdrop: Overlay with tap-to-dismiss
- Scroll: Content scrolls independently, sheet can be dragged down to dismiss
- Snap points: 50%, 85% (for tall content)
- Spring physics: Stiffness 300, damping 30

**Used For**:
- Event action menu (Share, Report, Save)
- Filter panel
- Date/time picker
- Category selector
- Attendee list quick view

### 3.9 Filter & Sort Chip Bar

**Layout**: Horizontal scroll, snap to start, gap 8px, padding 16px horizontal
**Chip States**:
- Inactive: Gray-100 bg, gray-600 text, radius-full, padding 8px 14px
- Active: Primary-50 bg, primary-700 text, primary-200 border 1px
- With value: Primary-100 bg, primary-700 text, close X icon right

**Sort Dropdown**:
- Trigger: "Sorted by: Upcoming ▼" chip
- Menu: Overlay below, radius-md, shadow-4, padding 8px
- Items: Radio selection, checkmark on active
- Options: Upcoming, Popular, Newest, Near me

**Active Filters Bar**:
- Shows below search as removable pills
- Each pill: "Category: Sports ✕"
- "Clear all" ghost button at end
- Appears with slide-down 200ms when filters active

### 3.10 Notification Badge & Center

**Bell Icon**:
- Default: Gray-500, 24px
- Active/unread: Primary-600, subtle pulse ring animation
- Badge: 18px circle, error-500, white 11px bold text
- Badge animation: Scale 0→1.2→1.0 400ms ease-bounce on new notification

**Notification Center** (Dropdown / Full page):
```
┌─────────────────────────────────────────┐
│ Notifications              [Mark All ✓] │
├─────────────────────────────────────────┤
│ Today                                   │
│ ┌─────────────────────────────────────┐ │
│ │ 🎉  Waitlist Promoted               │ │
│ │     You're now GOING to Tech Meetup │ │
│ │     2 min ago              ● unread   │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ ⭐  New Review                      │ │
│ │     You got a 5★ review on Yoga...  │ │
│ │     1h ago                 ○ read     │ │
│ └─────────────────────────────────────┘ │
│ Yesterday                               │
│ ...                                     │
├─────────────────────────────────────────┤
│ [View All Notifications →]              │
└─────────────────────────────────────────┘
```

**Item Specs**:
- Padding: 16px
- Icon: 40px circle, colored by type (success-50 bg + success-500 icon for promoted)
- Unread indicator: 8px primary-500 dot, left side
- Hover: Gray-50 bg
- Swipe left (mobile): Mark read / Delete with haptic

**Types & Icons**:

| Type | Icon | BG Color | Action URL |
|------|------|----------|------------|
| EVENT_APPROVED | Check circle | Success-50 | /events/{id} |
| EVENT_REJECTED | X circle | Error-50 | /my-events |
| WAITLIST_PROMOTED | Trending up | Primary-50 | /events/{id} |
| NEW_REVIEW | Star | Warning-50 | /events/{id}/reviews |
| TRUST_PROMOTED | Trophy | Success-50 | /profile |
| EVENT_CANCELLED | Alert triangle | Error-50 | /feed |
| RSVP_CANCELLED | User minus | Gray-50 | /events/{id}/attendees |
| REPORT_RESOLVED | Shield check | Info-50 | — |

---

## 4. Page Specifications

### 4.1 Guest Landing Page (`/`)

**Purpose**: Convert visitors to registered users. Showcase platform value.

**Layout**:
```
┌─────────────────────────────────────────┐
│ [Sticky Glass Header]                   │
│ Logo    🔍 Search...    [Log In] [Join] │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────┐   │
│  │                                 │   │
│  │   **Discover Campus Events**    │   │
│  │                                 │   │
│  │   Find, join, and host events   │   │
│  │   at your university.            │   │
│  │                                 │   │
│  │   [Explore Events]              │   │
│  │                                 │   │
│  └─────────────────────────────────┘   │
│                                         │
├─────────────────────────────────────────┤
│ 🔥 Featured Events                      │
│ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐         │
│ │     │ │     │ │     │ │     │         │
│ │     │ │     │ │     │ │     │         │
│ └─────┘ └─────┘ └─────┘ └─────┘         │
│ ←    Horizontal scroll    →             │
│                                         │
├─────────────────────────────────────────┤
│ Browse by Category                      │
│ ┌────────┐ ┌────────┐ ┌────────┐       │
│ │  🎵    │ │  💻    │ │  🏃    │       │
│ │ Music  │ │ Tech   │ │ Sports │       │
│ └────────┘ └────────┘ └────────┘       │
├─────────────────────────────────────────┤
│ Upcoming Events                         │
│ ┌─────────────┐ ┌─────────────┐        │
│ │ Event Card  │ │ Event Card  │        │
│ └─────────────┘ └─────────────┘        │
│ [Load More]                             │
├─────────────────────────────────────────┤
│ [Footer] About • Privacy • Contact      │
└─────────────────────────────────────────┘
```

**Header Behavior**:
- Initial: Transparent, white text over hero
- Scroll > 100px: Glassmorphism bg (white/80% + blur 12px), shadow-2, dark text
- Transition: 200ms ease

**Hero Section**:
- Background: Gradient primary-600→primary-800 or campus photo with overlay
- Height: 50vh mobile, 40vh desktop
- Text: White, centered
- CTA: Primary button (white bg, primary text) + ghost button (white border)

**Featured Events**:
- Horizontal scroll, snap to card
- Auto-advance every 6s, pause on hover/touch
- Card size: 280px wide, 340px tall
- Show 1.5 cards on mobile to indicate scrollability

**Category Grid**:
- 3 columns mobile, 6 desktop
- Icon + label, centered
- Hover: Scale 1.05, shadow-2
- Click: Filter feed by category

**Upcoming Events**:
- 2-column grid mobile, 3 desktop
- Infinite scroll
- Each card has "Login to register" subtle CTA instead of RSVP button

**Footer**:
- Minimal: Links + copyright
- Background: Gray-50

### 4.2 Authentication Flow

#### 4.2.1 Register Page (`/register`)

**Layout**: Centered card, max-width 440px, min-h-screen flex center
**Card**: Surface, radius-lg, shadow-4, padding 32px

**Form Flow**:
```
Step 1: Email
┌─────────────────────────────────────────┐
│ ← Back                                  │
│                                         │
│ **Create Account**                      │
│ Join your campus community              │
│                                         │
│ University Email *                      │
│ ┌─────────────────────────────────────┐ │
│ │ maria@tu-dortmund.de            │ │
│ └─────────────────────────────────────┘ │
│ ✓ Valid university email                │
│                                         │
│ [Continue →]                            │
│                                         │
│ Already have an account? **Log in**     │
└─────────────────────────────────────────┘
```

**Email Validation**:
- Real-time regex check (blocks free providers)
- Green check + "Valid university email" when passes
- Red alert + "Please use your university email" when fails
- Debounce 300ms

```
Step 2: Profile
│ Display Name *                          │
│ ┌─────────────────────────────────────┐ │
│ │ Maria Müller                    14/50│ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Step 3: Password                        │
│ Password *                              │
│ ┌─────────────────────────────────────┐ │
│ │ ••••••••••                    [👁️]│ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Strength: [████░░░░░░] Fair             │
│ ┌─────────────────────────────────────┐ │
│ │ ✓ 8+ characters                     │ │
│ │ ✓ 1 uppercase letter                │ │
│ │ ✓ 1 lowercase letter                │ │
│ │ ○ 1 number                          │ │
│ │ ○ 1 special character               │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Create Account]                        │
```

**Password Field**:
- Toggle visibility: Eye icon, switches to eye-off
- Strength meter: 5 segments, fills as criteria met
- Color: Error → Warning → Success
- Criteria checklist updates in real-time
- Button disabled until all criteria met

**Submit States**:
- Default: Primary-600, white text
- Loading: Spinner replaces text, disabled
- Success: "Account created!" toast → redirect `/verify-email-sent`

**Error Handling**:
- 409 Duplicate: Shake form (translateX ±8px, 3 cycles, 400ms), highlight email red, message "This email is already registered. [Log in instead]"
- 400 Validation: Inline per field, auto-scroll to first error, focus
- 429 Rate Limit: Button disabled, overlay countdown "Try again in 45s"

#### 4.2.2 Login Page (`/login`)

**Layout**: Same centered card pattern
**UX Details**:
- "Remember me" checkbox: Persists email in localStorage (never password)
- "Forgot password?" link: Subtle, below password field
- Submit: "Signing in..." loading state
- Success: Decode JWT, redirect based on role

**Error States**:
- 401: Shake + "Invalid email or password" (intentionally vague for security)
- 403 Disabled: Modal overlay (not just toast) with:
  - Title: "Verify your email"
  - Message: "Please check your inbox and click the verification link."
  - [Resend Email] button → `POST /api/auth/resend-verification`
  - [I'll check later] secondary
- 403 Locked: Full-screen error page "Account suspended" with support contact

#### 4.2.3 Email Verification (`/verify-email?token=xxx`)

**States**:
1. **Loading**: Full-screen centered spinner + "Verifying your email..." caption
2. **Success**: 
   - Green circle with checkmark draw animation (300ms)
   - "You're all set!" H2
   - "Your email has been verified. You can now log in." body
   - Auto-redirect `/login?verified=true` after 3s with countdown "Redirecting in 3..."
3. **Expired/Invalid**:
   - Red circle with X
   - "This link has expired or is invalid."
   - "Verification links are valid for 24 hours."
   - [Register Again] primary + [Contact Support] ghost

#### 4.2.4 Forgot Password Flow

**Step 1** (`/forgot-password`):
- Email input + [Send Reset Link]
- Security: Always show identical success toast regardless of email existence: "If that email is registered, you'll receive a reset link shortly."
- No error for non-existent emails

**Step 2** (`/reset-password?token=xxx`):
- Validate token on mount: `GET` check (optional)
- New password + Confirm password
- Real-time match validation: "Passwords match ✓" green text
- Token expired → Banner error + [Request New Link]
- Success → "Password updated!" → redirect `/login` after 2s

### 4.3 Authenticated Feed (`/feed`)

**Layout**:
```
┌─────────────────────────────────────────┐
│ Hello, Maria! 👋           🔔 3  [Avatar]│
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🔍  Search events, categories...  │ │
│ │     Cmd+K                         │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│ ┌────┐┌────┐┌────┐┌────┐┌────┐┌────┐  │
│ │All ││Tech││Food││Sport││+   ││... │  │
│ └────┘└────┘└────┘└────┘└────┘└────┘  │
├─────────────────────────────────────────┤
│ ⭐ Sorted by: **Upcoming** ▼            │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🎉 Promotion Alert                  │ │
│ │ You're now GOING to Tech Talk!    │ │
│ │ [Dismiss]                           │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ [Event Card 1]                      │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ [Event Card 2]                      │ │
│ └─────────────────────────────────────┘ │
│ ...                                     │
│ [Skeleton loading...]                   │
├─────────────────────────────────────────┤
│         [Bottom Navigation]             │
└─────────────────────────────────────────┘
```

**Header Greeting**:
- Time-aware: "Good morning, Maria!" / "Good afternoon!" / "Good evening!"
- Uses `displayName` from auth store
- Updates immediately on profile edit (no refresh needed)

**Search Integration**:
- Search bar is **part of feed**, not a separate page
- Clicking opens Command Palette overlay
- Recent searches shown below input before typing
- On mobile: Full-screen search overlay with keyboard auto-focus

**Category Pills**:
- Horizontal scroll, snap to start
- "All" always first, active by default
- Active: Primary style
- More (⋮): Opens bottom sheet with all categories grid
- Long press on category: Preview count "12 events"

**Sort Dropdown**:
- Chip trigger: "Sorted by: Upcoming ▼"
- Options: Upcoming (startTime,asc), Popular (viewCount,desc), Newest (createdAt,desc)
- Active option has checkmark

**Promotion Banners**:
- SSE-driven: When `waitlist-update` with promotion received
- Dismissible, stored in sessionStorage per eventId
- Auto-dismiss after 10 seconds if not interacted
- Background: Success-50, border success-200, left border 4px success-500

**RSVP Interactions**:
- Optimistic UI: Button state changes immediately on click
- Toast confirmation: "You're in! 🎉" (success) or "You're on the waitlist" (warning)
- If API fails: Rollback with shake animation + error toast
- Capacity bar animates smoothly on count change

**Empty State**: If no events in feed → Empty state component with "No upcoming events" + [Create First Event]

### 4.4 Event Detail Page (`/events/:id` or `/e/:slug`)

**Layout**:
```
┌─────────────────────────────────────────┐
│ ← Back    [Share]  [More ⋮]            │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ [Media Carousel 16:9]             │ │
│ │ • • • •                             │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│                                         │
│ **Campus Tech Meetup**                  │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ [Avatar] Maria Müller               │ │
│ │ ⭐ 4.9 (23 reviews)  🛡️ Trusted     │ │
│ │ 8 events hosted • TRUSTED_HOST      │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 📅 Do, 22. Mai 2026                 │ │
│ │ 🕒 18:00 – 21:00                    │ │
│ │ 📍 Library, Room 3.14               │ │
│ │ 👥 45/60 spots • ⚡ 15 remaining    │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ ████████████░░░░░░░░░░░░░░  75%   │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Your status: **GOING**                  │
│ [Cancel Registration]                   │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 📝 Description                      │ │
│ │ Join us for an evening of tech...   │ │
│ │ [Read more]                         │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ 🏷️ [Technology] [Networking]            │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ [Details] [Attendees] [Reviews]   │ │
│ │                                     │ │
│ │ Tab Content                         │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Related Events]                        │
│ ┌─────────┐ ┌─────────┐                │
│ │ Card    │ │ Card    │                │
│ └─────────┘ └─────────┘                │
├─────────────────────────────────────────┤
│ [Sticky Bottom Bar - Mobile Only]       │
│ [Cancel Registration]                   │
└─────────────────────────────────────────┘
```

**Media Carousel**:
- Full-width, 16:9, radius-none top (flush with screen on mobile)
- Swipe to navigate
- Pinch to zoom (opens fullscreen viewer)
- Video: Play overlay, native controls on click
- Thumbnail strip below if >1 media item

**Host Card**:
- Clickable, navigates to public profile
- Avatar 48px
- Rating stars: 5 stars, filled based on rating, half-star support
- "8 events hosted" = trust metric
- Trust badge: Clickable tooltip with full explanation

**Date/Time Block**:
- Icon + text layout, 16px gap
- Date: "Do, 22. Mai 2026" (German locale default)
- Time: "18:00 – 21:00" (24h format)
- Location: Clickable, opens maps intent on mobile
- Calendar add button: "+ Add to calendar" (generates .ics)

**Capacity Section**:
- Progress bar: Animated, color-coded
- "15 remaining" in primary-600, pulses on update
- If full: "Event is full" + waitlist count
- If waitlist: "Waitlist: 3 people" in warning-600

**Description**:
- Collapsible: Show first 3 lines, "Read more" expands
- Max 2000 chars, supports markdown (bold, italic, links)
- Links open in new tab

**Tab System**:

*Tab: Details*:
- Full description (expanded)
- Location map placeholder (future: embedded map)
- Host full card with bio
- Created date: "Posted 3 days ago"
- Share section: Copy link, native share (mobile)

*Tab: Attendees* (Host only, or "Who's going" for public):
- Filter chips: All | Going | Waitlisted | Attended | Cancelled
- Search by name
- List: Avatar 40px + name + status badge + join date
- Host actions: Check-in toggle, promote from waitlist
- Bulk select: Checkbox + actions bar (export, message)

*Tab: Reviews*:
- Average rating: Big number (4.5) + star breakdown (5★: 12, 4★: 8...)
- Sort: Most Helpful | Newest
- Review card: Avatar, name, stars, comment, date, helpful toggle, report
- "Write a Review" button (only if attended + event ended + not reviewed)
- Empty state if no reviews

**Host View Additions**:
- [Edit Event] floating action button or top-right icon
- [Check-in Code] expandable panel:
  - QR code (generated from checkInCode, 200px)
  - 6-character code in monospace, 24px, copy button
  - Countdown: "Refreshes in 4:32" (mm:ss)
  - [Regenerate Now] secondary button
- [Manage Media] button
- Attendance stats: "32 of 45 checked in (71%)"
- [Cancel Event] destructive button at bottom

**SSE Live Indicator**:
- Green pulsing dot in header: "Live updates"
- Pulse animation: Scale 1→1.5→1, opacity 1→0.5→1, 2s infinite
- On disconnect: Gray dot, "Updates paused"

**Bottom Bar (Mobile)**:
- Sticky, 64px + safe area, surface bg, shadow-4 top
- Primary action: RSVP button (full width) or Cancel
- Secondary: Share icon

### 4.5 Create / Edit Event Wizard (`/events/new`, `/events/:id/edit`)

**Pattern**: Step wizard (4 steps) with persistent auto-save
**Progress**: Step indicator at top, connected dots with labels

#### Step 1: Basics
```
┌─────────────────────────────────────────┐
│ ← Cancel                    1 of 4      │
│ ●━━━━━━━○━━━━━━○━━━━━━○                 │
│ Basics → Date → Media → Review          │
│                                         │
│ **Event Basics**                        │
│                                         │
│ Title *                                 │
│ ┌─────────────────────────────────────┐ │
│ │ Campus Tech Meetup              23/100│
│ └─────────────────────────────────────┘ │
│                                         │
│ Location *                              │
│ ┌─────────────────────────────────────┐ │
│ │ Library, Room 3.14                │ │
│ └─────────────────────────────────────┘ │
│ Suggestions: Library, Campuswiese...    │
│                                         │
│ Max Capacity *                          │
│ ┌─────────────────────────────────────┐ │
│ │ 60                              [+-]│ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Categories                              │
│ ┌────┐ ┌────┐ ┌────┐                   │
│ │Tech│ │Netw│ │+   │                   │
│ └────┘ └────┘ └────┘                   │
│                                         │
│ [Save Draft]        [Continue →]        │
│ Draft saved 2 min ago                   │
└─────────────────────────────────────────┘
```

**Auto-Save**:
- Debounced 5s after last keystroke
- `POST /api/events/draft` with current form data
- Micro-toast: "Draft saved" (appears bottom-left, fades 2s)
- Draft indicator: "Draft saved 2 min ago" caption below buttons
- On navigate away: Confirm modal if unsaved changes

**Validation**:
- Title: Real-time 3-100 chars, counter turns orange at 90, red at 101
- Location: Max 200 chars, location suggestions from past events
- Capacity: Min 1, stepper buttons (+/-), number input
- Categories: Multi-select chips, max 3

#### Step 2: Date & Time
```
│ **Date & Time**                         │
│                                         │
│ Start Time *                            │
│ ┌─────────────────────────────────────┐ │
│ │ 📅 22.05.2026    🕒 18:00         │ │
│ └─────────────────────────────────────┘ │
│ Timezone: Europe/Berlin                 │
│                                         │
│ End Time *                              │
│ ┌─────────────────────────────────────┐ │
│ │ 📅 22.05.2026    🕒 21:00         │ │
│ └─────────────────────────────────────┘ │
│ Duration: 3 hours                       │
│                                         │
│ [Visual Timeline Preview]               │
│ ┌─────────────────────────────────────┐ │
│ │ Now ────[====Event====]────→ Future │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Validation:                             │
│ ✓ Start time is in the future           │
│ ✓ End time is after start time          │
│ ○ No scheduling conflicts               │
```

**DateTime Picker**:
- Mobile: Native `datetime-local` with styled wrapper
- Desktop: Split date picker + time picker for better UX
- Timezone: Fixed display "Europe/Berlin", backend converts to UTC
- Validation: Real-time, inline
- Conflict warning: If host has another event within ±2h (future enhancement)

#### Step 3: Description & Media
```
│ **Description & Media**                 │
│                                         │
│ Description                             │
│ ┌─────────────────────────────────────┐ │
│ │ Tell people what to expect...       │ │
│ │                                   0/2000│
│ └─────────────────────────────────────┘ │
│ Supports: **bold**, *italic*, [links]   │
│                                         │
│ Media Upload                            │
│ ┌─────────────────────────────────────┐ │
│ │         📤                          │ │
│ │   Drag & drop or click to upload    │ │
│ │   Images: max 5 (5MB each)          │ │
│ │   Videos: max 2 (20MB each)         │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌────────┐ ┌────────┐ ┌────────┐       │
│ │ [Img1] │ │ [Img2] │ │ [Video]│       │
│ │   ✕    │ │   ✕    │ │   ✕    │       │
│ │  ≡drag │ │  ≡drag │ │  ≡drag │       │
│ └────────┘ └────────┘ └────────┘       │
│                                         │
│ [Save Draft]        [Continue →]        │
```

**Media Upload**:
- Drag-drop zone: Dashed border primary-300, bg primary-50 on drag-over
- File picker: Multi-select enabled
- Pre-upload validation: Type, size, count limits (client-side)
- Upload progress: Circular progress per file (SVG), percentage text
- Preview grid: 3 columns, gap 12px
- Each item: Thumbnail, delete X top-right, drag handle bottom-left
- Reorder: Drag and drop with visual ghost
- Error: Red border + message below failed item

#### Step 4: Preview & Publish
```
│ **Preview & Publish**                   │
│                                         │
│ This is how your event will appear:     │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ [Event Card Preview - Exact]        │ │
│ │ (Interactive, same as feed)         │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ⚠️ Trust Level Notice                 │
│ ┌─────────────────────────────────────┐ │
│ │ 🛡️ NEW Host                         │ │
│ │ Your event will be reviewed by      │ │
│ │ admins before publication.          │ │
│ │ Estimated review time: ~24 hours    │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Save as Draft]    [Publish Now]        │
│                                         │
│ By publishing, you agree to our       │
│ Community Guidelines.                   │
└─────────────────────────────────────────┘
```

**Preview Card**: Exact replica of feed card, non-interactive RSVP button
**Trust Warning**: Only shown for NEW hosts, warning-50 bg, warning-600 text
**Buttons**: 
- Save Draft: Secondary, always available
- Publish Now: Primary, disabled if validation errors, loading state "Publishing..."
**Success**: Redirect to event detail with "Event published!" toast
**If UNDER_REVIEW**: Toast "Event submitted for review. We'll notify you when it's live."

### 4.6 My Events Page (`/my-events`)

**Tabs** (sticky below header):
1. **Active** (default): DRAFT, PUBLISHED, UNDER_REVIEW, COMPLETED — not deleted
2. **Under Review**: Only UNDER_REVIEW (highlighted for NEW hosts)
3. **Trash**: deletedAt != null

**Active Event Card**:
```
┌─────────────────────────────────────────┐
│ [Thumbnail]    **Tech Meetup**          │
│                [PUBLISHED] ✓            │
│                📍 Library               │
│                🕒 Tomorrow 18:00          │
│                👥 45/60 spots           │
│                                         │
│ ┌────────┐ ┌────────┐ ┌────────┐         │
│ │  Edit  │ │ Share  │ │ Cancel │         │
│ └────────┘ └────────┘ └────────┘         │
└─────────────────────────────────────────┘
```

**Status Badges**:
- DRAFT: Gray pill, "Draft"
- PUBLISHED: Green pill, "Live"
- UNDER_REVIEW: Orange pill, "In Review" + clock icon
- COMPLETED: Gray pill, "Completed"

**Swipe Actions (Mobile)**:
- Swipe left: Edit (blue) | Delete (red)
- Swipe right: Share (gray)
- Haptic feedback on reveal

**Trash Card**:
- Opacity 0.6, grayscale
- "Deleted on 20.05.2026" caption
- [Restore] secondary + [Delete Forever] destructive
- Delete Forever: Confirmation modal "Type event name to confirm"

**Empty States**: Per tab with contextual actions

### 4.7 My Registrations (`/my-rsvps`)

**Tabs**: Upcoming | Waitlist | Past | Cancelled

**Upcoming Card**:
```
┌─────────────────────────────────────────┐
│ **Tech Meetup**        [GOING] ✓        │
│ 📍 Library • 🕒 Tomorrow 18:00           │
│ Host: Maria ⭐ 4.9                     │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 📋 Check-in Code: A7B2C3            │ │
│ │ Show QR Code                        │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Cancel] [View Event]                   │
└─────────────────────────────────────────┘
```

**Waitlist Card**:
```
│ **Tech Meetup**        [WAITLIST #3]    │
│ 📍 Library • 🕒 Tomorrow 18:00           │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ ⏳ Position: #3 of 5                │ │
│ │ Estimated wait: ~2 hours            │ │
│ │ [Progress bar showing position]     │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Leave Waitlist] [View Event]           │
```

**Waitlist Position API**: `GET /api/rsvps/{id}/position`
- Visual: Position number with count-up animation on update
- Progress: "#3 of 5" with mini bar
- SSE: If promoted while on this page → Full-screen banner + haptic heavy impact

**Past Card**:
- "Attended" badge if status = ATTENDED
- [Leave Review] button if not reviewed yet (primary)
- [View Event] secondary

**Cancelled Card**:
- "Cancelled on 20.05" timestamp
- [Re-register] primary button
- Reason shown if provided

### 4.8 Profile Page (`/profile`)

**Layout**:
```
┌─────────────────────────────────────────┐
│ ← Back                     ✏️ Edit     │
│                                         │
│              ┌─────────┐                │
│              │         │                │
│              │ Avatar  │                │
│              │  96px   │                │
│              │  (tap)  │                │
│              └─────────┘                │
│                                         │
│         **Maria Müller**                │
│         maria@tu-dortmund.de            │
│         🛡️ TRUSTED_HOST                │
│                                         │
│  ┌────────┐ ┌────────┐ ┌────────┐     │
│  │   8    │ │   23   │ │  4.9★  │     │
│  │ Events │ │ Reviews│ │ Rating │     │
│  └────────┘ └────────┘ └────────┘     │
│                                         │
│  Trust Progress                         │
│  ┌───────────────────────────────────┐  │
│  │ Events hosted: ████████░░ 8/3   │  │
│  │ Avg rating:    ██████████ 4.9/5 │  │
│  │                                 │  │
│  │ ✓ Qualifies for TRUSTED_HOST    │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌─────────────────────────────────────┐│
│  │ Bio                                 ││
│  │ Computer Science student & event    ││
│  │ enthusiast. Love tech meetups!      ││
│  │                           42/500   ││
│  └─────────────────────────────────────┘│
│                                         │
│  [My Events] [Settings] [Logout]        │
└─────────────────────────────────────────┘
```

**Edit Mode**:
- Inline transition: Fields become inputs, buttons change to Save/Cancel
- Avatar: Tap opens image picker → crop to 1:1 → preview immediately
- Bio: Textarea with counter, auto-resize height
- Display name: Input with validation
- Save: PUT `/api/auth/me` (multipart), optimistic update + toast

**Public Profile** (`/users/:id`):
- Same layout minus edit/settings/logout
- Shows hosted events list (paginated)
- Shows reviews received
- [Follow] button (future feature placeholder)

### 4.9 Settings Page (`/settings`)

**Grouped Sections**:

**Notifications**:
- Toggle rows: Email notifications, Push notifications, RSVP changes, New reviews
- Each: Icon left, label, description caption, toggle right
- Toggle: 48px wide, 24px tall, primary-500 active, gray-300 inactive, spring animation

**Appearance**:
- Dark mode: Toggle with sun/moon icon
- Compact view: Toggle (reduces card padding, smaller text)
- Preview: Live preview of card below toggles

**Preferences**:
- Language: Select dropdown (de/en), default de
- Timezone: Select, default Europe/Berlin, shows current time

**Account**:
- Change password: Opens modal with current + new + confirm
- Delete account: Destructive red text, opens confirmation modal with:
  - Warning icon + "This cannot be undone"
  - Checkbox "I understand all my events, RSVPs, and reviews will be deleted"
  - Type "DELETE" confirmation input
  - [Permanently Delete Account] button (disabled until confirmed)

### 4.10 Notifications Center

**Dropdown** (desktop, max-height 480px):
- Header: "Notifications" + "Mark all read" + settings gear
- Grouped by date: Today, Yesterday, Earlier
- Each item: Icon 36px + content + time + unread dot
- Footer: "View all notifications →"

**Full Page** (`/notifications`):
- Same grouped list, full scrollable
- Filter: All | Unread
- Bulk actions: Select mode (checkboxes) + Mark read / Delete
- Swipe actions (mobile): Mark read | Delete

**Real-time**:
- Badge updates via polling `GET /api/notifications/unread-count` every 30s
- On new notification: Badge bounce animation + optional push notification (if permitted)

### 4.11 Admin Dashboard (`/admin`)

**Layout**:
```
┌─────────────────────────────────────────┐
│ [Sidebar Nav]    Admin Dashboard        │
│                  [Exit Admin Mode]      │
├─────────────────────────────────────────┤
│ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐   │
│ │ 12 │ │ 5  │ │342 │ │ 8  │ │ 18 │   │
│ │Pend│ │Open│ │Tot │ │New │ │Week│   │
│ └────┘ └────┘ └────┘ └────┘ └────┘   │
│  Events Reports Users Today  Events   │
├─────────────────────────────────────────┤
│ Quick Actions                           │
│ ┌─────────────────────────────────────┐ │
│ │ • Movie Night    10m ago  [✓][✗]  │ │
│ │ • Debate Club    2h ago   [✓][✗]  │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│ Open Reports (3)          [View All →] │
│ ┌─────────────────────────────────────┐ │
│ │ • Fake event: Beach Party           │ │
│ │   Reported by: Alex                 │ │
│ │   [Investigate]                     │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│ Recent Users                            │
│ ...                                     │
└─────────────────────────────────────────┘
```

**Stats Cards**:
- Color-coded: Pending = warning, Open reports = error, Users = info
- Trend: Up/down arrow with percentage vs last week
- Click: Navigate to respective section

**Quick Actions**:
- Inline approve/reject for pending events
- Swipe (mobile): Approve right, Reject left
- Bulk: Checkbox selection + action bar

**Navigation Sidebar**:
- Dashboard, Events, Reports, Users, Categories
- Active: Primary left border + primary tint bg
- Collapsible on tablet

### 4.12 Admin Event Moderation (`/admin/events/pending`)

**Table View** (desktop):
- Columns: Checkbox | Thumbnail 60px | Title | Host | Submitted | Status | Actions
- Sortable columns: Click header to sort
- Row hover: Gray-50 bg
- Actions: Approve (green icon), Reject (red icon), Flag (orange icon)
- Bulk: Select all checkbox → action bar appears bottom

**Card View** (mobile):
- Event cards with admin actions overlay
- Swipe right: Approve
- Swipe left: Reject (opens reason modal)
- Long press: Multi-select

**Reject Modal**:
- Reason textarea (max 500 chars)
- "Also notify host" checkbox (default true)
- [Cancel] [Reject Event] destructive

### 4.13 Admin User Management (`/admin/users`)

**Table**:
- Columns: Avatar | Name | Email | Trust | Events | Joined | Actions
- Search: Debounced 300ms, searches name + email
- Filters: Trust level chips (All, New, Trusted, Flagged)
- Actions dropdown: Promote, Flag, Delete

**User Detail Drawer**:
- Slide from right, 400px
- Full profile + hosted events list + trust history
- Direct actions

### 4.14 Admin Category Management (`/admin/categories`)

**List**:
- Drag-and-drop sortable rows
- Each: Color swatch 24px | Icon | Name | Sort order | Actions
- Edit inline: Click to edit name, color picker, icon selector
- Create: "+ Add Category" button → inline form
- Delete: Confirmation if events use category

---

## 5. Search & Discovery System

### 5.1 Architecture Decision

**Search is NOT a page. Search is a component system integrated into Feed and global navigation.**

**Why**:
- Reduces navigation complexity
- Maintains context (user stays in feed)
- Modern pattern (Command Palette, Spotlight)
- Faster UX (no page load)

### 5.2 Command Palette (Global Search)

**Trigger**: `Cmd/Ctrl + K` or click search bar in header
**Placement**: Centered overlay (desktop), full-screen (mobile)

**States**:

**Empty (No Query)**:
```
┌─────────────────────────────────────────────┐
│                                             │
│   ┌─────────────────────────────────────┐    │
│   │ 🔍 Search anything...              │    │
│   │                                     │    │
│   │ Recent Searches                     │    │
│   │ → Yoga im Park                      │    │
│   │ → Tech Meetup                       │    │
│   │ → Sports category                   │    │
│   │                                     │    │
│   │ Popular Right Now                   │    │
│   │ → Campus Tech Meetup                │    │
│   │ → International Dinner              │    │
│   └─────────────────────────────────────┘    │
│                                             │
│   ↑↓ Navigate  ↵ Select  ␛ Close            │
└─────────────────────────────────────────────┘
```

**With Query**:
```
│   🔍 yoga                                   │
│                                             │
│   EVENTS                    (12 results)    │
│   🎯 Yoga im Park          Sat 18:00        │
│   🎯 Yoga for Beginners    Mon 10:00        │
│   🎯 Morning Yoga          Tue 07:00        │
│                                             │
│   CATEGORIES                                │
│   🏷️ Sports                                           │
│   🏷️ Wellness                                         │
│                                             │
│   PEOPLE                                    │
│   👤 Sarah (Yoga Instructor)                │
│                                             │
│   LOCATIONS                                 │
│   📍 Campuswiese                            │
│                                             │
│   [See all events for "yoga" →]             │
```

**Behavior**:
- Debounced 200ms API call: `GET /api/search/suggestions?q={query}&type=ALL`
- Max 5 results per type
- Keyboard: ↑↓ navigate, ↵ select, ␛ close
- Recent searches: Stored localStorage, max 5, shown before typing
- Click result: Navigate directly, close palette
- "See all": Closes palette, updates feed with `?q=yoga`, shows results inline

### 5.3 Feed Search Integration

**Search Bar in Feed**:
```
┌─────────────────────────────────────────┐
│ ┌─────────────────────────────────────┐ │
│ │ 🔍 Search events, people...    ⌘K  │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
```

**Active Search State**:
- User types → Command palette opens
- Selects result → Feed filters to that result OR navigates to detail
- "See all results for X" → Feed shows search results grid
- Active filter chip: "Search: yoga ✕"
- Clear search → Return to default feed

**Filter Integration**:
- Search + Category: "Search: yoga + Category: Sports"
- Search + Date: "Search: yoga + This week"
- Active filters shown as removable chips below search

### 5.4 Search Results Display

**When feed is in "search mode"**:
- Header: "12 results for 'yoga'"
- Sort options: Relevance (default), Date, Popularity
- Filter chips: Category, Date range, Location
- Grid: Same event cards as normal feed
- Empty: "No events match 'yoga'. Try different keywords."

**URL Sync**:
- `?q=yoga&category=1&dateFrom=...`
- Shareable search URLs
- Back button returns to unfiltered feed

---

## 6. Real-Time & SSE Integration

### 6.1 SSE Connection Manager

```typescript
class SseManager {
  private connections = new Map<string, EventSource>();
  private retryCounts = new Map<string, number>();
  private maxRetries = 3;
  private baseDelay = 5000;

  connect(eventId: string) {
    if (this.connections.has(eventId)) return;

    const es = new EventSource(
      `${API_BASE}/api/events/stream/${eventId}`,
      { withCredentials: true }
    );

    es.addEventListener('connected', (e) => {
      this.retryCounts.set(eventId, 0);
      // Show "Live updates active" indicator
    });

    es.addEventListener('rsvp-update', (e) => {
      const data = JSON.parse(e.data);
      this.handleRsvpUpdate(eventId, data);
    });

    es.addEventListener('waitlist-update', (e) => {
      const data = JSON.parse(e.data);
      this.handleWaitlistUpdate(eventId, data);
    });

    es.addEventListener('event-cancelled', (e) => {
      const data = JSON.parse(e.data);
      this.handleEventCancelled(eventId, data);
    });

    es.onerror = () => {
      es.close();
      const retries = this.retryCounts.get(eventId) || 0;

      if (retries < this.maxRetries) {
        const delay = Math.min(this.baseDelay * Math.pow(2, retries), 30000);
        setTimeout(() => this.connect(eventId), delay);
        this.retryCounts.set(eventId, retries + 1);
      } else {
        // Show "Live updates offline" banner
      }
    };

    this.connections.set(eventId, es);
  }

  disconnect(eventId: string) {
    const es = this.connections.get(eventId);
    if (es) {
      es.close();
      this.connections.delete(eventId);
    }
  }
}
```

### 6.2 Event Handlers

**rsvp-update**:
```json
{ "eventId": "uuid", "currentCount": 45, "maxCapacity": 60, 
  "spotsRemaining": 15, "isFull": false }
```
- UI: Capacity bar animates to new width (600ms)
- Counter: Number counts up/down with 300ms transition
- "X remaining" text: Pulses primary-600 color once
- If spotsRemaining went from 1→0: "Just filled!" micro-toast
- If user was waitlisted and spot opened: Flash border primary-300

**waitlist-update**:
```json
{ "eventId": "uuid", "waitlistCount": 3, "type": "PROMOTED" }
```
- If current user promoted:
  - Full-screen banner: "🎉 You've been promoted! You are now GOING."
  - Confetti animation (canvas, 3s, auto-cleanup)
  - Haptic: Heavy impact + success notification pattern
  - Toast: "You're in! Promoted from waitlist."
  - Update RSVP button: Morph to "Going ✓"
- If others promoted: Update waitlist count silently

**event-cancelled**:
```json
{ "eventId": "uuid", "reason": "Host unavailable" }
```
- Modal overlay: Cannot be dismissed by clicking outside
- Title: "Event Cancelled" with alert icon
- Message: "The host has cancelled this event."
- If reason provided: "Reason: Host unavailable"
- [Back to Feed] primary button
- Auto-redirect to feed after 5s countdown
- If user had RSVP: "Your registration has been cancelled."

### 6.3 Live Indicator UI

```
┌─────────────────────────────────────────┐
│ ← Back    🟢 Live    [Share]            │
├─────────────────────────────────────────┤
```

**States**:
- **Connected**: Green dot, pulse animation 2s infinite, "Live" text
- **Reconnecting**: Yellow dot, spinner, "Reconnecting..."
- **Offline**: Gray dot, "Updates paused", [Reconnect] button
- **Error**: Red dot, "Live updates error", [Retry] button

**Pulse Animation**:
```css
@keyframes pulse-ring {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}
```

---

## 7. Motion & Animation System

### 7.1 Animation Tokens

```css
:root {
  --ease-linear: linear;
  --ease-in: cubic-bezier(0.4, 0, 1, 1);
  --ease-out: cubic-bezier(0, 0, 0.2, 1);
  --ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
  --ease-bounce: cubic-bezier(0.68, -0.55, 0.265, 1.55);
  --ease-spring: cubic-bezier(0.175, 0.885, 0.32, 1.275);

  --duration-instant: 100ms;
  --duration-fast: 150ms;
  --duration-normal: 200ms;
  --duration-slow: 300ms;
  --duration-slower: 500ms;
  --duration-slowest: 800ms;
}
```

### 7.2 Page Transitions

**Route Change**:
- Exit: opacity 1→0, translateY(0→-8px), 150ms ease-in
- Enter: opacity 0→1, translateY(8px→0), 200ms ease-out
- Stagger: Child elements fade in 50ms apart

**Modal/Sheet**:
- Backdrop: opacity 0→1, 200ms
- Container: scale 0.96→1.0 + opacity, 250ms ease-out
- Bottom sheet: translateY(100%→0), 300ms spring

### 7.3 Micro-interactions

**Buttons**:
- Hover: translateY(-1px), shadow elevation +1, 150ms
- Active/Press: scale(0.97), 100ms
- Loading: Spinner rotation 1s linear infinite, text fades to 0 opacity

**Cards**:
- Hover: translateY(-3px), shadow-3, 200ms ease-out
- Press: scale(0.98), 100ms
- Like/Heart: scale 1→1.3→1.0, 400ms bounce
- Bookmark: Star fill animation, 300ms

**Inputs**:
- Focus: Border color transition 200ms, ring appears 150ms
- Valid: Checkmark icon scale 0→1, 200ms spring
- Invalid: Shake translateX(0→-8px→8px→-4px→4px→0), 400ms

**Badges & Counters**:
- New notification: Scale 0→1.2→1.0, 400ms bounce
- Number change: Old number slides up out, new slides in from below, 300ms

**Progress Bars**:
- Width transition: 600ms ease-in-out
- Color transition: 300ms when threshold crossed
- Completion: Flash success color, 200ms

### 7.4 Scroll-Driven Animations

**Header**:
- Scroll down > 50px: Header gains bg + shadow, 200ms
- Scroll up: Header returns to transparent (if at top)

**Parallax**:
- Event detail hero: Image scrolls at 0.5x speed (subtle)

**Reveal**:
- Cards below fold: opacity 0→1 + translateY(20px→0) as they enter viewport
- Trigger: IntersectionObserver at 100px before visible
- Stagger: 50ms between cards

### 7.5 Celebration Animations

**Waitlist Promotion**:
- Confetti: Canvas overlay, 150 particles, 3s, primary/success/warning colors
- Banner: Slide down from top, success-50 bg, stays 5s or until dismissed
- Button: Morph from "Waitlist" to "Going ✓" with check draw SVG animation

**Trust Promotion**:
- Trophy icon bounce
- "You're now a Trusted Host!" modal with confetti
- Profile badge: Glow animation 3s

**Event Published**:
- "Published!" toast with rocket icon
- Card appears in feed with highlight border, fades to normal over 3s

---

## 8. Accessibility & Inclusive Design

### 8.1 WCAG 2.1 AA Compliance

**Color Contrast**:
- All text on colored backgrounds: Minimum 4.5:1
- Large text (18px+ bold): Minimum 3:1
- Primary button: White on Primary-600 = 4.6:1 ✓
- Error text: Error-600 on white = 5.8:1 ✓
- Success text: Success-600 on Success-50 = 5.2:1 ✓

**Focus Indicators**:
- All interactive elements: 2px solid Primary-500, offset 2px
- Modals: Focus trap with Tab cycling
- Skip link: "Skip to main content", visible on focus, first tabbable element

### 8.2 Screen Reader Support

**Event Card**:
```html
<article aria-label="Event: Yoga im Park, Saturday at 18:00">
  <h2>Yoga im Park</h2>
  <p>
    <span aria-label="Host">Sarah Müller</span>,
    <span aria-label="Rating">4.5 stars, 12 reviews</span>,
    <span aria-label="Trust level">Trusted Host</span>
  </p>
  <p>
    <time datetime="2026-05-22T18:00:00Z">
      Saturday, May 22nd at 18:00
    </time>
  </p>
  <p aria-label="Capacity">12 of 20 spots remaining</p>
  <button aria-label="Register for Yoga im Park">
    Join
  </button>
</article>
```

**RSVP Button States**:
- "Join" → `aria-label="Register for Event Title. 15 spots remaining."`
- "Going" → `aria-label="You are registered for Event Title. Click to cancel registration."`
- "Waitlist #3" → `aria-label="You are number 3 on the waitlist for Event Title."`
- "Full" → `aria-label="Event Title is full. 5 people on waitlist."`

**Live Regions**:
- Toast container: `aria-live="polite"`, `aria-atomic="true"`
- SSE updates: `aria-live="polite"` for capacity changes
- Urgent (event cancelled): `aria-live="assertive"`
- Notification badge: `aria-label="3 unread notifications"`

### 8.3 Keyboard Navigation

**Global Shortcuts**:
- `Tab`: Navigate forward
- `Shift+Tab`: Navigate backward
- `Enter` / `Space`: Activate
- `Escape`: Close modal/dropdown/sheet
- `Cmd/Ctrl + K`: Open search
- `/`: Focus search (alternative)
- `?`: Open keyboard shortcuts help modal

**Feed**:
- `j` / `k`: Next/previous event card (vim-style)
- `Enter`: Open focused event
- `r`: Register for focused event (if available)

**Modal**:
- `Tab`: Cycle focus within modal
- `Shift+Tab`: Reverse cycle
- `Escape`: Close (if not critical)

### 8.4 Motion Preferences

```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }

  .skeleton {
    animation: none !important;
    opacity: 0.6;
  }

  .toast {
    animation: none !important;
  }
}
```

**Respects**:
- No auto-scrolling carousels (pause or manual)
- No parallax
- Instant page transitions
- Static skeletons

### 8.5 Touch Targets

- Minimum touch target: 44×44px (iOS) / 48×48px (Material)
- Buttons: Min-height 44px, padding 12px 20px
- Icon buttons: 44×44px container
- Form inputs: Min-height 48px
- List items: Min-height 56px
- Spacing between touch targets: Minimum 8px

---

## 9. Performance & Optimization

### 9.1 Performance Budget

| Metric | Target | Maximum | Measurement |
|--------|--------|---------|-------------|
| First Contentful Paint (FCP) | < 1.0s | 1.5s | Lighthouse |
| Largest Contentful Paint (LCP) | < 1.8s | 2.5s | Lighthouse |
| Time to Interactive (TTI) | < 2.5s | 4.0s | Lighthouse |
| Cumulative Layout Shift (CLS) | < 0.05 | 0.1 | Lighthouse |
| First Input Delay (FID) | < 50ms | 100ms | RUM |
| Total Blocking Time (TBT) | < 150ms | 300ms | Lighthouse |

### 9.2 Asset Budgets

| Asset | Budget | Strategy |
|-------|--------|----------|
| JS (initial) | 180 KB gzipped | Route splitting, tree shaking |
| JS (per route lazy) | 40 KB gzipped | Dynamic imports |
| CSS (critical) | 20 KB gzipped | Inline critical CSS |
| CSS (async) | 15 KB gzipped | Load non-critical async |
| Fonts | 35 KB | Inter variable woff2 |
| Images (card thumbnail) | 40 KB | WebP, quality 80 |
| Images (medium) | 120 KB | WebP, quality 80 |
| Images (full) | 300 KB | Lazy load |
| Total initial | < 250 KB | — |

### 9.3 Image Optimization Strategy

**Format Pipeline**:
1. Upload: User uploads JPG/PNG/WEBP
2. Backend: Generates WebP variants at 400w, 800w, 1200w
3. Frontend: Uses `srcset` with sizes attribute
4. Fallback: JPEG for old browsers

**Loading Strategy**:
- Above fold: Eager load, high priority
- Below fold: `loading="lazy"`, IntersectionObserver fallback
- Carousel: Preload next 2 images
- Thumbnails: Blurhash placeholder → low-res 40px → full

**Blurhash**:
- Decode 20px wide on client
- Display blurred 8px while loading
- Cross-fade to full image over 400ms

### 9.4 Code Optimization

**Splitting**:
- Route-based: Each major route is a separate chunk
- Component-based: Heavy components (carousel, map, charts) lazy loaded
- Library-based: Load TanStack Query, date-fns, etc. on first need

**Tree Shaking**:
- Import specific icons: `import { Star } from 'lucide-react'` not `import * as Icons`
- Import specific date-fns functions
- Use ESM builds exclusively

**Caching**:
- TanStack Query: Stale-while-revalidate
- Images: `Cache-Control: public, max-age=31536000, immutable` (with hash in URL)
- API responses: `Cache-Control: private, max-age=300` for events

### 9.5 Network Strategy

**Retry Logic**:
- Failed GET: Retry 2x, exponential backoff 1s, 2s
- Failed mutation: Toast error + [Retry] button
- Offline: Queue mutations, replay on reconnect

**Prefetching**:
- On hover over event card (desktop): Prefetch event detail query
- On visible (mobile): Prefetch next page of infinite scroll
- Route prefetch: On link hover, prefetch route chunk

**Compression**:
- Brotli for static assets
- Gzip fallback
- Minify JSON responses (backend)

---

## 10. Error Handling & Resilience

### 10.1 HTTP Status to UI Mapping

| Status | User-Facing Message | UI Pattern | Auto-Recovery |
|--------|---------------------|------------|---------------|
| 400 | "Please check your input." | Inline field errors + focus first | Manual fix |
| 401 | "Session expired. Please log in." | Toast + redirect to login | Auto-refresh token once, then login |
| 403 Disabled | "Account not verified. Check your email." | Modal with resend button | Resend verification |
| 403 Flagged | "Account suspended. Contact support." | Full-screen error + logout | Contact support link |
| 403 Forbidden | "You don't have permission." | Toast + navigate back | — |
| 404 | "Page not found." | Centered illustration + back | — |
| 409 | "This action isn't possible right now." | Toast or inline | Refresh data |
| 413 | "File too large (max X MB)." | Inline upload error | Choose smaller file |
| 429 | "Too many requests. Try again in Xs." | Toast with countdown timer | Wait for timer |
| 500 | "Something went wrong on our end." | Toast + [Retry] + report | Retry button |
| Network | "Connection lost." | Sticky banner + queue | Auto-retry when online |
| Timeout | "Request timed out." | Toast + [Retry] | Retry button |

### 10.2 Error Boundary Pages

**Global Error Boundary**:
- Catches React rendering errors
- UI: "Something went wrong" + error ID + [Reload] + [Report]
- Logs to monitoring service (Sentry/etc.)

**Route Error Boundaries**:
- Per-route errors (e.g., event not found)
- UI: Contextual message + [Go Back] + [Browse Events]

**Partial Error**:
- If one section fails (e.g., reviews fail to load):
  - Show inline error: "Couldn't load reviews. [Retry]"
  - Rest of page remains functional

### 10.3 Loading State Patterns

| Context | Pattern | Duration Handling |
|---------|---------|---------------------|
| Page initial | Skeleton screen matching layout | Until data + 200ms minimum (avoid flash) |
| Button action | Spinner in button, disabled state | Until response |
| Image upload | Progress bar (0-100%) + thumbnail preview | Until complete |
| Form submit | Button disabled + spinner + field lock | Until response |
| Infinite scroll | Skeleton row (2 items) at bottom | Until next page |
| Refresh | Pull-to-refresh spinner | Until revalidation |
| Navigation | Page transition fade | 200ms |
| Search | Skeleton results | Until API response |

**Minimum Loading Time**:
- For actions < 300ms: Show loading state for minimum 300ms to avoid jarring flash
- For skeletons: Show for minimum 500ms to avoid flicker

### 10.4 Rate Limit UX

**Trigger**: 429 response
**UI**:
- Toast: "Too many requests. Please slow down."
- Countdown: "Try again in 45 seconds"
- Button state: Disabled with countdown overlay
- Visual: Button opacity 0.6, spinner replaced by clock icon

**Auth Endpoints** (5/min):
- Show remaining attempts: "4 of 5 attempts remaining"
- Lockout: "Too many attempts. Try again in 12 minutes."

**Write Endpoints** (20/min):
- Subtle indicator: "20 requests per minute"
- Approaching limit: Warning color

### 10.5 Offline Handling

**Detection**: `navigator.onLine` + heartbeat fetch
**Banner**: "You're offline. Changes will sync when connected." Sticky top, warning-50, slide down 300ms

**Mutations**:
- Queue in IndexedDB with timestamp
- Show "Will send when online" micro-toast
- Visual indicator: Offline queue badge on sync icon
- On reconnect: Process queue in order, show "Synced X changes" toast

**Reads**:
- Serve from TanStack Query cache (stale-while-revalidate)
- Show "Last updated 2h ago" if stale
- Background revalidate when online

---

## 11. Complete API-to-UI Contract Map

### 11.1 Authentication Module

| Endpoint | Method | UI Location | Request | Response | UI Action |
|----------|--------|-------------|---------|----------|-----------|
| `/api/auth/register` | POST | Register | `RegisterRequest` | `AuthResponse` | Redirect `/verify-email-sent` |
| `/api/auth/login` | POST | Login | `LoginRequest` | `AuthResponse` | Store tokens, role-based redirect |
| `/api/auth/refresh` | POST | Interceptor | `X-Refresh-Token` | `AuthResponse` | Silent token refresh |
| `/api/auth/logout` | POST | Nav/Settings | — | Void | Clear store, redirect `/` |
| `/api/auth/verify` | GET | Verify | `?token=` | Void | Redirect `/login?verified=1` |
| `/api/auth/resend-verification` | POST | Verify modal | `ForgotPasswordRequest` | Void | Toast "Sent!" |
| `/api/auth/forgot-password` | POST | Forgot | `ForgotPasswordRequest` | Void | Generic success toast |
| `/api/auth/reset-password` | POST | Reset | `ResetPasswordRequest` | Void | Redirect `/login` |
| `/api/auth/me` | GET | Profile | — | `UserDto` | Render profile |
| `/api/auth/me` | PUT | Edit profile | `UpdateProfileRequest` | `UserDto` | Optimistic update |
| `/api/auth/me/password` | PUT | Settings | `ChangePasswordRequest` | Void | Toast + logout |
| `/api/auth/me` | DELETE | Delete account | — | Void | Clear all, redirect |
| `/api/auth/me/trust-status` | GET | Profile | — | `TrustQualificationStatus` | Render progress bars |
| `/api/auth/me/preferences` | GET | Settings | — | `UserPreferencesDto` | Render toggles |
| `/api/auth/me/preferences` | PUT | Settings | `UserPreferencesDto` | `UserPreferencesDto` | Toast "Saved" |

### 11.2 Events Module

| Endpoint | Method | UI | Request | Response | UI Action |
|----------|--------|-----|---------|----------|-----------|
| `/api/events` | POST | Create wizard | `CreateEventRequest` | `EventDto` | Redirect detail |
| `/api/events/draft` | POST | Auto-save | `CreateEventRequest` | `EventDto` | Micro-toast |
| `/api/events` | GET | Feed | Query | `Page<EventDto>` | Render grid |
| `/api/events/my-events` | GET | My Events | Query | `Page<EventDto>` | Render tabs |
| `/api/events/{id}` | GET | Detail | — | `EventDto` | Render page |
| `/api/events/by-slug/{slug}` | GET | Detail | — | `EventDto` | Render page |
| `/api/events/{id}` | PUT | Edit | `CreateEventRequest` | `EventDto` | Toast + update |
| `/api/events/{id}` | DELETE | My Events | — | Void | Move to trash |
| `/api/events/{id}/permanent` | DELETE | Trash | — | Void | Remove |
| `/api/events/{id}/cancel` | PATCH | Detail | `CancelEventRequest` | `EventDto` | Modal + redirect |
| `/api/events/{id}/restore` | PATCH | Trash | — | `EventDto` | Move to active |
| `/api/events/{id}/publish` | PUT | Draft | — | `EventDto` | Toast + redirect |
| `/api/events/{id}/media` | POST | Media step | multipart | `EventDto` | Update grid |
| `/api/events/{id}/media/{mid}` | DELETE | Media | — | `EventDto` | Remove |
| `/api/events/{id}/media/reorder` | PATCH | Media | `string[]` | `EventDto` | Update order |
| `/api/events/{id}/check-in-code` | GET | Host panel | — | `CheckInCodeDto` | Show QR + code |
| `/api/public/events` | GET | Guest feed | Query | `Page<EventDto>` | Render grid |
| `/api/public/events/featured` | GET | Landing | — | `Page<EventDto>` | Hero carousel |
| `/api/public/events/{id}` | GET | Guest detail | — | `EventDto` | Render read-only |
| `/api/public/events/slug/{slug}` | GET | Guest detail | — | `EventDto` | Render read-only |
| `/api/public/categories` | GET | Filter bar | — | `CategoryDto[]` | Render chips |

### 11.3 RSVP Module

| Endpoint | Method | UI | Response | UI Action |
|----------|--------|-----|----------|-----------|
| `/api/events/{id}/rsvps` | POST | Detail | `RsvpDto` | Optimistic + toast |
| `/api/rsvps/{id}/cancel` | PATCH | Detail/RSVPs | `RsvpDto` | Modal + update |
| `/api/rsvps/me` | GET | My RSVPs | `Page<RsvpDto>` | Render tabs |
| `/api/rsvps/{id}/position` | GET | Waitlist | `number` | Show position |
| `/api/events/{id}/rsvps` | GET | Host | `Page<RsvpDto>` | Render table |
| `/api/events/{id}/rsvps/{rid}/attended` | PATCH | Host | `RsvpDto` | Toggle |
| `/api/events/{id}/rsvps/{rid}/promote` | PATCH | Host | `RsvpDto` | Move to going |
| `/api/events/{id}/check-in` | POST | QR | `RsvpDto` | Success anim |

### 11.4 Reviews Module

| Endpoint | Method | UI | Request | Response | UI Action |
|----------|--------|-----|---------|----------|-----------|
| `/api/reviews` | POST | Review form | `CreateReviewRequest` | `ReviewDto` | Toast + append |
| `/api/reviews/event/{id}` | GET | Reviews tab | — | `Page<ReviewDto>` | Render list |
| `/api/reviews/host/{id}` | GET | Public profile | — | `Page<ReviewDto>` | Render list |
| `/api/reviews/{id}/helpful` | POST | Review card | — | `ReviewDto` | Toggle |
| `/api/reviews/{id}/report` | POST | Review card | `ReviewReportRequest` | Void | Modal + toast |
| `/api/reviews/{id}` | DELETE | Own review | — | Void | Remove |

### 11.5 Reports Module

| Endpoint | Method | UI | Request | Response | UI Action |
|----------|--------|-----|---------|----------|-----------|
| `/api/reports` | POST | Event detail | `CreateReportRequest` | `ReportDto` | Modal + toast |
| `/api/admin/reports` | GET | Admin | — | `Page<ReportDto>` | Render table |
| `/api/admin/reports/{id}` | GET | Admin | — | `ReportDto` | Render modal |
| `/api/admin/reports/{id}/resolve` | PATCH | Admin | `?flagEvent=` | `ReportDto` | Update row |
| `/api/admin/reports/{id}` | DELETE | Admin | — | Void | Remove |

### 11.6 Notifications Module

| Endpoint | Method | UI | Response | UI Action |
|----------|--------|-----|----------|-----------|
| `/api/notifications` | GET | Center | `Page<NotificationDto>` | Render list |
| `/api/notifications/unread-count` | GET | Badge | `number` | Update badge |
| `/api/notifications/{id}/read` | PATCH | Click | Void | Mark read |
| `/api/notifications/read-all` | PATCH | Mark all | `number` | Clear badges |
| `/api/notifications/{id}` | DELETE | Swipe | Void | Remove |

### 11.7 Admin Module

| Endpoint | Method | UI | Request | Response | UI Action |
|----------|--------|-----|---------|----------|-----------|
| `/api/admin/dashboard` | GET | Dashboard | — | `AdminDashboardDto` | Render stats |
| `/api/admin/events` | GET | Admin events | — | `Page<EventDto>` | Render table |
| `/api/admin/events/pending` | GET | Moderation | — | `Page<EventDto>` | Render queue |
| `/api/admin/events/{id}/approve` | PATCH | Moderation | — | `EventDto` | Remove + toast |
| `/api/admin/events/{id}/reject` | PATCH | Moderation | — | Void | Remove + toast |
| `/api/admin/events/{id}/flag` | PATCH | Moderation | — | `EventDto` | Update |
| `/api/admin/events/bulk-approve` | POST | Bulk | `BulkEventActionRequest` | `BulkEventActionResult` | Batch update |
| `/api/admin/events/bulk-reject` | POST | Bulk | `BulkEventActionRequest` | `BulkEventActionResult` | Batch update |
| `/api/admin/users` | GET | User mgmt | — | `Page<UserDto>` | Render table |
| `/api/admin/users/{id}/trust-level` | PATCH | User mgmt | — | Void | Update badge |
| `/api/admin/users/{id}/flag` | POST | User mgmt | — | Void | Update badge |
| `/api/admin/users/{id}/promote` | POST | User mgmt | `?force=` | Void | Update badge |
| `/api/admin/users/{id}` | DELETE | User mgmt | — | Void | Remove |
| `/api/admin/categories` | GET | Categories | — | `CategoryDto[]` | Render list |
| `/api/admin/categories` | POST | Categories | `CategoryRequest` | `CategoryDto` | Append |
| `/api/admin/categories/{id}` | PUT | Categories | `CategoryRequest` | `CategoryDto` | Update |
| `/api/admin/categories/{id}` | DELETE | Categories | — | Void | Remove |

### 11.8 Search Module

| Endpoint | Method | UI | Query | Response | UI Action |
|----------|--------|-----|-------|----------|-----------|
| `/api/search/suggestions` | GET | Command Palette | `?q=&type=` | `SearchSuggestionDto[]` | Dropdown groups |
| `/api/events` | GET | Feed results | `?q=` | `Page<EventDto>` | Results grid |

### 11.9 SSE Endpoint

| Endpoint | Method | UI | Events | UI Reaction |
|----------|--------|-----|--------|-------------|
| `/api/events/stream/{id}` | GET | Event detail | `rsvp-update` | Animate capacity |
| | | | `waitlist-update` | Promotion banner |
| | | | `event-cancelled` | Modal + redirect |

---

## 12. Appendices

### Appendix A: TypeScript Types (Complete)

```typescript
// ── Enums ──
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

// ── Response Wrappers ──
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string;
}

interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// ── Auth DTOs ──
interface RegisterRequest {
  universityEmail: string;
  password: string;
  displayName: string;
}

interface LoginRequest {
  universityEmail: string;
  password: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: UserDto;
}

interface UserDto {
  id: string;
  universityEmail: string;
  displayName: string;
  bio: string | null;
  profileImageUrl: string | null;
  role: Role;
  trustLevel: TrustLevel;
  createdAt: string;
}

interface PublicProfileDto {
  id: string;
  displayName: string;
  bio: string | null;
  profileImageUrl: string | null;
  trustLevel: TrustLevel;
  createdAt: string;
  completedEventsWithReviews: number;
  averageHostRating: number;
}

interface UpdateProfileRequest {
  displayName?: string;
  bio?: string;
  profileImage?: File;
}

interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

interface TrustQualificationStatus {
  completedEventsWithReviews: number;
  minimumEventsRequired: number;
  averageRating: number;
  minimumRatingRequired: number;
  meetsEventCount: boolean;
  meetsRatingThreshold: boolean;
  qualifies: boolean;
}

interface UserPreferencesDto {
  emailNotifications: boolean;
  pushNotifications: boolean;
  notifyOnRsvpChange: boolean;
  notifyOnReview: boolean;
  timezone: string;
  language: string;
}

// ── Event DTOs ──
interface HostDto {
  id: string;
  displayName: string;
  profileImageUrl: string | null;
  trustLevel: TrustLevel;
  averageHostRating: number;
  totalHostReviews: number;
  completedEventsWithReviews: number;
}

interface CategoryDto {
  id: number;
  name: string;
  icon: string | null;
  color: string | null;
  sortOrder: number;
}

interface EventMediaDto {
  id: string;
  url: string;
  mediaType: MediaType;
  filename: string;
  thumbnailUrl: string | null;
  mediumUrl: string | null;
  displayOrder: number;
}

interface EventDto {
  id: string;
  host: HostDto;
  title: string;
  description: string | null;
  location: string;
  startTime: string;
  endTime: string;
  maxCapacity: number;
  currentRsvpCount: number;
  status: EventStatus;
  categories: CategoryDto[];
  media: EventMediaDto[];
  createdAt: string;
  isHost: boolean;
  myRsvpStatus: RsvpStatus | null;
  slug: string;
  viewCount: number;
  cancellationReason: string | null;
}

interface CreateEventRequest {
  title: string;
  description?: string;
  location: string;
  startTime: string;
  endTime: string;
  maxCapacity: number;
  categoryIds?: number[];
  slug?: string;
}

interface CancelEventRequest {
  reason?: string;
}

interface CheckInCodeDto {
  checkInCode: string;
  eventId: string;
  eventTitle: string;
  generatedAt: string;
  refreshIntervalSeconds: number;
}

// ── RSVP DTOs ──
interface RsvpDto {
  id: string;
  eventId: string;
  eventTitle: string;
  user: UserDto;
  status: RsvpStatus;
  createdAt: string;
}

interface CancelRsvpRequest {
  reason?: string;
}

interface CheckInRequest {
  code: string;
}

// ── Review DTOs ──
interface CreateReviewRequest {
  eventId: string;
  rating: number;
  comment?: string;
}

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

interface ReviewReportRequest {
  reason: string;
}

// ── Report DTOs ──
interface CreateReportRequest {
  eventId: string;
  reason: ReportReason;
  details?: string;
}

interface ReportDto {
  id: string;
  eventId: string;
  eventTitle: string;
  reporter: UserDto;
  reason: ReportReason;
  details: string;
  status: ReportStatus;
  createdAt: string;
}

// ── Notification DTOs ──
interface NotificationDto {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  relatedEventId: string | null;
  relatedUserId: string | null;
  actionUrl: string | null;
  isRead: boolean;
  createdAt: string;
}

// ── Search DTOs ──
interface SearchSuggestionDto {
  type: "EVENT" | "CATEGORY" | "USER" | "LOCATION";
  value: string;
  id: string;
  subtitle: string;
}

// ── Admin DTOs ──
interface AdminDashboardDto {
  pendingEventsCount: number;
  openReportsCount: number;
  totalUsersCount: number;
  newUsersToday: number;
  eventsThisWeek: number;
  recentReports: ReportDto[];
  recentPendingEvents: EventDto[];
}

interface BulkEventActionRequest {
  eventIds: string[];
  reason?: string;
}

interface BulkEventActionResult {
  processedCount: number;
  successCount: number;
  failedCount: number;
  succeededIds: string[];
  failedIds: string[];
  message: string;
}

interface CategoryRequest {
  name: string;
  icon?: string;
  color?: string;
  sortOrder?: number;
}
```

### Appendix B: Environment Variables

```bash
# Frontend .env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=MyStudyApp
VITE_DEFAULT_LANGUAGE=de
VITE_DEFAULT_TIMEZONE=Europe/Berlin

# Upload Limits (bytes)
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
VITE_SENTRY_DSN=                    # Optional error tracking
```

### Appendix C: Implementation Checklist

#### Phase 1: Foundation (Week 1-2)
- [ ] Design system tokens (CSS/Tailwind config)
- [ ] Axios client with interceptors (auth + refresh + retry)
- [ ] Zustand stores (auth, ui, notifications)
- [ ] TanStack Query setup (default options, error handling, devtools)
- [ ] Toast notification system (4 types, positioning, stacking)
- [ ] Modal system (alert, confirm, form, bottom-sheet, drawer)
- [ ] Skeleton component library (card, detail, list variants)
- [ ] Empty state components (10+ contexts)
- [ ] Loading states (button, page, inline)
- [ ] Command Palette component (Cmd+K, keyboard nav)
- [ ] Error boundary (global + route)

#### Phase 2: Auth & Public Pages (Week 3)
- [ ] Register page (step UX, validation, strength meter)
- [ ] Login page (error states, remember me)
- [ ] Email verification flow (loading/success/error states)
- [ ] Password reset flow (token validation)
- [ ] Guest landing page (hero, featured, categories, feed)
- [ ] Public event detail (read-only)
- [ ] Bottom navigation (mobile)
- [ ] Top navigation (desktop)

#### Phase 3: Authenticated Core (Week 4)
- [ ] Authenticated feed (search integration, category filters, sort)
- [ ] Event detail (RSVP, tabs, SSE connection)
- [ ] Create event wizard (4 steps, auto-save, draft)
- [ ] My events (tabs, trash, swipe actions)
- [ ] My RSVPs (tabs, waitlist position, check-in codes)
- [ ] Profile page (view + edit mode, avatar upload)
- [ ] Settings (preferences, appearance, account)

#### Phase 4: Social Features (Week 5)
- [ ] Review system (create, list, sort, helpful toggle)
- [ ] Report event modal
- [ ] Report review modal
- [ ] Public profile pages
- [ ] Notification center (dropdown + full page)
- [ ] Notification badge polling

#### Phase 5: Media & Search (Week 6)
- [ ] Media upload (drag-drop, progress, validation)
- [ ] Media management (reorder, delete, preview)
- [ ] Image optimization (blurhash, lazy load, srcset)
- [ ] Search integration (Command Palette, feed search, filters)
- [ ] Search suggestions API integration

#### Phase 6: Admin (Week 7)
- [ ] Admin dashboard (stats cards, recent items)
- [ ] Event moderation queue (table + card views)
- [ ] Bulk actions UI (select + action bar)
- [ ] Reports management (table, resolve, flag)
- [ ] User management (table, search, filter, actions)
- [ ] Category management (drag sort, CRUD, color picker)

#### Phase 7: Polish & Performance (Week 8)
- [ ] Dark mode (system preference + toggle)
- [ ] Accessibility audit (contrast, focus, screen reader, keyboard)
- [ ] Performance audit (Lighthouse 90+ all categories)
- [ ] Offline handling (mutation queue, service worker)
- [ ] PWA setup (manifest, icons, offline page)
- [ ] Rate limit UI (countdown toasts, disabled states)
- [ ] Animation refinement (reduced motion, spring physics)
- [ ] E2E tests (critical user flows)
- [ ] Error tracking integration (Sentry)

---

*This specification is 100% contract-driven. Every design decision maps to a backend API endpoint, DTO, validation rule, or business logic constraint. No arbitrary UI decisions exist — everything serves the contract.*
