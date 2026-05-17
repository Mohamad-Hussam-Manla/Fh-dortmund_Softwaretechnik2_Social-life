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
    private final StorageProperties storageProperties;
    private final RsvpRepository rsvpRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

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

    @Transactional(readOnly = true)
    public EventDto getEvent(UUID eventId, String currentUserEmail) {
        User currentUser = userRepository.findByUniversityEmail(currentUserEmail).orElse(null);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        UUID currentUserId = currentUser != null ? currentUser.getId() : null;
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

        User currentUser = userRepository.findByUniversityEmail(currentUserEmail).orElse(null);
        UUID currentUserId = currentUser != null ? currentUser.getId() : null;

        Page<Event> page = eventRepository.findPublishedWithFilters(
                EventStatus.PUBLISHED, categoryId, dateFrom, dateTo, location, q, pageable);

        return buildPageResponse(page, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventDto> getMyEvents(String email, Pageable pageable) {
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
    public EventDto cancelEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(user.getId())) {
            throw new ForbiddenActionException("cancel", "Only the host can cancel this event");
        }

        event.setStatus(EventStatus.CANCELLED);
        Event updated = eventRepository.save(event);
        log.info("Event cancelled: {} by {}", eventId, userEmail);
        return eventMapper.toDto(updated, user.getId());
    }

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

        if (event.getEventMedia() != null) {
            for (EventMedia media : event.getEventMedia()) {
                fileStorageService.deleteEventMedia(media.getUrl());
            }
            event.getEventMedia().clear();
        }

        rsvpRepository.deleteAllByEventId(eventId);
        reviewRepository.deleteAllByEventId(eventId);
        reportRepository.deleteAllByEventId(eventId);

        eventRepository.delete(event);
        log.info("Event deleted: {} by {}", eventId, userEmail);
    }

    /* ==================== Media Management ==================== */

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
                    event.getEventMedia().add(EventMedia.builder()
                            .event(event)
                            .url(url)
                            .mediaType(MediaType.IMAGE)
                            .filename(img.getOriginalFilename())
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
        event.getEventMedia().remove(media);

        Event saved = eventRepository.save(event);
        return eventMapper.toDto(saved, user.getId());
    }

    /* ==================== Official Events ==================== */

    @Transactional
    public EventDto saveOfficialEvent(Event event) {
        Event saved = eventRepository.save(event);
        log.info("Official event saved via MQTT: {} (Status: {})", saved.getId(), saved.getStatus());
        return eventMapper.toDto(saved, null);
    }

    /* ==================== Helpers ==================== */

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