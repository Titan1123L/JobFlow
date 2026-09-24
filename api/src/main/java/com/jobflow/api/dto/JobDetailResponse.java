package com.jobflow.api.dto;

import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.entity.JobType;

import java.time.Instant;
import java.util.UUID;

public class JobDetailResponse {
    private UUID id;
    private JobType jobType;
    private JobStatus status;
    private Instant scheduledAt;
    private boolean notifyOnCompletion;
    private int retryCount;
    private int maxRetries;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private String errorMessage;

    public JobDetailResponse(Job job) {
        this.id = job.getId();
        this.jobType = job.getJobType();
        this.status = job.getStatus();
        this.scheduledAt = job.getScheduledAt();
        this.notifyOnCompletion = job.isNotifyOnCompletion();
        this.retryCount = job.getRetryCount();
        this.maxRetries = job.getMaxRetries();
        this.createdAt = job.getCreatedAt();
        this.startedAt = job.getStartedAt();
        this.completedAt = job.getCompletedAt();
        this.errorMessage = job.getErrorMessage();
    }

    public UUID getId() {
        return id;
    }

    public JobType getJobType() {
        return jobType;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public boolean isNotifyOnCompletion() {
        return notifyOnCompletion;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
