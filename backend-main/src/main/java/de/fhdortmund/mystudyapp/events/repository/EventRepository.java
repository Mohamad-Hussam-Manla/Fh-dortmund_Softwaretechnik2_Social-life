package de.fhdortmund.mystudyapp.events.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import jakarta.persistence.LockModeType;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /* -------------------- Atomic Capacity Updates -------------------- */

    @Modifying
    @Query("UPDATE Event e SET e.currentRsvpCount = e.currentRsvpCount + 1 " +
           "WHERE e.id = :eventId AND e.currentRsvpCount < e.maxCapacity AND e.status = 'PUBLISHED'")
    int incrementRsvpCount(@Param("eventId") UUID eventId);

    @Modifying
    @Query("UPDATE Event e SET e.currentRsvpCount = e.currentRsvpCount - 1 " +
           "WHERE e.id = :eventId AND e.currentRsvpCount > 0")
    int decrementRsvpCount(@Param("eventId") UUID eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :eventId")
    Optional<Event> findByIdLocked(@Param("eventId") UUID eventId);

    /* -------------------- Trust / Review Queries -------------------- */

    @Query("""
            SELECT COUNT(DISTINCT e.id)
            FROM Event e
            JOIN Review r ON r.event.id = e.id
            WHERE e.host.id = :userId
              AND e.endTime < CURRENT_TIMESTAMP
            """)
    Long countCompletedReviewedEventsByHostId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.host.id = :userId AND e.endTime < CURRENT_TIMESTAMP")
    Long countCompletedEventsByHostId(@Param("userId") UUID userId);

    @Query("""
            SELECT COUNT(e) > 0
            FROM Event e
            JOIN Review r ON r.event.id = e.id
            WHERE e.host.id = :userId
              AND e.endTime < CURRENT_TIMESTAMP
            """)
    Boolean hasCompletedReviewedEvents(@Param("userId") UUID userId);

    @Query("SELECT e.id FROM Event e WHERE e.host.id = :userId AND e.endTime < CURRENT_TIMESTAMP")
    List<UUID> findCompletedEventIdsByHostId(@Param("userId") UUID userId);

    /* -------------------- Host Queries -------------------- */

    List<Event> findByHostId(UUID hostId);

    List<Event> findByHostIdAndStatus(UUID hostId, EventStatus status);

    /* -------------------- Feed / Pagination Queries -------------------- */

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    Page<Event> findByHostId(UUID hostId, Pageable pageable);

    /**
     * Filtered feed query. All parameters are optional (NULL = ignored).
     * DISTINCT is required because of the JOIN with eventCategories.
     */
    @Query("""
            SELECT DISTINCT e FROM Event e
            LEFT JOIN e.eventCategories ec
            WHERE e.status = :status
              AND (:categoryId IS NULL OR ec.category.id = :categoryId)
              AND (:dateFrom IS NULL OR e.startTime >= :dateFrom)
              AND (:dateTo IS NULL OR e.endTime <= :dateTo)
              AND (:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:q IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Event> findPublishedWithFilters(
            @Param("status") EventStatus status,
            @Param("categoryId") Integer categoryId,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("location") String location,
            @Param("q") String q,
            Pageable pageable);

    /* -------------------- Lifecycle / Scheduled Queries -------------------- */

    List<Event> findByStatusAndEndTimeBefore(EventStatus status, Instant endTime);
}