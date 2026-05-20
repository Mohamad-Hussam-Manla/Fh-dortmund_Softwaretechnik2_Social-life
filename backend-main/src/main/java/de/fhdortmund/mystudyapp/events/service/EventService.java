package de.fhdortmund.mystudyapp.events.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import de.fhdortmund.mystudyapp.common.config.StorageProperties;
import de.fhdortmund.mystudyapp.common.exception.ForbiddenActionException;
import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.common.service.FileStorageService;
import de.fhdortmund.mystudyapp.common.service.ThumbnailService;
import de.fhdortmund.mystudyapp.events.dto.CreateEventRequest;
import de.fhdortmund.mystudyapp.events.dto.EventDto;
import de.fhdortmund.mystudyapp.events.factory.EventFactory;
import de.fhdortmund.mystudyapp.events.mapper.EventMapper;
import de.fhdortmund.mystudyapp.events.model.Category;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventCategory;
import de.fhdortmund.mystudyapp.events.model.EventCategoryId;
import de.fhdortmund.mystudyapp.events.model.EventMedia;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.model.MediaType;
import de.fhdortmund.mystudyapp.events.repository.CategoryRepository;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.moderation.repository.ReportRepository;
import de.fhdortmund.mystudyapp.moderation.repository.ReviewRepository;
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
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventFactory eventFactory;
    private final EventMapper eventMapper;
    private final FileStorageService fileStorageService;
    private final ThumbnailService thumbnailService;
    private final StorageProperties storageProperties;
    private final RsvpRepository rsvpRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    // PHASE 1.3: Notify attendees of event cancellation
    private final NotificationEventPublisher notificationPublisher;

    /* ==================== CRUD ==================== */

    @Transactional
    public EventDto createEvent(CreateEventRequest request, String hostEmail) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.getStartTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot create events in the past");
        }

        User host = userRepository.findByUniversityEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventFactory.createEvent(request, host);
        final Event savedEvent = eventRepository.save(event);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<EventCategory> categories = request.getCategoryIds().stream()
                    .map(catId -> {
                        Category category = categoryRepository.findById(catId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", catId));
                        return EventCategory.builder()
                                .id(new EventCategoryId(savedEvent.getId(), category.getId()))
                                .event(savedEvent)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toSet());
            savedEvent.setEventCategories(categories);
            eventRepository.save(savedEvent);
        }

        log.info("Event created: {} by {} with status {}", savedEvent.getId(), hostEmail, savedEvent.getStatus());
        return eventMapper.toDto(savedEvent, host.getId());
    }

    /**
     * PHASE 2: Create a draft event — partial validation, no date restrictions.
     */
    @Transactional
    public EventDto createDraft(CreateEventRequest request, String hostEmail) {
        User host = userRepository.findByUniversityEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event draft = eventFactory.createDraft(request, host);
        Event saved = eventRepository.save(draft);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<EventCategory> categories = request.getCategoryIds().stream()
                    .map(catId -> {
                        Category category = categoryRepository.findById(catId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", catId));
                        return EventCategory.builder()
                                .id(new EventCategoryId(saved.getId(), category.getId()))
                                .event(saved)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toSet());
            saved.setEventCategories(categories);
            eventRepository.save(saved);
        }

        log.info("Draft created: {} by {}", saved.getId(), hostEmail);
        return eventMapper.toDto(saved, host.getId());
    }

    /**
     * PHASE 2: Publish a draft event — validates and transitions to UNDER_REVIEW or PUBLISHED.
     */
    @Transactional
    public EventDto publishDraft(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("publish", "Only the host can publish this draft");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ForbiddenActionException("publish", "Only drafts can be published");
        }

        // Validate before publishing
        if (event.getEndTime().isBefore(event.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (event.getStartTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot publish events in the past");
        }

        EventStatus newStatus = (user.getTrustLevel() == de.fhdortmund.mystudyapp.identity.model.TrustLevel.TRUSTED_HOST
                || user.getRole() == de.fhdortmund.mystudyapp.identity.model.Role.ADMIN)
                ? EventStatus.PUBLISHED
                : EventStatus.UNDER_REVIEW;

        event.setStatus(newStatus);
        Event saved = eventRepository.save(event);

        log.info("Draft {} published by {} with status {}", eventId, userEmail, newStatus);
        return eventMapper.toDto(saved, user.getId());
    }

    @Transactional(readOnly = true)
    public EventDto getEvent(UUID eventId, String currentUserEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // PHASE 2: Soft delete check — only host or admin can view deleted events
        if (event.getDeletedAt() != null) {
            UUID currentUserId = null;
            if (currentUserEmail != null && !currentUserEmail.isBlank()) {
                currentUserId = userRepository.findByUniversityEmail(currentUserEmail)
                        .map(User::getId).orElse(null);
            }
            boolean isHost = currentUserId != null && event.getHost().getId().equals(currentUserId);
            boolean isAdmin = currentUserEmail != null && 
                    userRepository.findByUniversityEmail(currentUserEmail)
                            .map(u -> u.getRole() == de.fhdortmund.mystudyapp.identity.model.Role.ADMIN)
                            .orElse(false);
            if (!isHost && !isAdmin) {
                throw new ResourceNotFoundException("Event", "id", eventId);
            }
        }

        // PHASE 2: Increment view count for published events
        if (event.getStatus() == EventStatus.PUBLISHED && event.getDeletedAt() == null) {
            eventRepository.incrementViewCount(eventId);
        }

        UUID currentUserId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            currentUserId = userRepository.findByUniversityEmail(currentUserEmail)
                    .map(User::getId)
                    .orElse(null);
        }

        return eventMapper.toDto(event, currentUserId);
    }

    /**
     * PHASE 2: Get event by slug (public or authenticated).
     */
    @Transactional(readOnly = true)
    public EventDto getEventBySlug(String slug, String currentUserEmail) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "slug", slug));

        // Security: never expose non-published events publicly unless host/admin
        if (event.getStatus() != EventStatus.PUBLISHED || event.getDeletedAt() != null) {
            UUID currentUserId = null;
            if (currentUserEmail != null && !currentUserEmail.isBlank()) {
                currentUserId = userRepository.findByUniversityEmail(currentUserEmail)
                        .map(User::getId).orElse(null);
            }
            boolean isHost = currentUserId != null && event.getHost().getId().equals(currentUserId);
            boolean isAdmin = currentUserEmail != null &&
                    userRepository.findByUniversityEmail(currentUserEmail)
                            .map(u -> u.getRole() == de.fhdortmund.mystudyapp.identity.model.Role.ADMIN)
                            .orElse(false);
            if (!isHost && !isAdmin) {
                throw new ResourceNotFoundException("Event", "slug", slug);
            }
        }

        // Increment view count
        if (event.getStatus() == EventStatus.PUBLISHED && event.getDeletedAt() == null) {
            eventRepository.incrementViewCount(event.getId());
        }

        UUID currentUserId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            currentUserId = userRepository.findByUniversityEmail(currentUserEmail)
                    .map(User::getId)
                    .orElse(null);
        }

        return eventMapper.toDto(event, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventDto> getPublishedEvents(
            Integer categoryId,
            Instant dateFrom,
            Instant dateTo,
            String location,
            String q,
            Pageable pageable,
            String currentUserEmail) {

        UUID currentUserId = null;
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            currentUserId = userRepository.findByUniversityEmail(currentUserEmail)
                    .map(User::getId)
                    .orElse(null);
        }

        Page<Event> page = eventRepository.findPublishedWithFilters(
                EventStatus.PUBLISHED, categoryId, dateFrom, dateTo, location, q, pageable);

        return buildPageResponse(page, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventDto> getMyEvents(String email, Pageable pageable) {
        User user = userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<Event> page = eventRepository.findByHostIdAndDeletedAtIsNull(user.getId(), pageable);
        return buildPageResponse(page, user.getId());
    }

    /**
     * PHASE 2: Get my events including deleted (for trash bin).
     */
    @Transactional(readOnly = true)
    public PageResponse<EventDto> getMyEventsIncludingDeleted(String email, Pageable pageable) {
        User user = userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<Event> page = eventRepository.findByHostId(user.getId(), pageable);
        return buildPageResponse(page, user.getId());
    }

    @Transactional
    public EventDto updateEvent(UUID eventId, CreateEventRequest request, String userEmail) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.getStartTime().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot move events into the past");
        }

        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("update", "Only the host can update this event");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ForbiddenActionException("update", "Cannot update a cancelled event");
        }

        // PHASE 2: Cannot update soft-deleted events
        if (event.getDeletedAt() != null) {
            throw new ForbiddenActionException("update", "Cannot update a deleted event. Restore it first.");
        }

        if (request.getMaxCapacity() < event.getCurrentRsvpCount()) {
            throw new ForbiddenActionException("update",
                    "Cannot reduce capacity below current RSVP count (" + event.getCurrentRsvpCount() + ")");
        }

        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        event.setLocation(request.getLocation().trim());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setMaxCapacity(request.getMaxCapacity());

        // PHASE 2: Update slug if provided
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            event.setSlug(request.getSlug());
        }

        if (request.getCategoryIds() != null) {
            event.getEventCategories().clear();
            Set<EventCategory> categories = request.getCategoryIds().stream()
                    .map(catId -> {
                        Category category = categoryRepository.findById(catId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", catId));
                        return EventCategory.builder()
                                .id(new EventCategoryId(event.getId(), category.getId()))
                                .event(event)
                                .category(category)
                                .build();
                    })
                    .collect(Collectors.toSet());
            event.setEventCategories(categories);
        }

        Event updated = eventRepository.save(event);
        return eventMapper.toDto(updated, user.getId());
    }

    @Transactional
    public EventDto cancelEvent(UUID eventId, String userEmail, String reason) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("cancel", "Only the host can cancel this event");
        }

        event.setStatus(EventStatus.CANCELLED);
        event.setCancellationReason(reason);  // PHASE 2
        Event updated = eventRepository.save(event);

        // PHASE 1.3: Notify all GOING and WAITLISTED attendees
        List<Rsvp> affectedRsvps = rsvpRepository.findByEventId(eventId);
        for (Rsvp rsvp : affectedRsvps) {
            if (rsvp.getStatus() == RsvpStatus.GOING || rsvp.getStatus() == RsvpStatus.WAITLISTED) {
                String message = reason != null && !reason.isBlank()
                        ? "\"" + event.getTitle() + "\" has been cancelled by the host. Reason: " + reason
                        : "\"" + event.getTitle() + "\" has been cancelled by the host.";

                notificationPublisher.publishEventNotification(
                        rsvp.getUser().getId(),
                        NotificationType.EVENT_CANCELLED,
                        "Event Cancelled",
                        message,
                        event.getId(),
                        event.getTitle()
                );
            }
        }

        log.info("Event cancelled: {} by {}. Reason: {}. Notified {} attendees.",
                eventId, userEmail, reason, affectedRsvps.size());
        return eventMapper.toDto(updated, user.getId());
    }

    /**
     * PHASE 2: Soft delete an event (moves to trash bin).
     */
    @Transactional
    public void deleteEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isHost = event.getHost().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isHost && !isAdmin) {
            throw new ForbiddenActionException("delete", "Only the host or an admin can delete this event");
        }

        // PHASE 2: Soft delete — set deletedAt instead of hard delete
        event.setDeletedAt(Instant.now());
        eventRepository.save(event);

        log.info("Event soft-deleted: {} by {}", eventId, userEmail);
    }

    /**
     * PHASE 2: Hard delete (permanent) — only for already soft-deleted events or admin.
     */
    @Transactional
    public void permanentlyDeleteEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findByIdIncludingDeleted(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        boolean isHost = event.getHost().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isHost && !isAdmin) {
            throw new ForbiddenActionException("delete", "Only the host or an admin can delete this event");
        }

        // PHASE 2: Only allow permanent delete if already soft-deleted (safety) or admin
        if (event.getDeletedAt() == null && !isAdmin) {
            throw new ForbiddenActionException("delete", "Event must be soft-deleted first before permanent deletion");
        }

        if (event.getEventMedia() != null) {
            for (EventMedia media : event.getEventMedia()) {
                fileStorageService.deleteEventMedia(media.getUrl());
                // PHASE 2: Also delete thumbnails
                if (media.getThumbnailUrl() != null) {
                    fileStorageService.deleteEventMedia(media.getThumbnailUrl());
                }
                if (media.getMediumUrl() != null) {
                    fileStorageService.deleteEventMedia(media.getMediumUrl());
                }
            }
            event.getEventMedia().clear();
        }

        rsvpRepository.deleteAllByEventId(eventId);
        reviewRepository.deleteAllByEventId(eventId);
        reportRepository.deleteAllByEventId(eventId);

        eventRepository.delete(event);
        log.info("Event permanently deleted: {} by {}", eventId, userEmail);
    }

    /**
     * PHASE 2: Restore a soft-deleted event.
     */
    @Transactional
    public EventDto restoreEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findByIdIncludingDeleted(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new ForbiddenActionException("restore", "Only the host or an admin can restore this event");
        }

        if (event.getDeletedAt() == null) {
            throw new ForbiddenActionException("restore", "Event is not deleted");
        }

        event.setDeletedAt(null);
        // If event was past its end time, set to COMPLETED instead of restoring as PUBLISHED
        if (event.getEndTime().isBefore(Instant.now()) && event.getStatus() == EventStatus.PUBLISHED) {
            event.setStatus(EventStatus.COMPLETED);
        }

        Event restored = eventRepository.save(event);
        log.info("Event restored: {} by {}", eventId, userEmail);
        return eventMapper.toDto(restored, user.getId());
    }

    /* ==================== Media ==================== */

    @Transactional
    public EventDto addMedia(UUID eventId, List<MultipartFile> images, List<MultipartFile> videos, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("upload media", "Only the host can upload media");
        }

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ForbiddenActionException("upload media", "Cannot modify a cancelled event");
        }

        if (event.getDeletedAt() != null) {
            throw new ForbiddenActionException("upload media", "Cannot modify a deleted event");
        }

        long currentImages = event.getEventMedia().stream()
                .filter(m -> m.getMediaType() == MediaType.IMAGE).count();
        long currentVideos = event.getEventMedia().stream()
                .filter(m -> m.getMediaType() == MediaType.VIDEO).count();

        if (images != null && !images.isEmpty()) {
            if (currentImages + images.size() > storageProperties.getMaxImagesPerEvent()) {
                throw new IllegalArgumentException(
                        "Maximum " + storageProperties.getMaxImagesPerEvent() + " images allowed per event");
            }
            for (MultipartFile img : images) {
                if (img != null && !img.isEmpty()) {
                    String url = fileStorageService.storeEventImage(img, eventId);

                    // PHASE 2: Generate thumbnails
                    String[] thumbs = thumbnailService.generateThumbnails(
                            java.nio.file.Paths.get(
                                    storageProperties.getEventMediaLocation(),
                                    "events", eventId.toString(), "images",
                                    url.substring(url.lastIndexOf('/') + 1)
                            ),
                            url
                    );

                    event.getEventMedia().add(EventMedia.builder()
                            .event(event)
                            .url(url)
                            .thumbnailUrl(thumbs[0])
                            .mediumUrl(thumbs[1])
                            .mediaType(MediaType.IMAGE)
                            .filename(img.getOriginalFilename())
                            .displayOrder((int) currentImages)  // PHASE 2: auto-increment order
                            .build());
                }
            }
        }

        if (videos != null && !videos.isEmpty()) {
            if (currentVideos + videos.size() > storageProperties.getMaxVideosPerEvent()) {
                throw new IllegalArgumentException(
                        "Maximum " + storageProperties.getMaxVideosPerEvent() + " videos allowed per event");
            }
            for (MultipartFile vid : videos) {
                if (vid != null && !vid.isEmpty()) {
                    String url = fileStorageService.storeEventVideo(vid, eventId);
                    event.getEventMedia().add(EventMedia.builder()
                            .event(event)
                            .url(url)
                            .mediaType(MediaType.VIDEO)
                            .filename(vid.getOriginalFilename())
                            .displayOrder((int) currentVideos)
                            .build());
                }
            }
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved, user.getId());
    }

    @Transactional
    public EventDto removeMedia(UUID eventId, UUID mediaId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("delete media", "Only the host can remove media");
        }

        EventMedia media = event.getEventMedia().stream()
                .filter(m -> m.getId().equals(mediaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

        fileStorageService.deleteEventMedia(media.getUrl());
        // PHASE 2: Delete thumbnails too
        if (media.getThumbnailUrl() != null) {
            fileStorageService.deleteEventMedia(media.getThumbnailUrl());
        }
        if (media.getMediumUrl() != null) {
            fileStorageService.deleteEventMedia(media.getMediumUrl());
        }
        event.getEventMedia().remove(media);

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved, user.getId());
    }

    /**
     * PHASE 2: Reorder media for an event.
     */
    @Transactional
    public EventDto reorderMedia(UUID eventId, List<UUID> mediaIds, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("reorder media", "Only the host can reorder media");
        }

        if (mediaIds == null || mediaIds.isEmpty()) {
            throw new IllegalArgumentException("Media ID list cannot be empty");
        }

        // Validate all IDs belong to this event
        for (UUID mediaId : mediaIds) {
            boolean exists = event.getEventMedia().stream()
                    .anyMatch(m -> m.getId().equals(mediaId));
            if (!exists) {
                throw new ResourceNotFoundException("Media", "id", mediaId);
            }
        }

        // Update displayOrder based on provided list index
        for (int i = 0; i < mediaIds.size(); i++) {
            UUID mediaId = mediaIds.get(i);
            final int order = i;  // effectively final copy for the lambda
            event.getEventMedia().stream()
                    .filter(m -> m.getId().equals(mediaId))
                    .findFirst()
                    .ifPresent(m -> m.setDisplayOrder(order));
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved, user.getId());
    }

    /**
     * PHASE 2: Generate or regenerate check-in code for an event.
     */
    @Transactional
    public String generateCheckInCode(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("check-in", "Only the host can generate check-in codes");
        }

        String code = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        event.setCheckInCode(code);
        eventRepository.save(event);

        log.info("Check-in code generated for event {}: {}", eventId, code);
        return code;
    }

    @Transactional
    public EventDto saveOfficialEvent(Event event) {
        Event saved = eventRepository.save(event);
        log.info("Official event saved via MQTT: {} (Status: {})", saved.getId(), saved.getStatus());
        return eventMapper.toDto(saved, null);
    }

    private PageResponse<EventDto> buildPageResponse(Page<Event> page, UUID currentUserId) {
        return PageResponse.<EventDto>builder()
                .content(page.getContent().stream()
                        .map(e -> eventMapper.toDto(e, currentUserId))
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}