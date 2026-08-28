package com.connectabroad.service;

import com.connectabroad.dto.response.AuthorSummaryResponse;
import com.connectabroad.dto.response.JobResponse;
import com.connectabroad.dto.response.PageResponse;
import com.connectabroad.dto.response.SavedJobResponse;
import com.connectabroad.entity.Job;
import com.connectabroad.entity.Profile;
import com.connectabroad.entity.SavedJob;
import com.connectabroad.entity.User;
import com.connectabroad.exception.ResourceNotFoundException;
import com.connectabroad.repository.JobRepository;
import com.connectabroad.repository.ProfileRepository;
import com.connectabroad.repository.SavedJobRepository;
import com.connectabroad.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public SavedJobService(SavedJobRepository savedJobRepository,
                           JobRepository jobRepository,
                           UserRepository userRepository,
                           ProfileRepository profileRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public SavedJobResponse saveJob(String userEmail, Long jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId)) {
            SavedJob existing = savedJobRepository.findByUserIdAndJobId(user.getId(), jobId).get();
            return mapToSavedJobResponse(existing, user.getId());
        }

        SavedJob savedJob = new SavedJob(user, job);
        SavedJob saved = savedJobRepository.save(savedJob);
        return mapToSavedJobResponse(saved, user.getId());
    }

    public void unsaveJob(String userEmail, Long jobId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with id: " + jobId);
        }
        savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
    }

    @Transactional(readOnly = true)
    public PageResponse<SavedJobResponse> getSavedJobs(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Page<SavedJob> page = savedJobRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<SavedJobResponse> content = page.getContent().stream()
                .map(sj -> mapToSavedJobResponse(sj, user.getId()))
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

    public SavedJobResponse mapToSavedJobResponse(SavedJob savedJob, Long currentUserId) {
        Job job = savedJob.getJob();
        JobResponse jobDto = mapToJobResponse(job, currentUserId, true);
        return new SavedJobResponse(savedJob.getId(), savedJob.getCreatedAt(), jobDto);
    }

    private JobResponse mapToJobResponse(Job job, Long currentUserId, boolean isSaved) {
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
