package de.fhdortmund.mystudyapp.events.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for returning a generated check-in code to the host.
 * Includes the code and metadata for QR display.
 */
@Data
@Builder
public class CheckInCodeDto {
    private String checkInCode;
    private String eventId;
    private String eventTitle;
    
    /**
     * ISO timestamp when the code was generated.
     * Host should refresh QR periodically (e.g., every 5 minutes).
     */
    private String generatedAt;
    
    /**
     * Recommended QR refresh interval in seconds.
     */
    @Builder.Default
    private int refreshIntervalSeconds = 300; // 5 minutes
}