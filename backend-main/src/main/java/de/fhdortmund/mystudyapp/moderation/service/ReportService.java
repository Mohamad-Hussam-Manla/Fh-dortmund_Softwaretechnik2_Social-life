package de.fhdortmund.mystudyapp.moderation.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fhdortmund.mystudyapp.common.exception.ForbiddenActionException;
import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.moderation.dto.CreateReportRequest;
import de.fhdortmund.mystudyapp.moderation.dto.ReportDto;
import de.fhdortmund.mystudyapp.moderation.mapper.ReportMapper;
import de.fhdortmund.mystudyapp.moderation.model.Report;
import de.fhdortmund.mystudyapp.moderation.model.ReportReason;
import de.fhdortmund.mystudyapp.moderation.model.ReportStatus;
import de.fhdortmund.mystudyapp.moderation.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;
    private final AlertMqttGateway alertMqttGateway;
    private final ObjectMapper objectMapper;

    /* ==================== MQTT Alert Gateway ==================== */

    @MessagingGateway(defaultRequestChannel = "mqttAlertOutboundChannel")
    public interface AlertMqttGateway {
        void sendAlert(String payload);
    }

    /* ==================== CRUD ==================== */

    @Transactional
    public ReportDto createReport(CreateReportRequest request, String reporterEmail) {
        User reporter = userRepository.findByUniversityEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));

        if (event.getHost().getId().equals(reporter.getId())) {
            throw new ForbiddenActionException("report", "You cannot report your own event");
        }

        Report report = Report.builder()
                .event(event)
                .reporter(reporter)
                .reason(request.getReason())
                .details(request.getDetails())
                .status(ReportStatus.OPEN)
                .build();

        Report saved = reportRepository.save(report);
        log.info("Report created: {} for event {} by {}", saved.getId(), event.getId(), reporterEmail);

        // MQTT INTEGRATION: Publish critical alerts to backend-asta
        if (request.getReason() == ReportReason.INAPPROPRIATE || request.getReason() == ReportReason.FAKE_EVENT) {
            publishCriticalAlert(saved, event, reporter);
        }

        return reportMapper.toDto(saved);
    }

    /**
     * Publishes a critical report alert via MQTT to the AStA backend.
     */
    private void publishCriticalAlert(Report report, Event event, User reporter) {
        try {
            Map<String, Object> alertPayload = new HashMap<>();
            alertPayload.put("type", "CRITICAL_REPORT");
            alertPayload.put("reportId", report.getId().toString());
            alertPayload.put("eventId", event.getId().toString());
            alertPayload.put("eventTitle", event.getTitle());
            alertPayload.put("reason", report.getReason().name());
            alertPayload.put("details", report.getDetails());
            alertPayload.put("reporterEmail", reporter.getUniversityEmail());
            alertPayload.put("hostEmail", event.getHost().getUniversityEmail());
            alertPayload.put("timestamp", Instant.now().toString());
            alertPayload.put("severity", report.getReason() == ReportReason.INAPPROPRIATE ? "HIGH" : "MEDIUM");

            String jsonPayload = objectMapper.writeValueAsString(alertPayload);
            alertMqttGateway.sendAlert(jsonPayload);

            log.warn("CRITICAL ALERT published via MQTT: Report {} for event '{}' (Reason: {})",
                    report.getId(), event.getTitle(), report.getReason());
        } catch (Exception e) {
            log.error("Failed to publish MQTT alert for report {}: {}", report.getId(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ReportDto getReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        return reportMapper.toDto(report);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDto> getReports(ReportStatus status, ReportReason reason, Pageable pageable) {
        Page<Report> page;
        if (status != null && reason != null) {
            page = reportRepository.findByStatusAndReason(status, reason, pageable);
        } else if (status != null) {
            page = reportRepository.findByStatus(status, pageable);
        } else if (reason != null) {
            page = reportRepository.findByReason(reason, pageable);
        } else {
            page = reportRepository.findAll(pageable);
        }
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDto> getReportsByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> page = reportRepository.findByStatus(status, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportDto> getReportsByReason(ReportReason reason, Pageable pageable) {
        Page<Report> page = reportRepository.findByReason(reason, pageable);
        return buildPageResponse(page);
    }

    @Transactional
    public ReportDto resolveReport(UUID reportId) {
        return resolveReport(reportId, false);
    }

    @Transactional
    public ReportDto resolveReport(UUID reportId, boolean flagEvent) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        if (flagEvent) {
            Event event = report.getEvent();
            event.setStatus(EventStatus.UNDER_REVIEW);
            eventRepository.save(event);
            log.info("Event {} flagged to UNDER_REVIEW via report resolution {}", event.getId(), reportId);
        }

        report.setStatus(ReportStatus.RESOLVED);
        Report saved = reportRepository.save(report);
        log.info("Report resolved: {}", reportId);
        return reportMapper.toDto(saved);
    }

    @Transactional
    public void deleteReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
        reportRepository.delete(report);
        log.info("Report deleted: {}", reportId);
    }

    /* ==================== Helpers ==================== */

    private PageResponse<ReportDto> buildPageResponse(Page<Report> page) {
        return PageResponse.<ReportDto>builder()
                .content(page.getContent().stream()
                        .map(reportMapper::toDto)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}