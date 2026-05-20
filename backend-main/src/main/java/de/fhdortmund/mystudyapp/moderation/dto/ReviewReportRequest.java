package de.fhdortmund.mystudyapp.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * PHASE 3: Request body for reporting a review as inappropriate.
 */
@Data
public class ReviewReportRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}