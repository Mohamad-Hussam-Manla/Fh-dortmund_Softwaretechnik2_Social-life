package de.fhdortmund.mystudyapp.identity.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PHASE 3: User Preferences / Settings
 * 
 * One-to-one with User. Stores notification preferences, timezone, and language.
 * Created automatically when a user registers (default values).
 */
@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private boolean emailNotifications = true;

    @Column(name = "push_notifications", nullable = false)
    @Builder.Default
    private boolean pushNotifications = true;

    @Column(name = "notify_on_rsvp_change", nullable = false)
    @Builder.Default
    private boolean notifyOnRsvpChange = true;

    @Column(name = "notify_on_review", nullable = false)
    @Builder.Default
    private boolean notifyOnReview = true;

    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Europe/Berlin";

    @Column(name = "language", nullable = false, length = 10)
    @Builder.Default
    private String language = "de";
}