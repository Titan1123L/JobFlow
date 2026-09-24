package com.jobflow.api.dto;

public class StatsResponse {
    private long queueDepth;
    private long totalJobs;
    private long completedJobs;
    private long failedJobs;
    private long cancelledJobs;
    private long queuedJobs;
    private long processingJobs;
    private double successRate;
    private double failureRate;
    private long avgProcessingTimeMs;

    public StatsResponse(long queueDepth, long totalJobs, long completedJobs, long failedJobs,
                          long cancelledJobs, long queuedJobs, long processingJobs,
                          double successRate, double failureRate, long avgProcessingTimeMs) {
        this.queueDepth = queueDepth;
        this.totalJobs = totalJobs;
        this.completedJobs = completedJobs;
        this.failedJobs = failedJobs;
        this.cancelledJobs = cancelledJobs;
        this.queuedJobs = queuedJobs;
        this.processingJobs = processingJobs;
        this.successRate = successRate;
        this.failureRate = failureRate;
        this.avgProcessingTimeMs = avgProcessingTimeMs;
    }

    public long getQueueDepth() { return queueDepth; }
    public long getTotalJobs() { return totalJobs; }
    public long getCompletedJobs() { return completedJobs; }
    public long getFailedJobs() { return failedJobs; }
    public long getCancelledJobs() { return cancelledJobs; }
    public long getQueuedJobs() { return queuedJobs; }
    public long getProcessingJobs() { return processingJobs; }
    public double getSuccessRate() { return successRate; }
    public double getFailureRate() { return failureRate; }
    public long getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
}
