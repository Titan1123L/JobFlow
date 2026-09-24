package com.jobflow.api.service;

import com.jobflow.api.dto.StatsResponse;
import com.jobflow.api.security.CurrentUser;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.repository.JobRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StatsService {

    private final JobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;

    public StatsService(JobRepository jobRepository, StringRedisTemplate redisTemplate) {
        this.jobRepository = jobRepository;
        this.redisTemplate = redisTemplate;
    }

    public StatsResponse getStats() {
        UUID userId = CurrentUser.get();

        long totalJobs = jobRepository.countByUserId(userId);
        long completedJobs = jobRepository.countByUserIdAndStatus(userId, JobStatus.COMPLETED);
        long failedJobs = jobRepository.countByUserIdAndStatus(userId, JobStatus.FAILED);
        long cancelledJobs = jobRepository.countByUserIdAndStatus(userId, JobStatus.CANCELLED);
        long queuedJobs = jobRepository.countByUserIdAndStatus(userId, JobStatus.QUEUED);
        long processingJobs = jobRepository.countByUserIdAndStatus(userId, JobStatus.PROCESSING);

        double successRate = totalJobs == 0 ? 0.0 : (double) completedJobs / totalJobs;
        double failureRate = totalJobs == 0 ? 0.0 : (double) failedJobs / totalJobs;

        long avgProcessingTimeMs = computeAvgProcessingTime();

        return new StatsResponse(
                queuedJobs, totalJobs, completedJobs, failedJobs, cancelledJobs,
                queuedJobs, processingJobs, successRate, failureRate, avgProcessingTimeMs
        );
    }

    private long computeAvgProcessingTime() {
        String countStr = redisTemplate.opsForValue().get("stats:completed:count");
        String totalStr = redisTemplate.opsForValue().get("stats:completed:totalDurationMs");

        long count = countStr != null ? Long.parseLong(countStr) : 0;
        long total = totalStr != null ? Long.parseLong(totalStr) : 0;

        return count == 0 ? 0 : total / count;
    }
}