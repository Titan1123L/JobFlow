package com.jobflow.worker.processor;

import com.jobflow.common.email.EmailService;
import com.jobflow.common.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class EmailJobProcessor implements JobProcessor {

    private final EmailService emailService;

    public EmailJobProcessor(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void process(Job job) throws Exception {
        String to = (String) job.getPayload().get("to");
        String subject = (String) job.getPayload().get("subject");
        String body = (String) job.getPayload().get("body");

        emailService.send(to, subject, body);
    }
}