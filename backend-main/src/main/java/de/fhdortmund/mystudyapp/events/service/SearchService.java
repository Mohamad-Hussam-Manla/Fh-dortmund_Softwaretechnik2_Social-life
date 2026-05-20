package de.fhdortmund.mystudyapp.events.service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.events.dto.SearchSuggestionDto;
import de.fhdortmund.mystudyapp.events.repository.CategoryRepository;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_SUGGESTIONS_PER_TYPE = 5;
    private static final DateTimeFormatter DATE_FORMAT = 
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneId.of("Europe/Berlin"));

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SearchSuggestionDto> getSuggestions(String query, String type) {
        String normalizedQuery = query.toLowerCase().trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        List<SearchSuggestionDto> results = new ArrayList<>();
        Pageable limit = PageRequest.of(0, MAX_SUGGESTIONS_PER_TYPE);

        if (type.equals("ALL") || type.equals("EVENT")) {
            eventRepository.findTop5ByTitleContainingIgnoreCaseAndStatusPublished(normalizedQuery, limit)
                    .forEach(event -> results.add(SearchSuggestionDto.builder()
                            .type("EVENT")
                            .value(event.getTitle())
                            .id(event.getId().toString())
                            .subtitle(DATE_FORMAT.format(event.getStartTime()) + " • " + event.getLocation())
                            .build()));
        }

        if (type.equals("ALL") || type.equals("CATEGORY")) {
            categoryRepository.findTop5ByNameContainingIgnoreCase(normalizedQuery, limit)
                    .forEach(cat -> results.add(SearchSuggestionDto.builder()
                            .type("CATEGORY")
                            .value(cat.getName())
                            .id(cat.getId().toString())
                            .subtitle("Category")
                            .build()));
        }

        if (type.equals("ALL") || type.equals("USER")) {
            userRepository.findTop5ByDisplayNameContainingIgnoreCase(normalizedQuery, limit)
                    .forEach(user -> results.add(SearchSuggestionDto.builder()
                            .type("USER")
                            .value(user.getDisplayName())
                            .id(user.getId().toString())
                            .subtitle(user.getUniversityEmail())
                            .build()));
        }

        if (type.equals("ALL") || type.equals("LOCATION")) {
            eventRepository.findTop5DistinctLocationsByLocationContainingIgnoreCase(normalizedQuery, limit)
                    .forEach(loc -> results.add(SearchSuggestionDto.builder()
                            .type("LOCATION")
                            .value(loc)
                            .id(loc)
                            .subtitle("Location")
                            .build()));
        }

        return results;
    }
}