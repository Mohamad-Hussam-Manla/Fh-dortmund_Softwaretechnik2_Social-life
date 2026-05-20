package de.fhdortmund.mystudyapp.events.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchSuggestionDto {
    private String type;      // "EVENT", "CATEGORY", "USER", "LOCATION"
    private String value;     // The display text
    private String id;        // UUID or numeric ID for navigation
    private String subtitle;  // Extra context (e.g., event date, user email)
}