package com.jobflow.worker.messaging;

import com.jobflow.common.config.RabbitMQConfig;
import com.jobflow.common.entity.JobType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class JobRequeuePublisher {

    private final RabbitTemplate rabbitTemplate;

    public JobRequeuePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void requeue(UUID jobId, JobType jobType) {
        String routingKey = "job." + jobType.name();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, Map.of("jobId", jobId.toString()));
    }

    public void sendToDeadLetter(UUID jobId, JobType jobType) {
        // jobflow.dlx is a fanout exchange, so the routing key is ignored
        rabbitTemplate.convertAndSend(RabbitMQConfig.DLX, "", Map.of("jobId", jobId.toString()));
    }
}
