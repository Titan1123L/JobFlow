package com.jobflow.worker;

import com.jobflow.common.config.RabbitMQConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.jobflow.common.email.EmailService;

@SpringBootApplication
@EntityScan(basePackages = "com.jobflow.common.entity")
@EnableJpaRepositories(basePackages = "com.jobflow.common.repository")
@Import({ RabbitMQConfig.class, EmailService.class })
public class WorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}