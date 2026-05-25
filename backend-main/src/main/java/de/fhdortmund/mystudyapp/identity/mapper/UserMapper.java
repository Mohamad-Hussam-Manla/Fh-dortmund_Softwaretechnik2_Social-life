package de.fhdortmund.mystudyapp.identity.mapper;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.identity.dto.UpdateProfileRequest;
import de.fhdortmund.mystudyapp.identity.dto.UserDto;
import de.fhdortmund.mystudyapp.identity.model.User;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .universityEmail(user.getUniversityEmail())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .trustLevel(user.getTrustLevel())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Maps non-null fields from the request onto the existing entity.
     */
    public void updateEntity(User user, UpdateProfileRequest request) {
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
    }
}