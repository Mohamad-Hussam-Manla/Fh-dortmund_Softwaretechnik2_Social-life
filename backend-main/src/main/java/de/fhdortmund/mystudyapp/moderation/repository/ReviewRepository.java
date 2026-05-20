package de.fhdortmund.mystudyapp.moderation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.fhdortmund.mystudyapp.moderation.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.event e WHERE e.host.id = :userId")
    Double calculateAverageRatingByHostId(@Param("userId") UUID userId);

    List<Review> findByEventId(UUID eventId);

    Page<Review> findByEventId(UUID eventId, Pageable pageable);

    List<Review> findByReviewerId(UUID reviewerId);

    Page<Review> findByReviewerId(UUID reviewerId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN r.event e WHERE e.host.id = :hostId")
    List<Review> findReviewsForHostEvents(@Param("hostId") UUID hostId);

    @Query(value = "SELECT r FROM Review r JOIN r.event e WHERE e.host.id = :hostId",
           countQuery = "SELECT COUNT(r) FROM Review r JOIN r.event e WHERE e.host.id = :hostId")
    Page<Review> findReviewsForHostEvents(@Param("hostId") UUID hostId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.event.id = :eventId")
    Double calculateAverageRatingByEventId(@Param("eventId") UUID eventId);

    long countByEventId(UUID eventId);

    boolean existsByEventIdAndReviewerId(UUID eventId, UUID reviewerId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r JOIN r.event e WHERE e.host.id = :hostId GROUP BY r.rating")
    List<Object[]> getRatingDistributionForHost(@Param("hostId") UUID hostId);

    /* ============================================================
       PHASE 0: Host aggregate for total review count
       ============================================================ */

    @Query("SELECT COUNT(r) FROM Review r JOIN r.event e WHERE e.host.id = :hostId")
    Long countTotalReviewsByHostId(@Param("hostId") UUID hostId);

    /* -------------------- Bulk Deletion (Phase 3) -------------------- */

    @Modifying
    @Query("DELETE FROM Review r WHERE r.event.id = :eventId")
    void deleteAllByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.reviewer.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    /* ==================== PHASE 3 ADDITIONS ==================== */

    /** Sort reviews by helpfulness (most helpful first), then by date */
    Page<Review> findByEventIdOrderByHelpfulCountDescCreatedAtDesc(UUID eventId, Pageable pageable);

    /** Increment denormalized helpful count */
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
    int incrementHelpfulCount(@Param("reviewId") UUID reviewId);

    /** Decrement denormalized helpful count */
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount - 1 WHERE r.id = :reviewId AND r.helpfulCount > 0")
    int decrementHelpfulCount(@Param("reviewId") UUID reviewId);
}