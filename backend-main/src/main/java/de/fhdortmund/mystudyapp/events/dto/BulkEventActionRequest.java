package de.fhdortmund.mystudyapp.events.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BulkEventActionRequest {
    @NotEmpty(message = "At least one event ID is required")
    @Size(max = 50, message = "Cannot process more than 50 events at once")
    private List<UUID> eventIds;

    /** Optional rejection reason (only used for bulk reject) */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}