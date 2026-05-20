package de.fhdortmund.mystudyapp.registration.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaitlistPromotionDto {
    private UUID rsvpId;
    private UUID eventId;
    private UUID userId;
    private String userDisplayName;
    private String eventTitle;
    private int newRsvpCount;
    private int maxCapacity;
}