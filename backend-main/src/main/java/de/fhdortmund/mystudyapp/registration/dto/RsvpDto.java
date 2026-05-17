package de.fhdortmund.mystudyapp.registration.dto;

import java.time.Instant;
import java.util.UUID;

import de.fhdortmund.mystudyapp.identity.dto.UserDto;
import de.fhdortmund.mystudyapp.registration.model.RsvpStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RsvpDto {
    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private UserDto user;
    private RsvpStatus status;
    private Instant createdAt;
}