package de.fhdortmund.mystudyapp.registration.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelRsvpRequest {
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String reason;
}