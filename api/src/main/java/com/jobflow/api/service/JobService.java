package com.jobflow.api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.jobflow.api.exception.JobNotFoundException;
import com.jobflow.api.exception.JobResultNotAvailableException;
import com.jobflow.api.exception.ValidationException;
import com.jobflow.api.messaging.JobPublisher;
import com.jobflow.api.security.CurrentUser;
import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.entity.JobType;
import com.jobflow.common.repository.JobRepository;
import com.jobflow.common.security.SsrfGuard;

@Service
public class JobService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MAX_IMAGE_DIMENSION = 4096;

    private final JobRepository jobRepository;
    private final JobPublisher jobPublisher;

    public JobService(JobRepository jobRepository, JobPublisher jobPublisher) {
        this.jobRepository = jobRepository;
        this.jobPublisher = jobPublisher;
    }

    public Job createJob(JobType jobType, Map<String, Object> payload, Instant scheduledAt,
            boolean notifyOnCompletion) {
        validatePayload(jobType, payload);

        if (scheduledAt != null) {
            if (scheduledAt.isBefore(Instant.now())) {
                throw new ValidationException("scheduledAt", "scheduledAt must be in the future");
            }
            if (scheduledAt.isAfter(Instant.now().plus(365, ChronoUnit.DAYS))) {
                throw new ValidationException("scheduledAt", "scheduledAt must not be more than 1 year in the future");
            }
        }

        Job job = new Job();
        job.setUserId(CurrentUser.get());
        job.setJobType(jobType);
        job.setPayload(payload);
        job.setScheduledAt(scheduledAt);
        job.setNotifyOnCompletion(notifyOnCompletion);

        if (scheduledAt != null) {
            job.setStatus(JobStatus.SCHEDULED);
            job = jobRepository.save(job);
            // Not published yet - the scheduler will publish it once scheduledAt arrives
        } else {
            job.setStatus(JobStatus.QUEUED);
            job = jobRepository.save(job);
            jobPublisher.publish(job.getId(), job.getJobType());
        }

        return job;
    }

    public Job getJob(UUID id) {
        return jobRepository.findByIdAndUserId(id, CurrentUser.get())
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    public Job cancelJob(UUID id) {
        Job job = getJob(id);
        if (job.getStatus() != JobStatus.QUEUED && job.getStatus() != JobStatus.SCHEDULED) {
            throw new com.jobflow.api.exception.InvalidStateException(
                    "Job is not in a cancellable state (current status: " + job.getStatus() + ")");
        }
        job.setStatus(JobStatus.CANCELLED);
        return jobRepository.save(job);
    }

    public Job retryJob(UUID id) {
        Job job = getJob(id);
        if (job.getStatus() != JobStatus.FAILED) {
            throw new com.jobflow.api.exception.InvalidStateException(
                    "Job is not in FAILED state (current status: " + job.getStatus() + ")");
        }
        job.setRetryCount(0);
        job.setStatus(JobStatus.QUEUED);
        job.setErrorMessage(null);
        job = jobRepository.save(job);

        jobPublisher.publish(job.getId(), job.getJobType());
        return job;
    }

    @org.springframework.beans.factory.annotation.Value("${jobflow.storage.resized-image-path}")
    private String storagePath;

    public java.io.File getJobResultFile(UUID id) {
        Job job = getJob(id);

        if (job.getJobType() != JobType.RESIZE_IMAGE) {
            throw new JobResultNotAvailableException("This job type does not produce a downloadable result");
        }
        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new JobResultNotAvailableException(
                    "Job is not yet completed (current status: " + job.getStatus() + ")");
        }

        java.io.File file = java.nio.file.Path.of(storagePath, id + ".png").toFile();
        if (!file.exists()) {
            throw new JobResultNotAvailableException("Result file is missing on disk");
        }
        return file;
    }

    public org.springframework.data.domain.Page<Job> listJobs(JobStatus status, java.time.Instant from,
            java.time.Instant to, int page, int size, String sort) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("from", "'from' must not be after 'to'");
        }

        int cappedSize = Math.min(size, 100);

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        org.springframework.data.domain.Sort.Direction direction = (sortParts.length > 1
                && sortParts[1].equalsIgnoreCase("asc"))
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                cappedSize, org.springframework.data.domain.Sort.by(direction, sortField));

        org.springframework.data.jpa.domain.Specification<Job> spec = JobSpecifications.filter(CurrentUser.get(),
                status, from, to);

        return jobRepository.findAll(spec, pageable);
    }

    private void validatePayload(JobType jobType, Map<String, Object> payload) {
        switch (jobType) {
            case SEND_EMAIL -> validateSendEmail(payload);
            case RESIZE_IMAGE -> validateResizeImage(payload);
            case WEBHOOK -> validateWebhook(payload);
        }
    }

    private void validateSendEmail(Map<String, Object> payload) {
        Object to = payload.get("to");
        if (!(to instanceof String toStr) || !EMAIL_PATTERN.matcher(toStr).matches()) {
            throw new ValidationException("payload.to", "payload.to must be a valid email address");
        }
        Object subject = payload.get("subject");
        if (!(subject instanceof String subjectStr) || subjectStr.isBlank()) {
            throw new ValidationException("payload.subject", "payload.subject is required");
        }
        Object body = payload.get("body");
        if (!(body instanceof String bodyStr) || bodyStr.isBlank()) {
            throw new ValidationException("payload.body", "payload.body is required");
        }
    }

    private void validateResizeImage(Map<String, Object> payload) {
        Object sourceUrl = payload.get("sourceUrl");
        if (!(sourceUrl instanceof String urlStr) || urlStr.isBlank()) {
            throw new ValidationException("payload.sourceUrl", "payload.sourceUrl is required");
        }
        try {
            SsrfGuard.assertSafe(urlStr);
        } catch (SsrfGuard.UnsafeUrlException e) {
            throw new ValidationException("payload.sourceUrl", e.getMessage());
        }

        int width = requirePositiveInt(payload, "targetWidth");
        int height = requirePositiveInt(payload, "targetHeight");
        if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
            throw new ValidationException("payload.targetWidth",
                    "targetWidth/targetHeight must not exceed " + MAX_IMAGE_DIMENSION);
        }
    }

    private void validateWebhook(Map<String, Object> payload) {
        Object url = payload.get("url");
        if (!(url instanceof String urlStr) || urlStr.isBlank()) {
            throw new ValidationException("payload.url", "payload.url is required");
        }
        try {
            SsrfGuard.assertSafe(urlStr);
        } catch (SsrfGuard.UnsafeUrlException e) {
            throw new ValidationException("payload.url", e.getMessage());
        }

        Object method = payload.get("method");
        if (!(method instanceof String methodStr) || !List.of("GET", "POST", "PUT").contains(methodStr.toUpperCase())) {
            throw new ValidationException("payload.method", "payload.method must be one of GET, POST, PUT");
        }
        // headers and body are optional - no further validation needed
    }

    private int requirePositiveInt(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        if (!(value instanceof Number number) || number.intValue() <= 0) {
            throw new ValidationException("payload." + field, "payload." + field + " must be a positive integer");
        }
        return number.intValue();
    }
}