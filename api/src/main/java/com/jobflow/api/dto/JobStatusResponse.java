package com.jobflow.api.dto;

import com.jobflow.common.entity.JobStatus;

import java.util.UUID;

public class JobStatusResponse {
    private UUID id;
    private JobStatus status;
    private Integer retryCount;

    public JobStatusResponse(UUID id, JobStatus status) {
        this.id = id;
        this.status = status;
    }

    public JobStatusResponse(UUID id, JobStatus status, Integer retryCount) {
        this(id, status);
        this.retryCount = retryCount;
    }

    public UUID getId() { return id; }
    public JobStatus getStatus() { return status; }
    public Integer getRetryCount() { return retryCount; }
}