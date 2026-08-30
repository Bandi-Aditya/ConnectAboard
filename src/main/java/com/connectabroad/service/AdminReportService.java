package com.connectabroad.service;

import com.connectabroad.dto.admin.ReportResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.entity.Report;
import com.connectabroad.entity.ReportStatus;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.ReportRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final AdminAuditLogService auditLogService;

    public AdminReportService(ReportRepository reportRepository,
                               ReportService reportService,
                               UserRepository userRepository,
                               AdminAuditLogService auditLogService) {
        this.reportRepository = reportRepository;
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        Page<Report> page = reportRepository.findAdminReports(status, pageable);
        return PageResponse.from(page.map(reportService::mapToReportResponse));
    }

    public ReportResponse updateReportStatus(String adminEmail, Long reportId, ReportStatus newStatus) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminEmail));
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        ReportStatus oldStatus = report.getStatus();
        report.setStatus(newStatus);
        if (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.DISMISSED) {
            report.setResolvedAt(LocalDateTime.now());
            report.setResolvedBy(admin);
        }

        Report saved = reportRepository.save(report);

        auditLogService.logAction(
                admin,
                "UPDATE_REPORT_STATUS",
                "REPORT",
                reportId,
                "Updated report #" + reportId + " (" + report.getTargetType() + " #" + report.getTargetId() + ") from " + oldStatus + " to " + newStatus
        );

        return reportService.mapToReportResponse(saved);
    }
}
