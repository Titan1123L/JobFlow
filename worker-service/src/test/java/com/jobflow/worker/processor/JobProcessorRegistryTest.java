package com.jobflow.worker.processor;

import com.jobflow.common.email.EmailService;
import com.jobflow.common.entity.JobType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JobProcessorRegistryTest {

    @Test
    void returnsEmailProcessorForSendEmail() {
        EmailJobProcessor emailProcessor = new EmailJobProcessor(mock(EmailService.class));
        ImageResizeJobProcessor imageProcessor = new ImageResizeJobProcessor();
        WebhookJobProcessor webhookProcessor = new WebhookJobProcessor();
        JobProcessorRegistry registry = new JobProcessorRegistry(emailProcessor, imageProcessor, webhookProcessor);

        assertThat(registry.getProcessor(JobType.SEND_EMAIL)).isSameAs(emailProcessor);
    }

    @Test
    void returnsImageProcessorForResizeImage() {
        EmailJobProcessor emailProcessor = new EmailJobProcessor(mock(EmailService.class));
        ImageResizeJobProcessor imageProcessor = new ImageResizeJobProcessor();
                WebhookJobProcessor webhookProcessor = new WebhookJobProcessor();
        JobProcessorRegistry registry = new JobProcessorRegistry(emailProcessor, imageProcessor, webhookProcessor);

        assertThat(registry.getProcessor(JobType.RESIZE_IMAGE)).isSameAs(imageProcessor);
    }
}
