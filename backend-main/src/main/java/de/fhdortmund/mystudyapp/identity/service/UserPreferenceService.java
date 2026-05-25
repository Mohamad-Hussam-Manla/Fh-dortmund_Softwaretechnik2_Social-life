package de.fhdortmund.mystudyapp.identity.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.identity.dto.UserPreferencesDto;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.model.UserPreference;
import de.fhdortmund.mystudyapp.identity.repository.UserPreferenceRepository;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserPreferencesDto getPreferences(String email) {
        User user = findUser(email);
        UserPreference pref = userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        return toDto(pref);
    }

    @Transactional
    public UserPreferencesDto updatePreferences(String email, UserPreferencesDto dto) {
        User user = findUser(email);
        UserPreference pref = userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        pref.setEmailNotifications(dto.isEmailNotifications());
        pref.setPushNotifications(dto.isPushNotifications());
        pref.setNotifyOnRsvpChange(dto.isNotifyOnRsvpChange());
        pref.setNotifyOnReview(dto.isNotifyOnReview());

        if (dto.getTimezone() != null && !dto.getTimezone().isBlank()) {
            pref.setTimezone(dto.getTimezone());
        }
        if (dto.getLanguage() != null && !dto.getLanguage().isBlank()) {
            pref.setLanguage(dto.getLanguage());
        }

        UserPreference saved = userPreferenceRepository.save(pref);
        log.info("Preferences updated for user {}", user.getId());
        return toDto(saved);
    }

    /**
     * Creates default preferences for a user. Called during registration.
     */
    @Transactional
    public UserPreference createDefaultPreferences(User user) {
        UserPreference pref = UserPreference.builder()
                .user(user)
                .emailNotifications(true)
                .pushNotifications(true)
                .notifyOnRsvpChange(true)
                .notifyOnReview(true)
                .timezone("Europe/Berlin")
                .language("de")
                .build();
        return userPreferenceRepository.save(pref);
    }

    @Transactional(readOnly = true)
    public boolean shouldSendEmailNotification(UUID userId) {
        return userPreferenceRepository.findByUserId(userId)
                .map(UserPreference::isEmailNotifications)
                .orElse(true); // default to true if no prefs exist
    }

    @Transactional(readOnly = true)
    public boolean shouldSendPushNotification(UUID userId) {
        return userPreferenceRepository.findByUserId(userId)
                .map(UserPreference::isPushNotifications)
                .orElse(true);
    }

    private User findUser(String email) {
        return userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserPreferencesDto toDto(UserPreference pref) {
        return UserPreferencesDto.builder()
                .emailNotifications(pref.isEmailNotifications())
                .pushNotifications(pref.isPushNotifications())
                .notifyOnRsvpChange(pref.isNotifyOnRsvpChange())
                .notifyOnReview(pref.isNotifyOnReview())
                .timezone(pref.getTimezone())
                .language(pref.getLanguage())
                .build();
    }
}