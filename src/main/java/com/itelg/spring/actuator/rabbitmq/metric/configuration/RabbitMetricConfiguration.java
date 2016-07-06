package com.itelg.spring.actuator.rabbitmq.metric.configuration;

import java.util.List;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.itelg.spring.actuator.rabbitmq.metric.RabbitQueueMetrics;

@Configuration
public class RabbitMetricConfiguration
{
    @Bean
    public RabbitQueueMetrics rabbitQueueMetrics(List<Queue> queues)
    {
        return new RabbitQueueMetrics(queues);
    }
}