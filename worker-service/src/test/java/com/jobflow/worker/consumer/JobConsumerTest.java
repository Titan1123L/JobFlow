package com.jobflow.worker.consumer;

import com.jobflow.common.entity.Job;
import com.jobflow.common.entity.JobStatus;
import com.jobflow.common.entity.JobType;
import com.jobflow.common.repository.JobRepository;
import com.jobflow.worker.messaging.JobRequeuePublisher;
import com.jobflow.worker.processor.JobProcessor;
import com.jobflow.worker.processor.JobProcessorRegistry;
import com.jobflow.worker.stats.StatsRecorder;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobConsumerTest {

    @Mock private JobRepository jobRepository;
    @Mock private JobProcessorRegistry processorRegistry;
    @Mock private JobRequeuePublisher requeuePublisher;
    @Mock private StatsRecorder statsRecorder;
    @Mock private Channel channel;
    @Mock private JobProcessor jobProcessor;

    private JobConsumer jobConsumer;

    @BeforeEach
    void setUp() {
        jobConsumer = new JobConsumer(jobRepository, processorRegistry, requeuePublisher, statsRecorder, null, null);
        // Zero out backoff timing so failure tests don't actually wait (@Value fields aren't injected outside Spring)
        ReflectionTestUtils.setField(jobConsumer, "initialBackoffSeconds", 0L);
        ReflectionTestUtils.setField(jobConsumer, "multiplier", 1);
    }

    private Message messageWithDeliveryTag(long tag) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(tag);
        return new Message(new byte[0], props);
    }

    @Test
    void completesJobSuccessfully() throws Exception {
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setJobType(JobType.SEND_EMAIL);
        job.setStatus(JobStatus.QUEUED);
        job.setMaxRetries(3);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(processorRegistry.getProcessor(JobType.SEND_EMAIL)).thenReturn(jobProcessor);

        jobConsumer.handleMessage(Map.of("jobId", jobId.toString()), messageWithDeliveryTag(1L), channel);

        assertJobStatus(job, JobStatus.COMPLETED);
        verify(statsRecorder).recordCompletion(anyLong());
        verify(channel).basicAck(1L, false);
    }

    @Test
    void skipsAlreadyCompletedJob_idempotency() throws Exception {
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setStatus(JobStatus.COMPLETED);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        jobConsumer.handleMessage(Map.of("jobId", jobId.toString()), messageWithDeliveryTag(2L), channel);

        // The processor must NEVER run for an already-completed job
        verifyNoInteractions(processorRegistry);
        verify(channel).basicAck(2L, false);
    }

    @Test
    void requeuesOnFailure_whenRetriesRemain() throws Exception {
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setJobType(JobType.SEND_EMAIL);
        job.setStatus(JobStatus.QUEUED);
        job.setRetryCount(0);
        job.setMaxRetries(3);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(processorRegistry.getProcessor(JobType.SEND_EMAIL)).thenReturn(jobProcessor);
        doThrow(new RuntimeException("simulated failure")).when(jobProcessor).process(any());

        jobConsumer.handleMessage(Map.of("jobId", jobId.toString()), messageWithDeliveryTag(3L), channel);

        assertJobStatus(job, JobStatus.QUEUED);
        if (job.getRetryCount() != 1) {
            throw new AssertionError("Expected retryCount=1, got " + job.getRetryCount());
        }
        verify(requeuePublisher).requeue(jobId, JobType.SEND_EMAIL);
        verify(channel).basicAck(3L, false);
    }

    @Test
    void sendsToDeadLetter_whenRetriesExhausted() throws Exception {
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setJobType(JobType.SEND_EMAIL);
        job.setStatus(JobStatus.QUEUED);
        job.setRetryCount(3);   // already at max
        job.setMaxRetries(3);

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(processorRegistry.getProcessor(JobType.SEND_EMAIL)).thenReturn(jobProcessor);
        doThrow(new RuntimeException("simulated failure")).when(jobProcessor).process(any());

        jobConsumer.handleMessage(Map.of("jobId", jobId.toString()), messageWithDeliveryTag(4L), channel);

        assertJobStatus(job, JobStatus.FAILED);
        verify(requeuePublisher).sendToDeadLetter(jobId, JobType.SEND_EMAIL);
        verify(channel).basicAck(4L, false);
    }

    private void assertJobStatus(Job job, JobStatus expected) {
        if (job.getStatus() != expected) {
            throw new AssertionError("Expected status " + expected + " but was " + job.getStatus());
        }
    }
}