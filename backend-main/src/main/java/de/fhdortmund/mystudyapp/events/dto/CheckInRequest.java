package de.fhdortmund.mystudyapp.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for attendee self check-in via QR code.
 */
@Data
public class CheckInRequest {
    
    @NotBlank(message = "Check-in code is required")
    @Size(min = 6, max = 10, message = "Invalid check-in code format")
    private String code;
}