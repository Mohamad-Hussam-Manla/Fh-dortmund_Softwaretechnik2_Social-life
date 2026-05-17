package de.fhdortmund.mystudyapp.events.service;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventLifecycleService {

    private final EventRepository eventRepository;

    /**
     * Runs every hour at minute 0.
     * Flips all PUBLISHED events whose endTime has passed to COMPLETED.
     * This ensures the trust algorithm sees accurate "completed event" counts
     * and provides a clean status for analytics/history.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoCompletePastEvents() {
        Instant now = Instant.now();

        List<Event> pastEvents = eventRepository.findByStatusAndEndTimeBefore(EventStatus.PUBLISHED, now);

        if (pastEvents.isEmpty()) {
            return;
        }

        for (Event event : pastEvents) {
            event.setStatus(EventStatus.COMPLETED);
        }

        eventRepository.saveAll(pastEvents);
        log.info("Auto-completed {} past events to COMPLETED status", pastEvents.size());
    }
}