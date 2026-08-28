package com.connectabroad.service;

import com.connectabroad.dto.request.CreateJobRequest;
import com.connectabroad.dto.request.UpdateJobRequest;
import com.connectabroad.dto.response.AuthorSummaryResponse;
import com.connectabroad.dto.response.JobResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.entity.*;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.JobRepository;
import com.connectabroad.repository.ProfileRepository;
import com.connectabroad.repository.SavedJobRepository;
import com.connectabroad.repository.UserRepository;
import com.connectabroad.specification.JobSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public JobService(JobRepository jobRepository,
                      SavedJobRepository savedJobRepository,
                      UserRepository userRepository,
                      ProfileRepository profileRepository) {
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public JobResponse createJob(String userEmail, CreateJobRequest request) {
        User postedBy = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }

        Job job = new Job(
                postedBy,
                request.getTitle().trim(),
                request.getCompanyName().trim(),
                request.getDescription().trim(),
                request.getCountry().trim(),
                request.getCity().trim(),
                request.getEmploymentType(),
                request.getWorkMode()
        );

        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            job.setCurrency(request.getCurrency().trim());
        }
        job.setRequiredSkills(request.getRequiredSkills() != null ? request.getRequiredSkills().trim() : null);
        job.setExperienceRequired(request.getExperienceRequired() != null ? request.getExperienceRequired().trim() : null);
        job.setApplicationMethod(request.getApplicationMethod() != null ? request.getApplicationMethod().trim() : null);
        job.setApplicationUrl(request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : null);
        job.setContactEmail(request.getContactEmail() != null ? request.getContactEmail().trim() : null);
        job.setStatus(JobStatus.ACTIVE);

        Job savedJob = jobRepository.save(job);
        return mapToJobResponse(savedJob, postedBy.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getJobs(String currentUserEmail,
                                            String keyword,
                                            String country,
                                            String city,
                                            EmploymentType employmentType,
                                            WorkMode workMode,
                                            String experience,
                                            String skill,
                                            JobStatus status,
                                            Pageable pageable) {

        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        JobStatus targetStatus = (status != null) ? status : JobStatus.ACTIVE;

        Specification<Job> spec = JobSpecification.filterJobs(
                keyword, country, city, employmentType, workMode, experience, skill, targetStatus
        );

        Page<Job> page = jobRepository.findAll(spec, pageable);

        List<JobResponse> content = page.getContent().stream()
                .map(j -> mapToJobResponse(j, currentUserId))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getMyJobs(String userEmail, Pageable pageable) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Page<Job> page = jobRepository.findByPostedByIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        List<JobResponse> content = page.getContent().stream()
                .map(j -> mapToJobResponse(j, currentUser.getId()))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> getJobsByPoster(Long posterUserId, String currentUserEmail, Pageable pageable) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        Page<Job> page = jobRepository.findByPostedByIdAndStatusOrderByCreatedAtDesc(posterUserId, JobStatus.ACTIVE, pageable);

        List<JobResponse> content = page.getContent().stream()
                .map(j -> mapToJobResponse(j, currentUserId))
                .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional(readOnly = true)
    public JobResponse getJobById(String currentUserEmail, Long jobId) {
        User currentUser = currentUserEmail != null ? userRepository.findByEmail(currentUserEmail).orElse(null) : null;
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        return mapToJobResponse(job, currentUserId);
    }

    public JobResponse updateJob(String userEmail, Long jobId, UpdateJobRequest request) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to edit this job");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            job.setTitle(request.getTitle().trim());
        }
        if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()) {
            job.setCompanyName(request.getCompanyName().trim());
        }
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            job.setDescription(request.getDescription().trim());
        }
        if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            job.setCountry(request.getCountry().trim());
        }
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            job.setCity(request.getCity().trim());
        }
        if (request.getEmploymentType() != null) {
            job.setEmploymentType(request.getEmploymentType());
        }
        if (request.getWorkMode() != null) {
            job.setWorkMode(request.getWorkMode());
        }
        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }
        if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            job.setCurrency(request.getCurrency().trim());
        }
        if (request.getRequiredSkills() != null) {
            job.setRequiredSkills(request.getRequiredSkills().trim());
        }
        if (request.getExperienceRequired() != null) {
            job.setExperienceRequired(request.getExperienceRequired().trim());
        }
        if (request.getApplicationMethod() != null) {
            job.setApplicationMethod(request.getApplicationMethod().trim());
        }
        if (request.getApplicationUrl() != null) {
            job.setApplicationUrl(request.getApplicationUrl().trim());
        }
        if (request.getContactEmail() != null) {
            job.setContactEmail(request.getContactEmail().trim());
        }
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        Job updatedJob = jobRepository.save(job);
        return mapToJobResponse(updatedJob, currentUser.getId());
    }

    public JobResponse closeJob(String userEmail, Long jobId) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to close this job");
        }

        job.setStatus(JobStatus.CLOSED);
        Job closedJob = jobRepository.save(job);
        return mapToJobResponse(closedJob, currentUser.getId());
    }

    public void deleteJob(String userEmail, Long jobId) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getPostedBy().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this job");
        }

        savedJobRepository.deleteByUserIdAndJobId(currentUser.getId(), jobId);
        jobRepository.delete(job);
    }

    private JobResponse mapToJobResponse(Job job, Long currentUserId) {
        boolean isSaved = false;
        if (currentUserId != null) {
            isSaved = savedJobRepository.existsByUserIdAndJobId(currentUserId, job.getId());
        }

        AuthorSummaryResponse posterSummary = buildAuthorSummary(job.getPostedBy());

        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setPostedBy(posterSummary);
        response.setTitle(job.getTitle());
        response.setCompanyName(job.getCompanyName());
        response.setDescription(job.getDescription());
        response.setCountry(job.getCountry());
        response.setCity(job.getCity());
        response.setEmploymentType(job.getEmploymentType());
        response.setWorkMode(job.getWorkMode());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setCurrency(job.getCurrency());
        response.setRequiredSkills(job.getRequiredSkills());
        response.setExperienceRequired(job.getExperienceRequired());
        response.setApplicationMethod(job.getApplicationMethod());
        response.setApplicationUrl(job.getApplicationUrl());
        response.setContactEmail(job.getContactEmail());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setStatus(job.getStatus());
        response.setSaved(isSaved);
        response.setMine(currentUserId != null && currentUserId.equals(job.getPostedBy().getId()));

        return response;
    }

    private AuthorSummaryResponse buildAuthorSummary(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        String collegeName = (profile != null && profile.getCollege() != null) ? profile.getCollege().getName() : null;
        return new AuthorSummaryResponse(
                user.getId(),
                user.getName(),
                profile != null ? profile.getProfilePhoto() : null,
                profile != null ? profile.getProfession() : null,
                profile != null ? profile.getCurrentCity() : null,
                profile != null ? profile.getCurrentCountry() : null,
                collegeName,
                user.getUserType()
        );
    }
}
