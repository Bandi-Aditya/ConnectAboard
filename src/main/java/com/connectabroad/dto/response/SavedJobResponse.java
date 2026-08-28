package com.connectabroad.dto.response;

import java.time.LocalDateTime;

public class SavedJobResponse {

    private Long id;
    private LocalDateTime savedAt;
    private JobResponse job;

    public SavedJobResponse() {}

    public SavedJobResponse(Long id, LocalDateTime savedAt, JobResponse job) {
        this.id = id;
        this.savedAt = savedAt;
        this.job = job;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }

    public JobResponse getJob() { return job; }
    public void setJob(JobResponse job) { this.job = job; }
}
