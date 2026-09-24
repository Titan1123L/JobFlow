package com.jobflow.worker;

import com.jobflow.common.config.RabbitMQConfig;
import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.entity.JobType;
import com.jobflow.common.repository.JobRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class WorkerEndToEndTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void jobPublishedToQueue_isProcessedByRealWorkerAndCompleted() {
        // Save a job directly, exactly like the API does before publishing
        Job job = new Job();
        job.setJobType(JobType.SEND_EMAIL);
        job.setStatus(JobStatus.QUEUED);
        job.setPayload(Map.of("to", "test@example.com", "template", "welcome"));
        job = jobRepository.save(job);
        UUID jobId = job.getId();

        // Publish exactly like JobPublisher does
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                "job.SEND_EMAIL",
                Map.of("jobId", jobId.toString())
        );

        // The real @RabbitListener in this app should pick it up asynchronously -
        // poll until it completes, rather than a fixed sleep (which would be flaky)
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Job updated = jobRepository.findById(jobId).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(JobStatus.COMPLETED);
                });
    }
}