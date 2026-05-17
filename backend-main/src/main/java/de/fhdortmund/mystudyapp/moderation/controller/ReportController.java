package de.fhdortmund.mystudyapp.moderation.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.moderation.dto.CreateReportRequest;
import de.fhdortmund.mystudyapp.moderation.dto.ReportDto;
import de.fhdortmund.mystudyapp.moderation.model.ReportReason;
import de.fhdortmund.mystudyapp.moderation.model.ReportStatus;
import de.fhdortmund.mystudyapp.moderation.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportDto>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User principal) {
        ReportDto report = reportService.createReport(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(report, "Report submitted successfully"));
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReportDto>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportReason reason,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        PageResponse<ReportDto> reports = reportService.getReports(status, reason, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports, "Reports retrieved"));
    }

    @GetMapping("/admin/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportDto>> getReport(@PathVariable UUID reportId) {
        ReportDto report = reportService.getReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(report, "Report retrieved"));
    }

    @GetMapping("/admin/reports/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReportDto>>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        PageResponse<ReportDto> reports = reportService.getReportsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports, "Reports filtered by status"));
    }

    @GetMapping("/admin/reports/reason/{reason}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ReportDto>>> getReportsByReason(
            @PathVariable ReportReason reason,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        PageResponse<ReportDto> reports = reportService.getReportsByReason(reason, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports, "Reports filtered by reason"));
    }

    @PatchMapping("/admin/reports/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportDto>> resolveReport(
            @PathVariable UUID reportId,
            @RequestParam(defaultValue = "false") boolean flagEvent) {
        ReportDto report = reportService.resolveReport(reportId, flagEvent);
        return ResponseEntity.ok(ApiResponse.success(report, "Report resolved successfully"));
    }

    @DeleteMapping("/admin/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable UUID reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success(null, "Report deleted successfully"));
    }
}