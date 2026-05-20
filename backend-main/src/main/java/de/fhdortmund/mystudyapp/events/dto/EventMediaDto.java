package de.fhdortmund.mystudyapp.events.dto;

import java.util.UUID;

import de.fhdortmund.mystudyapp.events.model.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventMediaDto {
    private UUID id;
    private String url;
    private MediaType mediaType;
    private String filename;

    /* ==================== PHASE 2 ADDITIONS ==================== */

    /** 400x300 thumbnail for event cards */
    private String thumbnailUrl;

    /** 800x600 medium for detail views */
    private String mediumUrl;

    /** Display order in the media carousel */
    private Integer displayOrder;
}