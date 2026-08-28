package com.connectabroad.controller;

import com.connectabroad.dto.request.CreateJobRequest;
import com.connectabroad.dto.request.UpdateJobRequest;
import com.connectabroad.dto.response.JobResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.SavedJobResponse;
import com.connectabroad.entity.EmploymentType;
import com.connectabroad.entity.JobStatus;
import com.connectabroad.entity.WorkMode;
import com.connectabroad.service.JobService;
import com.connectabroad.service.SavedJobService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final SavedJobService savedJobService;

    public JobController(JobService jobService, SavedJobService savedJobService) {
        this.jobService = jobService;
        this.savedJobService = savedJobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            Authentication authentication,
            @Valid @RequestBody CreateJobRequest request) {
        String userEmail = authentication.getName();
        JobResponse response = jobService.createJob(userEmail, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobResponse>> getJobs(
            Authentication authentication,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "employmentType", required = false) EmploymentType employmentType,
            @RequestParam(value = "workMode", required = false) WorkMode workMode,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "skill", required = false) String skill,
            @RequestParam(value = "status", required = false) JobStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        String[] sortParts = sort.split(",");
        String sortProperty = sortParts[0];
        Sort.Direction sortDirection = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        PageResponse<JobResponse> response = jobService.getJobs(
                currentUserEmail, keyword, country, city, employmentType, workMode, experience, skill, status, pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<JobResponse>> getMyJobs(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(jobService.getMyJobs(userEmail, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<JobResponse>> getJobsByPoster(
            Authentication authentication,
            @PathVariable("userId") Long posterUserId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(jobService.getJobsByPoster(posterUserId, currentUserEmail, pageable));
    }

    @GetMapping("/saved")
    public ResponseEntity<PageResponse<SavedJobResponse>> getSavedJobs(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(savedJobService.getSavedJobs(userEmail, pageable));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<JobResponse> getJobById(
            Authentication authentication,
            @PathVariable("id") Long jobId) {
        String currentUserEmail = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(jobService.getJobById(currentUserEmail, jobId));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<JobResponse> updateJob(
            Authentication authentication,
            @PathVariable("id") Long jobId,
            @Valid @RequestBody UpdateJobRequest request) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(jobService.updateJob(userEmail, jobId, request));
    }

    @PatchMapping("/{id:\\d+}/close")
    public ResponseEntity<JobResponse> closeJob(
            Authentication authentication,
            @PathVariable("id") Long jobId) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(jobService.closeJob(userEmail, jobId));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteJob(
            Authentication authentication,
            @PathVariable("id") Long jobId) {
        String userEmail = authentication.getName();
        jobService.deleteJob(userEmail, jobId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id:\\d+}/save")
    public ResponseEntity<SavedJobResponse> saveJob(
            Authentication authentication,
            @PathVariable("id") Long jobId) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(savedJobService.saveJob(userEmail, jobId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id:\\d+}/save")
    public ResponseEntity<Void> unsaveJob(
            Authentication authentication,
            @PathVariable("id") Long jobId) {
        String userEmail = authentication.getName();
        savedJobService.unsaveJob(userEmail, jobId);
        return ResponseEntity.noContent().build();
    }
}
