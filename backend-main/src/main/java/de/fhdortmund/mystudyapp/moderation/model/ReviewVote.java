package de.fhdortmund.mystudyapp.moderation.model;

import java.util.UUID;

import de.fhdortmund.mystudyapp.identity.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PHASE 3: Review Helpfulness Voting
 * 
 * Users can mark reviews as helpful. Each user can vote once per review.
 * The composite unique constraint (review_id, user_id) enforces this.
 * The helpfulCount on Review is denormalized for fast reads.
 */
@Entity
@Table(
    name = "review_votes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Always HELPFUL for now; extensible if we add DOWNVOTE later */
    @Column(name = "vote_type", nullable = false, length = 20)
    @Builder.Default
    private String voteType = "HELPFUL";
}