package de.fhdortmund.mystudyapp.events.dto;

import java.util.List;

import de.fhdortmund.mystudyapp.moderation.dto.ReportDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardDto {
    private long pendingEventsCount;
    private long openReportsCount;
    private long totalUsersCount;
    private long newUsersToday;
    private long eventsThisWeek;
    private List<ReportDto> recentReports;
    private List<EventDto> recentPendingEvents;
}