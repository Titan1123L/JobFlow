package com.jobflow.api.controller;

import com.jobflow.api.dto.CreateJobRequest;
import com.jobflow.api.dto.JobDetailResponse;
import com.jobflow.api.dto.JobSubmittedResponse;
import com.jobflow.api.service.JobService;
import com.jobflow.common.entity.Job;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobSubmittedResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        Job job = jobService.createJob(request.getJobType(), request.getPayload(),
                request.getScheduledAt(), request.isNotifyOnCompletion());
        JobSubmittedResponse body = new JobSubmittedResponse(job.getId(), job.getStatus(), job.getCreatedAt());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<com.jobflow.api.dto.JobStatusResponse> cancelJob(@PathVariable UUID id) {
        Job job = jobService.cancelJob(id);
        return ResponseEntity.ok(new com.jobflow.api.dto.JobStatusResponse(job.getId(), job.getStatus()));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<com.jobflow.api.dto.JobStatusResponse> retryJob(@PathVariable UUID id) {
        Job job = jobService.retryJob(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new com.jobflow.api.dto.JobStatusResponse(job.getId(), job.getStatus(), job.getRetryCount()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDetailResponse> getJob(@PathVariable UUID id) {
        Job job = jobService.getJob(id);
        return ResponseEntity.ok(new JobDetailResponse(job));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<org.springframework.core.io.Resource> getJobResult(@PathVariable UUID id) {
        java.io.File file = jobService.getJobResultFile(id);
        org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<com.jobflow.api.dto.PagedJobResponse> listJobs(
            @RequestParam(required = false) com.jobflow.common.entity.JobStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.Instant from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        org.springframework.data.domain.Page<Job> jobPage = jobService.listJobs(status, from, to, page, size, sort);
        return ResponseEntity.ok(new com.jobflow.api.dto.PagedJobResponse(jobPage));
    }
}