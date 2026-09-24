package com.jobflow.api.scheduler;

import com.jobflow.api.messaging.JobPublisher;
import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.repository.JobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class JobScheduler {

    private final JobRepository jobRepository;
    private final JobPublisher jobPublisher;

    public JobScheduler(JobRepository jobRepository, JobPublisher jobPublisher) {
        this.jobRepository = jobRepository;
        this.jobPublisher = jobPublisher;
    }

    @Scheduled(fixedDelayString = "${jobflow.scheduler.poll-interval-seconds:10}000")
    public void promoteDueJobs() {
        List<Job> dueJobs = jobRepository.findByStatusAndScheduledAtBefore(JobStatus.SCHEDULED, Instant.now());

        for (Job job : dueJobs) {
            job.setStatus(JobStatus.QUEUED);
            jobRepository.save(job);
            jobPublisher.publish(job.getId(), job.getJobType());
            System.out.println("Scheduler promoted job " + job.getId() + " from SCHEDULED to QUEUED");
        }
    }
}