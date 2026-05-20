package de.fhdortmund.mystudyapp.events.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private Integer id;
    private String name;

    /* ==================== PHASE 2 ADDITIONS ==================== */

    /** Icon identifier for SVG mapping */
    private String icon;

    /** Hex color for badge background */
    private String color;

    /** Sort order for display */
    private Integer sortOrder;
}