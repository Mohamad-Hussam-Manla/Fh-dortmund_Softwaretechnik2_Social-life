package de.fhdortmund.mystudyapp.moderation.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.fhdortmund.mystudyapp.moderation.model.ReviewVote;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, UUID> {

    Optional<ReviewVote> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    long countByReviewId(UUID reviewId);

    @Modifying
    @Query("DELETE FROM ReviewVote rv WHERE rv.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") UUID reviewId);

    @Modifying
    @Query("DELETE FROM ReviewVote rv WHERE rv.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}