package de.fhdortmund.mystudyapp.events.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");

    private final EventRepository eventRepository;

    public String generateSlug(String title) {
        String base = toSlug(title);
        if (!eventRepository.existsBySlug(base)) {
            return base;
        }

        // Deduplicate: append -1, -2, etc.
        int counter = 1;
        String candidate;
        do {
            candidate = base + "-" + counter;
            counter++;
        } while (eventRepository.existsBySlug(candidate));

        return candidate;
    }

    private String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "event";
        }
        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("^-+|-+$", "");
    }
}