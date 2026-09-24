package com.jobflow.api.dto;

import com.jobflow.common.entity.JobStatus;

import java.time.Instant;
import java.util.UUID;

public class JobSubmittedResponse {
    private UUID id;
    private JobStatus status;
    private Instant createdAt;

    public JobSubmittedResponse(UUID id, JobStatus status, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public JobStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}