package com.jobflow.api.dto;

import com.jobflow.common.entity.JobType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

public class CreateJobRequest {

    @NotNull(message = "jobType is required")
    private JobType jobType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;

    private Instant scheduledAt;   // optional - null means run immediately

    private boolean notifyOnCompletion = false;

    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public boolean isNotifyOnCompletion() { return notifyOnCompletion; }
    public void setNotifyOnCompletion(boolean notifyOnCompletion) { this.notifyOnCompletion = notifyOnCompletion; }
}