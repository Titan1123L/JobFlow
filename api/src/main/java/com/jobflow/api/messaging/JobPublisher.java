package com.jobflow.api.messaging;

import com.jobflow.common.config.RabbitMQConfig;
import com.jobflow.common.entity.JobType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class JobPublisher {

    private final RabbitTemplate rabbitTemplate;

    public JobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(UUID jobId, JobType jobType) {
        String routingKey = "job." + jobType.name();
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                routingKey,
                Map.of("jobId", jobId.toString())
        );
    }
}