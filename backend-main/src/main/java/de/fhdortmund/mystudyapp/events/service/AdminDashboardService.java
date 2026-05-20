package de.fhdortmund.mystudyapp.events.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.events.dto.AdminDashboardDto;
import de.fhdortmund.mystudyapp.events.dto.EventDto;
import de.fhdortmund.mystudyapp.events.mapper.EventMapper;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.moderation.dto.ReportDto;
import de.fhdortmund.mystudyapp.moderation.mapper.ReportMapper;
import de.fhdortmund.mystudyapp.moderation.model.ReportStatus;
import de.fhdortmund.mystudyapp.moderation.repository.ReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final EventMapper eventMapper;
    private final ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        Instant now = Instant.now();
        Instant startOfToday = now.truncatedTo(ChronoUnit.DAYS);
        Instant startOfWeek = now.minus(7, ChronoUnit.DAYS);

        long pendingEventsCount = eventRepository.countByStatus(EventStatus.UNDER_REVIEW);
        long openReportsCount = reportRepository.countByStatus(ReportStatus.OPEN);
        long totalUsersCount = userRepository.count();
        long newUsersToday = userRepository.countByCreatedAtAfter(startOfToday);
        long eventsThisWeek = eventRepository.countByStatusAndCreatedAtAfter(EventStatus.PUBLISHED, startOfWeek);

        Pageable recentPage = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        List<ReportDto> recentReports = reportRepository.findByStatus(ReportStatus.OPEN, recentPage)
                .getContent().stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());

        List<EventDto> recentPendingEvents = eventRepository.findByStatus(EventStatus.UNDER_REVIEW, recentPage)
                .getContent().stream()
                .map(e -> eventMapper.toDto(e, null))
                .collect(Collectors.toList());

        return AdminDashboardDto.builder()
                .pendingEventsCount(pendingEventsCount)
                .openReportsCount(openReportsCount)
                .totalUsersCount(totalUsersCount)
                .newUsersToday(newUsersToday)
                .eventsThisWeek(eventsThisWeek)
                .recentReports(recentReports)
                .recentPendingEvents(recentPendingEvents)
                .build();
    }
}