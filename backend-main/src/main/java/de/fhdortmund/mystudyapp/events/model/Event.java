package de.fhdortmund.mystudyapp.events.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import de.fhdortmund.mystudyapp.identity.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    /**
     * Cached RSVP count — avoids a COUNT(*) on the rsvps table on every feed render.
     * Must be incremented/decremented transactionally when an RSVP is created or cancelled.
     */
    @Column(name = "current_rsvp_count", nullable = false)
    @Builder.Default
    private Integer currentRsvpCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UNDER_REVIEW;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EventCategory> eventCategories = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventMedia> eventMedia = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    /* ==================== PHASE 2 ADDITIONS ==================== */

    /** Human-readable URL slug (auto-generated from title) */
    @Column(name = "slug", unique = true, length = 150)
    private String slug;

    /** Social proof counter — incremented on every GET /api/events/{id} */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    /** Soft delete timestamp — null = active, non-null = deleted */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** QR check-in code for host display (6-char alphanumeric) */
    @Column(name = "check_in_code", length = 10)
    private String checkInCode;

    /** Reason provided when host cancels the event */
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}