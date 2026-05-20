package de.fhdortmund.mystudyapp.moderation.service;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.common.exception.ForbiddenActionException;
import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.identity.service.TrustLevelService;
import de.fhdortmund.mystudyapp.moderation.dto.CreateReviewRequest;
import de.fhdortmund.mystudyapp.moderation.dto.ReviewDto;
import de.fhdortmund.mystudyapp.moderation.dto.ReviewReportRequest;
import de.fhdortmund.mystudyapp.moderation.mapper.ReviewMapper;
import de.fhdortmund.mystudyapp.moderation.model.Report;
import de.fhdortmund.mystudyapp.moderation.model.ReportReason;
import de.fhdortmund.mystudyapp.moderation.model.ReportStatus;
import de.fhdortmund.mystudyapp.moderation.model.Review;
import de.fhdortmund.mystudyapp.moderation.model.ReviewVote;
import de.fhdortmund.mystudyapp.moderation.repository.ReportRepository;
import de.fhdortmund.mystudyapp.moderation.repository.ReviewRepository;
import de.fhdortmund.mystudyapp.moderation.repository.ReviewVoteRepository;
import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import de.fhdortmund.mystudyapp.notification.publisher.NotificationEventPublisher;
import de.fhdortmund.mystudyapp.registration.model.Rsvp;
import de.fhdortmund.mystudyapp.registration.model.RsvpStatus;
import de.fhdortmund.mystudyapp.registration.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RsvpRepository rsvpRepository;
    private final ReviewMapper reviewMapper;
    private final TrustLevelService trustLevelService;

    // PHASE 3: Review voting
    private final ReviewVoteRepository reviewVoteRepository;

    // PHASE 3: Review reporting
    private final ReportRepository reportRepository;

    // PHASE 1.3: Notify hosts of new reviews
    private final NotificationEventPublisher notificationPublisher;

    /* ==================== CRUD ==================== */

    @Transactional
    public ReviewDto createReview(CreateReviewRequest request, String reviewerEmail) {
        User reviewer = userRepository.findByUniversityEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));

        if (event.getEndTime().isAfter(Instant.now())) {
            throw new ForbiddenActionException("review", "Cannot review an event that has not ended yet");
        }

        Rsvp rsvp = rsvpRepository.findByEventIdAndUserId(event.getId(), reviewer.getId())
                .orElseThrow(() -> new ForbiddenActionException("review", "You must RSVP to this event before reviewing"));

        if (rsvp.getStatus() != RsvpStatus.ATTENDED) {
            throw new ForbiddenActionException("review", "Only attendees marked as attended can leave a review");
        }

        if (reviewRepository.existsByEventIdAndReviewerId(event.getId(), reviewer.getId())) {
            throw new ForbiddenActionException("review", "You have already reviewed this event");
        }

        Review review = Review.builder()
                .event(event)
                .reviewer(reviewer)
                .rating(request.getRating())
                .comment(request.getComment())
                .helpfulCount(0)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created: {} for event {} by {}", saved.getId(), event.getId(), reviewerEmail);

        // PHASE 1.3: Notify host of new review
       notificationPublisher.publishEventNotification(
        event.getHost().getId(),
        NotificationType.NEW_REVIEW,
        "New Review",
        reviewer.getDisplayName() + " left a " + request.getRating() + "-star review on \"" + event.getTitle() + "\".",
        event.getId(),
        event.getTitle()
);
        try {
            trustLevelService.promoteToTrustedHost(event.getHost().getId());
            log.info("Host {} auto-promoted to TRUSTED_HOST after review", event.getHost().getId());
        } catch (ForbiddenActionException e) {
            log.debug("Host {} does not yet qualify for TRUSTED_HOST: {}", event.getHost().getId(), e.getMessage());
        }

        return reviewMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ReviewDto getReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        return reviewMapper.toDto(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDto> getReviewsByEventId(UUID eventId, Pageable pageable, String currentUserEmail) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }

        // PHASE 3: Sort by helpfulCount desc, then createdAt desc
        Page<Review> page = reviewRepository.findByEventIdOrderByHelpfulCountDescCreatedAtDesc(eventId, pageable);
        return buildPageResponse(page, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDto> getReviewsByHostId(UUID hostId, Pageable pageable, String currentUserEmail) {
        if (!userRepository.existsById(hostId)) {
            throw new ResourceNotFoundException("User", "id", hostId);
        }
        Page<Review> page = reviewRepository.findReviewsForHostEvents(hostId, pageable);
        return buildPageResponse(page, currentUserEmail);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDto> getReviewsByReviewerEmail(String reviewerEmail, Pageable pageable) {
        User reviewer = userRepository.findByUniversityEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<Review> page = reviewRepository.findByReviewerId(reviewer.getId(), pageable);
        return buildPageResponse(page, reviewerEmail);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDto> getReviewsByReviewerId(UUID reviewerId, Pageable pageable, String currentUserEmail) {
        if (!userRepository.existsById(reviewerId)) {
            throw new ResourceNotFoundException("User", "id", reviewerId);
        }
        Page<Review> page = reviewRepository.findByReviewerId(reviewerId, pageable);
        return buildPageResponse(page, currentUserEmail);
    }

    @Transactional
    public void deleteReview(UUID reviewId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        boolean isReviewer = review.getReviewer().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isReviewer && !isAdmin) {
            throw new ForbiddenActionException("delete review", "Only the reviewer or an admin can delete this review");
        }

        // PHASE 3: Clean up review votes before deleting
        reviewVoteRepository.deleteAllByReviewId(reviewId);

        reviewRepository.delete(review);
        log.info("Review deleted: {} by {}", reviewId, userEmail);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event", "id", eventId);
        }
        return reviewRepository.calculateAverageRatingByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForHost(UUID hostId) {
        if (!userRepository.existsById(hostId)) {
            throw new ResourceNotFoundException("User", "id", hostId);
        }
        return reviewRepository.calculateAverageRatingByHostId(hostId);
    }

    @Transactional(readOnly = true)
    public long getReviewCountForEvent(UUID eventId) {
        return reviewRepository.countByEventId(eventId);
    }

    /* ==================== PHASE 3: HELPFUL VOTE ==================== */

    /**
     * Toggle a helpful vote on a review.
     * If the user has already voted, removes the vote (unhelpful).
     * If not, adds a vote and increments the denormalized counter.
     */
    @Transactional
    public ReviewDto toggleHelpfulVote(UUID reviewId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Users cannot vote on their own reviews
        if (review.getReviewer().getId().equals(user.getId())) {
            throw new ForbiddenActionException("vote", "You cannot vote on your own review");
        }

        var existingVote = reviewVoteRepository.findByReviewIdAndUserId(reviewId, user.getId());

        if (existingVote.isPresent()) {
            // Remove vote (toggle off)
            reviewVoteRepository.delete(existingVote.get());
            reviewRepository.decrementHelpfulCount(reviewId);
            log.info("Helpful vote removed: review {} by {}", reviewId, userEmail);

            // Refresh and return
            Review refreshed = reviewRepository.findById(reviewId).orElseThrow();
            return reviewMapper.toDto(refreshed, false);
        } else {
            // Add vote (toggle on)
            ReviewVote vote = ReviewVote.builder()
                    .review(review)
                    .user(user)
                    .voteType("HELPFUL")
                    .build();
            reviewVoteRepository.save(vote);
            reviewRepository.incrementHelpfulCount(reviewId);
            log.info("Helpful vote added: review {} by {}", reviewId, userEmail);

            // Refresh and return
            Review refreshed = reviewRepository.findById(reviewId).orElseThrow();
            return reviewMapper.toDto(refreshed, true);
        }
    }

    /* ==================== PHASE 3: REVIEW REPORTING ==================== */

    /**
     * Report a review as inappropriate. Creates a Report entity
     * linked to the review's event with reason INAPPROPRIATE.
     */
    @Transactional
    public void reportReview(UUID reviewId, ReviewReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByUniversityEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // Prevent self-reporting
        if (review.getReviewer().getId().equals(reporter.getId())) {
            throw new ForbiddenActionException("report", "You cannot report your own review");
        }

        Report report = Report.builder()
                .event(review.getEvent())
                .reporter(reporter)
                .reason(ReportReason.INAPPROPRIATE)
                .details("Reported review: " + reviewId + ". Reason: " + request.getReason())
                .status(ReportStatus.OPEN)
                .build();

        reportRepository.save(report);
        log.info("Review {} reported by {}. Reason: {}", reviewId, reporterEmail, request.getReason());
    }

    /* ==================== Helpers ==================== */

    private PageResponse<ReviewDto> buildPageResponse(Page<Review> page, String currentUserEmail) {
        UUID currentUserId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            currentUserId = userRepository.findByUniversityEmail(currentUserEmail)
                    .map(User::getId).orElse(null);
        }

        final UUID userId = currentUserId;
        return PageResponse.<ReviewDto>builder()
                .content(page.getContent().stream()
                        .map(r -> {
                            boolean isHelpful = userId != null && 
                                    reviewVoteRepository.existsByReviewIdAndUserId(r.getId(), userId);
                            return reviewMapper.toDto(r, isHelpful);
                        })
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}