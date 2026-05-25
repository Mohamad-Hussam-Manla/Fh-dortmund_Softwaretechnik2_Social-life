package de.fhdortmund.mystudyapp.identity.dto;

import java.time.Instant;
import java.util.UUID;

import de.fhdortmund.mystudyapp.identity.model.Role;
import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {

    private UUID id;
    private String universityEmail;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private Role role;
    private TrustLevel trustLevel;
    private Instant createdAt;
}