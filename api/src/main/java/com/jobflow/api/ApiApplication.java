package com.jobflow.api;

import com.jobflow.common.config.RabbitMQConfig;
import com.jobflow.common.email.EmailService;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.jobflow.common.entity")
@EnableJpaRepositories(basePackages = "com.jobflow.common.repository")
@Import({ RabbitMQConfig.class, EmailService.class })
@EnableScheduling
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
