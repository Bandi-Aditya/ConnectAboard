package com.connectabroad.controller;

import com.connectabroad.dto.admin.ReportRequest;
import com.connectabroad.dto.admin.ReportResponse;
import com.connectabroad.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> submitReport(@AuthenticationPrincipal UserDetails userDetails,
                                                        @Valid @RequestBody ReportRequest request) {
        ReportResponse response = reportService.submitReport(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
