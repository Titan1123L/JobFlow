package com.jobflow.worker.consumer;

import com.jobflow.common.config.RabbitMQConfig;
import com.jobflow.common.email.EmailService;
import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.entity.User;
import com.jobflow.common.repository.JobRepository;
import com.jobflow.common.repository.UserRepository;
import com.jobflow.worker.messaging.JobRequeuePublisher;
import com.jobflow.worker.processor.JobProcessorRegistry;
import com.jobflow.worker.stats.StatsRecorder;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class JobConsumer {

    private final JobRepository jobRepository;
    private final JobProcessorRegistry processorRegistry;
    private final JobRequeuePublisher requeuePublisher;
    private final StatsRecorder statsRecorder;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${jobflow.retry.initial-backoff-seconds:5}")
    private long initialBackoffSeconds;

    @Value("${jobflow.retry.multiplier:6}")
    private int multiplier;

    public JobConsumer(JobRepository jobRepository,
                        JobProcessorRegistry processorRegistry,
                        JobRequeuePublisher requeuePublisher,
                        StatsRecorder statsRecorder,
                        UserRepository userRepository,
                        EmailService emailService) {
        this.jobRepository = jobRepository;
        this.processorRegistry = processorRegistry;
        this.requeuePublisher = requeuePublisher;
        this.statsRecorder = statsRecorder;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_QUEUE)
    public void handleMessage(Map<String, String> body, Message message, Channel channel) throws IOException {
        UUID jobId = UUID.fromString(body.get("jobId"));
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        Optional<Job> maybeJob = jobRepository.findById(jobId);
        if (maybeJob.isEmpty()) {
            System.out.println("Job " + jobId + " not found, skipping");
            channel.basicAck(deliveryTag, false);
            return;
        }

        Job job = maybeJob.get();

        if (job.getStatus() == JobStatus.COMPLETED) {
            System.out.println("Job " + jobId + " already COMPLETED, skipping (duplicate delivery)");
            channel.basicAck(deliveryTag, false);
            return;
        }

        job.setStatus(JobStatus.PROCESSING);
        job.setStartedAt(Instant.now());
        jobRepository.save(job);

        try {
            processorRegistry.getProcessor(job.getJobType()).process(job);

            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);

            long durationMs = Duration.between(job.getStartedAt(), job.getCompletedAt()).toMillis();
            statsRecorder.recordCompletion(durationMs);

            if (job.isNotifyOnCompletion()) {
                sendNotification(job, "completed successfully", null);
            }

            channel.basicAck(deliveryTag, false);
            System.out.println("Job " + jobId + " COMPLETED");

        } catch (Exception ex) {
            handleFailure(job, ex.getMessage(), channel, deliveryTag);
        }
    }

    private void handleFailure(Job job, String errorMessage, Channel channel, long deliveryTag) throws IOException {
        int currentRetryCount = job.getRetryCount();

        if (currentRetryCount < job.getMaxRetries()) {
            long delaySeconds = (long) (initialBackoffSeconds * Math.pow(multiplier, currentRetryCount));

            job.setRetryCount(currentRetryCount + 1);
            job.setStatus(JobStatus.QUEUED);
            job.setErrorMessage(errorMessage);
            jobRepository.save(job);

            System.out.println("Job " + job.getId() + " failed (retry " + (currentRetryCount + 1)
                    + "/" + job.getMaxRetries() + "), waiting " + delaySeconds + "s before requeue: " + errorMessage);

            try {
                Thread.sleep(delaySeconds * 1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            requeuePublisher.requeue(job.getId(), job.getJobType());
            channel.basicAck(deliveryTag, false);

        } else {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            jobRepository.save(job);

            requeuePublisher.sendToDeadLetter(job.getId(), job.getJobType());
            channel.basicAck(deliveryTag, false);

            System.out.println("Job " + job.getId() + " FAILED permanently after "
                    + job.getRetryCount() + " retries, sent to DLQ: " + errorMessage);

            if (job.isNotifyOnCompletion()) {
                sendNotification(job, "failed permanently", errorMessage);
            }
        }
    }

    private void sendNotification(Job job, String outcome, String errorDetail) {
        try {
            Optional<User> maybeUser = userRepository.findById(job.getUserId());
            if (maybeUser.isEmpty()) {
                return; // user deleted their account or similar edge case - nothing to notify
            }
            String to = maybeUser.get().getEmail();
            String subject = "JobFlow: your " + job.getJobType() + " job " + outcome;
            String body = "Your job (" + job.getId() + ") of type " + job.getJobType() + " " + outcome + "."
                    + (errorDetail != null ? "\n\nError: " + errorDetail : "");

            emailService.send(to, subject, body);
        } catch (Exception ex) {
            // A failed notification must never fail the job itself - explicitly best-effort
            System.out.println("Could not send notification email for job " + job.getId() + ": " + ex.getMessage());
        }
    }
}