package de.fhdortmund.mystudyapp.events.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventCategoryId implements Serializable {

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
}