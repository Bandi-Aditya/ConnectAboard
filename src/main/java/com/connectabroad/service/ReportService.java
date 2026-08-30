package com.connectabroad.service;

import com.connectabroad.dto.admin.ReportRequest;
import com.connectabroad.dto.admin.ReportResponse;
import com.connectabroad.entity.Report;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.ReportRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public ReportResponse submitReport(String reporterEmail, ReportRequest request) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + reporterEmail));

        Report report = new Report(
                reporter,
                request.getTargetType(),
                request.getTargetId(),
                request.getReason(),
                request.getDescription()
        );

        Report saved = reportRepository.save(report);
        return mapToReportResponse(saved);
    }

    public ReportResponse mapToReportResponse(Report r) {
        ReportResponse response = new ReportResponse();
        response.setId(r.getId());
        response.setReporterName(r.getReporter() != null ? r.getReporter().getName() : "Anonymous");
        response.setReporterId(r.getReporter() != null ? r.getReporter().getId() : null);
        response.setTargetType(r.getTargetType());
        response.setTargetId(r.getTargetId());
        response.setReason(r.getReason());
        response.setDescription(r.getDescription());
        response.setStatus(r.getStatus());
        response.setCreatedAt(r.getCreatedAt());
        response.setResolvedAt(r.getResolvedAt());
        response.setResolvedByName(r.getResolvedBy() != null ? r.getResolvedBy().getName() : null);
        response.setResolvedById(r.getResolvedBy() != null ? r.getResolvedBy().getId() : null);
        return response;
    }
}
