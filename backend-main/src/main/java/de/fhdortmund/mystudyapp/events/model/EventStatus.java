package de.fhdortmund.mystudyapp.events.model;

public enum EventStatus {
    DRAFT,          // PHASE 2: Unpublished, editable by host only
    PUBLISHED,
    UNDER_REVIEW,
    CANCELLED,
    COMPLETED
}