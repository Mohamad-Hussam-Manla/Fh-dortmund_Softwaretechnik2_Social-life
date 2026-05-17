package de.fhdortmund.mystudyapp.events.mapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.events.dto.CategoryDto;
import de.fhdortmund.mystudyapp.events.dto.EventDto;
import de.fhdortmund.mystudyapp.events.dto.EventMediaDto;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventMedia;
import de.fhdortmund.mystudyapp.identity.mapper.UserMapper;
import de.fhdortmund.mystudyapp.registration.model.Rsvp;
import de.fhdortmund.mystudyapp.registration.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserMapper userMapper;
    private final RsvpRepository rsvpRepository;

    public EventDto toDto(Event event, UUID currentUserId) {
        if (event == null) return null;

        Set<CategoryDto> categories = event.getEventCategories().stream()
                .map(ec -> CategoryDto.builder()
                        .id(ec.getCategory().getId())
                        .name(ec.getCategory().getName())
                        .build())
                .collect(Collectors.toSet());

        boolean isHost = currentUserId != null && event.getHost() != null
                && currentUserId.equals(event.getHost().getId());

        Rsvp myRsvp = null;
        if (currentUserId != null) {
            myRsvp = rsvpRepository.findByEventIdAndUserId(event.getId(), currentUserId).orElse(null);
        }

        return EventDto.builder()
                .id(event.getId())
                .host(userMapper.toDto(event.getHost()))
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .maxCapacity(event.getMaxCapacity())
                .currentRsvpCount(event.getCurrentRsvpCount())
                .status(event.getStatus())
                .categories(categories)
                .media(mapMedia(event))
                .createdAt(event.getCreatedAt())
                .isHost(isHost)
                .myRsvpStatus(myRsvp != null ? myRsvp.getStatus() : null)
                .build();
    }

    public EventDto toDto(Event event) {
        return toDto(event, null);
    }

    private List<EventMediaDto> mapMedia(Event event) {
        if (event.getEventMedia() == null) return Collections.emptyList();
        return event.getEventMedia().stream()
                .sorted(Comparator.comparing(EventMedia::getCreatedAt))
                .map(m -> EventMediaDto.builder()
                        .id(m.getId())
                        .url(m.getUrl())
                        .mediaType(m.getMediaType())
                        .filename(m.getFilename())
                        .build())
                .collect(Collectors.toList());
    }
}