package com.jobflow.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "jobflow.exchange";
    public static final String DLX = "jobflow.dlx";
    public static final String JOB_QUEUE = "job_queue";
    public static final String DEAD_LETTER_QUEUE = "dead_letter_queue";

    @Bean
    public DirectExchange jobExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        // Fanout: anything dead-lettered goes to the DLQ regardless of its original
        // routing key
        return new FanoutExchange(DLX, true, false);
    }

    @Bean
    public Queue jobQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX);
        return new Queue(JOB_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding sendEmailBinding(Queue jobQueue, DirectExchange jobExchange) {
        return BindingBuilder.bind(jobQueue).to(jobExchange).with("job.SEND_EMAIL");
    }

    @Bean
    public Binding resizeImageBinding(Queue jobQueue, DirectExchange jobExchange) {
        return BindingBuilder.bind(jobQueue).to(jobExchange).with("job.RESIZE_IMAGE");
    }

    @Bean
    public Binding webhookBinding(Queue jobQueue, DirectExchange jobExchange) {
        return BindingBuilder.bind(jobQueue).to(jobExchange).with("job.WEBHOOK");
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, FanoutExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}