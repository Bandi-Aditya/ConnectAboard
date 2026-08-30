package com.connectabroad.controller;

import com.connectabroad.dto.admin.*;
import com.connectabroad.dto.response.JobResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.PostResponse;
import com.connectabroad.dto.response.UserResponse;
import com.connectabroad.entity.CommunityStatus;
import com.connectabroad.entity.JobStatus;
import com.connectabroad.entity.ReportStatus;
import com.connectabroad.entity.Role;
import com.connectabroad.entity.UserStatus;
import com.connectabroad.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final AdminUserService userService;
    private final AdminPostService postService;
    private final AdminJobService jobService;
    private final AdminCommunityService communityService;
    private final AdminReportService reportService;
    private final AnalyticsService analyticsService;
    private final AdminAuditLogService auditLogService;

    public AdminController(AdminDashboardService dashboardService,
                           AdminUserService userService,
                           AdminPostService postService,
                           AdminJobService jobService,
                           AdminCommunityService communityService,
                           AdminReportService reportService,
                           AnalyticsService analyticsService,
                           AdminAuditLogService auditLogService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.postService = postService;
        this.jobService = jobService;
        this.communityService = communityService;
        this.reportService = reportService;
        this.analyticsService = analyticsService;
        this.auditLogService = auditLogService;
    }

    // Dashboard APIs
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/dashboard/recent-activity")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }

    // User Management APIs
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userService.getUsers(keyword, role, status, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetails(id));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request) {

        return ResponseEntity.ok(userService.updateUserStatus(userDetails.getUsername(), id, request.getStatus()));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request) {

        return ResponseEntity.ok(userService.updateUserRole(userDetails.getUsername(), id, request.getRole()));
    }

    // Post Management APIs
    @GetMapping("/posts")
    public ResponseEntity<PageResponse<PostResponse>> getPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.getAdminPosts(keyword, pageable));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> removePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        postService.removePost(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // Job Management APIs
    @GetMapping("/jobs")
    public ResponseEntity<PageResponse<JobResponse>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(jobService.getAdminJobs(keyword, pageable));
    }

    @PutMapping("/jobs/{id}/status")
    public ResponseEntity<JobResponse> updateJobStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam JobStatus status) {

        return ResponseEntity.ok(jobService.updateJobStatus(userDetails.getUsername(), id, status));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> removeJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        jobService.removeJob(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // Community Management APIs
    @GetMapping("/communities")
    public ResponseEntity<PageResponse<CommunityResponse>> getCommunities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CommunityStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(communityService.searchCommunities(keyword, status, pageable));
    }

    @PutMapping("/communities/{id}/status")
    public ResponseEntity<CommunityResponse> updateCommunityStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam CommunityStatus status) {

        return ResponseEntity.ok(communityService.updateCommunityStatus(userDetails.getUsername(), id, status));
    }

    @DeleteMapping("/communities/{id}")
    public ResponseEntity<Void> removeCommunity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        communityService.removeCommunity(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // Reporting System APIs
    @GetMapping("/reports")
    public ResponseEntity<PageResponse<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(reportService.getReports(status, pageable));
    }

    @PutMapping("/reports/{id}/status")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ReportStatusUpdateRequest request) {

        return ResponseEntity.ok(reportService.updateReportStatus(userDetails.getUsername(), id, request.getStatus()));
    }

    // Platform Analytics APIs
    @GetMapping("/analytics")
    public ResponseEntity<PlatformAnalyticsResponse> getAnalytics(
            @RequestParam(defaultValue = "30") int days) {

        return ResponseEntity.ok(analyticsService.getPlatformAnalytics(days));
    }

    // Admin Audit Log APIs
    @GetMapping("/audit-logs")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(auditLogService.getAuditLogs(keyword, pageable));
    }
}
