package com.connectabroad.service;

import com.connectabroad.dto.response.JobResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.entity.Job;
import com.connectabroad.entity.JobStatus;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.JobRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminJobService {

    private final JobRepository jobRepository;
    private final JobService jobService;
    private final UserRepository userRepository;
    private final AdminAuditLogService auditLogService;

    public AdminJobService(JobRepository jobRepository,
                           JobService jobService,
                           UserRepository userRepository,
                           AdminAuditLogService auditLogService) {
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getAdminJobs(String keyword, Pageable pageable) {
        Page<Job> page = jobRepository.findAdminJobs(keyword, pageable);
        return PageResponse.from(page.map(job -> jobService.mapToJobResponse(job, null)));
    }

    public JobResponse updateJobStatus(String adminEmail, Long jobId, JobStatus newStatus) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        JobStatus oldStatus = job.getStatus();
        job.setStatus(newStatus);
        Job saved = jobRepository.save(job);

        auditLogService.logAction(
                admin,
                "UPDATE_JOB_STATUS",
                "JOB",
                jobId,
                "Changed job '" + job.getTitle() + "' status from " + oldStatus + " to " + newStatus
        );

        return jobService.mapToJobResponse(saved, null);
    }

    public void removeJob(String adminEmail, Long jobId) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + adminEmail));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        String title = job.getTitle();
        jobRepository.delete(job);

        auditLogService.logAction(
                admin,
                "DELETE_JOB",
                "JOB",
                jobId,
                "Deleted job post: " + title
        );
    }
}
