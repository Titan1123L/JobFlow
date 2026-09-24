package com.jobflow.api;

import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.entity.JobType;
import com.jobflow.common.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class JobRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    // Redirects the app's datasource to the temporary container instead of your real localhost:5432
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JobRepository jobRepository;

    @Test
    void savesAndRetrievesJobWithJsonbPayload() {
        Job job = new Job();
        job.setJobType(JobType.SEND_EMAIL);
        job.setStatus(JobStatus.QUEUED);
        job.setPayload(Map.of("to", "test@example.com", "template", "welcome"));

        Job saved = jobRepository.save(job);

        Optional<Job> found = jobRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getJobType()).isEqualTo(JobType.SEND_EMAIL);
        assertThat(found.get().getPayload()).containsEntry("to", "test@example.com");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void countByStatus_reflectsActualDatabaseState() {
        Job job1 = new Job();
        job1.setJobType(JobType.SEND_EMAIL);
        job1.setStatus(JobStatus.COMPLETED);
        job1.setPayload(Map.of("to", "a@example.com", "template", "x"));
        jobRepository.save(job1);

        Job job2 = new Job();
        job2.setJobType(JobType.SEND_EMAIL);
        job2.setStatus(JobStatus.COMPLETED);
        job2.setPayload(Map.of("to", "b@example.com", "template", "x"));
        jobRepository.save(job2);

        long count = jobRepository.countByStatus(JobStatus.COMPLETED);

        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}