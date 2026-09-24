package com.jobflow.common.repository;

import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    long countByStatus(JobStatus status);

    Optional<Job> findByIdAndUserId(UUID id, UUID userId);

    List<Job> findByStatusAndScheduledAtBefore(JobStatus status, Instant cutoff);

    long countByUserIdAndStatus(UUID userId, JobStatus status);

    long countByUserId(UUID userId);
}