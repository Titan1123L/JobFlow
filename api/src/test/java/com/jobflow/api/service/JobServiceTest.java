package com.jobflow.api.service;

import com.jobflow.api.exception.ValidationException;
import com.jobflow.api.messaging.JobPublisher;
import com.jobflow.api.security.CurrentUser;
import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobType;
import com.jobflow.common.repository.JobRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobPublisher jobPublisher;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setUp() {
        // createJob/getJob/etc. now read the current user from CurrentUser -
        // fake one in, since there's no real HTTP request in a unit test
        CurrentUser.set(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void createJob_withValidSendEmailPayload_savesAndPublishes() {
        Map<String, Object> payload = Map.of("to", "test@example.com", "subject", "hi", "body", "hello there");

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.createJob(JobType.SEND_EMAIL, payload, null, false);

        assertThat(result.getJobType()).isEqualTo(JobType.SEND_EMAIL);
        assertThat(result.getPayload()).isEqualTo(payload);

        verify(jobRepository).save(any(Job.class));
        verify(jobPublisher).publish(any(), any());
    }

    @Test
    void createJob_withInvalidEmail_throwsValidationException() {
        Map<String, Object> payload = Map.of("to", "not-an-email", "subject", "hi", "body", "hello");

        assertThatThrownBy(() -> jobService.createJob(JobType.SEND_EMAIL, payload, null, false))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("payload.to");
    }

    @Test
    void createJob_withMissingSubject_throwsValidationException() {
        Map<String, Object> payload = Map.of("to", "test@example.com", "body", "hello");

        assertThatThrownBy(() -> jobService.createJob(JobType.SEND_EMAIL, payload, null, false))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("payload.subject");
    }

    @Test
    void createJob_withValidResizeImagePayload_savesAndPublishes() {
        Map<String, Object> payload = Map.of(
                "sourceUrl", "https://example.com/photo.jpg",
                "targetWidth", 800,
                "targetHeight", 600
        );

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.createJob(JobType.RESIZE_IMAGE, payload, null, false);

        assertThat(result.getJobType()).isEqualTo(JobType.RESIZE_IMAGE);
        verify(jobPublisher).publish(any(), any());
    }

    @Test
    void createJob_withOversizedDimensions_throwsValidationException() {
        Map<String, Object> payload = Map.of(
                "sourceUrl", "https://example.com/photo.jpg",
                "targetWidth", 5000,
                "targetHeight", 600
        );

        assertThatThrownBy(() -> jobService.createJob(JobType.RESIZE_IMAGE, payload, null, false))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("targetWidth");
    }

    @Test
    void createJob_withNegativeDimensions_throwsValidationException() {
        Map<String, Object> payload = Map.of(
                "sourceUrl", "https://example.com/photo.jpg",
                "targetWidth", -100,
                "targetHeight", 600
        );

        assertThatThrownBy(() -> jobService.createJob(JobType.RESIZE_IMAGE, payload, null, false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createJob_withPastScheduledAt_throwsValidationException() {
        Map<String, Object> payload = Map.of("to", "test@example.com", "subject", "hi", "body", "hello");
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);

        assertThatThrownBy(() -> jobService.createJob(JobType.SEND_EMAIL, payload, past, false))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("scheduledAt");
    }

    @Test
    void createJob_withFutureScheduledAt_savesAsScheduledWithoutPublishing() {
        Map<String, Object> payload = Map.of("to", "test@example.com", "subject", "hi", "body", "hello");
        Instant future = Instant.now().plus(1, ChronoUnit.HOURS);

        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.createJob(JobType.SEND_EMAIL, payload, future, false);

        assertThat(result.getStatus()).isEqualTo(com.jobflow.common.entity.JobStatus.SCHEDULED);
        // A scheduled job must NOT be published yet - only the scheduler does that once it's due
        verifyNoInteractions(jobPublisher);
    }
}