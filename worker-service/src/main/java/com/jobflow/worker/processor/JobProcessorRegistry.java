package com.jobflow.worker.processor;

import com.jobflow.common.entity.JobType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JobProcessorRegistry {

    private final Map<JobType, JobProcessor> processors;

    public JobProcessorRegistry(EmailJobProcessor emailProcessor,
                                 ImageResizeJobProcessor imageProcessor,
                                 WebhookJobProcessor webhookProcessor) {
        this.processors = Map.of(
                JobType.SEND_EMAIL, emailProcessor,
                JobType.RESIZE_IMAGE, imageProcessor,
                JobType.WEBHOOK, webhookProcessor
        );
    }

    public JobProcessor getProcessor(JobType jobType) {
        JobProcessor processor = processors.get(jobType);
        if (processor == null) {
            throw new IllegalStateException("No processor registered for job type: " + jobType);
        }
        return processor;
    }
}